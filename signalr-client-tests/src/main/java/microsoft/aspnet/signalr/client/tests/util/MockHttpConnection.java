/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.tests.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import microsoft.aspnet.signalr.client.http.HttpConnection;
import microsoft.aspnet.signalr.client.http.HttpConnectionFuture;
import microsoft.aspnet.signalr.client.http.HttpConnectionFuture.ResponseCallback;
import microsoft.aspnet.signalr.client.http.Request;

public class MockHttpConnection implements HttpConnection {

    Semaphore mSemaphore = new Semaphore(0);

    Queue<RequestEntry> mRequests = new ConcurrentLinkedQueue<RequestEntry>();
    List<Thread> mThreads = new ArrayList<Thread>();

    @Override
    public HttpConnectionFuture execute(Request request, ResponseCallback responseCallback) {
        RequestEntry entry = new RequestEntry();
        entry.request = request;
        entry.callback = responseCallback;
        entry.future = new HttpConnectionFuture();
        entry.response = new MockResponse(200);

        mRequests.add(entry);
        mSemaphore.release();

        return entry.future;
    }

    public class RequestEntry {
        public Request request;
        public ResponseCallback callback;
        public HttpConnectionFuture future;
        public MockResponse response;
        private boolean mResponseTriggered = false;
        private Object mSync = new Object();

        public void finishRequest() {
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    response.finishWriting();
                    future.setResult(null);
                }
            });

            mThreads.add(t);

            t.start();
        }

        public void triggerResponse() {
            synchronized (mSync) {
                if (!mResponseTriggered) {
                    mResponseTriggered = true;

                    Thread t = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                callback.onResponse(response);
                            } catch (Exception e) {
                            }
                        }
                    });

                    mThreads.add(t);

                    t.start();
                }
            }
        }
    }

    public RequestEntry getRequest() throws InterruptedException {
        mSemaphore.acquire();
        return mRequests.poll();
    }
}
