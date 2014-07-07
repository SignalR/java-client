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
    public SignalRFuture<Void> start(ConnectionBase connection, ConnectionType connectionType, DataResultCallback callback) {
        final String connectionString = connectionType == ConnectionType.InitialConnection ? "connect" : "reconnect";
        String url = connection.getUrl() + connectionString + TransportHelper.getReceiveQueryString(this, connection);

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
                log("Opened", LogLevel.Verbose);
                mConnectionFuture.setResult(null);
                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                log("got message:" + s, LogLevel.Verbose);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                log("Closed " + s, LogLevel.Verbose);
            }

            @Override
            public void onError(Exception e) {
                log("Error " + e.getMessage(), LogLevel.Verbose);
            }
        };
        mWebSocketClient.connect();


        return mConnectionFuture;
    }
}
