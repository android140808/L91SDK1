package cn.appscomm.pedometer.application;

import android.app.Application;
import android.content.Context;

import com.newrelic.agent.android.NewRelic;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import apps.utils.CommonUtil;
import apps.utils.Constants;
import apps.utils.CrashHandler;
import apps.utils.PublicData;
import cn.l11.appscomm.pedometer.activity.R;

public class GlobalApp extends Application {

    public Calendar calendar;// = Calendar.getInstance();

    public String curDateStr;
    public AppManager mManager;

    public Date curDate;

    public static GlobalApp globalApp;

    //7天的运动汇总数据
    public Map<String, Map<String, String>> sport7Days;

    //7天的睡眠汇总数据
    public Map<String, Map<String, String>> sleep7Days;

    @Override
    public void onTerminate() {
        super.onTerminate();
        System.exit(0);
    }

    @Override
    public void onCreate() {
        PublicData.arrgender = new String[]{getString(R.string.reg_male), getString(R.string.reg_female)};         //gender 0: male 1: female
        PublicData.arrHeightUnit = new String[]{getString(R.string.cm), getString(R.string.ft)};
        PublicData.arrWeightUnit = new String[]{getString(R.string.kg), getString(R.string.lbs)};
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext(), this);
        mManager = AppManager.getAppManager();
        CommonUtil.updateGobalcontext(this);
//		BugtagsOptions options = new BugtagsOptions.Builder().
//				trackingLocation(true).//是否获取位置
//				trackingCrashLog(true).//是否收集crash
//				trackingConsoleLog(true).//是否收集console log
//				trackingUserSteps(true).//是否收集用户操作步骤
//				crashWithScreenshot(true).//crash附带图
//				versionName("1.0.1").//自定义版本名称
//				versionCode(10).//自定义版本号
//				build();

        NewRelic.withApplicationToken(Constants.NEWRELIC_APPLICATION_TOKEN).start(this);
//		Bugtags.start("3589931dcdf5487b71818d8362725448", this, Bugtags.BTGInvocationEventNone);
        //Bugtags.start("3589931dcdf5487b71818d8362725448", this, Bugtags.BTGInvocationEventBubble);
        globalApp = this;
        super.onCreate();
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public String getCurDateStr() {
        return curDateStr;
    }

    public void setCurDateStr(String curDateStr) {
        this.curDateStr = curDateStr;
    }

    public Date getCurDate() {
        return curDate;
    }

    public void setCurDate(Date curDate) {
        this.curDate = curDate;
    }

    public Map<String, Map<String, String>> getSport7Days() {
        return sport7Days;
    }

    public void setSport7Days(Map<String, Map<String, String>> sport7Days) {
        this.sport7Days = sport7Days;
    }

    public Map<String, Map<String, String>> getSleep7Days() {
        return sleep7Days;
    }

    public void setSleep7Days(Map<String, Map<String, String>> sleep7Days) {
        this.sleep7Days = sleep7Days;
    }

    // summer: add
    public boolean isAllowService() {
        return isAllowService;
    }

    public void setIsAllowService(boolean isAllowService) {
        this.isAllowService = isAllowService;
    }

    private boolean isAllowService = true;

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }


}
