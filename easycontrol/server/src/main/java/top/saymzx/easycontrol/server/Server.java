/*
 * 本项目大量借鉴学习了开源投屏软件：Scrcpy，在此对该项目表示感谢
 */
package top.saymzx.easycontrol.server;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.os.IInterface;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import top.saymzx.easycontrol.server.entity.Device;
import top.saymzx.easycontrol.server.entity.Options;
import top.saymzx.easycontrol.server.helper.AudioEncode;
import top.saymzx.easycontrol.server.helper.ControlPacket;
import top.saymzx.easycontrol.server.helper.VideoEncode;
import top.saymzx.easycontrol.server.wrappers.ClipboardManager;
import top.saymzx.easycontrol.server.wrappers.DisplayManager;
import top.saymzx.easycontrol.server.wrappers.InputManager;
import top.saymzx.easycontrol.server.wrappers.SurfaceControl;
import top.saymzx.easycontrol.server.wrappers.WindowManager;

// 此部分代码摘抄借鉴了著名投屏软件Scrcpy的开源代码(https://github.com/Genymobile/scrcpy/tree/master/server)
public final class Server {
  private static Socket mainSocket;
  private static Socket videoSocket;
  private static OutputStream mainOutputStream;
  private static OutputStream videoOutputStream;
  public static DataInputStream mainInputStream;

  private static final Object object = new Object();
  private static final int timeoutDelay = 1000 * 20;
  private static volatile boolean released;
  private static long lastKeepAliveTime = System.currentTimeMillis();

  private Server() {
  }

  public static void main(String... args) {
    try {
      Thread timeOutThread = new Thread(() -> {
        try {
          Thread.sleep(timeoutDelay);
          release();
        } catch (InterruptedException ignored) {
        }
      });
      timeOutThread.start();

      Options.parse(args);
      setManagers();
      Device.init();
      connectClient();
      lastKeepAliveTime = System.currentTimeMillis();

      boolean canAudio = AudioEncode.init();
      VideoEncode.init();

      ArrayList<Thread> threads = new ArrayList<>();
      threads.add(new Thread(Server::executeVideoOut));
      if (canAudio) {
        threads.add(new Thread(Server::executeAudioIn));
        threads.add(new Thread(Server::executeAudioOut));
      }
      threads.add(new Thread(Server::executeControlIn));
      for (Thread thread : threads) {
        thread.setPriority(Thread.MAX_PRIORITY);
      }
      for (Thread thread : threads) {
        thread.start();
      }

      timeOutThread.interrupt();
      synchronized (object) {
        object.wait();
      }
      for (Thread thread : threads) {
        thread.interrupt();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      release();
    }
  }

  private static Method GET_SERVICE_METHOD;

  @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
  private static void setManagers() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    GET_SERVICE_METHOD = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
    WindowManager.init(getService("window", "android.view.IWindowManager"));
    DisplayManager.init(Class.forName("android.hardware.display.DisplayManagerGlobal").getDeclaredMethod("getInstance").invoke(null));

    Class<?> inputManagerClass;
    try {
      inputManagerClass = Class.forName("android.hardware.input.InputManagerGlobal");
    } catch (ClassNotFoundException e) {
      inputManagerClass = android.hardware.input.InputManager.class;
    }
    InputManager.init(inputManagerClass.getDeclaredMethod("getInstance").invoke(null));
    ClipboardManager.init(getService("clipboard", "android.content.IClipboard"));
    SurfaceControl.init();
  }

