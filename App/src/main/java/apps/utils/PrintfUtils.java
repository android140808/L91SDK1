package apps.utils;

/**
 * Created by huang on 15-12-5-0005.
 */
public class PrintfUtils {
    /**
     * 解析并打印发送命令
     *
     * @param TAG
     * @param bytes
     */
    private static String[] str1 = new String[]{"产品类型", "watchID", "固件信息", "软件版本信息", "", "", "", "", "", "", "", ""};
    private static String[] str2 = new String[]{"增加", "修改", "单条删除", "全部删除", "查询", "", "", "", "", "", "", "", ""};
    private static String[] str3 = new String[]{"", "吃饭", "吃药", "运动", "睡觉", "喝水", "清醒", "会议", "自定义", "", "", "", "", "", ""};
    private static String[] str4 = new String[]{"关闭", "开启", "", "", "", "", "", ""};
    private static String[] str5 = new String[]{"男", "女", "", "", "", "", "", ""};
    private static String[] str6 = new String[]{"左手", "右手", "", "", "", "", "", ""};
    private static String[] str7 = new String[]{"关闭二次提醒", "开启二次提醒", "", "", "", "", "", ""};
    private static String[] str8 = new String[]{"删除运动数据", "删除睡眠数据", "设置成自动删除运动数据和睡眠数据命令", "设置成手动删除运动数据和睡眠数据命令", "", "", "", "", "", ""};
    private static String[] str9 = new String[]{"不敏感", "敏感", "", "", "", ""};
    private static String[] str10 = new String[]{"", "增加", "删除", "", "", "", "", "", ""};
    private static String[] str11 = new String[]{"步数", "卡路里", "距离", "运动时间", "", "", "", ""};

    private static String sendTAG, revTAG;

    private static String getDate(byte[] byteArray, int start) {
        int year = (int) (byteArray[start] & 0xff) + (int) ((byteArray[start + 1] & 0xff) << 8);
        int month = (int) (byteArray[start + 2] & 0xff);
        int day = (int) (byteArray[start + 3] & 0xff);
        return year + "年" + month + "月" + day + "日";
    }

    private static String getTime(byte[] bytes, int start) {
        int hour = (int) (bytes[start] & 0xff);
        int min = (int) (bytes[start + 1] & 0xff);
        int sec = (int) (bytes[start + 2] & 0xff);
        return hour + "时" + min + "分" + sec + "秒";
    }

    private static String getSendTime(byte[] bytes, int start) {
        int year = (bytes[start] - 0x30) * 1000 + (bytes[start + 1] - 0x30) * 100 + (bytes[start + 2] - 0x30) * 10 + (bytes[start + 3] - 0x30);
        int month = (bytes[start + 4] - 0x30) * 10 + (bytes[start + 5] - 0x30);
        int day = (bytes[start + 6] - 0x30) * 10 + (bytes[start + 7] - 0x30);
        int hour = (bytes[start + 9] - 0x30) * 10 + (bytes[start + 10] - 0x30);
        int min = (bytes[start + 11] - 0x30) * 10 + (bytes[start + 12] - 0x30);
        int sec = (bytes[start + 13] - 0x30) * 10 + (bytes[start + 14] - 0x30);

        return year + "年" + month + "月" + day + "日 " + hour + "时" + min + "分" + sec + "秒";
    }

    /**
     * 计算步数/卡路里/距离/运动时间
     *
     * @param bytes
     * @param start
     * @return
     */
    private static String getStepCalDisData(byte[] bytes, int start) {
        int value = (int) (bytes[start] & 0xff) +
                (int) ((bytes[start + 1] & 0xff) << 8) +
                (int) ((bytes[start + 2] & 0xff) << 16) +
                (int) ((bytes[start + 3] & 0xff) << 24);
        return value + "";
    }

