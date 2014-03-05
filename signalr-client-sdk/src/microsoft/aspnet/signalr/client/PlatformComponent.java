/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

import microsoft.aspnet.signalr.client.http.HttpConnection;

public interface PlatformComponent {
    /**
     * Returns a platform-specific HttpConnection
     */
    public HttpConnection createHttpConnection(Logger logger);

    /**
     * Returns a platform-specific Operating System name
     */

    public String getOSName();
}
