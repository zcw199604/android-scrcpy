/*
 * 本项目大量借鉴学习了开源投屏软件：Scrcpy，在此对该项目表示感谢
 */
package top.saymzx.easycontrol.server.entity;

import android.util.Pair;

public final class DisplayInfo {
  public static final int FLAG_SUPPORTS_PROTECTED_BUFFERS = 0x00000001;

  public final int displayId;
  public final int width;
  public final int height;
  public final int rotation;
  public final int layerStack;
  public final int density;
  public final int flags;
  public final String uniqueId;

  public DisplayInfo(int displayId, int width, int height, int rotation, int density, int layerStack) {
    this(displayId, width, height, rotation, density, layerStack, 0, null);
  }

  public DisplayInfo(int displayId, int width, int height, int rotation, int density, int layerStack, int flags, String uniqueId) {
    this.displayId = displayId;
    this.width = width;
    this.height = height;
    this.rotation = rotation;
    this.layerStack = layerStack;
    this.density = density;
    this.flags = flags;
    this.uniqueId = uniqueId;
  }

  public int getDisplayId() {
    return displayId;
  }

  public Pair<Integer, Integer> getSize() {
    return new Pair<>(width, height);
  }

  public int getRotation() {
    return rotation;
  }

  public int getLayerStack() {
    return layerStack;
  }

  public int getDpi() {
    return density;
  }

  public int getFlags() {
    return flags;
  }

  public String getUniqueId() {
    return uniqueId;
  }
}
