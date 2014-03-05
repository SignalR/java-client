/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.tests.realtransport;

import microsoft.aspnet.signalr.client.tests.util.Sync;
import microsoft.aspnet.signalr.client.tests.util.TransportType;

import org.junit.Before;

public class LongPollingTransportTests extends HttpClientTransportTests {

    @Before
    public void setUp() {
        Sync.reset();
    }

  
    @Override
    protected TransportType getTransportType() {
        return TransportType.LongPolling;
    }

}