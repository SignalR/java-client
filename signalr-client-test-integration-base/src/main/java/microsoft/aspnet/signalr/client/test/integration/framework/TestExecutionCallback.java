/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.test.integration.framework;

import java.util.List;

public interface TestExecutionCallback {
	public void onTestStart(TestCase test);

	public void onTestComplete(TestCase test, TestResult result);

	public void onTestGroupComplete(TestGroup group, List<TestResult> results);
}
