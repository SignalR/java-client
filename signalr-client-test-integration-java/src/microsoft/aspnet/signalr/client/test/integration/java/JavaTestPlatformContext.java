/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.test.integration.java;

import java.util.Scanner;
import java.util.concurrent.Future;

import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.test.integration.TestPlatformContext;
import microsoft.aspnet.signalr.client.test.integration.framework.TestCase;
import microsoft.aspnet.signalr.client.test.integration.framework.TestExecutionCallback;
import microsoft.aspnet.signalr.client.test.integration.framework.TestResult;

public class JavaTestPlatformContext implements TestPlatformContext {

    private boolean mLoggingEnabled = false;
    private String mServerUrl;
    
    public JavaTestPlatformContext(String serverUrl) {
        mServerUrl = serverUrl;
    }
    
    public void setLoggingEnabled(boolean loggingEnabled) {
        mLoggingEnabled = loggingEnabled;
    }
    
    @Override
    public Logger getLogger() {
        return new Logger() {
            
            @Override
            public void log(String message, LogLevel level) {
                if (mLoggingEnabled) {
                    System.out.println("LOG: " + level.toString() + ": " + message);
                }
            }
        };
    }

    @Override
    public String getServerUrl() {
        return mServerUrl;
    }

    @Override
    public String getLogPostUrl() {
        return "http://not-supported/";
    }

    @Override
    public Future<Void> showMessage(String message) {
        SignalRFuture<Void> future = new SignalRFuture<Void>();
        
        System.out.println(message);
        System.out.println("Press any key to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.next();
        
        future.setResult(null);
        
        return future;
    }

    @Override
    public void executeTest(TestCase testCase, TestExecutionCallback callback) {
        TestResult result = testCase.executeTest();
        callback.onTestComplete(testCase, result);
    }

    @Override
    public void sleep(int seconds) throws Exception {
        System.out.println("Sleeping for " + seconds + " seconds...");
        Thread.sleep(seconds * 1000);
        System.out.println("Woke up");
    }
}
