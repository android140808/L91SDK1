package cn.appscomm.pedometer.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bugtags.library.Bugtags;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import apps.utils.CommonUtil;
import apps.utils.ConfigHelper;
import apps.utils.DialogUtil;
import apps.utils.HttpUtil;
import apps.utils.ImageUtil;
import apps.utils.LeaderBoardUrl;
import apps.utils.Logger;
import apps.utils.PublicData;
import cn.appscomm.pedometer.model.LeaderBoard;
import cn.appscomm.pedometer.model.StrangerData;
import cn.appscomm.pedometer.service.StrangerAdapter;
import cn.l11.appscomm.pedometer.activity.R;

public class FindFriendsActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "FindFriendsActivity";
    private static final String ICON_FOLDER_PATH = "http://test.3plus.fashioncomm.com/sportimg/user/";       // �û�ͷ�����ļ��У���������
    private ImageButton btn_back;
    private Button btn_search, curAddBtn;
    private TextView curAddTV;
    private EditText et_search;
    private ListView lv_strangers;
    private StrangerAdapter strangerAdapter;
    private List<StrangerData> mStrangerDataList;
    private ProgressDialog mProgressDialog;

    private HttpUtil httpUtil;
    private boolean isSearching = false;
    private int versionCode;
    private int ddId, friendId;
    private String versionNo, clientType, accountId;
    private List<Integer> friendDDIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        TextView tv_title = (TextView) findViewById(R.id.title);
        tv_title.setText(getString(R.string.title_find_friends));
        Logger.i("", "onCreate");
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        btn_search = (Button) findViewById(R.id.btn_search);
        et_search = (EditText) findViewById(R.id.et_search);
        lv_strangers = (ListView) findViewById(R.id.lv_strangers);
        mStrangerDataList = new ArrayList<>();
        friendDDIDs = new ArrayList<Integer>();

