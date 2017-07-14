package cn.appscomm.pedometer.model;

public  class SportDataSendInfo {
	
	public static int totalDataCount =0;  //要从数据库上传的总记录
	public static int curTodayCount=0;    //当前这一笔上传数据中多少条属于今天的
	public static  int curCount=0;        //当前这一笔上传数据中多少条data（全部的）
	public static int totalSendedCount=0;  // 已经共上传了多少条数据
	public static int totalTodaySendedCount=0;  //已经共上传了多少条数据（今天的）
	
	public SportDataSendInfo()
	{
		 totalDataCount =0;
		 curTodayCount=0;
		 curCount=0;
		 totalSendedCount=0;
		 totalTodaySendedCount=0;
		
	}

  public void resetData()
  {
	     totalDataCount =0;
		 curTodayCount=0;
		 curCount=0;
		 totalSendedCount=0;
		 totalTodaySendedCount=0;  
	  
	  
  }
	
	@Override
	public String toString() {
		return "SportDataInfo [totalDataCount=" + totalDataCount
				+ ", curTodayCount=" + curTodayCount + ", curCount=" + curCount
				+ ", totalSendedCount=" + totalSendedCount
				+ ", totalTodaySendedCount=" + totalTodaySendedCount + "]";
	}

}
