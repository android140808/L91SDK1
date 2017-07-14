package apps.utils;

import android.content.Context;

import java.util.LinkedList;

import cn.appscomm.pedometer.interfaces.IAboutUpgrade;
import cn.appscomm.pedometer.model.UpgradeInfo;
import cn.appscomm.pedometer.service.DFUUpdateService;

public class UpgradeUtils {
    private static final String TAG = "UpgradeUtils";

    public static final int UPDATE_SUCCESS = 0;
    public static final int UPDATE_ING = 1;
    public static final int UPDATE_FAILD = 2;

    private final byte SEND_PACKAGE_COUNT = 0x14; // 一次发送的包数

    LinkedList<OrderInfo> sendOrders = new LinkedList<OrderInfo>(); // 所有要升级的命令
    private int max = -1; // 总包数,用于回调给调用者,计算进度

    private Context context;
    private boolean isUp09 = true;
    private DFUUpdateService dfuUpdateService;

    private static UpgradeUtils upgradeUtils = new UpgradeUtils(); // 单例

    private UpgradeUtils() {
    }

    class OrderInfo {
        /**
         * 内容
         */
        public byte[] content;
        /**
         * 是否1531特征通道
         */
        public boolean is1531Flag;
        /**
         * 备注
         */
        public String note;

        public OrderInfo(byte[] content, boolean is1531Flag, String note) {
            this.content = content;
            this.is1531Flag = is1531Flag;
            this.note = note;
        }
    }

    /**
     * 获取实例
     *
     * @return context和mBluetoothLeService都不为null的情况下, 才返回实例
     */
    public static UpgradeUtils getInstance() {
        return upgradeUtils;
    }

