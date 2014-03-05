/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

public class MessageResult {
    private boolean mDisconnect = false;
    private boolean mReconnect = false;
    private boolean mInitialize = false;

    public boolean disconnect() {
        return mDisconnect;
    }

    public void setDisconnect(boolean disconnect) {
        mDisconnect = disconnect;
    }

    public boolean reconnect() {
        return mReconnect;
    }

    public void setReconnect(boolean reconnect) {
        mReconnect = reconnect;
    }

    public boolean initialize() {
        return mInitialize;
    }

    public void setInitialize(boolean initialize) {
        mInitialize = initialize;
    }
}
