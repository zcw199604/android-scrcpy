/*
 * 本项目大量借鉴学习了开源投屏软件：Scrcpy，在此对该项目表示感谢
 */
package top.saymzx.easycontrol.server.wrappers;

import android.os.Build;
import android.os.IInterface;
import android.view.Display;
import android.view.IRotationWatcher;

import java.lang.reflect.Method;

public final class WindowManager {
  public static final int DISPLAY_IME_POLICY_LOCAL = 0;
  public static final int DISPLAY_IME_POLICY_FALLBACK_DISPLAY = 1;
  public static final int DISPLAY_IME_POLICY_HIDE = 2;

  private static IInterface manager;
  private static Class<?> managerClass;
  private static Method getRotationMethod;

  private static Method freezeRotationMethod;
  private static Method freezeDisplayRotationMethod;
  private static int freezeDisplayRotationMethodVersion = -1;

  private static Method isRotationFrozenMethod;
  private static Method isDisplayRotationFrozenMethod;
  private static int isDisplayRotationFrozenMethodVersion = -1;

  private static Method thawRotationMethod;
  private static Method thawDisplayRotationMethod;
  private static int thawDisplayRotationMethodVersion = -1;

  private static Method getDisplayImePolicyMethod;
  private static Method setDisplayImePolicyMethod;

  private WindowManager() {
  }

  public static void init(IInterface m) {
    manager = m;
    managerClass = manager.getClass();
    getRotationMethod = null;
    freezeRotationMethod = null;
    freezeDisplayRotationMethod = null;
    freezeDisplayRotationMethodVersion = -1;
    isRotationFrozenMethod = null;
    isDisplayRotationFrozenMethod = null;
    isDisplayRotationFrozenMethodVersion = -1;
    thawRotationMethod = null;
    thawDisplayRotationMethod = null;
    thawDisplayRotationMethodVersion = -1;
    getDisplayImePolicyMethod = null;
    setDisplayImePolicyMethod = null;
  }

  private static Method getGetRotationMethod() throws NoSuchMethodException {
    if (getRotationMethod == null) {
      try {
        getRotationMethod = managerClass.getMethod("getDefaultDisplayRotation");
      } catch (NoSuchMethodException e) {
        getRotationMethod = managerClass.getMethod("getRotation");
      }
    }
    return getRotationMethod;
  }

  private static Method getFreezeRotationMethod() throws NoSuchMethodException {
    if (freezeRotationMethod == null) {
      freezeRotationMethod = managerClass.getMethod("freezeRotation", int.class);
    }
    return freezeRotationMethod;
  }

  private static Method getFreezeDisplayRotationMethod() throws NoSuchMethodException {
    if (freezeDisplayRotationMethod == null) {
      try {
        freezeDisplayRotationMethod = managerClass.getMethod("freezeDisplayRotation", int.class, int.class, String.class);
        freezeDisplayRotationMethodVersion = 0;
      } catch (NoSuchMethodException e) {
        freezeDisplayRotationMethod = managerClass.getMethod("freezeDisplayRotation", int.class, int.class);
        freezeDisplayRotationMethodVersion = 1;
      }
    }
    return freezeDisplayRotationMethod;
  }

  private static Method getIsRotationFrozenMethod() throws NoSuchMethodException {
    if (isRotationFrozenMethod == null) {
      isRotationFrozenMethod = managerClass.getMethod("isRotationFrozen");
    }
    return isRotationFrozenMethod;
  }

  private static Method getIsDisplayRotationFrozenMethod() throws NoSuchMethodException {
    if (isDisplayRotationFrozenMethod == null) {
      isDisplayRotationFrozenMethod = managerClass.getMethod("isDisplayRotationFrozen", int.class);
      isDisplayRotationFrozenMethodVersion = 0;
    }
    return isDisplayRotationFrozenMethod;
  }

  private static Method getThawRotationMethod() throws NoSuchMethodException {
    if (thawRotationMethod == null) {
      thawRotationMethod = managerClass.getMethod("thawRotation");
    }
    return thawRotationMethod;
  }

  private static Method getThawDisplayRotationMethod() throws NoSuchMethodException {
    if (thawDisplayRotationMethod == null) {
      try {
        thawDisplayRotationMethod = managerClass.getMethod("thawDisplayRotation", int.class, String.class);
        thawDisplayRotationMethodVersion = 0;
      } catch (NoSuchMethodException e) {
        thawDisplayRotationMethod = managerClass.getMethod("thawDisplayRotation", int.class);
        thawDisplayRotationMethodVersion = 1;
      }
    }
    return thawDisplayRotationMethod;
  }