//        testListView();

        btn_back.setOnClickListener(this);
        btn_search.setOnClickListener(this);

        //设置软键盘搜索键
        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // 先隐藏键盘
                    ((InputMethodManager) et_search.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(
                                    FindFriendsActivity.this.getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                    //搜索逻辑
                    onClick(btn_search);
                    return true;
                }
                return false;
            }
        });


        httpUtil = new HttpUtil(this);
        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        versionNo = "" + versionCode;
        clientType = "android";
        accountId = (String) ConfigHelper.getCommonSharePref(
                this,
                PublicData.SHARED_PRE_SAVE_FILE_NAME,
                PublicData.CURRENT_EMAIL_ITEM_KEY, 1);
        ddId = (int) ConfigHelper.getCommonSharePref(FindFriendsActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                PublicData.CURRENT_DDID_ITEM_KEY, ConfigHelper.DATA_INT);
        if (ddId == -1) {

        }
        for (LeaderBoard friend : MainActivity.leaderBoardList) {
            friendDDIDs.add(friend.ddId);
        }
    }

    private void handleSearchResult() {
        Logger.i(TAG, "handleSearchResult: mStrangerDataList: " + mStrangerDataList.size());
        if (mStrangerDataList != null && mStrangerDataList.size() > 0) {
            if (strangerAdapter == null) {
                strangerAdapter = new StrangerAdapter(this, mStrangerDataList, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.btn_addFriend:
                                handlerAddBtn(v);
                                break;
                            case R.id.tv_addFriend:
                                handlerAddBtn(v);
                                break;
                        }
                    }
                });
                lv_strangers.setAdapter(strangerAdapter);
            }
            strangerAdapter.notifyDataSetChanged();
            isSearching = false;
        }
    }

    private void handlerAddBtn(View view) {
        StrangerData curStrangerData = (StrangerData) view.getTag();
//        curAddBtn = (Button) view;
        curAddTV = (TextView) view;
        friendId = curStrangerData.ddId;
        int curIndex = mStrangerDataList.indexOf(curStrangerData);
        Logger.i(TAG, "curIndex: " + curIndex);
        new Thread(inviteRunnable).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case R.id.btn_search:
                Logger.i("", "btn_search=" + isSearching);
                Logger.i("", "et_search=" + et_search.getText().toString());
                // 先隐藏键盘
                ((InputMethodManager) et_search.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(
                                FindFriendsActivity.this.getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                if (isSearching) {
                    Logger.i("", "mStrangerDataList!Q=null");

                    if (mStrangerDataList != null && mStrangerDataList.size() > 0) {
//                        mStrangerDataList.clear();
//                        strangerAdapter.notifyDataSetChanged();
                        mProgressDialog = null;
                        Logger.i("", "mStrangerDataList!Q=null");
                    }
//                    if (mProgressDialog == null) {
                    mProgressDialog = ProgressDialog.show(this, null,
                            getString(R.string.searching), true, true);
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    Logger.i("", "mStrangerDataList!Q=null");
//                    }
                    return;
                }
                if (!et_search.getText().toString().equals("")) {
                    if (!judgeInternet()) {
                        Toast.makeText(FindFriendsActivity.this, getResources().getString(R.string.NetWorkError_1), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (mStrangerDataList != null && mStrangerDataList.size() > 0) {
                        mStrangerDataList.clear();
                        strangerAdapter.notifyDataSetChanged();
                        Logger.i("", "mStrangerDataList!Q=null");
                    }
                    if (mProgressDialog == null) {
                        mProgressDialog = ProgressDialog.show(this, null,
                                getString(R.string.searching), true, true);
//                        mProgressDialog.setCancelable(false);
                        mProgressDialog.setCanceledOnTouchOutside(false);
                    }
                    new Thread(queryAccountRunnable).start();
//                    isSearching = true;
                }
                break;
        }
    }

    private boolean checkEmail() {
        if ("".equals(et_search.getText().toString())) {
            DialogUtil.commonDialog(FindFriendsActivity.this,
                    getString(R.string.app_name),
                    getString(R.string.reg_email_null));
            return false;
        }
        if (!(et_search.getText().toString().contains("@")) || (!CommonUtil.emailFormat(et_search.getText().toString().trim()))) {
            DialogUtil.commonDialog(FindFriendsActivity.this,
                    getString(R.string.app_name),
                    getString(R.string.reg_email_wrong));
            return false;
        }
        return true;
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
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == connectivityManager) {
            isHavaNet = false;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (null == networkInfo) {
            isHavaNet = false;
        }
        return isHavaNet;
    }


    Runnable queryAccountRunnable = new Runnable() {
        @Override
        public void run() {


            String url = LeaderBoardUrl.url_queryJoin;
            String seq = LeaderBoardUrl.createRandomSeq();
            String versionNo = "" + versionCode;
            String clientType = "android";
            String accountId = et_search.getText().toString().trim();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("seq", seq);
                jsonObject.put("versionNo", versionNo);
                jsonObject.put("clientType", clientType);
                jsonObject.put("accountId", accountId);
                jsonObject.put("type", "2");
//                jsonObject.put("customerCode", "3PLUS");
                jsonObject.put("customerCode", "appscomm");
                String params = jsonObject.toString();
                int respondStatus = httpUtil.httpPostWithJSON(url, params);
                String respondBody = httpUtil.httpResponseResult;
                Logger.e(TAG, "==>>respondStatus:" + respondStatus);
                switch (respondStatus) {
                    case 0:
                        try {
                            Logger.i(TAG, "==>>respondBody:" + respondBody);
                            if (respondBody.contains("\"seq\"") && respondBody.contains("\"code\"")
                                    && respondBody.contains("\"msg\"")) {
                                JSONObject jsonObj = new JSONObject(respondBody);
                                String seq1 = jsonObj.getString("seq");
                                String code = jsonObj.getString("code");
                                String msg = jsonObj.getString("msg");
                                Logger.e(TAG, "==>>code:" + code);
                                if (code.equals("0")) {
                                    Logger.i("", "code==0");
                                    JSONArray jsonArray = jsonObj.getJSONArray("accounts");
                                    Logger.i("", "jsonArray=" + jsonArray);

                                    if (jsonArray != null && !"".equals(jsonArray) && jsonArray.length() > 0) {
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject json = jsonArray.getJSONObject(i);
                                            int ddId = json.getInt("ddId");
                                            String accountId1 = json.getString("accountId");
                                            String userName = json.getString("userName");
                                            String iconUrl = json.getString("iconUrl");
                                            boolean isFriendFlag = false;
                                            if (!containStranger(ddId)) {
                                                if (friendDDIDs.contains(ddId)) {
                                                    isFriendFlag = true;
                                                }
                                                mStrangerDataList.add(new StrangerData(ddId, i, accountId1, userName, iconUrl, ImageUtil.getImage(FindFriendsActivity.this, iconUrl), isFriendFlag));
                                            }
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                handleSearchResult();
                                            }
                                        });
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(FindFriendsActivity.this, getResources().getString(R.string.no_this_username), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            }
                            closeProgressDiag();
                        } catch (JSONException e) {
                            closeProgressDiag();
                            e.printStackTrace();
                        }
                        break;
                    default:
                        isSearching = false;
                        closeProgressDiag();
                        break;
                }
            } catch (JSONException e) {
                closeProgressDiag();
                e.printStackTrace();
            }
        }
    };

    private boolean containStranger(int ddId) {
        for (StrangerData strangerData : mStrangerDataList) {
            if (strangerData.ddId == ddId)
                return true;
        }
        return false;
    }

    private void testListView() {
        String[] from = {"ci_pic", "tv_name"};
        int[] to = {R.id.ci_pic, R.id.tv_name};
        List<HashMap<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("ci_pic", testCreateBitmap());
            hashMap.put("tv_name", "NAME" + i);
            list.add(hashMap);

            mStrangerDataList.add(new StrangerData(i, i, "accountId", "NAME" + i, "", testCreateBitmap()));
        }
        strangerAdapter = new StrangerAdapter(this, mStrangerDataList, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlerAddBtn(v);
            }
        });
        lv_strangers.setAdapter(strangerAdapter);
        strangerAdapter.notifyDataSetChanged();
    }

    private Bitmap testCreateBitmap() {
        return BitmapFactory.decodeResource(getResources(), R.drawable.ic_default_pic);
    }

    public void createDD(View view) {
        new Thread(createDDRunnable).start();
    }

    Runnable createDDRunnable = new Runnable() {
        @Override
        public void run() {
            String url = LeaderBoardUrl.url_createDD;
            String seq = LeaderBoardUrl.createRandomSeq();
            String versionNo = "" + versionCode;
            String clientType = "android";
            String accountId = (String) ConfigHelper.getCommonSharePref(
                    FindFriendsActivity.this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_EMAIL_ITEM_KEY, ConfigHelper.DATA_STRING);
            String userName = (String) ConfigHelper.getCommonSharePref(
                    FindFriendsActivity.this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_NAME_ITEM_KEY, ConfigHelper.DATA_STRING);
            String deviceType = PublicData.getCloudDeviceType(PublicData.L39);
            String iconUrl = "";
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("seq", seq);
                jsonObject.put("versionNo", versionNo);
                jsonObject.put("clientType", clientType);
                jsonObject.put("accountId", accountId);
//                jsonObject.put("customerCode", "3PLUS");
                jsonObject.put("customerCode", "appscomm");
                jsonObject.put("deviceType", deviceType);
                jsonObject.put("userName", userName);
                jsonObject.put("iconUrl", iconUrl);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String params = jsonObject.toString();
            int respondStatus = httpUtil.httpPostWithJSON(url, params);
            String respondBody = httpUtil.httpResponseResult;
            Logger.e(TAG, "==>>respondStatus:" + respondStatus);
            switch (respondStatus) {
                case 200:
                    try {
                        Logger.i(TAG, "==>>respondBody:" + respondBody);
                        if (respondBody.contains("\"seq\"") && respondBody.contains("\"code\"")
                                && respondBody.contains("\"msg\"")) {
                            JSONObject jsonObj = new JSONObject(respondBody);
                            String seq1 = jsonObj.getString("seq");
                            String code = jsonObj.getString("code");
                            String msg = jsonObj.getString("msg");
                            int ddId = jsonObj.getInt("ddId");
                            Logger.e(TAG, "==>>code:" + code);
                            if (code.equals("0")) {
                                ConfigHelper.setCommonSharePref(FindFriendsActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                        PublicData.CURRENT_DDID_ITEM_KEY, ddId);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    public void invite(View view) {

    }

    Runnable inviteRunnable = new Runnable() {
        @Override
        public void run() {
            String url = LeaderBoardUrl.url_joinFriend;
            String seq = LeaderBoardUrl.createRandomSeq();
            String versionNo = "" + versionCode;
            String clientType = "android";
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("seq", seq);
                jsonObject.put("versionNo", versionNo);
                jsonObject.put("clientType", clientType);
                jsonObject.put("ddId", ddId);
                jsonObject.put("friendId", friendId);
                String params = jsonObject.toString();
                int respondStatus = httpUtil.httpPostWithJSON(url, params);
                String respondBody = httpUtil.httpResponseResult;
                Logger.e(TAG, "==>>respondStatus:" + respondStatus);
                switch (respondStatus) {
                    case 0:
                        try {
                            Logger.i(TAG, "==>>respondBody:" + respondBody);
                            if (respondBody.contains("\"seq\"") && respondBody.contains("\"code\"")
                                    && respondBody.contains("\"msg\"")) {
                                JSONObject jsonObj = new JSONObject(respondBody);
                                String seq1 = jsonObj.getString("seq");
                                String code = jsonObj.getString("code");
                                String msg = jsonObj.getString("msg");
                                Logger.e(TAG, "==>>code:" + code);
                                if (code.equals("0")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            /*if (curAddBtn != null) {
                                                curAddBtn.setBackgroundResource(R.drawable.btn_add_friend_success);
                                                curAddBtn.setText("added");
                                            }*/
                                            if (curAddTV != null) {
                                                StrangerData mData = (StrangerData) curAddTV.getTag();
                                                mData.isFriendFlag = true;
                                                strangerAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                    new Thread(queryLeaderBoardRunnable).start();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    Runnable queryLeaderBoardRunnable = new Runnable() {
        @Override
        public void run() {
            String url = LeaderBoardUrl.url_queryLeaderBoard;
            String seq = LeaderBoardUrl.createRandomSeq();
            ddId = (int) ConfigHelper.getCommonSharePref(FindFriendsActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_DDID_ITEM_KEY, ConfigHelper.DATA_INT);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();
            String today = sdf.format(new Date(calendar.getTimeInMillis()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            String tomorrow = sdf.format(new Date(calendar.getTimeInMillis()));
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("seq", seq);
                jsonObject.put("versionNo", versionNo);
                jsonObject.put("clientType", clientType);
                jsonObject.put("ddId", ddId);
                jsonObject.put("accountId", accountId);
                jsonObject.put("queryDateStart", today);
                jsonObject.put("queryDateEnd", tomorrow);
                String params = jsonObject.toString();
                int respondStatus = httpUtil.httpPostWithJSON(url, params);
                String respondBody = httpUtil.httpResponseResult;
                Logger.e(TAG, "==>>respondStatus:" + respondStatus);
                switch (respondStatus) {
                    case 0:
                        try {
                            Logger.i(TAG, "==>>respondBody:" + respondBody);
                            if (respondBody.contains("\"seq\"") && respondBody.contains("\"code\"")
                                    && respondBody.contains("\"msg\"")) {
                                JSONObject jsonObj = new JSONObject(respondBody);
                                String seq1 = jsonObj.getString("seq");
                                String code = jsonObj.getString("code");
                                String msg = jsonObj.getString("msg");
                                Logger.e(TAG, "==>>code:" + code);
                                if (code.equals("0")) {
                                    JSONArray jsonArray = jsonObj.getJSONArray("details");
                                    if (jsonArray != null && jsonArray.length() > 0) {
                                        MainActivity.leaderBoardList.clear();
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject json = jsonArray.getJSONObject(i);
                                            int ddId = json.getInt("ddId");
                                            int memberId = json.getInt("memberId");
                                            String userName = json.getString("userName");
                                            String iconUrl = json.getString("iconUrl");
                                            long dataDate = 0L;
                                            int sportsStep = json.getInt("sportsStep");
                                            float sportsDistance = (float) json.getDouble("sportsDistance");
                                            float sportsCalorie = (float) json.getDouble("sportsCalorie");
                                            float activeTime = (float) json.getDouble("activeTime");
                                            long updateTime = System.currentTimeMillis();
                                            try {
                                                updateTime = (long) json.getLong("updateTime");
                                            } catch (Exception e) {
                                                Logger.i(TAG, "updateTime为空");
                                            }
                                            LeaderBoard leaderBoard = new LeaderBoard(ddId, memberId, userName, iconUrl, dataDate,
                                                    sportsStep, sportsDistance, sportsCalorie, activeTime, updateTime);
                                            MainActivity.leaderBoardList.add(leaderBoard);
                                            Logger.i(TAG, i + ": " + leaderBoard.toString());
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

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
        Logger.i("", "ev" + ev);
        Bugtags.onDispatchTouchEvent(this, ev);
        return super.dispatchTouchEvent(ev);
    }

    private void closeProgressDiag() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        isSearching = false;
        super.onDestroy();
    }


}
