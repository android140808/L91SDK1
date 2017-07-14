package cn.appscomm.pedometer.sms;

import java.util.Calendar;
import java.util.Date;





import cn.appscomm.pedometer.service.BluetoothLeService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import apps.utils.ConfigHelper;
import apps.utils.DialogUtil;
import apps.utils.Logger;
import apps.utils.NumberUtils;
import apps.utils.PublicData;
import apps.utils.TimesrUtils;

public class BootService extends Service {

	public static final String TAG = "BootService";
    public String gLastCallNo = "";
	private ContentObserver mObserver;
	private ContentResolver resolver;
	
	private BluetoothLeService mBluetoothLeService;
	
	private static int smsCount = 0;
	private int orderType = 1; 
	private int retValue = 0;
	private boolean isNeedConnect = false;
	private boolean mConnected = false;
	private boolean Needresponse = false;
    private static int pushMsgType =0 ; //0电话      1: sms
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case SMS.PROC_SMS:
				Log.d(TAG, "<<<==proc_sms");
				
				getSmsFromPhone();
				//count = 3;
				Log.d(TAG, "---当天未读短信条数" + smsCount + "---");
			//	Toast.makeText(getApplicationContext(), "当天未读短信条数 [" + smsCount + "]", Toast.LENGTH_SHORT).show();
				if (null != mBluetoothLeService && smsCount > 0) {
					
					
			
					  boolean  is_smspush = (Boolean) ConfigHelper.getSharePref(getApplicationContext(), PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.IS_SMS_PUSH, ConfigHelper.DATA_BOOLEAN);
					
					if (!is_smspush)
						{
						 Log.d(TAG, "SMS notification disable");
						  return;	
						}
					
					
					
						isNeedConnect = true;
						Needresponse = true;
						String mDeviceAddress = (String)ConfigHelper.getSharePref(getApplicationContext(), PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.MAC_KEY, ConfigHelper.DATA_STRING);
					    mBluetoothLeService.NeedSynTime = false;
					    pushMsgType = 1;
						mBluetoothLeService.connect(mDeviceAddress,getApplicationContext(),false);
						
					}
					
				
				
				break;
				
			default :
				
