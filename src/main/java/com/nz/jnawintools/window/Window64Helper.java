package com.nz.jnawintools.window;

import com.nz.jnawintools.enums.WindowDisplayMode;
import com.nz.jnawintools.log.JWTLogger;
import com.nz.jnawintools.log.impl.JWTMuttedLogger;
import com.nz.jnawintools.window.result.*;
import com.nz.jnawintools.window.utils.WindowsErrorMap;
import com.sun.jna.platform.win32.WinDef;
import lombok.Getter;

import java.awt.*;
import java.util.function.Function;

public class Window64Helper {
    @Getter
    private final JWTLogger logger;

    public Window64Helper(JWTLogger logger) {
        this.logger = logger;
    }

    public Window64Helper() {
        this(new JWTMuttedLogger());
    }

    private <T extends WinApiResult> T withWindowName(
            String windowName,
            String actionName,
            Function<WinDef.HWND, T> action,
            Function<Integer, T> errorFactory) {

        HwndResult hwndRes = Window64Utils.getHwnd(windowName);
        if (hwndRes.isFailure()) {
            logger.error("Window not found: {} (errorCode={})", windowName, hwndRes.getErrorCode());
            return errorFactory.apply(hwndRes.getErrorCode()); // utilise la fabrique
        }
        logger.debug("Start action {} for window={}", actionName, windowName);
        return withHwnd(hwndRes.getHwnd(), actionName, action);
    }

    private <T extends WinApiResult> T withHwnd(
            WinDef.HWND hwnd,
            String actionName,
            Function<WinDef.HWND, T> action) {

        T result = action.apply(hwnd);

        WinApiResultExtended<String> nameResult = Window64Utils.getName(hwnd);
        String windowName = nameResult.isSuccess() ? nameResult.getResult() : "NO_NAME_FOUND";

        if (result.isFailure()) {
            logger.error("Action={} failed for window={} (errorCode={}, {})",
                    actionName, windowName, result.getErrorCode(), WindowsErrorMap.getErrorMessage(result.getErrorCode()));
        } else {
            if (result instanceof WinApiResultExtended<?>) {
                Object value = ((WinApiResultExtended<?>) result).getResult();
                logger.debug("Action={} succeeded for window={}, result={}", actionName, windowName, value);
            } else {
                logger.debug("Action={} succeeded for window={}", actionName, windowName);
            }
        }
        return result;
    }

    public boolean setBorderless(String windowName) {
        return withWindowName(windowName,
                "setBorderless",
                Window64Utils::setBorderless,
                WinApiResult::failure)
                .isSuccess();
    }

    public boolean setFullScreen(String windowName) {
        return withWindowName(windowName,
                "setFullScreen",
                Window64Utils::setFullScreen,
                WinApiResult::failure)
                .isSuccess();
    }

    public boolean setAlwaysOnTop(String windowName) {
        return withWindowName(windowName,
                "setAlwaysOnTop",
                Window64Utils::setAlwaysOnTop,
                WinApiResult::failure)
                .isSuccess();
    }

    public boolean setWindowPosition(String windowName, int x, int y) {
        return withWindowName(
                windowName,
                "getWindowBounds",
                hwnd -> Window64Utils.setWindowPosition(hwnd, x, y),
                WindowBoundsResult::failure
        ).isSuccess();
    }

    public boolean setWindowPosition(String windowName, Rectangle rectangle) {
        return withWindowName(
                windowName,
                "getWindowBounds",
                hwnd -> Window64Utils.setWindowPosition(hwnd, rectangle),
                WindowBoundsResult::failure
        ).isSuccess();
    }

    public boolean setWindowDecorated(String windowName) {
        return withWindowName(windowName,
                "setWindowDecorated",
                Window64Utils::setWindowDecorated,
                WinApiResult::failure)
                .isSuccess();
    }

    public boolean setWindowUnDecorated(String windowName) {
        return withWindowName(windowName,
                "setWindowUnDecorated",
                Window64Utils::setWindowUnDecorated,
                WinApiResult::failure)
                .isSuccess();
    }
    public boolean showWindow(String windowName) {
        return withWindowName(windowName,
                "showWindow",
                Window64Utils::showWindow,
                WinApiResult::failure)
                .isSuccess();
    }

    public boolean hideWindow(String windowName) {
        return withWindowName(windowName,
                "hideWindow",
                Window64Utils::hideWindow,
                WinApiResult::failure)
                .isSuccess();
    }

    public boolean setClickThrough(String windowName) {
        return withWindowName(windowName,
                "setClickThrough",
                Window64Utils::setClickThrough,
                WinApiResult::failure)
                .isSuccess();
    }

    public boolean isIconic(WinDef.HWND hwnd) {
        IconicResult isIconic = withHwnd(hwnd,
                "isIconic",
                Window64Utils::isIconic);
        return isIconic.isSuccess() && isIconic.isIconic();
    }

    public boolean isIconic(String windowName) {
        IconicResult isIconic = withWindowName(windowName,
                "isIconic",
                Window64Utils::isIconic,
                IconicResult::failure);
        return isIconic.isSuccess() && isIconic.isIconic();
    }

    public boolean isVisible(String windowName) {
        VisibleResult isVisible = withWindowName(windowName,
                "isVisible",
                Window64Utils::isVisible,
                VisibleResult::failure);
        return isVisible.isSuccess() && isVisible.getResult();
    }

    public boolean isActive(String windowName) {
        return Window64Utils.isActive(windowName);
    }

    public WinDef.HWND getForeGroundWindow() {
        return Window64Utils.getForegroundWindow().getHwnd();
    }

    public String getForegroundWindowTitle() {
        ForeGroundWindowNameResult foreGroundWindowNameResult = Window64Utils.getForegroundWindowName();
        return foreGroundWindowNameResult.getResult();
    }

    public Rectangle getWindowBounds(String windowName) {
        WindowBoundsResult res = (WindowBoundsResult) withWindowName(
                windowName,
                "getWindowBounds",
                Window64Utils::getWindowBounds,
                WindowBoundsResult::failure
        );
        return res.getResult();
    }

    public WindowDisplayMode getDisplayMode(String windowName) {
        return withWindowName(windowName,
                "getDisplayMode",
                Window64Utils::getDisplayMode,
                DisplayModeResult::failure)
                .getDisplayMode();
    }

    public WinDef.HWND getHwnd(String windowName) {
        return withWindowName(windowName,
                "getHwnd",
                hwnd -> Window64Utils.getHwnd(windowName),
                HwndResult::failure).getHwnd();
    }


    public String getName(WinDef.HWND hwnd) {
        return withHwnd(hwnd,
                "getName",
                res -> Window64Utils.getName(hwnd)).getResult();
    }



}
