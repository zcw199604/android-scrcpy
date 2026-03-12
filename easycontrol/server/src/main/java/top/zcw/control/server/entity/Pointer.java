/*
 * 本项目大量借鉴学习了开源投屏软件：Scrcpy，在此对该项目表示感谢
 */
package top.zcw.control.server.entity;

public final class Pointer {

  public final int pointerId;
  public final int localId;
  public float x;
  public float y;
  public float pressure = 1f;
  public boolean up;
  public final long downTime;

  public Pointer(int pointerId, int localId, long downTime) {
    this.pointerId = pointerId;
    this.localId = localId;
    this.downTime = downTime;
  }
}
