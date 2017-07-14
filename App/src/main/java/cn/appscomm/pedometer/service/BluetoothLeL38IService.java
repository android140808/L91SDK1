package cn.appscomm.pedometer.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import apps.utils.ConfigHelper;
import apps.utils.Logger;
import apps.utils.NumberUtils;
import apps.utils.PublicData;
import cn.appscomm.pedometer.protocol.BluetoothUtil;

/**
 *
 */
public class BluetoothLeL38IService extends Service {
    private static final String TAG = "BluetoothLeL38IService";
    public static final String ACTION_DATA_AVAILABLE = PublicData.MSGHEAD + "ACTION_DATA_AVAILABLE";
    public static final String ACTION_DATA_WRITER_CALLBACK = PublicData.MSGHEAD + "ACTION_DATA_WRITER_CALLBACK";
    public static final String ACTION_HEART_DATA_AVAILABLE = PublicData.MSGHEAD + "ACTION_HEART_DATA_AVAILABLE";
    public static final String ACTION_GATT_CONNECTED = PublicData.MSGHEAD + "ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = PublicData.MSGHEAD + "ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = PublicData.MSGHEAD + "ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_GATT_SERVICES_TIMEOUT = PublicData.MSGHEAD + "ACTION_GATT_TIMEOUT";
    public static final String EXTRA_DATA = PublicData.MSGHEAD + "EXTRA_DATA";
    public static final String EXTRA_DATA_ORDER = PublicData.MSGHEAD + "EXTRA_DATA_ORDER";        // summer: add
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static final UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(HEART_RATE_MEASUREMENT);
    private static final UUID UUID_CHARACTERISTIC_1 = UUID.fromString("00008001-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHARACTERISTIC_2 = UUID.fromString("00008002-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHARACTERISTIC_2_CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_SERVICE = UUID.fromString("00006006-0000-1000-8000-00805f9b34fb");


    public static final String DEVNAME = PublicData.L38I;

    public static long lastSendCommandTime = 0L;

    private static final int STATE_CONNECTED = 2;       // 连接状态:连接
    private static final int STATE_DISCONNECTED = 0;    // 连接状态:断开

    public static boolean isConnected = false;          // 当前设备是否已经连接
    public static boolean isServiceDisvered = false;    // 是否已经发现了服务
    public static boolean isEnable_time = false;        // 是否启用计时器

    private final int SN_LEN = 20;                      // 序列号的长度
    private final int WRITETIMEOUT = 20;                // 发送数据超时时间:20相当于10秒
    private final int SCANTIMEOUT = 20000;              // 扫描超时时间:20秒
    private int timeoutCount = 0;                       // 计时次数,每计一次为500毫秒
    private static int timeOutCount2 = 0;               // 连续超时的次数
    private Timer timer1 = null;                        // 计时器
    private BluetoothAdapter mBluetoothAdapter = null;  // 蓝牙适配器
    public static boolean SendTimeOut = false;          // 蓝牙工具类连接后，会把该值设为true
    private int connectTimes = 0;                       // 重连次数
    public static int scanStatus = 0;                   // 0:扫描失败 1:扫描成功 2:扫描中
    public static boolean isSend03 = true;              // true:需要发送03 false:不需要发送03
    public static boolean isSend = true;              // true:需要发送03 false:不需要发送03

    private BluetoothGatt mBluetoothGatt = null;
    private BluetoothManager mBluetoothManager = null;
    public static BluetoothDevice bluetoothdevice = null;

    public static String mDeviceAddress = "";
    private String REG_SN = "";

    private final IBinder mBinder = new LocalBinder();
    private Handler mHandler = new Handler() {
    };

    // "GT-N7100"及以后是Note2 的版本
    public class LocalBinder extends Binder {
        public BluetoothLeL38IService getService() {
            return BluetoothLeL38IService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    /**
     * 发送广播消息
     *
     * @param msgType 消息类型
     * @param bytes   具体数据
     */
    private void broadcastUpdate(String msgType, byte bytes[]) {
        Intent intent = new Intent(msgType);
        if (bytes != null)
            intent.putExtra(EXTRA_DATA, bytes);
        sendBroadcast(intent);
    }

    /**
     * 添加需要监听广播的消息
     *
     * @return IntentFilter
     */
    public static IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(ACTION_GATT_CONNECTED);
        intentfilter.addAction(ACTION_GATT_DISCONNECTED);
        intentfilter.addAction(ACTION_GATT_SERVICES_TIMEOUT);
        intentfilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        intentfilter.addAction(ACTION_DATA_AVAILABLE);
        intentfilter.addAction(ACTION_DATA_WRITER_CALLBACK);
        return intentfilter;
    }

    public void onCreate() {
        super.onCreate();
        Logger.d(TAG, TAG + "服务创建...!!!");
        lastSendCommandTime = System.currentTimeMillis() + 20000;
        timeoutCount = 0;
        if (null == timer1) {
            timer1 = new Timer();
            timer1.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!isEnable_time) {
                        timeoutCount = 0;
                        return;
                    }
                    Logger.i("test-timeout", "距离上次发送数据，已经用时:" + (float) ((timeoutCount * 500) / 1000f) + "秒...!!!");
                    if (++timeoutCount > WRITETIMEOUT) {
                        isEnable_time = false;
                        timeOutCount2++;
                        timeoutCount = -2;
//                        Logger.i("test-timeout", "---发送的数据已超时(" + timeoutCount + "秒)，超时次数是:" + timeOutCount2);
//                        broadcastUpdate(SendTimeOut ? ACTION_GATT_SERVICES_TIMEOUT : ACTION_GATT_DISCONNECTED, null); // 已经连接的话，发送超时广播，否则发送断开连接广播
//                        if (timeOutCount2 > 1) {    // 连续超时2次，断开连接
//                            timeOutCount2 = 0;
//                            disconnect();
//                        }
                        disconnect();
                        broadcastUpdate(ACTION_GATT_DISCONNECTED, null);
                    }
                }
            }, 0, 500);
        }
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager != null) {
            Logger.d(TAG, TAG + "mBluetoothManager!=null...!!!");

            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG, TAG + "服务已销毁...!!!");

        if (null != timer1) {
            timer1.cancel();
            timer1 = null;
        }

    }


    // 蓝牙广播回调
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        /**
         * 蓝牙状态改变 用于设备返回数据到手机
         * @param bluetoothgatt
         * @param bluetoothgattcharacteristic
         */
        public void onCharacteristicChanged(BluetoothGatt bluetoothgatt, BluetoothGattCharacteristic bluetoothgattcharacteristic) {
            Logger.i(TAG, "--------------------------------------------------onCharacteristicChanged------------------------------------------------------------");

            if (mBluetoothGatt != null) {
                byte[] bytes = bluetoothgattcharacteristic.getValue();
                Logger.e(TAG, "<<<<<<<<<<获取到中设备返回的数据(8002) : " + NumberUtils.binaryToHexString(bytes));
//                Logger.i(TAG, "------------------------------------------------------------------------------------------------------------------------------------");

                if (BluetoothLeL38IService.UUID_CHARACTERISTIC_2.equals(bluetoothgattcharacteristic.getUuid())) { // 如果是8002发送完毕，发送ACTION_DATA_AVAILABLE
                    Logger.e(TAG, "BluetoothLeL38IService.UUID_CHARACTERISTIC_2：");
                    if ((bytes != null) && (bytes.length > 1)) {
                        timeOutCount2 = 0; //收到数据就复位
                    }
                    timeoutCount = 0;
                    isEnable_time = false;
                    BluetoothUtil.getInstance().enableSendDataFlag();
                    Log.e(TAG, "开始发送数据");
                    broadcastUpdate(ACTION_DATA_AVAILABLE, bytes);
                    Log.e(TAG, "发送数据结束");
//                    BluetoothUtil.getInstance().setIsSendDataFlag();
                }

                if (UUID_HEART_RATE_MEASUREMENT.equals(bluetoothgattcharacteristic.getUuid())) {
                    Logger.e(TAG, "UUID_HEART_RATE_MEASUREMENT.equals：");
                    sendBroadcastUpdate(ACTION_HEART_DATA_AVAILABLE, bluetoothgattcharacteristic);
                }
            }
        }

        // 发送心率数据
        private void sendBroadcastUpdate(String action, BluetoothGattCharacteristic characteristic) {
//            Logger.e(TAG, "-->>uuid  " + characteristic.getUuid());
            if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//			Logger.e(TAG, "-->>2001");
                int flag = characteristic.getProperties();
                int format = -1;
                if ((flag & 0x01) != 0) {
                    format = BluetoothGattCharacteristic.FORMAT_UINT16;
                    Logger.e(TAG, "Heart rate format UINT16.");
                } else {
                    format = BluetoothGattCharacteristic.FORMAT_UINT8;
//                    Logger.e(TAG, "Heart rate format UINT8.");
                }
                final int heartRate = characteristic.getIntValue(format, 1);
                Logger.e(TAG, format + "   " + String.format("Received heart rate: %d", heartRate));

                Intent intent2 = new Intent(action);
//				intent2.putExtra(EXTRA_DATA, String.valueOf(heartRate));
                intent2.putExtra(EXTRA_DATA, heartRate);
                Logger.e(TAG, "发送了心率的广播啦！请注意接收！");
                sendBroadcast(intent2);
            }
        }

        /**
         * 读回调
         * @param bluetoothgatt
         * @param bluetoothgattcharacteristic
         * @param i
         */
        public void onCharacteristicRead(BluetoothGatt bluetoothgatt, BluetoothGattCharacteristic bluetoothgattcharacteristic, int i) {
            Logger.d(TAG, "==>>onCharacteristicRead(系统返回读回调)");
            if (mBluetoothGatt == null)
                return;

            if (i == BluetoothGatt.GATT_SUCCESS) {
                sendBroadcastUpdate(ACTION_HEART_DATA_AVAILABLE, bluetoothgattcharacteristic);
            }
        }

        /**
         * 写回调
         * @param bluetoothgatt
         * @param bluetoothgattcharacteristic
         * @param i
         */
        public void onCharacteristicWrite(BluetoothGatt bluetoothgatt, BluetoothGattCharacteristic bluetoothgattcharacteristic, int i) {
            Logger.d(TAG, "==>>onCharacteristicWrite(系统返回写回调)");
            broadcastUpdate(ACTION_DATA_WRITER_CALLBACK, null); // 发送写回调
            if (mBluetoothGatt != null && BluetoothLeL38IService.UUID_CHARACTERISTIC_1.equals(bluetoothgattcharacteristic.getUuid())) {
                if (!continueSendBytes() && isSend03) { // 如果是大字节数组，则继续发送
                    confirmByWriting0x03ToCharacteristic2();
                }
            }
        }

        // 在8002通道发送03到设备
        private void confirmByWriting0x03ToCharacteristic2() {
            Logger.d(TAG, "==>>命令已经发送到设备了，写03到设备结束...");
            if (mBluetoothGatt != null) {
                BluetoothGattCharacteristic bluetoothgattcharacteristic = mBluetoothGatt.getService(BluetoothLeL38IService.UUID_SERVICE).getCharacteristic(BluetoothLeL38IService.UUID_CHARACTERISTIC_2);
                bluetoothgattcharacteristic.setValue(new byte[]{0x03});
                mBluetoothGatt.writeCharacteristic(bluetoothgattcharacteristic);
            }
        }

        /**
         * 连接状态回调
         * @param bluetoothgatt
         * @param state
         * @param newState
         */
        @SuppressLint("NewApi")
        public void onConnectionStateChange(BluetoothGatt bluetoothgatt, int state, int newState) {
            // 断开连接
            if (newState == STATE_DISCONNECTED) {
                Logger.i(TAG, "xxxxxxxxxxxxx连接状态回调(state=" + state + " newState=" + newState + " 断开连接)");
                disconnect();
                broadcastUpdate(ACTION_GATT_DISCONNECTED, null);
            }

            // 已经连接
            else if ((newState == STATE_CONNECTED) && (state == 0)) {
                Logger.i(TAG, "==>>1、连接状态回调(state=" + state + " newState=" + newState + " (" + "已连接),准备发现服务...!!!)");
                SendTimeOut = false;
                isConnected = true;
                isServiceDisvered = false;
                timeoutCount = -10;
                connectTimes = 0;
                broadcastUpdate(ACTION_GATT_CONNECTED, null);
                mBluetoothGatt.discoverServices();
            }

            // 设备发送连接请求失败回调(此处断开一直重连)
            else if ((state == 133) && (newState == 2)) {
                Logger.i(TAG, "+++++++++++++连接状态回调(state=" + state + " newState=" + newState + " 未连接到设备,准备重新连接)");
                connectTimes++;
                Logger.d(TAG, "重新连接次数 : " + connectTimes);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        connect(mDeviceAddress);
                    }
                }, 3000);
            }
        }

        // 发现BluetoothGatt服务
        @Override
        public void onServicesDiscovered(BluetoothGatt bluetoothgatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                timeoutCount = -6;
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Logger.i(TAG, "==>>2、已发现服务(onServicesDiscovered),准备打开8002监听...!!!");
                enableNotificationForCharacteristic2();

                mHandler.postDelayed(new Runnable() {        // 延时监听心率以防失败
                    @Override
                    public void run() {
                        enableNotificationForHeartRateCharacteristic();
                    }
                }, 10000);
            } else {
                Logger.i(TAG, "==>>onServicesDiscovered,有异常...!!!");
            }
        }

        // 打开8002监听，让设备可以通过特征8002发送数据到手机
        private void enableNotificationForCharacteristic2() {
            if (mBluetoothGatt != null) {
                try {
                    Logger.i("", "enableNotificationForCharacteristic2");
                    BluetoothGattCharacteristic bluetoothgattcharacteristic = mBluetoothGatt.getService(BluetoothLeL38IService.UUID_SERVICE).getCharacteristic(BluetoothLeL38IService.UUID_CHARACTERISTIC_2);
                    mBluetoothGatt.setCharacteristicNotification(bluetoothgattcharacteristic, true);

                    BluetoothGattDescriptor bluetoothgattdescriptor = bluetoothgattcharacteristic.getDescriptor(BluetoothLeL38IService.UUID_CHARACTERISTIC_2_CONFIG_DESCRIPTOR);
                    bluetoothgattdescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(bluetoothgattdescriptor);
                } catch (Exception e) {
                }
            }
        }

        private void enableNotificationForHeartRateCharacteristic() {
            Logger.e(TAG, "设置心率通知");
            if (mBluetoothGatt == null)
                return;
            try {
                BluetoothGattCharacteristic bluetoothgattcharacteristic2 = mBluetoothGatt.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")).getCharacteristic(BluetoothLeL38IService.UUID_HEART_RATE_MEASUREMENT);
                boolean setNotificationResult = mBluetoothGatt.setCharacteristicNotification(bluetoothgattcharacteristic2, true);
                Logger.e(TAG, "==>>enableNotificationForHeartRateCharacteristic setCharacteristicNotification " + setNotificationResult);

                BluetoothGattDescriptor descriptor = bluetoothgattcharacteristic2.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                if (descriptor != null) {
                    Logger.e(TAG, "==>>enableNotificationForHeartRateCharacteristic getDescriptor success");
                    boolean setValue = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    Logger.e(TAG, "==>>enableNotificationForHeartRateCharacteristic descriptor.setValue " + setValue);

                    boolean notifyHRResult = mBluetoothGatt.writeDescriptor(descriptor);

                    Logger.e(TAG, "==>>enableNotificationForHeartRateCharacteristic mBluetoothGatt.writeDescriptor " + notifyHRResult);
                    if (notifyHRResult) {
                    } else {
                        mHandler.postDelayed(new Runnable() {        // 延时监听心率以防失败
                            @Override
                            public void run() {
                                enableNotificationForHeartRateCharacteristic();
                            }
                        }, 3000);
                    }
                }

            } catch (Exception e) {
                Logger.e(TAG, "==>>enableNotificationForHeartRateCharacteristic failed---exception" + e);
                e.printStackTrace();
            }

        }

        public void onDescriptorWrite(BluetoothGatt bluetoothgatt, BluetoothGattDescriptor bluetoothgattdescriptor, int i) {
            Logger.i(TAG, "==>>3、已打开8002监听(onDescriptorWrite),准备发送Discovered广播...!!!");
            bluetoothgatt.readDescriptor(bluetoothgattdescriptor);
        }

        public void onDescriptorRead(BluetoothGatt bluetoothgatt, BluetoothGattDescriptor bluetoothgattdescriptor, int i) {
            if (BluetoothLeL38IService.UUID_CHARACTERISTIC_2_CONFIG_DESCRIPTOR.equals(bluetoothgattdescriptor.getUuid())) {
                isServiceDisvered = true;
                Logger.i(TAG, "==>>4、已经连接完毕(onDescriptorRead),发送Discovered广播...!!!");
                ConfigHelper.setSharePref(getApplicationContext(), PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.IS_UPDATE, false);
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, null);
//                connect2(mDeviceAddress);
            } else {
                Logger.i(TAG, "==>>onDescriptorRead,有异常...!!!");
            }
        }
    };

    /**
     * 写数据到设备
     *
     * @param bytes 具体的数据
     */
    public void writeDataToDevice(byte bytes[]) {
        lastSendCommandTime = System.currentTimeMillis();
        if (bytes != null) {
            if (mBluetoothGatt != null) {
                isEnable_time = true;       // 发送数据到设备,启用计时器
                timeoutCount = 0;
                BluetoothGattCharacteristic bluetoothgattcharacteristic = null;
                try {
                    Logger.w(TAG, ">>>>>>>>>>>>>>>>>>>>写数据到设备(8001) : " + NumberUtils.binaryToHexString(bytes));
                    bluetoothgattcharacteristic = mBluetoothGatt.getService(UUID_SERVICE).getCharacteristic(UUID_CHARACTERISTIC_1);
                    bluetoothgattcharacteristic.setValue(bytes);
                    bluetoothgattcharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    mBluetoothGatt.writeCharacteristic(bluetoothgattcharacteristic);
                } catch (Exception e) {
                }
            }
        }
    }

    public void close() { // 这个调用接口是用来给以前短连接用的，
        onDestroy();
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        Logger.i(TAG, "disconnect");

        SendTimeOut = false;
        sendBytes = null;
        sendBytesPacketCount = 0;
        timeoutCount = -2;
        isConnected = false;
        isServiceDisvered = false;
        if (null != timer1) {
            timeoutCount = 0;
            timeOutCount2 = 0;
            isEnable_time = false;
        }
        if (mBluetoothGatt != null) {
            try {
                Logger.i(TAG, "disconnect");
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            } catch (Exception e) {
            }
        }
    }

    /**
     * 真正关闭蓝牙
     */
    public void real_close() {
        isConnected = false;
        isServiceDisvered = false;
        if (null != timer1) {
            timer1.cancel();
            timer1 = null;
        }
        if (mBluetoothGatt != null) {
            try {
                Logger.i(TAG, "real_close");
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            } catch (Exception e) {
            }
        }
    }

