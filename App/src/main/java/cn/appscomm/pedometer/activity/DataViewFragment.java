package cn.appscomm.pedometer.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import apps.utils.CommonUtil;
import apps.utils.ConfigHelper;
import apps.utils.Logger;
import apps.utils.NumberUtils;
import apps.utils.PublicData;
import apps.utils.TimesrUtils;
import cn.appscomm.pedometer.UI.AreaChartView;
import cn.appscomm.pedometer.UI.CircleSmallView;
import cn.appscomm.pedometer.UI.DataViewChart;
import cn.appscomm.pedometer.UI.HeartRateChartView;
import cn.appscomm.pedometer.UI.MySportView;
import cn.appscomm.pedometer.avater.CacheUtils;
import cn.appscomm.pedometer.model.HeartRateData;
import cn.appscomm.pedometer.model.ISetViewVal;
import cn.appscomm.pedometer.model.SleepTime;
import cn.appscomm.pedometer.service.DBService;
import cn.l11.appscomm.pedometer.activity.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interfaces
 * to handle interaction events.
 * Use the {@link DataViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DataViewFragment extends Fragment implements ISetViewVal {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DATETYPE = "datetype";
    private static final String ARG_KINDTYPE = "kindtype";
    private static final String TAG = "DATAVIEWFragment";
    private final static String TAGGE = "test_test" + "DataViewFragment";


    public static final int FRAGMENT_STEPID = 0x868;    //对应的5个ID ,用来查找对应的frament;
    public static final int FRAGMENT_CALOID = 0x869;
    public static final int FRAGMENT_DISID = 0x870;
    public static final int FRAGMENT_SLEEPID = 0x871;
    public static final int FRAGMENT_ACTIVITYID = 0x872;


    public int viewDateType = DataViewChart.DATEVIEW_WEEK;      //默认以周显示

    public int viewKindType = DataViewChart.VIEW_STEP;          //默认显示步数
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private BiMap<Integer, Integer> kindMaps = HashBiMap.create();
    private ImageView iv1, iv2, iv3, iv4, iv5, iv6;

    // public static Map<Integer,DataViewFragment> fragmentMap =  new HashMap<Integer,DataViewFragment>() ;

    private Context context;
    private int curIndex;

    private RelativeLayout ll_status;
    private LinearLayout ll_newui_bottom, ll_oldui_bottom, linearLayout1, linearLayout1_day;

    private DataViewChart stepView;
    private HeartRateChartView heartRateChartView;
    private float tmpData[];

    private CircleSmallView circleSmallView;

    private OnFragmentInteractionListener mListener;
    private LinearLayout layout_chartView, layout_dataview;

    private TextView tv_Value, tv_Descript, show_date;

    private String deviceType;

    private int mScreenHeight, heartRateChartHeight, bottomHeight;

    private android.os.Handler mHandler;

    private TextView typeTextView;
    private TextView goalTextView;
    private TextView unitTextView;
    private SeekBar seekBar;
    private TextView currentValueTextView;
    private TextView describeTextView;
    private LinearLayout linearLayout2;
    private int goal;
    private AreaChartView areaChartView;

    private ImageView iv_icon;

    public DataViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(TAGGE, "---------------onCreate");
        mHandler = new android.os.Handler() {
        };

        deviceType = (String) ConfigHelper.getSharePref(getActivity(),
                PublicData.SHARED_PRE_SAVE_FILE_NAME,
                PublicData.CURRENT_BIND_DEVICE_ITEM, ConfigHelper.DATA_STRING);

        initKindMaps();
//        args.putInt(ARG_DATETYPE, aviewDateType);
//        args.putInt(ARG_KINDTYPE, aViewKindType);
        //    viewDateType = getArguments().getInt(ARG_DATETYPE);
        viewDateType = DetailDataViewActivity.viewDateType;
        viewKindType = getArguments().getInt(ARG_KINDTYPE);

    }


    public static void emptyFragmentList() {

        Logger.i(TAGGE, "---------------emptyFragmentList");
//      if (fragmentMap!=null)  fragmentMap.clear();
//        fragmentMap = null;


    }

    public static DataViewFragment create(int position) {
        Logger.i(TAGGE, "---------------create");

        DataViewFragment fragment;

        Logger.d(TAG, "xxxxxxxxxxx Create Fragment !" + position);

        //    boolean isCreate = false;

//        if (null== fragmentMap)
//        {
//
//           fragmentMap = new HashMap<Integer,DataViewFragment>() ;
//        }
//
//
//        fragment = fragmentMap.get(position);


        //if (fragment==null)
        {
            // isCreate = true;
            fragment = new DataViewFragment();

        }

        if (fragment == null) {
            Logger.d(TAG, "xxxxxxxxxxx getFragment is Null!");

        }

        Bundle args = new Bundle();

        int aviewDateType = DataViewChart.DATEVIEW_WEEK;
        int aViewKindType = DataViewChart.VIEW_ACTIVITY;

        String deviceType = (String) ConfigHelper.getSharePref(PublicData.appContext2,
                PublicData.SHARED_PRE_SAVE_FILE_NAME,
                PublicData.CURRENT_BIND_DEVICE_ITEM, ConfigHelper.DATA_STRING);
        if (deviceType.equals(PublicData.L28H))
            switch (position) {
                case 0:
                    aViewKindType = DataViewChart.VIEW_STEP;
                    break;

                case 1:
                    aViewKindType = DataViewChart.VIEW_ACTIVITY;
                    break;

                case 2:
                    aViewKindType = DataViewChart.VIEW_DISTANCE;
                    break;

                case 3:
                    aViewKindType = DataViewChart.VIEW_CALORIES;
                    break;

                case 4:
                    aViewKindType = DataViewChart.VIEW_SLEEP;
                    break;

                default:
                    aViewKindType = DataViewChart.VIEW_STEP;
                    break;
            }
        else if (deviceType.equals(PublicData.L39)) //91
            switch (position) {
                case 0:
                    aViewKindType = HeartRateChartView.VIEW_HEARTRATE;
                    break;

                case 1:
                    aViewKindType = DataViewChart.VIEW_STEP;
                    break;

                case 5:
                    aViewKindType = DataViewChart.VIEW_ACTIVITY;
                    break;

                case 4:
                    aViewKindType = DataViewChart.VIEW_DISTANCE;
                    break;

                case 2:
                    aViewKindType = DataViewChart.VIEW_CALORIES;
                    break;

                case 3:
                    aViewKindType = DataViewChart.VIEW_SLEEP;
                    break;
            }
        else if (deviceType.equals(PublicData.L38I)) { //L91执行入口
            switch (position) {
                case 0:
                    aViewKindType = DataViewChart.VIEW_STEP;
                    break;

                case 4:
                    aViewKindType = DataViewChart.VIEW_ACTIVITY;
                    break;

                case 3:
                    aViewKindType = DataViewChart.VIEW_DISTANCE;
                    break;

                case 1:
                    aViewKindType = DataViewChart.VIEW_CALORIES;
                    break;

                case 2:
                    aViewKindType = DataViewChart.VIEW_SLEEP;
                    break;

                default:
                    aViewKindType = DataViewChart.VIEW_STEP;
                    break;
            }
        } else        // summer: add 非L28H无Activity Time
            switch (position) {
                case 0:
                    aViewKindType = DataViewChart.VIEW_STEP;
                    break;

                case 1:
                    aViewKindType = DataViewChart.VIEW_DISTANCE;
                    break;

                case 2:
                    aViewKindType = DataViewChart.VIEW_CALORIES;
                    break;

                case 3:
                    aViewKindType = DataViewChart.VIEW_SLEEP;
                    break;

                default:
                    aViewKindType = DataViewChart.VIEW_STEP;
                    break;
            }


        args.putInt(ARG_DATETYPE, aviewDateType);
        args.putInt(ARG_KINDTYPE, aViewKindType);

        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.i(TAGGE, "---------------onCreateView");

        Logger.d(TAG, "fragment On CreateView");

        View rootView = inflater.inflate(R.layout.fragment_data_view, container, false);

        findView(rootView);

        return rootView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {

        super.onHiddenChanged(hidden);
        Logger.i(TAGGE, "---------------onHiddenChanged");
        Logger.d(TAG, "hidden: change");
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        Logger.i(TAGGE, "---------------onButtonPressed");
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.i(TAGGE, "---------------onActivityCreated");

        // 获取状态栏高度
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            Logger.e(TAG, "get status bar height fail");
            e1.printStackTrace();
        }
        /*Rect frame = new Rect();      // 无效
        getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;*/
        // 计算心率图和底部图的高度
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenHeight = dm.heightPixels;
        bottomHeight = (mScreenHeight - sbar - dp2px(120)) * 6 / 17;
        heartRateChartHeight = mScreenHeight - sbar - bottomHeight - dp2px(60);
        Logger.i(TAG, "==>>dm.mScreenHeight = " + mScreenHeight);
        Logger.i(TAG, "==>>bottomHeight = " + bottomHeight);
        Logger.i(TAG, "==>>heartRateChartHeight = " + heartRateChartHeight);

        Logger.e(TAG, "==>>onActivityCreated kindMaps: " + kindMaps);
        Logger.e(TAG, "==>>onActivityCreated inverse: " + kindMaps.inverse());
        Logger.e(TAG, "==>>onActivityCreated viewKindType: " + viewKindType);
        Logger.e(TAG, "==>>onActivityCreated get(viewKindType): " + kindMaps.inverse().get(viewKindType));
        setCurPageNum(kindMaps.inverse().get(viewKindType));

        setViewType(viewKindType, viewDateType);
        Logger.d(TAG, "onActivity created");
    }


    private void initKindMaps() {
        Logger.i(TAGGE, "---------------initKindMaps");

        kindMaps.clear();
//        Logger.i(TAG, "==>>initKindMaps: deviceType: " + deviceType);
        if (deviceType.equals(PublicData.L28H)) {
            kindMaps.put(0, DataViewChart.VIEW_STEP);
            kindMaps.put(1, DataViewChart.VIEW_ACTIVITY);
            kindMaps.put(2, DataViewChart.VIEW_DISTANCE);
            kindMaps.put(3, DataViewChart.VIEW_CALORIES);
            kindMaps.put(4, DataViewChart.VIEW_SLEEP);
        } else if (deviceType.equals(PublicData.L39)) {
            kindMaps.put(0, HeartRateChartView.VIEW_HEARTRATE);
            kindMaps.put(1, DataViewChart.VIEW_STEP);
            kindMaps.put(2, DataViewChart.VIEW_ACTIVITY);
            kindMaps.put(3, DataViewChart.VIEW_DISTANCE);
            kindMaps.put(4, DataViewChart.VIEW_CALORIES);
            kindMaps.put(5, DataViewChart.VIEW_SLEEP);
        } else if (deviceType.equals(PublicData.L38I)) { //L91
            kindMaps.put(0, DataViewChart.VIEW_STEP);
            kindMaps.put(1, DataViewChart.VIEW_CALORIES);
            kindMaps.put(2, DataViewChart.VIEW_SLEEP);
            kindMaps.put(3, DataViewChart.VIEW_DISTANCE);
            kindMaps.put(4, DataViewChart.VIEW_ACTIVITY);
        } else {                        // summer: add 非L28H无Activity Time
            kindMaps.put(0, DataViewChart.VIEW_STEP);
            kindMaps.put(1, DataViewChart.VIEW_DISTANCE);
            kindMaps.put(2, DataViewChart.VIEW_CALORIES);
            kindMaps.put(3, DataViewChart.VIEW_SLEEP);
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Logger.i(TAGGE, "---------------onAttach");
        context = activity;
        Logger.d(TAG, "onActivity Attach");

//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {

        super.onDetach();
        Logger.i(TAGGE, "---------------onDetach");

        Logger.d(TAG, "onActivity DeAttach");
        mListener = null;
    }

    public float[] setViewActivity(float[] viewActivity) {
        float[] buy = new float[viewActivity.length];
        Logger.i(TAG, "buy.length+" + buy.length);

        for (int i = 0; i < buy.length; i++) {
            Logger.i(TAG, "viewActivity[i]+" + viewActivity[i]);
            Logger.i(TAG, "viewActivity[i]+" + viewActivity[i] / 60);
//            buy[i] = viewActivity[i] / 60;
            buy[i] = viewActivity[i];
        }
        Logger.i(TAG, "setViewActivity+" + Arrays.toString(buy));
        return buy;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private void setListener() {
        Logger.i(TAGGE, "---------------setListener");
        ll_status.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                }
                return true;
            }
        });
    }

    public void setTextViewDate(int viewDateType) {
        if (viewDateType == DataViewChart.DATEVIEW_DAY) {

        } else if (viewDateType == DataViewChart.DATEVIEW_WEEK) {
            String dataText = getResources().getString(R.string.week_activity);
            show_date.setText(dataText);
        } else if (viewDateType == DataViewChart.DATEVIEW_MONTH) {
            String dataText = getResources().getString(R.string.month_days);
            show_date.setText(dataText);
        }
    }

    private void findView(View rootView) {
        Logger.i(TAGGE, "---------------findView");

        layout_chartView = (LinearLayout) rootView.findViewById(R.id.ll_dataChart);

        layout_dataview = (LinearLayout) rootView.findViewById(R.id.circle_dataview);

        tv_Value = (TextView) rootView.findViewById(R.id.tv_Value);
        show_date = (TextView) rootView.findViewById(R.id.show_date);
        tv_Descript = (TextView) rootView.findViewById(R.id.tv_descript);

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int mScreenWidth = dm.widthPixels;
        if (mScreenWidth <= 480) {
            tv_Value.setTextSize(40);
            tv_Descript.setTextSize(16);
        }

        ll_status = (RelativeLayout) rootView.findViewById(R.id.ll_status);
        linearLayout1 = (LinearLayout) rootView.findViewById(R.id.linearLayout1);
        linearLayout1.setVisibility(View.VISIBLE);
        linearLayout1_day = (LinearLayout) rootView.findViewById(R.id.linearLayout1_day);
        linearLayout1_day.setVisibility(View.GONE);

        ll_oldui_bottom = (LinearLayout) rootView.findViewById(R.id.ll_oldui_bottom);
        ll_newui_bottom = (LinearLayout) rootView.findViewById(R.id.ll_newui_bottom);

        iv_icon = (ImageView) rootView.findViewById(R.id.iv_icon);

        iv1 = (ImageView) rootView.findViewById(R.id.dot_1);
        iv2 = (ImageView) rootView.findViewById(R.id.dot_2);
        iv3 = (ImageView) rootView.findViewById(R.id.dot_3);
        iv4 = (ImageView) rootView.findViewById(R.id.dot_4);
        iv5 = (ImageView) rootView.findViewById(R.id.dot_5);
        iv6 = (ImageView) rootView.findViewById(R.id.dot_6);

        typeTextView = (TextView) rootView.findViewById(R.id.typeTextView);
        goalTextView = (TextView) rootView.findViewById(R.id.goalTextView);
        unitTextView = (TextView) rootView.findViewById(R.id.unitTextView);
        seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        currentValueTextView = (TextView) rootView.findViewById(R.id.currentValueTextView);
        describeTextView = (TextView) rootView.findViewById(R.id.describeTextView);
        linearLayout2 = (LinearLayout) rootView.findViewById(R.id.linearLayout2);
    }

    private void myCreate() {
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.i(TAGGE, "---------------onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.i(TAGGE, "---------------onResume");
    }

    private void setCurPageNum(int num) {
        Logger.i(TAGGE, "---------------setCurPageNum");

        if (deviceType.equals(PublicData.L28H)) {     // summer: add 非L28H无Activity Time
            iv5.setVisibility(View.VISIBLE);
            iv6.setVisibility(View.GONE);
        } else if (deviceType.equals(PublicData.L39)) {
            iv5.setVisibility(View.VISIBLE);
            iv6.setVisibility(View.VISIBLE);
        } else if (deviceType.equals(PublicData.L38I)) {
            iv5.setVisibility(View.VISIBLE);
            iv6.setVisibility(View.GONE);
        } else {
            iv5.setVisibility(View.GONE);
            iv6.setVisibility(View.GONE);
        }
        iv1.setImageResource(R.drawable.dot_off);
        iv2.setImageResource(R.drawable.dot_off);
        iv3.setImageResource(R.drawable.dot_off);
        iv4.setImageResource(R.drawable.dot_off);
        iv5.setImageResource(R.drawable.dot_off);
        iv6.setImageResource(R.drawable.dot_off);

        switch (num) {
            case 0:
                iv1.setImageResource(R.drawable.dot_on);
                break;
            case 1:
                iv2.setImageResource(R.drawable.dot_on);
                break;
            case 2:
                iv3.setImageResource(R.drawable.dot_on);
                break;
            case 3:
                iv4.setImageResource(R.drawable.dot_on);
                break;
            case 4:
                iv5.setImageResource(R.drawable.dot_on);
                break;
            case 5:
                iv6.setImageResource(R.drawable.dot_on);
                break;
        }
    }


    public void setCurIndex(int curIndex) {
        Logger.i(TAGGE, "---------------setCurIndex");
        this.curIndex = curIndex;
    }

    @Override
    public void setCurVal(float curVal) {
        Logger.i(TAGGE, "---------------setCurVal");
        if (context == null) {
            Logger.e(TAG, "===>>setCurVal(): context = null");
            return;
        }
        if (!this.isAdded())
            return;
        Logger.i(TAG, "===>>setCurVal(): curVal = " + curVal);
        float curVal1 = curVal;

//        String unit = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.UNIT_KEY, ConfigHelper
//                .DATA_STRING);
        String unit = CacheUtils.getString(getActivity(), "unit_value");
        Logger.e(TAG, "==>>unit: " + unit);
        if (unit == null || unit.equals("")) unit = "1";
        if (viewKindType == DataViewChart.VIEW_DISTANCE) {
            float dis = unit.equals("1") ? (float) CommonUtil.KM2Mile(curVal / 1000L) : curVal / 1000L;
            Logger.e(TAG, "++dis=" + dis);
            curVal1 = dis;
            //tv_Value.setText(String.format(Locale.ENGLISH, "%.2f", dis)); //进位方式
            tv_Value.setText(NumberUtils.getFormatData(String.valueOf(dis)));//小数位载取方式

            if ("zh".equals(PublicData.currentLang))
                tv_Descript.setText(getString((unit.equals("1")) ? R.string.feetmiles : R.string.km));
            else
                tv_Descript.setText(getString((unit.equals("1")) ? R.string.feetmiles : R.string.km) + " TRAVELED");
            currentValueTextView.setText(NumberUtils.getFormatData(String.valueOf(dis)));
            seekBar.setProgress((int) (dis * 100));
        } else if (viewKindType == DataViewChart.VIEW_ACTIVITY) {
            Logger.i(TAG, "运动时长111...." + curVal);
//            curVal1 = curVal / 60;
//                curVal1 = curVal / 60;
            tv_Value.setText(String.format(Locale.ENGLISH, "%d", (int) curVal1));
            currentValueTextView.setText(String.format(Locale.ENGLISH, "%d", (int) curVal1));
            seekBar.setProgress((int) curVal1);
        } else if (viewKindType == DataViewChart.VIEW_SLEEP) {

            // summer: change "%.0f", curVal1/60) to "%d", ((int) curVal1) / 60)
            String hour = String.format(Locale.ENGLISH, "%d", ((int) curVal1) / 60) + getString(R.string.h);
            String min = String.format(Locale.ENGLISH, "%.0f", curVal1 % 60) + getString(R.string.m);

//            if (hour.length() >6) tv_Value.setTextSize(40);
//            else tv_Value.setTextSize(50);
//            tv_Value.setText(hour);
            String time = hour + " " + min;
            Spannable spannable = new SpannableString(time);
            AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(25, true), absoluteSizeSpan2 = new AbsoluteSizeSpan(25, true);
            spannable.setSpan(absoluteSizeSpan, hour.length() - 2, hour.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannable.setSpan(absoluteSizeSpan2, time.length() - 3, time.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            tv_Value.setText(spannable);
            currentValueTextView.setText(hour + " " + min);
//            currentValueTextView.setText(curVal1 / 60 + "");
            seekBar.setProgress((int) curVal1);
        } else {
            tv_Value.setText(String.format(Locale.ENGLISH, "%.00f", curVal));
            currentValueTextView.setText(String.format(Locale.ENGLISH, "%.00f", curVal));
            seekBar.setProgress((int) curVal);
        }
        Logger.e("setCurval", "setCurval++setCurval+curVal1" + curVal1);
        circleSmallView.setCurval(curVal1);
        Logger.e(TAG, "PublicData.curShowCal=" + new SimpleDateFormat("MM/dd").format(PublicData.curShowCal.getTime()));
        showCur();
    }

    private void showVal() {
        Logger.i(TAGGE, "---------------showVal");

        int cur_steps_total = (int) ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData
                .CUR_STEPS_TOTAL, ConfigHelper.DATA_INT);
        int cur_calories_total = (int) ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData
                .CUR_CALORIES_TOTAL, ConfigHelper.DATA_INT);
        float cur_dist_total = (float) ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData
                .CUR_DIS_TOTAL, ConfigHelper.DATA_FLOAT);
        int cur_sporttime_total = (int) ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData
                .CUR_SPORTTIME_TOTAL, ConfigHelper.DATA_INT);

        int step = (int) ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.TEMP_DAYSTEP,
                ConfigHelper.DATA_INT);
        int cal = (int) ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.TEMP_DAYCAL,
                ConfigHelper.DATA_INT);
        float dist = (float) ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.TEMP_DAYDIS,
                ConfigHelper.DATA_FLOAT);
        int mins = (int) ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.TEMP_DAYMINS,
                ConfigHelper.DATA_INT);
        float sleep = (float) ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.TEMP_DAYSLEEP,
                ConfigHelper.DATA_FLOAT);
        Logger.e(TAG, ">>>>>>>>>>>>>>>viewKindType:" + "step" + step + "cal" + cal + "dist" + dist + "mins" + mins + "sleep" + sleep);
        if (!PublicData.isUserCurData) { // 如果以服务器为主，则使用服务器的数据
            Logger.i(TAG, "以服务器为主...");
            cur_steps_total = step;
            cur_calories_total = cal;
            cur_dist_total = dist;
            cur_sporttime_total = mins;
        }
        if (deviceType.equals(PublicData.L38I)) {
            cur_steps_total = step;
            cur_calories_total = cal;
            cur_dist_total = dist;
            cur_sporttime_total = mins;
        }


        Logger.e(TAG, ">>>>>>>>>>>>>>>viewKindType:" + viewKindType);
        if (viewKindType == DataViewChart.VIEW_STEP) {
            Logger.e("setCurval", "setCurval++setCurval++cur_steps_total" + cur_steps_total);
            tv_Value.setText("" + cur_steps_total);
            circleSmallView.setCurval(cur_steps_total);

            currentValueTextView.setText("" + cur_steps_total);
            seekBar.setProgress(cur_steps_total);
        } else if (viewKindType == DataViewChart.VIEW_DISTANCE) {
//            String unit = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.UNIT_KEY, ConfigHelper
//                    .DATA_STRING);
            String unit = CacheUtils.getString(getActivity(), "unit_value");
            Logger.e(TAG, "==>>unit: " + unit);
            if (unit == null || unit.equals("")) unit = "1";
            float dis = unit.equals("1") ? (float) CommonUtil.KM2Mile(cur_dist_total / 1000L) : cur_dist_total / 1000L;
            String str = NumberUtils.getFormatData(String.valueOf(dis));
            tv_Value.setText(str);
            //circleSmallView.setCurval(cur_dist_total/1000L);//km
            circleSmallView.setCurval(dis);//根据单位转换后的值

            currentValueTextView.setText("" + str);
            seekBar.setProgress((int) (dis * 100));
        } else if (viewKindType == DataViewChart.VIEW_CALORIES) {
            tv_Value.setText("" + cur_calories_total);
            circleSmallView.setCurval(cur_calories_total);

            currentValueTextView.setText("" + cur_calories_total);
            seekBar.setProgress(cur_calories_total);
        } else if (viewKindType == DataViewChart.VIEW_ACTIVITY) {
            if (deviceType.equals(PublicData.L38I)) {
//                cur_sporttime_total = cur_sporttime_total / 60;
            }
            tv_Value.setText("" + cur_sporttime_total);
            circleSmallView.setCurval(cur_sporttime_total);

            currentValueTextView.setText("" + cur_sporttime_total);
            seekBar.setProgress(cur_sporttime_total);
        } else if (viewKindType == DataViewChart.VIEW_SLEEP) {
            int s = (int) (sleep * 60);
            String hour = String.format(Locale.ENGLISH, "%d", (int) (sleep * 60) / 60) + "hr";
            String min = String.format(Locale.ENGLISH, "%.0f", (sleep * 60) % 60) + "min";

//            if (hour.length() >6) tv_Value.setTextSize(40);
//            else tv_Value.setTextSize(50);
//            tv_Value.setText(hour);
            String time = hour + " " + min;
            Spannable spannable = new SpannableString(time);
            AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(25, true), absoluteSizeSpan2 = new AbsoluteSizeSpan(25, true);
            spannable.setSpan(absoluteSizeSpan, hour.length() - 1, hour.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannable.setSpan(absoluteSizeSpan2, time.length() - 1, time.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            tv_Value.setText(spannable);
            circleSmallView.setCurval(sleep * 60);

            currentValueTextView.setText(hour + " " + min);
            seekBar.setProgress((int) (sleep * 60));
        } else {
            Logger.e(TAG, "+++++++++++++++++++dd/heartRate=" + heartRate);

            tv_Value.setText("" + heartRate);
            circleSmallView.setCurval(heartRate);
        }
    }

    int heartRate = 0;

    private void showCur() {
        Logger.i(TAGGE, "---------------showCur");

        Logger.d(TAG, ">>>>>>>>>>>>>>index:" + this.curIndex);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");

        String curDate = sdf.format(PublicData.curShowCal.getTime());
        if (curDate.equals(sdf.format(new Date()))) {
            Logger.e(TAG, "ppppppppppppppppppp");

            switch (PublicData.curShowCal.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.MONDAY:
                    Logger.e(TAG, "Monday");
                    if (this.curIndex == 1) {
                        if (tmpData != null && tmpData.length >= 7) {
                            heartRate = (int) tmpData[1];
                        }
                        showVal();
                    }
                    break;
                case Calendar.TUESDAY:
                    Logger.e(TAG, "Tuesday");
                    if (this.curIndex == 2) {
                        if (tmpData != null && tmpData.length >= 7) {
                            heartRate = (int) tmpData[2];
                        }
                        showVal();
                    }
                    break;
                case Calendar.WEDNESDAY:
                    Logger.e(TAG, "Wednesday");
                    if (this.curIndex == 3) {
                        if (tmpData != null && tmpData.length >= 1) {
                            Logger.i(TAG, "length=====" + tmpData.length + "/[3]" + tmpData[3] + "/[2]=" + tmpData[2]);
                            heartRate = (int) tmpData[3];
                        }
                        showVal();
                    }
                    break;
                case Calendar.THURSDAY:
                    Logger.e(TAG, "Thursday");
                    if (this.curIndex == 4) {
                        if (tmpData != null && tmpData.length >= 1) {
                            heartRate = (int) tmpData[4];
                        }
                        showVal();
                    }
                    break;
                case Calendar.FRIDAY:
                    Logger.e(TAG, "Friday");
                    if (this.curIndex == 5) {
                        if (tmpData != null && tmpData.length >= 1) {
                            heartRate = (int) tmpData[5];
                        }
                        showVal();
                    }
                    break;
                case Calendar.SATURDAY:
                    Logger.e(TAG, "Saturday");
                    if (this.curIndex == 6) {
                        if (tmpData != null && tmpData.length >= 1) {
                            heartRate = (int) tmpData[6];
                        }
                        showVal();
                    }
                    break;
                case Calendar.SUNDAY:
                    Logger.e(TAG, "Sunday");
                    if (this.curIndex == 0) {
                        if (tmpData != null && tmpData.length >= 1) {
                            heartRate = (int) tmpData[0];
                        }
                        showVal();
                    }
                    break;
            }

        } else {
            Logger.e(TAG, "ttttttttttttttttttt");
        }


    }

    @Override
    public void setSleepRange(String begin, String end) {
        Logger.i(TAGGE, "---------------setSleepRange");

        ((ISetViewVal) getActivity()).setSleepRange(begin, end);
    }

    @Override
    public void setTimeDisplay(String s) {
        Logger.i(TAGGE, "---------------setTimeDisplay");

    }

    private void useNewUIBottom(boolean flag) {
        Logger.i(TAGGE, "---------------useNewUIBottom");


        if (DataViewChart.VIEW_HEART == viewKindType) {
            linearLayout1.setVisibility(View.GONE);
            linearLayout1_day.setVisibility(View.GONE);
            ll_newui_bottom.setVisibility(View.GONE);
            ll_oldui_bottom.setVisibility(View.VISIBLE);

        } else {
            linearLayout1.setVisibility(flag ? View.VISIBLE : View.GONE);
            linearLayout1_day.setVisibility(flag ? View.GONE : View.VISIBLE);
            ll_newui_bottom.setVisibility(View.VISIBLE);
            ll_oldui_bottom.setVisibility(View.GONE);
        }
//        linearLayout1.setVisibility(View.GONE);
//        linearLayout1_day.setVisibility(View.VISIBLE);
    }

    /**
     * 设置显示类型和数值
     *
     * @param viewType
     * @param viewTimeType
     */
    public void setViewType(int viewType, int viewTimeType) {
        Logger.i(TAGGE, "---------------setViewType");

        //从Activity 中传入 fragment 2个类型参数

        Logger.d(TAG, "set ViewType:viewType,viewTimeType" + viewType + "," + viewTimeType);

        viewDateType = viewTimeType;
        viewKindType = viewType;

        if (circleSmallView == null) {
            circleSmallView = new CircleSmallView(getActivity(), 0, 1000, viewType);
            layout_dataview.addView(circleSmallView);

        } else {
            circleSmallView.setViewType(viewType);

            circleSmallView.invalidate();

        }
        int mGoal = 0;
        switch (viewType) {
            case DataViewChart.VIEW_STEP:

                setTextViewDate(viewDateType);
//                ll_status.setBackgroundColor(DataViewChart.DRAW_STEP_COLOR);
                useNewUIBottom(true);

                tv_Descript.setText(R.string.step_desc);

                linearLayout2.setVisibility(View.VISIBLE);
                typeTextView.setText(R.string.steps1);
                unitTextView.setText(R.string.steps);
                describeTextView.setText(R.string.steps_taken);
                Logger.i(TAG, "iv_icon+VIEW_STEP");
                iv_icon.setImageResource(R.drawable.steps);
//                seekBar.setThumb(getResources().getDrawable(R.drawable.steps));
                seekBar.setProgressDrawable(getResources().getDrawable(R.drawable.steps_bar_layer));
                mGoal = (Integer) ConfigHelper.getSharePref(getActivity(), PublicData.SHARED_PRE_SAVE_FILE_NAME,
                        PublicData.TOTAL_TARGET_STEPS_KEY, ConfigHelper.DATA_INT);
                if (mGoal <= 0) {
                    mGoal = 7000;
                }
                Logger.i(TAG, "mGoal" + mGoal);
                goal = mGoal;
                seekBar.setMax(goal);
                break;

            case DataViewChart.VIEW_DISTANCE:
                setTextViewDate(viewDateType);
//                ll_status.setBackgroundColor(DataViewChart.DRAW_DISTANCE_COLOR);
                useNewUIBottom(true);

                String unit = (String) ConfigHelper.getSharePref(getActivity(), PublicData.SHARED_PRE_SAVE_FILE_NAME, "heightunit", ConfigHelper
                        .DATA_STRING);

                if (unit == null) unit = "0";

                if (PublicData.currentLang != null)
                    if (PublicData.currentLang.equals("zh"))
                        tv_Descript.setText(unit.equals("1") ? getString(R.string.feetmiles) : getString(R.string.km));
                    else
                        tv_Descript.setText(unit.equals("1") ? getString(R.string.feetmiles) : getString(R.string.km) + " TRAVELED");

                linearLayout2.setVisibility(View.VISIBLE);
                typeTextView.setText(R.string.distance);
                boolean isCent = false;
//                isCent = (boolean)ConfigHelper.getSharePref(getActivity(), PublicData.SHARED_PRE_SAVE_FILE_NAME,PublicData.IS_CENT_KILOMETERS_KEY,ConfigHelper.DATA_BOOLEAN);
//                String a = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.UNIT_KEY, ConfigHelper.DATA_STRING);
                String a = CacheUtils.getString(getActivity(), "unit_value");
                if (a.contains("1")) {
                    isCent = false;
                } else {
                    isCent = true;
                }
                if (isCent) {
                    unitTextView.setText(R.string.kms);
                    describeTextView.setText(R.string.kms);
                } else {
                    unitTextView.setText(R.string.miles1);
                    describeTextView.setText(R.string.miles2);
                }
                Logger.i(TAG, "iv_icon+VIEW_DISTANCE");
                iv_icon.setImageResource(R.drawable.distance);
//                seekBar.setThumb(getResources().getDrawable(R.drawable.distance));
                seekBar.setProgressDrawable(getResources().getDrawable(R.drawable.distance_bar_layer));
                mGoal = (Integer) ConfigHelper.getSharePref(getActivity(), PublicData.SHARED_PRE_SAVE_FILE_NAME,
                        PublicData.TOTAL_TARGET_DISTANCE_KEY, ConfigHelper.DATA_INT);
                if (mGoal <= 0) {
                    mGoal = 5;
                }
                Logger.i(TAG, "mGoal" + mGoal);
                goal = mGoal;
                seekBar.setMax(goal * 100);
                break;

            case DataViewChart.VIEW_CALORIES:
                setTextViewDate(viewDateType);
//                ll_status.setBackgroundColor(DataViewChart.DRAW_CALORIES_COLOR);
                useNewUIBottom(true);
                tv_Descript.setText(R.string.calories_desc);

                linearLayout2.setVisibility(View.VISIBLE);
                typeTextView.setText(R.string.calories);
                unitTextView.setText(R.string.calories2);
                describeTextView.setText(R.string.calories_burned);
                Logger.i(TAG, "iv_icon+VIEW_CALORIES");

                iv_icon.setImageResource(R.drawable.caloeies);
//                seekBar.setThumb(getResources().getDrawable(R.drawable.caloeies));
                seekBar.setProgressDrawable(getResources().getDrawable(R.drawable.calories_bar_layer));
                mGoal = (Integer) ConfigHelper.getSharePref(getActivity(), PublicData.SHARED_PRE_SAVE_FILE_NAME,
                        PublicData.TOTAL_TARGET_CALORIES_KEY, ConfigHelper.DATA_INT);
                if (mGoal <= 0) {
                    mGoal = 350;
                }
                Logger.i(TAG, "mGoal" + mGoal);
                goal = mGoal;
                seekBar.setMax(goal);
                break;


            case DataViewChart.VIEW_SLEEP:
                setTextViewDate(viewDateType);
//                ll_status.setBackgroundColor(DataViewChart.DRAW_SLEEP_COLOR);
                useNewUIBottom(viewDateType == DataViewChart.DATEVIEW_DAY ? false : true);
                tv_Descript.setTextColor(getResources().getColor(R.color.red));
                tv_Descript.setText("0" + getString(R.string.sleep_desc));

                linearLayout2.setVisibility(View.VISIBLE);
                typeTextView.setText(R.string.sleep);
                unitTextView.setText(R.string.hours);
                describeTextView.setText(R.string.slept_time);
                Logger.i(TAG, "iv_icon+VIEW_SLEEP");

                iv_icon.setImageResource(R.drawable.sleep);
//                seekBar.setThumb(getResources().getDrawable(R.drawable.sleep));
                seekBar.setProgressDrawable(getResources().getDrawable(R.drawable.sleep_bar_layer));
                mGoal = (Integer) ConfigHelper.getSharePref(getActivity(), PublicData.SHARED_PRE_SAVE_FILE_NAME,
                        PublicData.TOTAL_TARGET_SLEEP_KEY, ConfigHelper.DATA_INT);
                if (mGoal <= 0) {
                    mGoal = 8;
                }
                Logger.i(TAG, "mGoal=" + mGoal);
                goal = mGoal;
                seekBar.setMax(goal * 60);
                break;

            case DataViewChart.VIEW_ACTIVITY:
                setTextViewDate(viewDateType);
//                ll_status.setBackgroundColor(DataViewChart.DRAW_ACTIVITY_COLOR);
                useNewUIBottom(true);
                tv_Descript.setText(R.string.activity_desc);

                linearLayout2.setVisibility(View.VISIBLE);
                typeTextView.setText(R.string.activity_time);
                unitTextView.setText(R.string.minutes1);
                describeTextView.setText(R.string.minutes3);
                Logger.i(TAG, "iv_icon+VIEW_ACTIVITY");

                iv_icon.setImageResource(R.drawable.activity);
//                seekBar.setThumb(getResources().getDrawable(R.drawable.activity));
                seekBar.setProgressDrawable(getResources().getDrawable(R.drawable.activity_bar_layer));
                mGoal = (Integer) ConfigHelper.getSharePref(getActivity(), PublicData.SHARED_PRE_SAVE_FILE_NAME,
                        PublicData.TOTAL_TARGET_ACTIVE_MINUTES_KEY, ConfigHelper.DATA_INT);
                if (mGoal <= 0) {
                    mGoal = 60;
                }
                goal = mGoal;
                seekBar.setMax(goal);
                break;

            case HeartRateChartView.VIEW_HEARTRATE:
                setTextViewDate(viewDateType);
//                ll_status.setBackgroundColor(getResources().getColor(R.color.red_bg));
                useNewUIBottom(false);
                ll_status.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, bottomHeight));
                tv_Descript.setText("BPM");

//                layout_dataview.setVisibility(View.VISIBLE);
                linearLayout2.setVisibility(View.GONE);
                break;
        }
        seekBar.setThumbOffset(-3000);
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        goalTextView.setText("" + goal);

        Logger.e(TAG, "======================================================viewKindType=" + viewKindType + "/viewDateType=" + viewDateType);
        if (viewKindType == HeartRateChartView.VIEW_HEARTRATE && viewDateType == DataViewChart.DATEVIEW_DAY) {      // summer: heart rate chart view
            Logger.e(TAG, "==>>viewKindType == HeartRateChartView.VIEW_HEARTRATE");
            if (heartRateChartView == null) {
                heartRateChartView = new HeartRateChartView(getActivity());
                heartRateChartView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heartRateChartHeight
                ));
                Logger.e(TAG, "==>>viewKindType == layout_chartView");
                layout_chartView.addView(heartRateChartView);
                heartRateChartView.initLength();
                // 延时以确保控件获取到自身宽高
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        heartRateChartView.setData(getLocalHeartRateData(), Calendar.getInstance());
                    }
                }, 200);
            }
            return;
        }

        Logger.e(TAG, "======================================================aaa");
        if (areaChartView == null) {
            Logger.e(TAG, "======================================================bbb");

            if (!(viewKindType == DataViewChart.VIEW_SLEEP && viewDateType == DataViewChart.DATEVIEW_DAY)) {
                test(getActivity());
            } else {
                stepView = new DataViewChart(getActivity(), viewType, viewTimeType);
                Logger.e(TAG, "============================================layout_chartView==========ddb");
                layout_chartView.addView(stepView);

                areaChartView = new AreaChartView(getActivity());
                areaChartView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams
                        .MATCH_PARENT));
                areaChartView.setView_timeType(viewTimeType);
                areaChartView.setView_category(viewType);
                Logger.e(TAG, "=====================================layout_chartView=================db");
                layout_chartView.addView(areaChartView);
            }
        } else {
            Logger.e(TAG, "======================================================ccc");
            stepView.setView_category(viewType);

            stepView.setView_timeType(viewTimeType);
            stepView.cleanData();

            stepView.invalidate();

            areaChartView.setView_timeType(viewTimeType);
            areaChartView.setView_category(viewType);
        }
    }

    private MySportView mySportView = null;

    private void test(Context context) {
        Logger.i(TAGGE, "---------------test");

        List<MySportView.Point> points = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            points.add(new MySportView.Point(i, 0));
        }
        mySportView = new MySportView(context);

        String tipText = "步数66步";

        mySportView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams
                .MATCH_PARENT));

        String[] xStr = new String[]{""};
        String[] yStr = new String[]{""};
