//package cn.appscomm.pedometer.avater;
//
//import android.app.AlertDialog;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattCallback;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.github.mikephil.charting.charts.LineChart;
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Collections;
//import java.util.Date;
//import java.util.List;
//
//import apps.utils.ConfigHelper;
//import apps.utils.Logger;
//import apps.utils.NumberUtils;
//import apps.utils.PublicData;
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.Unbinder;
//import cn.appscomm.pedometer.activity.BloodActivity;
//import cn.appscomm.pedometer.activity.LoginActivity;
//import cn.appscomm.pedometer.protocol.AboutSetting.OpenHeartRates;
//import cn.appscomm.pedometer.protocol.BluetoothUtil;
//import cn.appscomm.pedometer.protocol.IResultCallback;
//import cn.appscomm.pedometer.protocol.Leaf;
//import cn.appscomm.pedometer.service.BluetoothLeL38IService;
//import cn.appscomm.pedometer.service.BluetoothLeService;
//import cn.appscomm.pedometer.service.CloudDataService;
//import cn.l11.appscomm.pedometer.activity.R;
//
///**
// * Created by Administrator on 2017/3/7.
// */
//
//public class HeartFragment extends Fragment implements IResultCallback {
//    private static String TAG = "Avaters";
//
//    @BindView(R.id.blood_listview)
//    ListView heartListview;
//    Unbinder unbinder;
//    @BindView(R.id.linechart)
//    LineChart linechart;
//    private List<HeartRateBeans.DataBean> heartBeanses = new ArrayList<>();
//    private HeartAdapter adapter;
//    private CloudDataService cloudDataService;
//    private String respondBody = "";
//    private AlertDialog alertdialog;
//    private Calendar current_date;
//    private String personId = "";
//    private String deviceId = "";
//    private String deviceType = "";
//    private HeartRateResponBody heartRateResponBody = new HeartRateResponBody();
//    private View views;
//    private TextView mTextView;
//
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    private Calendar calendar;
//
//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = View.inflate(getActivity(), R.layout.heartlistview, null);
//        unbinder = ButterKnife.bind(this, view);
//        linechart.setVisibility(View.GONE);
//        BloodActivity activity = (BloodActivity) getActivity();
//        activity.setOnTextListener(new BloodActivity.TextListener() {
//            @Override
//            public void DoingSome(boolean what, int flag) {
//                mTextView.setText(getResources().getString(R.string.dialog_heart_bodys) + "");
//                showMessageDialog("");
//                Calendar instance = Calendar.getInstance();
//                instance.setTime(new Date());
//                calendar = instance;
//                openBloodMesure();
//                handler.postDelayed(new Runnable() {//15s后发送命令关闭测量
//                    @Override
//                    public void run() {
//                        closeBloodMesure();
//                        getMinandMaxBloodValues();
//                        //TODO 数据上传的操作，上传成功后，再清除数据
//                    }
//                }, 15000);
//            }
//        });
//        views = View.inflate(getActivity(), R.layout.dialog_layout, null);
//        alertdialog = new AlertDialog.Builder(getActivity())
//                .setTitle(getResources().getString(R.string.avater_tips) + "")
//                .setView(views)
//                .create();
//        mTextView = (TextView) views.findViewById(R.id.tv_data);
//
//        initData();
//        return view;
//    }
//
//    private void initData() {
//        cloudDataService = new CloudDataService(getActivity(), handler);
//        cloudDataService.setData();
//        getCloudHeartRateData(Calendar.getInstance());
//
//        //TODO  Mesuare Or Get Heart Rate !
//
//        personId = (String) ConfigHelper.getCommonSharePref(
//                getActivity(),
//                PublicData.SHARED_PRE_SAVE_FILE_NAME,
//                PublicData.CURRENT_USERID_ITEM_KEY, 1);
//        Log.e(TAG, "personId == " + personId);
//        deviceId = (String) ConfigHelper.getSharePref(getActivity(), PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.CURRENT_BIND_ID_ITEM, 1);
//        Log.e(TAG, "deviceId == " + deviceId);
//        deviceType = (String) getSharePref(PublicData.CURRENT_BIND_DEVICE_ITEM, ConfigHelper.DATA_STRING);
//        Log.e(TAG, "deviceType == " + deviceType);
//        heartRateResponBody.setPersonId(personId);
//        heartRateResponBody.setDeviceId(deviceId);
//        heartRateResponBody.setDeviceType(deviceType);
//        registerBrocasterReceiver();
//
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        unbinder.unbind();
//    }
//
//    @Override
//    public void onDestroy() {
//        getActivity().unregisterReceiver(mGattUpdateReceiver);
//        super.onDestroy();
//    }
//
//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            Log.d(TAG, "receicer messages");
//            switch (msg.what) {
//                case 0:
//                    hideMessageDialog();
//                    if (respondBody != "" && respondBody != null) {
//                        respondBody = "";
//                        heartBeanses.clear();
//                        adapter.notifyDataSetChanged();
//                    }
//                    respondBody = cloudDataService.respondBody;
//                    JSONObject jsonObject = JSON.parseObject(respondBody);
//                    String json_data = jsonObject.getString("data");
//                    Log.e("TAG", "data == " + json_data);
//                    //TODO 进行数据的缓存，确保在没有网络的情况下能访问到数据
////                    CacheUtils.putString(getActivity(),);
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//                    String curDay = sdf.format(current_date.getTime());
//                    String startTime = curDay + "%2000:00:00" + "avater";
//                    if (json_data.length() > 8) {
//                        CacheUtils.putString(getActivity(), startTime, json_data);
//                    }
//                    List<HeartRateBeans.DataBean> dataBeanList = new Gson().fromJson(json_data, new TypeToken<List<HeartRateBeans.DataBean>>() {
//                    }.getType());
//                    for (HeartRateBeans.DataBean dataBean : dataBeanList) {
//                        heartBeanses.add(dataBean);
//                    }
//                    adapter = new HeartAdapter(heartBeanses, getActivity());
//                    heartListview.setAdapter(adapter);
//                    adapter.notifyDataSetChanged();
//                    break;
//                case 1: //TODO 无网络的情况下，解析本地的数据
//                    hideMessageDialog();
//                    if (null != heartBeanses) {
//                        heartBeanses.clear();
//                        //TODO 暂时不刷新适配器
//                    }
//                    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
//                    String curDay1 = sdf1.format(current_date.getTime());
//                    String startTime1 = curDay1 + "%2000:00:00" + "avater";
//                    String json_no_net = CacheUtils.getString(getActivity(), startTime1);
//                    if (json_no_net == null || json_no_net.length() == 0) {
//                        return;
//                    }
//                    List<HeartRateBeans.DataBean> dataBeanList1 = new Gson().fromJson(json_no_net, new TypeToken<List<HeartRateBeans.DataBean>>() {
//                    }.getType());
//                    for (HeartRateBeans.DataBean dataBean : dataBeanList1) {
//                        heartBeanses.add(dataBean);
//                    }
//
//                    adapter = new HeartAdapter(heartBeanses, getActivity());
//                    heartListview.setAdapter(adapter);
//                    adapter.notifyDataSetChanged();
//                    break;
//            }
//        }
//    };
//
//    /**
//     * 获取某一天的心率数据
//     *
//     * @param calendar
//     */
//    public void getCloudHeartRateData(Calendar calendar) {
//        current_date = calendar;
//        showMessageDialog("");
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        String curDay = sdf.format(calendar.getTime());
//        String startTime = curDay + "%2000:00:00";
//        String endTime = curDay + "%2023:59:59";
//        if (judgeInternet()) {
//            cloudDataService.getCloudHeartRateData(startTime, endTime, "2");
//        } else {
//            handler.sendEmptyMessageDelayed(1, 1000);
//        }
//    }
//
//    public void showMessageDialog(String message) {
//        if (message.equals("1")) {
//            mTextView.setText(getResources().getString(R.string.avater_synnow) + "");
//        }
//        //屏蔽返回键
//        alertdialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                return true;
//            }
//        });
//        alertdialog.show();
//    }
//
//    private void hideMessageDialog() {
//        if (alertdialog != null) {
//            alertdialog.dismiss();
//        }
//    }
//
//    /**
//     * 判断是否有网络
//     * false:使用本地缓存的数据
//     * true:联网获取数据
//     *
//     * @return
//     */
//    private boolean judgeInternet() {
//        boolean isHavaNet = true;
//        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (null == connectivityManager) {
//            isHavaNet = false;
//        }
//        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//        if (null == networkInfo) {
//            isHavaNet = false;
//        }
//        return isHavaNet;
//    }
//
//    /**
//     * 注册广播，接收心率
//     */
//    private void registerBrocasterReceiver() {
//        getActivity().registerReceiver(mGattUpdateReceiver, BluetoothLeService.makeGattUpdateIntentFilter());
//    }
//
//
//    @Override
//    public void onSuccess(Leaf leaf) {
//        Log.e("TAG", "获取设备返回的结果集，需要开始解析命令码" + "NumberUtils.binaryToHexString ==" + NumberUtils.binaryToHexString(leaf.getBytes()));
//    }
//
//    @Override
//    public void onFaild(Leaf leaf) {
//
//    }
//
//    private List<Integer> blood_arrays = new ArrayList<>();
//
//    private void getMinandMaxBloodValues() {
//        Collections.sort(blood_arrays);
//        Log.e(TAG, "最小值==" + blood_arrays.get(0));
//        Log.e(TAG, "最大值==" + blood_arrays.get(blood_arrays.size() - 1));
////        detailsBeen
//        HeartRateResponBody.DetailsBean beans = new HeartRateResponBody.DetailsBean();
//
//        beans.setHeartMax(blood_arrays.get(blood_arrays.size() - 1) + "");
//        beans.setHeartMin(blood_arrays.get(0) + "");
//        beans.setTimeZone("");
//        beans.setEndTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
//        int sum = 0;
//        for (int i = 0; i < blood_arrays.size(); i++) {
//            sum = sum + blood_arrays.get(i);
//        }
//        int avg = sum / blood_arrays.size();
//        beans.setHeartAvg(avg + "");
//        ArrayList<HeartRateResponBody.DetailsBean> detailsBeen = new ArrayList<>();
//        detailsBeen.add(beans);
//        heartRateResponBody.setDetails(detailsBeen);
//
//        Gson gson = new Gson();
//        String json = heartRateResponBody.toString();
//        String json1 = gson.toJson(heartRateResponBody);
//
//        Log.e(TAG, "json == " + json);
//        Log.e(TAG, "json == " + json1);
//
//    }
//
//    /**
//     * 在这个广播里面，发送终止命令，然后将数据上传至服务器
//     */
//    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            mTextView.setText(getResources().getString(R.string.dialog_heart_ing) + "");
//            Log.d(TAG, "BroadcastReceiver.action=" + action);
//            if (BluetoothLeService.ACTION_HEART_DATA_AVAILABLE.equals(intent.getAction())) {
//                hideMessageDialog();
//                int heart_value = intent.getExtras().getInt(BluetoothLeService.EXTRA_DATA);
//                blood_arrays.add(heart_value);
//                Log.d(TAG, "--Not heart_value Response" + heart_value);
//            }
//        }
//    };
//
//    /**
//     * 获取sp中key对应的value
//     *
//     * @param key  键
//     * @param type value类型
//     * @return
//     */
//    private Object getSharePref(String key, int type) {
//        return ConfigHelper.getSharePref(getActivity(), PublicData.SHARED_PRE_SAVE_FILE_NAME, key, type);
//    }
//
//    /**
//     * 发送命令打开设备，进行测量血压
//     */
//    private void openBloodMesure() {
//        BluetoothUtil.getInstance().send(new OpenHeartRates(HeartFragment.this, 1, 02));
//    }
//
//    /**
//     * 发送命令，关闭血压测量
//     */
//    private void closeBloodMesure() {
//        BluetoothUtil.getInstance().send(new OpenHeartRates(HeartFragment.this, 1, 04));
//    }
//
//}
