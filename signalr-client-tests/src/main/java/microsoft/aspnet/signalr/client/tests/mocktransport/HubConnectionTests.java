/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.tests.mocktransport;

import static org.junit.Assert.*;
import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.NullLogger;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.Subscription;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler2;
import microsoft.aspnet.signalr.client.tests.util.MockClientTransport;
import microsoft.aspnet.signalr.client.tests.util.MultiResult;
import microsoft.aspnet.signalr.client.tests.util.Utils;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class HubConnectionTests {

    private static final String SERVER_URL = "http://myUrl.com/";

    @Test
    public void testConnectionDefaultUrl() throws Exception {
        HubConnection connection = new HubConnection(SERVER_URL, "", true, new NullLogger());

        assertEquals(SERVER_URL + "signalr/", connection.getUrl());
    }

    @Test
    public void testConnectionData() throws Exception {

        HubConnection connection = new HubConnection(SERVER_URL, "", true, new NullLogger());

        connection.createHubProxy("myProxy1");
        connection.createHubProxy("myProxy2");

        assertEquals(connection.getConnectionData(), "[{\"name\":\"myproxy1\"},{\"name\":\"myproxy2\"}]");
    }

    @Test
    public void testInvoke() throws Exception {

        MockClientTransport transport = new MockClientTransport();
        HubConnection connection = new HubConnection(SERVER_URL, "", true, new NullLogger());

        HubProxy proxy = connection.createHubProxy("myProxy1");

        connection.start(transport);
        transport.negotiationFuture.setResult(Utils.getDefaultNegotiationResponse());
        transport.startOperation.future.setResult(null);
        transport.startOperation.callback.onData("{\"S\":1}");

        final MultiResult multiResult = new MultiResult();
        multiResult.booleanResult = false;

        String method = "myMethod";
        final String arg1 = "arg1";
        final int arg2 = 2;

        InvocationResult arg = new InvocationResult();
        arg.prop1 = arg1;
        arg.prop2 = arg2;

        proxy.invoke(InvocationResult.class, method, arg).done(new Action<InvocationResult>() {

            @Override
            public void run(InvocationResult result) throws Exception {
                multiResult.booleanResult = true;

                assertEquals(arg1, result.prop1);
                assertEquals(arg2, result.prop2);
            }
        });

        transport.sendOperation.future.setResult(null);

        JsonObject expectedSendData = new JsonObject();
        expectedSendData.addProperty("I", "0");
        expectedSendData.addProperty("H", "myProxy1");
        expectedSendData.addProperty("M", method);
        JsonArray sentArguments = new JsonArray();
        JsonObject jsonArg1 = new JsonObject();
        jsonArg1.addProperty("prop1", arg1);
        jsonArg1.addProperty("prop2", arg2);
        sentArguments.add(jsonArg1);
        expectedSendData.add("A", sentArguments);

        assertEquals(expectedSendData.toString(), transport.sendOperation.data.toString());

        JsonObject jsonResult = new JsonObject();
        jsonResult.addProperty("I", "0");
        jsonResult.add("R", jsonArg1);

        transport.startOperation.callback.onData(jsonResult.toString());

        assertTrue(multiResult.booleanResult);
    }

    @Test
    public void testDynamicSubscriptionHandler() throws Exception {

        MockClientTransport transport = new MockClientTransport();
        HubConnection connection = new HubConnection(SERVER_URL, "", true, new NullLogger());

        HubProxy proxy = connection.createHubProxy("myProxy1");

        final MultiResult multiResult = new MultiResult();

        final String pString = "p1";
        final int pInt = 1;

        proxy.subscribe(new Object() {
            @SuppressWarnings("unused")
            public void message1(String arg1, int arg2) {
                assertEquals(pString, arg1);
                assertEquals(pInt, arg2);

                multiResult.listResult.add(1);
            }

            @SuppressWarnings("unused")
            public void message2(int arg1, String arg2) {
                assertEquals(pInt, arg1);
                assertEquals(pString, arg2);

                multiResult.listResult.add(2);
            }
        });

        connection.start(transport);
        transport.negotiationFuture.setResult(Utils.getDefaultNegotiationResponse());
        transport.startOperation.future.setResult(null);

        JsonObject message = new JsonObject();

        // message1
        JsonObject hubMessage1 = new JsonObject();
        hubMessage1.addProperty("H", "myproxy1");
        hubMessage1.addProperty("M", "message1");
        JsonArray jsonArgs = new JsonArray();
        jsonArgs.add(new JsonPrimitive(pString));
        jsonArgs.add(new JsonPrimitive(1));
        hubMessage1.add("A", jsonArgs);
        JsonArray messageArray = new JsonArray();
        messageArray.add(hubMessage1);

        // message2
        JsonObject hubMessage2 = new JsonObject();
        hubMessage2.addProperty("H", "myproxy1");
        hubMessage2.addProperty("M", "message2");
        jsonArgs = new JsonArray();
        jsonArgs.add(new JsonPrimitive(1));
        jsonArgs.add(new JsonPrimitive(pString));
        hubMessage2.add("A", jsonArgs);
        messageArray.add(hubMessage2);

        message.add("M", messageArray);

        transport.startOperation.callback.onData(message.toString());

        assertEquals(2, multiResult.listResult.size());
        assertEquals(1, multiResult.listResult.get(0));
        assertEquals(2, multiResult.listResult.get(1));
    }

    @Test
    public void testOnSubscriptionHandler() throws Exception {

        MockClientTransport transport = new MockClientTransport();
        HubConnection connection = new HubConnection(SERVER_URL, "", true, new NullLogger());

        HubProxy proxy = connection.createHubProxy("myProxy1");

        final MultiResult multiResult = new MultiResult();

        final String pString = "p1";
        final int pInt = 1;

        proxy.on("message1", new SubscriptionHandler2<String, Integer>() {

            @Override
            public void run(String arg1, Integer arg2) {
                assertEquals(pString, arg1);
                assertEquals(pInt, (int) arg2);

                multiResult.listResult.add(1);
            }
        }, String.class, Integer.class);

        proxy.on("message2", new SubscriptionHandler2<Integer, String>() {

            @Override
            public void run(Integer arg1, String arg2) {
                assertEquals(pInt, (int) arg1);
                assertEquals(pString, arg2);

                multiResult.listResult.add(2);
            }
        }, Integer.class, String.class);

        connection.start(transport);
        transport.negotiationFuture.setResult(Utils.getDefaultNegotiationResponse());
        transport.startOperation.future.setResult(null);

        JsonObject message = new JsonObject();

        // message1
        JsonObject hubMessage1 = new JsonObject();
        hubMessage1.addProperty("H", "myproxy1");
        hubMessage1.addProperty("M", "message1");
        JsonArray jsonArgs = new JsonArray();
        jsonArgs.add(new JsonPrimitive(pString));
        jsonArgs.add(new JsonPrimitive(1));
        hubMessage1.add("A", jsonArgs);
        JsonArray messageArray = new JsonArray();
        messageArray.add(hubMessage1);

        // message2
        JsonObject hubMessage2 = new JsonObject();
        hubMessage2.addProperty("H", "myproxy1");
        hubMessage2.addProperty("M", "message2");
        jsonArgs = new JsonArray();
        jsonArgs.add(new JsonPrimitive(1));
        jsonArgs.add(new JsonPrimitive(pString));
        hubMessage2.add("A", jsonArgs);
        messageArray.add(hubMessage2);

        message.add("M", messageArray);

        transport.startOperation.callback.onData(message.toString());

        assertEquals(2, multiResult.listResult.size());
        assertEquals(1, multiResult.listResult.get(0));
        assertEquals(2, multiResult.listResult.get(1));
    }

    @Test
    public void testSubscription() throws Exception {

        MockClientTransport transport = new MockClientTransport();
        HubConnection connection = new HubConnection(SERVER_URL, "", true, new NullLogger());

        HubProxy proxy = connection.createHubProxy("myProxy1");

        final MultiResult multiResult = new MultiResult();

        final String pString = "p1";
        final int pInt = 1;

        Subscription sub1 = proxy.subscribe("message1");

        sub1.addReceivedHandler(new Action<JsonElement[]>() {

            @Override
            public void run(JsonElement[] obj) throws Exception {
                assertEquals(pString, obj[0].getAsString());
                assertEquals(pInt, obj[1].getAsInt());

                multiResult.listResult.add(1);
            }
        });

        Subscription sub2 = proxy.subscribe("message2");

        sub2.addReceivedHandler(new Action<JsonElement[]>() {

            @Override
            public void run(JsonElement[] obj) throws Exception {
                assertEquals(pInt, obj[0].getAsInt());
                assertEquals(pString, obj[1].getAsString());

                multiResult.listResult.add(2);
            }
        });

        connection.start(transport);
        transport.negotiationFuture.setResult(Utils.getDefaultNegotiationResponse());
        transport.startOperation.future.setResult(null);

        JsonObject message = new JsonObject();

        // message1
        JsonObject hubMessage1 = new JsonObject();
        hubMessage1.addProperty("H", "myproxy1");
        hubMessage1.addProperty("M", "message1");
        JsonArray jsonArgs = new JsonArray();
        jsonArgs.add(new JsonPrimitive(pString));
        jsonArgs.add(new JsonPrimitive(1));
        hubMessage1.add("A", jsonArgs);
        JsonArray messageArray = new JsonArray();
        messageArray.add(hubMessage1);

        // message2
        JsonObject hubMessage2 = new JsonObject();
        hubMessage2.addProperty("H", "myproxy1");
        hubMessage2.addProperty("M", "message2");
        jsonArgs = new JsonArray();
        jsonArgs.add(new JsonPrimitive(1));
        jsonArgs.add(new JsonPrimitive(pString));
        hubMessage2.add("A", jsonArgs);
        messageArray.add(hubMessage2);

        message.add("M", messageArray);

        transport.startOperation.callback.onData(message.toString());

        assertEquals(2, multiResult.listResult.size());
        assertEquals(1, multiResult.listResult.get(0));
        assertEquals(2, multiResult.listResult.get(1));
    }

    @Test
    public void setHubConnectionHeaders(){
        HubConnection connection = new HubConnection(SERVER_URL, "", true, new NullLogger());
        connection.getHeaders().put("key", "value");

        assertEquals(1, connection.getHeaders().values().size());
        assertEquals("value", connection.getHeaders().get("key"));
    }

    @Test
    public void testMultipleSubscriptionForEvent() throws Exception {

        MockClientTransport transport = new MockClientTransport();
        HubConnection connection = new HubConnection(SERVER_URL, "", true, new NullLogger());

        HubProxy proxy = connection.createHubProxy("myProxy1");

        final MultiResult multiResult = new MultiResult();

        final String pString = "p1";

        proxy.subscribe(new Object() {
            @SuppressWarnings("unused")
            public void message1(String arg1, int arg2) {
                multiResult.listResult.add(1);
            }
        });

        proxy.subscribe(new Object() {
            @SuppressWarnings("unused")
            public void message1(String arg1, int arg2) {
                multiResult.listResult.add(1);
            }
        });

        connection.start(transport);
        transport.negotiationFuture.setResult(Utils.getDefaultNegotiationResponse());
        transport.startOperation.future.setResult(null);

        JsonObject message = new JsonObject();

        // message1
        JsonObject hubMessage1 = new JsonObject();
        hubMessage1.addProperty("H", "myproxy1");
        hubMessage1.addProperty("M", "message1");
        JsonArray jsonArgs = new JsonArray();
        jsonArgs.add(new JsonPrimitive(pString));
        jsonArgs.add(new JsonPrimitive(1));
        hubMessage1.add("A", jsonArgs);
        JsonArray messageArray = new JsonArray();
        messageArray.add(hubMessage1);
        message.add("M", messageArray);

        transport.startOperation.callback.onData(message.toString());

        assertEquals(2, multiResult.listResult.size());
    }

    public class InvocationResult {
        public String prop1;
        public int prop2;
    }

}