//        mySportView.init(points, xStr, yStr, tipText, 100);

        Logger.i(TAGGE, "---------------layout_chartView");
        layout_chartView.addView(mySportView);
    }

    // 取本地心率数据
    private List<HeartRateData> getLocalHeartRateData() {
        Logger.i(TAGGE, "---------------getLocalHeartRateData");

        List<HeartRateData> heartRateDatas = new ArrayList<>();
        int firstSec = TimesrUtils.getTimesMorning(Calendar.getInstance());
        DBService dbService = new DBService(getActivity());
        List<HeartRateData> heartRateList = dbService.getHeartRateDataList();
        for (HeartRateData heartRateData : heartRateList) {
            int time = (int) heartRateData.heartRate_time_stamp;
            if (time < firstSec)    // 只取当天心率数据
                break;
            heartRateDatas.add(heartRateData);
            Logger.e(TAG, "==>>local: value, time: " + heartRateData.heartRate_value + ", " + NumberUtils.timeStamp2format(heartRateData
                    .heartRate_time_stamp));
        }
        return heartRateDatas;
    }

    private List<HeartRateData> makeData() {     // summer: for test
        Logger.i(TAGGE, "---------------makeData");

        Calendar calendar = Calendar.getInstance();
        long time = calendar.getTimeInMillis() / 1000L;
        int j = 1;
        List<HeartRateData> heartRateDatas = new ArrayList<HeartRateData>();
        for (int i = 0; i < 80; i++) {
            time -= 60 * 5;
            heartRateDatas.add(new HeartRateData(time, 130 + i * j));
            if (i % 4 == 0)
                j = -j;
            Logger.i(TAG, "==>>heartRateDatas:" + heartRateDatas.get(i));
        }
        return heartRateDatas;
    }

