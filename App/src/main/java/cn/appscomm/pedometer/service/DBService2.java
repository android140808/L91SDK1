package cn.appscomm.pedometer.service;

/**
 * Created with Eclipse.
 * Author: Tim Liu  email:9925124@qq.com
 * Date: 14-4-7
 * Time: 11:27
 */

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

import apps.utils.Logger;
import apps.utils.TimesrUtils;
import cn.appscomm.pedometer.model.AllRecordData;
import cn.appscomm.pedometer.model.SportsEveryDate;
import cn.l11.appscomm.pedometer.activity.R;



public class DBService2 {
	
	
	private static final String TAG = "DBService2";
	private DBOpenHelper dbOpenHelper;
	private Context mContext;
	
	public DBService2(Context context){
		if (context != null) {
			dbOpenHelper = new DBOpenHelper(context);
			mContext = context;
		}
		
	}
	
	public DBService2(){}
	
	
// ======================运动数据===========================
	// 数据库保存当天的运动数据
	public synchronized  void saveSportsEveryDate(SportsEveryDate mData ){
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();		
		db.beginTransaction(); // 事务
		try{
			db.execSQL("insert or replace into tb_sports_every_date values(null, ? , ? , ? ,? , ?,?)",
				new Object[]{ mData.date_time_stamp, mData.date_steps, mData.date_energy, mData.date_cal, mData.date_goal_energy} );
			
			db.setTransactionSuccessful();
		}finally {
		    db.endTransaction();//由事务的标志决定是提交事务，还是回滚事务
		}
		
		db.close();
	}
	


	// 查询出当前日历的当天的运动数据列表
	public synchronized SportsEveryDate getSportsEveryDate( Calendar cal )
	{
		String whereStr = "";
		if(cal != null )
		{
			int currentDayFirst = TimesrUtils.getTimesMorning(cal);
			whereStr = "where date_time_stamp = " + currentDayFirst ;
		}
		if (dbOpenHelper == null) {
			return null;
		}
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		Cursor cursor =db.rawQuery("select * from  tb_sports_every_date " + whereStr + " ORDER BY date_key_pk_id desc" , null ); // 倒叙排列
		
		Logger.d(TAG, "<<====cursor.getCount():" + cursor.getCount());
		SportsEveryDate mData = new SportsEveryDate();
		while(cursor.moveToNext())
		{
											
			mData.date_key_pk_id = cursor.getInt(cursor.getColumnIndex("date_key_pk_id"));
			mData.date_time_stamp = cursor.getInt(cursor.getColumnIndex("date_time_stamp"));
			mData.date_steps = cursor.getInt(cursor.getColumnIndex("date_steps"));
			mData.date_energy = cursor.getInt(cursor.getColumnIndex("date_energy"));
			mData.date_cal = cursor.getInt(cursor.getColumnIndex("date_cal"));
			mData.date_goal_energy = cursor.getInt(cursor.getColumnIndex("date_goal_energy"));
			
			
			
		}
		cursor.close();
		db.close();
		Logger.i(TAG,"<<====mDatas=" + mData);
		return mData;
		
		
	}
	
	// 查询出当前日历的前面或后面几天的运动数据列表, 从：当前系统日期的往后第几天开始（startDays：为正数），往后获取到第几天的数据（afterDays：为正数）
	public synchronized List<AllRecordData> getAllRecordDataList2(int startDays, int afterDays )
	{
		Logger.i(TAG, "getAllRecordDataList2:startDays2=" + startDays + " afterDays2=" + afterDays);
		Calendar calendar = Calendar.getInstance();		
		List<AllRecordData> mDatas = new ArrayList<AllRecordData>();
		final DateFormat dateFormat2 = new SimpleDateFormat("M/dd"); 
		/*long pDate = 1396263826 * 1000L;
        System.out.println(dateFormat2.format(pDate)); */
		
		calendar.add(Calendar.DATE, -1 * startDays);
		int currentDayFirst = TimesrUtils.getTimesNight( calendar );
		
		int steps = 0;
		
		for(int i = afterDays; i > 0; i--)
		{
			steps = 0;
			// 从00:00 到23:59分
			String whereStr = "where " + (currentDayFirst - 24*60*60*i) + " <= date_time_stamp AND date_time_stamp < " + (currentDayFirst - 24*60*60*(i-1));
			
			
			SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
			Cursor cursor = db.rawQuery("select date_steps from  tb_sports_every_date " + whereStr + " ORDER BY date_key_pk_id desc" , null ); // 倒叙排列
			
			AllRecordData mData = null;
			while(cursor.moveToNext())
			{				
				steps = cursor.getInt(0);				
			}
			cursor.close();
			db.close();
			
			mData = new AllRecordData(steps, dateFormat2.format((currentDayFirst - 24*60*60*i) * 1000L));
			Logger.i(TAG, "getAllRecordDataList2=" + mData);
			mDatas.add(mData);
		}
		
		
		
		
//		Logger.i(TAG,"mDatas=" + mDatas);
		return mDatas;
		
		
	}
	
