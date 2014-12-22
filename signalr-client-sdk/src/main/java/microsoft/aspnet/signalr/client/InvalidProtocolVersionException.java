/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

/**
 * Exception to indicate that the protocol version is different than expected
 */
public class InvalidProtocolVersionException extends Exception {

    private static final long serialVersionUID = -1655367340327068570L;

    public InvalidProtocolVersionException(String version) {
        super("Invalid protocol version " + version);
    }
}
