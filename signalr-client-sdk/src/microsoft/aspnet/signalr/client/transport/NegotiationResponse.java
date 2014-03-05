/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.transport;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Represents the negotiation response sent by the server in the handshake
 */
public class NegotiationResponse {
    public static final double INVALID_KEEP_ALIVE_TIMEOUT = -1;

    private String mConnectionId;
    private String mConnectionToken;
    private String mUrl;
    private String mProtocolVersion;
    private double mDisconnectTimeout;
    private boolean mTryWebSockets;
    private double mKeepAliveTimeout;

    /**
     * Initializes the negotiation response with Json data
     * 
     * @param jsonContent
     *            Json data
     */
    public NegotiationResponse(String jsonContent, JsonParser parser) {
        if (jsonContent == null || "".equals(jsonContent)) {
            return;
        }

        JsonObject json = parser.parse(jsonContent).getAsJsonObject();

        setConnectionId(json.get("ConnectionId").getAsString());
        setConnectionToken(json.get("ConnectionToken").getAsString());
        setUrl(json.get("Url").getAsString());
        setProtocolVersion(json.get("ProtocolVersion").getAsString());
        setDisconnectTimeout(json.get("DisconnectTimeout").getAsDouble());
        setTryWebSockets(json.get("TryWebSockets").getAsBoolean());

        JsonElement keepAliveElement = json.get("KeepAliveTimeout");
        if (keepAliveElement != null && !keepAliveElement.isJsonNull()) {
            setKeepAliveTimeout(keepAliveElement.getAsDouble());
        } else {
            setKeepAliveTimeout(INVALID_KEEP_ALIVE_TIMEOUT);
        }

    }

    public String getConnectionId() {
        return mConnectionId;
    }

    public void setConnectionId(String connectionId) {
        mConnectionId = connectionId;
    }

    public String getConnectionToken() {
        return mConnectionToken;
    }

    public void setConnectionToken(String connectionToken) {
        mConnectionToken = connectionToken;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getProtocolVersion() {
        return mProtocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        mProtocolVersion = protocolVersion;
    }

    public double getDisconnectTimeout() {
        return mDisconnectTimeout;
    }

    public void setDisconnectTimeout(double disconnectTimeout) {
        mDisconnectTimeout = disconnectTimeout;
    }

    public boolean shouldTryWebSockets() {
        return mTryWebSockets;
    }

    public void setTryWebSockets(boolean tryWebSockets) {
        mTryWebSockets = tryWebSockets;
    }

    public double getKeepAliveTimeout() {
        return mKeepAliveTimeout;
    }

    public void setKeepAliveTimeout(double keepAliveTimeout) {
        mKeepAliveTimeout = keepAliveTimeout;
    }
}
