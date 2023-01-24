# Implementation of the JavaScript Promise in Java

## About

This was an assignment for the Advanced Programming Techniques (M135) course of the CS Master's program of the National and Kapodistrian University of Athens.

In this assignment we were asked to implement a JavaScript Promise mechanism in java using only the low-level Java concurrency primitives (like java.lang.Thread/Runnable, wait/notify, synchronized, volatile, etc).

## Project Description

You can find a "skeleton" of the `Promise` class in 
the `lib/src/main/java/gr/uoa/di/promise/Promise.java` file.

What you need to do:

1. "Complete" the implementation of this class.
2. Ensure that your implementation passes all the tests contained in the `lib/src/test/groovy/gr/uoa/di/promise/PromiseSpec.groovy` file.

To verify that your implementation compiles and passes the tests, invoke the following command from the project root directory:

```
> ./gradlew build
```

or 

```
> ./gradlew test
```

After each execution, you can find the test reports in the `lib/build/reports/tests/test/index.html` file.