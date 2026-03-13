package top.zcw.control.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import top.zcw.control.app.databinding.ActivitySetBinding;
import top.zcw.control.app.entity.AppData;
import top.zcw.control.app.helper.PublicTools;
import top.zcw.control.app.helper.ViewTools;

public class SetActivity extends Activity {
  private ActivitySetBinding activitySetBinding;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ViewTools.setStatusAndNavBar(this);
    ViewTools.setLocale(this);
    activitySetBinding = ActivitySetBinding.inflate(this.getLayoutInflater());
    setContentView(activitySetBinding.getRoot());
    // 设置页面
    drawUi();
    setButtonListener();
  }

  // 设置默认值
  private void drawUi() {
    // 其他
    activitySetBinding.setOther.addView(ViewTools.createTextCard(this, getString(R.string.set_other_ip), () -> startActivity(new Intent(this, IpActivity.class))).getRoot());
    activitySetBinding.setOther.addView(ViewTools.createTextCard(this, getString(R.string.set_other_log), () -> startActivity(new Intent(this, LogActivity.class))).getRoot());
    activitySetBinding.setOther.addView(ViewTools.createTextCard(this, getString(R.string.set_other_custom_key), () -> startActivity(new Intent(this, AdbKeyActivity.class))).getRoot());
    activitySetBinding.setOther.addView(ViewTools.createTextCard(this, getString(R.string.set_other_reset_key), () -> {
      AppData.appKeyPair = PublicTools.reGenerateAdbKeyPair();
      Toast.makeText(this, getString(R.string.toast_success), Toast.LENGTH_SHORT).show();
    }).getRoot());
    activitySetBinding.setOther.addView(ViewTools.createTextCard(this, getString(R.string.set_other_locale), () -> {
      AppData.setting.setLocale(AppData.setting.getLocale().equals("en") ? "zh" : "en");
      Toast.makeText(this, getString(R.string.toast_change_locale), Toast.LENGTH_SHORT).show();
    }).getRoot());
    // 关于
    activitySetBinding.setAbout.addView(ViewTools.createTextCard(this, getString(R.string.set_about_website), () -> PublicTools.startUrl(this, "https://github.com/zcw199604/android-scrcpy")).getRoot());
    activitySetBinding.setAbout.addView(ViewTools.createTextCard(this, getString(R.string.set_about_privacy), () -> PublicTools.startUrl(this, "https://github.com/zcw199604/android-scrcpy/blob/main/PRIVACY.md")).getRoot());
    activitySetBinding.setAbout.addView(ViewTools.createTextCard(this, getString(R.string.set_about_version) + BuildConfig.VERSION_NAME, () -> PublicTools.startUrl(this, "https://github.com/zcw199604/android-scrcpy/releases/latest")).getRoot());
  }

  // 设置按钮监听
  private void setButtonListener() {
    activitySetBinding.backButton.setOnClickListener(v -> finish());
  }
}
