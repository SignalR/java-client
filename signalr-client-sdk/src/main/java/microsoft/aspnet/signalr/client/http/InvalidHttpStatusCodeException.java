/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.http;

/**
 * Exception thrown when an invalid HTTP Status code is received
 */
public class InvalidHttpStatusCodeException extends Exception {

    private static final long serialVersionUID = 7073157073424850921L;

    public InvalidHttpStatusCodeException(int statusCode, String responseContent, String responseHeaders) {
        super("Invalid status code: " + statusCode + "\nResponse: " + responseContent + "\nHeaders: " + responseHeaders);
    }
}
