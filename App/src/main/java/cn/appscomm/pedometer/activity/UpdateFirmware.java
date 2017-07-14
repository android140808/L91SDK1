package cn.appscomm.pedometer.activity;




import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bugtags.library.Bugtags;

import java.util.Random;

import apps.utils.ConfigHelper;
import apps.utils.Logger;
import apps.utils.PublicData;
import cn.appscomm.pedometer.service.BluetoothLeService;
import cn.l11.appscomm.pedometer.activity.R;

/**
 * Created with Eclipse.
 * Author: Tim Liu  email:9925124@qq.com
 * Date: 14-3-4
 * Time: 10:21
 */
public class UpdateFirmware extends Activity
{
	private final static String TAG = "ReSetDeviceActivity";
	

	private Button btn_reset = null;    

	private RelativeLayout layout_set_up_deveice ;
	
	//顶部时间、电量
	private TextView top_title_time,top_title_battery;
	
	//add begin 2014-5-22
    private int orderType = 0;
    private boolean mIsBind = false; // service与广播是否已经绑定
    private boolean mConnected = false;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
 
    
    
    
   
	private final int RESET_OK = 0x3221;
	private final int RESET_ERROR = 0x3222;
	private final int RESET_TIMEOUT = 0x3223;
	
    private Boolean isRESETSuccess = false;
    private  Boolean IsSyning = false;
	private int retValue = 0;
 
	private Random rand = null;
	private int Randomcode= 0;
	private static final int REQUEST_ENABLE_BT = 3111;

	private static final long MAXSYNTIME = 30000;

	private ProgressDialog mProgressDialog = null; // 进度条
	private AlertDialog.Builder builder = null;
    private boolean isNeedConnect = false; 
    private boolean isAlreadyReturn = false;
    private ImageButton btn_xx;

    
	
    
    
    
    private void closeProgressDiag() {
		if (mProgressDialog != null ) {
			mProgressDialog.dismiss();

		}
	} 
    
    
   
    
	
	
	@Override
	public void onStop() 
	{
    
   
    
    super.onStop();
	}
    
    
    
    
    
    
    
    
    
    
    private Handler  mhander = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			
			if (!mIsBind) return;
			
			switch (msg.what) {
			
				 
			default :
				
				break;

		}
		
		
		
	}
	};
    
    
    
    
    
   
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		

		setContentView(R.layout.updatefireware);
		initView();
		rand = new Random(25);
		mDeviceAddress =   (String) ConfigHelper.getSharePref(this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.MAC_KEY, ConfigHelper.DATA_STRING);
		
		
		
	}
	
  
	
	
	

	
	private void initView() {
		TextView title=(TextView) findViewById(R.id.title);
		title.setText(R.string.app_name);

		
		
		 ConfigHelper.setSharePref(UpdateFirmware.this,
				 PublicData.SHARED_PRE_SAVE_FILE_NAME,
				 PublicData.ISSHOW_SERVER_FW_UPDATE, 0);
		
		
		btn_xx = (ImageButton)findViewById(R.id.btn_xx);
		
		btn_xx.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				finish();
				// TODO Auto-generated method stub
				
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		Bugtags.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Bugtags.onPause(this);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		Bugtags.onDispatchTouchEvent(this, ev);
		return super.dispatchTouchEvent(ev);
	}
	
	// 返回按钮响应事件：
	public void btn_return_clicked(View view){



		finish();
	}
	
	public void onDestroy() {    
    	Logger.w(TAG, "onDestroy()-->");
    	
        super.onDestroy();
    }
    
	


    public void downloadNow(View view) {

        Intent intent = new Intent(this, ShowWebActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("loadingUrl",
                getString(R.string.update_firmware_website));

        intent.putExtras(bundle);
        startActivity(intent);
    }
	

}
