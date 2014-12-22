/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.tests.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import microsoft.aspnet.signalr.client.http.Response;

public class MockResponse implements Response {

    Semaphore mSemaphore = new Semaphore(0);

    Object mLinesLock = new Object();
    Queue<String> mLines = new ConcurrentLinkedQueue<String>();
    Map<String, List<String>> mHeaders = new HashMap<String, List<String>>();
    int mStatus;
    boolean mFinished = false;

    public MockResponse(int status) {
        mStatus = status;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public void writeLine(String line) {
        if (line != null) {
            synchronized (mLinesLock) {
                mLines.add(line);
            }
            mSemaphore.release();
        }
    }

    public void finishWriting() {
        mFinished = true;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        mHeaders = new HashMap<String, List<String>>();
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return new HashMap<String, List<String>>(mHeaders);
    }

    @Override
    public List<String> getHeader(String headerName) {
        return mHeaders.get(headerName);
    }

    @Override
    public String readToEnd() throws IOException {
        StringBuilder sb = new StringBuilder();

        while (!mFinished || !mLines.isEmpty()) {
            String line = readLine();
            sb.append(line);
            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public String readLine() throws IOException {
        if (mFinished) {
            if (mLines.isEmpty()) {
                return null;
            } else {
                synchronized (mLinesLock) {
                    return mLines.poll();
                }
            }
        } else {
            try {
                mSemaphore.acquire();
            } catch (InterruptedException e) {
            }

            synchronized (mLinesLock) {
                String line = mLines.poll();
                return line;
            }
        }
    }

    @Override
    public int getStatus() {
        return mStatus;
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return readToEnd().getBytes();
    }

}
