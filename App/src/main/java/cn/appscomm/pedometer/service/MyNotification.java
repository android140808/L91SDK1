
package cn.appscomm.pedometer.service;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import apps.utils.ConfigHelper;
import apps.utils.Logger;
import apps.utils.ParseUtil;
import apps.utils.PublicData;
import cn.appscomm.pedometer.application.GlobalApp;
import cn.appscomm.pedometer.model.NotificatuinPushModel;
import cn.appscomm.pedometer.protocol.AboutMsgPush.CalendarPush;
import cn.appscomm.pedometer.protocol.AboutMsgPush.MailPush;
import cn.appscomm.pedometer.protocol.AboutMsgPush.MsgCountPush;
import cn.appscomm.pedometer.protocol.AboutMsgPush.SocialPush;
import cn.appscomm.pedometer.protocol.AboutMsgPush.SocialPush1;
import cn.appscomm.pedometer.protocol.BluetoothUtil;
import cn.appscomm.pedometer.protocol.Commands;
import cn.appscomm.pedometer.protocol.IResultCallback;
import cn.appscomm.pedometer.protocol.Leaf;

/**
 * @author zenglinguo@appscomm.cn
 * @version: 1.01
 */
public class MyNotification extends NotificationListenerService implements IResultCallback {

    private String TAG = this.getClass().getSimpleName();
    public final static String NEW_NOTIFICATION = "com.appscomm.new_notification_l28h";
    public final static String DEL_NOTIFICATION = "com.appscomm.del_notification_l28h";

    // 消息推送中各app对应的包名
    public static final String GMAIL_PKG = "com.google.android.gm";// gmail
    public static final String OUTLOOK_PKG = "com.outlook.Z7";// outlook com.microsoft.office.outlook
    public static final String OUTLOOK2_PKG = "com.microsoft.office.outlook";// outlook com.microsoft.office.outlook
    public static final String QQMAIL_PKG = "com.tencent.androidqqmail";// QQ邮箱
    public static final String MAIL139_PKG = "cn.cj.pe";// 139邮箱
    public static final String NETMAIL_PKG = "com.netease.mobimail";// 网易邮箱
    public static final String SINAMAIL_PKG = "com.sina.mail";// 新浪邮箱
    public static final String WPSMAIL_PKG = "com.kingsoft.email";// wps邮箱
    public static final String YAHOOMAIL_PKG = "com.yahoo.mobile.client.android.mail";// 雅虎邮箱
    public static final String SCHEDULE_PKG = "com.android.calendar";// 日程
    public static final String SCHEDULE_HTC_PKG = "com.htc.calendar";
    public static final String SCHEDULE_BBK_PKG = "com.bbk.calendar";

    public static final String GMAIL_SCHEDULE_PKG = "com.google.android.calendar";// gmail
    // 日程
    private Handler mHandler = new Handler();

    public static final String QQLIST_PKG = "com.tencent.qqlite";// QQ轻聊版
    public static final String MOBILEQQ_PKG = "com.tencent.mobileqq";// 手机QQ
    public static final String FACEBOOK_PKG = "com.facebook.katana";
    public static final String FBMSG_PKG = "com.facebook.orca"; // messager
    public static final String TWITTER_PKG = "com.twitter.android";
    public static final String WHATSAPP_PKG = "com.whatsapp";// whatsapp
    public static final String KAKAO_PKG = "com.kakao.talk";// kakao
    public static final String MM_PKG = "com.tencent.mm";// 微信
    public static final String LINE_PKG = "jp.naver.line.android";// LINE
    public static final String SKYPE_PKG = "com.skype.android.verizon";//com.skype.polaris
    public static final String SKYPE_PKG2 = "com.skype.polaris";
    public static final String SKYPE_PKG3 = "com.skype.rover";
    public static final String INSTAGRAM_PKG = "com.instagram.android";
    public static final String VIBER_PKG = "com.viber.voip";                            // Viber
    public static final String SNAPCHAT_PKG = "com.snapchat.android";                   // Snapchat com.snapchat.android
    public static final String HANGOUTS_PKG = "com.google.android.talk";                // Hangouts 环聊
    public static final String LINKEDIN_PKG = "com.linkedin.android";                   // Linkedin
    public static final String UBER_PKG = "com.ubercab";                                // Uber

