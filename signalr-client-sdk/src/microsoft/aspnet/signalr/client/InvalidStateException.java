/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

/**
 * Exception to indicate that an operation is not allowed with the connection in
 * a specific state
 */
public class InvalidStateException extends RuntimeException {
    private static final long serialVersionUID = 2754012197945989794L;

    public InvalidStateException(ConnectionState connectionState) {
        super("The operation is not allowed in the '" + connectionState.toString() + "' state");
    }
}
