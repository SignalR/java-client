/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.test.integration;

import java.util.concurrent.Future;

import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.test.integration.framework.TestCase;
import microsoft.aspnet.signalr.client.test.integration.framework.TestExecutionCallback;

public interface TestPlatformContext {

    Logger getLogger();

    String getServerUrl();

    String getLogPostUrl();

    Future<Void> showMessage(String message);

    void executeTest(TestCase testCase, TestExecutionCallback callback);

    void sleep(int seconds) throws Exception;
}
