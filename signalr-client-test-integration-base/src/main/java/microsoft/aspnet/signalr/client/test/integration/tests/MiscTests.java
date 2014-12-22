/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.test.integration.tests;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.Credentials;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.MessageReceivedHandler;
import microsoft.aspnet.signalr.client.StateChangedCallback;
import microsoft.aspnet.signalr.client.http.Request;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubException;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.test.integration.ApplicationContext;
import microsoft.aspnet.signalr.client.test.integration.TransportType;
import microsoft.aspnet.signalr.client.test.integration.framework.ExpectedValueException;
import microsoft.aspnet.signalr.client.test.integration.framework.TestCase;
import microsoft.aspnet.signalr.client.test.integration.framework.TestGroup;
import microsoft.aspnet.signalr.client.test.integration.framework.TestResult;
import microsoft.aspnet.signalr.client.test.integration.framework.TestStatus;
import microsoft.aspnet.signalr.client.test.integration.framework.Util;
import microsoft.aspnet.signalr.client.transport.ClientTransport;

public class MiscTests extends TestGroup {
    
	private static final String INTEGRATION_TESTS_HUB_NAME = "integrationTestsHub";

    private TestCase createBasicConnectionFlowTest(String name, final TransportType transportType) {
		TestCase test = new TestCase() {
		    
		    private InteralTestData testData;
		    
			@Override
			public TestResult executeTest() {
				try {
				    final HubConnection connection = ApplicationContext.createHubConnection();
				    ClientTransport transport = ApplicationContext.createTransport(transportType);
				    
				    testData = new InteralTestData();
				    testData.connectionStates.add(connection.getState());
				    connection.stateChanged(new StateChangedCallback() {
                        
                        @Override
                        public void stateChanged(ConnectionState oldState, ConnectionState newState) {
                            testData.connectionStates.add(newState);
                        }
                    });
				    
				    connection.received(new MessageReceivedHandler() {

                        @Override
                        public void onMessageReceived(JsonElement json) {
                            testData.receivedMessages.add(json);
                        }
                    });

				    final Semaphore semaphore = new Semaphore(0);
				    HubProxy proxy = connection.createHubProxy(INTEGRATION_TESTS_HUB_NAME);
                    connection.closed(new Runnable() {
                        
                        @Override
                        public void run() {
                            testData.connectionWasClosed = true;
                            semaphore.release();
                        }
                    });
				    
				    proxy.subscribe(new Object() {
				           
	                    @SuppressWarnings("unused")
                        public void Echo(String data) {
                            testData.receivedData.add(data);
                        }
                    });
	                    
				    String data = UUID.randomUUID().toString();
				    
				    connection.start(transport).get();
				    
				    proxy.setState("myVar", new JsonPrimitive(1));
				    proxy.invoke("echo", data);
				    proxy.invoke("updateState", "myVar", 2);
				    
				    ApplicationContext.sleep();

				    connection.stop();

				    semaphore.acquire();

                    ApplicationContext.sleep();

				    TestResult result = new TestResult();
				    result.setStatus(TestStatus.Passed);
				    result.setTestCase(this);
				    
				    //validations
				    
				    if (!Util.compareArrays(
				            new ConnectionState[] {
				                    ConnectionState.Disconnected, 
				                    ConnectionState.Connecting, 
				                    ConnectionState.Connected, 
				                    ConnectionState.Disconnected},
				            testData.connectionStates.toArray())) {
				        return createResultFromException(new Exception("The connection states were incorrect"));
				    }
				    
				    if (testData.receivedMessages.size() == 0) {
				        return createResultFromException(new Exception("Messages not received"));
				    }
				    
				    if (!testData.connectionWasClosed) {
				        return createResultFromException(new Exception("Conneciton was not closed"));
				    }
				    
				    if (testData.receivedData.size() != 1 || !testData.receivedData.get(0).toString().equals(data)) {
				        return createResultFromException(new Exception("Invalid received data"));
				    }
				    
				    // pending: validate tracing messages
				    
					return result;
				} catch (Exception e) {
					return createResultFromException(e);
				}
			}
		};
		
		test.setName(name);

		return test;
	}

	class InteralTestData {
        List<ConnectionState> connectionStates = new ArrayList<ConnectionState>();
        List<JsonElement> receivedMessages = new ArrayList<JsonElement>();
        boolean connectionWasClosed = false;
        List<Throwable> errors = new ArrayList<Throwable>();
        List<Object> receivedData = new ArrayList<Object>();
    }
	
