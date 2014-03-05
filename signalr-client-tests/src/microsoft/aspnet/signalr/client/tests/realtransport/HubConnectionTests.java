/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
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