/*
    private void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.actions, popup.getMenu());
        popup.show();
    }
*/


    @Override
    public void onDestroyView() {
        Logger.i(TAGGE, "---------------onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Logger.i(TAGGE, "---------------onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    private void ChangeChartType(int viewType, int timeType) {
        Logger.i(TAGGE, "---------------ChangeChartType");

    }

    // summer: add
    public void initData(Map<String, List<SleepTime>> mMapSleepTimes) {
        Logger.i(TAGGE, "---------------initData1");
        if (stepView != null) {
            stepView.setSleepDetailData(mMapSleepTimes);
            try {
                if (viewKindType == DataViewChart.VIEW_SLEEP && viewDateType == DataViewChart.DATEVIEW_DAY) {
                    int awakeCount = (int) ConfigHelper.getSharePref(getActivity(), PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.SLEEP_DAY_AWAKECOUNT, ConfigHelper.DATA_INT);
                    awakeCount = awakeCount < 0 ? 0 : awakeCount;
                    tv_Descript.setText(awakeCount + getString(R.string.sleep_desc));
                }
                if (mMapSleepTimes == null) {
                    Logger.e("setCurval", "setCurval++setCurval" + 0);
                    setCurVal(0);
                }
            } catch (Exception e) {
            }
        }
    }

    public void initData(float[] data, float[] totalDatas, int maxDays, int viewDateType) {
        Logger.i(TAGGE, "---------------initData2");
        if (viewKindType == DataViewChart.VIEW_SLEEP) {
            try {
                currentValueTextView.setText(getActivity().getResources().getString(R.string.time_no_data));
            } catch (Exception e) {
                e.printStackTrace();
            }
            seekBar.setProgress(0);
        } else {
            currentValueTextView.setText("0");
            seekBar.setProgress(0);
        }
        Logger.i("test-ui", "initData... data = " + (data != null) + " totalDatas = " + (totalDatas != null));
        this.viewDateType = viewDateType;
        if (stepView != null) {
            this.tmpData = data;
            stepView.setData(data, maxDays);
        }
        if (areaChartView != null) {
            areaChartView.setView_category(viewKindType);
            areaChartView.setView_timeType(this.viewDateType);
            areaChartView.setData(data, goal);
        }
        if (mySportView != null && data != null /*&& totalDatas != null*/) {
            Logger.i("test-ui", "mDatas.len : " + data.length);
            initMyView(data, totalDatas);
        }
    }

    private void initMyView(float[] data, float[] totalDatas) {
        Logger.i(TAGGE, "---------------initMyView");

        Logger.i("test-ui", "kindtype : " + viewKindType + " datatype : " + viewDateType);
        float maxVal;
        int goal = 0;
        switch (viewKindType) {
            case DataViewChart.VIEW_STEP:
                goal = (Integer) ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.TOTAL_TARGET_STEPS_KEY, ConfigHelper.DATA_INT);
                goal = goal <= 0 ? 7000 : goal; // 设置步数默认值为1000
                break;
            case DataViewChart.VIEW_CALORIES:
                goal = (Integer) ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.TOTAL_TARGET_CALORIES_KEY, ConfigHelper.DATA_INT);
                goal = goal <= 0 ? 350 : goal; // 设置卡路里默认值为50
                break;
            case DataViewChart.VIEW_DISTANCE:
                goal = (Integer) ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.TOTAL_TARGET_DISTANCE_KEY, ConfigHelper.DATA_INT);
                goal = goal <= 0 ? 5 : goal; // 设置距离默认值为1
                break;
            case DataViewChart.VIEW_ACTIVITY:
                goal = (Integer) ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.TOTAL_TARGET_ACTIVE_MINUTES_KEY, ConfigHelper.DATA_INT);
                goal = goal <= 0 ? 60 : goal; // 设置运动时长默认值为60
                break;
            case DataViewChart.VIEW_SLEEP:
                goal = (Integer) ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.TOTAL_TARGET_SLEEP_KEY, ConfigHelper.DATA_INT);
                goal = goal <= 0 ? 8 * 60 : goal * 60; // 设置睡眠默认值为60
                break;
            default:
                viewKindType = DataViewChart.VIEW_HEART;
                goal = 200;
                break;
        }
        Logger.i("test-ui", "deviceType : " + deviceType);
        Logger.i("test-ui", "viewKindType : " + viewKindType);
        if (deviceType.equals(PublicData.L38I) && viewKindType == 7) {
            data = setViewActivity(data);
            totalDatas = setViewActivity(totalDatas);
        }
        float max = getMax(data); // 每周或每月的最大值
        float max1 = getMax(totalDatas); // 前5周或前5月的最大值

