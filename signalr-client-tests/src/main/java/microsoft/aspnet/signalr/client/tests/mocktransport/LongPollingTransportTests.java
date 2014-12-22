/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.tests.mocktransport;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import microsoft.aspnet.signalr.client.NullLogger;
import microsoft.aspnet.signalr.client.tests.util.MockHttpConnection;
import microsoft.aspnet.signalr.client.tests.util.MockHttpConnection.RequestEntry;
import microsoft.aspnet.signalr.client.tests.util.MockConnection;
import microsoft.aspnet.signalr.client.tests.util.MultiResult;
import microsoft.aspnet.signalr.client.tests.util.Sync;
import microsoft.aspnet.signalr.client.tests.util.TransportType;
import microsoft.aspnet.signalr.client.tests.util.Utils;
import microsoft.aspnet.signalr.client.transport.ConnectionType;
import microsoft.aspnet.signalr.client.transport.DataResultCallback;
import microsoft.aspnet.signalr.client.transport.LongPollingTransport;

public class LongPollingTransportTests extends HttpClientTransportTests {

    @Before
    public void setUp() {
        Sync.reset();
    }

    @Test
    public void testSupportKeepAlive() throws Exception {
        MockHttpConnection httpConnection = new MockHttpConnection();
        LongPollingTransport transport = new LongPollingTransport(new NullLogger(), httpConnection);

        assertFalse(transport.supportKeepAlive());
    }

    @Test
    public void testStart() throws Exception {

        MockHttpConnection httpConnection = new MockHttpConnection();
        LongPollingTransport transport = new LongPollingTransport(new NullLogger(), httpConnection);

        MockConnection connection = new MockConnection();

        final String dataLock1 = "dataLock1" + getTransportType().toString();
        final String dataLock2 = "dataLock2" + getTransportType().toString();

        final MultiResult result = new MultiResult();
        result.stringResult = "";
        result.booleanResult = true;
        result.futureResult = transport.start(connection, ConnectionType.InitialConnection, new DataResultCallback() {

            @Override
            public void onData(String data) {
                result.listResult.add(data);

                if (result.booleanResult) {
                    result.booleanResult = false;
                    Sync.complete(dataLock1);
                } else {
                    Sync.complete(dataLock2);
                    // the second time, trigger the end of the long polling
                    result.futureResult.cancel();
                }
            }
        });

        RequestEntry entry = httpConnection.getRequest();

        String originalUrl = entry.request.getUrl();

        entry.response.writeLine("Hello");
        entry.response.writeLine("World");

        Utils.finishMessage(entry);

        entry = httpConnection.getRequest();
        entry.response.writeLine("Goodbye");
        entry.response.writeLine("World");

        Utils.finishMessage(entry);

        Sync.waitComplete(dataLock1);
        Sync.waitComplete(dataLock2);

        String startUrl = connection.getUrl() + "connect?transport=longPolling&connectionToken=" + Utils.encode(connection.getConnectionToken())
                + "&connectionId=" + Utils.encode(connection.getConnectionId()) + "&messageId=" + Utils.encode(connection.getMessageId()) + "&groupsToken="
                + Utils.encode(connection.getGroupsToken()) + "&connectionData=" + Utils.encode(connection.getConnectionData()) + "&"
                + connection.getQueryString();

        assertEquals(startUrl, originalUrl);

        String pollUrl = startUrl.replace("connect?", "poll?");

        assertEquals(pollUrl, entry.request.getUrl());

        assertEquals("Hello\nWorld", result.listResult.get(0).toString().trim());
        assertEquals("Goodbye\nWorld", result.listResult.get(1).toString().trim());
        assertTrue(result.futureResult.isDone());
    }

    @Override
    protected TransportType getTransportType() {
        return TransportType.LongPolling;
    }

}