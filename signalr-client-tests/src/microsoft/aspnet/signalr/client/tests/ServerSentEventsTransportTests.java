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

package microsoft.aspnet.signalr.client.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import microsoft.aspnet.signalr.client.NullLogger;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.tests.util.MockHttpConnection;
import microsoft.aspnet.signalr.client.tests.util.MockHttpConnection.RequestEntry;
import microsoft.aspnet.signalr.client.tests.util.MockConnection;
import microsoft.aspnet.signalr.client.tests.util.MultiResult;
import microsoft.aspnet.signalr.client.tests.util.Sync;
import microsoft.aspnet.signalr.client.tests.util.TransportType;
import microsoft.aspnet.signalr.client.tests.util.Utils;
import microsoft.aspnet.signalr.client.transport.ConnectionType;
import microsoft.aspnet.signalr.client.transport.DataResultCallback;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;

public class ServerSentEventsTransportTests extends HttpClientTransportTests {

    @Before
    public void setUp() {
        Sync.reset();
    }

    @Test
    public void testSupportKeepAlive() throws Exception {
        MockHttpConnection httpConnection = new MockHttpConnection();
        ServerSentEventsTransport transport = new ServerSentEventsTransport(new NullLogger(), httpConnection);

        assertTrue(transport.supportKeepAlive());
    }

    @Test
    public void testStart() throws Exception {

        MockHttpConnection httpConnection = new MockHttpConnection();
        ServerSentEventsTransport transport = new ServerSentEventsTransport(new NullLogger(), httpConnection);

        MockConnection connection = new MockConnection();

        final MultiResult result = new MultiResult();

        final String dataLock = "dataLock" + getTransportType().toString();

        SignalRFuture<Void> future = transport.start(connection, ConnectionType.InitialConnection, new DataResultCallback() {

            @Override
            public void onData(String data) {
                result.stringResult = data;
                Sync.complete(dataLock);
            }
        });

        RequestEntry entry = httpConnection.getRequest();
        entry.response.writeLine("data: initialized\n\n");
        entry.response.writeLine("data: Hello");
        entry.response.writeLine("world\n\n");

        Utils.finishMessage(entry);

        Sync.waitComplete(dataLock);

        String startUrl = connection.getUrl() + "connect?transport=serverSentEvents&connectionToken=" + Utils.encode(connection.getConnectionToken())
                + "&connectionId=" + Utils.encode(connection.getConnectionId()) + "&messageId=" + Utils.encode(connection.getMessageId()) + "&groupsToken="
                + Utils.encode(connection.getGroupsToken()) + "&connectionData=" + Utils.encode(connection.getConnectionData()) + "&"
                + connection.getQueryString();

        assertEquals(startUrl, entry.request.getUrl());

        assertEquals("Hello\nworld", result.stringResult);
        assertTrue(future.isDone());
    }

    @Override
    protected TransportType getTransportType() {
        return TransportType.LongPolling;
    }

}