//        String unit = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.UNIT_KEY, ConfigHelper.DATA_STRING);
        String unit = CacheUtils.getString(getActivity(), "unit_value");
        if (unit.equals("1") && viewKindType == DataViewChart.VIEW_DISTANCE) { // 类型是距离，则最大值需要转换到英制
            float dis = (float) CommonUtil.KM2Mile(max / 1000L);
            max = ((int) (dis * 100)) / 100.0f;

            dis = (float) CommonUtil.KM2Mile(max1 / 1000L);
            max1 = ((int) (dis * 100)) / 100.0f;
        }

        maxVal = getMax(new float[]{goal, max, max1}); // 在目标 每周/每月 前5周/前5月 获取三者中最大值

        if (true) {
            Logger.i("test-ui", "准备要绘画的data : " + Arrays.toString(data));
            Logger.i("test-ui", "最大值 : " + maxVal);

        }
        int curPos = 0; // 打开页面后，默认显示位置
        if (viewDateType == DataViewChart.DATEVIEW_WEEK) {
            curPos = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
            curPos = curPos < 0 || curPos > 6 ? 0 : curPos;
            Logger.i("test-test", "周 curpos : " + curPos);
        } else if (viewDateType == DataViewChart.DATEVIEW_MONTH) {
            curPos = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1;
            curPos = curPos < 0 ? 0 : curPos;
            Logger.i("test-test", "月 curpos : " + curPos);
        }

        Logger.i("test-test", "data : " + Arrays.toString(data) + " totalDatas : " + Arrays.toString(totalDatas));
        mySportView.setDatas(viewKindType, viewDateType, data, totalDatas, maxVal, curPos, goal);
//        mySportView.setDatas(viewKindType, viewDateType, data, maxVal, curPos, goal);
        mySportView.invalidate();
    }

    private float getMax(float[] data) {
        Logger.i(TAGGE, "---------------getMax");

        if (data != null) {
            float max = 0.0f;
            for (float a : data) {
                max = a > max ? a : max;
            }
            return max;
        }
        return 0.0f;
    }

    // summer: add
    public void initData(List<HeartRateData> heartRateDatas) {
        Logger.i(TAGGE, "---------------heartRateDatas");
        Logger.e(TAG, "==>>initData(heartRateDatas):" + heartRateDatas.size());
        if (heartRateChartView != null) {
            Logger.i(TAGGE, "---------------heartRateDatas" + heartRateDatas.toString());
            heartRateChartView.setData(heartRateDatas, Calendar.getInstance());
        }
//        heartRateChartView.init();
    }

    public int dp2px(float dpValue) {
        Logger.i(TAGGE, "---------------dp2px");
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Logger.i(TAGGE, "---------------onOptionsItemSelected");

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
