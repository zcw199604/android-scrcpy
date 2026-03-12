/*
 * 本项目大量借鉴学习了开源投屏软件：Scrcpy，在此对该项目表示感谢
 */
package top.saymzx.easycontrol.server.entity;

import android.content.IOnPrimaryClipChangedListener;
import android.hardware.display.VirtualDisplay;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Pair;
import android.view.Display;
import android.view.IRotationWatcher;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.saymzx.easycontrol.server.helper.ControlPacket;
import top.saymzx.easycontrol.server.helper.VideoEncode;
import top.saymzx.easycontrol.server.wrappers.ClipboardManager;
import top.saymzx.easycontrol.server.wrappers.DisplayManager;
import top.saymzx.easycontrol.server.wrappers.InputManager;
import top.saymzx.easycontrol.server.wrappers.SurfaceControl;
import top.saymzx.easycontrol.server.wrappers.WindowManager;

public final class Device {
  public static final int DISPLAY_ID_NONE = -1;
  private static final boolean USE_ANDROID_15_DISPLAY_POWER = false;

  private static int displayId = Display.DEFAULT_DISPLAY;
  private static VirtualDisplay virtualDisplay;
  public static Pair<Integer, Integer> realSize;
  public static DisplayInfo displayInfo;
  public static Pair<Integer, Integer> videoSize;
  private static boolean needReset = false;
  private static int oldScreenOffTimeout = 60000;

  private Device() {
  }

  public static void init() throws Exception {
    if (!Objects.equals(Options.startApp, "")) {
      virtualDisplay = DisplayManager.createVirtualDisplay();
      displayId = virtualDisplay.getDisplay().getDisplayId();
      startAndMoveAppToVirtualDisplay();
      needReset = true;
    }
    getRealSize();
    updateSize();
    setRotationListener();
    if (Options.listenerClip) {
      setClipBoardListener();
    }
    if (Options.keepAwake) {
      setKeepScreenLight();
    }
  }

  private static void startAndMoveAppToVirtualDisplay() throws IOException, InterruptedException {
    int appStackId = getAppStackId();
    if (appStackId == -1) {
      Device.execReadOutput("monkey -p " + Options.startApp + " -c android.intent.category.LAUNCHER 1");
      appStackId = getAppStackId();
    }
    if (appStackId == -1) {
      throw new IOException("error app");
    }
    Device.execReadOutput("am display move-stack " + appStackId + " " + displayId);
  }

  private static int getAppStackId() throws IOException, InterruptedException {
    String amStackList = Device.execReadOutput("am stack list");
    Matcher matcher = Pattern.compile("taskId=([0-9]+): " + Options.startApp).matcher(amStackList);
    if (!matcher.find()) {
      return -1;
    }
    return Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
  }

  private static void getRealSize() throws IOException, InterruptedException {
    String output = Device.execReadOutput("wm size");
    String pattern = (output.contains("Override") ? "Override" : "Physical") + " size: (\\d+)x(\\d+)";
    Matcher matcher = Pattern.compile(pattern).matcher(output);
    if (matcher.find()) {
      String width = matcher.group(1);
      String height = matcher.group(2);
      if (width == null || height == null) {
        return;
      }
      realSize = new Pair<>(Integer.parseInt(width), Integer.parseInt(height));
    }
  }

  private static void updateSize() {
    DisplayInfo newDisplayInfo = DisplayManager.getDisplayInfo(displayId);
    if (newDisplayInfo != null) {
      displayInfo = newDisplayInfo;
    }
    if (displayInfo == null) {
      return;
    }

    boolean isPortrait = displayInfo.width < displayInfo.height;
    int major = isPortrait ? displayInfo.height : displayInfo.width;
    int minor = isPortrait ? displayInfo.width : displayInfo.height;
    if (major > Options.maxSize) {
      minor = minor * Options.maxSize / major;
      major = Options.maxSize;
    }
    minor = minor + 8 & ~15;
    major = major + 8 & ~15;
    videoSize = isPortrait ? new Pair<>(minor, major) : new Pair<>(major, minor);
  }

  public static void changeResolution(float targetRatio) {
    try {
      if (targetRatio > 3 || targetRatio < 0.34 || realSize == null) {
        return;
      }

      float originalRatio = (float) realSize.first / realSize.second;
      float ratioChange = targetRatio / originalRatio;
      int newWidth;
      int newHeight;
      if (ratioChange > 1) {
        newWidth = realSize.first;
        newHeight = (int) (realSize.second / ratioChange);
      } else {
        newWidth = (int) (realSize.first * ratioChange);
        newHeight = realSize.second;
      }
      changeResolution(newWidth, newHeight);
    } catch (Exception ignored) {
    }
  }

