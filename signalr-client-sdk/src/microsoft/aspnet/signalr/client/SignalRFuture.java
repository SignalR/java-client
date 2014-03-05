/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents long running SignalR operations
 */
public class SignalRFuture<V> implements Future<V> {
    boolean mIsCancelled = false;
    boolean mIsDone = false;
    private V mResult = null;
    private List<Runnable> mOnCancelled = new ArrayList<Runnable>();
    private List<Action<V>> mOnDone = new ArrayList<Action<V>>();
    private Object mDoneLock = new Object();
    private List<ErrorCallback> mErrorCallback = new ArrayList<ErrorCallback>();
    private Queue<Throwable> mErrorQueue = new ConcurrentLinkedQueue<Throwable>();
    private Object mErrorLock = new Object();
    private Throwable mLastError = null;

    private Semaphore mResultSemaphore = new Semaphore(0);

    /**
     * Handles the cancellation event
     * 
     * @param onCancelled
     *            The handler
     */
    public void onCancelled(Runnable onCancelled) {
        mOnCancelled.add(onCancelled);
    }

    /**
     * Cancels the operation
     */
    public void cancel() {
        mIsCancelled = true;
        if (mOnCancelled != null) {
            for (Runnable onCancelled : mOnCancelled) {
                onCancelled.run();
            }
        }
        mResultSemaphore.release();
    }

    /**
     * Sets a result to the future and finishes its execution
     * 
     * @param result
     *            The future result
     */
    public void setResult(V result) {
        synchronized (mDoneLock) {
            mResult = result;
            mIsDone = true;

            if (mOnDone.size() > 0) {
                for (Action<V> handler : mOnDone) {
                    try {
                        handler.run(result);
                    } catch (Exception e) {
                        triggerError(e);
                    }
                }
            }
        }

        mResultSemaphore.release();
    }

    /**
     * Indicates if the operation is cancelled
     * 
     * @return True if the operation is cancelled
     */
    public boolean isCancelled() {
        return mIsCancelled;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        cancel();
        return true;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        try {
            return get(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (mResultSemaphore.tryAcquire(timeout, unit)) {
            if (errorWasTriggered()) {
                throw new ExecutionException(mLastError);
            } else if (isCancelled()) {
                throw new InterruptedException("Operation was cancelled");
            } else {
                return mResult;
            }
        } else {
            throw new TimeoutException();
        }
    }

    @Override
    public boolean isDone() {
        return mIsDone;
    }

    /**
     * Handles the completion of the Future. If the future was already
     * completed, it triggers the handler right away.
     * 
     * @param action
     *            The handler
     */
    public SignalRFuture<V> done(Action<V> action) {
        synchronized (mDoneLock) {
            mOnDone.add(action);

            if (isDone()) {
                try {
                    action.run(get());
                } catch (Exception e) {
                    triggerError(e);
                }
            }
        }

        return this;
    }

    /**
     * Handles error during the execution of the Future. If it's the first time
     * the method is invoked on the object and errors were already triggered,
     * the handler will be called once per error, right away.
     * 
     * @param errorCallback
     *            The handler
     */
    public SignalRFuture<V> onError(ErrorCallback errorCallback) {
        synchronized (mErrorLock) {
            mErrorCallback.add(errorCallback);
            while (!mErrorQueue.isEmpty()) {
                // Only the first error handler will get the queued errors
                if (errorCallback != null) {
                    errorCallback.onError(mErrorQueue.poll());
                }
            }
        }

        return this;
    }

    /**
     * Triggers an error for the Future
     * 
     * @param error
     *            The error
     */
    public void triggerError(Throwable error) {
        synchronized (mErrorLock) {
            mLastError = error;
            mResultSemaphore.release();
            if (mErrorCallback.size() > 0) {
                for (ErrorCallback handler : mErrorCallback) {
                    handler.onError(error);
                }
            } else {
                mErrorQueue.add(error);
            }
        }
    }
    
    /**
     * Indicates if an error was triggered
     * 
     * @return True if an error was triggered
     */
    public boolean errorWasTriggered() {
        return mLastError != null;
    }

}
