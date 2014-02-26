/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
Apache 2.0 License
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 
See the Apache Version 2.0 License for specific language governing permissions and limitations under the License.
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