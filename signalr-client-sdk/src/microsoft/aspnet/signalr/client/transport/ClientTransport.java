/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.transport;

import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.ConnectionBase;

/**
 * Represents a client implementation of a network transport
 */
public interface ClientTransport {

    /**
     * Transport name
     */
    public String getName();

    /**
     * True if the transport supports keepalive messages
     */
    public boolean supportKeepAlive();

    /**
     * Begins the negotiation with the server
     * 
     * @param connection
     *            Connection information to do the negotiation
     * @return A Future for the operation
     */
    public SignalRFuture<NegotiationResponse> negotiate(ConnectionBase connection);

    /**
     * Starts the transport
     * 
     * @param connection
     *            Connection information to start the transport
     * @param connectionType
     *            Connection type
     * @param callback
     *            Callback to invoke when there is new data
     * @return A Future for the operation
     */
    public SignalRFuture<Void> start(ConnectionBase connection, ConnectionType connectionType, DataResultCallback callback);

    /**
     * Sends data using the transport
     * 
     * @param connection
     *            Connection information to send data
     * @param data
     *            data to send
     * @param callback
     *            Callback to invoke when data is returned
     * @return A Future for the operation
     */
    public SignalRFuture<Void> send(ConnectionBase connection, String data, DataResultCallback callback);

    /**
     * Aborts the transport
     * 
     * @param connection
     *            Connection information to abort
     * @return A Future for the operation
     */
    public SignalRFuture<Void> abort(ConnectionBase connection);
}
