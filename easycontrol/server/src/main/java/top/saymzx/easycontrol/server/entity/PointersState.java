/*
 * 本项目大量借鉴学习了开源投屏软件：Scrcpy，在此对该项目表示感谢
 */
package top.saymzx.easycontrol.server.entity;

import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

public final class PointersState {

  public static final int MAX_POINTERS = 10;

  private final List<Pointer> pointers = new ArrayList<>();
  public final MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[MAX_POINTERS];
  public final MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[MAX_POINTERS];

  public PointersState() {
    for (int i = 0; i < MAX_POINTERS; ++i) {
      MotionEvent.PointerProperties properties = new MotionEvent.PointerProperties();
      properties.toolType = MotionEvent.TOOL_TYPE_FINGER;
      pointerProperties[i] = properties;

      MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
      coords.orientation = 0;
      coords.size = 0.01f;
      coords.pressure = 1f;
      pointerCoords[i] = coords;
    }
  }

  private int indexOf(int pointerId) {
    for (int i = 0; i < pointers.size(); ++i) {
      if (pointers.get(i).pointerId == pointerId) {
        return i;
      }
    }
    return -1;
  }

  private boolean isLocalIdAvailable(int localId) {
    for (Pointer pointer : pointers) {
      if (pointer.localId == localId) {
        return false;
      }
    }
    return true;
  }

  private int nextUnusedLocalId() {
    for (int localId = 0; localId < MAX_POINTERS; ++localId) {
      if (isLocalIdAvailable(localId)) {
        return localId;
      }
    }
    return -1;
  }

  public Pointer get(int pointerId) {
    int index = indexOf(pointerId);
    return index == -1 ? null : pointers.get(index);
  }

  public int getIndex(int pointerId) {
    return indexOf(pointerId);
  }

  public Pointer obtain(int pointerId, long now) {
    Pointer pointer = get(pointerId);
    if (pointer != null) {
      return pointer;
    }
    if (pointers.size() >= MAX_POINTERS) {
      return null;
    }
    int localId = nextUnusedLocalId();
    if (localId == -1) {
      return null;
    }
    pointer = new Pointer(pointerId, localId, now);
    pointers.add(pointer);
    return pointer;
  }

  public void remove(int pointerId) {
    int index = indexOf(pointerId);
    if (index != -1) {
      pointers.remove(index);
    }
  }

  public int update() {
    int count = pointers.size();
    for (int i = 0; i < count; ++i) {
      Pointer pointer = pointers.get(i);
      pointerProperties[i].id = pointer.localId;
      pointerCoords[i].x = pointer.x;
      pointerCoords[i].y = pointer.y;
      pointerCoords[i].pressure = pointer.pressure;
    }
    cleanUp();
    return count;
  }

  private void cleanUp() {
    for (int i = pointers.size() - 1; i >= 0; --i) {
      if (pointers.get(i).up) {
        pointers.remove(i);
      }
    }
  }
}
