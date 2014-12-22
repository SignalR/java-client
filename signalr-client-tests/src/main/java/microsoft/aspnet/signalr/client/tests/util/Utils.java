/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.tests.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.Constants;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.MessageReceivedHandler;
import microsoft.aspnet.signalr.client.NullLogger;
import microsoft.aspnet.signalr.client.Connection;
import microsoft.aspnet.signalr.client.http.HttpConnection;
import microsoft.aspnet.signalr.client.tests.util.MockHttpConnection.RequestEntry;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.LongPollingTransport;
import microsoft.aspnet.signalr.client.transport.NegotiationResponse;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;

public class Utils {

    public static String encode(String str) {
        try {
            return URLEncoder.encode(str, Constants.UTF8_NAME);
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    public static ClientTransport createTransport(TransportType transportType, HttpConnection httpConnection) {
        if (transportType == TransportType.ServerSentEvents) {
            return new ServerSentEventsTransport(new NullLogger(), httpConnection);
        } else {
            return new LongPollingTransport(new NullLogger(), httpConnection);
        }
    }

    public static String getNegotiationResponseContent(NegotiationResponse negotiation) {

        return String
                .format("{\"Url\":\"/signalr\", \"ConnectionToken\":\"%s\", \"ConnectionId\":\"%s\",\"KeepAliveTimeout\":%s,\"DisconnectTimeout\":%s,\"TryWebSockets\":%s,\"WebSocketServerUrl\":\"%s\", \"ProtocolVersion\":\"%s\"}",
                        negotiation.getConnectionToken(), negotiation.getConnectionId(), negotiation.getKeepAliveTimeout(), negotiation.getDisconnectTimeout(),
                        negotiation.shouldTryWebSockets(), negotiation.getUrl(), negotiation.getProtocolVersion());
    }

    public static NegotiationResponse getDefaultNegotiationResponse() {

        NegotiationResponse negotiation = new NegotiationResponse(null, new JsonParser());

        negotiation.setConnectionToken(UUID.randomUUID().toString());
        negotiation.setConnectionId(UUID.randomUUID().toString());
        negotiation.setProtocolVersion("1.3");
        negotiation.setDisconnectTimeout(6);
        negotiation.setKeepAliveTimeout(3);
        negotiation.setTryWebSockets(false);
        negotiation.setUrl("/signalr");

        return negotiation;
    }

    public static void addResultHandlersToConnection(Connection connection, final MultiResult result, final boolean throwOnError) {
        connection.connected(new Runnable() {

            @Override
            public void run() {
                result.statesResult.add(ConnectionState.Connected);
            }
        });

        connection.closed(new Runnable() {

            @Override
            public void run() {
                result.statesResult.add(ConnectionState.Disconnected);
            }
        });

        connection.reconnected(new Runnable() {

            @Override
            public void run() {
                result.statesResult.add(ConnectionState.Connected);
            }
        });

        connection.reconnecting(new Runnable() {

            @Override
            public void run() {
                result.statesResult.add(ConnectionState.Reconnecting);
            }
        });

        connection.received(new MessageReceivedHandler() {

            @Override
            public void onMessageReceived(JsonElement json) {
                result.listResult.add(json.toString());
            }
        });

        connection.error(new ErrorCallback() {

            @Override
            public void onError(Throwable error) {
                result.errorsResult.add(error);

                if (throwOnError) {
                    throw new RuntimeException(error);
                }
            }
        });
    }

    public static void finishMessage(RequestEntry entry) throws Exception {
        entry.finishRequest();
        entry.triggerResponse();
    }
}
