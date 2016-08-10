/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import microsoft.aspnet.signalr.client.http.Request;
import microsoft.aspnet.signalr.client.transport.AutomaticTransport;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.ConnectionType;
import microsoft.aspnet.signalr.client.transport.DataResultCallback;
import microsoft.aspnet.signalr.client.transport.NegotiationResponse;
import microsoft.aspnet.signalr.client.transport.TransportHelper;
import microsoft.aspnet.signalr.client.Logger;

/**
 * Represents a basic SingalR connection
 */
public class Connection implements ConnectionBase {

    public static final Version PROTOCOL_VERSION = new Version("1.3");

    private Logger mLogger;

    private String mUrl;

    private String mConnectionToken;

    private String mConnectionId;

    private String mMessageId;

    private String mGroupsToken;

    private Credentials mCredentials;

    private String mQueryString;

    private Map<String, String> mHeaders = new HashMap<String, String>();

    private UpdateableCancellableFuture<Void> mConnectionFuture;

    private boolean mAborting = false;

    private SignalRFuture<Void> mAbortFuture = new SignalRFuture<Void>();

    private Runnable mOnReconnecting;

    private Runnable mOnReconnected;

    private Runnable mOnConnected;

    private MessageReceivedHandler mOnReceived;

    private ErrorCallback mOnError;

    private Runnable mOnConnectionSlow;

    private Runnable mOnClosed;

    private StateChangedCallback mOnStateChanged;

    private ClientTransport mTransport;

    private HeartbeatMonitor mHeartbeatMonitor;

    private KeepAliveData mKeepAliveData;

    protected ConnectionState mState;

    protected JsonParser mJsonParser;

    protected Gson mGson;

    private Object mStateLock = new Object();

    private Object mStartLock = new Object();

    /**
     * Initializes the connection with an URL
     * 
     * @param url
     *            The connection URL
     */
    public Connection(String url) {
        this(url, (String) null);
    }

    /**
     * Initializes the connection with an URL and a query string
     * 
     * @param url
     *            The connection URL
     * @param queryString
     *            The connection query string
     */
    public Connection(String url, String queryString) {
        this(url, queryString, new NullLogger());
    }

    /**
     * Initializes the connection with an URL and a logger
     * 
     * @param url
     *            The connection URL
     * @param logger
     *            The connection logger
     */
    public Connection(String url, Logger logger) {
        this(url, null, logger);
    }

