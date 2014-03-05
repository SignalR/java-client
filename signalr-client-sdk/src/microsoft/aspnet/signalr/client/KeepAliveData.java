/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

import java.util.Calendar;

/**
 * Keep Alive data for the Heartbeat monitor
 */
class KeepAliveData {

    /**
     * Determines when we warn the developer that the connection may be lost
     */
    private double mKeepAliveWarnAt = 2.0 / 3.0;

    private long mLastKeepAlive;

    /**
     * Timeout to designate when to force the connection into reconnecting
     */
    private long mTimeout;

    /**
     * Timeout to designate when to warn the developer that the connection may
     * be dead or is hanging.
     */

    private long mTimeoutWarning;

    /**
     * Frequency with which we check the keep alive. It must be short in order
     * to not miss/pick up any changes
     */
    private long mCheckInterval;

    /**
     * Initializes the Keep Alive data
     * 
     * @param timeout
     *            Timeout in milliseconds
     */
    public KeepAliveData(long timeout) {
        setTimeout(timeout);
        setTimeoutWarning((long) (timeout * mKeepAliveWarnAt));
        setCheckInterval((timeout - getTimeoutWarning()) / 3);
        setLastKeepAlive(Calendar.getInstance().getTimeInMillis());
    }

    /**
     * Returns the last time the keep alive data was detected
     */
    public long getLastKeepAlive() {
        return mLastKeepAlive;
    }

    /**
     * Sets the last time the keep alive data was detected
     */
    public void setLastKeepAlive(long timeInmilliseconds) {
        mLastKeepAlive = timeInmilliseconds;
    }

    /**
     * Returns the timeout interval
     */
    public long getTimeout() {
        return mTimeout;
    }

    /**
     * Sets the timeout interval
     */
    public void setTimeout(long timeout) {
        mTimeout = timeout;
    }

    /**
     * Returns the timeout warning
     */
    public long getTimeoutWarning() {
        return mTimeoutWarning;
    }

    /**
     * Sets the timeout warning
     */
    public void setTimeoutWarning(long timeoutWarning) {
        mTimeoutWarning = timeoutWarning;
    }

    /**
     * Returns the Check interval
     */
    public long getCheckInterval() {
        return mCheckInterval;
    }

    /**
     * Sets the Check interval
     */
    public void setCheckInterval(long checkInterval) {
        mCheckInterval = checkInterval;
    }
}
