package top.saymzx.easycontrol.app.client.tools;

import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.CRC32;

import top.saymzx.easycontrol.app.BuildConfig;
import top.saymzx.easycontrol.app.R;
import top.saymzx.easycontrol.app.adb.Adb;
import top.saymzx.easycontrol.app.buffer.BufferStream;
import top.saymzx.easycontrol.app.client.decode.DecodecTools;
import top.saymzx.easycontrol.app.entity.AppData;
import top.saymzx.easycontrol.app.entity.Device;
import top.saymzx.easycontrol.app.entity.MyInterface;
import top.saymzx.easycontrol.app.helper.PublicTools;

public class ClientStream {
  private static final String ARG_SERVER_PORT = "serverPort";
  private static final String ARG_LISTEN_CLIP = "listenClip";
  private static final String ARG_IS_AUDIO = "isAudio";
  private static final String ARG_MAX_SIZE = "maxSize";
  private static final String ARG_MAX_FPS = "maxFps";
  private static final String ARG_MAX_VIDEO_BIT = "maxVideoBit";
  private static final String ARG_KEEP_AWAKE = "keepAwake";
  private static final String ARG_SUPPORT_H265 = "supportH265";
  private static final String ARG_SUPPORT_OPUS = "supportOpus";
  private static final String ARG_START_APP = "startApp";

  private static final String ERROR_ADB_USB_DEVICE_MISSING = "ADB_USB_DEVICE_MISSING";
  private static final String ERROR_ADB_AUTH_FAILED = "ADB_AUTH_FAILED";
  private static final String ERROR_ADB_CONNECT_FAILED = "ADB_CONNECT_FAILED";
  private static final String ERROR_SERVER_CONNECT_FAILED = "SERVER_CONNECT_FAILED";

  private boolean isClose = false;
  private boolean connectDirect = false;
  private final Device device;
  private Adb adb;
  private Socket mainSocket;
  private Socket videoSocket;
  private OutputStream mainOutputStream;
  private DataInputStream mainDataInputStream;
  private DataInputStream videoDataInputStream;
  private BufferStream mainBufferStream;
  private BufferStream videoBufferStream;
  private BufferStream shell;
  private Thread connectThread = null;
  private final AtomicBoolean connectResultHandled = new AtomicBoolean(false);
  private static String cachedServerName;
  private static final boolean supportH265 = DecodecTools.isSupportH265();
  private static final boolean supportOpus = DecodecTools.isSupportOpus();

  private static final int timeoutDelay = 1000 * 15;

  public ClientStream(Device device, MyInterface.MyFunctionBoolean handle) {
    this.device = device;
    PublicTools.logInfo("stream", device.name + " 开始建立连接");
    Thread timeOutThread = new Thread(() -> {
      try {
        Thread.sleep(timeoutDelay);
        if (notifyConnectResult(handle, false)) {
          PublicTools.logInfo("stream", device.name + " 连接超时");
          showConnectToast(AppData.applicationContext.getString(R.string.toast_connect_timeout_detail));
        }
        if (connectThread != null) connectThread.interrupt();
      } catch (InterruptedException ignored) {
      }
    });
    connectThread = new Thread(() -> {
      try {
        PublicTools.logInfo("stream", device.name + " 正在连接 ADB");
        adb = AdbTools.connectADB(device);
        PublicTools.logInfo("stream", device.name + " ADB 连接成功");
        startServer(device);
        PublicTools.logInfo("stream", device.name + " 被控端服务已启动");
        connectServer(device);
        PublicTools.logInfo("stream", device.name + " 数据通道已建立（" + (connectDirect ? "直连" : "ADB forward") + "）");
        notifyConnectResult(handle, true);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        PublicTools.logInfo("stream", device.name + " 连接线程被中断");
        notifyConnectResult(handle, false);
      } catch (Exception e) {
        if (notifyConnectResult(handle, false)) showConnectError(device, e);
      } finally {
        timeOutThread.interrupt();
      }
    });
    connectThread.start();
    timeOutThread.start();
  }

