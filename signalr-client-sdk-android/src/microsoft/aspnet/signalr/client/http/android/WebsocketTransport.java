package microsoft.aspnet.signalr.client.http.android;

import android.net.Uri;
import android.os.Build;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.util.Charsetfunctions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;

import microsoft.aspnet.signalr.client.ConnectionBase;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.UpdateableCancellableFuture;
import microsoft.aspnet.signalr.client.http.HttpConnection;
import microsoft.aspnet.signalr.client.transport.ConnectionType;
import microsoft.aspnet.signalr.client.transport.DataResultCallback;
import microsoft.aspnet.signalr.client.transport.HttpClientTransport;

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

    private String mPrefix;

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
                    + "connectionData=" + URLEncoder.encode(URLEncoder.encode(connectionData, "UTF-8"), "UTF-8")
                    + "&connectionToken=" + URLEncoder.encode(URLEncoder.encode(connectionToken, "UTF-8"), "UTF-8")
                    + "&groupsToken=" + URLEncoder.encode(groupsToken, "UTF-8")
                    + "&messageId=" + URLEncoder.encode(messageId, "UTF-8")
                    + "&transport=" + URLEncoder.encode(transport, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

//        log("websockettransport connecting to url:" + url, LogLevel.Verbose);

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
            }

            @Override
            public void onMessage(String s) {
                callback.onData(s);
                log("websockettransport got message:" + s, LogLevel.Verbose);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                mWebSocketClient.close();
            }

            @Override
            public void onError(Exception e) {
                log("websockettransport Error " + e.getMessage(), LogLevel.Verbose);
                mWebSocketClient.close();
            }

            @Override
            public void onFragment(Framedata frame) {
                try {
                    String decodedString = Charsetfunctions.stringUtf8(frame.getPayloadData());

                    if(decodedString.equals("]}")){
                        return;
                    }

                    if(decodedString.endsWith(":[") || null == mPrefix){
                        mPrefix = decodedString;
                        return;
                    }

                    String simpleConcatenate = mPrefix + decodedString;

                    if(isJSONValid(simpleConcatenate)){
                        onMessage(simpleConcatenate);
                    }else{
                        String extendedConcatenate = simpleConcatenate + "]}";
                        if (isJSONValid(extendedConcatenate)) {
                            onMessage(extendedConcatenate);
                        } else {
                            log("invalid json received:" + decodedString, LogLevel.Critical);
                        }
                    }
//                    log("websockettransport framecontent:" + decodedString, LogLevel.Verbose);
                } catch (InvalidDataException e) {
                    e.printStackTrace();
                }
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

    @Override
    public SignalRFuture<Void> send(ConnectionBase connection, String data, DataResultCallback callback) {
        log("about to send:" + data, LogLevel.Verbose);
        mWebSocketClient.send(data);
        return new UpdateableCancellableFuture<Void>(null);
    }

    private boolean isJSONValid(String test){
        try{
            new JSONObject(test);
        }
        catch(JSONException ex){
            try{
                new JSONArray(test);
            }
            catch(JSONException ex2){
                return false;
            }
        }
        return true;
    }
}
