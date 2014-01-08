package com.merchantry.goncharov;

import java.util.concurrent.*;

/**
 * Simple Future that state is controlled by others
 * @param <T>
 */
public class ManageableFuture<T> implements Future<T> {

    private enum ManageableFutureState {
        WAITING,
        DONE,
        ERROR, CANCELLED
    }

    private volatile ManageableFutureState state = ManageableFutureState.WAITING;
    private volatile T content;
    private Exception exception;
    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (state == ManageableFutureState.WAITING) {
            state = ManageableFutureState.CANCELLED;
            latch.countDown();
        }
        return true;
    }

    @Override
    public boolean isCancelled() {
        return state == ManageableFutureState.CANCELLED;
    }

    @Override
    public boolean isDone() {
        return state == ManageableFutureState.DONE;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        latch.await();
        return getResult();
    }

    private T getResult() throws ExecutionException, InterruptedException {
        switch (state) {
            case ERROR:
                throw new ExecutionException("Error during request processing.\nServer returned exception:", exception);
            case CANCELLED:
                throw new InterruptedException("Execution cancelled");
            default:
                state = ManageableFutureState.DONE;
                return content;
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        latch.await(timeout, unit);
        return getResult();
    }

    public void setResult(T result) {
        content = result;
        state = ManageableFutureState.DONE;
        latch.countDown();
    }

    public void setException(Exception e) {
        if (state != ManageableFutureState.WAITING) return;
        state = ManageableFutureState.ERROR;
        exception = e;
        latch.countDown();
    }
}