  private boolean notifyConnectResult(MyInterface.MyFunctionBoolean handle, boolean result) {
    if (connectResultHandled.compareAndSet(false, true)) {
      handle.run(result);
      return true;
    }
    return false;
  }

  private void showConnectError(Device device, Exception e) {
    String userMessage = buildUserMessage(device, e);
    PublicTools.logToast("stream", buildDebugMessage(device, e), false);
    PublicTools.logInfo("stream", device.name + " 连接失败: " + userMessage);
    showConnectToast(userMessage);
  }

  private void showConnectToast(String message) {
    AppData.uiHandler.post(() -> Toast.makeText(AppData.applicationContext, message, Toast.LENGTH_LONG).show());
  }

  private String buildDebugMessage(Device device, Exception e) {
    StringBuilder builder = new StringBuilder();
    builder.append(device.name);
    builder.append(" [");
    builder.append(device.isLinkDevice() ? "USB" : buildAdbTarget(device));
    builder.append("] ");
    builder.append(e);
    Throwable root = getRootCause(e);
    if (root != e) {
      builder.append(" | root=");
      builder.append(root);
    }
    return builder.toString();
  }

  private String buildUserMessage(Device device, Exception e) {
    Throwable root = getRootCause(e);
    String errorCode = e.getMessage();
    String rootMessage = root.getMessage();

    if (ERROR_ADB_USB_DEVICE_MISSING.equals(errorCode) || ERROR_ADB_USB_DEVICE_MISSING.equals(rootMessage) || "no usb connect".equals(rootMessage)) {
      return AppData.applicationContext.getString(R.string.toast_connect_adb_usb_missing);
    }
    if ("not have usbManager".equals(rootMessage) || "有线连接错误".equals(rootMessage)) {
      return AppData.applicationContext.getString(R.string.toast_connect_adb_usb_error);
    }
    if (ERROR_ADB_AUTH_FAILED.equals(errorCode) || ERROR_ADB_AUTH_FAILED.equals(rootMessage)) {
      return AppData.applicationContext.getString(device.isLinkDevice() ? R.string.toast_connect_adb_usb_authorize : R.string.toast_connect_adb_network_authorize);
    }
    if (ERROR_ADB_CONNECT_FAILED.equals(errorCode) || ERROR_ADB_CONNECT_FAILED.equals(rootMessage)) {
      return AppData.applicationContext.getString(device.isLinkDevice() ? R.string.toast_connect_adb_usb_error : R.string.toast_connect_adb_network, buildAdbTarget(device));
    }
    if (root instanceof UnknownHostException || root instanceof ConnectException || root instanceof SocketTimeoutException) {
      return AppData.applicationContext.getString(device.isLinkDevice() ? R.string.toast_connect_adb_usb_error : R.string.toast_connect_adb_network, buildAdbTarget(device));
    }
    if (ERROR_SERVER_CONNECT_FAILED.equals(errorCode) || ERROR_SERVER_CONNECT_FAILED.equals(rootMessage) || AppData.applicationContext.getString(R.string.toast_connect_server).equals(errorCode)) {
      return AppData.applicationContext.getString(R.string.toast_connect_server_detail, device.serverPort);
    }
    if (rootMessage != null && !rootMessage.trim().isEmpty()) {
      return AppData.applicationContext.getString(R.string.toast_connect_detail, sanitizeMessage(rootMessage));
    }
    return AppData.applicationContext.getString(R.string.toast_connect_server);
  }

  private Throwable getRootCause(Throwable throwable) {
    Throwable current = throwable;
    while (current.getCause() != null && current.getCause() != current) current = current.getCause();
    return current;
  }

  private String sanitizeMessage(String message) {
    return message.replace("\n", " ").replace("\r", " ").trim();
  }