	// 查询出当前日历的前面或后面几天的运动数据列表, 从：当前系统日期的往后第几天开始（startDays：为正数），往后获取到第几天的数据（afterDays：为正数）
	public synchronized List<AllRecordData> getAllRecordDataList2(int startDays, int afterDays, Date firstDayOfWeek)
	{
		Logger.i(TAG, ">>>getAllRecordDataList2:startDays=" + startDays + " afterDays=" + afterDays + "|date:" + firstDayOfWeek);
		
		Calendar calendar = Calendar.getInstance();
		
		String week = "";
		if (firstDayOfWeek == null) {
			firstDayOfWeek = new Date();
		}
		
		final DateFormat dateFormat2 = new SimpleDateFormat("M/dd");
		List<AllRecordData> mDatas = new ArrayList<AllRecordData>();
		
		int steps = 0;
		Calendar c = Calendar.getInstance();//new GregorianCalendar();
		for (int i=0; i < afterDays; i++) {
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
			
			steps = 0;
			SportsEveryDate sportsEveryDate = getSportsEveryDate(c);
			if (sportsEveryDate != null) {
				steps = sportsEveryDate.date_steps;
			}
			
			AllRecordData mData = new AllRecordData(steps, week);//显示星期
			//AllRecordData mData = new AllRecordData(steps, dateFormat2.format(c.getTime()));//显示月/日
			
			mDatas.add(mData);
			
		}
		
		return mDatas;
	}
	
	public synchronized List<AllRecordData> getAllRecordDataList2_Calories(int startDays, int afterDays, Date firstDayOfWeek)
	{
		Logger.i(TAG, ">>>getAllRecordDataList2:startDays=" + startDays + " afterDays=" + afterDays + "|date:" + firstDayOfWeek);
		
		Calendar calendar = Calendar.getInstance();
		
		String week = "";
		if (firstDayOfWeek == null) {
			firstDayOfWeek = new Date();
		}
		
		final DateFormat dateFormat2 = new SimpleDateFormat("M/dd");
		List<AllRecordData> mDatas = new ArrayList<AllRecordData>();
		
		int calories = 0;
		Calendar c = Calendar.getInstance();//new GregorianCalendar();
		for (int i=0; i < afterDays; i++) {
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
			
			calories = 0;
			SportsEveryDate sportsEveryDate = getSportsEveryDate(c);
			if (sportsEveryDate != null) {
				calories = sportsEveryDate.date_cal;
			}
			
			AllRecordData mData = new AllRecordData(calories, week);//显示星期
			//AllRecordData mData = new AllRecordData(steps, dateFormat2.format(c.getTime()));//显示月/日
			
			mDatas.add(mData);
			
		}
		
		return mDatas;
	}
	
