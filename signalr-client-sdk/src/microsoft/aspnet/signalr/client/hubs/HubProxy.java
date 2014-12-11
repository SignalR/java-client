/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.hubs;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.SignalRFuture;

import com.google.gson.JsonElement;

/**
 * Proxy for hub operations
 */
public class HubProxy {

    private String mHubName;

    private HubConnection mConnection;

    private Map<String, Subscription> mSubscriptions = Collections.synchronizedMap(new HashMap<String, Subscription>());

    private Map<String, JsonElement> mState = Collections.synchronizedMap(new HashMap<String, JsonElement>());

    private Logger mLogger;

    private static final List<String> EXCLUDED_METHODS = Arrays.asList(new String[] { "equals", "getClass", "hashCode", "notify", "notifyAll", "toString",
            "wait" });

    private static final String SUBSCRIPTION_HANDLER_METHOD = "run";

    /**
     * Initializes the HubProxy
     * 
     * @param connection
     *            HubConnection to use
     * @param hubName
     *            Hub name
     */
    protected HubProxy(HubConnection connection, String hubName, Logger logger) {
        mConnection = connection;
        mHubName = hubName;
        mLogger = logger;
    }

    /**
     * Sets the state for a key
     * 
     * @param key
     *            Key to set
     * @param state
     *            State to set
     */
    public void setState(String key, JsonElement state) {
        mState.put(key, state);
    }

    /**
     * Gets the state for a key
     * 
     * @param key
     *            Key to get
     */
    public JsonElement getState(String key) {
        return mState.get(key);
    }

    /**
     * Gets the value for a key
     * 
     * @param key
     *            Key to get
     * @param clazz
     *            Class used to to deserialize the value
     * @return
     */
    public <E> E getValue(String key, Class<E> clazz) {
        return mConnection.getGson().fromJson(getState(key), clazz);
    }

    /**
     * Creates a subscription to an event
     * 
     * @param eventName
     *            The name of the event
     * @return The subscription object
     */
    public Subscription subscribe(String eventName) {
        log("Subscribe to event " + eventName, LogLevel.Information);
        if (eventName == null) {
            throw new IllegalArgumentException("eventName cannot be null");
        }

        eventName = eventName.toLowerCase(Locale.getDefault());

        Subscription subscription;
        if (mSubscriptions.containsKey(eventName)) {
            log("Adding event to existing subscription: " + eventName, LogLevel.Information);
            subscription = mSubscriptions.get(eventName);
        } else {
            log("Creating new subscription for: " + eventName, LogLevel.Information);
            subscription = new Subscription();
            mSubscriptions.put(eventName, subscription);
        }

        return subscription;
    }

    /**
     * Create subscriptions for all the object methods
     * 
     * @param handler
     *            Handler for the hub messages
     */
    public void subscribe(final Object handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }

        Method[] methods = handler.getClass().getMethods();

