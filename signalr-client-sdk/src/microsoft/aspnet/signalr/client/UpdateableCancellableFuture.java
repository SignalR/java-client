package microsoft.aspnet.signalr.client;

/**
 * An updateable SignalRFuture that, when cancelled, triggers cancellation on an
 * internal instance
 */
public class UpdateableCancellableFuture<V> extends SignalRFuture<V> {
    SignalRFuture<?> mFuture = null;

    Object mSync = new Object();

    public UpdateableCancellableFuture(SignalRFuture<?> token) {
        mFuture = token;
    }

    public void setFuture(SignalRFuture<?> token) {
        synchronized (mSync) {
            mFuture = token;
        }

        if (isCancelled()) {
            if (mFuture != null) {
                mFuture.cancel();
            }
        }
    }

    @Override
    public void cancel() {
        synchronized (mSync) {
            super.cancel();
            if (mFuture != null) {
                mFuture.cancel();
                mFuture = null;
            }
        }
    }
}
