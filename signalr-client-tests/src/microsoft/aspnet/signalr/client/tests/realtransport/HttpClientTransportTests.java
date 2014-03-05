/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.tests.realtransport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import microsoft.aspnet.signalr.client.Connection;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.tests.util.ConsoleLogger;
import microsoft.aspnet.signalr.client.tests.util.TransportType;
import microsoft.aspnet.signalr.client.tests.util.Utils;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.DataResultCallback;
import microsoft.aspnet.signalr.client.transport.NegotiationResponse;

import org.junit.Test;

public abstract class HttpClientTransportTests {

    protected abstract TransportType getTransportType();

    @Test
    public void testNegotiate() throws Exception {
        ClientTransport transport = Utils.createTransport(getTransportType(), Platform.createHttpConnection(new ConsoleLogger()));

        Connection connection = new Connection(TestData.SERVER_URL, TestData.CONNECTION_QUERYSTRING, TestData.getLogger());
        SignalRFuture<NegotiationResponse> future = transport.negotiate(connection);

        NegotiationResponse negotiationResponse = future.get();

        assertNotNull(negotiationResponse);
        assertNotNull(negotiationResponse.getConnectionId());
        assertNotNull(negotiationResponse.getConnectionToken());
        assertNotNull(negotiationResponse.getDisconnectTimeout());
        assertNotNull(negotiationResponse.getKeepAliveTimeout());
        assertEquals("1.3", negotiationResponse.getProtocolVersion());
        assertNotNull(negotiationResponse.getUrl());
    }

    @Test
    public void testSend() throws Exception {
        ClientTransport transport = Utils.createTransport(getTransportType(), Platform.createHttpConnection(new ConsoleLogger()));
        Connection connection = new Connection(TestData.SERVER_URL, TestData.CONNECTION_QUERYSTRING, TestData.getLogger());

        connection.start(transport).get();
        
        String dataToSend = UUID.randomUUID().toString();

        transport.send(connection, dataToSend, new DataResultCallback() {
            
            @Override
            public void onData(String data) {
                // TODO Auto-generated method stub
                
            }
        }).get();

        String lastSentData = TestData.getLastSentData();
        
        assertEquals(dataToSend, lastSentData);
    }
}