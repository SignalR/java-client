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