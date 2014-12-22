/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.tests.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class Sync {

    private static Map<String, Semaphore> mSemaphores = new HashMap<String, Semaphore>();
    public static Object mSync = new Object();

    private static Semaphore getSemaphore(String name) {
        synchronized (mSync) {
            if (!mSemaphores.containsKey(name)) {
                mSemaphores.put(name, new Semaphore(0));
            }

            return mSemaphores.get(name);
        }
    }

    public static void waitComplete(String name, int count) throws InterruptedException {
        getSemaphore(name).acquireUninterruptibly(count);
    }

    public static void waitComplete(String name) throws InterruptedException {
        getSemaphore(name).acquireUninterruptibly();
    }

    public static void complete(String name) {
        getSemaphore(name).release();
    }

    public static void completeAll(String name) {
        getSemaphore(name).release(Integer.MAX_VALUE);
    }

    public static void reset() {
        synchronized (mSync) {
            mSemaphores = new HashMap<String, Semaphore>();
        }
    }

}