  public static int getRotation() {
    try {
      Method method = getGetRotationMethod();
      return (int) method.invoke(manager);
    } catch (Exception ignored) {
      return 0;
    }
  }

  public static void freezeRotation(int displayId, int rotation) {
    try {
      Method method = getFreezeDisplayRotationMethod();
      switch (freezeDisplayRotationMethodVersion) {
        case 0:
          method.invoke(manager, displayId, rotation, "easycontrol#freezeRotation");
          return;
        case 1:
          method.invoke(manager, displayId, rotation);
          return;
        default:
          break;
      }
    } catch (Exception ignored) {
    }

    try {
      if (displayId == Display.DEFAULT_DISPLAY) {
        Method method = getFreezeRotationMethod();
        method.invoke(manager, rotation);
      }
    } catch (Exception ignored) {
    }
  }

  public static boolean isRotationFrozen(int displayId) {
    try {
      Method method = getIsDisplayRotationFrozenMethod();
      if (isDisplayRotationFrozenMethodVersion == 0) {
        return (boolean) method.invoke(manager, displayId);
      }
    } catch (Exception ignored) {
    }

    try {
      if (displayId == Display.DEFAULT_DISPLAY) {
        Method method = getIsRotationFrozenMethod();
        return (boolean) method.invoke(manager);
      }
    } catch (Exception ignored) {
    }
    return false;
  }

  public static void thawRotation(int displayId) {
    try {
      Method method = getThawDisplayRotationMethod();
      switch (thawDisplayRotationMethodVersion) {
        case 0:
          method.invoke(manager, displayId, "easycontrol#thawRotation");
          return;
        case 1:
          method.invoke(manager, displayId);
          return;
        default:
          break;
      }
    } catch (Exception ignored) {
    }

    try {
      if (displayId == Display.DEFAULT_DISPLAY) {
        Method method = getThawRotationMethod();
        method.invoke(manager);
      }
    } catch (Exception ignored) {
    }
  }

  public static void registerRotationWatcher(IRotationWatcher rotationWatcher, int displayId) {
    try {
      try {
        managerClass.getMethod("watchRotation", IRotationWatcher.class, int.class).invoke(manager, rotationWatcher, displayId);
      } catch (NoSuchMethodException e) {
        managerClass.getMethod("watchRotation", IRotationWatcher.class).invoke(manager, rotationWatcher);
      }
    } catch (Exception ignored) {
    }
  }

  private static Method getGetDisplayImePolicyMethod() throws NoSuchMethodException {
    if (getDisplayImePolicyMethod == null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getDisplayImePolicyMethod = managerClass.getMethod("getDisplayImePolicy", int.class);
      } else {
        getDisplayImePolicyMethod = managerClass.getMethod("shouldShowIme", int.class);
      }
    }
    return getDisplayImePolicyMethod;
  }

  public static int getDisplayImePolicy(int displayId) {
    try {
      Method method = getGetDisplayImePolicyMethod();
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return (int) method.invoke(manager, displayId);
      }
      boolean shouldShowIme = (boolean) method.invoke(manager, displayId);
      return shouldShowIme ? DISPLAY_IME_POLICY_LOCAL : DISPLAY_IME_POLICY_FALLBACK_DISPLAY;
    } catch (Exception ignored) {
      return -1;
    }
  }

  private static Method getSetDisplayImePolicyMethod() throws NoSuchMethodException {
    if (setDisplayImePolicyMethod == null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        setDisplayImePolicyMethod = managerClass.getMethod("setDisplayImePolicy", int.class, int.class);
      } else {
        setDisplayImePolicyMethod = managerClass.getMethod("setShouldShowIme", int.class, boolean.class);
      }
    }
    return setDisplayImePolicyMethod;
  }

  public static void setDisplayImePolicy(int displayId, int displayImePolicy) {
    try {
      Method method = getSetDisplayImePolicyMethod();
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        method.invoke(manager, displayId, displayImePolicy);
      } else if (displayImePolicy != DISPLAY_IME_POLICY_HIDE) {
        method.invoke(manager, displayId, displayImePolicy == DISPLAY_IME_POLICY_LOCAL);
      }
    } catch (Exception ignored) {
    }
  }
}