    public static final String XIAOMI = "com.xiaomi.xmsf"; //小米
    public static final String ONE_SIX_THREE = "com.netease.mobimail";

    /*
     * Intent intent=new
     * Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
     * startActivity(intent);
     */
    boolean is_email_on = false, is_soc_on = false, is_cal_on = false;  // 通知开关状态
    private final int TYPE_SOCIAL = 0;//社交
    private final int TYPE_EMAIL = 1;//邮件
    private final int TYPE_CALENDAR = 2;//日历
    private LinkedList<NotificatuinPushModel> sendMSG = new LinkedList<>();
    private static int calendarCount;
    private static int emailCount;

    private Handler handler = new Handler();
    private Intent intent;
    private static String lastPackage = "";

    private List<MsgInOutTime> msgList;
    private Timer timer1;


    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("new-test3", "MyNotification+onCreate");

        msgList = new ArrayList<MsgInOutTime>();

        msgList.add(new MsgInOutTime(GMAIL_PKG, TYPE_EMAIL, 0));
        msgList.add(new MsgInOutTime(OUTLOOK_PKG, TYPE_EMAIL, 0));
        msgList.add(new MsgInOutTime(OUTLOOK2_PKG, TYPE_EMAIL, 0));
        msgList.add(new MsgInOutTime(QQMAIL_PKG, TYPE_EMAIL, 0));

        msgList.add(new MsgInOutTime(SCHEDULE_PKG, TYPE_CALENDAR, 0));
        msgList.add(new MsgInOutTime(GMAIL_SCHEDULE_PKG, TYPE_CALENDAR, 0));
        msgList.add(new MsgInOutTime(SCHEDULE_HTC_PKG, TYPE_CALENDAR, 0)); // HTC的日历
        msgList.add(new MsgInOutTime(SCHEDULE_BBK_PKG, TYPE_CALENDAR, 0)); // HTC的日历
        msgList.add(new MsgInOutTime(QQLIST_PKG, TYPE_SOCIAL, Commands.MSGPUSHTYPE_QQ));
        msgList.add(new MsgInOutTime(MOBILEQQ_PKG, TYPE_SOCIAL, Commands.MSGPUSHTYPE_QQ));
        msgList.add(new MsgInOutTime(FACEBOOK_PKG, TYPE_SOCIAL, Commands.MSGPUSHTYPE_FACEBOOK));
        msgList.add(new MsgInOutTime(FBMSG_PKG, TYPE_SOCIAL, Commands.MSGPUSHTYPE_MESSENGER));
        msgList.add(new MsgInOutTime(TWITTER_PKG, TYPE_SOCIAL, Commands.MSGPUSHTYPE_TWITTER));
        msgList.add(new MsgInOutTime(WHATSAPP_PKG, TYPE_SOCIAL, Commands.MSGPUSHTYPE_WHATSAPP));
        msgList.add(new MsgInOutTime(KAKAO_PKG, TYPE_SOCIAL, 0));
        msgList.add(new MsgInOutTime(MM_PKG, TYPE_SOCIAL, Commands.MSGPUSHTYPE_WECHAT));
        msgList.add(new MsgInOutTime(LINE_PKG, TYPE_SOCIAL, Commands.MSGPUSHTYPE_LINE));
        msgList.add(new MsgInOutTime(SKYPE_PKG3, TYPE_SOCIAL, Commands.MSGPUSHTYPE_SKYPE));
        msgList.add(new MsgInOutTime(SKYPE_PKG2, TYPE_SOCIAL, Commands.MSGPUSHTYPE_SKYPE));
        msgList.add(new MsgInOutTime(SKYPE_PKG, TYPE_SOCIAL, Commands.MSGPUSHTYPE_SKYPE));
        msgList.add(new MsgInOutTime(INSTAGRAM_PKG, TYPE_SOCIAL, Commands.MSGPUSHTYPE_INSTAGRAM));
        msgList.add(new MsgInOutTime(SNAPCHAT_PKG, TYPE_SOCIAL, Commands.MSGPUSHTYPE_SNAPCHAT));
//        msgList.add(new MsgInOutTime(XIAOMI, TYPE_EMAIL, 0));
        msgList.add(new MsgInOutTime(ONE_SIX_THREE, TYPE_EMAIL, 0));


