package com.nz.jnawintools.hook.events;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Impl asynchrone : poste les événements sur un Executor. */
public final class AsyncEventDispatcher<T> extends AbstractEventDispatcher<T> implements AutoCloseable {
    private final Executor executor;
    private final ExecutorService ownedService; // non-null si créé en interne

    /** Utilise un Executor fourni (lifecycle non géré ici). */
    public AsyncEventDispatcher(Executor executor) {
        this.executor = executor;
        this.ownedService = null;
    }

    /** Crée un single-thread daemon interne, nommé. */
    public AsyncEventDispatcher(String threadName) {
        this.ownedService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, threadName);
            t.setDaemon(true);
            return t;
        });
        this.executor = ownedService;
    }

    @Override public void dispatch(final T event) {
        executor.execute(() -> deliver(event));
    }

    @Override public void close() {
        if (ownedService != null) ownedService.shutdownNow();
    }
}
