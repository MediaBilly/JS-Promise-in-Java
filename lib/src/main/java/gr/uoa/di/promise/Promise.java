package gr.uoa.di.promise;

import org.apache.commons.math3.util.Pair;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/*
 * > "What I cannot create, I do not understand"
 * > Richard Feynman
 * > https://en.wikipedia.org/wiki/Richard_Feynman
 * 
 * This is an incomplete implementation of the Javascript Promise machinery in Java.
 * You should expand and ultimately complete it according to the following:
 * 
 * (1) You should only use the low-level Java concurrency primitives (like 
 * java.lang.Thread/Runnable, wait/notify, synchronized, volatile, etc)
 * in your implementation. 
 * 
 * (2) The members of the java.util.concurrent package 
 * (such as Executor, Future, CompletableFuture, etc.) cannot be used.
 * 
 * (3) No other library should be used.
 * 
 * (4) Create as many threads as you think appropriate and don't worry about
 * recycling them or implementing a Thread Pool.
 * 
 * (5) I may have missed something from the spec, so please report any issues
 * in the course's e-class.
 * 
 * The Javascript Promise reference is here:
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise
 * 
 * A helpful guide to help you understand Promises is available here:
 * https://javascript.info/async
 */
public class Promise<V> {

    public static enum Status {
        PENDING,
        FULFILLED,
        REJECTED
    }

    private class PromiseState {
        private volatile Status status;
        private volatile ValueOrError<V> result;

        public PromiseState() {
            status = Status.PENDING;
            result = null;
        }

        public synchronized void resolve(V value) {
            if (status.equals(Status.PENDING)) {
                status = Status.FULFILLED;
                result = ValueOrError.Value.of(value);
            }
            notifyAll();
        }

        public synchronized void reject(Throwable error) {
            if (status.equals(Status.PENDING)) {
                status = Status.REJECTED;
                result = ValueOrError.Error.of(error);
            }
            notifyAll();
        }

        public synchronized Pair<Status, ValueOrError<V>> getFinalState() {
            while (status.equals(Status.PENDING)) {
                try {
                    wait();
                } catch (InterruptedException ignored) {

                }
            }
            notifyAll();
            return new Pair<>(this.status, this.result);
        }
    }
    private PromiseExecutor<V> executor;
    private PromiseState state;

    public Promise(PromiseExecutor<V> executor) {
        // Initialize Promise fields
        this.state = new PromiseState();
        this.executor = executor;
        // Call the executor's execute function and save the resolution or rejection status accordingly on the Promise object.
        executor.execute((value) -> state.resolve(value), (error) -> state.reject(error));
    }

    public <T> Promise<T> then(Function<V, T> onResolve, Consumer<Throwable> onReject) {
        return new Promise<>((resolve, reject) -> {
            new Thread(() -> {
                // Wait for result
                Pair<Status, ValueOrError<V>> finalState = state.getFinalState();
                Status status = finalState.getFirst();
                ValueOrError<V> result = finalState.getSecond();
                // Act based on the promise status returned by the executor
                if (status.equals(Status.FULFILLED)) {
                    resolve.accept(onResolve.apply(result.value()));
                } else if (status.equals(Status.REJECTED)) {
                    onReject.accept(result.error());
                    reject.accept(result.error());
                }
            }).start();
        });
    }

    public <T> Promise<T> then(Function<V, T> onResolve) {
        return new Promise<>((resolve, reject) -> {
            new Thread(() -> {
                // Wait for result
                Pair<Status, ValueOrError<V>> finalState = state.getFinalState();
                Status status = finalState.getFirst();
                ValueOrError<V> result = finalState.getSecond();
                // If the status is FULLFILLED, return a new promise with the value. Otherwise, return one with nothing.
                if (status.equals(Status.FULFILLED)) {
                    resolve.accept(onResolve.apply(result.value()));
                }
            }).start();
        });
    }

    // catch is a reserved word in Java.
    public Promise<?> catchError(Consumer<Throwable> onReject) {
        return new Promise<>((resolve, reject) -> {
            new Thread(() -> {
                // Wait for result
                Pair<Status, ValueOrError<V>> finalState = state.getFinalState();
                Status status = finalState.getFirst();
                ValueOrError<V> result = finalState.getSecond();
                // If the status is REJECTED, return a new promise with the error. Otherwise, return one with nothing.
                if (status.equals(Status.REJECTED)) {
                    onReject.accept(result.error());
                    reject.accept(result.error());
                } else {
                    reject.accept(null);
                }
            }).start();
        });
    }

    // finally is a reserved word in Java.
    public Promise<V> andFinally(Consumer<ValueOrError<V>> onSettle) {
        return new Promise<>((resolve, reject) -> {
            new Thread(() -> {
                // Wait for result
                Pair<Status, ValueOrError<V>> finalState = state.getFinalState();
                Status status = finalState.getFirst();
                ValueOrError<V> result = finalState.getSecond();
                // Call onSettle
                if (status.equals(Status.FULFILLED)) {
                    onSettle.accept(ValueOrError.Value.of(result.value()));
                } else if (status.equals(Status.REJECTED)) {
                    onSettle.accept(ValueOrError.Error.of(result.error()));
                }
            }).start();
        });
    }

    public static <T> Promise<T> resolve(T value) {
        return new Promise<>((resolve, reject) -> resolve.accept(value));
    }

    public static Promise<Void> reject(Throwable error) {
        return new Promise<>((resolve, reject) -> reject.accept(error));
    }

    public static Promise<ValueOrError<?>> race(List<Promise<?>> promises) {
        throw new UnsupportedOperationException("IMPLEMENT ME");
    }

    public static Promise<?> any(List<Promise<?>> promises) {
        throw new UnsupportedOperationException("IMPLEMENT ME");
    }

    public static Promise<List<?>> all(List<Promise<?>> promises) {
        throw new UnsupportedOperationException("IMPLEMENT ME");
    }

    public static Promise<List<ValueOrError<?>>> allSettled(List<Promise<?>> promises) {
        throw new UnsupportedOperationException("IMPLEMENT ME");
    }

}