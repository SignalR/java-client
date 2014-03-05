/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.transport;

public class NegotiationException extends Exception {
    public NegotiationException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    private static final long serialVersionUID = 5861305478561443777L;

}
