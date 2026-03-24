package com.nz.jnawintools.hook;

import com.nz.jnawintools.hook.event.RawWinEvent;
import com.nz.jnawintools.hook.handler.BaseWindowEventHandler;

import java.util.ArrayList;
import java.util.List;

public class WinEventRouter {

    private final List<BaseWindowEventHandler> handlers = new ArrayList<>();

    public void register(BaseWindowEventHandler handler) {
        handlers.add(handler);
    }

    public void route(RawWinEvent event) {
        for (BaseWindowEventHandler handler : handlers) {
            if (handler.supports(event)) {
                handler.handle(event);
            }
        }
    }
}