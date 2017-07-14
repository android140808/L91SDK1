package cn.appscomm.pedometer.avater;

/**
 * Created by Administrator on 2017/3/6.
 */

public class HeartBeans {

    /**
     * date : 2017/3/6
     * time : 17:47
     * valur : 123
     */

    private String date;
    private String time;
    private String valur;

    public HeartBeans() {
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getValur() {
        return valur;
    }

    public void setValur(String valur) {
        this.valur = valur;
    }

    public HeartBeans(String date, String time, String valur) {
        this.date = date;
        this.time = time;
        this.valur = valur;
    }
}