  public static void changeResolution(int width, int height) {
    try {
      if (realSize == null || displayInfo == null) {
        return;
      }
      float originalRatio = (float) realSize.first / realSize.second;
      if (originalRatio > 3 || originalRatio < 0.34) {
        return;
      }

      needReset = true;
      width = width + 8 & ~15;
      height = height + 8 & ~15;
      if (width == height) {
        width -= 16;
      }

      if (virtualDisplay != null) {
        virtualDisplay.resize(width, height, displayInfo.density);
      } else {
        Device.execReadOutput("wm size " + width + "x" + height);
      }

      Thread.sleep(200);
      updateSize();
      VideoEncode.isHasChangeConfig = true;
    } catch (Exception ignored) {
    }
  }

  public static void fallbackResolution() throws IOException, InterruptedException {
    if (!needReset) {
      return;
    }
    if (virtualDisplay != null) {
      int appStackId = getAppStackId();
      if (appStackId != -1) {
        Device.execReadOutput("am display move-stack " + appStackId + " " + Display.DEFAULT_DISPLAY);
      }
      virtualDisplay.release();
      virtualDisplay = null;
      return;
    }
    if (Device.realSize != null) {
      Device.execReadOutput("wm size " + Device.realSize.first + "x" + Device.realSize.second);
    } else {
      Device.execReadOutput("wm size reset");
    }
  }

  private static String nowClipboardText = "";

  public static void setClipBoardListener() {
    ClipboardManager.addPrimaryClipChangedListener(new IOnPrimaryClipChangedListener.Stub() {
      public void dispatchPrimaryClipChanged() {
        String newClipboardText = ClipboardManager.getText();
        if (newClipboardText == null || newClipboardText.equals(nowClipboardText)) {
          return;
        }
        nowClipboardText = newClipboardText;
        ControlPacket.sendClipboardEvent(nowClipboardText);
      }
    });
  }

  public static void setClipboardText(String text) {
    if (text == null || text.equals(nowClipboardText)) {
      return;
    }
    nowClipboardText = text;
    ClipboardManager.setText(nowClipboardText);
  }

  private static void setRotationListener() {
    WindowManager.registerRotationWatcher(new IRotationWatcher.Stub() {
      public void onRotationChanged(int rotation) {
        updateSize();
        VideoEncode.isHasChangeConfig = true;
      }
    }, displayId);
  }

  private static final PointersState pointersState = new PointersState();