	/**
	 * 查询当月的按天的汇数数据
	 * @param monthDays 当月的天数
	 * @param firstDayOfMonth 当月的第一天
	 * @return
	 */
	public synchronized List<AllRecordData> getAllRecordDataList(int monthDays, Date firstDayOfMonth)
	{
		Logger.i(TAG, ">>>getAllRecordDataList:monthDays=" + monthDays + "|firstDayOfMonth:" + firstDayOfMonth);
		
		Calendar calendar = Calendar.getInstance();
		
		
		if (firstDayOfMonth == null) {
			calendar.set(Calendar.DATE, 1);
			firstDayOfMonth = calendar.getTime();
			Logger.d(TAG, ">>>>>>>firstDayOfMonth:" + firstDayOfMonth);
		}
		
		final DateFormat sdf = new SimpleDateFormat("yyyy-MM");
		final DateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
		final DateFormat dateFormat2 = new SimpleDateFormat("M/dd");
		String year_month = sdf.format(firstDayOfMonth);
		
		List<AllRecordData> mDatas = new ArrayList<AllRecordData>();
		
		int steps = 0;
		Calendar c = Calendar.getInstance();//new GregorianCalendar();
		for (int i=1; i <= monthDays; i++) {
			String str = year_month + "-" + i;
			String day = "";
			Date date = null;
			/**
			if (i % 5 == 0) {
				day = "" + i;
			}
			*/
			if (i == 1 || i == 7 || i == 13 || i == 19 || i == 25 || i == 30) {
				day = "" + i;
			}
			
			try {
				date = sdf2.parse(str);
				Logger.i(TAG, ">>>>>>date" + i + ":" + date);
			} catch (ParseException e) {
				e.printStackTrace();
				date = new Date();
			}
			
			c.setTime(date);
			
			steps = 0;
			SportsEveryDate sportsEveryDate = getSportsEveryDate(c);
			if (sportsEveryDate != null) {
				steps = sportsEveryDate.date_steps;
			}
			Logger.i(TAG, ">>steps:" + steps + "|(" + i + ")");
			
			AllRecordData mData = new AllRecordData(steps, day);//显示星期
			//AllRecordData mData = new AllRecordData(steps, dateFormat2.format(c.getTime()));//显示月/日
			
			mDatas.add(mData);
			
		}
		
		return mDatas;
	}
	
	public synchronized List<AllRecordData> getAllRecordDataList_Calories(int monthDays, Date firstDayOfMonth)
	{
		Logger.i(TAG, ">>>getAllRecordDataList:monthDays=" + monthDays + "|firstDayOfMonth:" + firstDayOfMonth);
		
		Calendar calendar = Calendar.getInstance();
		
		
		if (firstDayOfMonth == null) {
			calendar.set(Calendar.DATE, 1);
			firstDayOfMonth = calendar.getTime();
			Logger.d(TAG, ">>>>>>>firstDayOfMonth:" + firstDayOfMonth);
		}
		
		final DateFormat sdf = new SimpleDateFormat("yyyy-MM");
		final DateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
		final DateFormat dateFormat2 = new SimpleDateFormat("M/dd");
		String year_month = sdf.format(firstDayOfMonth);
		
		List<AllRecordData> mDatas = new ArrayList<AllRecordData>();
		
		int steps = 0;
		Calendar c = Calendar.getInstance();//new GregorianCalendar();
		for (int i=1; i <= monthDays; i++) {
			String str = year_month + "-" + i;
			String day = "";
			Date date = null;
			/**
			if (i % 5 == 0) {
				day = "" + i;
			}
			*/
			if (i == 1 || i == 7 || i == 13 || i == 19 || i == 25 || i == 30) {
				day = "" + i;
			}
			
			try {
				date = sdf2.parse(str);
				Logger.i(TAG, ">>>>>>date" + i + ":" + date);
			} catch (ParseException e) {
				e.printStackTrace();
				date = new Date();
			}
			
			c.setTime(date);
			
			steps = 0;
			SportsEveryDate sportsEveryDate = getSportsEveryDate(c);
			if (sportsEveryDate != null) {
				steps = sportsEveryDate.date_cal;
			}
			Logger.i(TAG, ">>steps:" + steps + "|(" + i + ")");
			
			AllRecordData mData = new AllRecordData(steps, day);//显示星期
			//AllRecordData mData = new AllRecordData(steps, dateFormat2.format(c.getTime()));//显示月/日
			
			mDatas.add(mData);
			
		}
		
		return mDatas;
	}
	
}
