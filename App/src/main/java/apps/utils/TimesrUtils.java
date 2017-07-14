package apps.utils;

import java.util.Calendar;

public class TimesrUtils {

	/*//获得当前日历的0点时间
	public static int getTimesMorning(Calendar cal )
	{ 		
		cal.set(Calendar.HOUR_OF_DAY, 0); 
		cal.set(Calendar.SECOND, 0); 
		cal.set(Calendar.MINUTE, 0); 
		cal.set(Calendar.MILLISECOND, 0); 
		return (int) (cal.getTimeInMillis()/1000); 
	}
		
	//获得当前日历的24点时间 
	public static int getTimesNight(Calendar cal)
	{ 	
		cal.set(Calendar.HOUR_OF_DAY, 24); 
		cal.set(Calendar.SECOND, 0); 
		cal.set(Calendar.MINUTE, 0); 
		cal.set(Calendar.MILLISECOND, 0); 
		return (int) (cal.getTimeInMillis()/1000); 
	}*/

	//获得当前日历的0点时间
	public static int getTimesMorning(Calendar cal )
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, cal.get(Calendar.YEAR));
		calendar.set(Calendar.MONTH, cal.get(Calendar.MONTH));
		calendar.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return (int) (calendar.getTimeInMillis()/1000);
	}

	//获得当前日历的24点时间
	public static int getTimesNight(Calendar cal)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, cal.get(Calendar.YEAR));
		calendar.set(Calendar.MONTH, cal.get(Calendar.MONTH));
		calendar.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
		calendar.set(Calendar.HOUR_OF_DAY, 24);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return (int) (calendar.getTimeInMillis()/1000);
	}

	// summer: add 获得当前日历的23点59分59秒
	public static int getTimesLastSec(Calendar cal)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, cal.get(Calendar.YEAR));
		calendar.set(Calendar.MONTH, cal.get(Calendar.MONTH));
		calendar.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 0);
		return (int) (calendar.getTimeInMillis()/1000);
	}






	/**
	 * @param timestamp
	 * @return 对应的日期整数部分
	 */
	public static long getUnixDate(long timestamp)
	{
		
		return (long) timestamp / (3600*24);
		
	}
	
	

	/**
	 * @param timestamp
	 * @return  0-23小时
	 */
	public static int getUnixHours(long timestamp)
	{
		
		return (int) ((timestamp % (3600*24)) /3600  );
		
	}

    public static int getUnixMins(long timestamp)
    {

        return (int) ((timestamp % (3600*24)) /60  );

    }
}
