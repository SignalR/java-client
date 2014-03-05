/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.transport;

import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.ConnectionBase;
import microsoft.aspnet.signalr.client.Constants;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.UpdateableCancellableFuture;
import microsoft.aspnet.signalr.client.http.HttpConnection;
import microsoft.aspnet.signalr.client.http.HttpConnectionFuture;
import microsoft.aspnet.signalr.client.http.HttpConnectionFuture.ResponseCallback;
import microsoft.aspnet.signalr.client.http.Request;
import microsoft.aspnet.signalr.client.http.Response;

/**
 * HttpClientTransport implementation over long polling
 */
public class LongPollingTransport extends HttpClientTransport {
    private UpdateableCancellableFuture<Void> mConnectionFuture;
    private Object mPollSync = new Object();

    /**
     * Initializes the transport
     * 
     * @param logger
     *            logger to log actions
     */
    public LongPollingTransport(Logger logger) {
        super(logger);
    }

    /**
     * Initializes the transport with a logger
     * 
     * @param logger
     *            Logger to log actions
     * @param httpConnection
     *            HttpConnection for the transport
     */
    public LongPollingTransport(Logger logger, HttpConnection httpConnection) {
        super(logger, httpConnection);
    }

    @Override
    public String getName() {
        return "longPolling";
    }

    @Override
    public boolean supportKeepAlive() {
        return false;
    }

    @Override
    public SignalRFuture<Void> start(ConnectionBase connection, ConnectionType connectionType, DataResultCallback callback) {
        return poll(connection, connectionType == ConnectionType.InitialConnection ? "connect" : "reconnect", callback);
    }

    /**
     * Polls the server
     * 
     * @param connection
     *            the implemented connection
     * @param connectionUrl
     *            the connection action url
     * @param callback
     *            callback to invoke when data is received
     * @return Future for the operation
     */
    private SignalRFuture<Void> poll(final ConnectionBase connection, final String connectionUrl, final DataResultCallback callback) {
        synchronized (mPollSync) {
            log("Start the communication with the server", LogLevel.Information);
            String url = connection.getUrl() + connectionUrl + TransportHelper.getReceiveQueryString(this, connection);

            Request get = new Request(Constants.HTTP_GET);

            get.setUrl(url);
            get.setHeaders(connection.getHeaders());

            connection.prepareRequest(get);

            log("Execute the request", LogLevel.Verbose);
            mConnectionFuture = new UpdateableCancellableFuture<Void>(null);

            final HttpConnectionFuture future = mHttpConnection.execute(get, new ResponseCallback() {

                @Override
                public void onResponse(Response response) {
                    synchronized (mPollSync) {
                        try {
                            throwOnInvalidStatusCode(response);

                            if (connectionUrl != "poll") {
                                mConnectionFuture.setResult(null);
                            }
                            log("Response received", LogLevel.Verbose);

                            log("Read response to the end", LogLevel.Verbose);
                            String responseData = response.readToEnd();
                            if (responseData != null) {
                                responseData = responseData.trim();
                            }

                            log("Trigger onData with data: " + responseData, LogLevel.Verbose);
                            callback.onData(responseData);

                            if (!mConnectionFuture.isCancelled() && connection.getState() == ConnectionState.Connected) {
                                log("Continue polling", LogLevel.Verbose);
                                mConnectionFuture.setFuture(poll(connection, "poll", callback));
                            }
                        } catch (Throwable e) {
                            if (!mConnectionFuture.isCancelled()) {
                                log(e);
                                mConnectionFuture.triggerError(e);
                            }
                        }
                    }
                }
            });

            future.onTimeout(new ErrorCallback() {

                @Override
                public void onError(Throwable error) {
                    synchronized (mPollSync) {
                        if (connectionUrl.equals("poll")) {
                            // if the poll request timed out, it should re-poll
                            mConnectionFuture.setFuture(poll(connection, "poll", callback));
                        } else {
                            future.triggerError(error);
                        }
                    }
                }
            });

            future.onError(new ErrorCallback() {

                @Override
                public void onError(Throwable error) {
                    synchronized (mPollSync) {
                        mConnectionFuture.triggerError(error);
                    }
                }
            });

            mConnectionFuture.setFuture(future);

            return mConnectionFuture;
        }
    }
}