    /**
     * Initializes the connection with an URL, a query string and a Logger
     * 
     * @param url
     *            The connection URL
     * @param queryString
     *            The connection query string
     * @param logger
     *            The connection logger
     */
    public Connection(String url, String queryString, Logger logger) {
        if (url == null) {
            throw new IllegalArgumentException("URL cannot be null");
        }

        if (logger == null) {
            throw new IllegalArgumentException("Logger cannot be null");
        }

        if (!url.endsWith("/")) {
            url += "/";
        }

        log("Initialize the connection", LogLevel.Information);
        log("Connection data: " + url + " - " + queryString == null ? "" : queryString, LogLevel.Verbose);

        mUrl = url;
        mQueryString = queryString;
        mLogger = logger;
        mJsonParser = new JsonParser();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new DateSerializer());
        mGson = gsonBuilder.create();
        mState = ConnectionState.Disconnected;
    }

    @Override
    public Logger getLogger() {
        return mLogger;
    }

    @Override
    public ConnectionState getState() {
        return mState;
    }

    @Override
    public String getUrl() {
        return mUrl;
    }

    @Override
    public String getConnectionToken() {
        return mConnectionToken;
    }

    @Override
    public String getConnectionId() {
        return mConnectionId;
    }

    @Override
    public String getQueryString() {
        return mQueryString;
    }

    @Override
    public String getMessageId() {
        return mMessageId;
    }

    @Override
    public void setMessageId(String messageId) {
        mMessageId = messageId;
    }

    @Override
    public String getGroupsToken() {
        return mGroupsToken;
    }

    @Override
    public void setGroupsToken(String groupsToken) {
        mGroupsToken = groupsToken;
    }
    
    @Override
    public void addHeader(String headerName, String headerValue) {
        mHeaders.put(headerName, headerValue);
    }

    @Override
    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    @Override
    public void reconnecting(Runnable handler) {
        mOnReconnecting = handler;
    }

    @Override
    public void reconnected(Runnable handler) {
        mOnReconnected = handler;
    }

    @Override
    public void connected(Runnable handler) {
        mOnConnected = handler;
    }

    @Override
    public void error(ErrorCallback handler) {
        mOnError = handler;
    }

    @Override
    public void received(MessageReceivedHandler handler) {
        mOnReceived = handler;
    }

    @Override
    public void connectionSlow(Runnable handler) {
        mOnConnectionSlow = handler;
    }

    @Override
    public void closed(Runnable handler) {
        mOnClosed = handler;
    }

    @Override
    public void stateChanged(StateChangedCallback handler) {
        mOnStateChanged = handler;
    }

    /**
     * Starts the connection using the best available transport
     * 
     * @return A Future for the operation
     */
    public SignalRFuture<Void> start() {
        return start(new AutomaticTransport(mLogger));
    }

    /**
     * Sends a serialized object
     * 
     * @param object
     *            The object to send. If the object is a JsonElement, its string
     *            representation is sent. Otherwise, the object is serialized to
     *            Json.
     * @return A Future for the operation
     */
    public SignalRFuture<Void> send(Object object) {
        String data = null;
        if (object != null) {
            if (object instanceof JsonElement) {
                data = object.toString();
            } else {
                data = mGson.toJson(object);
            }
        }

        return send(data);
    }

    @Override
    public SignalRFuture<Void> send(String data) {
        log("Sending: " + data, LogLevel.Information);

        if (mState == ConnectionState.Disconnected || mState == ConnectionState.Connecting) {
            onError(new InvalidStateException(mState), false);
            return new SignalRFuture<Void>();
        }

        final Connection that = this;

        log("Invoking send on transport", LogLevel.Verbose);
        SignalRFuture<Void> future = mTransport.send(this, data, new DataResultCallback() {

            @Override
            public void onData(String data) {
                that.processReceivedData(data);
            }
        });

        handleFutureError(future, false);
        return future;
    }

    /**
     * Handles a Future error, invoking the connection onError event
     * 
     * @param future
     *            The future to handle
     * @param mustCleanCurrentConnection
     *            True if the connection must be cleaned when an error happens
     */
    private void handleFutureError(SignalRFuture<?> future, final boolean mustCleanCurrentConnection) {
        final Connection that = this;

        future.onError(new ErrorCallback() {

            @Override
            public void onError(Throwable error) {
                that.onError(error, mustCleanCurrentConnection);
            }
        });
    }

    @Override
    public SignalRFuture<Void> start(final ClientTransport transport) {
        synchronized (mStartLock) {
            log("Entered startLock in start", LogLevel.Verbose);
            if (!changeState(ConnectionState.Disconnected, ConnectionState.Connecting)) {
                log("Couldn't change state from disconnected to connecting.", LogLevel.Verbose);
                return mConnectionFuture;
            }

            log("Start the connection, using " + transport.getName() + " transport", LogLevel.Information);

            mTransport = transport;
            mConnectionFuture = new UpdateableCancellableFuture<Void>(null);
            handleFutureError(mConnectionFuture, true);

            log("Start negotiation", LogLevel.Verbose);
            SignalRFuture<NegotiationResponse> negotiationFuture = transport.negotiate(this);

            try {
                negotiationFuture.done(new Action<NegotiationResponse>() {

                    @Override
                    public void run(NegotiationResponse negotiationResponse) throws Exception {
                        log("Negotiation completed", LogLevel.Information);
                        if (!verifyProtocolVersion(negotiationResponse.getProtocolVersion())) {
                            Exception err = new InvalidProtocolVersionException(negotiationResponse.getProtocolVersion()); 
                            onError(err, true);
                            mConnectionFuture.triggerError(err);
                            return;
                        }

                        mConnectionId = negotiationResponse.getConnectionId();
                        mConnectionToken = negotiationResponse.getConnectionToken();
                        log("ConnectionId: " + mConnectionId, LogLevel.Verbose);
                        log("ConnectionToken: " + mConnectionToken, LogLevel.Verbose);

                        KeepAliveData keepAliveData = null;
                        if (negotiationResponse.getKeepAliveTimeout() > 0) {
                            log("Keep alive timeout: " + negotiationResponse.getKeepAliveTimeout(), LogLevel.Verbose);
                            keepAliveData = new KeepAliveData((long) (negotiationResponse.getKeepAliveTimeout() * 1000));
                        }

                        startTransport(keepAliveData, false);
                    }
                });
                
                negotiationFuture.onError(new ErrorCallback() {
                    
                    @Override
                    public void onError(Throwable error) {
                        mConnectionFuture.triggerError(error);
                    }
                });
                
            } catch (Exception e) {
                onError(e, true);
            }

            handleFutureError(negotiationFuture, true);
            mConnectionFuture.setFuture(negotiationFuture);

            return mConnectionFuture;
        }
    }

    /**
     * Changes the connection state
     * 
     * @param oldState
     *            The expected old state
     * @param newState
     *            The new state
     * @return True, if the state was changed
     */
    private boolean changeState(ConnectionState oldState, ConnectionState newState) {
        synchronized (mStateLock) {
            if (mState == oldState) {
                mState = newState;
                if (mOnStateChanged != null) {
                    try {
                        mOnStateChanged.stateChanged(oldState, newState);
                    } catch (Throwable e) {
                        onError(e, false);
                    }
                }
                return true;
            }

            return false;
        }
    }

    @Override
    public Credentials getCredentials() {
        return mCredentials;
    }

    @Override
    public void setCredentials(Credentials credentials) {
        mCredentials = credentials;
    }

    @Override
    public void prepareRequest(Request request) {
        if (mCredentials != null) {
            log("Preparing request with credentials data", LogLevel.Information);
            mCredentials.prepareRequest(request);
        }
    }

    @Override
    public String getConnectionData() {
        return null;
    }

    @Override
    public void stop() {
        synchronized (mStartLock) {
            log("Entered startLock in stop", LogLevel.Verbose);
            if (mAborting) {
                log("Abort already started.", LogLevel.Verbose);
                return;
            }

            if (mState == ConnectionState.Disconnected) {
                log("Connection already in disconnected state. Exiting abort", LogLevel.Verbose);
                return;
            }

            log("Stopping the connection", LogLevel.Information);
            mAborting = true;

            log("Starting abort operation", LogLevel.Verbose);
            mAbortFuture = mTransport.abort(this);

            final Connection that = this;
            mAbortFuture.onError(new ErrorCallback() {

                @Override
                public void onError(Throwable error) {
                    synchronized (mStartLock) {
                        that.onError(error, false);
                        disconnect();
                        mAborting = false;
                    }
                }
            });

            mAbortFuture.onCancelled(new Runnable() {

                @Override
                public void run() {
                    synchronized (mStartLock) {
                        log("Abort cancelled", LogLevel.Verbose);
                        mAborting = false;
                    }
                }
            });

            mAbortFuture.done(new Action<Void>() {

                @Override
                public void run(Void obj) throws Exception {
                    synchronized (mStartLock) {
                        log("Abort completed", LogLevel.Information);
                        disconnect();
                        mAborting = false;
                    }
                }
            });
        }
    }

    @Override
    public void disconnect() {
        synchronized (mStateLock) {
            log("Entered stateLock in disconnect", LogLevel.Verbose);

            if (mState == ConnectionState.Disconnected) {
                return;
            }

            log("Disconnecting", LogLevel.Information);
            ConnectionState oldState = mState;
            mState = ConnectionState.Disconnected;
            if (mOnStateChanged != null) {
                try {
                    mOnStateChanged.stateChanged(oldState, ConnectionState.Disconnected);
                } catch (Throwable e) {
                    onError(e, false);
                }
            }
            
            if (mHeartbeatMonitor != null) {
                log("Stopping Heartbeat monitor", LogLevel.Verbose);
                mHeartbeatMonitor.stop();
            }

            mHeartbeatMonitor = null;

            if (mConnectionFuture != null) {
                log("Stopping the connection", LogLevel.Verbose);
                mConnectionFuture.cancel();
                mConnectionFuture = new UpdateableCancellableFuture<Void>(null);
            }

            if (mAbortFuture != null) {
                log("Cancelling abort", LogLevel.Verbose);
                mAbortFuture.cancel();
            }

            mConnectionId = null;
            mConnectionToken = null;
            mCredentials = null;
            mGroupsToken = null;
            mHeaders.clear();
            mMessageId = null;
            mTransport = null;

            onClosed();
        }
    }

    @Override
    public Gson getGson() {
        return mGson;
    }

    @Override
    public void setGson(Gson gson) {
        mGson = gson;
    }

    @Override
    public JsonParser getJsonParser() {
        return mJsonParser;
    }

    /**
     * Triggers the Reconnecting event
     */
    protected void onReconnecting() {
        if (mOnReconnecting != null) {
            mOnReconnecting.run();
        }
    }

    /**
     * Triggers the Reconnected event
     */
    protected void onReconnected() {
        if (mOnReconnected != null) {
            mOnReconnected.run();
        }
    }

    /**
     * Triggers the Connected event
     */
    protected void onConnected() {
        if (mOnConnected != null) {
            mOnConnected.run();
        }
    }

    /**
     * Verifies the protocol version
     * 
     * @param versionString
     *            String representing a Version
     * @return True if the version is supported.
     */
    private static boolean verifyProtocolVersion(String versionString) {
        try {
            if (versionString == null || versionString.equals("")) {
                return false;
            }

            Version version = new Version(versionString);

            return version.equals(PROTOCOL_VERSION);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Starts the transport
     * 
     * @param keepAliveData
     *            Keep Alive data for heartbeat monitor
     * @param isReconnecting
     *            True if is reconnecting
     */
    private void startTransport(KeepAliveData keepAliveData, final boolean isReconnecting) {
        synchronized (mStartLock) {
            log("Entered startLock in startTransport", LogLevel.Verbose);
            // if the connection was closed before this callback, just return;
            if (mTransport == null) {
                log("Transport is null. Exiting startTransport", LogLevel.Verbose);
                return;
            }

            log("Starting the transport", LogLevel.Information);
            if (isReconnecting) {
                if (mHeartbeatMonitor != null) {
                    log("Stopping heartbeat monitor", LogLevel.Verbose);
                    mHeartbeatMonitor.stop();
                }

                changeState(ConnectionState.Connected, ConnectionState.Reconnecting);
                onReconnecting();
            }

            mHeartbeatMonitor = new HeartbeatMonitor();

            mHeartbeatMonitor.setOnWarning(new Runnable() {

                @Override
                public void run() {
                    log("Slow connection detected", LogLevel.Information);
                    if (mOnConnectionSlow != null) {
                        mOnConnectionSlow.run();
                    }
                }
            });

            mHeartbeatMonitor.setOnTimeout(new Runnable() {

                @Override
                public void run() {
                    log("Timeout", LogLevel.Information);
                    reconnect();
                }
            });

            final Connection that = this;

            ConnectionType connectionType = isReconnecting ? ConnectionType.Reconnection : ConnectionType.InitialConnection;

            log("Starting transport for " + connectionType.toString(), LogLevel.Verbose);
            SignalRFuture<Void> future = mTransport.start(this, connectionType, new DataResultCallback() {
                @Override
                public void onData(String data) {
                    log("Received data: ", LogLevel.Verbose);
                    processReceivedData(data);
                }
            });

            handleFutureError(future, true);

            mConnectionFuture.setFuture(future);
            future.onError(new ErrorCallback() {
                
                @Override
                public void onError(Throwable error) {
                    mConnectionFuture.triggerError(error);
                }
            });
            
            mKeepAliveData = keepAliveData;

            try {
                future.done(new Action<Void>() {

                    @Override
                    public void run(Void obj) throws Exception {
                        synchronized (mStartLock) {
                            log("Entered startLock after transport was started", LogLevel.Verbose);
                            log("Current state: " + mState, LogLevel.Verbose);
                            if (changeState(ConnectionState.Reconnecting, ConnectionState.Connected)) {

                                log("Starting Heartbeat monitor", LogLevel.Verbose);
                                mHeartbeatMonitor.start(mKeepAliveData, that);
                                
                                log("Reconnected", LogLevel.Information);
                                onReconnected();

                            } else if (changeState(ConnectionState.Connecting, ConnectionState.Connected)) {

                                log("Starting Heartbeat monitor", LogLevel.Verbose);
                                mHeartbeatMonitor.start(mKeepAliveData, that);
                                
                                log("Connected", LogLevel.Information);
                                onConnected();
                                mConnectionFuture.setResult(null);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                onError(e, false);
            }
        }
    }

    /**
     * Parses the received data and triggers the OnReceived event
     * 
     * @param data
     *            The received data
     */
    private void processReceivedData(String data) {
        if (mHeartbeatMonitor != null) {
            mHeartbeatMonitor.beat();
        }

        MessageResult result = TransportHelper.processReceivedData(data, this);

        if (result.disconnect()) {
            disconnect();
            return;
        }

        if (result.reconnect()) {
            reconnect();
        }
    }

    /**
     * Processes a received message
     * 
     * @param message
     *            The message to process
     * @return The processed message
     * @throws Exception
     *             An exception could be thrown if there an error while
     *             processing the message
     */
    protected JsonElement processMessage(JsonElement message) throws Exception {
        return message;
    }

    @Override
    public void onError(Throwable error, boolean mustCleanCurrentConnection) {
        log(error);
        if (mustCleanCurrentConnection) {
            if (mState == ConnectionState.Connected) {
                log("Triggering reconnect", LogLevel.Verbose);
                reconnect();
            } else {
                log("Triggering disconnect", LogLevel.Verbose);
                disconnect();
                if (mOnError != null) {
                    mOnError.onError(error);
                }
            }
        } else {
            if (mOnError != null) {
                mOnError.onError(error);
            }
        }
    }

    /**
     * Triggers the Closed event
     */
    protected void onClosed() {
        if (mOnClosed != null) {
            mOnClosed.run();
        }
    }

    /**
     * Stops the heartbeat monitor and re-starts the transport
     */
    private void reconnect() {
        if (mState == ConnectionState.Connected) {
            log("Stopping Heartbeat monitor", LogLevel.Verbose);
            mHeartbeatMonitor.stop();
            log("Restarting the transport", LogLevel.Information);
            startTransport(mHeartbeatMonitor.getKeepAliveData(), true);
        }
    }

    protected void log(String message, LogLevel level) {
        if (message != null & mLogger != null) {
            mLogger.log(getSourceNameForLog() + " - " + message, level);
        }
    }

    protected void log(Throwable error) {
        mLogger.log(getSourceNameForLog() + " - Error: " + error.toString(), LogLevel.Critical);
    }

    protected String getSourceNameForLog() {
        return "Connection";
    }

    @Override
    public void onReceived(JsonElement message) {
        if (mOnReceived != null && getState() == ConnectionState.Connected) {
            log("Invoking messageReceived with: " + message, LogLevel.Verbose);
            try {
                mOnReceived.onMessageReceived(message);
            } catch (Throwable error) {
                onError(error, false);
            }
        }
    }
}
