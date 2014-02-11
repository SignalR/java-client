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
import java.util.concurrent.Semaphore;

import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;
import microsoft.aspnet.signalr.client.tests.util.MultiResult;

import org.junit.Test;

public class HubConnectionTests {

    @Test
    public void testInvoke() throws Exception {

        HubConnection connection = new HubConnection(TestData.HUB_URL, TestData.CONNECTION_QUERYSTRING, true, TestData.getLogger());

        HubProxy proxy = connection.createHubProxy(TestData.HUB_NAME);

        connection.start().get();
        
        String data = UUID.randomUUID().toString();
        
        proxy.invoke("TestMethod", data).get();
        
        String lastHubData = TestData.getLastHubData();
        
        assertEquals(data, lastHubData);
    }
    
    @Test
    public void testReceivedMessageForSubscription() throws Exception {
        HubConnection connection = new HubConnection(TestData.HUB_URL, TestData.CONNECTION_QUERYSTRING, true, TestData.getLogger());

        HubProxy proxy = connection.createHubProxy(TestData.HUB_NAME);
        
        final Semaphore semaphore = new Semaphore(0);
        final MultiResult result = new MultiResult();
        proxy.on("testMessage", new SubscriptionHandler1<String>() {
            
            @Override
            public void run(String val) {
                semaphore.release();
                result.stringResult = val;
            }
        }, String.class);

        connection.start().get();
        TestData.triggerHubTestMessage();
        
        semaphore.acquire();
        
        assertNotNull(result.stringResult);
    }
}