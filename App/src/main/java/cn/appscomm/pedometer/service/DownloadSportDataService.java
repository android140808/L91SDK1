package cn.appscomm.pedometer.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import apps.utils.CommonUtil;
import apps.utils.Logger;
import apps.utils.PublicData;
import apps.utils.TimesrUtils;
import cn.appscomm.pedometer.model.SportDataCache;

public class DownloadSportDataService extends Service {

	public static final String TAG = "DownloadDataService";

	public static final String DOWNLOAD_DATA_OK = "cn.appscomm.pedometer.service.DOWNLOAD_SPORTDATA_OK";
	public static final String DOWNLOAD_DATA_FAIL = "cn.appscomm.pedometer.service.DOWNLOAD_SPORTDATA_FAIL";
	private String respondBody = "";
	private List<SportDataCache> mSportDataCacheList;

	private DBService dbService;

	private CloudDataService cloudDataService = null;
	private List pkList = new ArrayList<Integer>();
	private boolean isSportSave = false;
	private long SynDate_End = 0, SynDate_Begin = 0;
    private long serverCurDate =0;
	private boolean isNetConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			NetworkInfo[] infos = cm.getAllNetworkInfo();
			if (infos != null) {
				for (NetworkInfo ni : infos) {
					if (ni.isConnected()) {
						Logger.d(TAG, "NetWork is OK....");
						return true;
					}
				}
			}
		}
		return false;
	}

	private void broadcastUpdate(String s) {
		Intent intent = new Intent(s);

		sendBroadcast(intent);
	}

	
	
	
	
	
	
	
	
	
	
	public static IntentFilter makeGattUpdateIntentFilter() {
		IntentFilter intentfilter = new IntentFilter();
		intentfilter
				.addAction(DOWNLOAD_DATA_FAIL);
		intentfilter
				.addAction(DOWNLOAD_DATA_OK);
		
		return intentfilter;
	}
	   
	
	
	
	private void parseData(String respondBody) {

		try {
			// 正确的响应信息
			if (respondBody.indexOf("\"result\"") != -1
					&& respondBody.indexOf("\"message\"") != -1
					&& respondBody.indexOf("\"data\"") != -1) {

				JSONObject jsonObj = new JSONObject(respondBody);
				String result = jsonObj.getString("result");

				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

				mSportDataCacheList.clear();

				if ("0".equals(result)) {// 成功的结果码
					JSONArray jsonArray = (JSONArray) jsonObj
							.getJSONArray("data");
					Logger.d(TAG, ">>jsonArray Length:" + jsonArray.length());

					
					try
					{
						serverCurDate = jsonObj.getLong("servertime");
						serverCurDate = TimesrUtils.getUnixDate(serverCurDate);
					}
					catch ( Exception e )
					{
						serverCurDate = -1;
						
					}
					
					if (jsonArray != null && !"".equals(jsonArray)
							&& jsonArray.length() > 0) {
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject json = jsonArray.getJSONObject(i);

							int steps = json.getInt("steps");
							// int cal = json.getInt("cal")/1000;
							int cal = json.getInt("cal");

							float dis = (float) json.getDouble("dist");

							String mDatetime = json.getString("hours");
							String mDate = "";
							String mHours = "";
							long iDate = 0;
							int iHours = 0;
							if (mDatetime.length() == 10) {
								mHours = mDatetime.substring(8);

								mDate = mDatetime.substring(0, 8);
								Logger.d(TAG, "date :" + mDate + "  >>>hours:"
										+ mHours);

								try {

									iDate = (long) (sdf.parse(mDate).getTime() / 1000);
									iDate = TimesrUtils.getUnixDate(iDate);
									iHours = Integer.parseInt(mHours);
									SportDataCache mspData = new SportDataCache(
											iDate, iHours, steps, cal, dis);
									Logger.d(TAG, "new cache data : " + mspData);
									mSportDataCacheList.add(mspData);
								} catch (ParseException e) {
									Logger.d(TAG, "parse date error");

									// return;
									// TODO Auto-generated catch block

								}

							}
						}

					}

					try {

						SimpleDateFormat sdf1 = new SimpleDateFormat(
								"yyyy-MM-dd");

						String startTime = sdf1.format(new Date(
								SynDate_Begin * 24 * 3600 * 1000L)); // "2014-10-01 00:00:00";
						String endTime = sdf1.format(new Date(
								SynDate_End * 24 * 3600 * 1000L));

						dbService.upDateSportsCacheData(mSportDataCacheList,
								startTime, endTime);
						Logger.d(TAG, "update local date region :" + startTime
								+ "---" + endTime);

					}

					catch (Exception e) {
					}

				}

			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onStart(Intent intent, int startId) {
		Logger.d(TAG, "Download Service start");
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Logger.d(TAG, "Download Service startCommand");
		updateLocalData();
		return super.onStartCommand(intent, flags, startId);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {

			case 0:

				Logger.d(TAG, "Download service get data success" + '\n');
				respondBody = cloudDataService.respondBody;
				parseData(respondBody);
				CommonUtil.setSynSportDateToLocal(PublicData.appContext2,SynDate_Begin,SynDate_End,serverCurDate);
				broadcastUpdate(DOWNLOAD_DATA_OK);
				
				stopSelf();

				break;

			default:
				broadcastUpdate(DOWNLOAD_DATA_FAIL);
				break;

			}
		}
	};

	private  void updateLocalData() {

		if (!isNetConnected()) {

			broadcastUpdate(DOWNLOAD_DATA_FAIL);
			return;
		}

		if (cloudDataService == null) {
			cloudDataService = new CloudDataService(getApplicationContext(),
					mHandler);
		}

		long curMSec = 0;

		
		// new Date(System.get).getTime();
		
		
		TimeZone tz = TimeZone.getDefault();
		int offset = tz.getRawOffset();
		
		if (PublicData.synSport_date_begin<=1000)
		{
			
			long cusmis = System.currentTimeMillis() +offset ;
			SynDate_End = TimesrUtils.getUnixDate(cusmis/1000L);
			SynDate_Begin = SynDate_End - 31; // 上一个月
			
			
			
		}
		else {
			
			SynDate_End = PublicData.synSport_date_begin;
			SynDate_Begin = SynDate_End - 31; // 上一个月	
			
		}

	    Logger.d(TAG, "11 ..  begin_date" +PublicData.synSport_date_begin );
	
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		try {

			String startTime = sdf.format(new Date(
					SynDate_Begin * 24 * 3600 * 1000L)) + " 00:00:00"; // "2014-10-01 00:00:00";
			String endTime = sdf
					.format(new Date(SynDate_End * 24 * 3600 * 1000L))
					+ " 23:59:59";

			cloudDataService.setData();
			cloudDataService.getCloudSportData(startTime, endTime, "2");

			Logger.d(TAG, "download serivcie get date regon is " + startTime
					+ "  -- " + endTime);

		}

		catch (Exception e) {
		}
	}

	@Override
	public void onCreate() {
		Logger.i(TAG, "DownloadService onCreate().");
		super.onCreate();

		dbService = new DBService(getApplicationContext());


		if (cloudDataService == null) {
			cloudDataService = new CloudDataService(getApplicationContext(),
					mHandler);
		}
		  mSportDataCacheList = new ArrayList<SportDataCache>();
		
	}

	
	

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		Logger.i(TAG, "onDestroy().");

		super.onDestroy();

	}

}
