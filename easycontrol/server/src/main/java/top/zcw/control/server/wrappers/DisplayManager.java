/*
 * 本项目大量借鉴学习了开源投屏软件：Scrcpy，在此对该项目表示感谢
 */
package top.zcw.control.server.wrappers;

import android.content.Context;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.os.Build;
import android.view.Display;
import android.view.Surface;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.zcw.control.server.entity.Device;
import top.zcw.control.server.entity.DisplayInfo;
import top.zcw.control.server.helper.FakeContext;

public final class DisplayManager {
  private static final long EVENT_FLAG_DISPLAY_CHANGED = 1L << 2;

  private static Object manager;
  private static Method getDisplayInfoMethod;
  private static Method getDisplayIdsMethod;
  private static Method createVirtualDisplayMethod;
  private static Method requestDisplayPowerMethod;

  private static final int VIRTUAL_DISPLAY_FLAG_DESTROY_CONTENT_ON_REMOVAL = 1 << 8;
  private static final int VIRTUAL_DISPLAY_FLAG_SHOULD_SHOW_SYSTEM_DECORATIONS = 1 << 9;
  private static final int VIRTUAL_DISPLAY_FLAG_TRUSTED = 1 << 10;
  private static final int VIRTUAL_DISPLAY_FLAG_OWN_DISPLAY_GROUP = 1 << 11;
  private static final int VIRTUAL_DISPLAY_FLAG_ALWAYS_UNLOCKED = 1 << 12;

  private DisplayManager() {
  }

  public static void init(Object m) {
    manager = m;
    getDisplayInfoMethod = null;
    getDisplayIdsMethod = null;
    createVirtualDisplayMethod = null;
    requestDisplayPowerMethod = null;
  }

  private static DisplayInfo parseDisplayInfo(String dumpsysDisplayOutput, int displayId) {
    Pattern regex = Pattern.compile(
      "^\\s+mOverrideDisplayInfo=DisplayInfo\\{\".*?, displayId " + displayId + ".*?(, FLAG_.*)?, real ([0-9]+) x ([0-9]+).*?, rotation ([0-9]+).*?, density ([0-9]+).*?, layerStack ([0-9]+)",
      Pattern.MULTILINE);
    Matcher matcher = regex.matcher(dumpsysDisplayOutput);
    if (!matcher.find()) {
      regex = Pattern.compile(
        "^\\s+mBaseDisplayInfo=DisplayInfo\\{\".*?, displayId " + displayId + ".*?(, FLAG_.*)?, real ([0-9]+) x ([0-9]+).*?, rotation ([0-9]+).*?, density ([0-9]+).*?, layerStack ([0-9]+)",
        Pattern.MULTILINE);
      matcher = regex.matcher(dumpsysDisplayOutput);
      if (!matcher.find()) {
        return null;
      }
    }

    int flags = parseDisplayFlags(matcher.group(1));
    int width = Integer.parseInt(matcher.group(2));
    int height = Integer.parseInt(matcher.group(3));
    int rotation = Integer.parseInt(matcher.group(4));
    int density = Integer.parseInt(matcher.group(5));
    int layerStack = Integer.parseInt(matcher.group(6));
    return new DisplayInfo(displayId, width, height, rotation, density, layerStack, flags, null);
  }

  private static int parseDisplayFlags(String text) {
    if (text == null) {
      return 0;
    }

    int flags = 0;
    Pattern regex = Pattern.compile("FLAG_[A-Z_]+");
    Matcher matcher = regex.matcher(text);
    while (matcher.find()) {
      String flagString = matcher.group();
      try {
        Field field = Display.class.getDeclaredField(flagString);
        flags |= field.getInt(null);
      } catch (Exception ignored) {
      }
    }
    return flags;
  }

  private static DisplayInfo getDisplayInfoFromDumpsysDisplay(int displayId) {
    try {
      String dumpsysDisplayOutput = Device.execReadOutput("dumpsys display");
      return parseDisplayInfo(dumpsysDisplayOutput, displayId);
    } catch (Exception ignored) {
      return null;
    }
  }

  private static synchronized Method getGetDisplayInfoMethod() throws NoSuchMethodException {
    if (getDisplayInfoMethod == null) {
      getDisplayInfoMethod = manager.getClass().getMethod("getDisplayInfo", int.class);
    }
    return getDisplayInfoMethod;
  }