  private String buildAdbTarget(Device device) {
    if (device.isLinkDevice()) return device.name;
    try {
      return PublicTools.getIp(device.address) + ":" + device.adbPort;
    } catch (Exception ignored) {
      return device.address + ":" + device.adbPort;
    }
  }

  private static synchronized String getServerName() {
    if (cachedServerName != null) return cachedServerName;
    String serverSuffix = String.valueOf(BuildConfig.VERSION_CODE);
    try (InputStream inputStream = AppData.applicationContext.getResources().openRawResource(R.raw.easycontrol_server)) {
      CRC32 crc32 = new CRC32();
      byte[] buffer = new byte[8192];
      int len;
      while ((len = inputStream.read(buffer)) != -1) crc32.update(buffer, 0, len);
      serverSuffix = BuildConfig.VERSION_CODE + "_" + Long.toHexString(crc32.getValue());
    } catch (Exception ignored) {
    }
    cachedServerName = "/data/local/tmp/easycontrol_server_" + serverSuffix + ".jar";
    return cachedServerName;
  }

  // 启动Server
  private void startServer(Device device) throws Exception {
    String serverName = getServerName();
    if (BuildConfig.ENABLE_DEBUG_FEATURE || !adb.runAdbCmd("ls /data/local/tmp/easycontrol_*").contains(serverName)) {
      PublicTools.logInfo("stream", device.name + " 正在同步被控端服务文件");
      adb.runAdbCmd("rm /data/local/tmp/easycontrol_* ");
      adb.pushFile(AppData.applicationContext.getResources().openRawResource(R.raw.easycontrol_server), serverName, null);
    } else PublicTools.logInfo("stream", device.name + " 被控端服务文件已是最新版本");
    shell = adb.getShell();
    shell.write(ByteBuffer.wrap(("app_process -Djava.class.path=" + serverName + " / top.saymzx.easycontrol.server.Server"
      + " " + ARG_SERVER_PORT + "=" + device.serverPort
      + " " + ARG_LISTEN_CLIP + "=" + (device.listenClip ? 1 : 0)
      + " " + ARG_IS_AUDIO + "=" + (device.isAudio ? 1 : 0)
      + " " + ARG_MAX_SIZE + "=" + device.maxSize
      + " " + ARG_MAX_FPS + "=" + device.maxFps
      + " " + ARG_MAX_VIDEO_BIT + "=" + device.maxVideoBit
      + " " + ARG_KEEP_AWAKE + "=" + (device.keepWakeOnRunning ? 1 : 0)
      + " " + ARG_SUPPORT_H265 + "=" + ((device.useH265 && supportH265) ? 1 : 0)
      + " " + ARG_SUPPORT_OPUS + "=" + (supportOpus ? 1 : 0)
      + " " + ARG_START_APP + "=" + device.startApp + " \n").getBytes()));
  }

