package top.saymzx.easycontrol.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import top.saymzx.easycontrol.app.entity.AppData;
import top.saymzx.easycontrol.app.helper.MyBroadcastReceiver;

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
