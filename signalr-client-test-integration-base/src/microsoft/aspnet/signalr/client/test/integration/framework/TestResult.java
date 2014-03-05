/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.test.integration.framework;

public class TestResult {
	private TestStatus mStatus;

	private Exception mException;

	private TestCase mTestCase;

	public TestStatus getStatus() {
		return mStatus;
	}

	public void setStatus(TestStatus status) {
		this.mStatus = status;
	}

	public Exception getException() {
		return mException;
	}

	public void setException(Exception e) {
		this.mException = e;
	}

	public TestCase getTestCase() {
		return mTestCase;
	}

	public void setTestCase(TestCase testCase) {
		mTestCase = testCase;
	}
}