  // 连接Server
  private void connectServer(Device device) throws Exception {
    Thread.sleep(50);
    int reTry = 40;
    int reTryTime = timeoutDelay / reTry;
    Exception directException = null;
    Exception forwardException = null;
    if (!device.isLinkDevice() && !device.forceAdbForwardOnConnect) {
      long startTime = System.currentTimeMillis();
      boolean mainConn = false;
      InetSocketAddress inetSocketAddress = new InetSocketAddress(PublicTools.getIp(device.address), device.serverPort);
      for (int i = 0; i < reTry; i++) {
        try {
          if (!mainConn) {
            mainSocket = new Socket();
            mainSocket.connect(inetSocketAddress, timeoutDelay / 2);
            mainConn = true;
          }
          videoSocket = new Socket();
          videoSocket.connect(inetSocketAddress, timeoutDelay / 2);
          mainOutputStream = mainSocket.getOutputStream();
          mainDataInputStream = new DataInputStream(mainSocket.getInputStream());
          videoDataInputStream = new DataInputStream(videoSocket.getInputStream());
          connectDirect = true;
          PublicTools.logInfo("stream", device.name + " 已通过直连建立数据通道");
          return;
        } catch (Exception e) {
          directException = e;
          if (mainSocket != null) mainSocket.close();
          if (videoSocket != null) videoSocket.close();
          if (System.currentTimeMillis() - startTime >= timeoutDelay / 2 - 1000) i = reTry;
          else Thread.sleep(reTryTime);
        }
      }
      if (directException != null) PublicTools.logInfo("stream", device.name + " 直连未成功，回退到 ADB forward");
    }
    for (int i = 0; i < reTry; i++) {
      try {
        if (mainBufferStream == null) mainBufferStream = adb.tcpForward(device.serverPort);
        if (videoBufferStream == null) videoBufferStream = adb.tcpForward(device.serverPort);
        PublicTools.logInfo("stream", device.name + " 已通过 ADB forward 建立数据通道");
        return;
      } catch (Exception e) {
        forwardException = e;
        Thread.sleep(reTryTime);
      }
    }
    throw new Exception(ERROR_SERVER_CONNECT_FAILED, forwardException != null ? forwardException : directException);
  }

  public String runShell(String cmd) throws Exception {
    return adb.runAdbCmd(cmd);
  }

  public byte readByteFromMain() throws IOException, InterruptedException {
    if (connectDirect) return mainDataInputStream.readByte();
    else return mainBufferStream.readByte();
  }

  public byte readByteFromVideo() throws IOException, InterruptedException {
    if (connectDirect) return videoDataInputStream.readByte();
    else return videoBufferStream.readByte();
  }

  public int readIntFromMain() throws IOException, InterruptedException {
    if (connectDirect) return mainDataInputStream.readInt();
    else return mainBufferStream.readInt();
  }

  public int readIntFromVideo() throws IOException, InterruptedException {
    if (connectDirect) return videoDataInputStream.readInt();
    else return videoBufferStream.readInt();
  }

  public ByteBuffer readByteArrayFromMain(int size) throws IOException, InterruptedException {
    if (connectDirect) {
      byte[] buffer = new byte[size];
      mainDataInputStream.readFully(buffer);
      return ByteBuffer.wrap(buffer);
    } else return mainBufferStream.readByteArray(size);
  }

  public ByteBuffer readByteArrayFromVideo(int size) throws IOException, InterruptedException {
    if (connectDirect) {
      byte[] buffer = new byte[size];
      videoDataInputStream.readFully(buffer);
      return ByteBuffer.wrap(buffer);
    }
    return videoBufferStream.readByteArray(size);
  }

  public ByteBuffer readFrameFromMain() throws Exception {
    if (!connectDirect) mainBufferStream.flush();
    return readByteArrayFromMain(readIntFromMain());
  }

  public ByteBuffer readFrameFromVideo() throws Exception {
    if (!connectDirect) videoBufferStream.flush();
    int size = readIntFromVideo();
    return readByteArrayFromVideo(size);
  }

  public void writeToMain(ByteBuffer byteBuffer) throws Exception {
    if (connectDirect) mainOutputStream.write(byteBuffer.array());
    else mainBufferStream.write(byteBuffer);
  }

  public void close() {
    if (isClose) return;
    isClose = true;
    PublicTools.logInfo("stream", device.name + " 开始释放连接资源");
    if (shell != null) {
      String shellOutput = new String(shell.readByteArrayBeforeClose().array()).trim();
      if (!shellOutput.isEmpty()) PublicTools.logToast("server", shellOutput, false);
    }
    if (connectDirect) {
      try {
        mainOutputStream.close();
        videoDataInputStream.close();
        mainDataInputStream.close();
        mainSocket.close();
        videoSocket.close();
      } catch (Exception ignored) {
      }
    } else {
      if (mainBufferStream != null) mainBufferStream.close();
      if (videoBufferStream != null) videoBufferStream.close();
    }
  }
}
