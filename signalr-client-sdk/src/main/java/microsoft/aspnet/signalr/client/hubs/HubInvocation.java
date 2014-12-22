/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.hubs;

import java.util.Map;

import com.google.gson.JsonElement;

public class HubInvocation {
    @com.google.gson.annotations.SerializedName("I")
    private String mCallbackId;

    @com.google.gson.annotations.SerializedName("H")
    private String mHub;

    @com.google.gson.annotations.SerializedName("M")
    private String mMethod;

    @com.google.gson.annotations.SerializedName("A")
    private JsonElement[] mArgs;

    @com.google.gson.annotations.SerializedName("S")
    private Map<String, JsonElement> mState;

    public String getCallbackId() {
        return mCallbackId;
    }

    public void setCallbackId(String callbackId) {
        mCallbackId = callbackId;
    }

    public String getHub() {
        return mHub;
    }

    public void setHub(String hub) {
        mHub = hub;
    }

    public String getMethod() {
        return mMethod;
    }

    public void setMethod(String method) {
        mMethod = method;
    }

    public JsonElement[] getArgs() {
        return mArgs;
    }

    public void setArgs(JsonElement[] args) {
        mArgs = args;
    }

    public Map<String, JsonElement> getState() {
        return mState;
    }

    public void setState(Map<String, JsonElement> state) {
        mState = state;
    }
}
