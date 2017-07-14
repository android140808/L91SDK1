package cn.appscomm.pedometer.service;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import apps.utils.Logger;
import apps.utils.TimesrUtils;
import cn.appscomm.pedometer.model.AllRecordData;
import cn.appscomm.pedometer.model.AllSleepRecordData;
import cn.appscomm.pedometer.model.RemindNotesData;
import cn.appscomm.pedometer.model.SleepData;
import cn.appscomm.pedometer.model.SleepTime;
import cn.appscomm.pedometer.model.SportDataCache;
import cn.appscomm.pedometer.model.SportsData;
import cn.l11.appscomm.pedometer.activity.R;


/**
 * @author glin2
 */
public class DBCacheService {


    private static final String TAG = "DBCacheService";
    private DBOpenHelper dbOpenHelper;
    private Context mContext;
    private Calendar mCalendar = Calendar.getInstance();

    public DBCacheService(Context context) {
        dbOpenHelper = new DBOpenHelper(context);
        this.mContext = context;
    }


    // ======================缓存运动数据===========================
    // 数据库保存运动数据： 注意事务的处理
    public synchronized void saveSportsCacheData(SportDataCache mSportDataCache) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.beginTransaction(); // 事务
        try {
            //db.execSQL("insert or replace into tb_sports_data values(null, ? , ? , ? ,? , ?)",
            //	new Object[]{ mSportsData.sport_type, mSportsData.sport_time_stamp, mSportsData.sport_steps, mSportsData.sport_energy, mSportsData.sport_cal} );

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();//由事务的标志决定是提交事务，还是回滚事务
        }

        db.close();
    }


    //