  public static void touchEvent(int action, Float x, Float y, int pointerId, int offsetTime) {
    if (displayInfo == null || x == null || y == null) {
      return;
    }

    Pointer pointer = pointersState.get(pointerId);
    if (pointer == null) {
      if (action != MotionEvent.ACTION_DOWN) {
        return;
      }
      pointer = pointersState.obtain(pointerId, SystemClock.uptimeMillis() - 50);
      if (pointer == null) {
        return;
      }
    }

    int pointerIndex = pointersState.getIndex(pointerId);
    if (pointerIndex == -1) {
      return;
    }

    float normalizedX = Math.max(0f, Math.min(1f, x));
    float normalizedY = Math.max(0f, Math.min(1f, y));
    pointer.x = normalizedX * displayInfo.width;
    pointer.y = normalizedY * displayInfo.height;
    pointer.pressure = action == MotionEvent.ACTION_UP ? 0f : 1f;
    pointer.up = action == MotionEvent.ACTION_UP;

    int pointerCount = pointersState.update();
    if (pointerCount <= 0) {
      return;
    }

    if (action == MotionEvent.ACTION_UP) {
      if (pointerCount > 1) {
        action = MotionEvent.ACTION_POINTER_UP | (pointerIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
      }
    } else if (action == MotionEvent.ACTION_DOWN && pointerCount > 1) {
      action = MotionEvent.ACTION_POINTER_DOWN | (pointerIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
    }

    MotionEvent event = MotionEvent.obtain(pointer.downTime, pointer.downTime + offsetTime, action, pointerCount,
      pointersState.pointerProperties, pointersState.pointerCoords, 0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
    try {
      injectEvent(event);
    } finally {
      event.recycle();
    }
  }

  public static void keyEvent(int keyCode, int meta) {
    long now = SystemClock.uptimeMillis();
    KeyEvent downEvent = new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, meta, -1, 0, 0, InputDevice.SOURCE_KEYBOARD);
    KeyEvent upEvent = new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, meta, -1, 0, 0, InputDevice.SOURCE_KEYBOARD);
    injectEvent(downEvent);
    injectEvent(upEvent);
  }

  public static boolean supportsInputEvents(int targetDisplayId) {
    return targetDisplayId == Display.DEFAULT_DISPLAY || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
  }

  private static boolean injectEvent(InputEvent inputEvent) {
    try {
      if (!supportsInputEvents(displayId)) {
        return false;
      }
      if (displayId != Display.DEFAULT_DISPLAY) {
        InputManager.setDisplayId(inputEvent, displayId);
      }
      InputManager.injectInputEvent(inputEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
      return true;
    } catch (Exception ignored) {
      return false;
    }
  }

  public static void changeScreenPowerMode(int mode) {
    try {
      boolean on = mode == SurfaceControl.POWER_MODE_NORMAL;
      if (USE_ANDROID_15_DISPLAY_POWER && Build.VERSION.SDK_INT >= 35 && DisplayManager.requestDisplayPower(displayId, on)) {
        return;
      }

      boolean applyToMultiPhysicalDisplays = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
      if (applyToMultiPhysicalDisplays
        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        && Build.BRAND != null
        && Build.BRAND.equalsIgnoreCase("honor")
        && SurfaceControl.hasGetBuiltInDisplayMethod()) {
        applyToMultiPhysicalDisplays = false;
      }

      int powerMode = on ? SurfaceControl.POWER_MODE_NORMAL : SurfaceControl.POWER_MODE_OFF;
      if (applyToMultiPhysicalDisplays) {
        long[] physicalDisplayIds = SurfaceControl.getPhysicalDisplayIds();
        if (physicalDisplayIds != null) {
          for (long physicalDisplayId : physicalDisplayIds) {
            IBinder token = SurfaceControl.getPhysicalDisplayToken(physicalDisplayId);
            if (token != null) {
              SurfaceControl.setDisplayPowerMode(token, powerMode);
            }
          }
          return;
        }
      }

      IBinder displayToken = SurfaceControl.getBuiltInDisplay();
      if (displayToken != null) {
        SurfaceControl.setDisplayPowerMode(displayToken, powerMode);
      }
    } catch (Exception ignored) {
    }
  }

  public static void changePower(int mode) {
    if (mode == -1) {
      keyEvent(26, 0);
      return;
    }
    try {
      String output = execReadOutput("dumpsys deviceidle | grep mScreenOn");
      Boolean isScreenOn = null;
      if (output.contains("mScreenOn=true")) {
        isScreenOn = true;
      } else if (output.contains("mScreenOn=false")) {
        isScreenOn = false;
      }
      if (isScreenOn != null && isScreenOn ^ (mode == 1)) {
        Device.keyEvent(26, 0);
      }
    } catch (Exception ignored) {
    }
  }

  public static void rotateDevice() {
    if (displayInfo == null) {
      return;
    }
    boolean accelerometerRotation = !WindowManager.isRotationFrozen(displayId);
    int newRotation = (displayInfo.rotation & 1) ^ 1;
    WindowManager.freezeRotation(displayId, newRotation);
    if (accelerometerRotation) {
      WindowManager.thawRotation(displayId);
    }
  }

  public static String execReadOutput(String cmd) throws IOException, InterruptedException {
    Process process = new ProcessBuilder().command("sh", "-c", cmd).start();
    StringBuilder builder = new StringBuilder();
    String line;
    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      while ((line = bufferedReader.readLine()) != null) {
        builder.append(line).append("\n");
      }
    }
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new IOException("命令执行错误" + cmd);
    }
    return builder.toString();
  }

  private static void setKeepScreenLight() {
    try {
      String output = execReadOutput("settings get system screen_off_timeout");
      Matcher matcher = Pattern.compile("\\d+").matcher(output);
      if (matcher.find()) {
        int timeout = Integer.parseInt(matcher.group());
        if (timeout >= 20 && timeout <= 60 * 30) {
          oldScreenOffTimeout = timeout;
        }
      }
      execReadOutput("settings put system screen_off_timeout 600000000");
    } catch (Exception ignored) {
    }
  }

  public static void fallbackScreenLightTimeout() throws IOException, InterruptedException {
    if (Options.keepAwake) {
      Device.execReadOutput("settings put system screen_off_timeout " + Device.oldScreenOffTimeout);
    }
  }
}
