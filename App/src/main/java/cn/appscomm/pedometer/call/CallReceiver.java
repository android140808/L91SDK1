package cn.appscomm.pedometer.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import apps.utils.Logger;

public class CallReceiver extends BroadcastReceiver {
	
	private static final String TAG = "CallReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Logger.i(TAG, "CallReceiver Start...");
/*		TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	CallListener customPhoneListener = new CallListener(context);
		
		telephony.listen(customPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
		
		Bundle bundle = intent.getExtras();
		String phoneNr = bundle.getString("incoming_number");
		Logger.i(TAG, "CallReceiver Phone Number : " + phoneNr);*/
	}
}