// ======================运动数据===========================
    // 数据库保存运动数据： 注意事务的处理

    public synchronized void saveSportsData(SportsData mSportsData) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.beginTransaction(); // 事务
        try {
            db.execSQL("insert or replace into tb_sports_data values(null, ? , ? , ? ,? , ?,?)",
                    new Object[]{mSportsData.sport_type, mSportsData.sport_time_stamp, mSportsData.sport_steps, mSportsData.sport_energy, mSportsData.sport_cal, mSportsData.sport_timeTotal});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();//由事务的标志决定是提交事务，还是回滚事务
        }

        db.close();
    }

    // 把查询到的记录数组保存到数据库： 注意事务的处理
    public synchronized void saveSportsDataList(List<SportsData> mSportsDataList) {
//		Logger.i(TAG, "saveSportsDataList-->mSportsDataList=" + mSportsDataList);
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.beginTransaction(); // 事务
        try {
            for (SportsData mSportsData : mSportsDataList) {
                db.execSQL("insert or replace into tb_sports_data values(null, ? , ? , ? ,? , ?,?)",
                        new Object[]{mSportsData.sport_type, mSportsData.sport_time_stamp, mSportsData.sport_steps, mSportsData.sport_energy, mSportsData.sport_cal, mSportsData.sport_timeTotal});
//				Logger.i(TAG, "saveSportsDataList-->mSportsData=" + mSportsData);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();//由事务的标志决定是提交事务，还是回滚事务
        }

        db.close();
    }


    //  删除数据库里面的SportsData信息： 注意事务的处理
    public synchronized void deleteSportsData(int id) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.beginTransaction(); // 事务
        try {
            db.execSQL("delete from tb_sports_data where sports_key_pk_id = ?", new Object[]{id}); // 删除新闻内容表的数据
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();//由事务的标志决定是提交事务，还是回滚事务
        }

        db.close();
    }

    public synchronized void deleteSportsData() {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.beginTransaction(); // 事务
        try {
            db.execSQL("delete from tb_sports_data", new Object[]{}); // 删除内容表的数据
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();//由事务的标志决定是提交事务，还是回滚事务
        }

        db.close();
    }

    /**
     * 获取所有运动数据
     *
     * @return
     */
    public List<SportsData> getSportsDataList() {

        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from  tb_sports_data ORDER BY sports_key_pk_id desc limit 500", null); // 倒叙排列
        List<SportsData> mDatas = new ArrayList<SportsData>();
        SportsData mSportsData = null;
        Logger.d(TAG, ">>获取到的所有待上传的运动数据条数：" + cursor.getCount());
        while (cursor.moveToNext()) {
            int pkid = cursor.getInt(cursor.getColumnIndex("sports_key_pk_id"));
            int sport_type = cursor.getInt(cursor.getColumnIndex("sport_type"));
            long sport_time = cursor.getLong(cursor.getColumnIndex("sport_time_stamp"));
            int sport_steps = cursor.getInt(cursor.getColumnIndex("sport_steps"));
            int sport_energy = cursor.getInt(cursor.getColumnIndex("sport_energy"));
            int sport_cal = cursor.getInt(cursor.getColumnIndex("sport_cal"));
            int sport_totaltime = cursor.getInt(cursor.getColumnIndex("sport_totaltime"));
            mSportsData = new SportsData(pkid, sport_type, sport_time, sport_steps, sport_energy, sport_cal, sport_totaltime);
            mDatas.add(mSportsData);
        }
        cursor.close();
        db.close();
        return mDatas;
    }


    // 查询出当前日历的当天的运动数据列表
    public synchronized List<SportsData> getSportsDataList(Calendar cal) {
        String whereStr = "";
        if (cal != null) {
            int currentDayFirst = TimesrUtils.getTimesMorning(cal);
            whereStr = "where " + currentDayFirst + " <= sport_time_stamp AND sport_time_stamp < " + (currentDayFirst + 24 * 60 * 60);
        }

        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from  tb_sports_data " + whereStr + " ORDER BY sports_key_pk_id desc", null); // 倒叙排列
        List<SportsData> mDatas = new ArrayList<SportsData>();
        SportsData mSportsData = null;
        while (cursor.moveToNext()) {

            int sport_type = cursor.getInt(cursor.getColumnIndex("sport_type"));
            long sport_time = cursor.getLong(cursor.getColumnIndex("sport_time_stamp"));
            int sport_steps = cursor.getInt(cursor.getColumnIndex("sport_steps"));
            int sport_energy = cursor.getInt(cursor.getColumnIndex("sport_energy"));
            int sport_cal = cursor.getInt(cursor.getColumnIndex("sport_cal"));
            int sport_totaltime = cursor.getInt(cursor.getColumnIndex("sport_totaltime"));

            mSportsData = new SportsData(sport_type, sport_time, sport_steps, sport_energy, sport_cal, sport_totaltime);
            Logger.i(TAG, "getSportsDataList->mSportsData=" + mSportsData);
            mDatas.add(mSportsData);
        }
        cursor.close();
        db.close();
//		Logger.i(TAG,"mDatas=" + mDatas);
        return mDatas;


    }

    // 查询出当前日历的前面或后面几天的运动数据列表, 从：当前系统日期的往后第几天开始（startDays：为正数），往后获取到第几天的数据（afterDays：为正数）
    public synchronized List<AllRecordData> getAllRecordDataList(int startDays, int afterDays) {
        Logger.i(TAG, "getAllRecordDataList:startDays=" + startDays + " afterDays=" + afterDays);
        Calendar calendar = Calendar.getInstance();
        List<AllRecordData> mDatas = new ArrayList<AllRecordData>();
        final DateFormat dateFormat2 = new SimpleDateFormat("M/dd");
        /*long pDate = 1396263826 * 1000L;
        System.out.println(dateFormat2.format(pDate)); */

        calendar.add(Calendar.DATE, -1 * startDays);
        int currentDayFirst = TimesrUtils.getTimesNight(calendar);

        int steps = 0;

        for (int i = afterDays; i > 0; i--) {
            steps = 0;
            // 从00:00 到23:59分
            String whereStr = "where " + (currentDayFirst - 24 * 60 * 60 * i) + " <= sport_time_stamp AND sport_time_stamp < " + (currentDayFirst - 24 * 60 * 60 * (i - 1));


            SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("select sum(sport_steps) from  tb_sports_data " + whereStr + " ORDER BY sports_key_pk_id desc", null); // 倒叙排列

            AllRecordData mData = null;
            while (cursor.moveToNext()) {
                steps = cursor.getInt(0);
            }
            cursor.close();
            db.close();

            mData = new AllRecordData(steps, dateFormat2.format((currentDayFirst - 24 * 60 * 60 * i) * 1000L));
            Logger.i(TAG, "getAllRecordDataList=" + mData);
            mDatas.add(mData);
        }


//		Logger.i(TAG,"mDatas=" + mDatas);
        return mDatas;


    }


    // 数据库里面做了SportsData的数据条数：
    public synchronized int getSportsDataCount() {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select count( * ) from  tb_sports_data where 1=1", null);
        cursor.moveToFirst();
        int x = cursor.getInt(0);
        cursor.close();
        db.close();
        return x;
    }


    // ======================睡眠===========================
    // 数据库保存睡眠数据： 注意事务的处理
    public synchronized void saveSleepData(SleepData mData) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.beginTransaction(); // 事务
        try {
            db.execSQL("insert or replace into tb_sleep values(null, ? , ? )",
                    new Object[]{mData.sleep_type, mData.sleep_time_stamp});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();//由事务的标志决定是提交事务，还是回滚事务
        }

        db.close();
    }

    // 把睡眠记录数组保存到数据库： 注意事务的处理
    public synchronized void saveSleepDataList(List<SleepData> mDataList) {
//		Logger.i(TAG, "saveSportsDataList-->mSportsDataList=" + mSportsDataList);
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.beginTransaction(); // 事务
        try {
            for (SleepData mData : mDataList) {
                db.execSQL("insert or replace into tb_sleep values(null, ? , ? )",
                        new Object[]{mData.sleep_type, mData.sleep_time_stamp});
//				Logger.i(TAG, "saveSportsDataList-->mSportsData=" + mSportsData);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();//由事务的标志决定是提交事务，还是回滚事务
        }

        db.close();
    }

    //=================================
    public synchronized void deleteSleepData() {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.beginTransaction(); // 事务
        try {
            db.execSQL("delete from tb_sleep", new Object[]{}); // 删除内容表的数据
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();//由事务的标志决定是提交事务，还是回滚事务
        }

        db.close();
    }

    /**
     * 获取所有睡眠数据
     *
     * @return
     */
    public List<SleepData> getSleepDataList() {

        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from  tb_sleep ORDER BY sleep_key_pk_id asc", null); // 升叙排列

        List<SleepData> mDatas = new ArrayList<SleepData>();
        SleepData mData = null;
        Logger.d(TAG, ">>获取到的所有待上传的睡眠数据条数：" + cursor.getCount());
        while (cursor.moveToNext()) {

            int id = cursor.getInt(cursor.getColumnIndex("sleep_key_pk_id"));
            int sleep_type = cursor.getInt(cursor.getColumnIndex("sleep_type"));
            // 秒转成毫秒
            long sleep_time_stamp = cursor.getLong(cursor.getColumnIndex("sleep_time_stamp")) * 1000L;

            mData = new SleepData(id, sleep_type, sleep_time_stamp);
            mDatas.add(mData);
        }
        cursor.close();
        db.close();
        return mDatas;
    }


    //  删除数据库里面的睡眠记录： 注意事务的处理
    public synchronized void deleteSleepData(int id) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.beginTransaction(); // 事务
        try {
            db.execSQL("delete from tb_sleep where sleep_key_pk_id = ?", new Object[]{id}); // 删除新闻内容表的数据
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();//由事务的标志决定是提交事务，还是回滚事务
        }

        db.close();
    }


    // 查询出当前日历的当天的睡眠数据列表
    public synchronized List<SleepData> getSleepDataList(Calendar cal) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        List<SleepData> mDatas = new ArrayList<SleepData>();
        SleepData mData = null;
        // 获取到离日历当天最近的那条数据：比如日历当天是：2014-04-18 14:23，找前面那条时间小于等于 2014-04-18 00:00 最近的记录
        String str = "select * from  tb_sleep where sleep_time_stamp <=" + TimesrUtils.getTimesMorning(cal) + " ORDER BY sleep_time_stamp DESC limit 0,1";
        Cursor cursor0 = db.rawQuery(str, null); // 顺序排列：ASC
        Logger.i(TAG, "cursor0=" + str);
        Logger.i(TAG, "cursor0.count=" + cursor0.getCount());

        while (cursor0.moveToNext()) {

            int id = cursor0.getInt(cursor0.getColumnIndex("sleep_key_pk_id"));
            int sleep_type = cursor0.getInt(cursor0.getColumnIndex("sleep_type"));
            // 秒转成毫秒
            long sleep_time_stamp = cursor0.getLong(cursor0.getColumnIndex("sleep_time_stamp")) * 1000L;

            mData = new SleepData(id, sleep_type, sleep_time_stamp);
            Logger.i(TAG, "getSleepDataList->mData0=" + mData);
            mDatas.add(mData);
        }
        cursor0.close();


        String whereStr = "";
        if (cal != null) {
            int currentDayFirst = TimesrUtils.getTimesMorning(cal);
            whereStr = "where " + currentDayFirst + " <= sleep_time_stamp AND sleep_time_stamp < " + (currentDayFirst + 24 * 60 * 60);
        }

        Cursor cursor = db.rawQuery("select * from  tb_sleep " + whereStr + " ORDER BY sleep_time_stamp ASC", null); // 顺序排列：ASC  倒叙排列：DESC

        while (cursor.moveToNext()) {

            int id = cursor.getInt(cursor.getColumnIndex("sleep_key_pk_id"));
            int sleep_type = cursor.getInt(cursor.getColumnIndex("sleep_type"));
            // 秒转成毫秒
            long sleep_time_stamp = cursor.getLong(cursor.getColumnIndex("sleep_time_stamp")) * 1000L;

            mData = new SleepData(id, sleep_type, sleep_time_stamp);
            Logger.i(TAG, "getSleepDataList->mData=" + mData);
            mDatas.add(mData);
        }
        cursor.close();
        db.close();
