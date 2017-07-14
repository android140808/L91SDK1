package cn.appscomm.pedometer.model;

import java.util.ArrayList;

import apps.utils.NumberUtils;

public class SleepRecordData {
	public static class SleepTime{
		public String startTime;
		public String endTime;
	}
	
	public ArrayList<SleepTime> awakeTimes;
	public ArrayList<SleepTime> lightTimes;
	public ArrayList<SleepTime> deepTimes;
	
	public String sAwakeTime; // 开始醒着
	public String eAwakeTime; // 结束醒着
	public String sLightTime; // 开始轻睡
	public String eLightTime; // 结束轻睡
	public String sDeepTime;  // 开始深睡
	public String eDeepTime;  // 结束深睡
	
	
	
	public SleepRecordData(){};	
		
	public SleepRecordData(String sAwakeTime , String eAwakeTime, String sLightTime, String eLightTime , String sDeepTime, String eDeepTime) 
	{
		super();
		this.sAwakeTime = sAwakeTime;
		this.eAwakeTime = eAwakeTime;
		this.sLightTime = sLightTime;
		this.eLightTime = eLightTime;
		this.sDeepTime = sDeepTime;	
		this.eDeepTime = eDeepTime;	
	}
	
	
	@Override
	public String toString()
	{		
		return "sAwakeTime:" + sAwakeTime  +" eAwakeTime:"+ eAwakeTime  + " sLightTime:"+ sLightTime + "\n" + " eLightTime:"+ eLightTime  +" sDeepTime:"+ sDeepTime +" eDeepTime:"+ eDeepTime ;
	} 
	
	
}