    /**
     * 开始升级
     *
     * @return true:可以升级 false:传入的参数有误，不可以升级
     */
    public boolean startUpgrade(Context context, DFUUpdateService dfuUpdateService) {
        if (context != null && dfuUpdateService != null) {
            this.context = context;
            this.dfuUpdateService = dfuUpdateService;
            sendDatasToDevice();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 整理升级命令
     *
     * @param nordicUpgradeInfos    Nordic的升级信息(Bin的文件长度、内容、CRC校验)
     * @param freescaleUpgradeInfos Freescale的升级信息(Bin的文件长度、内容、CRC校验)
     * @param upgradeMode           升级模式
     * @return 命令的长度 0:错误 >0:命令的条数
     */
    public int proOrders(UpgradeInfo nordicUpgradeInfos, UpgradeInfo freescaleUpgradeInfos, byte[] binLengths, int upgradeMode) {
        sendOrders.clear();
        if (nordicUpgradeInfos != null || freescaleUpgradeInfos != null) {
            if (upgradeMode == 1) { // 仅升级Nordic
                baseOrders(nordicUpgradeInfos.binLength, nordicUpgradeInfos.crcCheck, nordicUpgradeInfos.binContents, binLengths, true, (byte) upgradeMode);
            } else if (upgradeMode == 2) { // 仅升级Freescale
                baseOrders(freescaleUpgradeInfos.binLength, freescaleUpgradeInfos.crcCheck, freescaleUpgradeInfos.binContents, binLengths, false, (byte) upgradeMode);
            } else if (upgradeMode == 3) { // 升级Nordic和Freescale
                baseOrders(freescaleUpgradeInfos.binLength, freescaleUpgradeInfos.crcCheck, freescaleUpgradeInfos.binContents, binLengths, false, (byte) upgradeMode); // 后Freescale
                baseOrders(nordicUpgradeInfos.binLength, nordicUpgradeInfos.crcCheck, nordicUpgradeInfos.binContents, binLengths, true, (byte) upgradeMode); // 先Nordic
            }
        }
        max = sendOrders.size();
        return max;
    }

    /**
     * 整理升级命令
     *
     * @param nordicUpgradeInfos    Nordic的升级信息(Bin的文件长度、内容、CRC校验)
     * @param freescaleUpgradeInfos Freescale的升级信息(Bin的文件长度、内容、CRC校验)
     * @param upgradeMode           升级模式
     * @return 命令的长度 0:错误 >0:命令的条数
     */
    public int proOrders(UpgradeInfo nordicUpgradeInfos, UpgradeInfo freescaleUpgradeInfos, UpgradeInfo heartUpgradeInfos, byte[] binLengths, int upgradeMode) {
        sendOrders.clear();
        if (nordicUpgradeInfos != null || freescaleUpgradeInfos != null || heartUpgradeInfos != null) {
            if (upgradeMode == 1) { // 仅升级Nordic
                //全使用三个，不过不需要升级的就传空进来
                boolean isNordicFlag = false;

                if (heartUpgradeInfos != null) {
                    if (freescaleUpgradeInfos != null || nordicUpgradeInfos != null) {
                        isNordicFlag = false;
                    } else {
                        isNordicFlag = true;
                    }
                    baseOrders(heartUpgradeInfos.binLength, heartUpgradeInfos.crcCheck, heartUpgradeInfos.binContents, binLengths, false, (byte) 0x20);
                }
                if (freescaleUpgradeInfos != null) {
                    if (heartUpgradeInfos != null || nordicUpgradeInfos != null) {
                        isNordicFlag = false;
                    } else {
                        isNordicFlag = true;
                    }
                    Logger.i("","1");
                    baseOrders(freescaleUpgradeInfos.binLength, freescaleUpgradeInfos.crcCheck, freescaleUpgradeInfos.binContents, binLengths, false, (byte) 0x40);
                }


                if (nordicUpgradeInfos != null) {
                    Logger.i("","2");
                    baseOrders(nordicUpgradeInfos.binLength, nordicUpgradeInfos.crcCheck, nordicUpgradeInfos.binContents, binLengths, true, (byte) 0x04);
                }

            }
//            else if (upgradeMode == 2) { // 仅升级Freescale
//                baseOrders(freescaleUpgradeInfos.binLength, freescaleUpgradeInfos.crcCheck, freescaleUpgradeInfos.binContents,binLengths, false, (byte)upgradeMode);
//            } else if (upgradeMode == 3) { // 升级Nordic和Freescale
//                baseOrders(freescaleUpgradeInfos.binLength, freescaleUpgradeInfos.crcCheck, freescaleUpgradeInfos.binContents,binLengths, false, (byte)upgradeMode); // 后Freescale
//                baseOrders(nordicUpgradeInfos.binLength, nordicUpgradeInfos.crcCheck, nordicUpgradeInfos.binContents,binLengths, true,(byte) upgradeMode); // 先Nordic
//            }
        }
        max = sendOrders.size();
        return max;
    }

    /**
     * Nordic或Freescale命令整理
     *
     * @param binLength    Nordic或Freescale Bin的长度
     * @param crcCheck     Nordic或Freescale CRC校验
     * @param binContents  Nordic或Freescale Bin的内容
     * @param isNordicFlag true:整理的是Nordic false:整理的是Freescale
     * @param upgradeMode  升级模式(1:仅Nordic 2:仅Freescale 3:Nordic和Freescale)
     */
    private void baseOrders(byte[] binLength, byte[] crcCheck, byte[] binContents, byte[] binLengths, boolean isNordicFlag, byte upgradeMode) {

//        PublicData.nordicUpVersion
//                upgradeMode
        if (upgradeMode == (byte) 0x04) {
            if (PublicData.heartrateUpVersion || PublicData.resMapUpVersion) {
                isUp09 = false;
            }else{
                isUp09 = true;

            }
        } else if (upgradeMode == (byte) 0x40) {
            if (PublicData.heartrateUpVersion) {
                isUp09 = false;
            }else{
                isUp09 = true;
            }
        } else {
            isUp09 = true;
        }
        if (isUp09) {
            // 09
            sendOrders.addLast(new OrderInfo(new byte[]{0x09}, true, ""));

            //这个要更改这个是所有bin的大小还有就是字库的地址
            sendOrders.addLast(new OrderInfo(binLengths, false, "BIN_LENGTH_W"));

            Logger.i("", "binLengths=" + binLengths[0]);
            Logger.i("", "binLengths=" + binLengths[1]);
            Logger.i("", "binLengths=" + binLengths[2]);
            Logger.i("", "binLengths=" + binLengths[3]);
        }

//        PublicData.resMapUpVersion
//         PublicData.heartrateUpVersion


        // 01 08
        sendOrders.addLast(new OrderInfo(new byte[]{0x01, upgradeMode}, true, ""));

        // bin文件大小
        sendOrders.addLast(new OrderInfo(binLength, false, "BIN_LENGTH"));

        // 02 00
        sendOrders.addLast(new OrderInfo(new byte[]{0x02, 0x00}, true, ""));

        // CRC校验值
        sendOrders.addLast(new OrderInfo(crcCheck, false, "CRC"));

        // 02 01
        sendOrders.addLast(new OrderInfo(new byte[]{0x02, 0x01}, true, ""));

        // 08 发送包数 00
        sendOrders.addLast(new OrderInfo(new byte[]{0x08, SEND_PACKAGE_COUNT, 0x00}, true, ""));

        // 03
        sendOrders.addLast(new OrderInfo(new byte[]{0x03}, true, ""));

        // bin内容
        if (binContents != null) {
            int binContentLen = binContents.length;
            int onePackageLen = SEND_PACKAGE_COUNT * 20;
            int totalPackageCount = binContentLen % onePackageLen == 0 ? binContentLen / onePackageLen : (binContentLen / onePackageLen) + 1;
            Logger.i(TAG, "内容总长度为:" + binContentLen + "   每包发送的最大长度为:" + onePackageLen + "   总包数为:" + totalPackageCount);
            byte[] tempBytes = null;
            int tempLen = 0;
            for (int i = 0; i < totalPackageCount; i++) {
                tempLen = ((i + 1) == totalPackageCount) ? binContentLen - (onePackageLen * i) : onePackageLen;
                tempBytes = new byte[tempLen];
                System.arraycopy(binContents, onePackageLen * i, tempBytes, 0, tempLen);
                sendOrders.addLast(new OrderInfo(tempBytes, false, "BIN_CONTENT"));
            }
        }

        // 04
        sendOrders.addLast(new OrderInfo(new byte[]{0x04}, true, "ASK_FOR_CHECK_CRC"));

        // 05
        if (isNordicFlag) { // 当升级模式为全部升级，并且目前是整理Freescale的，则不需要重启命令
            sendOrders.addLast(new OrderInfo(new byte[]{0x05}, true, ""));
        }
        Logger.i("","sendOrders="+sendOrders.toString());

    }

    /**
     * 解析数据
     *
     * @param bytes 回调时传入null,设备返回时传入具体数据
     * @return 0:升级成功 1:正在升级 2:升级错误
     */

    public int parseRevDatas(byte[] bytes) {
        if (sendOrders.size() > 0) { // 集合里还有命令才需要解析
            String note = sendOrders.getFirst().note;
            byte[] content = sendOrders.getFirst().content;
            // 回调返回
            if (bytes == null) {
                if (note.equals("BIN_LENGTH") ||note.equals("BIN_LENGTH_W") || note.equals("BIN_CONTENT") || note.endsWith("ASK_FOR_CHECK_CRC")) { // 需要等设备返回的命令
                    // Logger.i(TAG, "不需要继续发送，等设备返回数据");
                } else if (content.length == 1 && content[0] == 0x05) { // 05
                    sendOrders.clear();
                    if (context instanceof IAboutUpgrade && max > 0) {
                        ((IAboutUpgrade) context).curUpgradeProgress(max, max - sendOrders.size());
                    }
                    Logger.i(TAG, "升级完毕...!!!");
                    return UPDATE_SUCCESS;
                } else { // 其他命令
                    Logger.i(TAG, "继续发送...");
                    sendOrders.removeFirst();
                    sendDatasToDevice();
                }
            }
            // 设备返回数据
            else {
                Logger.i(TAG, "<<<<<<<<<<接收:" + NumberUtils.binaryToHexString(bytes));
                // (10 01 01) && (10 02 01) && (10 03 01) && (10 04 01)
                if (bytes.length == 3 && bytes[0] == 0x10 && bytes[2] == 0x01) {
                    if ((bytes[1] == 0x01 && note.equals("BIN_LENGTH"))
                            // || (bytes[1] == 0x02 && note.equals("02 01"))
                            || (bytes[1] == 0x03) || (bytes[1] == 0x04 && note.equals("ASK_FOR_CHECK_CRC"))) {
                        sendOrders.removeFirst();
                        sendDatasToDevice();
                    }
                    if ((bytes[1] == 0x09)) {
//                        byte[] con = sendOrders.getFirst().content;
//                        Logger.i(TAG, "con长度=" + con.length);
//
//                        if (con.length != 2) {
                            sendOrders.removeFirst();
//                        }
                        sendDatasToDevice();
                    }
                }
                // 11 xx xx xx xx
                else if (bytes[0] == 0x11) {
                    sendOrders.removeFirst();
                    sendDatasToDevice();
                } else {
                    Logger.i(TAG, "存在错误，错误结果为:" + NumberUtils.bytes2BinaryStr(bytes) + " 上次发送的命令是:" + NumberUtils.binaryToHexString(sendOrders.getFirst().content));
                    return UPDATE_FAILD;
                }
            }
            return UPDATE_ING;
        } else {
            return UPDATE_FAILD;
        }
    }

    /**
     * 发送数据到设备,顺便把进度回调给apk
     */
    private void sendDatasToDevice() {
        byte[] content = sendOrders.getFirst().content;
        Logger.i(TAG, ">>>>>>>>>>发送(" + sendOrders.size() + "):" + NumberUtils.binaryToHexString(content));
        Logger.i(TAG, ">>>>>>>>>>发送(" + sendOrders.size() + "):" + content[0]);
        Logger.i(TAG, ">>>>>>>>>>发送(" + sendOrders.size() + "):" + NumberUtils.binaryToHexString(content));

        dfuUpdateService.sendDataToPedometer(content, sendOrders.getFirst().is1531Flag);
        if (context instanceof IAboutUpgrade && max > 0) {
            ((IAboutUpgrade) context).curUpgradeProgress(max, max - sendOrders.size());
        }
    }
}