//		Logger.i(TAG,"mDatas=" + mDatas);
        return mDatas;


    }

    //2014-06-17 begin
    // 查询出当前日历的前面或后面几天的睡眠数据列表, 从：当前系统日期的往后第几天开始（startDays：为正数），往后获取到第几天的数据（afterDays：为正数）
    public synchronized List<AllSleepRecordData> getAllSleepRecordDataList(int startDays, int afterDays, Date firstDayOfWeek) {
        Logger.i(TAG, ">>>getAllSleepRecordDataList:startDays=" + startDays + " afterDays=" + afterDays + "|date:" + firstDayOfWeek);

        Calendar calendar = Calendar.getInstance();

        String week = "";
        if (firstDayOfWeek == null) {
            firstDayOfWeek = new Date();
        }

        final DateFormat dateFormat2 = new SimpleDateFormat("M/dd");
        List<AllSleepRecordData> mDatas = new ArrayList<AllSleepRecordData>();
        int currentDayFirst = TimesrUtils.getTimesNight(calendar);

        Calendar c = Calendar.getInstance();//new GregorianCalendar();
        for (int i = 0; i < afterDays; i++) {
            if (i == 0) {
                week = mContext.getString(R.string.reminder_mon);

                c.setFirstDayOfWeek(Calendar.MONDAY);
                c.setTime(firstDayOfWeek);
                c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            } else if (i == 1) {
                week = mContext.getString(R.string.reminder_tues);

                c.setFirstDayOfWeek(Calendar.MONDAY);
                c.setTime(firstDayOfWeek);
                c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);

            } else if (i == 2) {
                week = mContext.getString(R.string.reminder_wed);

                c.setFirstDayOfWeek(Calendar.MONDAY);
                c.setTime(firstDayOfWeek);
                c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);

            } else if (i == 3) {
                week = mContext.getString(R.string.reminder_thu);

                c.setFirstDayOfWeek(Calendar.MONDAY);
                c.setTime(firstDayOfWeek);
                c.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);

            } else if (i == 4) {
                week = mContext.getString(R.string.reminder_fri);

                c.setFirstDayOfWeek(Calendar.MONDAY);
                c.setTime(firstDayOfWeek);
                c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);

            } else if (i == 5) {
                week = mContext.getString(R.string.reminder_sat);

                c.setFirstDayOfWeek(Calendar.MONDAY);
                c.setTime(firstDayOfWeek);
                c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);

            } else if (i == 6) {
                week = mContext.getString(R.string.reminder_sun);

                c.setFirstDayOfWeek(Calendar.MONDAY);
                c.setTime(firstDayOfWeek);
                c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

            }


            // 获取日历当天的24小时的睡眠原始数据
            List<SleepData> mSleepDataList = getSleepDataList(c);
            // 将日历当天的24小时的睡眠原始数据，转换成睡眠图表要的数据结构：key值有：	"AWAKE","LIGHT","DEEP" 3组。
            Map<String, List<SleepTime>> mMapOneDaySleepTime = DataService.getOneDaySleepTimeListlocal(mSleepDataList);// 一天里面的轻睡、深睡、醒着的数据

            // 浅睡时间，深睡时间：单位秒
            long intLightSleep = DataService.totalLightSleep(mMapOneDaySleepTime) / 1000L;
            long intDeepSleep = DataService.totalDeepSleep(mMapOneDaySleepTime) / 1000L;

            // 总睡眠小时数
            int slept_hours = (int) (intLightSleep + intDeepSleep) / (60 * 60);
            // 总睡眠分钟数
            int slept_mins = (int) (intLightSleep + intDeepSleep) / 60 % 60;

            String tmp = slept_hours + "." + (slept_mins * 100) / 60;

            Logger.d(TAG, ">>>>>>>>>>>>>>>>hours:" + slept_hours + "|mins:" + slept_mins);
            Logger.d(TAG, ">>>>>>date:" + dateFormat2.format(c.getTime()));

            //AllSleepRecordData mData = new AllSleepRecordData(slept_hours, dateFormat2.format((currentDayFirst - 24*60*60*i) * 1000L));
            AllSleepRecordData mData = new AllSleepRecordData(slept_hours, week);
            Logger.i(TAG, "getAllSleepRecordDataList=" + mData);
            mDatas.add(mData);

        }

        return mDatas;
    }
    //2014-06-17 end

    //2014-06-18 begin
    // 查询出当前日历的前面或后面几天的睡眠数据列表, 从：当前系统日期的往后第几天开始（startDays：为正数），往后获取到第几天的数据（afterDays：为正数）
    public synchronized List<AllSleepRecordData> getAllMonthSleepRecordDataList(int startDays, int afterDays, Date firstDayOfMonth) {
        Logger.i(TAG, ">>>>getAllMonthSleepRecordDataList:startDays=" + startDays + " afterDays=" + afterDays + "|firstDayOfMonth:" + firstDayOfMonth);

        Calendar calendar = Calendar.getInstance();

        String week = "";
        if (firstDayOfMonth == null) {
            calendar.set(Calendar.DATE, 1);
            firstDayOfMonth = calendar.getTime();
            Logger.d(TAG, ">>>>>>>firstDayOfMonth:" + firstDayOfMonth);
        }


        final DateFormat sdf = new SimpleDateFormat("yyyy-MM");
        final DateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        final DateFormat dateFormat2 = new SimpleDateFormat("M/dd");
        String year_month = sdf.format(firstDayOfMonth);
        List<AllSleepRecordData> mDatas = new ArrayList<AllSleepRecordData>();
        int currentDayFirst = TimesrUtils.getTimesNight(calendar);


        Calendar c = Calendar.getInstance();//new GregorianCalendar();
        for (int i = 1; i <= afterDays; i++) {

            String str = year_month + "-" + i;
            Date date = null;
            try {
                date = sdf2.parse(str);
                Logger.i(TAG, ">>>>>>date" + i + ":" + date);
            } catch (ParseException e) {
                e.printStackTrace();
                date = new Date();
            }

            c.setTime(date);


            // 获取日历当天的24小时的睡眠原始数据
            List<SleepData> mSleepDataList = getSleepDataList(c);
            // 将日历当天的24小时的睡眠原始数据，转换成睡眠图表要的数据结构：key值有：	"AWAKE","LIGHT","DEEP" 3组。
            Map<String, List<SleepTime>> mMapOneDaySleepTime = DataService.getOneDaySleepTimeListlocal(mSleepDataList);// 一天里面的轻睡、深睡、醒着的数据

            // 浅睡时间，深睡时间：单位秒
            long intLightSleep = DataService.totalLightSleep(mMapOneDaySleepTime) / 1000L;
            long intDeepSleep = DataService.totalDeepSleep(mMapOneDaySleepTime) / 1000L;

            // 总睡眠小时数
            int slept_hours = (int) (intLightSleep + intDeepSleep) / (60 * 60);
            // 总睡眠分钟数
            int slept_mins = (int) (intLightSleep + intDeepSleep) / 60 % 60;

            String tmp = slept_hours + "." + (slept_mins * 100) / 60;

            Logger.d(TAG, ">>>>>>>>>>>>>>>>hours:" + slept_hours + "|mins:" + slept_mins);
            Logger.d(TAG, ">>>>>>date:" + dateFormat2.format(c.getTime()));

            //AllSleepRecordData mData = new AllSleepRecordData(slept_hours, dateFormat2.format((currentDayFirst - 24*60*60*i) * 1000L));
            AllSleepRecordData mData = new AllSleepRecordData(slept_hours, "");
            Logger.i(TAG, "getAllSleepRecordDataList=" + mData);
            mDatas.add(mData);

        }

        return mDatas;
    }
    //2014-06-18 end


    // ======================提醒便签===========================
    // 数据库保存提醒便签数据： 注意事务的处理,如果id不为-1，就修改记录，为-1就插入新记录
    public synchronized void saveRemindNotesData(RemindNotesData mRemindNotesData) {

        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.beginTransaction(); // 事务
        if (mRemindNotesData.remind_id == -1) {
            try {
                db.execSQL("insert or replace into tb_remind_notes values(null, ? , ? , ? ,? , ?, ?)",
                        new Object[]{mRemindNotesData.remind_type, mRemindNotesData.remind_text, mRemindNotesData.remind_time_hours
                                , mRemindNotesData.remind_time_minutes, mRemindNotesData.remind_week, mRemindNotesData.remind_set_ok});

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();//由事务的标志决定是提交事务，还是回滚事务
            }
        } else {
            try {
                db.execSQL("insert or replace into tb_remind_notes values(?, ? , ? , ? ,? , ?, ?)",
                        new Object[]{mRemindNotesData.remind_id, mRemindNotesData.remind_type, mRemindNotesData.remind_text, mRemindNotesData.remind_time_hours
                                , mRemindNotesData.remind_time_minutes, mRemindNotesData.remind_week, mRemindNotesData.remind_set_ok});

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();//由事务的标志决定是提交事务，还是回滚事务
            }
        }
        db.close();
    }


    // 查询提醒便签列表
    public synchronized List<RemindNotesData> getRemindNotesList() {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from  tb_remind_notes " + " ORDER BY remind_key_pk_id desc", null); // 倒叙排列
        List<RemindNotesData> mDatas = new ArrayList<RemindNotesData>();
        RemindNotesData mData = null;
        while (cursor.moveToNext()) {

            int remind_id = cursor.getInt(cursor.getColumnIndex("remind_key_pk_id"));
            int remind_type = cursor.getInt(cursor.getColumnIndex("remind_type"));
            String remind_text = cursor.getString(cursor.getColumnIndex("remind_text"));
            int remind_time_hours = cursor.getInt(cursor.getColumnIndex("remind_time_hours"));
            int remind_time_minutes = cursor.getInt(cursor.getColumnIndex("remind_time_minutes"));
            String remind_week = cursor.getString(cursor.getColumnIndex("remind_week"));
            int remind_set_ok = cursor.getInt(cursor.getColumnIndex("remind_set_ok"));
//            String remind_ampm = cursor.getString(cursor.getColumnIndex("remind_set_amorpm"));
            mData = new RemindNotesData(remind_id, remind_type, remind_text, remind_time_hours, remind_time_minutes, remind_week, remind_set_ok);
            Logger.i(TAG, "getRemindNotesList->mData=" + mData);
            mDatas.add(mData);
        }
        cursor.close();
        db.close();
//		Logger.i(TAG,"mDatas=" + mDatas);
        return mDatas;


    }


    // 是否有时间一样的记录，有就返回id号，没有就返回-1,排除已经有的id的记录
    public synchronized int findSameTimesID(int hours, int minutes, int outID) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select remind_key_pk_id from  tb_remind_notes where remind_time_hours=? AND remind_time_minutes=? AND remind_key_pk_id!=?", new String[]{hours + "", minutes + "", outID + ""});

        int x = -1;
        if (cursor.moveToFirst()) {
            x = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return x;
    }

    // 数据库里面总共有多少条的提醒便签：
    public synchronized int getRemindNotesCount() {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select count( * ) from  tb_remind_notes where 1=1", null);
        cursor.moveToFirst();
        int x = cursor.getInt(0);
        cursor.close();
        db.close();
        return x;
    }

    // 删除提醒便签表所有数据：
    public synchronized void deleteRemindNotesTableData() {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.beginTransaction(); // 事务
        try {
            db.execSQL("DELETE FROM tb_remind_notes ", new Object[]{}); //
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();//由事务的标志决定是提交事务，还是回滚事务
        }

        db.close();
    }

    // 根据id删除提醒便签表一条记录：
    public synchronized void deleteRemindNoteByID(int id) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.beginTransaction(); // 事务
        try {
            db.execSQL("DELETE FROM tb_remind_notes WHERE remind_key_pk_id = ?", new Object[]{id}); //
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();//由事务的标志决定是提交事务，还是回滚事务
        }

        db.close();
    }


    // 用新闻id查询出：新闻的内容
    public synchronized int getBookmark(int id) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from  tb_sports_data where news_id = ?", new String[]{"" + id});
        int x = 0;
        if (cursor.moveToFirst()) {
            x = cursor.getInt(0);
        } else {
            x = 0;
        }


        cursor.close();
        db.close();
        return x;
    }