	private TestCase createErrorHandledAndConnectionContinuesTest(String name, final TransportType transportType) {
        TestCase test = new TestCase() {
            
            InteralTestData testData;
            
            @Override
            public TestResult executeTest() {
                try {
                    HubConnection connection = ApplicationContext.createHubConnection();
                    ClientTransport transport = ApplicationContext.createTransport(transportType);
                    
                    testData = new InteralTestData();
                    
                    connection.received(new MessageReceivedHandler() {

                        @Override
                        public void onMessageReceived(JsonElement json) {
                            testData.receivedMessages.add(json);
                        }
                    });
                    
                    connection.error(new ErrorCallback() {
                        
                        @Override
                        public void onError(Throwable error) {
                            testData.errors.add(error);
                        }
                    });
                    
                    HubProxy proxy = connection.createHubProxy(INTEGRATION_TESTS_HUB_NAME);
                    
                    proxy.subscribe(new Object() {
                        @SuppressWarnings("unused")
                        public void echo(String data) {
                            testData.receivedData.add(data);
                        }
                    });
                    
                    String data = UUID.randomUUID().toString();
                    
                    connection.start(transport).get();
                    
                    proxy.invoke("triggerError");
                    
                    ApplicationContext.sleep();
                    
                    proxy.invoke("echo", data);
                    
                    ApplicationContext.sleep();
                    
                    connection.stop();

                    ApplicationContext.sleep();
                    
                    TestResult result = new TestResult();
                    result.setStatus(TestStatus.Passed);
                    result.setTestCase(this);
                    
                    //validations
                    
                    if (testData.receivedMessages.size() == 0) {
                        return createResultFromException(new Exception("Messages expected"));
                    }
                    
                    if (testData.errors.size() != 1 || testData.errors.get(0).getClass() != HubException.class) {
                        return createResultFromException(new Exception("Expected one error"));
                    }
                    
                    if (testData.receivedData.size() != 1 || !testData.receivedData.get(0).toString().equals(data)) {
                        return createResultFromException(new Exception("Invalid received data"));
                    }
                    
                    // pending: validate tracing messages
                    
                    return result;
                } catch (Exception e) {
                    return createResultFromException(e);
                }
            }
        };
        
        test.setName(name);

        return test;
    }
	
	private TestCase createMessagesToGroupsTest(String name, final TransportType transportType) {
        TestCase test = new TestCase() {
            
            InteralTestData testData;
            
            @Override
            public TestResult executeTest() {
                try {
                    HubConnection connection = ApplicationContext.createHubConnection();
                    ClientTransport transport = ApplicationContext.createTransport(transportType);
                    
                    testData = new InteralTestData();
                    
                    HubProxy proxy = connection.createHubProxy(INTEGRATION_TESTS_HUB_NAME);
                    
                    proxy.subscribe(new Object() {
                        @SuppressWarnings("unused")
                        public void echo(String data) {
                            testData.receivedData.add(data);
                        }
                    });
                    
                    connection.start(transport).get();
                    
                    proxy.invoke("sendMessageToGroup", "group1", "message1").get();
                    
                    ApplicationContext.sleep();
                    
                    proxy.invoke("joinGroup", "group1").get();

                    ApplicationContext.sleep();
                    
                    proxy.invoke("sendMessageToGroup", "group1", "message2").get();
                    
                    ApplicationContext.sleep();
                    
                    proxy.invoke("leaveGroup", "group1").get();
                    
                    ApplicationContext.sleep();
                    
                    proxy.invoke("sendMessageToGroup", "group1", "message3").get();
                    
                    ApplicationContext.sleep();
                    
                    connection.stop();

                    ApplicationContext.sleep();
                    
                    TestResult result = new TestResult();
                    result.setStatus(TestStatus.Passed);
                    result.setTestCase(this);
                    
                    //validations
                    
                    if (testData.receivedData.size() != 1 || !testData.receivedData.get(0).equals("message2")) {
                        return createResultFromException(new Exception("Expected only one message with value 'message2'"));
                    }
                    
                    return result;
                } catch (Exception e) {
                    return createResultFromException(e);
                }
            }
        };
        
        test.setName(name);

        return test;
    }
	
