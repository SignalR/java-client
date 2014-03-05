/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.hubs;

/**
 * Represents a Hub Exception
 */
public class HubException extends Exception {

    private static final long serialVersionUID = 5958638666959902780L;
    private Object mErrorData;

    /**
     * Creates a new Hub exception
     * 
     * @param error
     *            The error message
     * @param errorData
     *            The error data
     */
    public HubException(String error, Object errorData) {
        super(error);

        mErrorData = errorData;
    }

    /**
     * Returns the error data
     */
    public Object getErrorData() {
        return mErrorData;
    }
}
