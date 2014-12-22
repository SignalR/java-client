/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.transport;

import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.ConnectionBase;
import microsoft.aspnet.signalr.client.Constants;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.http.HttpConnection;
import microsoft.aspnet.signalr.client.http.HttpConnectionFuture.ResponseCallback;
import microsoft.aspnet.signalr.client.http.Request;
import microsoft.aspnet.signalr.client.http.Response;

/**
 * HttpClientTransport implementation over Server Sent Events implementation
 */
public class ServerSentEventsTransport extends HttpClientTransport {

    private static final int SSE_DATA_PREFIX_LENGTH = 6;
    private static final String DATA_INITIALIZED = "data: initialized";
    private static final String END_OF_SSE_MESSAGE = "\n\n";

    private SignalRFuture<Void> mConnectionFuture;

    /**
     * Initializes the transport with a logger
     * 
     * @param logger
     *            Logger to log actions
     */
    public ServerSentEventsTransport(Logger logger) {
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
    public ServerSentEventsTransport(Logger logger, HttpConnection httpConnection) {
        super(logger, httpConnection);
    }

    @Override
    public String getName() {
        return "serverSentEvents";
    }

    @Override
    public boolean supportKeepAlive() {
        return true;
    }

    @Override
    public SignalRFuture<Void> start(ConnectionBase connection, ConnectionType connectionType, final DataResultCallback callback) {
        log("Start the communication with the server", LogLevel.Information);
        String url = connection.getUrl() + (connectionType == ConnectionType.InitialConnection ? "connect" : "reconnect")
                + TransportHelper.getReceiveQueryString(this, connection);

        Request get = new Request(Constants.HTTP_GET);

        get.setUrl(url);
        get.setHeaders(connection.getHeaders());
        get.addHeader("Accept", "text/event-stream");

        connection.prepareRequest(get);

        log("Execute the request", LogLevel.Verbose);
        mConnectionFuture = mHttpConnection.execute(get, new ResponseCallback() {

            @Override
            public void onResponse(Response response) {
                try {
                    log("Response received", LogLevel.Verbose);
                    throwOnInvalidStatusCode(response);

                    mConnectionFuture.setResult(null);

                    StringBuilder buffer = new StringBuilder();
                    String line = null;

                    log("Read the response content by line", LogLevel.Verbose);
                    while ((line = response.readLine()) != null) {
                        buffer.append(line);
                        buffer.append("\n");
                        String currentData = buffer.toString();
                        if (currentData.endsWith(END_OF_SSE_MESSAGE)) {
                            currentData = currentData.trim();
                            log("Found new data: " + currentData, LogLevel.Verbose);
                            if (currentData.equals(DATA_INITIALIZED)) {
                                log("Initialization message found", LogLevel.Verbose);
                            } else {
                                String content = currentData.substring(SSE_DATA_PREFIX_LENGTH).trim();

                                log("Trigger onData: " + content, LogLevel.Verbose);
                                callback.onData(content);
                            }

                            buffer = new StringBuilder();
                        }
                    }

                    // if the request finishes, it means the connection was finalized
                } catch (Throwable e) {
                    if (!mConnectionFuture.isCancelled()) {
                        mConnectionFuture.triggerError(e);
                    }
                }
            }
        });

        return mConnectionFuture;
    }
}
