/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.test.integration.framework;

import microsoft.aspnet.signalr.client.test.integration.ApplicationContext;


public abstract class TestCase {
	private String mName;

	private String mDescription;

	private Class<?> mExpectedExceptionClass;

	private boolean mEnabled;

	private TestStatus mStatus;

	private StringBuilder mTestLog;

	public TestCase(String name) {
		mEnabled = false;
		mStatus = TestStatus.NotRun;
		mTestLog = new StringBuilder();
		mName = name;
	}
	
	public TestCase() {
		this(null);
	}

	public void log(String log) {
		mTestLog.append(log);
		mTestLog.append("\n");
	}

	public String getLog() {
		return mTestLog.toString();
	}
	
	public void clearLog() {
		mTestLog = new StringBuilder();
	}

	public TestStatus getStatus() {
		return mStatus;
	}

	public void setStatus(TestStatus status) {
		mStatus = status;
	}

	public boolean isEnabled() {
		return mEnabled;
	}

	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}

	public void run(TestExecutionCallback callback) {
		try {
			if (callback != null)
				callback.onTestStart(this);
		} catch (Exception e) {
			// do nothing
		}
		mStatus = TestStatus.Running;
		try {
		    ApplicationContext.executeTest(this, callback); 
		} catch (Exception e) {
			StackTraceElement[] stackTrace = e.getStackTrace();
			for (int i = 0; i < stackTrace.length; i++) {
				log("  " + stackTrace[i].toString());
			}
			
			TestResult result;
			if (e.getClass() != this.getExpectedExceptionClass()) {
				result = createResultFromException(e);
				mStatus = result.getStatus();
			} else {
				result = new TestResult();
				result.setException(e);
				result.setStatus(TestStatus.Passed);
				result.setTestCase(this);
				mStatus = result.getStatus();
			}

			if (callback != null)
				callback.onTestComplete(this, result);
		}
	}

	public abstract TestResult executeTest();

	protected TestResult createResultFromException(Exception e) {
		return createResultFromException(new TestResult(), e);
	}

	protected TestResult createResultFromException(TestResult result, Exception e) {
		result.setException(e);
		result.setTestCase(this);

		result.setStatus(TestStatus.Failed);

		return result;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	public void setExpectedExceptionClass(Class<?> expectedExceptionClass) {
		mExpectedExceptionClass = expectedExceptionClass;
	}

	public Class<?> getExpectedExceptionClass() {
		return mExpectedExceptionClass;
	}
}
