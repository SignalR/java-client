/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

/**
 * Interface to define a Logger
 */
public interface Logger {
    /**
     * Logs a message
     * 
     * @param message
     *            Message to log
     * @param level
     *            Message level
     */
    public void log(String message, LogLevel level);
}
