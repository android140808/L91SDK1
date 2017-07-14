//package cn.appscomm.pedometer.activity;
//
//
//
//import android.app.AlertDialog;
//import android.app.ProgressDialog;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.res.Configuration;
//import android.content.res.Resources;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.support.v4.app.Fragment;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.CompoundButton;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.ToggleButton;
//
//import java.io.File;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Properties;
//import java.util.Random;
//
//import apps.utils.ConfigHelper;
//import apps.utils.DialogUtil;
//import apps.utils.HttpUtil;
//import apps.utils.Logger;
//import apps.utils.PropertiesUtil;
//import apps.utils.PublicData;
//import cn.appscomm.pedometer.service.DBService;
//import cn.appscomm.pedometer.service.HttpResDataService;
//import cn.appscomm.pedometer.service.MyPushMsgL38iService;
//import cn.appscomm.pedometer.service.MyPushMsgService;
//import cn.appscomm.pedometer.service.UploadDataService;
//import cn.l11.appscomm.pedometer.activity.R;
//
///**
// * Created with Eclipse.
// * Author: Tim Liu  email:9925124@qq.com
// * Date: 14-3-2
// * Time: 21:09
// */
//public class TabSettingActivity extends Fragment
//{
//	private final static String TAG = "TabSettingActivity";
//
//	private View rootView ;
//	private Button btn_logout;
//
//	//顶部时间、电量
//	private TextView top_title_time, top_title_battery;
//	private RelativeLayout rl_top_hint;
//    private TextView tv_left_hint02,tv_right_hint02;
//    private ImageView iv_right_battery01,iv_right_battery02,iv_right_battery03,iv_right_battery04;
//    private ToggleButton btn_alwayson;
//    private TextView set_up_title;
//	private Intent mIntent;
//	private Bundle bundle;
//	private DBService dbService;
//	private ProgressDialog mProgressDialog = null;
//    private boolean isAwaysSyn = false;
//    private Handler mhander = null;
//    private HttpUtil mHttpUtil;
//
//    private final static int SET_SYNC_SW_OK = 0x7648;
//    private final static int SET_SYNC_SW_FAIL = 0x7649;
//
//    private final static int SET_SYNC_SW_TIMEOUT = 0x7650;
//
//    private final static int SET_SYNC_MAXTIME= 15000;
//
//    private int randomNum = 0;
//    private Random random;
//    private AlertDialog.Builder builder = null;
//    private boolean isAlreadyReturn = false;
//    private boolean isSkipSetSync = false;
//
//
//
//
//    public void showProgressDialog() {
//		if (getActivity() != null) {
//			String title = getString(R.string.app_name);
//			String message = getString(R.string.login_loading);
//			mProgressDialog = DialogUtil.comProDialog(getActivity(), title,
//					message);
//			mProgressDialog.setCanceledOnTouchOutside(false);
//			mProgressDialog.show();
//		}
//	}
//
//
//
//    public  void closeProgressDialog()
//    {
//        if (mProgressDialog != null) {
//           mProgressDialog.dismiss();
//        }
//
//
//    }
//
//	private final BroadcastReceiver mRecData = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//
//			final String action = intent.getAction();
//			Logger.i(TAG, "BroadcastReceiver.action=" + action);
//
//            try {
//                if (getActivity() ==null) return;
//                getActivity().runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        if (UploadDataService.UPLOAD_DATA_OK.equals(action)  || UploadDataService.UPLOAD_DATA_NODATA.equals(action)) {
//                            updateShowLastSyncInfo();
//
//                        }
//                    }
//                });
//            }
//            catch ( Exception e) {}
//		}
//
//	};
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//		if (rootView == null) {
//			rootView = inflater.inflate(R.layout.settings_view2, container,
//					false);
//			initView();
//
//		}
//
//		return rootView;
//	}
//
//    @Override
//	public void onActivityCreated(Bundle savedInstanceState) {
//    			dbService = new DBService(getActivity()) ;
//    			 getActivity().registerReceiver(mRecData,UploadDataService.makeGattUpdateIntentFilter());
//    			 super.onActivityCreated(savedInstanceState);
//    }
//
//
//
//    public void onDestroy() {
//
//    	getActivity().unregisterReceiver(mRecData);
//        super.onDestroy();
//    }
//
//
//	private void initView() {
//		TextView title=(TextView)rootView.findViewById(R.id.title);
//		title.setText(R.string.settings_title);
//
//        random = new Random();
//
//		top_title_time = (TextView) rootView.findViewById(R.id.top_title_time);
//		top_title_battery = (TextView) rootView.findViewById(R.id.top_title_battery);
//        btn_alwayson = (ToggleButton) rootView.findViewById(R.id.btn_alwayson);
//
//        btn_alwayson.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                 isAwaysSyn = b;
//
//               if ( !isSkipSetSync) {
//
//
//                 if (!b)
//                 {
//                     showProgressDialog();
//                     new Thread(SetSyncSWRunnable).start();
//
//
//                 }
//                else {
//                     AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                     builder.setMessage(R.string.battery_warning);
//
//                     builder.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
//                         @Override
//                         public void onClick(DialogInterface dialog, int which) {
//
//
//                             showProgressDialog();
//                             new Thread(SetSyncSWRunnable).start();
//
//                         }
//                     });
//                     builder.setNegativeButton(getString(android.R.string.no),new DialogInterface.OnClickListener(){
//
//                                 @Override
//                                 public void onClick(DialogInterface dialog, int which) {
//
//                                     isSkipSetSync = true;
//
//                                     btn_alwayson.setChecked(!btn_alwayson.isChecked());
//                                     isSkipSetSync = false;
//
//
//                                 }
//
//                             }
//                     );
//                     builder.show();
//                 }
//
//               }
//
//            }
//        });
//
//
//        isSkipSetSync = true;
//         btn_alwayson.setChecked((Boolean)ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME,
//                PublicData.ALWAYS_ON_KEY, ConfigHelper.DATA_BOOLEAN));
//        isSkipSetSync = false;
//
//		rl_top_hint = (RelativeLayout) rootView.findViewById(R.id.rl_top_hint);
//		tv_left_hint02 = (TextView) rootView.findViewById(R.id.tv_left_hint02);//显示最后一次同步时间
//		tv_right_hint02 = (TextView) rootView.findViewById(R.id.tv_right_hint02);//显示电量百分比
//	    iv_right_battery01 = (ImageView) rootView.findViewById(R.id.iv_right_battery01);//显示电量图标
//	    iv_right_battery02 = (ImageView) rootView.findViewById(R.id.iv_right_battery02);
//	    iv_right_battery03 = (ImageView) rootView.findViewById(R.id.iv_right_battery03);
//	    iv_right_battery04 = (ImageView) rootView.findViewById(R.id.iv_right_battery04);
//
//		/**
//		// 显示当前app的版本号：
//		TextView textview_app_ver = (TextView)rootView.findViewById(R.id.textview_app_ver);
//		String mAppVersion = "1.0";
//		try {
//			mAppVersion = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
//		} catch (NameNotFoundException e)
//		{
//
//			e.printStackTrace();
//		}
//		textview_app_ver.setText(getResources().getString(R.string.app_version) + mAppVersion);
//		*/
//
//		ClickListener mMyClickListener = new ClickListener();
//
//		// 基本设置
//		RelativeLayout layout_base_setting = (RelativeLayout) rootView.findViewById(R.id.layout_base_setting);
//		layout_base_setting.setOnClickListener(mMyClickListener);
//
//		// 帮助
//		RelativeLayout layout_help = (RelativeLayout) rootView.findViewById(R.id.layout_help);
//		layout_help.setOnClickListener(mMyClickListener);
//
//		// 设置设备
//		RelativeLayout layout_set_up_device = (RelativeLayout) rootView.findViewById(R.id.layout_set_up_device);
//		layout_set_up_device.setOnClickListener(mMyClickListener);
//
//
//		RelativeLayout layout_reset_device = (RelativeLayout) rootView.findViewById(R.id.layout_reset);
//		layout_reset_device.setOnClickListener(mMyClickListener);
//
//		/**
//		// 睡眠目标
//		RelativeLayout layout_sleep_goal = (RelativeLayout) rootView.findViewById(R.id.layout_sleep_goal);
//		layout_sleep_goal.setOnClickListener(mMyClickListener);
//		*/
//
//		RelativeLayout layout_advanced_setting = (RelativeLayout) rootView.findViewById(R.id.layout_advanced_setting);
//		layout_advanced_setting.setOnClickListener(mMyClickListener);
//
//		RelativeLayout layout_notification = (RelativeLayout) rootView.findViewById(R.id.layout_notification);
//		layout_notification.setOnClickListener(mMyClickListener);
//
//
//
//		// 关于此app
//		RelativeLayout layout_about_app = (RelativeLayout) rootView.findViewById(R.id.layout_about_app);
//		layout_about_app.setOnClickListener(mMyClickListener);
//
//
//		RelativeLayout layout_logout = (RelativeLayout) rootView.findViewById(R.id.layout_logout);
//		layout_logout.setOnClickListener(mMyClickListener);
//
//		btn_logout = (Button) rootView.findViewById(R.id.btn_logout);
//		btn_logout.setOnClickListener(mMyClickListener);
//		set_up_title = (TextView) rootView.findViewById(R.id.set_up_title);
//		PublicData.BindingPedometer = PublicData.isBindingPedometer(getActivity(), false);
//		if (PublicData.BindingPedometer) set_up_title.setText(getString(R.string.remove));
//		else  set_up_title.setText(getString(R.string.binddevice));
//
//
//        mHttpUtil = new HttpUtil(getActivity());
//
//
//        mhander = new Handler()
//        {
//            @Override
//            public void handleMessage(Message msg) {
//
//                switch ( msg.what) {
//                    case SET_SYNC_SW_OK:
//                        isAlreadyReturn = true;
//
//                        closeProgressDialog();
//                        builder = new AlertDialog.Builder(getActivity());
//                        builder.setTitle(R.string.tips);
//                        builder.setMessage(R.string.success);
//                        builder.setPositiveButton(getString(android.R.string.ok), null);
//                        builder.show();
//
//                        ConfigHelper.setSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME,
//                                PublicData.ALWAYS_ON_KEY,isAwaysSyn);
//                        break;
//
//                    case SET_SYNC_SW_TIMEOUT :
//                    case SET_SYNC_SW_FAIL :
//                        if ((msg.arg1 != randomNum) ) return;
//
//
//                        isAlreadyReturn = true;
//
//
//                        isSkipSetSync = true;
//                        btn_alwayson.setChecked(!btn_alwayson.isChecked());
//                        isSkipSetSync = false;
//
//
//                        closeProgressDialog();
//                        builder = new AlertDialog.Builder(getActivity());
//                        builder.setTitle(R.string.tips);
//                        builder.setMessage(R.string.setting_failed);
//                        builder.setPositiveButton(getString(android.R.string.ok), null);
//                        builder.show();
//
//                        break;
//
//                    default:
//                        break;
//                }
//
//
//            }
//
//
//        };
//
//
//	}
//
//	@Override
//	public void onPause() {
//		super.onPause();
//		Logger.d(TAG, "===onPause()");
//
////		if (mBatteryInfoReceiver != null) {
////			getActivity().getApplicationContext().unregisterReceiver(mBatteryInfoReceiver);
////		}
//
//	}
//
//	@Override
//	public void onResume() {
//		super.onResume();
//		Logger.d(TAG, "===onResume()");
//
//		updateShowLastSyncInfo();
//		PublicData.BindingPedometer = PublicData.isBindingPedometer(getActivity(), false);
//		if (PublicData.BindingPedometer) set_up_title.setText(getString(R.string.remove));
//		else  set_up_title.setText(getString(R.string.binddevice));
//
//		//getActivity().getApplicationContext().registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
//	}
//
//	public BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			String action = intent.getAction();
//			if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
//				int level = intent.getIntExtra("level", 0); //获得当前电量
//				int scale = intent.getIntExtra("scale", 100); //获得总电量
//
//				int percent = level * 100 /scale;
//				Logger.d(TAG, "===电量：" + percent + "%");
//				top_title_battery.setText(percent + "%");
//
//				SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm");
//				String date = sDateFormat.format(new Date());
//				top_title_time.setText(date);
//			}
//		}
//
//	};
//
//	class ClickListener implements View.OnClickListener{
//
//		@Override
//		public void onClick(View v) {
//
//			switch (v.getId()) {
//			case R.id.layout_base_setting:
////				mIntent = new Intent(getActivity(), BaseSettingActivity.class);
////				startActivity(mIntent);
//				mIntent = new Intent(getActivity(), MainActivity.class);
//
//				bundle = new Bundle();
//				bundle.putString("dateType", PublicData.SETTINGS_MY_PROFILE_ITEM_KEY);
//	            mIntent.putExtras(bundle);
//
//				getActivity().startActivity(mIntent);
//				getActivity().finish();
//				getActivity().overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
//
//				break;
//
//			case R.id.layout_help:
////				mIntent = new Intent(getActivity(), HelpActivity.class);
////				startActivity(mIntent);
//				mIntent = new Intent(getActivity(), MainActivity.class);
//
//				bundle = new Bundle();
//				bundle.putString("dateType", PublicData.SETTINGS_HELP_ITEM_KEY);
//	            mIntent.putExtras(bundle);
//
//				getActivity().startActivity(mIntent);
//				getActivity().finish();
//				getActivity().overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
//
//
//				break;
//
//			case R.id.layout_reset:
//				mIntent = new Intent(getActivity(), ReSetDeviceActivity.class);
//				startActivity(mIntent);
//
//				break;
//
//			case R.id.layout_set_up_device:
//				if (PublicData.selectDeviceName.equals(PublicData.L38I)){
//					mIntent = new Intent(getActivity(), SetUpDeviceL38iActivity.class);
//				}else {
//					mIntent = new Intent(getActivity(), SetUpDeviceActivity.class);
//				}
//
//				startActivity(mIntent);
//				break;
//
//			case R.id.layout_advanced_setting:
////				mIntent = new Intent(getActivity(), AdvancedActivity.class);
////				startActivity(mIntent);
//				mIntent = new Intent(getActivity(), MainActivity.class);
//
//				bundle = new Bundle();
//				bundle.putString("dateType", PublicData.SETTINGS_ADVANCED_SETTINGS_ITEM_KEY);
//	            mIntent.putExtras(bundle);
//
//				getActivity().startActivity(mIntent);
//				getActivity().finish();
//				getActivity().overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
//
//				break;
//
//			case R.id.layout_notification:
//
//				Logger.d(TAG, "notification clicked...");
//				mIntent = new Intent(getActivity(), MainActivity.class);
//
//				bundle = new Bundle();
//				bundle.putString("dateType", PublicData.SETTINGS_NOTIFI_ITEM_KEY);
//				//.
//	            mIntent.putExtras(bundle);
//
//				getActivity().startActivity(mIntent);
//				getActivity().finish();
//				getActivity().overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
//
//
//
//
//
//				break;
//
//
//
//			case R.id.layout_about_app:
////				mIntent = new Intent(getActivity(), AboutAppActivity.class);
////				startActivity(mIntent);
////				Toast.makeText(getActivity(), "about_app", Toast.LENGTH_SHORT).show();
//
//				mIntent = new Intent(getActivity(), MainActivity.class);
//
//				bundle = new Bundle();
//				bundle.putString("dateType", PublicData.SETTINGS_ABOUT_US_ITEM_KEY);
//	            mIntent.putExtras(bundle);
//
//				getActivity().startActivity(mIntent);
//				getActivity().finish();
//				getActivity().overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
//
//
//
//				break;
//
//			case R.id.layout_logout:
//			//
//				TextView tv1 = new TextView(getActivity() );
//
//                tv1.setText(R.string.DeterminedToExit);
//                tv1.setTextColor(Color.rgb(255, 255, 255));
//
//                tv1.setTextSize(22);
//              //  tv1.setTextColor(0x00);
//                tv1.setGravity(Gravity.CENTER);
//				AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
//		  		//builder.setTitle(R.string.reset_hint);
//               // builder.setTitle("        ");
//                builder.setView(tv1);
//		  		//builder.setMessage(R.string.DeterminedToExit);
//               // builder.setTitle(R.string.DeterminedToExit);
//		  		builder.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						// TODO Auto-generated method stub
//
//
//					}
//				});
//
//
//
//
//		  		builder.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
//		  			@Override
//		  			public void onClick(DialogInterface dialog, int which) {
//		  				getActivity().runOnUiThread(new Runnable() {
//
//							@Override
//							public void run() {
//								login_out();
//
//
//							}
//						});
//
//		  				//PublicData.upload_data_now = 1;
//		  			//
//
//
//
//
//		  			}
//		  		});
//
//		  		builder.show();
//
//				//if (dbService.getSportsDataCount()>0)
//			//	{
//
//				/*
//							AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
//					  		//builder.setTitle(R.string.reset_hint);
//					  		builder.setMessage(R.string.need_upload_data);
//					  		builder.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
//
//								@Override
//								public void onClick(DialogInterface dialog, int which) {
//									// TODO Auto-generated method stub
//
//
//								}
//							});
//
//
//
//
//					  		builder.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
//					  			@Override
//					  			public void onClick(DialogInterface dialog, int which) {
//					  				getActivity().runOnUiThread(new Runnable() {
//
//										@Override
//										public void run() {
//											login_out();
//
//
//										}
//									});
//
//					  				//PublicData.upload_data_now = 1;
//					  			//
//
//
//
//
//					  			}
//					  		});
//
//					  		builder.show();
//
//				}
//				else */
//
//				//	login_out();
//
//				break;
//
//			default:
//				break;
//			}
//
//		}
//
//
//
//	}
//
//	private void login_out() {
//	/*	ConfigHelper.setSharePref(getActivity(), PublicData.SHARED_PRE_SAVE_FILE_NAME,
//				PublicData.LOGIN_USERNAME_KEY, "");
//		ConfigHelper.setSharePref(getActivity(), PublicData.SHARED_PRE_SAVE_FILE_NAME,
//				PublicData.LOGIN_PASSWORD_KEY, "");*/
////		ConfigHelper.setSharePref(getActivity(), PublicData.SHARED_PRE_SAVE_FILE_NAME,
////				PublicData.ACCESS_TOKEN_KEY, "");
//
//		ConfigHelper.setSharePref(getActivity(),
//				PublicData.SHARED_PRE_SAVE_FILE_NAME,
//				PublicData.CUR_STEPS_TOTAL, 0);//退出当前账号,清除保存在本地的汇总数据
//		ConfigHelper.setSharePref(getActivity(),
//				PublicData.SHARED_PRE_SAVE_FILE_NAME,
//				PublicData.CUR_SPORTTIME_TOTAL, 0);//退出当前账号,清除保存在本地的汇总数据
//
//		ConfigHelper.setSharePref(getActivity(), PublicData.SHARED_PRE_SAVE_FILE_NAME,
//				PublicData.LOGOUT_KEY, 1);
//
//		ConfigHelper.setSharePref(getActivity(), PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.CUR_STEPS_TOTAL, 0);
//		ConfigHelper.setSharePref(getActivity(), PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.CUR_CALORIES_TOTAL, 0);
//		ConfigHelper.setSharePref(getActivity(), PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.CUR_DIS_TOTAL, 0);
//        ConfigHelper.setSharePref(getActivity(), PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.CUR_SPORTTIME_TOTAL, 0);
//
//        Intent intent = new Intent(getActivity(),MyPushMsgService.class);
//        getActivity().stopService(intent);
//        Intent intentL38i = new Intent(getActivity(),MyPushMsgL38iService.class);
//        getActivity().stopService(intentL38i);
//
//		File imgfile2 = new File(PublicData.SAVE_IMG_PATH,BaseSettingActivity.SAVED_FACE_IMG);
//
//		if (imgfile2.exists()) {
//
//			Logger.d("new-test", "img2 :" + imgfile2.getAbsolutePath() + "exist");
//			imgfile2.delete();
//			}
//
//
//
//		mIntent = new Intent(getActivity(), LoginActivity.class);
//		startActivity(mIntent);
//
//		System.exit(0);
//		// TODO Auto-generated method stub
//
//	}
//
//
//
//
//
//
//
//    Runnable SetSyncSWRunnable = new Runnable() {
//
//        @Override
//        public void run() {
//            Logger.d(TAG, "--SetSyncSWRunnableRunable---");
//
//
//            PropertiesUtil pu = new PropertiesUtil();
//            pu.initResRawPropFile(getActivity(), R.raw.server);
//
//            Properties props = pu.getPropsObj();
//            String url = props.getProperty("server.set_syncsw", "http://app.appscomm.cn/appscomm/api/sport-info/target/set");
//            //String url = "http://app.appscomm.cn/appscomm/api/sport-info/target/set";
//
//
//            String method = "post";
//
//            String uid = (String) ConfigHelper.getCommonSharePref(
//                    getActivity(),
//                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
//                    PublicData.CURRENT_USERID_ITEM_KEY, 1);
//
//            String watchId = ConfigHelper.GetBind_DN(getActivity());
//
//            //url = url + "/" + uid;
//            Logger.d(TAG, "请求地址：" + url);
//
//            String status = isAwaysSyn ? "1" : "0";
//            String params = "userId=" + uid + "&watchId=" + watchId + "&status=" + status;
//
//            Logger.d(TAG, "请求Params：" + params);
//
//            int respondStatus = mHttpUtil.httpReq(method, url, params);
//            String respondBody = mHttpUtil.httpResponseResult;
//
//
//            HttpResDataService httpResDataService = new HttpResDataService(
//                    getActivity().getApplicationContext());
//
//            int i = httpResDataService.commonParse(respondStatus, respondBody,
//                    "200"); // 6是get goal
//
//            Logger.i(TAG, "------------->>>:" + i);
//
//
//            Message msg = Message.obtain();
//
//            switch (i) {
//                case 0: // 成功
//
//                    msg.what = SET_SYNC_SW_OK;
//                    randomNum = random.nextInt();
//                    msg.arg1 = randomNum;
//                    mhander.sendMessage(msg);
//
//                    break;
//
//
//                default:
//
//                    msg.what = SET_SYNC_SW_FAIL;
//                    randomNum = random.nextInt();
//                    msg.arg1 = randomNum;
//                    mhander.sendMessage(msg);
//
//                    break;
//
//            }
//
//        }
//
//    };
//
//
//
//
//
//
//
//
//
//
//
//
//
//    /**
//	 * 更新最后同步相关信息
//	 */
//	public void  updateShowLastSyncInfo() {
//		Resources res = getResources();
//		Configuration conf = res.getConfiguration();
//
//		String language = conf.locale.getLanguage();
//		Logger.d(TAG, "--------language:" + language);
//
//		//获取本地保存的上次同步时间及电量
//		String lastDate = (String)ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME,
//				PublicData.TOP_HINT_LASTSYNCED_DATE_KEY, ConfigHelper.DATA_STRING);
//    	String lastTime = (String)ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME,
//				PublicData.TOP_HINT_LASTSYNCED_TIME_KEY, ConfigHelper.DATA_STRING);
//    	int battery = (Integer)ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME,
//				PublicData.TOP_HINT_BATTERY_KEY, ConfigHelper.DATA_INT);
//
//    	Logger.i(TAG, ">>1lastDate:" + lastDate);
//    	if ("".equals(lastDate)) {
//    		rl_top_hint.setVisibility(View.GONE);
//    	} else {
//    		rl_top_hint.setVisibility(View.VISIBLE);
//    		Calendar tmpCalendar = Calendar.getInstance();
//        	tmpCalendar.setTime(new Date());
//        	tmpCalendar.add(Calendar.DATE, -1);
//
//        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        	String today = sdf.format(new Date());
//        	String yesterday = sdf.format(tmpCalendar.getTime());
//        	Logger.i(TAG, ">>1today:" + today);
//        	Logger.i(TAG, ">>1yesterday:" + yesterday);
//
//        	String tmp = "";
//        	if (lastDate.equals(today)) {
//        		tmp = getString(R.string.today1) + " " + lastTime;
//        	} else if (lastDate.equals(yesterday)) {
//        		tmp = getString(R.string.Yesterday) + " " + lastTime;
//        	} else {
//        		String year = "";
//    			String month = "";
//    			String day = "";
//    			String str[] = lastDate.split("-");
//    			if (str.length == 3) {
//    				year = str[0];
//    				month = str[1];
//    				day = str[2];
//    			}
//    			if ("en".equals(language)) {
//    				tmp = month + "/" + day + "/" + year;
//
//        		} else if ("zh".equals(language)) {
//        			tmp = year + "/" + month + "/" + day;
//
//        		} else {
//        			tmp = day + "/" + month + "/" + year;
//
//        		}
//
//
//        		tmp = tmp + " " + lastTime;
//        	}
//        	tv_left_hint02.setText(tmp);
//    	}
//
//    	//更新上次同步时间及电池信息
//    	if (battery != -1) {
//    		tv_right_hint02.setText(battery + "%");
//    	}
//
//    	if (battery <= 20) {
//    		iv_right_battery01.setVisibility(View.VISIBLE);
//    		iv_right_battery02.setVisibility(View.GONE);
//    		iv_right_battery03.setVisibility(View.GONE);
//    		iv_right_battery04.setVisibility(View.GONE);
//    	} else if (battery > 20 && battery <= 50) {
//    		iv_right_battery01.setVisibility(View.GONE);
//    		iv_right_battery02.setVisibility(View.VISIBLE);
//    		iv_right_battery03.setVisibility(View.GONE);
//    		iv_right_battery04.setVisibility(View.GONE);
//    	} else if (battery > 50 && battery <= 75) {
//    		iv_right_battery01.setVisibility(View.GONE);
//    		iv_right_battery02.setVisibility(View.GONE);
//    		iv_right_battery03.setVisibility(View.VISIBLE);
//    		iv_right_battery04.setVisibility(View.GONE);
//    	} else if (battery > 75) {
//    		iv_right_battery01.setVisibility(View.GONE);
//    		iv_right_battery02.setVisibility(View.GONE);
//    		iv_right_battery03.setVisibility(View.GONE);
//    		iv_right_battery04.setVisibility(View.VISIBLE);
//    	}
//
//
//	}
//
//}