	private TestCase createDisconnectServerTest(String name, final TransportType transportType) {
        TestCase test = new TestCase() {
            
            InteralTestData testData;
            
            @Override
            public TestResult executeTest() {
                try {
                    HubConnection connection = ApplicationContext.createHubConnection();
                    ClientTransport transport = ApplicationContext.createTransport(transportType);
                    
                    testData = new InteralTestData();
                    
                    connection.reconnecting(new Runnable() {
                        
                        @Override
                        public void run() {
                            testData.connectionStates.add(ConnectionState.Reconnecting);
                        }
                    });
                    
                    connection.start(transport).get();
                    
                    ApplicationContext.showMessage("Break connection with the server").get();
                    
                    long current = Calendar.getInstance().getTimeInMillis();
                    
                    while (Calendar.getInstance().getTimeInMillis() - current < 60 * 1000) {
                        if (connection.getState() == ConnectionState.Disconnected) {
                            break;
                        }
                    }
                    
                    TestResult result = new TestResult();
                    result.setStatus(TestStatus.Passed);
                    result.setTestCase(this);
                    
                    ApplicationContext.showMessage("Enable connection with the server").get();
                    
                    //validations
                    
                    if (connection.getState() != ConnectionState.Disconnected) {
                        return createResultFromException(new Exception("Connection should be disconnected"));
                    }
                    
                    if (!testData.connectionStates.contains(ConnectionState.Reconnecting)) {
                        return createResultFromException(new Exception("The client should have tried to reconnect"));
                    }
                    
                    return result;
                } catch (Exception e) {
                    return createResultFromException(e);
                }
            }
        };
        
        test.setName(name);

        return test;
    }
	
	private TestCase createReconnectServerTest(String name, final TransportType transportType) {
        TestCase test = new TestCase() {
            
            InteralTestData testData;
            
            @Override
            public TestResult executeTest() {
                try {
                    HubConnection connection = ApplicationContext.createHubConnection();
                    ClientTransport transport = ApplicationContext.createTransport(transportType);
                    
                    testData = new InteralTestData();
                    
                    connection.reconnecting(new Runnable() {
                        
                        @Override
                        public void run() {
                            testData.connectionStates.add(ConnectionState.Reconnecting);
                        }
                    });
                    
                    connection.start(transport).get();
                    
                    ApplicationContext.showMessage("Break connection with the server for 10 seconds and re-enable it").get();
                    
                    ApplicationContext.sleep(10);
                    
                    TestResult result = new TestResult();
                    result.setStatus(TestStatus.Passed);
                    result.setTestCase(this);
                    
                    //validations
                    
                    if (connection.getState() != ConnectionState.Connected) {
                        return createResultFromException(new Exception("Connection should be connected"));
                    }
                    
                    connection.disconnect();
                    
                    if (!testData.connectionStates.contains(ConnectionState.Reconnecting)) {
                        return createResultFromException(new Exception("The client should have tried to reconnect"));
                    }
                    
                    return result;
                } catch (Exception e) {
                    return createResultFromException(e);
                }
            }
        };
        
        test.setName(name);

        return test;
    }
	
	private TestCase createConnectToUnavailableServerTest(String name, final TransportType transportType) {
        TestCase test = new TestCase() {
            
            InteralTestData testData;
            
            @Override
            public TestResult executeTest() {
                try {
                    HubConnection connection = ApplicationContext.createHubConnectionWithInvalidURL();
                    ClientTransport transport = ApplicationContext.createTransport(transportType);
                    testData = new InteralTestData();
                    
                    connection.start(transport)
                        .onError(new ErrorCallback() {
                            
                            @Override
                            public void onError(Throwable error) {
                                testData.errors.add(error);
                            }
                        });
                    
                    
                    ApplicationContext.sleep();
                    
                    TestResult result = new TestResult();
                    result.setStatus(TestStatus.Passed);
                    result.setTestCase(this);
                    
                    //validations
                    
                    if (connection.getState() != ConnectionState.Disconnected) {
                        return createResultFromException(new Exception("Connection should be disconnected"));
                    }
                    
                    if (testData.errors.size() == 0) {
                        return createResultFromException(new Exception("Exception should have been thrown"));
                    }
                    
                    return result;
                } catch (Exception e) {
                    return createResultFromException(e);
                }
            }
        };
        
        test.setName(name);

        return test;
    }
	