    private static String printfSendMsg(String TAG, byte[] bytes, int type) {
        String msg = "", details = "";
        try {
            String readDOC = "具体查看文档";
            String separator = " : ";
            byte command = 0x00;
            if (type == 0) {
                command = bytes[2];
            } else {
                command = bytes[3];
            }
            switch (command) {
                case 0x02:
                    msg = "获取产品类型";
                    break;
                case 0x03:
                    msg = "获取设备固化信息";
                    if (type == 0)
                        details = msg + separator + str1[bytes[3]];
                    break;
                case 0x04:
                    msg = "获取watchID";
                    break;
                case 0x06:
                    msg = "上传运动数据";
                    break;
                /*case 0x05:
                    msg = "增/删/查/改提醒";
                    if (type == 0) {
                        details = msg + separator + str2[bytes[3]];
                        details += separator + str3[bytes[4]];
                        details += separator + (int) (bytes[5] & 0xff) + ":" + (int) (bytes[6] & 0xff);
                    }
                    break;
                case 0x06:
                    msg = "二次提醒设置";
                    if (type == 0)
                        details = msg + separator + str4[bytes[3]];
                    break;*/
                case 0x0C:
                    msg = "个人设置(不清除数据)";
                    if (type == 0) {
                        details = msg + separator + str5[bytes[3]];
                        details += separator + getDate(bytes, 4);
                        details += separator + "身高:" + (int) (bytes[8] & 0xff);
                        details += separator + "体重:" + (int) (bytes[9] & 0xff) + (int) ((bytes[10] & 0xff) << 8);
                    }
                    break;
                case 0x0F:
                    msg = "获取电量值";
                    break;
               /* case 0x09:
                    msg = "获取初始化标志";
                    break;*/
                case 0x11:
                    msg = "恢复出厂设置";
                    break;
                case 0x12:
                    msg = "初始化个人信息";
                    if (type == 0) {
                        details = msg + separator + str5[bytes[3]];
                        details += separator + getDate(bytes, 4);
                        details += separator + "身高:" + (int) (bytes[8] & 0xff);
                        details += separator + "体重:" + (int) (bytes[9] & 0xff) + (int) ((bytes[10] & 0xff) << 8);
                    }
                    break;
                /*case 0x0C:
                    msg = "使用设置1(初始化时用)";
                    if (type == 0)
                        details = msg + separator + str6[bytes[3]];
                    break;*/
                case 0x14:
                    msg = "获取个人资料信息";
                    break;
                case 0x15:
                    msg = "设置日期时间";
                    if (type == 0) {
                        details = msg + separator + getDate(bytes, 3);
                        details += separator + getTime(bytes, 7);
                    }
                    break;
                /*case 0x0F:
                    msg = "设置目标能力值";
                    if (type == 0)
                        details = msg + separator + readDOC;
                    break;
                case 0x10:
                    msg = "获取目标能力值";
                    break;*/
                /*case 0x11:
                    msg = "使用设置修改(日常使用时设置)";
                    if (type == 0) {
                        details = msg + separator + str6[bytes[3]];
                        details += separator + str7[bytes[3]];
                    }
                    break;*/
                case 0x1B:
                    msg = "获取最新(当天汇总)的运动数据";
                    break;
                /*case 0x13:
                    msg = "升级程序信息";
                    if (type == 0)
                        details = msg + separator + readDOC;
                    break;*/
                /*case 0x1F:
                    msg = "显示元素设置";
                    if (type == 0)
                        details = msg + separator + readDOC;
                    break;*/
                case 0x30:
                    msg = "请求运动记录、睡眠数据格式总数";
                    break;
                case 0x31:
                    msg = "请求发送睡眠数据命令";
                    break;
                case 0x32:
                    msg = "请求删除运动数据、睡眠数据命令";
                    if (type == 0) {
                        details = msg + separator + str8[bytes[3]];
                    }
                    break;
                case 0x33:
                    msg = "设置、获取睡眠敏感度";
                    if (type == 0)
                        details = msg + separator + str9[bytes[3]];
                    break;
                case (byte) 0xA1:
                    msg = "返回当前运动数据命令";
                    break;
                case 0x34:
                    msg = "设置、获取小时制、距离单位显示";
                    if (type == 0)
                        details = msg + separator + readDOC;
                    break;
                case (byte) 0xB1:
                    msg = "来信提醒";
                    if (type == 0)
                        details = msg + separator + "来信条数" + (int) (bytes[3] & 0xff);
                    break;
                case (byte) 0xB2:
                    msg = "来电提醒";
                    if (type == 0)
                        details = msg + separator + readDOC;
                    break;
                case (byte) 0xB3:
                    msg = "其他信息提醒";
                    if (type == 0) {
                        details = msg + separator + str10[bytes[3]];
                        String tmptype = "";
                        switch ((int) (bytes[4] & 0xff)) {
                            case 0x01:
                                tmptype = "电子邮件";
                                break;
                            case 0x02:
                                tmptype = "未接来电";
                                break;
                            case 0x04:
                                tmptype = "日历事件";
                                break;
                            case 0x08:
                                tmptype = "短信";
                                break;
                            case 0x10:
                                tmptype = "来电";
                                break;
                            case 0x20:
                                tmptype = "社交";
                                break;
                            case 0x40:
                                tmptype = "电话结束";
                                break;
                        }
                        details += separator + tmptype;
                        details += separator + bytes[5] + "条";
                    }
                    break;
                case (byte) 0xA2:
                    msg = "设置目标步数、卡路里、目标距离";
                    if (type == 0) {
                        details = msg + separator + str11[bytes[3]];
                        details += separator + getStepCalDisData(bytes, 4);
                    }
                    break;
                case 0x1F:
                    msg = "获取单个运动数据";
                    break;
                case 0x36:
                    msg = "设置自动睡眠时间点";
                    break;
                case (byte) 0xB4:
                    msg = "设置是否开启消息通知命令";
                    break;
                case 0x43:
                    msg = "设置静坐提醒命令";
                    break;
                case 0x23:
                    msg = "设置显示格式";
                    break;
                case 0x24:
                    msg = "读取日期时间显示格式";
                    break;
                case 0x25:
                    msg = "发送初始化完成命令";
                    break;
                case 0x26:
                    msg = "来电/重拨通知";
                    break;
                case 0x27:
                    msg = "读取每天的目标设置";
                    break;
                case 0x28:
                    msg = "发送未接来电号码或姓名";
                    break;
                case (byte) 0xBA:
                    msg = "未读短信的发送者号码或发送者姓名";
                    if (type == 0) {
                        details = msg + separator + "长度:" + bytes[3];
                        details += separator + "总包数:" + bytes[4];
                        details += separator + "当前包序号:" + bytes[5];
                    }
                    break;
                case (byte) 0xB6:
                    msg = "发送未读短信内容";
                    if (type == 0) {
                        details = msg + separator + "长度:" + bytes[3];
                        details += separator + "总包数:" + bytes[4];
                        details += separator + "当前包序号:" + bytes[5];
                    }
                    break;
                case 0x2B:
                    msg = "发送未读短信的时间日期";
                    if (type == 0) {
                        details = msg + separator + getSendTime(bytes, 3);
                    }
                    break;
                case 0x2C:
                    msg = "读取睡眠模式设置";
                    break;
                case 0x2D:
                    msg = "读取音量值";
                    break;
                case 0x2E:
                    msg = "设置音量值";
                    break;
                case 0x2F:
                    msg = "查询设备端运动/睡眠模式";
                    break;
                case 0x44:
                    msg = "设置自动心率监测";
                    break;
                case 0x45:
                    msg = "上传心率数据";
                    break;
                /*case 0x32:
                    msg = "设置响铃模式";
                    break;
                case 0x33:
                    msg = "设置OLED亮度";
                    break;
                case 0x34:
                    msg = "支付:读取芯片应用卡号";
                    break;
                case 0x35:
                    msg = "支付:读取余额";
                    break;
                case 0x36:
                    msg = "支付:读取交易记录";
                    break;
                case 0x37:
                    msg = "支付:透传";
                    break;
                case 0x38:
                    msg = "设置设备端界面语言格式";
                    break;
                case 0x39:
                    msg = "设置设备端时间类型界面";
                    break;
                default:
                    msg = "没有该命令";
                    break;*/
            }


            if (type == 0) {
                Logger.i("PrintfUtils", "+----------------------------------start : (" + TAG + ")--------------------------------+");
                sendTAG = TAG;
                if (details == "") {
                    Logger.w(TAG, msg);
                    Logger.w("PrintfUtils", "   发送:" + NumberUtils.bytes2HexString(bytes) + "(" + msg + ")");
                } else {
                    Logger.w(TAG, details);
                    Logger.w("PrintfUtils", "   发送:" + NumberUtils.bytes2HexString(bytes) + "(" + details + ")");
                }
            } else {
                return msg;
            }
        } catch (Exception e) {
            return msg;
        }
        return "";
    }

