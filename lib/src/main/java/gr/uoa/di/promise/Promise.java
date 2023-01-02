package gr.uoa.di.promise;

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
        FULLFILLED,
        REJECTED
    }

    // No instance fields are defined, perhaps you should add some!
    private Status status;
    private ValueOrError<V> result;
    private PromiseExecutor<V> executor;
    private Thread thread;
    
    public Promise(PromiseExecutor<V> executor) {
        // Initialize Promise fields
        this.status = Status.PENDING;
        this.result = null;
        this.executor = executor;
        // Create a thread that calls the executor's execute function and saves the resolution or rejection status accordingely on the Promise object.
        this.thread = new Thread(() -> executor.execute((value) -> {
            if (status.equals(Status.PENDING)) {
                status = Status.FULLFILLED;
                result = ValueOrError.Value.of(value);
            }
        }, (error) -> {
            if (status.equals(Status.PENDING)) {
                status = Status.REJECTED;
                result = ValueOrError.Error.of(error);
            }
        }));
        // Start the thread
        this.thread.start();
    }

    public <T> Promise<ValueOrError<T>> then(Function<V, T> onResolve, Consumer<Throwable> onReject) {
        return new Promise<>((resolve, reject) -> {
            // Wait for executor Thread to finish
            try {
                thread.join();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex.getMessage());
            }
            // Act based on the promise status returned by the executor
            if (status.equals(Status.FULLFILLED)) {
                resolve.accept(ValueOrError.Value.of(onResolve.apply(result.value())));
            } else if (status.equals(Status.REJECTED)) {
                onReject.accept(result.error());
                reject.accept(result.error());
            } else {
                // Error: Neither resolve.accept() nor reject.accept() called by PromiseExecutor so return a new promise with the error below:
                Throwable noResolveOrRejectCalled = new RuntimeException("Promise Error: Neither resolve.accept() nor reject.accept() called by PromiseExecutor!");
                onReject.accept(noResolveOrRejectCalled);
                reject.accept(noResolveOrRejectCalled);
            }
        });
    }

    public <T> Promise<T> then(Function<V, T> onResolve) {
        return new Promise<>((resolve, reject) -> {
            // Wait for executor Thread to finish
            try {
                thread.join();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex.getMessage());
            }
            // If the status is FULLFILLED, return a new promise with the value. Otherwise, return one with nothing.
            if (status.equals(Status.FULLFILLED)) {
                resolve.accept(onResolve.apply(result.value()));
            } else {
                resolve.accept(null);
            }
        });
    }

    // catch is a reserved word in Java.
    public Promise<Throwable> catchError(Consumer<Throwable> onReject) {
        return new Promise<>((resolve, reject) -> {
            // Wait for executor Thread to finish
            try {
                thread.join();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex.getMessage());
            }
            // If the status is REJECTED, return a new promise with the error. Otherwise, return one with nothing.
            if (status.equals(Status.REJECTED)) {
                onReject.accept(result.error());
                reject.accept(result.error());
            } else {
                reject.accept(null);
            }
        });
    }

    // finally is a reserved word in Java.
    public <T> Promise<ValueOrError<T>> andFinally(Consumer<ValueOrError<T>> onSettle) {
        return new Promise<>((resolve, reject) -> {
            // Wait for executor Thread to finish
            try {
                thread.join();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex.getMessage());
            }
            // Call onSettle
            if (status.equals(Status.FULLFILLED)) {
                onSettle.accept(ValueOrError.Value.of((T)result.value()));
            } else if (status.equals(Status.REJECTED)) {
                onSettle.accept(ValueOrError.Error.of(result.error()));
            } else {
                // Error: Neither resolve.accept() nor reject.accept() called by PromiseExecutor so return a new promise with the error below:
                Throwable noResolveOrRejectCalled = new RuntimeException("Promise Error: Neither resolve.accept() nor reject.accept() called by PromiseExecutor!");
                onSettle.accept(ValueOrError.Error.of(noResolveOrRejectCalled));
            }
        });
    }

    public static <T> Promise<T> resolve(T value) {
        return new Promise<>((resolve, reject) -> resolve.accept(value));
    }

    public static Promise<Throwable> reject(Throwable error) {
        return new Promise<>((resolve, reject) -> reject.accept(error));
    }

    public static <T> Promise<T> race(Iterable<Promise<?>> promises) {
        throw new UnsupportedOperationException("IMPLEMENT ME");
    }

    public static <T> Promise<T> any(Iterable<Promise<?>> promises) {
        throw new UnsupportedOperationException("IMPLEMENT ME");
    }

    public static <T> Promise<T> all(Iterable<Promise<?>> promises) {
        throw new UnsupportedOperationException("IMPLEMENT ME");
    }

    public static <T> Promise<T> allSettled(Iterable<Promise<?>> promises) {
        throw new UnsupportedOperationException("IMPLEMENT ME");
    }

}