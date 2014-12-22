/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

/**
 * Represents a generic executable action
 * 
 * @param <E>
 *            The action parameter type
 */
public interface Action<E> {

    /**
     * Executes the action
     * 
     * @param obj
     *            The action parameter
     * @throws Exception
     *             An Exception is thrown if there is an error executing the
     *             action
     */
    public void run(E obj) throws Exception;
}
