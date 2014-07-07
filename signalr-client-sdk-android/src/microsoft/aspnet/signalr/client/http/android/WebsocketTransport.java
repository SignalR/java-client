package microsoft.aspnet.signalr.client.http.android;

import android.os.Build;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import microsoft.aspnet.signalr.client.ConnectionBase;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.UpdateableCancellableFuture;
import microsoft.aspnet.signalr.client.http.HttpConnection;
import microsoft.aspnet.signalr.client.transport.ConnectionType;
import microsoft.aspnet.signalr.client.transport.DataResultCallback;
import microsoft.aspnet.signalr.client.transport.HttpClientTransport;
import microsoft.aspnet.signalr.client.transport.TransportHelper;

/**
 *
 * Implements the WebsocketTransport for the java SignalR library.
 *
 * Created by stas on 07/07/14.
 */
public class WebsocketTransport extends HttpClientTransport {

    WebSocketClient mWebSocketClient;
    private UpdateableCancellableFuture<Void> mConnectionFuture;

    public WebsocketTransport(Logger logger) {
        super(logger);
    }

    public WebsocketTransport(Logger logger, HttpConnection httpConnection) {
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

    @Override
    public SignalRFuture<Void> start(ConnectionBase connection, ConnectionType connectionType, final DataResultCallback callback) {
        final String connectionString = connectionType == ConnectionType.InitialConnection ? "connect" : "reconnect";

        final String transport = getName();
        final String connectionToken = connection.getConnectionToken();
        final String messageId = connection.getMessageId() != null ? connection.getMessageId() : "";
        final String groupsToken = connection.getGroupsToken() != null ? connection.getGroupsToken() : "";
        final String connectionData = connection.getConnectionData() != null ? connection.getConnectionData() : "";


        String url = null;
        try {
            url = connection.getUrl() + "signalr/" + connectionString + '?'
                    + "connectionData=" + URLEncoder.encode(connectionData, "UTF-8")
                    + "&connectionToken=" + URLEncoder.encode(connectionToken, "UTF-8")
                    + "&groupsToken=" + URLEncoder.encode(groupsToken, "UTF-8")
                    + "&messageId=" + URLEncoder.encode(messageId, "UTF-8")
                    + "&transport=" + URLEncoder.encode(transport, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

//           String url = connection.getUrl() + "signalr/" + connectionString;
//           String parameters = "connectionData=" + connectionData
//            + "&connectionToken=" + connectionToken
//            + "&groupsToken=" + groupsToken
//            + "&messageId=" + messageId
//            + "&transport=" + transport;

//        String url = connection.getUrl() + connectionString + TransportHelper.getReceiveQueryString(this, connection);

        log("websockettransport connecting to url:" + url, LogLevel.Verbose);

        mConnectionFuture = new UpdateableCancellableFuture<Void>(null);

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            mConnectionFuture.triggerError(e);
            return mConnectionFuture;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                log("websockettransport Opened", LogLevel.Verbose);
                mConnectionFuture.setResult(null);
                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                callback.onData(s);
                log("websockettransport got message:" + s, LogLevel.Verbose);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                log("websockettransport Closed " + s, LogLevel.Verbose);
            }

            @Override
            public void onError(Exception e) {
                log("websockettransport Error " + e.getMessage(), LogLevel.Verbose);
            }
        };
        mWebSocketClient.connect();

        connection.closed(new Runnable() {
            @Override
            public void run() {
                mWebSocketClient.close();
            }
        });


        return mConnectionFuture;
    }
}