/*	// 用新闻id查询出：新闻的内容
	public News getBookmark(int id)
	{
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		Cursor cursor =db.rawQuery("select * from  tb_sports_data where news_id = ?" ,new String[]{""+id} );
		NewsInfo mData = new NewsInfo();
		if(cursor.moveToFirst())
		{
			
			mData.news_id = cursor.getInt(cursor.getColumnIndex("news_id"));									
			mData.news_title = cursor.getString(cursor.getColumnIndex("news_title"));			
			mData.news_date = cursor.getString(cursor.getColumnIndex("news_date"));
			mData.news_thumb = cursor.getString(cursor.getColumnIndex("news_thumb"));		
			
		}
		cursor.close();
		db.close();
		return mData;
	}
*/	
	/*
	// 用新闻id查询出：新闻图片的列表
	public List<String[]> getNewsPics(int id)
	{
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		Cursor cursor =db.rawQuery("select * from  tb_news_pic where news_id = ?" ,new String[]{""+id} );
		List<String[]> mDatas =  new ArrayList<String[]>();
		String[] picArr = new String[2];
		while(cursor.moveToNext())
		{
			picArr[0] = cursor.getString(cursor.getColumnIndex("news_picpath"));		
			picArr[1] = cursor.getString(cursor.getColumnIndex("picpath_id"));		
			mDatas.add(picArr);
						
		}
		cursor.close();
		db.close();
//		Logger.i(TAG,"mDatas=" + mDatas);
		return mDatas;
	}*/
	
	/*
	
	//返回游标，主要给ListView控件的SimpleCursorAdapter来处理,注意一定要有_id 字段返回，否则SimpleCursorAdapter会出错
	public Cursor getCursorData( ){
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		Cursor cursor =db.rawQuery("select name_key_pk_id as _id , name_key , is_choose from tb_my_quotes" 
				,null );
		db.close();
		return cursor;
	}
	
	
	*/

}
