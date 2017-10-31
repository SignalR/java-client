package microsoft.aspnet.signalr.client.transport;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketCall;
import com.squareup.okhttp.ws.WebSocketListener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import microsoft.aspnet.signalr.client.ConnectionBase;
import microsoft.aspnet.signalr.client.Constants;
import microsoft.aspnet.signalr.client.FutureHelper;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.UpdateableCancellableFuture;
import microsoft.aspnet.signalr.client.http.HttpConnectionFuture;
import microsoft.aspnet.signalr.client.http.InvalidHttpStatusCodeException;
import microsoft.aspnet.signalr.client.http.Request;
import microsoft.aspnet.signalr.client.http.Response;
import okio.Buffer;

/**
 * Created by giovannimeo on 31/10/17.
 */

public class OkWebsocketTransportNew implements ClientTransport {
    private final OkHttpClient mOkHttpClient;
    private final Logger mLogger;
    private final ExecutorService mSendExecutor = Executors.newSingleThreadExecutor();

    private ConnectionWebSocketListener mCurrentWebSocketListener;
    private SignalRFuture<Void> mAbortFuture;

    protected boolean mStartedAbort = false;

    public OkWebsocketTransportNew(OkHttpClient okHttpClient, Logger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("logger");
        }

        this.mOkHttpClient = okHttpClient;
        this.mOkHttpClient.setReadTimeout(0, TimeUnit.MILLISECONDS);
        this.mLogger = logger;
    }

    @Override
    public String getName() {
        return "webSockets";
    }

    @Override
    public boolean supportKeepAlive() {
        return true;
    }

    @Override
    public SignalRFuture<NegotiationResponse> negotiate(final ConnectionBase connection) {
        log("Start the negotiation with the server", LogLevel.Information);

        String url = connection.getUrl() + "negotiate" + TransportHelper.getNegotiateQueryString(connection);

        com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                .url(url)
                .method(Constants.HTTP_GET, null)
        // TODO .headers()
                .build();

        final SignalRFuture<NegotiationResponse> negotiationFuture = new SignalRFuture<NegotiationResponse>();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            private void handleFailure(Exception e) {
                log(e);
                negotiationFuture.triggerError(new NegotiationException("There was a problem in the negotiation with the server", e));
            }

            @Override
            public void onFailure(com.squareup.okhttp.Request request, IOException e) {
                handleFailure(e);
            }

            @Override
            public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                try {
                    log("Response received", LogLevel.Verbose);
                    if (response.isSuccessful())
                        negotiationFuture.setResult(new NegotiationResponse(response.body().string(), connection.getJsonParser()));
                    else
                        throw new InvalidHttpStatusCodeException(response.code(), response.message(), response.headers().toString());
                } catch (InvalidHttpStatusCodeException e) {
                    handleFailure(e);
                } finally {
                    response.body().close();
                }
            }
        });


