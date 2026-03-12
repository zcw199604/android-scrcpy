package top.zcw.control.app.client.tools;

import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class ControlPacket {
  public static final byte TYPE_TOUCH_EVENT = 1;
  public static final byte TYPE_KEY_EVENT = 2;
  public static final byte TYPE_CLIPBOARD_EVENT = 3;
  public static final byte TYPE_KEEP_ALIVE = 4;
  public static final byte TYPE_CHANGE_RESOLUTION_BY_RATIO = 5;
  public static final byte TYPE_ROTATE_EVENT = 6;
  public static final byte TYPE_LIGHT_EVENT = 7;
  public static final byte TYPE_POWER_EVENT = 8;
  public static final byte TYPE_CHANGE_RESOLUTION_BY_SIZE = 9;

  public static final byte EVENT_AUDIO = 1;
  public static final byte EVENT_CLIPBOARD = 2;
  public static final byte EVENT_VIDEO_SIZE = 3;

  private ControlPacket() {
  }

  // 触摸事件
  public static ByteBuffer createTouchEvent(int action, int p, float x, float y, int offsetTime) {
    if (x < 0 || x > 1 || y < 0 || y > 1) {
      if (x < 0) x = 0;
      if (x > 1) x = 1;
      if (y < 0) y = 0;
      if (y > 1) y = 1;
      action = MotionEvent.ACTION_UP;
    }
    ByteBuffer byteBuffer = ByteBuffer.allocate(15);
    byteBuffer.put(TYPE_TOUCH_EVENT);
    byteBuffer.put((byte) action);
    byteBuffer.put((byte) p);
    byteBuffer.putFloat(x);
    byteBuffer.putFloat(y);
    byteBuffer.putInt(offsetTime);
    byteBuffer.flip();
    return byteBuffer;
  }

  // 按键事件
  public static ByteBuffer createKeyEvent(int key, int meta) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(9);
    byteBuffer.put(TYPE_KEY_EVENT);
    byteBuffer.putInt(key);
    byteBuffer.putInt(meta);
    byteBuffer.flip();
    return byteBuffer;
  }

  // 剪切板事件
  public static ByteBuffer createClipboardEvent(String text) {
    byte[] tmpTextByte = text.getBytes(StandardCharsets.UTF_8);
    if (tmpTextByte.length == 0 || tmpTextByte.length > 5000) return null;
    ByteBuffer byteBuffer = ByteBuffer.allocate(5 + tmpTextByte.length);
    byteBuffer.put(TYPE_CLIPBOARD_EVENT);
    byteBuffer.putInt(tmpTextByte.length);
    byteBuffer.put(tmpTextByte);
    byteBuffer.flip();
    return byteBuffer;
  }

  // 心跳包
  public static ByteBuffer createKeepAlive() {
    return ByteBuffer.wrap(new byte[]{TYPE_KEEP_ALIVE});
  }

  // 修改分辨率事件
  public static ByteBuffer createChangeResolutionEvent(float newSize) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(5);
    byteBuffer.put(TYPE_CHANGE_RESOLUTION_BY_RATIO);
    byteBuffer.putFloat(newSize);
    byteBuffer.flip();
    return byteBuffer;
  }

  // 修改分辨率事件
  public static ByteBuffer createChangeResolutionEvent(int width, int height) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(9);
    byteBuffer.put(TYPE_CHANGE_RESOLUTION_BY_SIZE);
    byteBuffer.putInt(width);
    byteBuffer.putInt(height);
    byteBuffer.flip();
    return byteBuffer;
  }

  // 旋转请求事件
  public static ByteBuffer createRotateEvent() {
    return ByteBuffer.wrap(new byte[]{TYPE_ROTATE_EVENT});
  }

  // 背光控制事件
  public static ByteBuffer createLightEvent(int mode) {
    return ByteBuffer.wrap(new byte[]{TYPE_LIGHT_EVENT, (byte) mode});
  }

  // 电源键事件
  public static ByteBuffer createPowerEvent(int mode) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(5);
    byteBuffer.put(TYPE_POWER_EVENT);
    byteBuffer.putInt(mode);
    byteBuffer.flip();
    return byteBuffer;
  }
}