    private TestCase createPendingCallbacksAbortedTest(String name, final TransportType transportType) {
        TestCase test = new TestCase() {
            
            InteralTestData testData;
            
            @Override
            public TestResult executeTest() {
                try {
                    HubConnection connection = ApplicationContext.createHubConnection();
                    ClientTransport transport = ApplicationContext.createTransport(transportType);
                    
                    testData = new InteralTestData();
                    
                    HubProxy proxy = connection.createHubProxy(INTEGRATION_TESTS_HUB_NAME);
                    connection.start(transport).get();
                    
                    proxy.invoke(String.class, "waitAndReturn", 20)
                        .done(new Action<String>() {
                            
                            @Override
                            public void run(String obj) throws Exception {
                                testData.receivedData.add(obj);
                            }
                        })
                        .onError(new ErrorCallback() {
                            
                            @Override
                            public void onError(Throwable error) {
                                testData.errors.add(error);
                            }
                        });
                    
                    connection.stop();
                    
                    ApplicationContext.sleep(15);
                    
                    TestResult result = new TestResult();
                    result.setStatus(TestStatus.Passed);
                    result.setTestCase(this);
                    
                    //validations
                    
                    if (System.getProperty("java.runtime.name").toLowerCase().contains("android")) {
                        // outside android, java lib might not break the connection soon enough to avoid receiving the message callback
                        if (testData.receivedData.size() != 0) {
                            return createResultFromException(new Exception("No result should have been received"));
                        }
                    }
                    
                    if (testData.errors.size() == 0) {
                        return createResultFromException(new Exception("Exception should have been thrown when connection was closed"));
                    }
                    
                    return result;
                } catch (Exception e) {
                    return createResultFromException(e);
                }
            }
        };
        
        test.setName(name);

        return test;
    }
    
    private TestCase createCheckHeaderTest(String name, final TransportType transportType) {
        TestCase test = new TestCase() {
            @Override
            public TestResult executeTest() {
                try {
                    HubConnection connection = ApplicationContext.createHubConnection();
                    ClientTransport transport = ApplicationContext.createTransport(transportType);
                    
                    final String headerName = UUID.randomUUID().toString();
                    final String headerValue = UUID.randomUUID().toString();
                    connection.setCredentials(new Credentials() {
                        
                        @Override
                        public void prepareRequest(Request request) {
                            request.addHeader(headerName, headerValue);
                        }
                    });
                    
                    HubProxy proxy = connection.createHubProxy(INTEGRATION_TESTS_HUB_NAME);
                    connection.start(transport).get();
                    
                    String retValue = proxy.invoke(String.class, "HeaderData", headerName).get();
                    
                    connection.stop();
                    
                    TestResult result = new TestResult();
                    result.setStatus(TestStatus.Passed);
                    result.setTestCase(this);
                    
                    //validations
                    
                    if (!headerValue.equals(retValue)) {
                        return createResultFromException(new ExpectedValueException(headerName, retValue));
                    }
                    
                    return result;
                } catch (Exception e) {
                    return createResultFromException(e);
                }
            }
        };
        
        test.setName(name);

        return test;
    }


	
	
	public MiscTests() {
		super("SignalR tests");

		for (TransportType transportType : TransportType.values()) {
            this.addTest(createBasicConnectionFlowTest("Basic connection flow - " + transportType.name(), transportType));
        }
		
		for (TransportType transportType : TransportType.values()) {
		    this.addTest(createMessagesToGroupsTest("Join and leave groups - " + transportType.name(), transportType));
		}
		
		for (TransportType transportType : TransportType.values()) {
            this.addTest(createErrorHandledAndConnectionContinuesTest("Error handled and connection continues - " + transportType.name(), transportType));
        }
		
		for (TransportType transportType : TransportType.values()) {
            this.addTest(createDisconnectServerTest("Disconnect server after connection - " + transportType.name(), transportType));
        }
		
		for (TransportType transportType : TransportType.values()) {
            this.addTest(createReconnectServerTest("Reconnect server after brief disconnection - " + transportType.name(), transportType));
        }
		
		for (TransportType transportType : TransportType.values()) {
            this.addTest(createConnectToUnavailableServerTest("Connecto to unavailable server - " + transportType.name(), transportType));
        }
		
		for (TransportType transportType : TransportType.values()) {
		    this.addTest(createPendingCallbacksAbortedTest("Pending callbacks aborted - " + transportType.name(), transportType));
        }
		
		for (TransportType transportType : TransportType.values()) {
            this.addTest(createCheckHeaderTest("Check headers - " + transportType.name(), transportType));
        }
	
	}   

}
