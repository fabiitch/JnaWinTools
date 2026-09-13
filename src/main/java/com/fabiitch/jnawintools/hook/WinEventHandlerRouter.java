package com.fabiitch.jnawintools.hook;

import com.fabiitch.jnawintools.hook.event.RawWinEvent;
import com.fabiitch.jnawintools.hook.handler.BaseWindowEventHandler;

import java.util.ArrayList;
import java.util.List;

public class WinEventHandlerRouter {

    private final List<BaseWindowEventHandler> handlers = new ArrayList<>();

    public void register(BaseWindowEventHandler handler) {
        handlers.add(handler);
    }

    public void route(RawWinEvent event) {
        for(int i =0 , n = handlers.size(); i < n; i++) {
            BaseWindowEventHandler handler = handlers.get(i);
            if (handler.supports(event)) {
                handler.handle(event);
            }
        }
    }
}
