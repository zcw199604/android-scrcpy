/*
 * 本项目大量借鉴学习了开源投屏软件：Scrcpy，在此对该项目表示感谢
 */
package top.saymzx.easycontrol.server.wrappers;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.view.Surface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressLint("PrivateApi")
public final class SurfaceControl {
  public static final int POWER_MODE_OFF = 0;
  public static final int POWER_MODE_NORMAL = 2;

  private static Class<?> CLASS;
  private static Method openTransactionMethod;
  private static Method closeTransactionMethod;
  private static Method setDisplayProjectionMethod;
  private static Method setDisplayLayerStackMethod;
  private static Method setDisplaySurfaceMethod;
  private static Method createDisplayMethod;
  private static Method destroyDisplayMethod;
  private static Method getBuiltInDisplayMethod;
  private static Method setDisplayPowerModeMethod;
  private static Method getPhysicalDisplayTokenMethod;
  private static Method getPhysicalDisplayIdsMethod;

  private SurfaceControl() {
  }

  public static void init() throws ClassNotFoundException {
    CLASS = Class.forName("android.view.SurfaceControl");
    openTransactionMethod = null;
    closeTransactionMethod = null;
    setDisplayProjectionMethod = null;
    setDisplayLayerStackMethod = null;
    setDisplaySurfaceMethod = null;
    createDisplayMethod = null;
    destroyDisplayMethod = null;
    getBuiltInDisplayMethod = null;
    setDisplayPowerModeMethod = null;
    getPhysicalDisplayTokenMethod = null;
    getPhysicalDisplayIdsMethod = null;
    resolvePhysicalDisplayMethods();
  }

  private static void resolvePhysicalDisplayMethods() {
    try {
      getPhysicalDisplayIdsMethod = CLASS.getMethod("getPhysicalDisplayIds");
      getPhysicalDisplayTokenMethod = CLASS.getMethod("getPhysicalDisplayToken", long.class);
    } catch (Exception ignored) {
      try {
        Class<?> displayControlClass = loadDisplayControlClass();
        getPhysicalDisplayIdsMethod = displayControlClass.getMethod("getPhysicalDisplayIds");
        getPhysicalDisplayTokenMethod = displayControlClass.getMethod("getPhysicalDisplayToken", long.class);
      } catch (Exception ignoredAgain) {
      }
    }
  }

  @SuppressLint({"PrivateApi", "SoonBlockedPrivateApi", "BlockedPrivateApi"})
  private static Class<?> loadDisplayControlClass() throws Exception {
    Method createClassLoaderMethod = Class.forName("com.android.internal.os.ClassLoaderFactory").getDeclaredMethod(
      "createClassLoader", String.class, String.class, String.class, ClassLoader.class, int.class, boolean.class, String.class);
    ClassLoader classLoader = (ClassLoader) createClassLoaderMethod.invoke(
      null, "/system/framework/services.jar", null, null, ClassLoader.getSystemClassLoader(), 0, true, null);
    return classLoader.loadClass("com.android.server.display.DisplayControl");
  }

  private static Method getOpenTransactionMethod() throws NoSuchMethodException {
    if (openTransactionMethod == null) {
      openTransactionMethod = CLASS.getMethod("openTransaction");
    }
    return openTransactionMethod;
  }