    public static String printfSendMsg(String TAG, byte[] bytes) {
        return printfSendMsg(TAG, bytes, 0);
    }

    public static void printfRevMsg(String TAG, byte[] bytes) {
        try {
            String msg = "";
            String readDOC = "具体查看文档";
            String separator = " : ";
            if (bytes[0] == 0x6E && bytes[1] == 0x01) {
                switch (bytes[2]) {
                    case 0x00:
                        msg = "电池剩余量";
                        break;
                    case 0x01:
                        msg = "响应消息";
                        String command = printfSendMsg("", bytes, 1);
                        String result = "";
                        if (bytes[4] == 0x00)
                            result = "成功";
                        else if (bytes[4] == 0x01)
                            result = "失败";
                        else if (bytes[4] == 0x02)
                            result = "非法命令";
                        else if (bytes[4] == 0x03)
                            result = "没有数据 或 其他";
                        else
                            result = "未定义。。。";
                        if (bytes[4] == 0x04)
                            result = "其他结果,参考如下";
                        msg += separator + command + separator + result;
                        break;
                    case 0x02:
                        msg = "产品类型";
                        break;
                    case 0x04:
                        msg = "获取watchID";
                        break;
                    case 0x05:
                        msg = "运动数据";
                        break;
                    case 0x08:
                        msg = "固件信息";
                        break;
                    case 0x09:
                        msg = "软件版本信息";
                        break;
                    case 0x0A:
                        msg = "初始化标示";
                        break;
                    case 0x0B:
                        msg = "band的个人信息";
                        break;
                    case 0x0D:
                        msg = "目标时段能量值";
                        break;
                    case 0x0F:
                        msg = "发送当天的汇总数据";
                        break;
                    case 0x10:
                        msg = "手环启动信息";
                        break;
                    case 0x11:
                        msg = "提醒内容";
                        break;
                    case 0x12:
                        msg = "回传运动记录总数";
                        break;
                    case 0x13:
                        msg = "回传睡眠记录数据";
                        break;
                    case 0x14:
                        msg = "回传睡眠总记录数据";
                        break;
                    case 0x15:
                        msg = "睡眠记录总个数";
                        break;
                    case 0x16:
                        msg = "睡眠模式敏感度";
                        break;
                    case 0x17:
                        msg = "及时采样点运动数据";
                        break;
                    case 0x30:
                        msg = "心率数据";
                        break;
                    case 0x18:
                        msg = "当前时间制设置";
                        break;
                    case 0x19:
                        msg = "上传目标卡路里和距离";
                        break;
                    case 0x1A:
                        msg = "算法版本号";
                        break;

                    /*case 0x17:
                        msg = "返回设备端开关设置";
                        break;
                    case 0x18:
                        msg = "返回显示格式";
                        break;
                    case 0x19:
                        msg = "返回睡眠模式设置";
                        break;
                    case 0x1A:
                        msg = "返回音量值";
                        break;
                    case 0x1B:
                        msg = "返回设备端运动/睡眠模式";
                        break;
                    case 0x1C:
                        msg = "返回距离单位设置";
                        break;
                    case 0x1D:
                        msg = "返回静坐提醒设置";
                        break;
                    case 0x1E:
                        msg = "支付：返回芯片应用卡号";
                        break;
                    case 0x1F:
                        msg = "支付：返回余额";
                        break;
                    case 0x20:
                        msg = "支付：返回交易记录";
                        break;
                    case 0x21:
                        msg = "支付：透传";
                        break;
                    case 0x22:
                        msg = "返回设备端界面语言格式";
                        break;
                    case 0x23:
                        msg = "返回设备端时间类型界面";
                        break;*/
                }
            } else {
                msg = "接收剩余数据";
            }
            Logger.e(TAG, msg);
            if (TAG == sendTAG) {
                Logger.e("PrintfUtils", "   接收:" + NumberUtils.bytes2HexString(bytes) + "(" + msg + ")");
                Logger.i("PrintfUtils", "+----------------------------------end : (" + TAG + ")----------------------------------+");
                Logger.e("PrintfUtils", " ");
                Logger.e("PrintfUtils", " ");

            }
        } catch (Exception e) {
        }
    }

}
