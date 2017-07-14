package cn.appscomm.pedometer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import apps.utils.ConfigHelper;
import apps.utils.Logger;
import apps.utils.PublicData;
import cn.l11.appscomm.pedometer.activity.R;

import static cn.l11.appscomm.pedometer.activity.R.string.adseting_advanced_Settings;


public class DvancedSettingsActivity extends Activity implements View.OnClickListener{
    private Context mContext;
    private String deviceType;
    private RelativeLayout rl_inactivity;
    private RelativeLayout rl_preset_sleep;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dvanced_settings);
        mContext = this;
        //绑定的设备类型
        deviceType = (String) ConfigHelper.getSharePref(mContext,
                PublicData.SHARED_PRE_SAVE_FILE_NAME,
                PublicData.CURRENT_BIND_DEVICE_ITEM, ConfigHelper.DATA_STRING);


        findView();
        initData();
        initView();
    }
    // 返回按钮响应事件：
    public void btn_return1_clicked(View view) {
        Logger.d("", ">>>>>>>>>>>return1");
        DvancedSettingsActivity.this.finish();
    }
    private void findView(){
        TextView tv_title = (TextView) findViewById(R.id.title);
        tv_title.setText(getString(R.string.adseting_advanced_Settings));
        rl_inactivity=  (RelativeLayout) findViewById(R.id.rl_inactivity);
        rl_preset_sleep=  (RelativeLayout) findViewById(R.id.rl_preset_sleep);
        rl_inactivity.setOnClickListener(this);
        rl_preset_sleep.setOnClickListener(this);
    }
    private void initData(){}
    private void initView(){}

    @Override
    public void onClick(View v) {
        Intent intent=null;

        switch (v.getId()){
            case R.id.rl_inactivity:
                intent = new Intent(DvancedSettingsActivity.this, AlertActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_preset_sleep:
                intent = new Intent(DvancedSettingsActivity.this, PresetSleepActivity.class);
                startActivity(intent);
                break;
        }
    }
}