  public static void openTransaction() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = getOpenTransactionMethod();
    method.invoke(null);
  }

  private static Method getCloseTransactionMethod() throws NoSuchMethodException {
    if (closeTransactionMethod == null) {
      closeTransactionMethod = CLASS.getMethod("closeTransaction");
    }
    return closeTransactionMethod;
  }

  public static void closeTransaction() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = getCloseTransactionMethod();
    method.invoke(null);
  }

  private static Method getSetDisplayProjectionMethod() throws NoSuchMethodException {
    if (setDisplayProjectionMethod == null) {
      setDisplayProjectionMethod = CLASS.getMethod("setDisplayProjection", IBinder.class, int.class, Rect.class, Rect.class);
    }
    return setDisplayProjectionMethod;
  }

  public static void setDisplayProjection(IBinder displayToken, int orientation, Rect layerStackRect, Rect displayRect) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = getSetDisplayProjectionMethod();
    method.invoke(null, displayToken, orientation, layerStackRect, displayRect);
  }

  private static Method getSetDisplayLayerStackMethod() throws NoSuchMethodException {
    if (setDisplayLayerStackMethod == null) {
      setDisplayLayerStackMethod = CLASS.getMethod("setDisplayLayerStack", IBinder.class, int.class);
    }
    return setDisplayLayerStackMethod;
  }

  public static void setDisplayLayerStack(IBinder displayToken, int layerStack) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = getSetDisplayLayerStackMethod();
    method.invoke(null, displayToken, layerStack);
  }

  private static Method getSetDisplaySurfaceMethod() throws NoSuchMethodException {
    if (setDisplaySurfaceMethod == null) {
      setDisplaySurfaceMethod = CLASS.getMethod("setDisplaySurface", IBinder.class, Surface.class);
    }
    return setDisplaySurfaceMethod;
  }

  public static void setDisplaySurface(IBinder displayToken, Surface surface) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = getSetDisplaySurfaceMethod();
    method.invoke(null, displayToken, surface);
  }

  private static Method getCreateDisplayMethod() throws NoSuchMethodException {
    if (createDisplayMethod == null) {
      createDisplayMethod = CLASS.getMethod("createDisplay", String.class, boolean.class);
    }
    return createDisplayMethod;
  }

  public static IBinder createDisplay(String name, boolean secure) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = getCreateDisplayMethod();
    return (IBinder) method.invoke(null, name, secure);
  }

  private static Method getDestroyDisplayMethod() throws NoSuchMethodException {
    if (destroyDisplayMethod == null) {
      destroyDisplayMethod = CLASS.getMethod("destroyDisplay", IBinder.class);
    }
    return destroyDisplayMethod;
  }

  public static void destroyDisplay(IBinder displayToken) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = getDestroyDisplayMethod();
    method.invoke(null, displayToken);
  }

  private static Method getGetBuiltInDisplayMethod() throws NoSuchMethodException {
    if (getBuiltInDisplayMethod == null) {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        getBuiltInDisplayMethod = CLASS.getMethod("getBuiltInDisplay", int.class);
      } else {
        getBuiltInDisplayMethod = CLASS.getMethod("getInternalDisplayToken");
      }
    }
    return getBuiltInDisplayMethod;
  }

  public static boolean hasGetBuiltInDisplayMethod() {
    try {
      getGetBuiltInDisplayMethod();
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  // 兼容上游历史拼写，避免后续接入时再改调用侧
  public static boolean hasGetBuildInDisplayMethod() {
    return hasGetBuiltInDisplayMethod();
  }

  public static IBinder getBuiltInDisplay() {
    try {
      Method method = getGetBuiltInDisplayMethod();
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        return (IBinder) method.invoke(null, 0);
      }
      return (IBinder) method.invoke(null);
    } catch (Exception ignored) {
      return null;
    }
  }

  public static IBinder getPhysicalDisplayToken(long physicalDisplayId) {
    if (getPhysicalDisplayTokenMethod == null) {
      return null;
    }
    try {
      return (IBinder) getPhysicalDisplayTokenMethod.invoke(null, physicalDisplayId);
    } catch (Exception ignored) {
      return null;
    }
  }

  public static boolean hasGetPhysicalDisplayIdsMethod() {
    return getPhysicalDisplayIdsMethod != null;
  }

  public static long[] getPhysicalDisplayIds() {
    if (getPhysicalDisplayIdsMethod == null) {
      return null;
    }
    try {
      return (long[]) getPhysicalDisplayIdsMethod.invoke(null);
    } catch (Exception ignored) {
      return null;
    }
  }

  private static Method getSetDisplayPowerModeMethod() throws NoSuchMethodException {
    if (setDisplayPowerModeMethod == null) {
      setDisplayPowerModeMethod = CLASS.getMethod("setDisplayPowerMode", IBinder.class, int.class);
    }
    return setDisplayPowerModeMethod;
  }

  public static void setDisplayPowerMode(IBinder displayToken, int mode) {
    try {
      Method method = getSetDisplayPowerModeMethod();
      method.invoke(null, displayToken, mode);
    } catch (Exception ignored) {
    }
  }
}
