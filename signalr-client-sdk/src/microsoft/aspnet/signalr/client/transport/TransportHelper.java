/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.transport;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import microsoft.aspnet.signalr.client.Connection;
import microsoft.aspnet.signalr.client.ConnectionBase;
import microsoft.aspnet.signalr.client.Constants;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.MessageResult;

public class TransportHelper {
    public static MessageResult processReceivedData(String data, ConnectionBase connection) {
        Logger logger = connection.getLogger();
        MessageResult result = new MessageResult();
        
        if (data == null) {
            return result;
        }

        data = data.trim();

        if ("".equals(data)) {
            return result;
        }

        JsonObject json = null;

        try {
            json = connection.getJsonParser().parse(data).getAsJsonObject();
        } catch (Exception e) {
            connection.onError(e, false);
            return result;
        }

        if (json.entrySet().size() == 0) {
            return result;
        }

        if (json.get("I") != null) {
            logger.log("Invoking message received with: " + json.toString(), LogLevel.Verbose);
            connection.onReceived(json);
        } else {

            // disconnected
            if (json.get("D") != null) {
                if (json.get("D").getAsInt() == 1) {
                    logger.log("Disconnect message received", LogLevel.Verbose);
                    result.setDisconnect(true);
                    return result;
                }
            }

            // should reconnect
            if (json.get("T") != null) {
                if (json.get("T").getAsInt() == 1) {
                    logger.log("Reconnect message received", LogLevel.Verbose);
                    result.setReconnect(true);
                }
            }

            if (json.get("G") != null) {
                String groupsToken = json.get("G").getAsString();
                logger.log("Group token received: " + groupsToken, LogLevel.Verbose);
                connection.setGroupsToken(groupsToken);
            }

            JsonElement messages = json.get("M");
            if (messages != null && messages.isJsonArray()) {

                if (json.get("C") != null) {
                    String messageId = json.get("C").getAsString();
                    logger.log("MessageId received: " + messageId, LogLevel.Verbose);
                    connection.setMessageId(messageId);
                }

                JsonArray messagesArray = messages.getAsJsonArray();
                int size = messagesArray.size();

                for (int i = 0; i < size; i++) {
                    JsonElement message = messagesArray.get(i);
                    JsonElement processedMessage = null;

                    logger.log("Invoking OnReceived with: " + processedMessage, LogLevel.Verbose);
                    connection.onReceived(message);
                }
            }

            if (json.get("S") != null) {
                if (json.get("S").getAsInt() == 1) {
                    logger.log("Initialization message received", LogLevel.Information);
                    result.setInitialize(true);
                }
            }
        }

        return result;
    }

    /**
     * Creates the query string used on receive
     * 
     * @param transport
     *            Transport to use
     * @param connection
     *            Current connection
     * @return The querystring
     */
    public static String getReceiveQueryString(ClientTransport transport, ConnectionBase connection) {
        StringBuilder qsBuilder = new StringBuilder();

        qsBuilder.append("?transport=" + transport.getName()).append("&connectionToken=" + urlEncode(connection.getConnectionToken()));

        qsBuilder.append("&connectionId=" + urlEncode(connection.getConnectionId()));

        if (connection.getMessageId() != null) {
            qsBuilder.append("&messageId=" + urlEncode(connection.getMessageId()));
        }

        if (connection.getGroupsToken() != null) {
            qsBuilder.append("&groupsToken=" + urlEncode(connection.getGroupsToken()));
        }

        String connectionData = connection.getConnectionData();
        if (connectionData != null) {
            qsBuilder.append("&connectionData=" + urlEncode(connectionData));
        }

        String customQuery = connection.getQueryString();

        if (customQuery != null) {
            qsBuilder.append("&").append(customQuery);
        }

        return qsBuilder.toString();
    }

    /**
     * Creates the query string used on sending
     * 
     * @param connection
     *            current connection
     * @return The querystring
     */
    public static String getNegotiateQueryString(ConnectionBase connection) {
        StringBuilder qsBuilder = new StringBuilder();
        qsBuilder.append("?clientProtocol=" + urlEncode(Connection.PROTOCOL_VERSION.toString()));

        if (connection.getConnectionData() != null) {
            qsBuilder.append("&").append("connectionData=" + urlEncode(connection.getConnectionData()));
        }

        if (connection.getQueryString() != null) {
            qsBuilder.append("&").append(connection.getQueryString());
        }

        return qsBuilder.toString();
    }

    /**
     * Creates the query string used on sending
     * 
     * @param transport
     *            the transport to use
     * @param connection
     *            current connection
     * @return The querystring
     */
    public static String getSendQueryString(ClientTransport transport, ConnectionBase connection) {
        StringBuilder qsBuilder = new StringBuilder();
        qsBuilder.append("?transport=" + TransportHelper.urlEncode(transport.getName()));

        qsBuilder.append("&connectionToken=" + TransportHelper.urlEncode(connection.getConnectionToken()));

        qsBuilder.append("&connectionId=" + TransportHelper.urlEncode(connection.getConnectionId()));

        if (connection.getConnectionData() != null) {
            qsBuilder.append("&connectionData=" + TransportHelper.urlEncode(connection.getConnectionData()));
        }

        if (connection.getQueryString() != null) {
            qsBuilder.append("&").append(connection.getQueryString());
        }

        return qsBuilder.toString();
    }

    public static String urlEncode(String s) {
        if (s == null) {
            return "";
        }

        String encoded = null;
        try {
            encoded = URLEncoder.encode(s, Constants.UTF8_NAME);
        } catch (UnsupportedEncodingException e) {
        }

        return encoded;
    }
}
