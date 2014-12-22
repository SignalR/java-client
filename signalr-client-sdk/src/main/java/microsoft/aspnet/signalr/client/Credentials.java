/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

import microsoft.aspnet.signalr.client.http.Request;

/**
 * Interface for credentials to be sent in a request
 */
public interface Credentials {

    /**
     * Adds the credentials to the request
     * 
     * @param request
     *            The request to prepare
     */
    public void prepareRequest(Request request);
}
