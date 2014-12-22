/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.test.integration.android;

import java.net.URI;
import java.util.List;

import microsoft.aspnet.signalr.client.android.test.integration.R;
import microsoft.aspnet.signalr.client.test.integration.ApplicationContext;
import microsoft.aspnet.signalr.client.test.integration.framework.TestCase;
import microsoft.aspnet.signalr.client.test.integration.framework.TestExecutionCallback;
import microsoft.aspnet.signalr.client.test.integration.framework.TestGroup;
import microsoft.aspnet.signalr.client.test.integration.framework.TestResult;
import microsoft.aspnet.signalr.client.test.integration.tests.MiscTests;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {

	private StringBuilder mLog;

	private ListView mTestCaseList;
	
	private Spinner mTestGroupSpinner;

	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// don't restart the activity. Just process the configuration change
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		AndroidTestPlatformContext testPlatformContext = new AndroidTestPlatformContext(this);
		ApplicationContext.setTestPlatformContext(testPlatformContext);
		
		setContentView(R.layout.activity_main);

		mTestCaseList = (ListView) findViewById(R.id.testCaseList);
		TestCaseAdapter testCaseAdapter = new TestCaseAdapter(this, R.layout.row_list_test_case);
		mTestCaseList.setAdapter(testCaseAdapter);

		mTestGroupSpinner = (Spinner) findViewById(R.id.testGroupSpinner);

		ArrayAdapter<TestGroup> testGroupAdapter = new ArrayAdapter<TestGroup>(this, android.R.layout.simple_spinner_item);
		mTestGroupSpinner.setAdapter(testGroupAdapter);
		mTestGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				selectTestGroup(pos);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// do nothing
			}
		});
		
		refreshTestGroupsAndLog();
	}

	private void selectTestGroup(int pos) {
		TestGroup tg = (TestGroup) mTestGroupSpinner.getItemAtPosition(pos);
		List<TestCase> testCases = tg.getTestCases();

		fillTestList(testCases);
	}

	@SuppressWarnings("unchecked")
	private void refreshTestGroupsAndLog() {
		mLog = new StringBuilder();

		ArrayAdapter<TestGroup> adapter = (ArrayAdapter<TestGroup>) mTestGroupSpinner.getAdapter();
		adapter.clear();
		adapter.add(new MiscTests());
		mTestGroupSpinner.setSelection(0);
		selectTestGroup(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(this, SignalRPreferenceActivity.class));
			return true;

		case R.id.menu_run_tests:
			if (ApplicationContext.getServerUrl().trim().equals("") ) {
		        startActivity(new Intent(this, SignalRPreferenceActivity.class));
			} else {
				runTests();
			}
			return true;

		case R.id.menu_check_all:
			changeCheckAllTests(true);
			return true;

		case R.id.menu_uncheck_all:
			changeCheckAllTests(false);
			return true;

		case R.id.menu_reset:
			refreshTestGroupsAndLog();
			return true;

		case R.id.menu_view_log:
			AlertDialog.Builder logDialogBuilder = new AlertDialog.Builder(this);
			logDialogBuilder.setTitle("Log");

			final WebView webView = new WebView(this);
			
			String logContent = TextUtils.htmlEncode(mLog.toString()).replace("\n", "<br />");
			String logHtml = "<html><body><pre>" + logContent + "</pre></body></html>";
			webView.loadData(logHtml, "text/html", "utf-8");
			
			logDialogBuilder.setPositiveButton("Copy", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					clipboardManager.setText(mLog.toString());
				}
			});
			
			final String postContent = mLog.toString();
			
			logDialogBuilder.setNeutralButton("Post data", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new AsyncTask<Void, Void, Void>() {

						@Override
						protected Void doInBackground(Void... params) {
							try {
								String url = ApplicationContext.getLogPostURL();
								if (url != null && url.trim() != "") {
									url = url + "?platform=android";
									HttpPost post = new HttpPost();
									post.setEntity(new StringEntity(postContent, "utf-8"));
									
									post.setURI(new URI(url));
									
									new DefaultHttpClient().execute(post);
								}
							} catch (Exception e) {
								// Wasn't able to post the data. Do nothing
							}
							
							return null;
						}	
					}.execute();
				}
			});

			logDialogBuilder.setView(webView);

			logDialogBuilder.create().show();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void changeCheckAllTests(boolean check) {
		TestGroup tg = (TestGroup) mTestGroupSpinner.getSelectedItem();
		List<TestCase> testCases = tg.getTestCases();

		for (TestCase testCase : testCases) {
			testCase.setEnabled(check);
		}

		fillTestList(testCases);
	}

	private void fillTestList(List<TestCase> testCases) {
		TestCaseAdapter testCaseAdapter = (TestCaseAdapter) mTestCaseList.getAdapter();

		testCaseAdapter.clear();
		for (TestCase testCase : testCases) {
			testCaseAdapter.add(testCase);
		}
	}

	private void runTests() {
		TestGroup group = (TestGroup) mTestGroupSpinner.getSelectedItem();

		group.runTests(new TestExecutionCallback() {

			@Override
			public void onTestStart(TestCase test) {
				TestCaseAdapter adapter = (TestCaseAdapter) mTestCaseList.getAdapter();
				adapter.notifyDataSetChanged();
				log("TEST START", test.getName());
			}

			@Override
			public void onTestGroupComplete(TestGroup group, List<TestResult> results) {
				log("TEST GROUP COMPLETED", group.getName() + " - " + group.getStatus().toString());
				logSeparator();
			}

			@Override
			public void onTestComplete(TestCase test, TestResult result) {
				Throwable e = result.getException();
				String exMessage = "-";
				if (e != null) {
					StringBuilder sb = new StringBuilder();
					while (e != null) {
						sb.append(e.getClass().getSimpleName() + ": ");
						sb.append(e.getMessage());
						sb.append(" // ");
						e = e.getCause();
					}

					exMessage = sb.toString();
				}

				final TestCaseAdapter adapter = (TestCaseAdapter) mTestCaseList.getAdapter();
				
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						adapter.notifyDataSetChanged();
						
					}
					
				});
				log("TEST LOG", test.getLog());
				log("TEST COMPLETED", test.getName() + " - " + result.getStatus().toString() + " - Ex: " + exMessage);
				logSeparator();
			}
		});

	}

	private void logSeparator() {
		mLog.append("\n");
		mLog.append("----\n");
		mLog.append("\n");
	}
	
	@SuppressWarnings("unused")
	private void log(String content) {
		log("Info", content);
	}

	private void log(String title, String content) {
		String message = title + " - " + content;
		Log.d("SIGNALR-TEST-INTEGRATION", message);

		mLog.append(message);
		mLog.append('\n');
	}

	
	/**
	 * Creates a dialog and shows it
	 * 
	 * @param exception
	 *            The exception to show in the dialog
	 * @param title
	 *            The dialog title
	 */
	@SuppressWarnings("unused")
	private void createAndShowDialog(Exception exception, String title) {
		createAndShowDialog(exception.toString(), title);
	}

	/**
	 * Creates a dialog and shows it
	 * 
	 * @param message
	 *            The dialog message
	 * @param title
	 *            The dialog title
	 */
	private void createAndShowDialog(String message, String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(message);
		builder.setTitle(title);
		builder.create().show();
	}

}
