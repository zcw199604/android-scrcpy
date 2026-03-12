/*
 * 本项目大量借鉴学习了开源投屏软件：Scrcpy，在此对该项目表示感谢
 */
package top.saymzx.easycontrol.server.helper;

import android.graphics.Rect;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.IBinder;
import android.system.ErrnoException;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

import top.saymzx.easycontrol.server.Server;
import top.saymzx.easycontrol.server.entity.Device;
import top.saymzx.easycontrol.server.entity.Options;
import top.saymzx.easycontrol.server.wrappers.DisplayManager;
import top.saymzx.easycontrol.server.wrappers.SurfaceControl;

public final class VideoEncode {
  private static MediaCodec encedec;
  private static MediaFormat encodecFormat;
  public static volatile boolean isHasChangeConfig = false;
  private static boolean useH265;
  private static IBinder display;
  private static VirtualDisplay virtualDisplay;
  private static Surface surface;
  private static final MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

  private VideoEncode() {
  }

  public static void init() throws Exception {
    useH265 = Options.supportH265 && EncodecTools.isSupportH265();
    ByteBuffer byteBuffer = ByteBuffer.allocate(9);
    byteBuffer.put((byte) (useH265 ? 1 : 0));
    byteBuffer.putInt(Device.videoSize.first);
    byteBuffer.putInt(Device.videoSize.second);
    byteBuffer.flip();
    Server.writeVideo(byteBuffer);

    createEncodecFormat();
    startEncode();
  }

  private static void createEncodecFormat() throws IOException {
    String codecMime = useH265 ? MediaFormat.MIMETYPE_VIDEO_HEVC : MediaFormat.MIMETYPE_VIDEO_AVC;
    encedec = MediaCodec.createEncoderByType(codecMime);
    encodecFormat = new MediaFormat();
    encodecFormat.setString(MediaFormat.KEY_MIME, codecMime);
    encodecFormat.setInteger(MediaFormat.KEY_BIT_RATE, Options.maxVideoBit);
    encodecFormat.setInteger(MediaFormat.KEY_FRAME_RATE, Options.maxFps);
    encodecFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      encodecFormat.setInteger(MediaFormat.KEY_INTRA_REFRESH_PERIOD, Options.maxFps * 3);
    }
    encodecFormat.setFloat("max-fps-to-encoder", Options.maxFps);
    encodecFormat.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 50_000);
    encodecFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
  }

  public static void startEncode() throws Exception {
    if (encedec == null) {
      return;
    }
    ControlPacket.sendVideoSizeEvent();
    encodecFormat.setInteger(MediaFormat.KEY_WIDTH, Device.videoSize.first);
    encodecFormat.setInteger(MediaFormat.KEY_HEIGHT, Device.videoSize.second);
    encedec.configure(encodecFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    surface = encedec.createInputSurface();
    createDisplay(surface);
    encedec.start();
  }

  private static void createDisplay(Surface surface) throws Exception {
    releaseDisplay();
    Exception displayManagerException = null;
    try {
      virtualDisplay = DisplayManager.createVirtualDisplay("easycontrol", Device.videoSize.first, Device.videoSize.second, Device.displayInfo.getDisplayId(), surface);
      System.out.println("Display: using DisplayManager API");
      return;
    } catch (Exception e) {
      displayManagerException = e;
      System.out.println("Display: DisplayManager API unavailable, fallback to SurfaceControl");
    }

    try {
      display = SurfaceControl.createDisplay("easycontrol",
        Build.VERSION.SDK_INT < Build.VERSION_CODES.R || (Build.VERSION.SDK_INT == Build.VERSION_CODES.R && !"S".equals(Build.VERSION.CODENAME)));
      setDisplaySurface(display, surface);
      System.out.println("Display: using SurfaceControl API");
    } catch (Exception e) {
      if (displayManagerException != null) e.addSuppressed(displayManagerException);
      throw e;
    }
  }

  public static void stopEncode() {
    if (encedec == null) {
      return;
    }
    try {
      encedec.stop();
    } catch (Exception ignored) {
    }
    try {
      encedec.reset();
    } catch (Exception ignored) {
    }
    if (surface != null) {
      try {
        surface.release();
      } catch (Exception ignored) {
      }
      surface = null;
    }
  }

  private static void setDisplaySurface(IBinder display, Surface surface) throws Exception {
    SurfaceControl.openTransaction();
    try {
      SurfaceControl.setDisplaySurface(display, surface);
      SurfaceControl.setDisplayProjection(display, 0, new Rect(0, 0, Device.displayInfo.width, Device.displayInfo.height),
        new Rect(0, 0, Device.videoSize.first, Device.videoSize.second));
      SurfaceControl.setDisplayLayerStack(display, Device.displayInfo.layerStack);
    } finally {
      SurfaceControl.closeTransaction();
    }
  }

  public static void encodeOut() throws IOException {
    if (encedec == null) {
      return;
    }
    try {
      int outIndex;
      do {
        outIndex = encedec.dequeueOutputBuffer(bufferInfo, -1);
      } while (outIndex < 0);
      ByteBuffer buffer = encedec.getOutputBuffer(outIndex);
      if (buffer == null) {
        return;
      }
      ControlPacket.sendVideoEvent(bufferInfo.presentationTimeUs, buffer);
      encedec.releaseOutputBuffer(outIndex, false);
    } catch (IllegalStateException ignored) {
    }
  }

  public static void release() {
    stopEncode();
    releaseDisplay();
    if (encedec != null) {
      try {
        encedec.release();
      } catch (Exception ignored) {
      }
      encedec = null;
    }
  }

  private static void releaseDisplay() {
    if (virtualDisplay != null) {
      try {
        virtualDisplay.release();
      } catch (Exception ignored) {
      }
      virtualDisplay = null;
    }
    if (display != null) {
      try {
        SurfaceControl.destroyDisplay(display);
      } catch (Exception ignored) {
      }
      display = null;
    }
  }
}
