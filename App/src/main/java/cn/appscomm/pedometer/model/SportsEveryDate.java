package cn.appscomm.pedometer.model;

import java.io.Serializable;

import apps.utils.NumberUtils;


public class SportsEveryDate implements Serializable{
	private static final long serialVersionUID = 1L;
	public int date_key_pk_id;      // id
	public long date_time_stamp;    // 运动时间，时间戳，秒
	public int date_steps;          // 步数
	public int date_energy;         // 能量环值Energy 
	public int date_cal;            // 卡路里
	public int date_goal_energy;    // 目标能量值
	
	
	
	public SportsEveryDate(){};	
		
	public SportsEveryDate(int date_key_pk_id , long date_time_stamp, int date_steps, int date_energy , int date_cal, int date_goal_energy) 
	{
		super();
		this.date_key_pk_id = date_key_pk_id;
		this.date_time_stamp = date_time_stamp;
		this.date_steps = date_steps;
		this.date_energy = date_energy;
		this.date_cal = date_cal;
		this.date_goal_energy = date_goal_energy;	
	}
	
	
	@Override
	public String toString()
	{		
		return " id:" + date_key_pk_id + " date_steps:"+ date_steps + " date_energy:"+ date_energy  +" date_cal:"+ date_cal +" date_goal_energy:"+ date_goal_energy  + "\n"  + " time_stamp:"+ date_time_stamp + " =" + NumberUtils.timeStamp2format(date_time_stamp);
	} 
	
}