        timer1 = new Timer();
        timer1.schedule(new TimerTask() {

            @Override
            public void run() {

                for (MsgInOutTime msg1 : msgList) {

                    if (msg1.removTime > msg1.addTime) {
                        Logger.d("new-test3", "timeout");
                        msg1.timeOutCount++;

                        if (msg1.timeOutCount > 2) {
                            // send timeout
//                            Intent intent1 = new Intent(DEL_NOTIFICATION);
//                            intent1.putExtra("event", msg1.packageName);
//                            sendBroadcast(intent1);
                            msg1.timeOutCount = 0;
                            msg1.removTime = 0;
                        }
                    } else {
                        msg1.timeOutCount = 0;
                    }

                }

                // TODO Auto-generated method stub

            }
        }, 1000, 500);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("new-test3", "mynotification-onDestroy");

    }

    @Override
    public void onSuccess(Leaf leaf) {
        BluetoothUtil.getInstance().continueSend();
    }

    @Override
    public void onFaild(Leaf leaf) {
        BluetoothUtil.getInstance().continueSend();

    }

    class MsgInOutTime {

        public MsgInOutTime(String packageName, int type, int commandType) {
            super();
            this.packageName = packageName;
            this.type = type;
            this.commandType = commandType;
        }

        public String packageName = "";
        public int timeOutCount = 0;

        public long addTime = 0;
        public long removTime = 0;

        public int type;
        public int commandType;
    }


    /* (non-Javadoc)
     * @see android.service.notification.NotificationListenerService#onNotificationPosted(android.service.notification.StatusBarNotification)
     *android消息通知在这里获取，有些消息推送会先删除前一个消息（会触发一次onNotificationRemoved），注意时间间隔处理
     */
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Logger.i(TAG, "**********  onNotificationPosted");
        boolean needSendNotif = false;

        try {
            Logger.i(TAG, "ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());

        } catch (Exception e) {
        }

        String addPackage = sbn.getPackageName();
        String type = "9";
        for (MsgInOutTime msg1 : msgList) {
            if (msg1.packageName.equals(addPackage)) {

                needSendNotif = true;
                type = msg1.type + "";
                if ((System.currentTimeMillis() - msg1.addTime) > 100) {
                    msg1.addTime = System.currentTimeMillis();
                } else {
                    msg1.addTime = System.currentTimeMillis();
                    return;

                }

            }

        }

        if (!needSendNotif)
            return;

//        if (PublicData.selectDeviceName.equals(PublicData.L42C)) {
        Logger.i("", "  sendMsg(sbn);");
        sendMsg(sbn);
//        }else{
//            String packageName = sbn.getPackageName();                                                  // 包名
//            Notification notification = sbn.getNotification();
//
//            String content = notification.tickerText != null ? notification.tickerText.toString() : ""; // 内容
//            String time = ParseUtil.timeStampToString(sbn.getPostTime(), ParseUtil.SDF_TYPE_YMDTHMS);   // 时间
//            String title = "";                                                                          // 标题
//            Logger.i(TAG, "新消息到来..."+"packageName="+packageName+",content="+content+",time="+time+",title="+title);
//
//            try {
//                title = getPackageManager().getApplicationLabel(getPackageManager().getPackageInfo(packageName, 0).applicationInfo).toString();
//            } catch (PackageManager.NameNotFoundException e) {
//                Logger.i(TAG, "找不到该包名");
//                e.printStackTrace();
//            }
//            Logger.i(TAG, "新消息到来..."+"packageName="+packageName+",content="+content+",time="+time+",title="+title);
//
//            Intent i = new Intent(NEW_NOTIFICATION);
//            i.putExtra("event", sbn.getPackageName());
//            sendBroadcast(i);
//        }


    }


    MsgInOutTime removeNotifications;
    Runnable removeMsgCountRunnable = new Runnable() {
        @Override
        public void run() {
            if (removeNotifications != null) {
                removeNotifications.timeOutCount = 0;
            }
        }
    };

    private void sendMsg(StatusBarNotification sbn) {
        Logger.v(TAG, "新消息到来...");
        mHandler.removeCallbacks(removeMsgCountRunnable);
        String packageName = sbn.getPackageName();                                                  // 包名
        Notification notification = sbn.getNotification();

        String content = notification.tickerText != null ? notification.tickerText.toString() : ""; // 内容
        String time = ParseUtil.timeStampToString(sbn.getPostTime(), ParseUtil.SDF_TYPE_YMDTHMS);   // 时间
        String title = "";                                                                          // 标题
        Logger.i(TAG, "新消息到来..." + "packageName=" + packageName + ",content=" + content + ",time=" + time + ",title=" + title);

        try {
            title = getPackageManager().getApplicationLabel(getPackageManager().getPackageInfo(packageName, 0).applicationInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Logger.i(TAG, "找不到该包名");
            e.printStackTrace();
        }
        Logger.i(TAG, "新消息到来..." + "packageName=" + packageName + ",content=" + content + ",time=" + time + ",title=" + title);
        String text = "";
        String sendName = "";
        if (android.os.Build.VERSION.SDK_INT >= 19) {
            text = notification.extras.getString(Notification.EXTRA_TEXT);
            sendName = notification.extras.getString(Notification.EXTRA_TITLE);
        }
        if (packageName.equals(WHATSAPP_PKG)) {
            //whatsApp的title有名字外，还有软件的名称。
            content = sendName + ":" + text;
        }
        if (TextUtils.isEmpty(content) && !TextUtils.isEmpty(text)) {
            content = text;
        }

        Logger.i(TAG, "新消息到来..." + "packageName=" + packageName + ",content=" + content + ",time=" + time + ",title=" + title);

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(time)) {
            content = TextUtils.isEmpty(content) ? "" : content;
            boolean isSendNotification = false;
            MsgInOutTime currentNotifications = null;
            for (MsgInOutTime notifications : msgList) {
//                Logger.i(TAG, "要发送的包名是:" + notifications.addTime+"时间="+System.currentTimeMillis());
                Logger.i(TAG, "要发送的包名是:-----" + packageName);
                Logger.i(TAG, "要发送的包名是:-----" + notifications.packageName);
                Logger.i(TAG, "要发送的包名是:" + (((Math.abs(System.currentTimeMillis() - notifications.addTime)) > 100 ? true : false) ? 0 : notifications.addTime));
                if (notifications.packageName.equals(packageName)) {
                    Logger.i(TAG, "要发送的包名是:" + packageName);
                    isSendNotification = true;
                    currentNotifications = notifications;
                    notifications.addTime = System.currentTimeMillis();
                    break;
                }
            }

            if (isSendNotification && currentNotifications != null) {
                getNotifyStatus();
                Logger.i(TAG, "发送类型:" + currentNotifications.type + "(0:社交 1:邮件 2:日历)");
                Logger.i(TAG, "是否含有:" + content.contains(":"));
                if ((TextUtils.isEmpty(content)) && (currentNotifications.type == TYPE_SOCIAL || currentNotifications.type == TYPE_EMAIL)) {
                    if (packageName.equals(QQMAIL_PKG)) {
                        content = "www.appscomm.com";
                    } else {
                        Logger.i(TAG, "发送社交，内容不能为空");
                        return;
                    }
                }
                if (!content.contains(":") && currentNotifications.packageName == MM_PKG) {
                    //邮件内容出现空时，直接卡死了。防止出现触摸了解应用或者停止应用的情况。
                    Logger.i(TAG, "发送社交，内容不能为空");
                    return;
                }
//                if (!PublicData.checkAndJumpBind(false)) {
//                    Logger.i(TAG, "还没有绑定设备,这里不做任何处理了...");
//                    return;
//                }
                Logger.i(TAG, "发送社交，内容不为空");

                try {
                    switch (currentNotifications.type) {
                        case TYPE_SOCIAL:
                            if (is_soc_on) {
                                if (rememberMSG(title, content, time)) {
                                    Logger.i(TAG, "---社交数量:" + currentNotifications.timeOutCount);
                                    currentNotifications.timeOutCount++;
                                    sendSocial(title, content, time, currentNotifications.timeOutCount, currentNotifications.commandType);
                                }
                            }
                            break;
                        case TYPE_CALENDAR:
                            if (PublicData.L38I.equals(PublicData.selectDeviceName)) {
                                Intent i = new Intent(NEW_NOTIFICATION);
                                i.putExtra("event", sbn.getPackageName());
                                sendBroadcast(i);
                                return;
                            }
                            calendarCount = calendarCount < 0 ? 0 : calendarCount;
                            calendarCount++;
                            Logger.i(TAG, "---日历数量:" + calendarCount);
                            if (is_cal_on) {

                                sendCalendar(content, calendarCount, time);

//                                BluetoothUtil.getInstance().send(new MsgCountPush(this, 2, Commands.MSGPUSHTYPE_CALENDAR, (byte) calendarCount));

                            }
                            break;
                        case TYPE_EMAIL:
                            if (PublicData.L38I.equals(PublicData.selectDeviceName)) {
                                Intent i = new Intent(NEW_NOTIFICATION);
                                i.putExtra("event", sbn.getPackageName());
                                sendBroadcast(i);
                                return;
                            }
                            emailCount = emailCount < 0 ? 0 : emailCount;
                            emailCount++;
                            Logger.i(TAG, "---邮件数量:" + emailCount);
                            if (is_email_on) {

                                if (rememberMSG(title, content, time)) {
                                    sendMail(title, content, time, emailCount);
                                }
//                                BluetoothUtil.getInstance().sendEmailMsg(this, emailCount);
                            }
                            break;
                    }
                } catch (Exception e) {
                    Logger.i(TAG, "Activity已销毁,有异常了...");
                    e.printStackTrace();
                }
            }
        } else {
            if (!TextUtils.isEmpty(packageName)) {
                Logger.i(TAG, "xxxxxx标题/内容/时间为空，不能发送通知(" + packageName + ")");
            }
        }
    }

    private void sendCalendar(String content, int count, String time) {
        try {
            //日历内容
            String sSendDatas = content;
            int len = (sSendDatas.getBytes().length > Commands.MSGPUSHTYPE_MAXNAMELEN ? Commands.MSGPUSHTYPE_MAXNAMELEN : sSendDatas.getBytes().length) + 1;
            byte[] bSendDatas;
            LinkedList<Leaf> leafs = new LinkedList<Leaf>();
            bSendDatas = new byte[len - 1];
            System.arraycopy(sSendDatas.getBytes(), 0, bSendDatas, 0, len - 1);
            Logger.i(TAG, "邮件" + " 整理要发送的内容(" + sSendDatas + ")");
            leafs.add(new CalendarPush(this, len, (byte) 0x01, bSendDatas));

            //
            len = 16; // 日期时间长度15+命令码长度1
            sSendDatas = time;
            bSendDatas = sSendDatas.getBytes("US-ASCII");
            Logger.i(TAG, "邮件" + " 整理要发送的时间(" + sSendDatas + ")");
            leafs.add(new CalendarPush(this, len, (byte) 0x02, bSendDatas));

            // 消息条数
            Logger.i(TAG, "邮件数量:" + count);
            Logger.i(TAG, " ");
            leafs.add(new MsgCountPush(this, 2, Commands.MSGPUSHTYPE_CALENDAR, (byte) count));

            BluetoothUtil.getInstance().send(leafs);
        } catch (Exception e) {

        }


    }

    // 发送邮件
    private void sendMail(String title, String content, String time, int count) {
        try {
            String sSendDatas = title;
            int len = (sSendDatas.getBytes().length > Commands.MSGPUSHTYPE_MAXNAMELEN ? Commands.MSGPUSHTYPE_MAXNAMELEN : sSendDatas.getBytes().length) + 1;

            byte[] bSendDatas;
            LinkedList<Leaf> leafs = new LinkedList<Leaf>();

            // 邮件标题
            bSendDatas = new byte[len - 1];
            System.arraycopy(sSendDatas.getBytes(), 0, bSendDatas, 0, len - 1);
            Logger.i(TAG, "邮件" + " 整理要发送的标题(" + sSendDatas + ")");
            leafs.add(new MailPush(this, len, (byte) 0x00, bSendDatas));

            // 邮件内容
            sSendDatas = content;
            len = (sSendDatas.getBytes().length > Commands.MSGPUSHTYPE_MAXCONTENTLEN ? Commands.MSGPUSHTYPE_MAXCONTENTLEN : sSendDatas.getBytes().length) + 1;
            bSendDatas = new byte[len - 1];
            System.arraycopy(sSendDatas.getBytes(), 0, bSendDatas, 0, len - 1);
            Logger.i(TAG, "邮件" + " 整理要发送的内容(" + sSendDatas + ")");
            leafs.add(new MailPush(this, len, (byte) 0x01, bSendDatas));

            // 邮件时间
            len = 16; // 日期时间长度15+命令码长度1
            sSendDatas = time;
            bSendDatas = sSendDatas.getBytes("US-ASCII");
            Logger.i(TAG, "邮件" + " 整理要发送的时间(" + sSendDatas + ")");
            leafs.add(new MailPush(this, len, (byte) 0x02, bSendDatas));

            // 消息条数
            Logger.i(TAG, "邮件数量:" + count);
            Logger.i(TAG, " ");
            leafs.add(new MsgCountPush(this, 2, Commands.MSGPUSHTYPE_EMAIL, (byte) count));

            BluetoothUtil.getInstance().send(leafs);
        } catch (Exception e) {
        }
    }

    private boolean rememberMSG(String title, String content, String time) {
        boolean result = false;
        NotificatuinPushModel msg = new NotificatuinPushModel(title, content, time);
        if (!sendMSG.contains(msg)) {
            sendMSG.add(0, msg);
            if (sendMSG.size() >= 100) {//做过滤的信息记录，若消息条数小于100
                sendMSG.removeLast();
            }
            result = true;
        }
        return result;
    }

    // 发送社交
    private void sendSocial(String title, String content, String time, int count, int socialType) {
        try {
            String sSendDatas = title;
            int len = (sSendDatas.getBytes().length > Commands.MSGPUSHTYPE_MAXNAMELEN ? Commands.MSGPUSHTYPE_MAXNAMELEN : sSendDatas.getBytes().length) + 1;

            byte[] bSendDatas;
            LinkedList<Leaf> leafs = new LinkedList<Leaf>();

            // 社交标题
            bSendDatas = new byte[len - 1];
            System.arraycopy(sSendDatas.getBytes(), 0, bSendDatas, 0, len - 1);
            Logger.i(TAG, "社交" + " 整理要发送的标题(" + sSendDatas + ")");
            leafs.add(new SocialPush(this, len, (byte) 0x00, bSendDatas));

            // 社交内容
            sSendDatas = content;
            len = (sSendDatas.getBytes().length > Commands.MSGPUSHTYPE_MAXCONTENTLEN ? Commands.MSGPUSHTYPE_MAXCONTENTLEN : sSendDatas.getBytes().length) + 1;
            bSendDatas = new byte[len - 1];
            System.arraycopy(sSendDatas.getBytes(), 0, bSendDatas, 0, len - 1);

            Logger.i(TAG, "社交" + " 整理要发送的内容(" + sSendDatas + ")");
            Log.e("lenth", "处理后的字符的字节长度：" + new String(bSendDatas).getBytes().length);

            if (bSendDatas.length == 120) {
                String limit_str = new String(bSendDatas);
                String litmit = limit_str.substring(0, limit_str.length() - 4);
                String new_litmit = litmit + "...";
                bSendDatas = new_litmit.getBytes();
            }

            leafs.add(new SocialPush(this, len, (byte) 0x01, bSendDatas));
            // 社交时间
            len = 16; // 日期时间长度15+命令码长度1
            sSendDatas = time;
            bSendDatas = sSendDatas.getBytes("US-ASCII");
            Logger.i(TAG, "社交" + " 整理要发送的时间(" + sSendDatas + ")");
            leafs.add(new SocialPush(this, len, (byte) 0x02, bSendDatas));

            // 消息条数
            Logger.i(TAG, "社交数量:" + count);
            Logger.i(TAG, " ");
            leafs.add(new MsgCountPush(this, 2, (byte) socialType, (byte) count));

            BluetoothUtil.getInstance().send(leafs);
        } catch (Exception e) {
        }
    }

    /**
     * 获取通知设置状态(来电、未接来电、短信、邮件、日历、社交)
     */
    private void getNotifyStatus() {
        Logger.i("", "getNotifyStatus");

        is_email_on = (Boolean) getSharePref(PublicData.NOTI_EMAILSW_ITEM_KEY, ConfigHelper.DATA_BOOLEAN);
        is_soc_on = (Boolean) getSharePref(PublicData.NOTI_SOCSW_ITEM_KEY, ConfigHelper.DATA_BOOLEAN);
        is_cal_on = (Boolean) getSharePref(PublicData.NOTI_CALSW_ITEM_KEY, ConfigHelper.DATA_BOOLEAN);

        Logger.i(TAG, " 社交(" + is_soc_on + ") 邮件(" + is_email_on + ") 日历(" + is_cal_on + ")");
    }

    /**
     * 获取sp中key对应的value
     *
     * @param key  键
     * @param type value类型
     * @return
     */
    private Object getSharePref(String key, int type) {
        return ConfigHelper.getSharePref(GlobalApp.globalApp.getApplicationContext(), PublicData.SHARED_PRE_SAVE_FILE_NAME, key, type);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Logger.i(TAG, "********** onNOtificationRemoved");
        if (PublicData.selectDeviceName.equals(PublicData.L42)) {
            removeMsg(sbn);
        }
        try {

            Logger.i(TAG, "ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());

        } catch (Exception e) {

        }

        try {

            String removePkg = sbn.getPackageName();

            // lastPackage = removePkg;

            for (MsgInOutTime msg1 : msgList) {
                if (msg1.packageName.equals(removePkg)) {
                    msg1.removTime = System.currentTimeMillis();

                }

            }

        } catch (Exception e) {
        }
    }

    private void removeMsg(StatusBarNotification sbn) {
        Log.e(TAG, "移除消息...");
        String packageName = sbn.getPackageName();
        Notification notification = sbn.getNotification();
        String content = notification.tickerText != null ? notification.tickerText.toString() : "";
        Logger.i(TAG, "移除的内容:" + content + " 时间:" + sbn.getPostTime());

        for (MsgInOutTime notifications : msgList) {
            if (notifications.packageName.equals(packageName)) {
                //此处原来有500S的判断，但是使用系统的删除所有通知功能时，会出现过滤后续的通知删除
                //从而导致实际数目与变量不一致的情况
                switch (notifications.type) {
                    case TYPE_EMAIL:
                        emailCount--;
                        emailCount = emailCount < 0 ? 0 : emailCount;
                        break;
                    case TYPE_CALENDAR:
                        if ((Math.abs(System.currentTimeMillis() - notifications.removTime) > 500)) {
                            calendarCount--;
                            calendarCount = calendarCount < 0 ? 0 : calendarCount;
                        }
                        break;
                    case TYPE_SOCIAL:
                        if ((Math.abs(System.currentTimeMillis() - notifications.removTime) > 500)) {
                            removeNotifications = notifications;
                            mHandler.postDelayed(removeMsgCountRunnable, 1000);                         // 延迟1秒钟执行，若1秒钟内有新消息过来，则取消执行(解决QQ有新消息过来会先移除消息)
                        }
                        break;
                }
                notifications.removTime = System.currentTimeMillis();
                break;
            }
        }
    }

}
