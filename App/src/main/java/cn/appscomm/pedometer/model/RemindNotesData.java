package cn.appscomm.pedometer.model;

import java.io.Serializable;


public class RemindNotesData implements Serializable {
    private static final long serialVersionUID = 1L;
    public int remind_id;            // 提醒ID
    public int remind_type;            // 提醒类型	1：运动；2：睡觉；3：吃饭；4：吃药；5:醒来；6:自定义；
    public String remind_text = "";      // 提醒文字
    public int remind_time_hours;   // 提醒时间，小时：24小时制
    public int remind_time_minutes; // 提醒时间，分钟Hours and minutes
    public String remind_week;      // 星期提醒：从周日、六、五、四、三、二到周一，1是提醒，0是不提醒；星期一星期二提醒：0000011；
    public int remind_set_ok;   // 提醒是否已经提交到手环上：已经提交成功到手环是：1； 没有提交是：0；  停用的是：2； 要删除的标志是-1；
    public String amorpm;


    public RemindNotesData() {
    }

    ;


//    public RemindNotesData(int remind_type, String remind_text, int remind_time_hours, int remind_time_minutes, String remind_week, int remind_set_ok, String amorpm) {
//        super();
//        this.remind_id = -1; // 如果没有传值进来就默认为-1
//        this.remind_type = remind_type;
//        this.remind_text = remind_text;
//        this.remind_time_hours = remind_time_hours;
//        this.remind_time_minutes = remind_time_minutes;
//        this.remind_week = remind_week;
//        this.remind_set_ok = remind_set_ok;
//        this.amorpm = amorpm;
//    }

    public RemindNotesData(int remind_type, String remind_text, int remind_time_hours, int remind_time_minutes, String remind_week, int remind_set_ok) {
        super();
        this.remind_id = -1; // 如果没有传值进来就默认为-1
        this.remind_type = remind_type;
        this.remind_text = remind_text;
        this.remind_time_hours = remind_time_hours;
        this.remind_time_minutes = remind_time_minutes;
        this.remind_week = remind_week;
        this.remind_set_ok = remind_set_ok;
    }

//    public RemindNotesData(int remind_id, int remind_type, String remind_text, int remind_time_hours, int remind_time_minutes, String remind_week, int remind_set_ok, String amorpm) {
//        super();
//        this.remind_id = remind_id;
//        this.remind_type = remind_type;
//        this.remind_text = remind_text;
//        this.remind_time_hours = remind_time_hours;
//        this.remind_time_minutes = remind_time_minutes;
//        this.remind_week = remind_week;
//        this.remind_set_ok = remind_set_ok;
//        this.amorpm = amorpm;
//    }

    public RemindNotesData(int remind_id, int remind_type, String remind_text, int remind_time_hours, int remind_time_minutes, String remind_week, int remind_set_ok) {
        super();
        this.remind_id = remind_id;
        this.remind_type = remind_type;
        this.remind_text = remind_text;
        this.remind_time_hours = remind_time_hours;
        this.remind_time_minutes = remind_time_minutes;
        this.remind_week = remind_week;
        this.remind_set_ok = remind_set_ok;
    }


    @Override
    public String toString() {
        return "id:" + remind_id + "type:" + remind_type + " text:" + remind_text + "\n" + " hours:" + remind_time_hours + " minutes:" + remind_time_minutes + " week:" + remind_week + " set_ok:" + remind_set_ok;
    }

}
