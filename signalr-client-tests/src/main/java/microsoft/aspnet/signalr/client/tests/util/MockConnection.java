/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.tests.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

import microsoft.aspnet.signalr.client.ConnectionBase;
import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.Credentials;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.MessageReceivedHandler;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.StateChangedCallback;
import microsoft.aspnet.signalr.client.http.Request;
import microsoft.aspnet.signalr.client.transport.ClientTransport;

public class MockConnection implements ConnectionBase {

    private Credentials mCredentials;

    @Override
    public String getUrl() {
        return "http://myUrl.com/signalr/";
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
    public void reconnecting(Runnable handler) {
    }

    @Override
    public void reconnected(Runnable handler) {
    }

    @Override
    public void connected(Runnable handler) {
    }

    @Override
    public void error(ErrorCallback handler) {
    }

    @Override
    public void received(MessageReceivedHandler handler) {
    }

    @Override
    public void connectionSlow(Runnable handler) {
    }

    @Override
    public void closed(Runnable handler) {
    }

    @Override
    public String getConnectionToken() {
        return "$CONNECTIONTOKEN";
    }

    @Override
    public String getConnectionId() {
        return "$CONNECTIONID";
    }

    @Override
    public String getQueryString() {
        return "val=1";
    }

    @Override
    public String getMessageId() {
        return "$MESSAGEID";
    }

    @Override
    public String getGroupsToken() {
        return "$GROUPSTOKEN";
    }

    @Override
    public String getConnectionData() {
        return "$CONNECTIONDATA";
    }

    @Override
    public ConnectionState getState() {
        return ConnectionState.Connected;
    }

    @Override
    public SignalRFuture<Void> start(ClientTransport transport) {
        return null;
    }

    @Override
    public void stop() {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public SignalRFuture<Void> send(String data) {
        return null;
    }

    @Override
    public void prepareRequest(Request request) {
    }

    @Override
    public Map<String, String> getHeaders() {
        return new HashMap<String, String>();
    }

    @Override
    public Gson getGson() {
        return new Gson();
    }

    @Override
    public void setGson(Gson gson) {
    }

    @Override
    public JsonParser getJsonParser() {
        return new JsonParser();
    }

    @Override
    public void setMessageId(String messageId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setGroupsToken(String groupsToken) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onError(Throwable error, boolean mustCleanCurrentConnection) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReceived(JsonElement message) {
        // TODO Auto-generated method stub

    }

    @Override
    public Logger getLogger() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void stateChanged(StateChangedCallback handler) {
        // TODO Auto-generated method stub

    }

}
