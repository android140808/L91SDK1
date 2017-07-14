package cn.appscomm.pedometer.avater;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import apps.utils.Logger;
import apps.utils.NumberUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.appscomm.pedometer.activity.BloodActivity1;
import cn.appscomm.pedometer.protocol.AboutSetting.OpenBloodPress;
import cn.appscomm.pedometer.protocol.BluetoothUtil;
import cn.appscomm.pedometer.protocol.IResultCallback;
import cn.appscomm.pedometer.protocol.Leaf;
import cn.l11.appscomm.pedometer.activity.R;

/**
 * Created by Administrator on 2017/3/7.
 */

public class BloodFragment extends Fragment implements IResultCallback {
    public BloodFragment() {
    }

    private static final String TAG = "BloodFragment";

    @BindView(R.id.blood_listview)
    ListView bloodListview;
    Unbinder unbinder;
    List<BloodBeans> datas;
    @BindView(R.id.linechart)
    LineChart linechart;
    @BindView(R.id.nodatatext)
    TextView nodatatext;
    private InnerAdapter adapter;
    private BloodActivity1 activity;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    break;
            }
        }
    };

    private Context mContext;

    @SuppressLint("ValidFragment")
    public BloodFragment(Context context) {
        this.mContext = context;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.heartlistview, null);
        activity = (BloodActivity1) getActivity();
        unbinder = ButterKnife.bind(this, view);
        initData();
        return view;
    }

    private void initData() {
        linechart.setVisibility(View.GONE);
        adapter = new InnerAdapter();
        bloodListview.addHeaderView(View.inflate(getActivity(), R.layout.bloodt_item, null));
        bloodListview.setAdapter(adapter);
    }


    /**
     * 发送命令，测量血压
     */
    private void openBloodPress() {
        BluetoothUtil.getInstance().send(new OpenBloodPress(BloodFragment.this, 1, 1));
    }

    /**
     * 发送命令，停止测量血压
     */
    private void closeBloodPress() {
        BluetoothUtil.getInstance().send(new OpenBloodPress(BloodFragment.this, 1, 3));
    }


    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    private List<BloodResponsBody.DataBean.ValuesBean> valuesBeens = new ArrayList<>();

    public void addDate(BloodResponsBody.DataBean.ValuesBean valuesBean) {
        this.valuesBeens.add(0, valuesBean);
        Intent intent = new Intent("com.avater.blood");
        intent.putExtra("Hight", valuesBeens.get(0).getPressureMax());
        intent.putExtra("Lower", valuesBeens.get(0).getPressureMin());
        mContext.sendBroadcast(intent);
        adapter.notifyDataSetChanged();
    }

    public void setListData(List<BloodResponsBody.DataBean.ValuesBean> valueBeen) {
        try {
            Logger.w(TAG, "valueBeen == " + valueBeen);
            List<BloodResponsBody.DataBean.ValuesBean> cache = new ArrayList<>();
            for (int i = 0; i < valueBeen.size(); i++) {
                cache.add(i, valueBeen.get(i));
            }
            this.valuesBeens = cache;
            if (valuesBeens.size() > 0) {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String nowTime = sdf.format(calendar.getTime());
                Collections.reverse(valuesBeens);//
                String data_time = valuesBeens.get(0).getStartTime();
                if (data_time.contains(nowTime)) {
                    Intent intent = new Intent("com.avater.blood");
                    intent.putExtra("Hight", valuesBeens.get(0).getPressureMax());
                    intent.putExtra("Lower", valuesBeens.get(0).getPressureMin());
                    //TODO 保存第一个数据，需要在第一次进入主界面时，显示当前最新的数据
                    CacheUtils.putString(mContext, "B_Hight", valuesBeens.get(0).getPressureMax() + "");
                    CacheUtils.putString(mContext, "B_Lower", valuesBeens.get(0).getPressureMin() + "");
                    String startTime = valuesBeens.get(0).getStartTime();
                    Log.e("start_time", "血压缓存数据的开始时间 = " + startTime);
                    CacheUtils.putString(mContext, "blood_start_time", startTime);
                    mContext.sendBroadcast(intent);
                }
            }
            adapter.notifyDataSetChanged();
            if (valueBeen.size() == 0) {
                if (bloodListview != null) {
                    bloodListview.setVisibility(View.GONE);
                    nodatatext.setVisibility(View.VISIBLE);
                }
            } else {
                if (bloodListview != null) {
                    bloodListview.setVisibility(View.VISIBLE);
                    nodatatext.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVisible(int what) {
        if (what == 0) {
            bloodListview.setVisibility(View.GONE);
            nodatatext.setVisibility(View.VISIBLE);
        } else {
            bloodListview.setVisibility(View.VISIBLE);
            nodatatext.setVisibility(View.GONE);
        }
    }

    /**
     * 根据日期请求数据，无网络的情况下读取本地的数据
     *
     * @param calendar
     */
    public void getCloudHeartRateData(Calendar calendar) {
//        showMessageDialog("");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String curDay = sdf.format(calendar.getTime());
        String startTime = curDay + "%2000:00:00";
        String endTime = curDay + "%2023:59:59";
        if (judgeInternet()) {
            //TODO 联网更新
//            handler.sendEmptyMessageDelayed(1, 100);
        } else {
//            handler.sendEmptyMessageDelayed(1, 100);
        }
    }

    /**
     * 判断是否有网络
     * false:使用本地缓存的数据
     * true:联网获取数据
     *
     * @return
     */
    private boolean judgeInternet() {
        boolean isHavaNet = true;
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == connectivityManager) {
            isHavaNet = false;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (null == networkInfo) {
            isHavaNet = false;
        }
        return isHavaNet;
    }


    @Override
    public void onSuccess(Leaf leaf) {
        Log.e(TAG, "获取设备返回的结果集，需要开始解析命令码" + "NumberUtils.binaryToHexString ==" + NumberUtils.binaryToHexString(leaf.getBytes()));
    }

    @Override
    public void onFaild(Leaf leaf) {

    }

    class InnerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return valuesBeens.size();
        }

        @Override
        public Object getItem(int position) {
            return valuesBeens.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.bloodt_item_1, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            int i = (valuesBeens.get(position).getPressureMax()) - (valuesBeens.get(position).getPressureMin());
            String time = valuesBeens.get(position).getStartTime();
            Log.e("BloodFragment", "time == " + time);
            String times[] = time.split("\\.");
            Log.e("BloodFragment", "time[0] == " + times[0]);
            Log.e("BloodFragment", "time[1] == " + times[1]);
            int index = times[0].lastIndexOf("-") + 3;
            String cutTime = times[0].substring(index, times[0].length());
            Log.e("BloodFragment", "time == " + time);
            Log.e("BloodFragment", "time == " + time);
            holder.time.setText(cutTime);
            holder.hight.setText(valuesBeens.get(position).getPressureMax() + "");
            holder.lower.setText(valuesBeens.get(position).getPressureMin() + "");
            holder.value.setText(i + "");
            return convertView;
        }

        class ViewHolder {
            @BindView(R.id.time)
            TextView time;
            @BindView(R.id.hight)
            TextView hight;
            @BindView(R.id.lower)
            TextView lower;
            @BindView(R.id.value)
            TextView value;

            ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }

    /**
     * 在切换日期的时候，清空当前的数据
     */
    public void clearCacheDate() {
        if (valuesBeens != null && valuesBeens.size() > -1) {
            valuesBeens.clear();
            adapter.notifyDataSetChanged();
        }
    }

    private AlertDialog alertdialog;

    public void showMessageDialog(String msg) {
        View view = View.inflate(getActivity(), R.layout.dialog_layout, null);

        alertdialog = new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.avater_tips) + "")
                .setView(view)
                .create();
        //屏蔽返回键
        alertdialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return true;
            }
        });
        alertdialog.show();

    }

    public void hideAlertDiaLog() {
        if (alertdialog != null) {
            alertdialog.dismiss();
        }
    }
}
