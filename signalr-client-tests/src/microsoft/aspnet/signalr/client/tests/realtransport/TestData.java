/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.tests.realtransport;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.tests.util.ConsoleLogger;

public class TestData {

    public static final String SERVER_ADDRESS = "10.0.0.133/signalrtestserver";
    public static final String SERVER_URL = "http://" + SERVER_ADDRESS + "/testendpoint";
    public static final String HUB_URL = "http://" + SERVER_ADDRESS + "/signalr";
    public static final String CONNECTION_QUERYSTRING = "myVal=1";
    public static final String HUB_NAME = "TestHub";
    
    public static Logger getLogger() {
        return new ConsoleLogger();
    }
    
    public static void triggerTestMessage() throws Exception {
        invokeServerAction("TriggerTestMessage");
    }
    
    public static String getLastSentData() throws Exception {
        return invokeServerAction("LastSentData");
    }
    
    public static String invokeServerAction(String action) throws Exception {
        URL url = new URL("http://" + TestData.SERVER_ADDRESS + "/home/" + action);
        
        URLConnection connection = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder sb = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine);
            sb.append("\n");
        }
            
        in.close();
        
        return sb.toString().trim();
    }

    public static String getLastHubData() throws Exception {
        return invokeServerAction("LastHubData");
    }

    public static void triggerHubTestMessage() throws Exception {
        invokeServerAction("TriggerHubTestMessage");
    }



}