  private static IInterface getService(String service, String type) {
    try {
      IBinder binder = (IBinder) GET_SERVICE_METHOD.invoke(null, service);
      Method asInterfaceMethod = Class.forName(type + "$Stub").getMethod("asInterface", IBinder.class);
      return (IInterface) asInterfaceMethod.invoke(null, binder);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private static void connectClient() throws IOException {
    try (ServerSocket serverSocket = new ServerSocket(Options.serverPort)) {
      mainSocket = serverSocket.accept();
      videoSocket = serverSocket.accept();
      mainOutputStream = mainSocket.getOutputStream();
      videoOutputStream = videoSocket.getOutputStream();
      mainInputStream = new DataInputStream(mainSocket.getInputStream());
    }
  }

  private static void executeVideoOut() {
    try {
      int frame = 0;
      while (!Thread.interrupted()) {
        if (VideoEncode.isHasChangeConfig) {
          VideoEncode.isHasChangeConfig = false;
          VideoEncode.stopEncode();
          VideoEncode.startEncode();
        }
        VideoEncode.encodeOut();
        frame++;
        if (frame > 120) {
          if (System.currentTimeMillis() - lastKeepAliveTime > timeoutDelay) {
            throw new IOException("连接断开");
          }
          frame = 0;
        }
      }
    } catch (Exception e) {
      errorClose(e);
    }
  }

  private static void executeAudioIn() {
    while (!Thread.interrupted()) {
      AudioEncode.encodeIn();
    }
  }

  private static void executeAudioOut() {
    try {
      while (!Thread.interrupted()) {
        AudioEncode.encodeOut();
      }
    } catch (Exception e) {
      errorClose(e);
    }
  }

  private static void executeControlIn() {
    try {
      while (!Thread.interrupted()) {
        switch (Server.mainInputStream.readByte()) {
          case ControlPacket.TYPE_TOUCH_EVENT:
            ControlPacket.handleTouchEvent();
            break;
          case ControlPacket.TYPE_KEY_EVENT:
            ControlPacket.handleKeyEvent();
            break;
          case ControlPacket.TYPE_CLIPBOARD_EVENT:
            ControlPacket.handleClipboardEvent();
            break;
          case ControlPacket.TYPE_KEEP_ALIVE:
            lastKeepAliveTime = System.currentTimeMillis();
            break;
          case ControlPacket.TYPE_CHANGE_RESOLUTION_BY_RATIO:
            Device.changeResolution(mainInputStream.readFloat());
            break;
          case ControlPacket.TYPE_ROTATE_EVENT:
            Device.rotateDevice();
            break;
          case ControlPacket.TYPE_LIGHT_EVENT:
            Device.changeScreenPowerMode(mainInputStream.readByte());
            break;
          case ControlPacket.TYPE_POWER_EVENT:
            Device.changePower(mainInputStream.readInt());
            break;
          case ControlPacket.TYPE_CHANGE_RESOLUTION_BY_SIZE:
            Device.changeResolution(mainInputStream.readInt(), mainInputStream.readInt());
            break;
          default:
            break;
        }
      }
    } catch (Exception e) {
      errorClose(e);
    }
  }

  public static synchronized void writeMain(ByteBuffer byteBuffer) throws IOException {
    mainOutputStream.write(byteBuffer.array());
  }

  public static synchronized void writeVideo(ByteBuffer byteBuffer) throws IOException {
    videoOutputStream.write(byteBuffer.array());
  }

  public static void errorClose(Exception e) {
    e.printStackTrace();
    synchronized (object) {
      object.notify();
    }
  }

  private static void closeQuietly(Closeable closeable) {
    if (closeable == null) {
      return;
    }
    try {
      closeable.close();
    } catch (Exception ignored) {
    }
  }

  private static void closeQuietly(Socket socket) {
    if (socket == null) {
      return;
    }
    try {
      socket.close();
    } catch (Exception ignored) {
    }
  }

  // 释放资源
  private static void release() {
    if (released) {
      return;
    }
    released = true;

    closeQuietly(mainInputStream);
    closeQuietly(mainOutputStream);
    closeQuietly(videoOutputStream);
    closeQuietly(mainSocket);
    closeQuietly(videoSocket);

    try {
      VideoEncode.release();
    } catch (Exception ignored) {
    }
    try {
      AudioEncode.release();
    } catch (Exception ignored) {
    }
    try {
      Device.fallbackResolution();
    } catch (Exception ignored) {
    }
    try {
      Device.fallbackScreenLightTimeout();
    } catch (Exception ignored) {
    }
    Runtime.getRuntime().exit(0);
  }
}
