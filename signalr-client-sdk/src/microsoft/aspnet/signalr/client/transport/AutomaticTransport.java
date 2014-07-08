/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.transport;

import java.util.ArrayList;
import java.util.List;

import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.ConnectionBase;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.NullLogger;
import microsoft.aspnet.signalr.client.http.HttpConnection;

/**
 * ClientTransport implementation that selects the best available transport
 */
public class AutomaticTransport extends HttpClientTransport {

    private List<ClientTransport> mTransports;
    private ClientTransport mRealTransport;

    /**
     * Initializes the transport with a NullLogger
     */
    public AutomaticTransport() {
        this(new NullLogger());
    }

    /**
     * Initializes the transport with a logger
     * 
     * @param logger
     *            logger to log actions
     */
    public AutomaticTransport(Logger logger) {
        super(logger);
        initialize(logger);
    }

    /**
     * Initializes the transport with a logger and an httpConnection
     * 
     * @param logger
     *            the logger
     * @param httpConnection
     *            the httpConnection
     */
    public AutomaticTransport(Logger logger, HttpConnection httpConnection) {
        super(logger, httpConnection);
        initialize(logger);
    }

    private void initialize(Logger logger) {
        mTransports = new ArrayList<ClientTransport>();
        mTransports.add(new WebsocketTransport(logger));
        mTransports.add(new ServerSentEventsTransport(logger));
        mTransports.add(new LongPollingTransport(logger));
    }

    @Override
    public String getName() {
        if (mRealTransport == null) {
            return "AutomaticTransport";
        }

        return mRealTransport.getName();
    }

    @Override
    public boolean supportKeepAlive() {
        if (mRealTransport != null) {
            return mRealTransport.supportKeepAlive();
        }

        return false;
    }

    private void resolveTransport(final ConnectionBase connection, final ConnectionType connectionType, final DataResultCallback callback,
            final int currentTransportIndex, final SignalRFuture<Void> startFuture) {
        final ClientTransport currentTransport = mTransports.get(currentTransportIndex);

        final SignalRFuture<Void> transportStart = currentTransport.start(connection, connectionType, callback);

        transportStart.done(new Action<Void>() {

            @Override
            public void run(Void obj) throws Exception {
                // set the real transport and trigger end the start future
                mRealTransport = currentTransport;
                startFuture.setResult(null);
            }
        });

        final ErrorCallback handleError = new ErrorCallback() {

            @Override
            public void onError(Throwable error) {

                // if the transport is already started, forward the error
                if (mRealTransport != null) {
                    startFuture.triggerError(error);
                    return;
                }

                log(String.format("Auto: Faild to connect using transport %s. %s", currentTransport.getName(), error.toString()), LogLevel.Information);
                int next = currentTransportIndex + 1;
                if (next < mTransports.size()) {
                    resolveTransport(connection, connectionType, callback, next, startFuture);
                } else {
                    startFuture.triggerError(error);
                }
            }
        };

        transportStart.onError(handleError);
        
        startFuture.onCancelled(new Runnable() {

            @Override
            public void run() {
                // if the transport is already started, forward the cancellation
                if (mRealTransport != null) {
                    transportStart.cancel();
                    return;
                }

                handleError.onError(new Exception("Operation cancelled"));
            }
        });
    }

    @Override
    public SignalRFuture<Void> start(final ConnectionBase connection, final ConnectionType connectionType, final DataResultCallback callback) {
        SignalRFuture<Void> startFuture = new SignalRFuture<Void>();

        resolveTransport(connection, connectionType, callback, 0, startFuture);

        return startFuture;
    }

    @Override
    public SignalRFuture<Void> send(ConnectionBase connection, String data, DataResultCallback callback) {
        if (mRealTransport != null) {
            return mRealTransport.send(connection, data, callback);
        }

        return null;
    }

    @Override
    public SignalRFuture<Void> abort(ConnectionBase connection) {
        if (mRealTransport != null) {
            return mRealTransport.abort(connection);
        }

        return null;
    }
}
