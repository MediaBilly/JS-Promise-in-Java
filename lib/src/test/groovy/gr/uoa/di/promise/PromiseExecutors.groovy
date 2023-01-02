package gr.uoa.di.promise

import java.util.function.Consumer

class PromiseExecutors {

    static PromiseExecutor<String> delayedResolve(String value, int delayMillis) {
        return new DelayedValue<String>(value, delayMillis)
    }

    static PromiseExecutor<Void> delayedReject(Throwable error, int delayMillis) {
        return new DelayedError(error, delayMillis)
    }


    private class DelayedValue<V> implements PromiseExecutor<V> {
        
        V value
        int delayMillis

        DelayedValue(V value, int delayMillis) {
            this.value = value
            this.delayMillis = delayMillis
        }

        void execute(Consumer<V> resolve, Consumer<Throwable> reject) {
            new Thread( () -> {
                try {
                    Thread.sleep(delayMillis)
                    resolve.accept(value)
                }
                catch(InterruptedException ie) {
                    // will never occur
                    // if it occurs, the promise will remain PENDING forever
                }
            })
        }
    }

    private class DelayedError<V> implements PromiseExecutor<V> {
        
        Throwable error
        int delayMillis

        DelayedError(Throwable error, int delayMillis) {
            this.error = error
            this.delayMillis = delayMillis
        }

        void execute(Consumer<V> resolve, Consumer<Throwable> reject) {
            new Thread( () -> {
                try {
                    Thread.sleep(delayMillis)
                    reject.accept(error)
                }
                catch(InterruptedException ie) {
                    // will never occur
                    // if it occurs, the promise will remain PENDING forever
                }
            })
        }
    }

}