  public static DisplayInfo getDisplayInfo(int displayId) {
    try {
      Method method = getGetDisplayInfoMethod();
      Object displayInfo = method.invoke(manager, displayId);
      if (displayInfo == null) {
        return getDisplayInfoFromDumpsysDisplay(displayId);
      }
      Class<?> cls = displayInfo.getClass();
      int width = cls.getDeclaredField("logicalWidth").getInt(displayInfo);
      int height = cls.getDeclaredField("logicalHeight").getInt(displayInfo);
      int rotation = cls.getDeclaredField("rotation").getInt(displayInfo);
      int layerStack = cls.getDeclaredField("layerStack").getInt(displayInfo);
      int density = cls.getDeclaredField("logicalDensityDpi").getInt(displayInfo);
      int flags = 0;
      try {
        flags = cls.getDeclaredField("flags").getInt(displayInfo);
      } catch (NoSuchFieldException ignored) {
      }
      String uniqueId = null;
      try {
        uniqueId = (String) cls.getDeclaredField("uniqueId").get(displayInfo);
      } catch (NoSuchFieldException ignored) {
      }
      return new DisplayInfo(displayId, width, height, rotation, density, layerStack, flags, uniqueId);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private static synchronized Method getGetDisplayIdsMethod() throws NoSuchMethodException {
    if (getDisplayIdsMethod == null) {
      getDisplayIdsMethod = manager.getClass().getMethod("getDisplayIds");
    }
    return getDisplayIdsMethod;
  }

  public static int[] getDisplayIds() {
    try {
      Method method = getGetDisplayIdsMethod();
      return (int[]) method.invoke(manager);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private static synchronized Method getCreateVirtualDisplayMethod() throws NoSuchMethodException {
    if (createVirtualDisplayMethod == null) {
      createVirtualDisplayMethod = android.hardware.display.DisplayManager.class.getMethod(
        "createVirtualDisplay", String.class, int.class, int.class, int.class, Surface.class);
    }
    return createVirtualDisplayMethod;
  }

  public static VirtualDisplay createVirtualDisplay(String name, int width, int height, int displayIdToMirror, Surface surface) throws Exception {
    Method method = getCreateVirtualDisplayMethod();
    return (VirtualDisplay) method.invoke(null, name, width, height, displayIdToMirror, surface);
  }

  public static VirtualDisplay createNewVirtualDisplay(String name, int width, int height, int dpi, Surface surface, int flags) throws Exception {
    Constructor<android.hardware.display.DisplayManager> constructor = android.hardware.display.DisplayManager.class.getDeclaredConstructor(Context.class);
    constructor.setAccessible(true);
    android.hardware.display.DisplayManager displayManager = constructor.newInstance(FakeContext.get());
    return displayManager.createVirtualDisplay(name, width, height, dpi, surface, flags);
  }

  // 此处大量借鉴了 群友 @○_○ 所编写的易控车机版本相应功能
  public static VirtualDisplay createVirtualDisplay() throws Exception {
    DisplayInfo realDisplayInfo = getDisplayInfo(Display.DEFAULT_DISPLAY);
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
      throw new Exception("Virtual display is not supported before Android 11");
    }

    int flags = android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
      | android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
      | VIRTUAL_DISPLAY_FLAG_DESTROY_CONTENT_ON_REMOVAL
      | android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      flags |= VIRTUAL_DISPLAY_FLAG_TRUSTED | VIRTUAL_DISPLAY_FLAG_OWN_DISPLAY_GROUP | VIRTUAL_DISPLAY_FLAG_ALWAYS_UNLOCKED;
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      flags |= VIRTUAL_DISPLAY_FLAG_SHOULD_SHOW_SYSTEM_DECORATIONS;
    }

    Surface surface = MediaCodec.createPersistentInputSurface();
    return createNewVirtualDisplay("easycontrol", realDisplayInfo.width, realDisplayInfo.height, realDisplayInfo.density, surface, flags);
  }

  private static synchronized Method getRequestDisplayPowerMethod() throws NoSuchMethodException {
    if (requestDisplayPowerMethod == null) {
      requestDisplayPowerMethod = manager.getClass().getMethod("requestDisplayPower", int.class, boolean.class);
    }
    return requestDisplayPowerMethod;
  }

  public static boolean requestDisplayPower(int displayId, boolean on) {
    try {
      Method method = getRequestDisplayPowerMethod();
      return (boolean) method.invoke(manager, displayId, on);
    } catch (Exception ignored) {
      return false;
    }
  }
}
