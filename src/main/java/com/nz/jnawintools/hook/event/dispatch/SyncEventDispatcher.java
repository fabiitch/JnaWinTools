package com.nz.jnawintools.hook.event.dispatch;

/** Impl synchrone : traite l’événement dans le thread appelant. */
public final class SyncEventDispatcher<T> extends AbstractEventDispatcher<T> {
    @Override
    public void dispatch(T event) {
        deliver(event);
    }
}
