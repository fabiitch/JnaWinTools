package com.nz.jnawintools.hook.v2;

import com.nz.jnawintools.hook.v2.event.RawWinEvent;
import com.nz.jnawintools.hook.v2.handler.BaseWindowEventHandler;

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