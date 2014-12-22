/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.tests.realtransport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import microsoft.aspnet.signalr.client.Connection;
import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.MessageReceivedHandler;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.StateChangedCallback;
import microsoft.aspnet.signalr.client.tests.util.MultiResult;
import microsoft.aspnet.signalr.client.tests.util.Utils;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ConnectionTests {

    @Test
    public void testStart() throws Exception {

        Connection connection = new Connection(TestData.SERVER_URL, TestData.CONNECTION_QUERYSTRING, TestData.getLogger());

        final List<ConnectionState> newStates = new ArrayList<ConnectionState>();
        
        connection.stateChanged(new StateChangedCallback() {
            
            @Override
            public void stateChanged(ConnectionState oldState, ConnectionState newState) {
                newStates.add(newState);
            }
        });
        
        SignalRFuture<Void> startFuture = connection.start();
        
        startFuture.get();
        
        assertEquals(2, newStates.size());
        assertEquals(ConnectionState.Connecting, newStates.get(0));
        assertEquals(ConnectionState.Connected, newStates.get(1));
        assertEquals(ConnectionState.Connected, connection.getState());
        
        assertTrue(startFuture.isDone());
        
        connection.disconnect();
    }

    
    @Test
    public void testMessageReceived() throws Exception {

        Connection connection = new Connection(TestData.SERVER_URL, TestData.CONNECTION_QUERYSTRING, TestData.getLogger());

        final MultiResult result = new MultiResult();

        Utils.addResultHandlersToConnection(connection, result, true);

        connection.start().get();

        final Semaphore semaphore = new Semaphore(0);
        connection.received(new MessageReceivedHandler() {

            @Override
            public void onMessageReceived(JsonElement json) {
                result.listResult.add(json);
                semaphore.release();
            }
        });
        
        TestData.triggerTestMessage();

        
        semaphore.acquire();
        
        assertEquals(1, result.listResult.size());
        JsonPrimitive json = (JsonPrimitive) result.listResult.get(0);
        String message = json.getAsString();
        
        assertEquals("test message", message);
        
        connection.disconnect();
    }


   

    @Test
    public void testSendMessage() throws Exception {
        Connection connection = new Connection(TestData.SERVER_URL, TestData.CONNECTION_QUERYSTRING, TestData.getLogger());

        final MultiResult result = new MultiResult();

        Utils.addResultHandlersToConnection(connection, result, true);

        connection.start().get();

        String dataToSend = UUID.randomUUID().toString();
        connection.send(dataToSend).get();
        
        String lastSentData = TestData.getLastSentData();

        assertEquals(dataToSend, lastSentData);
        
        connection.disconnect();
    }

    @Test
    public void testStop() throws Exception {
        
        Connection connection = new Connection(TestData.SERVER_URL, TestData.CONNECTION_QUERYSTRING, TestData.getLogger());

        connection.start().get();
        
        final MultiResult result = new MultiResult();
        assertEquals(ConnectionState.Connected, connection.getState());

        final Semaphore semaphore = new Semaphore(0);
        connection.closed(new Runnable() {

            @Override
            public void run() {
                result.intResult++;
                semaphore.release();
            }
        });

        connection.stop();

        semaphore.acquire();
        
        assertEquals(ConnectionState.Disconnected, connection.getState());
        assertEquals(1, result.intResult);
    }

}