/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.test.integration.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class TestGroup {
	List<TestCase> mTestCases;
	String mName;
	TestStatus mStatus;
	ConcurrentLinkedQueue<TestCase> mTestRunQueue;
	boolean mNewTestRun;
	
	public TestGroup(String name) {
		mName = name;
		mStatus = TestStatus.NotRun;
		mTestCases = new ArrayList<TestCase>();
		mTestRunQueue = new ConcurrentLinkedQueue<TestCase>();
		mNewTestRun = false;
	}

	public TestStatus getStatus() {
		return mStatus;
	}

	public List<TestCase> getTestCases() {
		return mTestCases;
	}

	protected void addTest(TestCase testCase) {
		mTestCases.add(testCase);
	}

	
	public void runTests(TestExecutionCallback callback) {
		List<TestCase> testsToRun = new ArrayList<TestCase>();
		
		for (int i = 0; i < mTestCases.size(); i++) {
			if (mTestCases.get(i).isEnabled()) {
				testsToRun.add(mTestCases.get(i));
			}
		}

		if (testsToRun.size() > 0) {
			runTests(testsToRun, callback);
		}
	}
	
	
	public void runTests(List<TestCase> testsToRun, final TestExecutionCallback callback) {
		try {
			onPreExecute();
		} catch (Exception e) {
			mStatus = TestStatus.Failed;
			if (callback != null)
				callback.onTestGroupComplete(this, null);
			return;
		}
		
		final TestRunStatus testRunStatus = new TestRunStatus();

		mNewTestRun = true;
		
		int oldQueueSize = mTestRunQueue.size();
		mTestRunQueue.clear();
		mTestRunQueue.addAll(testsToRun);
		cleanTestsState();
		testRunStatus.results.clear();
		mStatus = TestStatus.NotRun;
		
		if (oldQueueSize == 0) {
			executeNextTest(callback, testRunStatus);
		}
	}


	private void cleanTestsState() {
		for (TestCase test : mTestRunQueue) {
			test.setStatus(TestStatus.NotRun);
			test.clearLog();
		}
	}
	
	private void executeNextTest(final TestExecutionCallback callback, final TestRunStatus testRunStatus) {
		mNewTestRun = false;
		final TestGroup group = this;

		try {
			TestCase nextTest = mTestRunQueue.poll();
			if (nextTest != null) {
				nextTest.run(new TestExecutionCallback() {
					@Override
					public void onTestStart(TestCase test) {
						if (!mNewTestRun && callback != null)
							callback.onTestStart(test);
					}

					@Override
					public void onTestGroupComplete(TestGroup group, List<TestResult> results) {
						if (!mNewTestRun && callback != null)
							callback.onTestGroupComplete(group, results);
					}

					@Override
					public void onTestComplete(TestCase test, TestResult result) {
						if (mNewTestRun) {
							cleanTestsState();
							testRunStatus.results.clear();
							mStatus = TestStatus.NotRun;
						} else {
							if (test.getExpectedExceptionClass() != null) {
								if (result.getException() != null && result.getException().getClass() == test.getExpectedExceptionClass()) {
									result.setStatus(TestStatus.Passed);
								} else {
									result.setStatus(TestStatus.Failed);
								}
							}
		
							test.setStatus(result.getStatus());
							testRunStatus.results.add(result);
		
							if (callback != null)
								callback.onTestComplete(test, result);
						}
						
						executeNextTest(callback, testRunStatus);
					}
				});
				
				
			} else {
				// end run
				
				try {
					onPostExecute();
				} catch (Exception e) {
					mStatus = TestStatus.Failed;
				}
				
				// if at least one test failed, the test group
				// failed
				if (mStatus != TestStatus.Failed) {
					mStatus = TestStatus.Passed;
					for (TestResult r : testRunStatus.results) {
						if (r.getStatus() == TestStatus.Failed) {
							mStatus = TestStatus.Failed;
							break;
						}
					}
				}

				if (callback != null)
					callback.onTestGroupComplete(group, testRunStatus.results);
			}
			
			
		} catch (Exception e) {
			if (callback != null)
				callback.onTestGroupComplete(this, testRunStatus.results);
		}
	}

	public String getName() {
		return mName;
	}

	protected void setName(String name) {
		mName = name;
	}

	@Override
	public String toString() {
		return getName();
	}

	public void onPreExecute() {

	}

	public void onPostExecute() {

	}

	private class TestRunStatus {
		public List<TestResult> results;

		public TestRunStatus() {
			results = new ArrayList<TestResult>();
		}
	}
}