package top.saymzx.easycontrol.app;

import android.app.Activity;
import android.os.Bundle;

import top.saymzx.easycontrol.app.databinding.ActivityLogBinding;
import top.saymzx.easycontrol.app.helper.PublicTools;
import top.saymzx.easycontrol.app.helper.ViewTools;

public class LogActivity extends Activity {
  private ActivityLogBinding activityLogBinding;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ViewTools.setStatusAndNavBar(this);
    ViewTools.setLocale(this);
    activityLogBinding = ActivityLogBinding.inflate(this.getLayoutInflater());
    setContentView(activityLogBinding.getRoot());
    setButtonListener();
    drawUi();
  }

  @Override
  protected void onResume() {
    super.onResume();
    drawUi();
  }

  private void drawUi() {
    String logText = PublicTools.getRuntimeLogText();
    activityLogBinding.logContent.setText(logText.isEmpty() ? getString(R.string.log_empty) : logText);
  }

  // 设置返回按钮监听
  private void setButtonListener() {
    activityLogBinding.backButton.setOnClickListener(v -> finish());
  }
}
