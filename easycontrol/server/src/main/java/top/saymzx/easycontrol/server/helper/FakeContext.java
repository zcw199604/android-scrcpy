/*
 * 本项目大量借鉴学习了开源投屏软件：Scrcpy，在此对该项目表示感谢
 */
package top.saymzx.easycontrol.server.helper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.AttributionSource;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Process;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class FakeContext extends ContextWrapper {

  public static final String PACKAGE_NAME = "com.android.shell";
  public static final int ROOT_UID = 0; // Like android.os.Process.ROOT_UID, but before API 29

  private static final FakeContext INSTANCE = new FakeContext();

  public static FakeContext get() {
    return INSTANCE;
  }

  private FakeContext() {
    super(resolveBaseContext());
  }

  private static Context resolveBaseContext() {
    try {
      Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
      Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
      Object activityThread = currentActivityThreadMethod.invoke(null);
      if (activityThread == null) {
        Method systemMainMethod = activityThreadClass.getDeclaredMethod("systemMain");
        activityThread = systemMainMethod.invoke(null);
      }
      if (activityThread != null) {
        Method getSystemContextMethod = activityThreadClass.getDeclaredMethod("getSystemContext");
        getSystemContextMethod.setAccessible(true);
        Context systemContext = (Context) getSystemContextMethod.invoke(activityThread);
        if (systemContext != null) return systemContext;
      }
    } catch (Throwable ignored) {
    }

    try {
      Class<?> appGlobalsClass = Class.forName("android.app.AppGlobals");
      Method getInitialApplicationMethod = appGlobalsClass.getDeclaredMethod("getInitialApplication");
      return (Context) getInitialApplicationMethod.invoke(null);
    } catch (Throwable ignored) {
      return null;
    }
  }

  private static Field findField(Class<?> clazz, String fieldName) {
    Class<?> current = clazz;
    while (current != null) {
      try {
        Field field = current.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
      } catch (NoSuchFieldException ignored) {
        current = current.getSuperclass();
      }
    }
    return null;
  }

  @Override
  public String getPackageName() {
    return PACKAGE_NAME;
  }

  @Override
  public String getOpPackageName() {
    return PACKAGE_NAME;
  }

  @TargetApi(Build.VERSION_CODES.S)
  @Override
  public AttributionSource getAttributionSource() {
    AttributionSource.Builder builder = new AttributionSource.Builder(Process.SHELL_UID);
    builder.setPackageName(PACKAGE_NAME);
    return builder.build();
  }

  // @Override to be added on SDK upgrade for Android 14
  @SuppressWarnings("unused")
  public int getDeviceId() {
    return 0;
  }

  @Override
  public Context getApplicationContext() {
    return this;
  }

  @Override
  public Context createPackageContext(String packageName, int flags) {
    return this;
  }

  @Override
  public ContentResolver getContentResolver() {
    Context base = getBaseContext();
    return base != null ? base.getContentResolver() : super.getContentResolver();
  }

  @SuppressLint("SoonBlockedPrivateApi")
  @Override
  public Object getSystemService(String name) {
    Context base = getBaseContext();
    if (base == null) return null;

    Object service;
    try {
      service = base.getSystemService(name);
    } catch (Throwable ignored) {
      return null;
    }
    if (service == null) return null;

    if (Context.CLIPBOARD_SERVICE.equals(name) || Context.ACTIVITY_SERVICE.equals(name) || "semclipboard".equals(name)) {
      try {
        Field contextField = findField(service.getClass(), "mContext");
        if (contextField == null) return service;
        contextField.set(service, this);
      } catch (ReflectiveOperationException ignored) {
      }
    }
    return service;
  }
}
