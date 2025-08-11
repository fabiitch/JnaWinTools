package com.nz.jnawintools.hook.events;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Parent générique : gère listeners, delivery, et error handler optionnel.
 */
public abstract class AbstractEventDispatcher<T> implements AutoCloseable {
    private final CopyOnWriteArrayList<Consumer<? super T>> listeners = new CopyOnWriteArrayList<>();
    private volatile BiConsumer<? super T, ? super Throwable> errorHandler; // optionnel

    /**
     * Dispatch à implémenter (synchro/async).
     */
    public abstract void dispatch(T event);

    /**
     * Ajoute un listener. Retourne un handle pour le retirer facilement.
     */
    public AutoCloseable addListener(Consumer<? super T> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public boolean removeListener(Consumer<? super T> listener) {
        return listeners.remove(listener);
    }

    public void clear() {
        listeners.clear();
    }

    public int size() {
        return listeners.size();
    }

    /**
     * Setter optionnel pour gérer les erreurs des listeners.
     */
    public void setErrorHandler(BiConsumer<? super T, ? super Throwable> handler) {
        this.errorHandler = handler;
    }

    /**
     * Appelle tous les listeners, en isolant les erreurs.
     */
    protected void deliver(T event) {
        for (Consumer<? super T> l : listeners) {
            try {
                l.accept(event);
            } catch (Throwable t) {
                BiConsumer<? super T, ? super Throwable> eh = this.errorHandler;
                if (eh != null) {
                    try {
                        eh.accept(event, t);
                    } catch (Throwable ignore) { /* no-op */ }
                }
                // sinon: swallow pour ne pas casser la chaîne
            }
        }
    }

    /**
     * Par défaut: rien à fermer. (Async override)
     */
    @Override
    public void close() { /* no-op */ }
}
