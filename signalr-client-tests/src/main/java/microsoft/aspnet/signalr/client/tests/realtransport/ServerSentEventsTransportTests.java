/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.tests.realtransport;

import microsoft.aspnet.signalr.client.tests.util.TransportType;

public class ServerSentEventsTransportTests extends HttpClientTransportTests {

    @Override
    protected TransportType getTransportType() {
        return TransportType.ServerSentEvents;
    }

}