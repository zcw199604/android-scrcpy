package top.zcw.control.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import top.zcw.control.app.entity.AppData;
import top.zcw.control.app.helper.MyBroadcastReceiver;

public class UsbActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (AppData.mainActivity == null) startActivity(new Intent(this, MainActivity.class));
    else {
      Intent intent = new Intent();
      intent.setAction(MyBroadcastReceiver.ACTION_UPDATE_USB);
      sendBroadcast(intent);
    }
    finish();
  }
}
