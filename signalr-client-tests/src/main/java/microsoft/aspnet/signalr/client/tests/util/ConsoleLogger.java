/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.tests.util;

import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;

public class ConsoleLogger implements Logger {

    @Override
    public void log(String message, LogLevel level) {
        System.out.println(level.toString() + " - " + message);
    }

}
