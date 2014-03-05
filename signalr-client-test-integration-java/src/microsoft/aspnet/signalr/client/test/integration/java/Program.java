/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.test.integration.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import microsoft.aspnet.signalr.client.test.integration.ApplicationContext;
import microsoft.aspnet.signalr.client.test.integration.framework.TestCase;
import microsoft.aspnet.signalr.client.test.integration.framework.TestExecutionCallback;
import microsoft.aspnet.signalr.client.test.integration.framework.TestGroup;
import microsoft.aspnet.signalr.client.test.integration.framework.TestResult;
import microsoft.aspnet.signalr.client.test.integration.tests.MiscTests;

public class Program {

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        if (args.length != 1) {
            System.err.println("There must be one argument with the server url.");
            return;
        }
        
        String serverUrl = args[0];
        JavaTestPlatformContext testPlatformContext = new JavaTestPlatformContext(serverUrl);
        testPlatformContext.setLoggingEnabled(false);
        ApplicationContext.setTestPlatformContext(testPlatformContext);
        
        List<TestGroup> testGroups = new ArrayList<TestGroup>();
        testGroups.add(new MiscTests());
        
        List<TestCase> tests = new ArrayList<TestCase>();
        
        for (TestGroup group : testGroups) {
            for (TestCase test : group.getTestCases()) {
                tests.add(test);
            }
        }
        
        final Scanner scanner = new Scanner (System.in);
        String option = "";
        while (!option.equals("q")) {
            System.out.println("Type a test number to execute the test. 'q' to quit:");
            
            for (int i = 0; i < tests.size(); i++) {
                System.out.println(i + ". " + tests.get(i).getName());
            }
            
            option = scanner.next();
            if (!option.equals("q")) {
                int index = -1;
                try {
                    index = Integer.decode(option);
                } catch (NumberFormatException ex) {
                }
                
                if (index > -1 && index < tests.size()) {
                    TestCase test = tests.get(index);
                    
                    test.run(new TestExecutionCallback() {
                        
                        @Override
                        public void onTestStart(TestCase test) {
                            System.out.println("Starting test - " + test.getName());
                        }
                        
                        @Override
                        public void onTestGroupComplete(TestGroup group, List<TestResult> results) {
                        }
                        
                        @Override
                        public void onTestComplete(TestCase test, TestResult result) {
                            String extraData = "";
                            if (result.getException() != null) {
                                extraData = " - " + result.getException().toString();
                            }
                            System.out.println("Test completed - " + test.getName() + " - " + result.getStatus() + extraData);
                            System.out.println("Press any key to continue...");
                            scanner.next();
                        }
                    });
                }
            }
        }
    }

}
