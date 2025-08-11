package com.nz.jnawintools.hook.events;

/** Impl synchrone : traite l’événement dans le thread appelant. */
public final class SyncEventDispatcher<T> extends AbstractEventDispatcher<T> {
    @Override
    public void dispatch(T event) {
        deliver(event);
    }
}