//    /**
//     * 连接设备1
//     *
//     * @param s              mac地址
//     * @param mcontext
//     * @param isShowBindStep 如果没有绑定，是否需要跳转到设置界面 true：需要 false：不需要
//     * @return
//     */
//    @SuppressLint("NewApi")
//    public boolean connect(String s, Context mcontext, boolean isShowBindStep) {
//
//        if ((null == s) || "".equals(s)) { // mac地址为空，需要重新查找并配对
//            if (!PublicData.isBindingPedometer(mcontext, isShowBindStep)) {
//                return false;
//            }
//        }
//        Logger.i("","mDeviceAddress"+s);
//        connectTimes = 1;
//        return connect2(s);
//    }

    /**
     * 通过mac地址连接设备
     *
     * @param s
     * @return
     */
    @SuppressLint("NewApi")
    public synchronized boolean connect(String s) {
        Logger.i("", "connect=");
        if (!PublicData.checkBluetoothStatus()) return false;

        Logger.i("", "connect=" + s);
        if (TextUtils.isEmpty(s)) {
//            deviceName = (String) ConfigUtil.getSPValue(ConfigNames.SP_DEVICE_NAME, ConfigUtil.DATA_STRING);
            REG_SN = (String) ConfigHelper.getSharePref(getApplicationContext(), PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.CURRENT_BIND_ID_ITEM, 1);
            Logger.i("", "connect=");

            if (!TextUtils.isEmpty(REG_SN)) {
                Logger.i(TAG, "MAC地址为空,现在开始扫描连接(" + REG_SN + ")...!");
                scanLeDevice(true);
            }
            return false;
        } else if (scanStatus == 2) {
            Logger.i("", "connect=");

            scanLeDevice(false);
        }
        boolean flag = true;
        mDeviceAddress = s;
        Logger.i("", "connect=");

        if (mBluetoothAdapter == null) {
            flag = false;
        } else {
            if ((mBluetoothGatt != null)) {
                try {
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mBluetoothGatt = null;
            }
            Logger.i("", "connect=");

            bluetoothdevice = mBluetoothAdapter.getRemoteDevice(s);
            if (bluetoothdevice == null) {
                flag = false;
            } else {
                timeoutCount = -8;
                synchronized (bluetoothdevice) {
                    mBluetoothGatt = bluetoothdevice.connectGatt(this, android.os.Build.VERSION.SDK_INT >= 19 ? false : true, mGattCallback);
                }
                Logger.w(TAG, "-------------连接设备(通过mac地址连接设备,mac : " + mDeviceAddress + "   绑定状态是 : " + bluetoothdevice.getBondState() + ")");
            }
        }
        return flag;
    }
//    /**
//     * 通过mac地址连接设备
//     *
//     * @param s
//     * @return
//     */
//    @SuppressLint("NewApi")
//    public boolean connect2(String s) {
//        if ((null == s) || "".equals(s)) {
//            REG_SN = (String) ConfigHelper.getSharePref(getApplicationContext(), PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.CURRENT_BIND_ID_ITEM, 1);
//            scanLeDevice(true);
//            s = "FF:FF:FF:FF:FF:FF";
//            return false;
//        }
//        Logger.i(TAG, "-------------REG_SN=!null "+REG_SN);
//
//        boolean flag = true;
//        mDeviceAddress = s;
//
//        if (mBluetoothAdapter == null){
//            flag = false;}
//        else {
//            if ((mBluetoothGatt != null)) {
//                Logger.i(TAG, "-------------mBluetoothGatt=!null ");
//                try {
//                    mBluetoothGatt.disconnect();
//                    mBluetoothGatt.close();
//                    Thread.sleep(500);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                mBluetoothGatt = null;
//            }
//            Logger.i("","mDeviceAddress"+mDeviceAddress);
//            bluetoothdevice = mBluetoothAdapter.getRemoteDevice(s);
//            if (bluetoothdevice == null) {
//                Logger.i(TAG, "-------------bluetoothdevice==null ");
//
//                flag = false;
//            } else {
//                timeoutCount = -8;
//                Logger.i(TAG, "------------- android.os.Build.VERSION.SDK_INT=" + (android.os.Build.VERSION.SDK_INT >= 19 ? false : true));
//                mBluetoothGatt = bluetoothdevice.connectGatt(GlobalApp.globalApp.getApplicationContext(), android.os.Build.VERSION.SDK_INT >= 19 ? false : true, mGattCallback);
//
//                Logger.i(TAG, "-------------连接设备(通过mac地址连接设备,mac : " + mDeviceAddress + "   绑定状态是 : " + bluetoothdevice.getBondState() + ")");
//            }
//        }
//        return flag;
//    }

    /**
     * 重新扫描或停止扫描
     *
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            Logger.i(TAG, "准备扫描设备...");
            scanStatus = 2;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanStatus = scanStatus == 2 ? 0 : 1;
                    Logger.i(TAG, "超时:停止扫描,扫描状态是 : " + scanStatus + "(0:扫描失败 1:扫描成功 2:扫描中)");
                    if (scanStatus == 0 && TextUtils.isEmpty(REG_SN)) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }
                }
            }, SCANTIMEOUT);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            scanStatus = scanStatus == 2 ? 0 : 1;
            Logger.i(TAG, "手动:停止扫描,扫描状态是 : " + scanStatus + "(0:扫描失败 1:扫描成功 2:扫描中)");
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /**
     * 扫描回调，这里会通过devname和watchid拼接出devname进行比对，比对通过则直接连接设备
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @SuppressLint("NewApi")
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            {
                try {
                    String devname = device.getName();
//                    Logger.i(TAG, "device.getUuids=" + device.getUuids());
//                    Logger.i(TAG, "device.getUuids=" + devname);
//                    Logger.i(TAG, "device.getdevice=" + device.getName() + " /mac=" + device.getAddress());
                    // 搜索是否找到L28的蓝牙设备
//                    if (!TextUtils.isEmpty(devname) && REG_SN.equals(devname)) {
//                        Logger.w(TAG, "找到该设备了 设备名称是:" + REG_SN + " MAC:" + device.getAddress());
//                        scanStatus = 1;
//                        mDeviceAddress = device.getAddress();
////
////                        ConfigUtil.setSPValue(ConfigNames.SP_MAC, mDeviceAddress);
////                        ConfigUtil.setSPValue(ConfigNames.SP_DEVICE_NAME, devname);
//                        ConfigHelper.setSharePref(getApplicationContext(),
//                                PublicData.SHARED_PRE_SAVE_FILE_NAME,
//                                PublicData.MAC_KEY, mDeviceAddress);
//                        ConfigHelper.setSharePref(getApplicationContext(),        // summer: add
//                                PublicData.SHARED_PRE_SAVE_FILE_NAME,
//                                PublicData.DEVICE_NAME, devname);
//                        scanLeDevice(false);
//                        mHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                connect(mDeviceAddress);
//                            }
//                        }, 1000);
//                    }

                    if (REG_SN.length() < 20) return;
                    if (null != devname
                            && devname.length() > 8
                            //&& DEVNAME.toUpperCase().equals(devname.toUpperCase().subSequence(0, BluetoothLeService.DEVNAME.length()))
                            && ((mDeviceAddress != null && mDeviceAddress.equals(device.getAddress())) || "".equals(mDeviceAddress))
                            && (REG_SN.toUpperCase().substring(15, 20)
                            .equals(devname.toUpperCase().substring(
                                    devname.length() - 5,
                                    devname.length())))) {

                        mDeviceAddress = device.getAddress();
                        Logger.d(TAG, ">>>>>>>>>>>>3333" + mDeviceAddress);

                        ConfigHelper.setSharePref(getApplicationContext(),
                                PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                PublicData.MAC_KEY, mDeviceAddress);
//                        ConfigHelper.setSharePref(getApplicationContext(),        // summer: add
//                                PublicData.SHARED_PRE_SAVE_FILE_NAME,
//                                PublicData.DEVICE_NAME, devname);

                        scanLeDevice(false);

                        mHandler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub

                                connect(mDeviceAddress);
                            }
                        }, 1000);

                    }

                } catch (Exception e) {
                }


            }

        }
    };

    public void sendDataToPedometer(byte abyte0[]) {
        sendLargeBytes(abyte0);
    }

    /**
     * 发送多字节包使用该接口
     */
    private byte[] sendBytes = null;                // 发送的字节数组
    private int sendBytesPacketCount = 0;           // 发送的包数

    public void sendLargeBytes(byte[] bytes) {
        sendBytes = null;
        sendBytesPacketCount = 0;
        if (bytes != null) {
            sendBytes = bytes;
            if (bytes.length <= 20) {               // 小包直接发走
                writeDataToDevice(bytes);
                return;
            }
            sendBytesPacketCount = sendBytes.length / 20 + (sendBytes.length % 20 == 0 ? 0 : 1);
            byte[] firstBytes = new byte[20];
            System.arraycopy(sendBytes, 0, firstBytes, 0, 20);
            Logger.i("test-sendLargeBytes", "大字节数组发送第一包:" + NumberUtils.binaryToHexString(firstBytes) + " 共" + sendBytesPacketCount + "包数据!!!");
            sendBytesPacketCount--;                 // 发了一包，总数减一
            writeDataToDevice(firstBytes);          // 先发第一包
        }
    }

    /**
     * 继续发送数据
     *
     * @return false(已经发送完毕) true(继续发送)
     */
    private boolean continueSendBytes() {
        if (sendBytesPacketCount != 0) {
            byte[] tmpBytes = null;
            if (sendBytesPacketCount == 1) {        // 是否最后一包，最后一包需检查要发送的字节数
                Logger.w("test-sendLargeBytes", "还有最后一包没有发...");
                tmpBytes = new byte[sendBytes.length % 20 == 0 ? 20 : (sendBytes.length % 20)];
            } else {
                Logger.w("test-sendLargeBytes", "还有" + sendBytesPacketCount + "包没有发!!!");
                tmpBytes = new byte[20];
            }
            int count = sendBytes.length / 20 + (sendBytes.length % 20 == 0 ? 0 : 1);
            int index = count - sendBytesPacketCount;
            Logger.w("test-sendLargeBytes", "index : " + index + "   len : " + tmpBytes.length + "   totallen : " + sendBytes.length);
            System.arraycopy(sendBytes, 20 * index, tmpBytes, 0, tmpBytes.length);
            Logger.w("test-sendLargeBytes", "包数据是：" + NumberUtils.binaryToHexString(tmpBytes));
            sendBytesPacketCount--;                 // 每发一包，减一包
            writeDataToDevice(tmpBytes);
            return true;
        }
        return false;
    }
}

