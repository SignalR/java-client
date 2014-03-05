/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.hubs;

public interface SubscriptionHandler4<E1, E2, E3, E4> {
    public void run(E1 p1, E2 p2, E3 p3, E4 p4);
}
