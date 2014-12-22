/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.hubs;

import java.util.Locale;
import java.util.Map;

import com.google.gson.JsonElement;

/**
 * Represents the result of a hub operation
 */
public class HubResult {
    @com.google.gson.annotations.SerializedName("I")
    private String mId;

    @com.google.gson.annotations.SerializedName("R")
    private JsonElement mResult;

    @com.google.gson.annotations.SerializedName("H")
    private boolean mIsHubException;

    @com.google.gson.annotations.SerializedName("E")
    private String mError;

    @com.google.gson.annotations.SerializedName("D")
    private Object mErrorData;

    @com.google.gson.annotations.SerializedName("S")
    private Map<String, JsonElement> mState;

    public String getId() {
        return mId == null ? null : mId.toLowerCase(Locale.getDefault());
    }

    public void setId(String id) {
        mId = id;
    }

    public JsonElement getResult() {
        return mResult;
    }

    public void setResult(JsonElement result) {
        mResult = result;
    }

    public boolean isHubException() {
        return mIsHubException;
    }

    public void setIsHubException(boolean isHubException) {
        mIsHubException = isHubException;
    }

    public String getError() {
        return mError;
    }

    public void setError(String error) {
        mError = error;
    }

    public Object getErrorData() {
        return mErrorData;
    }

    public void setErrorData(Object errorData) {
        mErrorData = errorData;
    }

    public Map<String, JsonElement> getState() {
        return mState;
    }

    public void setState(Map<String, JsonElement> state) {
        mState = state;
    }
}
