/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.tests.mocktransport;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.UUID;

import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.Connection;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.tests.util.MockHttpConnection;
import microsoft.aspnet.signalr.client.tests.util.MockHttpConnection.RequestEntry;
import microsoft.aspnet.signalr.client.tests.util.MockConnection;
import microsoft.aspnet.signalr.client.tests.util.MultiResult;
import microsoft.aspnet.signalr.client.tests.util.Sync;
import microsoft.aspnet.signalr.client.tests.util.TransportType;
import microsoft.aspnet.signalr.client.tests.util.Utils;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.DataResultCallback;
import microsoft.aspnet.signalr.client.transport.NegotiationResponse;

public abstract class HttpClientTransportTests {

    protected abstract TransportType getTransportType();

    @Test
    public void testNegotiate() throws Exception {
        final MockHttpConnection httpConnection = new MockHttpConnection();
        ClientTransport transport = Utils.createTransport(getTransportType(), httpConnection);

        Connection connection = new Connection("http://myUrl.com/");
        SignalRFuture<NegotiationResponse> future = transport.negotiate(connection);

        NegotiationResponse negotiation = Utils.getDefaultNegotiationResponse();

        String negotiationContent = Utils.getNegotiationResponseContent(negotiation);

        RequestEntry entry = httpConnection.getRequest();
        entry.response.writeLine(negotiationContent);

        Utils.finishMessage(entry);

        NegotiationResponse negotiationResponse = null;

        negotiationResponse = future.get();

        assertEquals(negotiation.getConnectionId(), negotiationResponse.getConnectionId());
        assertEquals(negotiation.getConnectionToken(), negotiationResponse.getConnectionToken());
        assertEquals(negotiation.getProtocolVersion(), negotiationResponse.getProtocolVersion());
    }

    protected void testSend() throws Exception {
        final MockHttpConnection httpConnection = new MockHttpConnection();
        ClientTransport transport = Utils.createTransport(getTransportType(), httpConnection);

        MockConnection connection = new MockConnection();

        String dataToSend = UUID.randomUUID().toString();
        final MultiResult result = new MultiResult();

        final String dataLock = "dataLock" + getTransportType().toString();

        SignalRFuture<Void> send = transport.send(connection, dataToSend, new DataResultCallback() {

            @Override
            public void onData(String receivedData) {
                result.stringResult = receivedData.trim();
                Sync.complete(dataLock);
            }
        });

        RequestEntry entry = httpConnection.getRequest();
        entry.response.writeLine(entry.request.getContent());

        Utils.finishMessage(entry);

        String sendUrl = connection.getUrl() + "send?transport=" + transport.getName() + "&connectionToken=" + Utils.encode(connection.getConnectionToken())
                + "&connectionId=" + Utils.encode(connection.getConnectionId()) + "&connectionData=" + Utils.encode(connection.getConnectionData()) + "&"
                + connection.getQueryString();

        Sync.waitComplete(dataLock);
        assertEquals(sendUrl, entry.request.getUrl());

        assertEquals("data=" + dataToSend + "&", entry.request.getContent());
        assertEquals("data=" + dataToSend + "&", result.stringResult);
        assertTrue(send.isDone());
    }

    @Test
    public void testAbort() throws Exception {
        final MockHttpConnection httpConnection = new MockHttpConnection();

        ClientTransport transport = Utils.createTransport(getTransportType(), httpConnection);

        MockConnection connection = new MockConnection();

        final String connectLock = "connectLock" + getTransportType().toString();

        SignalRFuture<Void> abort = transport.abort(connection);
        abort.done(new Action<Void>() {

            @Override
            public void run(Void obj) throws Exception {
                Sync.complete(connectLock);
            }
        });

        RequestEntry entry = httpConnection.getRequest();
        entry.response.writeLine(entry.request.getContent());

        Utils.finishMessage(entry);

        String abortUrl = connection.getUrl() + "abort?transport=" + transport.getName() + "&connectionToken=" + Utils.encode(connection.getConnectionToken())
                + "&connectionId=" + Utils.encode(connection.getConnectionId()) + "&connectionData=" + Utils.encode(connection.getConnectionData()) + "&"
                + connection.getQueryString();

        Sync.waitComplete(connectLock);
        assertEquals(abortUrl, entry.request.getUrl());
        assertTrue(abort.isDone());
    }

    @Test
    public void testInvalidNegotiationData() throws Exception {
        final MockHttpConnection httpConnection = new MockHttpConnection();
        ClientTransport transport = Utils.createTransport(getTransportType(), httpConnection);

        Connection connection = new Connection("http://myUrl.com/");
        SignalRFuture<NegotiationResponse> future = transport.negotiate(connection);

        final MultiResult result = new MultiResult();
        result.booleanResult = false;
        future.onError(new ErrorCallback() {

            @Override
            public void onError(Throwable error) {
                result.booleanResult = true;
                Sync.complete("invalidNegotiationData");
            }
        });

        future.done(new Action<NegotiationResponse>() {

            @Override
            public void run(NegotiationResponse obj) throws Exception {
                Sync.complete("invalidNegotiationData");
            }
        });

        String invalidNegotiationContent = "bad-data-123";

        RequestEntry entry = httpConnection.getRequest();
        entry.response.writeLine(invalidNegotiationContent);

        Utils.finishMessage(entry);

        Sync.waitComplete("invalidNegotiationData");

        assertTrue(result.booleanResult);
    }

    @Test
    public void testInvalidNegotiationJsonData() throws Exception {
        final MockHttpConnection httpConnection = new MockHttpConnection();
        ClientTransport transport = Utils.createTransport(getTransportType(), httpConnection);

        Connection connection = new Connection("http://myUrl.com/");
        SignalRFuture<NegotiationResponse> future = transport.negotiate(connection);

        final MultiResult result = new MultiResult();
        result.booleanResult = false;
        future.onError(new ErrorCallback() {

            @Override
            public void onError(Throwable error) {
                result.booleanResult = true;
                Sync.complete("invalidNegotiationData");
            }
        });

        future.done(new Action<NegotiationResponse>() {

            @Override
            public void run(NegotiationResponse obj) throws Exception {
                Sync.complete("invalidNegotiationData");
            }
        });

        String invalidNegotiationContent = "{\"myValue\":\"bad-data-123\"}";

        RequestEntry entry = httpConnection.getRequest();
        entry.response.writeLine(invalidNegotiationContent);

        Utils.finishMessage(entry);

        Sync.waitComplete("invalidNegotiationData");

        assertTrue(result.booleanResult);
    }
}