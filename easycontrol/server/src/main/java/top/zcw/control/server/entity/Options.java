/*
 * 本项目大量借鉴学习了开源投屏软件：Scrcpy，在此对该项目表示感谢
 */
package top.zcw.control.server.entity;

public final class Options {
  public static final String ARG_SERVER_PORT = "serverPort";
  public static final String ARG_LISTENER_CLIP = "listenerClip";
  public static final String ARG_LISTEN_CLIP = "listenClip";
  public static final String ARG_IS_AUDIO = "isAudio";
  public static final String ARG_MAX_SIZE = "maxSize";
  public static final String ARG_MAX_VIDEO_BIT = "maxVideoBit";
  public static final String ARG_MAX_FPS = "maxFps";
  public static final String ARG_KEEP_AWAKE = "keepAwake";
  public static final String ARG_SUPPORT_H265 = "supportH265";
  public static final String ARG_SUPPORT_OPUS = "supportOpus";
  public static final String ARG_START_APP = "startApp";

  public static int serverPort = 25166;
  public static boolean listenerClip = true;
  public static boolean isAudio = true;
  public static int maxSize = 1600;
  public static int maxVideoBit = 4000000;
  public static int maxFps = 60;
  public static boolean keepAwake = true;
  public static boolean supportH265 = true;
  public static boolean supportOpus = true;
  public static String startApp = "";

  private Options() {
  }

  private static boolean parseBoolean(String value) {
    return "1".equals(value) || Boolean.parseBoolean(value);
  }

  public static void parse(String... args) {
    for (String arg : args) {
      int equalIndex = arg.indexOf('=');
      if (equalIndex == -1) throw new IllegalArgumentException("参数格式错误");
      String key = arg.substring(0, equalIndex);
      String value = arg.substring(equalIndex + 1);
      switch (key) {
        case ARG_SERVER_PORT:
          serverPort = Integer.parseInt(value);
          break;
        case ARG_LISTENER_CLIP:
        case ARG_LISTEN_CLIP:
          listenerClip = parseBoolean(value);
          break;
        case ARG_IS_AUDIO:
          isAudio = parseBoolean(value);
          break;
        case ARG_MAX_SIZE:
          maxSize = Integer.parseInt(value);
          break;
        case ARG_MAX_FPS:
          maxFps = Integer.parseInt(value);
          break;
        case ARG_MAX_VIDEO_BIT:
          maxVideoBit = Integer.parseInt(value) * 1000000;
          break;
        case ARG_KEEP_AWAKE:
          keepAwake = parseBoolean(value);
          break;
        case ARG_SUPPORT_H265:
          supportH265 = parseBoolean(value);
          break;
        case ARG_SUPPORT_OPUS:
          supportOpus = parseBoolean(value);
          break;
        case ARG_START_APP:
          startApp = value;
          break;
        default:
          break;
      }
    }
  }
}
