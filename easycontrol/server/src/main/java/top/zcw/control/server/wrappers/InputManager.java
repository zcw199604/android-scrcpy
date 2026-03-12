/*
 * 本项目大量借鉴学习了开源投屏软件：Scrcpy，在此对该项目表示感谢
 */
package top.zcw.control.server.wrappers;

import android.view.InputEvent;
import android.view.MotionEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class InputManager {

  public static final int INJECT_INPUT_EVENT_MODE_ASYNC = 0;
  public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT = 1;
  public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2;

  private static Object manager;
  private static Method injectInputEventMethod;
  private static Method setDisplayIdMethod;
  private static Method setActionButtonMethod;
  private static Method addUniqueIdAssociationByPortMethod;
  private static Method removeUniqueIdAssociationByPortMethod;

  private InputManager() {
  }

  public static void init(Object m) throws NoSuchMethodException {
    manager = m;
    injectInputEventMethod = null;
    setDisplayIdMethod = null;
    setActionButtonMethod = null;
    addUniqueIdAssociationByPortMethod = null;
    removeUniqueIdAssociationByPortMethod = null;
  }

  private static Method getInjectInputEventMethod() throws NoSuchMethodException {
    if (injectInputEventMethod == null) {
      injectInputEventMethod = manager.getClass().getMethod("injectInputEvent", InputEvent.class, int.class);
    }
    return injectInputEventMethod;
  }

  private static Method getSetDisplayIdMethod() throws NoSuchMethodException {
    if (setDisplayIdMethod == null) {
      setDisplayIdMethod = InputEvent.class.getMethod("setDisplayId", int.class);
    }
    return setDisplayIdMethod;
  }

  private static Method getSetActionButtonMethod() throws NoSuchMethodException {
    if (setActionButtonMethod == null) {
      setActionButtonMethod = MotionEvent.class.getMethod("setActionButton", int.class);
    }
    return setActionButtonMethod;
  }

  private static Method getAddUniqueIdAssociationByPortMethod() throws NoSuchMethodException {
    if (addUniqueIdAssociationByPortMethod == null) {
      addUniqueIdAssociationByPortMethod = manager.getClass().getMethod("addUniqueIdAssociationByPort", String.class, String.class);
    }
    return addUniqueIdAssociationByPortMethod;
  }

  private static Method getRemoveUniqueIdAssociationByPortMethod() throws NoSuchMethodException {
    if (removeUniqueIdAssociationByPortMethod == null) {
      removeUniqueIdAssociationByPortMethod = manager.getClass().getMethod("removeUniqueIdAssociationByPort", String.class);
    }
    return removeUniqueIdAssociationByPortMethod;
  }

  public static void setDisplayId(InputEvent inputEvent, int displayId) throws InvocationTargetException, IllegalAccessException {
    try {
      Method method = getSetDisplayIdMethod();
      method.invoke(inputEvent, displayId);
    } catch (NoSuchMethodException ignored) {
    }
  }

  public static void setActionButton(MotionEvent motionEvent, int actionButton) throws InvocationTargetException, IllegalAccessException {
    try {
      Method method = getSetActionButtonMethod();
      method.invoke(motionEvent, actionButton);
    } catch (NoSuchMethodException ignored) {
    }
  }

  public static void addUniqueIdAssociationByPort(String inputPort, String uniqueId) throws InvocationTargetException, IllegalAccessException {
    try {
      Method method = getAddUniqueIdAssociationByPortMethod();
      method.invoke(manager, inputPort, uniqueId);
    } catch (NoSuchMethodException ignored) {
    }
  }

  public static void removeUniqueIdAssociationByPort(String inputPort) throws InvocationTargetException, IllegalAccessException {
    try {
      Method method = getRemoveUniqueIdAssociationByPortMethod();
      method.invoke(manager, inputPort);
    } catch (NoSuchMethodException ignored) {
    }
  }

  public static void injectInputEvent(InputEvent inputEvent, int mode) throws InvocationTargetException, IllegalAccessException {
    try {
      Method method = getInjectInputEventMethod();
      method.invoke(manager, inputEvent, mode);
    } catch (NoSuchMethodException e) {
      throw new AssertionError(e);
    }
  }
}
