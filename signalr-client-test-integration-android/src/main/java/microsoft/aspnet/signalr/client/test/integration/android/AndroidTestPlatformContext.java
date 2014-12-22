/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.test.integration.android;

import java.util.concurrent.Future;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.test.integration.TestPlatformContext;
import microsoft.aspnet.signalr.client.test.integration.framework.TestCase;
import microsoft.aspnet.signalr.client.test.integration.framework.TestExecutionCallback;
import microsoft.aspnet.signalr.client.test.integration.framework.TestResult;

public class AndroidTestPlatformContext implements TestPlatformContext {

    private static Activity mActivity;
    
    public AndroidTestPlatformContext(Activity activity) {
        mActivity = activity;
        Platform.loadPlatformComponent(new AndroidPlatformComponent());
    }
    
    @Override
    public Logger getLogger() {
        return new Logger() {
            
            @Override
            public void log(String message, LogLevel level) {
                Log.d("SignalR-Integration-Test", level.toString() + ": " + message);
            }
        };
    }

    @Override
    public String getServerUrl() {
        return PreferenceManager.getDefaultSharedPreferences(mActivity).getString(Constants.PREFERENCE_SIGNALR_URL, "");
    }

    @Override
    public String getLogPostUrl() {
        return PreferenceManager.getDefaultSharedPreferences(mActivity).getString(Constants.PREFERENCE_LOG_POST_URL, "");
    }

    @Override
    public Future<Void> showMessage(final String message) {
        final SignalRFuture<Void> result = new SignalRFuture<Void>();
        
        mActivity.runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                
                builder.setTitle("Message");
                builder.setMessage(message);
                builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.setResult(null);
                    }
                });
                
                builder.create().show();

            }
        });
        
        return result;
    }

    @Override
    public void executeTest(final TestCase testCase, final TestExecutionCallback callback) {
        AsyncTask<Void, Void, TestResult> task = new AsyncTask<Void, Void, TestResult>() {

            @Override
            protected TestResult doInBackground(Void... params) {
                return testCase.executeTest();
            }
            
            @Override
            protected void onPostExecute(TestResult result) {
                callback.onTestComplete(testCase, result);
            }
        };
        
        task.execute();
    }

    @Override
    public void sleep(int seconds) throws Exception {
        Thread.sleep(seconds * 1000);
    }

}