        for (int j = 0; j < methods.length; j++) {
            final Method method = methods[j];

            if (!EXCLUDED_METHODS.contains(method.getName())) {
                Subscription subscription = subscribe(method.getName());
                subscription.addReceivedHandler(new Action<JsonElement[]>() {

                    @Override
                    public void run(JsonElement[] eventParameters) throws Exception {
                        log("Handling dynamic subscription: " + method.getName(), LogLevel.Verbose);
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length != eventParameters.length) {
                            throw new RuntimeException("The handler has " + parameterTypes.length + " parameters, but there are " + eventParameters.length
                                    + " values.");
                        }

                        Object[] parameters = new Object[parameterTypes.length];

                        for (int i = 0; i < eventParameters.length; i++) {
                            parameters[i] = mConnection.getGson().fromJson(eventParameters[i], parameterTypes[i]);
                        }
                        method.setAccessible(true);
                        log("Invoking method for dynamic subscription: " + method.getName(), LogLevel.Verbose);
                        method.invoke(handler, parameters);
                    }
                });
            }
        }
    }

    /**
     * Removes all the subscriptions attached to an event
     * 
     * @param eventName
     *            the event
     */
    public void removeSubscription(String eventName) {
        if (eventName != null) {
        	mSubscriptions.remove(eventName.toLowerCase(Locale.getDefault()));
        }
    }

    /**
     * Invokes a hub method
     * 
     * @param method
     *            Method name
     * @param args
     *            Method arguments
     * @return A Future for the operation
     */
    public SignalRFuture<Void> invoke(String method, Object... args) {
        return invoke(null, method, args);
    }

    /**
     * Invokes a hub method that returns a value
     * 
     * @param method
     *            Method name
     * @param args
     *            Method arguments
     * @return A Future for the operation, that will return the method result
     */
    public <E> SignalRFuture<E> invoke(final Class<E> resultClass, final String method, Object... args) {
        if (method == null) {
            throw new IllegalArgumentException("method cannot be null");
        }

        if (args == null) {
            throw new IllegalArgumentException("args cannot be null");
        }

        log("Invoking method on hub: " + method, LogLevel.Information);

        JsonElement[] jsonArguments = new JsonElement[args.length];

        for (int i = 0; i < args.length; i++) {
            jsonArguments[i] = mConnection.getGson().toJsonTree(args[i]);
        }

        final SignalRFuture<E> resultFuture = new SignalRFuture<E>();

        final String callbackId = mConnection.registerCallback(new Action<HubResult>() {

            @Override
            public void run(HubResult result) {
                log("Executing invocation callback for: " + method, LogLevel.Information);
                if (result != null) {
                    if (result.getError() != null) {
                        if (result.isHubException()) {
                            resultFuture.triggerError(new HubException(result.getError(), result.getErrorData()));
                        } else {
                            resultFuture.triggerError(new Exception(result.getError()));
                        }
                    } else {
                        boolean errorHappened = false;
                        E resultObject = null;
                        try {
                            if (result.getState() != null) {
                                for (String key : result.getState().keySet()) {
                                    setState(key, result.getState().get(key));
                                }
                            }

                            if (result.getResult() != null && resultClass != null) {
                                log("Found result invoking method on hub: " + result.getResult(), LogLevel.Information);
                                resultObject = mConnection.getGson().fromJson(result.getResult(), resultClass);
                            }
                        } catch (Exception e) {
                            errorHappened = true;
                            resultFuture.triggerError(e);
                        }

                        if (!errorHappened) {
                            try {
                                resultFuture.setResult(resultObject);
                            } catch (Exception e) {
                                resultFuture.triggerError(e);
                            }
                        }
                    }
                }
            }
        });

        HubInvocation hubData = new HubInvocation();
        hubData.setHub(mHubName);
        hubData.setMethod(method);
        hubData.setArgs(jsonArguments);
        hubData.setCallbackId(callbackId);

        if (mState.size() != 0) {
            hubData.setState(mState);
        }

        final SignalRFuture<Void> sendFuture = mConnection.send(hubData);

        resultFuture.onCancelled(new Runnable() {

            @Override
            public void run() {
                mConnection.removeCallback(callbackId);
            }
        });

        resultFuture.onError(new ErrorCallback() {

            @Override
            public void onError(Throwable error) {
                sendFuture.triggerError(error);
            }
        });

        return resultFuture;
    }
    
    /**
     * Overload of 'invoke' hub method that takes a type instead of class for GSON deserialisation
     * 
     * @param method
     *            Method name
     * @param args
     *            Method arguments
     * @return A Future for the operation, that will return the method result
     */
    public <E> SignalRFuture<E> invoke(final Class<E> resultClass, final Type resultType, final String method, Object... args) {
        if (method == null) {
            throw new IllegalArgumentException("method cannot be null");
        }

        if (args == null) {
            throw new IllegalArgumentException("args cannot be null");
        }

        log("Invoking method on hub: " + method, LogLevel.Information);

        JsonElement[] jsonArguments = new JsonElement[args.length];

        for (int i = 0; i < args.length; i++) {
            jsonArguments[i] = mConnection.getGson().toJsonTree(args[i]);
        }

        final SignalRFuture<E> resultFuture = new SignalRFuture<E>();

        final String callbackId = mConnection.registerCallback(new Action<HubResult>() {

            @Override
            public void run(HubResult result) {
                log("Executing invocation callback for: " + method, LogLevel.Information);
                if (result != null) {
                    if (result.getError() != null) {
                        if (result.isHubException()) {
                            resultFuture.triggerError(new HubException(result.getError(), result.getErrorData()));
                        } else {
                            resultFuture.triggerError(new Exception(result.getError()));
                        }
                    } else {
                        boolean errorHappened = false;
                        E resultObject = null;
                        try {
                            if (result.getState() != null) {
                                for (String key : result.getState().keySet()) {
                                    setState(key, result.getState().get(key));
                                }
                            }

                            if (result.getResult() != null && resultType != null) {
                                log("Found result invoking method on hub: " + result.getResult(), LogLevel.Information);
                                resultObject = mConnection.getGson().fromJson(result.getResult(), resultType);
                            }
                        } catch (Exception e) {
                            errorHappened = true;
                            resultFuture.triggerError(e);
                        }

                        if (!errorHappened) {
                            try {
                                resultFuture.setResult(resultObject);
                            } catch (Exception e) {
                                resultFuture.triggerError(e);
                            }
                        }
                    }
                }
            }
        });

        HubInvocation hubData = new HubInvocation();
        hubData.setHub(mHubName);
        hubData.setMethod(method);
        hubData.setArgs(jsonArguments);
        hubData.setCallbackId(callbackId);

        if (mState.size() != 0) {
            hubData.setState(mState);
        }

        final SignalRFuture<Void> sendFuture = mConnection.send(hubData);

        resultFuture.onCancelled(new Runnable() {

            @Override
            public void run() {
                mConnection.removeCallback(callbackId);
            }
        });

        resultFuture.onError(new ErrorCallback() {

            @Override
            public void onError(Throwable error) {
                sendFuture.triggerError(error);
            }
        });

        return resultFuture;
    }

    /**
     * Invokes a hub event with argument
     * 
     * @param eventName
     *            The name of the event
     * @param args
     *            The event args
     * @throws Exception
     */
    void invokeEvent(String eventName, JsonElement[] args) throws Exception {
        if (eventName == null) {
            throw new IllegalArgumentException("eventName cannot be null");
        }

        eventName = eventName.toLowerCase(Locale.getDefault());

        if (mSubscriptions.containsKey(eventName)) {
            Subscription subscription = mSubscriptions.get(eventName);
            subscription.onReceived(args);
        }
    }

    private <E1, E2, E3, E4, E5> void on(String eventName, final SubscriptionHandler5<E1, E2, E3, E4, E5> handler, final Class<?>... parameterTypes) {
        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }

        Subscription subscription = subscribe(eventName);
        subscription.addReceivedHandler(new Action<JsonElement[]>() {

            @Override
            public void run(JsonElement[] eventParameters) throws Exception {
                Method method = null;

                for (Method m : handler.getClass().getMethods()) {
                    if (m.getName().equals(SUBSCRIPTION_HANDLER_METHOD)) {
                        method = m;
                        break;
                    }
                }

                if (parameterTypes.length != eventParameters.length) {
                    throw new RuntimeException("The handler has " + parameterTypes.length + " parameters, but there are " + eventParameters.length + " values.");
                }

                Object[] parameters = new Object[5];

                for (int i = 0; i < eventParameters.length; i++) {
                    parameters[i] = mConnection.getGson().fromJson(eventParameters[i], parameterTypes[i]);
                }
                method.setAccessible(true);
                method.invoke(handler, parameters);
            }
        });
    }

    public <E1, E2, E3, E4, E5> void on(String eventName, final SubscriptionHandler5<E1, E2, E3, E4, E5> handler, Class<E1> parameter1, Class<E2> parameter2,
            Class<E3> parameter3, Class<E4> parameter4, Class<E5> parameter5) {
        on(eventName, new SubscriptionHandler5<E1, E2, E3, E4, E5>() {

            @Override
            public void run(E1 p1, E2 p2, E3 p3, E4 p4, E5 p5) {
                handler.run(p1, p2, p3, p4, p5);
            }
        }, parameter1, parameter2, parameter3, parameter4, parameter5);
    }

    public <E1, E2, E3, E4> void on(String eventName, final SubscriptionHandler4<E1, E2, E3, E4> handler, Class<E1> parameter1, Class<E2> parameter2,
            Class<E3> parameter3, Class<E4> parameter4) {
        on(eventName, new SubscriptionHandler5<E1, E2, E3, E4, Void>() {

            @Override
            public void run(E1 p1, E2 p2, E3 p3, E4 p4, Void p5) {
                handler.run(p1, p2, p3, p4);
            }
        }, parameter1, parameter2, parameter3, parameter4);
    }

    public <E1, E2, E3> void on(String eventName, final SubscriptionHandler3<E1, E2, E3> handler, Class<E1> parameter1, Class<E2> parameter2,
            Class<E3> parameter3) {
        on(eventName, new SubscriptionHandler5<E1, E2, E3, Void, Void>() {

            @Override
            public void run(E1 p1, E2 p2, E3 p3, Void p4, Void p5) {
                handler.run(p1, p2, p3);
            }
        }, parameter1, parameter2, parameter3);
    }

    public <E1, E2> void on(String eventName, final SubscriptionHandler2<E1, E2> handler, Class<E1> parameter1, Class<E2> parameter2) {
        on(eventName, new SubscriptionHandler5<E1, E2, Void, Void, Void>() {

            @Override
            public void run(E1 p1, E2 p2, Void p3, Void p4, Void p5) {
                handler.run(p1, p2);
            }
        }, parameter1, parameter2);
    }

    public <E1> void on(String eventName, final SubscriptionHandler1<E1> handler, Class<E1> parameter1) {
        on(eventName, new SubscriptionHandler5<E1, Void, Void, Void, Void>() {

            @Override
            public void run(E1 p1, Void p2, Void p3, Void p4, Void p5) {
                handler.run(p1);
            }
        }, parameter1);
    }

    public <E1> void on(String eventName, final SubscriptionHandler handler) {
        on(eventName, new SubscriptionHandler5<Void, Void, Void, Void, Void>() {

            @Override
            public void run(Void p1, Void p2, Void p3, Void p4, Void p5) {
                handler.run();
            }
        });
    }

    protected void log(String message, LogLevel level) {
        if (message != null & mLogger != null) {
            mLogger.log("HubProxy " + mHubName + " - " + message, level);
        }
    }
}
