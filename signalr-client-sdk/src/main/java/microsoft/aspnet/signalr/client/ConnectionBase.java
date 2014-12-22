/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import microsoft.aspnet.signalr.client.http.Request;
import microsoft.aspnet.signalr.client.transport.ClientTransport;

public interface ConnectionBase {

    /**
     * Returns the URL used by the connection
     */
    public String getUrl();

    /**
     * Returns the credentials used by the connection
     */
    public Credentials getCredentials();

    /**
     * Sets the credentials the connection should use
     */
    public void setCredentials(Credentials credentials);

    /**
     * Sets the message id the connection should use
     */
    public void setMessageId(String messageId);

    /**
     * Sets the groups token the connection should use
     */
    public void setGroupsToken(String groupsToken);

    /**
     * Sets the handler for the "Reconnecting" event
     */
    public void reconnecting(Runnable handler);

    /**
     * Sets the handler for the "Reconnected" event
     */
    public void reconnected(Runnable handler);

    /**
     * Sets the handler for the "Connected" event
     */
    public void connected(Runnable handler);

    /**
     * Sets the handler for the "Error" event
     */
    public void error(ErrorCallback handler);

    /**
     * Sets the handler for the "StateChanged" event
     */
    public void stateChanged(StateChangedCallback handler);

    /**
     * Triggers the Error event
     * 
     * @param error
     *            The error that triggered the event
     * @param mustCleanCurrentConnection
     *            True if the connection must be cleaned
     */
    public void onError(Throwable error, boolean mustCleanCurrentConnection);

    /**
     * Sets the handler for the "Received" event
     */
    public void received(MessageReceivedHandler handler);

    public void onReceived(JsonElement message);

    /**
     * Sets the handler for the "ConnectionSlow" event
     */
    public void connectionSlow(Runnable handler);

    /**
     * Sets the handler for the "Closed" event
     */
    public void closed(Runnable handler);

    /**
     * Returns the connection token
     */
    public String getConnectionToken();

    /**
     * Returns the connection Id
     */
    public String getConnectionId();

    /**
     * Returns the query string used by the connection
     */
    public String getQueryString();

    /**
     * Returns the current message Id
     */
    public String getMessageId();

    /**
     * Returns the connection groups token
     */
    public String getGroupsToken();

    /**
     * Returns the data used by the connection
     */
    public String getConnectionData();

    /**
     * Returns the connection state
     */
    public ConnectionState getState();

    /**
     * Starts the connection
     * 
     * @param transport
     *            Transport to be used by the connection
     * @return Future for the operation
     */
    public SignalRFuture<Void> start(ClientTransport transport);

    /**
     * Aborts the connection and closes it
     */
    public void stop();

    /**
     * Closes the connection
     */
    public void disconnect();

    /**
     * Sends data using the connection
     * 
     * @param data
     *            Data to send
     * @return Future for the operation
     */
    public SignalRFuture<Void> send(String data);

    /**
     * Prepares a request that is going to be sent to the server
     * 
     * @param request
     *            The request to prepare
     */
    void prepareRequest(Request request);

    /**
     * Returns the connection headers
     */
    Map<String, String> getHeaders();

    /**
     * Returns the Gson instance used by the connection
     */
    Gson getGson();

    /**
     * Sets the Gson instance used by the connection
     */
    void setGson(Gson gson);

    /**
     * Returns the JsonParser used by the connection
     */
    JsonParser getJsonParser();

    /**
     * Returns the Logger used by the connection
     */
    public Logger getLogger();
}