//        Request get = new Request(Constants.HTTP_GET);
//        get.setUrl(url);
//        get.setVerb(Constants.HTTP_GET);
//        get.setHeaders(connection.getHeaders());
//
//        connection.prepareRequest(get);
//
//        final SignalRFuture<NegotiationResponse> negotiationFuture = new SignalRFuture<NegotiationResponse>();
//
//        log("Execute the request", LogLevel.Verbose);
//        HttpConnectionFuture connectionFuture = mHttpConnection.execute(get, new HttpConnectionFuture.ResponseCallback() {
//
//            public void onResponse(Response response) {
//                try {
//                    log("Response received", LogLevel.Verbose);
//                    throwOnInvalidStatusCode (response);
//
//                    log("Read response data to the end", LogLevel.Verbose);
//                    String negotiationContent = response.readToEnd();
//
//                    log("Trigger onSuccess with negotiation data: " + negotiationContent, LogLevel.Verbose);
//                    negotiationFuture.setResult(new NegotiationResponse(negotiationContent, connection.getJsonParser()));
//
//                } catch (Throwable e) {
//                    log(e);
//                    negotiationFuture.triggerError(new NegotiationException("There was a problem in the negotiation with the server", e));
//                }
//            }
//        });
//
//        FutureHelper.copyHandlers(connectionFuture, negotiationFuture);

        return negotiationFuture;
    }

    @Override
    public SignalRFuture<Void> start(ConnectionBase connection, ConnectionType connectionType, DataResultCallback callback) {
        // TODO
//        if (mCurrentWebSocketListener != null) {
//            mCurrentWebSocketListener.connectionFuture.cancel();
//            mCurrentWebSocketListener = null;
//        }
//
//        final String connectionString = connectionType == ConnectionType.InitialConnection ? "connect" : "reconnect";
//
//        final String transport = getName();
//        final String connectionToken = connection.getConnectionToken();
//        final String messageId = connection.getMessageId() != null ? connection.getMessageId() : "";
//        final String groupsToken = connection.getGroupsToken() != null ? connection.getGroupsToken() : "";
//        final String connectionData = connection.getConnectionData() != null ? connection.getConnectionData() : "";
//
//        String url = null;
//        try {
//            url = connection.getUrl() + connectionString + '?'
//                    + "connectionData=" + URLEncoder.encode(connectionData, "UTF-8")
//                    + "&connectionToken=" + URLEncoder.encode(connectionToken, "UTF-8")
//                    + "&groupsToken=" + URLEncoder.encode(groupsToken, "UTF-8")
//                    + "&messageId=" + URLEncoder.encode(messageId, "UTF-8")
//                    + "&transport=" + URLEncoder.encode(transport, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        final com.squareup.okhttp.Request.Builder reqBuilder = new com.squareup.okhttp.Request.Builder().get().url(url);
//        for (Map.Entry<String, String> h : connection.getHeaders().entrySet()) {
//            reqBuilder.addHeader(h.getKey(), h.getValue());
//        }
//
        final UpdateableCancellableFuture<Void> connectionFuture = new UpdateableCancellableFuture<Void>(null);
//        final ConnectionWebSocketListener connectionWebSocketListener = new ConnectionWebSocketListener(connectionFuture, callback);
//
//        final WebSocketCall call = WebSocketCall.create(mOkHttpClient, reqBuilder.build());
//        connectionFuture.onCancelled(new Runnable() {
//            @Override
//            public void run() {
//                call.cancel();
//                if (connectionWebSocketListener.abortCalled) return;
//                final WebSocket webSocket = connectionWebSocketListener.webSocket;
//                if (webSocket != null) {
//                    try {
//                        webSocket.close(1000, "");
//                    } catch (IOException e) {
//                        log(e);
//                    }
//                }
//                connectionWebSocketListener.webSocket = null;
//            }
//        });
//
//        call.enqueue(connectionWebSocketListener);
//
//        mCurrentWebSocketListener = connectionWebSocketListener;
        return connectionFuture;
    }

    @Override
    public SignalRFuture<Void> send(ConnectionBase connection, final String data, DataResultCallback callback) {
        final WebSocket webSocket = mCurrentWebSocketListener.webSocket;
        final SignalRFuture<Void> connectionFuture = mCurrentWebSocketListener.connectionFuture;
        if (webSocket == null) {
            SignalRFuture<Void> future = new SignalRFuture<Void>();
            future.triggerError(new Exception("Web socket isn't available"));
            return future;
        }

        mSendExecutor.submit(new Runnable() {
            @Override
            public void run() {
                if (connectionFuture.isCancelled()) return;
                try {
                    webSocket.sendMessage(RequestBody.create(WebSocket.TEXT, data));
                } catch (IOException e) {
                    if (!connectionFuture.isCancelled()) connectionFuture.triggerError(e);
                }
            }
        });

        return new UpdateableCancellableFuture<Void>(null);
    }

    @Override
    public SignalRFuture<Void> abort(ConnectionBase connection) {
        if (mCurrentWebSocketListener != null) mCurrentWebSocketListener.abortCalled = true;

        synchronized (this) {
            if (!mStartedAbort) {
                log("Started aborting", LogLevel.Information);
                mStartedAbort = true;
                String url = connection.getUrl() + "abort" + TransportHelper.getSendQueryString(this, connection);

                com.squareup.okhttp.Request postRequest = new com.squareup.okhttp.Request.Builder()
                        .url(url)
                        .method(Constants.HTTP_POST, null)
                // TODO .headers()
                        .build();


                mAbortFuture = new SignalRFuture<>();
                log("Execute request", LogLevel.Verbose);
                final Call call = mOkHttpClient.newCall(postRequest);
                mAbortFuture.onCancelled(new Runnable() {
                    @Override
                    public void run() {
                        call.cancel();
                    }
                });

                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(com.squareup.okhttp.Request request, IOException e) {
                        log(e);
                        log("Finishing abort", LogLevel.Verbose);
                        mStartedAbort = false;
                        mAbortFuture.triggerError(e);
                    }

                    @Override
                    public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                        log("Finishing abort", LogLevel.Verbose);
                        mStartedAbort = false;
                        mAbortFuture.setResult(null);
                    }
                });
                return mAbortFuture;

            } else {
                return mAbortFuture;
            }
        }
    }

    protected void log(String message, LogLevel level) {
        mLogger.log(getName() + " - " + message, level);
    }

    protected void log(Throwable error) {
        mLogger.log(getName() + " - Error: " + error.toString(), LogLevel.Critical);
    }

    private static class ConnectionWebSocketListener implements WebSocketListener {
        final SignalRFuture<Void> connectionFuture;
        final DataResultCallback dataCallback;
        WebSocket webSocket;
        boolean abortCalled;

        public ConnectionWebSocketListener(SignalRFuture<Void> connectionFuture, DataResultCallback dataCallback) {
            this.connectionFuture = connectionFuture;
            this.dataCallback = dataCallback;
        }

        @Override
        public void onOpen(WebSocket webSocket, com.squareup.okhttp.Response response) {
            this.webSocket = webSocket;
            connectionFuture.setResult(null);
        }

        @Override
        public void onFailure(IOException e, com.squareup.okhttp.Response response) {
            if (!connectionFuture.isCancelled() && !abortCalled) {
                connectionFuture.cancel();
                connectionFuture.triggerError(e);
            }
        }

        @Override
        public void onMessage(ResponseBody message) throws IOException {
            dataCallback.onData(message.string());
            message.close();
        }

        @Override
        public void onPong(Buffer payload) {
            payload.close();
        }

        @Override
        public void onClose(int code, String reason) {
            if (!connectionFuture.isCancelled() && !abortCalled) {
                connectionFuture.triggerError(new Exception("Received close from server"));
            }
            webSocket = null;
        }
    }
}