				break;
			
			}
		}
	};

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate().");
		super.onCreate();

		addSMSObserver();
		
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		CallListener1 customPhoneListener = new CallListener1(this);
		
		tm.listen(customPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
		
		
		
	}

	public void addSMSObserver() {
		Log.i(TAG, "add a SMS observer. ");
		resolver = getContentResolver();
		Handler handler = new SMSHandler(this);

		mObserver = new SMSObserver(resolver, mHandler);
		resolver.registerContentObserver(SMS.CONTENT_URI, true, mObserver);
		
		bindLeService();
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy().");
		this.getContentResolver().unregisterContentObserver(mObserver);
		super.onDestroy();

		
	}
	
	public void bindLeService() {

		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, this.BIND_AUTO_CREATE);
		registerReceiver(mGattUpdateReceiver, BluetoothLeService.makeGattUpdateIntentFilter());

	}
	
	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
		
			Log.i(TAG, "onServiceConnected()-->mBluetoothLeService="
					+ mBluetoothLeService);

		
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
			Log.i(TAG, "Service DISCONNECT...");
		}
	};
	
	private  BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (!Needresponse){
			    Log.d(TAG, "--Not Need Response");
				return;
			}
			
			final String action = intent.getAction();
			Logger.i(TAG, "BroadcastReceiver.action=" + action);
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				Log.d(TAG, "Connected......................");
				mConnected = true;

			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				Log.d(TAG, "DisConnected......................");
				mConnected = false;
			

			}

			else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				Log.d(TAG, "DISCOVERD......................");
				Log.d(TAG, "<<<==isNeedConnect:" + isNeedConnect);
				if (isNeedConnect)
				{
					isNeedConnect = false;
					retValue = 0;
					if (pushMsgType == 0) orderType = 6;
					else orderType = 5;
					Log.d(TAG, "<<<==orderType:" + orderType);
					sendOrderToDevice(orderType);
				}

			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				byte[] bytes = intent
						.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
				Logger.e(TAG, "获取到的数据2：" + NumberUtils.bytes2HexString(bytes));
				parseBytesArray(bytes);

			}
		}
	};
	
	public void sendOrderToDevice(int orderType) {
		if (mBluetoothLeService != null) {
			byte[] bytes = null;
			
			switch (orderType) {
			case 0x01: // 同步时间
				bytes = SynTimeToZefit();
				break;
				
			case 0x05:
				bytes= new byte[]{0x6E, 0x01, (byte)0xB1, (byte)smsCount, (byte)0x8F};
				break;
				
			case 0x06:
			 
				bytes= new byte[]{0x6E, 0x01, (byte)0xB2, (byte) gLastCallNo.charAt(0),(byte) gLastCallNo.charAt(1),
						(byte) gLastCallNo.charAt(2),(byte) gLastCallNo.charAt(3),(byte) gLastCallNo.charAt(4),(byte) gLastCallNo.charAt(5),
						(byte) gLastCallNo.charAt(6),(byte) gLastCallNo.charAt(7),(byte) gLastCallNo.charAt(8),(byte) gLastCallNo.charAt(9),
						(byte) gLastCallNo.charAt(10),0,0,0,0, (byte)0x8F};
				break;

			default:
				break;
			}

			mBluetoothLeService.sendDataToPedometer(bytes);
			Logger.w(TAG, "要发送的命令是=" + NumberUtils.bytes2HexString(bytes));
		}
	}
	
	private void parseBytesArray(byte[] bytes) {
		if (bytes.length == 6 && bytes[0] == 0x6e && bytes[2] == 0x01) { // 返回响应消息
			if ((retValue == 0) && (bytes[3] == 0x15)) {
				retValue = 1;
				orderType = 1;
				sendOrderToDevice(orderType);

			} else if ((bytes[4] == 0) && (bytes[3] == 0x15)) { // 时间同步成功
				
				//orderType = 4; //是否设置长连接
				orderType = 5; //推送来信
				//orderType = 6; //推送来电
				
				sendOrderToDevice(orderType);
			}  else if (bytes[3] == (byte)0xB1 && bytes[4] == 0x00) {
				Log.d(TAG, "<<<==设置来信推送成功");
				mHandler.sendEmptyMessage(0);
				
			} else if (bytes[3] == (byte)0xB1 && bytes[4] == 0x01) {
				Log.d(TAG, "<<<==设置来信推送失败");
				mHandler.sendEmptyMessage(1);
				
			}
			
		} else if (bytes.length == 6 && (bytes[4] == 1) && (bytes[3] == 0x34)) { // 时区设置成功
			mHandler.sendEmptyMessage(1);	
		}

	}
	
	private byte[] SynTimeToZefit() {

		Calendar calendar = Calendar.getInstance();
		int i = calendar.get(1);
		byte abyte0[] = new byte[11];
		abyte0[0] = 110;
		abyte0[1] = 1;
		abyte0[2] = 21;
		abyte0[3] = (byte) i;
		abyte0[4] = (byte) (i >> 8);
		abyte0[5] = (byte) (1 + calendar.get(2));
		abyte0[6] = (byte) calendar.get(5);
		abyte0[7] = (byte) calendar.get(11);
		abyte0[8] = (byte) calendar.get(12);
		abyte0[9] = (byte) calendar.get(13);
		abyte0[10] = -113;
		return abyte0;

	}
	
	private Uri SMS_INBOX = Uri.parse("content://sms/");
	
	public void getSmsFromPhone() {
		Log.d(TAG, "<<<==进入getSmsFromPhone");
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		long time = TimesrUtils.getTimesMorning(calendar)*1000L;
		
		if (resolver != null) {
			String[] projection = new String[] {"body", "_id", "address",
					"person", "date", "type","subject"};
			 String where = " type='1' and read='0' AND date > " + time;
			 	//+ (System.currentTimeMillis() - 20 * 60 * 1000);
			
			Cursor cur = resolver.query(SMS_INBOX, projection, where, null, "date desc");
			smsCount = cur.getCount();
			Log.d(TAG, "<<===cur count is:" + smsCount);
			if (null == cur)
				return;
			
			int i = 1;
			while (cur.moveToNext()) {
				String number = cur.getString(cur.getColumnIndex("address"));// 手机号
				String name = cur.getString(cur.getColumnIndex("person"));// 联系人姓名列表
				String body = cur.getString(cur.getColumnIndex("body"));
				String subject = cur.getString(cur.getColumnIndex("subject"));
				
				Log.d(TAG, "<<==" + i + " |number:" + number + "|name:" + name + "|body:" + body + "|subject:" + subject);
				i++;
			}
		}
	}


	
	
	
 class CallListener1 extends PhoneStateListener {
			private static final String TAG = "CallListener";
			private int lastetState = TelephonyManager.CALL_STATE_IDLE; // 最后的状态
			private Context context;
		    private String lastCallNo;
			public CallListener1(Context context) {
				super();
				this.context = context;
			}

			public void onCallStateChanged(int state, String incomingNumber) {
				Log.v(TAG, "<<==CallListener call state changed : " + incomingNumber);
				
				
				String m = null;
				TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				
				Log.d(TAG, "<<==state:" + state+"  "+lastCallNo);
				// 如果当前状态为空闲,上次状态为响铃中的话,则认为是未接来电
			/*	if (lastetState == TelephonyManager.CALL_STATE_RINGING
						&& state == TelephonyManager.CALL_STATE_IDLE) {*/
				if (state == TelephonyManager.CALL_STATE_RINGING)
						 {	
					if ((null != incomingNumber  ) && (incomingNumber.length()>2))
					{
						
					  boolean  is_callpush = (Boolean) ConfigHelper.getSharePref(this.context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.IS_CALL_PUSH, ConfigHelper.DATA_BOOLEAN);
					  boolean  is_smspush = (Boolean) ConfigHelper.getSharePref(this.context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.IS_SMS_PUSH, ConfigHelper.DATA_BOOLEAN);
					
					if (!is_callpush)
						{
						 Log.d(TAG, "call notification disable");
						  return;	
						}
					//	Toast.makeText(this.context, "incoming  call： " +incomingNumber, Toast.LENGTH_LONG).show();
						Log.d(TAG, "incoming call" + incomingNumber);
						gLastCallNo = incomingNumber;
						isNeedConnect = true;
						Needresponse = true;
						String mDeviceAddress = (String)ConfigHelper.getSharePref(this.context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.MAC_KEY, ConfigHelper.DATA_STRING);
					    mBluetoothLeService.NeedSynTime = false;
					    pushMsgType = 0;
						mBluetoothLeService.connect(mDeviceAddress,getApplicationContext(),false);
						
					}
					//Log.d(TAG, "<<==未接电话");
				
					//Log.d(TAG, "<<===a:" + lastCallNo);
					
					//procMissedCall(incomingNumber);
				}
				// 最后改变当前值
				lastetState = state;
				
		  
				switch (state) {
				case TelephonyManager.CALL_STATE_RINGING:
					lastCallNo = incomingNumber;
					Log.d(TAG, "<<==RINGING");
					break;
				case TelephonyManager.CALL_STATE_IDLE:
					Log.d(TAG, "<<==IDLE");
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					Log.d(TAG, "<<==OFFHOOK");
					break;
				}
			}

			private void procMissedCall(String incomingNumber) {
				// 未接来电处理(发短信,发email等)
			}
			
			private int readMissCall() {
		        int result = 0;
		        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[] {
		                Calls.TYPE
		            }, " type=? and new=?", new String[] {
		                    Calls.MISSED_TYPE + "", "1"
		            }, "date desc");

		        if (cursor != null) {
		            result = cursor.getCount();
		            Log.d(TAG, "<<==cursor count is " + result);
		            cursor.close();
		        }
		        return result;
		    }
		}
	
	
}
	
	
	





