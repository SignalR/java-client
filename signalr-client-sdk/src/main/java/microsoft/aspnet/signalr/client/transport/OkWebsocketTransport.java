/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.transport;

import com.google.gson.Gson;
import com.squareup.okhttp.*;
import com.squareup.okhttp.ws.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import microsoft.aspnet.signalr.client.ConnectionBase;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.UpdateableCancellableFuture;
import microsoft.aspnet.signalr.client.http.HttpConnection;
import okio.Buffer;

/**
 * Implements the WebsocketTransport for the Java SignalR library
 * Created by stas on 07/07/14.
 */
public class OkWebsocketTransport extends HttpClientTransport {

    private static final String TAG = "WebsocketTransport";

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private final ExecutorService mSendExecutor = Executors.newSingleThreadExecutor();

    private ConnectionWebSocketListener mCurrentWebSocketListener;

    public OkWebsocketTransport(Logger logger) {
        super(logger);
        mOkHttpClient.setReadTimeout(0, TimeUnit.MILLISECONDS);
    }

    public OkWebsocketTransport(Logger logger, HttpConnection httpConnection) {
        super(logger, httpConnection);
    }

    @Override
    public String getName() {
        return "webSockets";
    }

    @Override
    public boolean supportKeepAlive() {
        return true;
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
        public void onOpen(WebSocket webSocket, Response response) {
            this.webSocket = webSocket;
            connectionFuture.setResult(null);
        }

        @Override
        public void onFailure(IOException e, Response response) {
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

    @Override
    public SignalRFuture<Void> start(final ConnectionBase connection, ConnectionType connectionType, final DataResultCallback callback) {
        if (mCurrentWebSocketListener != null) {
            mCurrentWebSocketListener.connectionFuture.cancel();
            mCurrentWebSocketListener = null;
        }

        final String connectionString = connectionType == ConnectionType.InitialConnection ? "connect" : "reconnect";

        final String transport = getName();
        final String connectionToken = connection.getConnectionToken();
        final String messageId = connection.getMessageId() != null ? connection.getMessageId() : "";
        final String groupsToken = connection.getGroupsToken() != null ? connection.getGroupsToken() : "";
        final String connectionData = connection.getConnectionData() != null ? connection.getConnectionData() : "";

        String url = null;
        try {
            url = connection.getUrl() + connectionString + '?'
                    + "connectionData=" + URLEncoder.encode(connectionData, "UTF-8")
                    + "&connectionToken=" + URLEncoder.encode(connectionToken, "UTF-8")
                    + "&groupsToken=" + URLEncoder.encode(groupsToken, "UTF-8")
                    + "&messageId=" + URLEncoder.encode(messageId, "UTF-8")
                    + "&transport=" + URLEncoder.encode(transport, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final Request.Builder reqBuilder = new Request.Builder().get().url(url);
        for (Map.Entry<String, String> h : connection.getHeaders().entrySet()) {
            reqBuilder.addHeader(h.getKey(), h.getValue());
        }

        final UpdateableCancellableFuture<Void> connectionFuture = new UpdateableCancellableFuture<Void>(null);
        final ConnectionWebSocketListener connectionWebSocketListener = new ConnectionWebSocketListener(connectionFuture, callback);

        final WebSocketCall call = WebSocketCall.create(mOkHttpClient, reqBuilder.build());
        connectionFuture.onCancelled(new Runnable() {
            @Override
            public void run() {
                call.cancel();
                if (connectionWebSocketListener.abortCalled) return;
                final WebSocket webSocket = connectionWebSocketListener.webSocket;
                if (webSocket != null) {
                    try {
                        webSocket.close(1000, "");
                    } catch (IOException e) {
                        log(e);
                    }
                }
                connectionWebSocketListener.webSocket = null;
            }
        });

        call.enqueue(connectionWebSocketListener);

        mCurrentWebSocketListener = connectionWebSocketListener;
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
        return super.abort(connection);
    }

}