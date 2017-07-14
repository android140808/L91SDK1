package cn.appscomm.pedometer.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import apps.utils.ConfigHelper;
import apps.utils.Logger;
import apps.utils.NumberUtils;
import apps.utils.PublicData;

public class DFUUpdateService extends Service {
    public static final String MSGHEAD = "cn.appscomm.pedometer.";
    public static final String ACTION_DATA_AVAILABLE = MSGHEAD + "ACTION_DATA_AVAILABLE";
    public static final String ACTION_GATT_CONNECTED = MSGHEAD + "ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = MSGHEAD + "ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = MSGHEAD + "ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_GATT_SERVICES_TIMEOUT = MSGHEAD + "ACTION_GATT_TIMEOUT";
    public static final String EXTRA_DATA = MSGHEAD + "EXTRA_DATA";
    public static final String EXTRA_DATA_ORDER = MSGHEAD + "EXTRA_DATA_ORDER";
    public static final String ACTION_GATT_ONCHARACTERISTICWRITE = MSGHEAD + "ACTION_GATT_ONCHARACTERISTICWRITE";

    private static final int STATE_CONNECTED = 2;
    private static final int STATE_DISCONNECTED = 0;
    private static boolean NEED_BOND_FIRST = false; // 每次通讯前是否必须要求配对，
    public static boolean isConnected = false; // 当前设备是否已经连接
    public static boolean isServiceDisvered = false; // 是否已经发线了服务
    public static boolean isEnable_time = false; // 是否启用计时器

    public static long lastConnectTime = 0;

    public static long lastSendCommandTime = 0L;

    private static final String TAG = "DFUUpdateService";
    private static final UUID UUID_CHARACTERISTIC_1 = UUID.fromString("00008001-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHARACTERISTIC_2 = UUID.fromString("00008002-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHARACTERISTIC_2_CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_SERVICE = UUID.fromString("00006006-0000-1000-8000-00805f9b34fb");

    private static final UUID UUID_CLIENT_CHARACTERISTIC_CONFIG = new UUID(0x0000290200001000l, 0x800000805f9b34fbl);
    public static final UUID UUID_DFU_SERVICE = UUID.fromString("00001530-1212-EFDE-1523-785FEABCD123");
    public static final UUID UUID_DFU_1531 = UUID.fromString("00001531-1212-EFDE-1523-785FEABCD123");
    public static final UUID UUID_DFU_1532 = UUID.fromString("00001532-1212-EFDE-1523-785FEABCD123");
    public static final UUID UUID_DFU_1534 = UUID.fromString("00001534-1212-EFDE-1523-785FEABCD123");

    private final IBinder mBinder = new LocalBinder();
    private BluetoothAdapter mBluetoothAdapter = null;

    private Timer timer1 = null;

    private BluetoothGatt mBluetoothGatt = null;
    private BluetoothManager mBluetoothManager = null;
    public static String mDeviceAddress = "";
    private String deviceName = "";
    private final int SN_LEN = 20; // 序列号的长度
    private final int MAXTIMEOUT = 20;
    private int timeoutCount = 0;
    private static int timeOutCount2 = 0; // 连续超时的次数
    public static boolean NeedSynTime = true;
    public static boolean isNeedSynTime = false;

    public static boolean SendTimeOut = false;
    public static boolean IsOSKikat = false;
    public static boolean IsGalaxyS3 = false;
    public boolean isUpdateing = false;

    private int connectTimes = 0;
    private Handler mHandler = new Handler() {
    };

    public static BluetoothDevice bluetoothdevice = null;

    public static BluetoothDevice lastpairedble = null;

    public static final String[] GalaxyS3_Types = {"GT-I9300", "GT-I9305", "SGH-T999", "SGH-I747", "SCH-R530", "SCH-I535", "SHW-M440", "SPH-L710", "SHV-E210", "SGH-T999",
            "SGH-N064", "SGH-N035", "SCH-J021", "SCH-R530", "SCH-S960", "SCH-S968", "GT-I9308", "SCH-I939", "GT-N7100", "GT-N7105", "SCH-I605", "SCH-R950", "SGH-I317", "SGH-T899",
            "SPH-L900", "GT-N7102", "GT-N7108", "SGH-T889", "SCH-N719", "SGH-N025", "SHV-E250", "SM-G9200", "SM-G9250", "SM-G9208", "SM-G9209", "SM-G920F", "SM-G900H", "SM-G900H"};

    // "GT-N7100"及以后是Note2 的版本
    public class LocalBinder extends Binder {
        public DFUUpdateService getService() {
            return DFUUpdateService.this;
        }
    }

    public DFUUpdateService() {
    }

    /**
     * 发送广播消息
     *
     * @param msgType 消息类型
     * @param abyte0  具体数据
     */
    private void broadcastUpdate(String msgType, byte abyte0[]) {
        Intent intent = new Intent(msgType);
        if (abyte0 != null)
            intent.putExtra(EXTRA_DATA, abyte0);
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
        intentfilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        intentfilter.addAction(ACTION_DATA_AVAILABLE);
        intentfilter.addAction(ACTION_GATT_ONCHARACTERISTICWRITE);
        /*
         * intentfilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED); intentfilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
		 */
        intentfilter.addAction(ACTION_GATT_SERVICES_TIMEOUT);
        return intentfilter;
    }

    /**
     * 发送数据(1531 1532特征通道共用)
     *
     * @param bytes      要发送的数据
     * @param is1531Flag true:1531 ; false:1532
     */
    public void writeDataToCharateristic(byte bytes[], boolean is1531Flag) {
        if (bytes == null)
            return;

        lastSendCommandTime = System.currentTimeMillis();

        if (mBluetoothGatt != null) {
            isEnable_time = true;
            timeoutCount = 0;
            BluetoothGattCharacteristic bluetoothgattcharacteristic = null;
            try {
                if (is1531Flag) {
                    bluetoothgattcharacteristic = mBluetoothGatt.getService(UUID_DFU_SERVICE).getCharacteristic(UUID_DFU_1531);
                    Logger.d(TAG, ">>>> 1531 发送的数据是 : " + NumberUtils.binaryToHexString(bytes));
                } else {
                    bluetoothgattcharacteristic = mBluetoothGatt.getService(UUID_DFU_SERVICE).getCharacteristic(UUID_DFU_1532);
                    bluetoothgattcharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    Logger.d(TAG, ">>>> 1532 发送的数据是 : " + NumberUtils.binaryToHexString(bytes));
                }
                bluetoothgattcharacteristic.setValue(bytes);
                mBluetoothGatt.writeCharacteristic(bluetoothgattcharacteristic);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 让设备进入UPDATING状态
     */
    public void enterUpdatingMode() {
        byte[] bytes = new byte[]{(byte) 0x6F, (byte) 0x0E, (byte) 0x71, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x8F};

        lastSendCommandTime = System.currentTimeMillis();

        if (mBluetoothGatt != null) {
            isEnable_time = true;
            timeoutCount = 0;
            BluetoothGattCharacteristic bluetoothgattcharacteristic = null;
            try {
                bluetoothgattcharacteristic = mBluetoothGatt.getService(UUID_SERVICE).getCharacteristic(UUID_CHARACTERISTIC_1);
                Logger.d(TAG, ">>>> 8001 发送的数据是 : " + NumberUtils.binaryToHexString(bytes));
                bluetoothgattcharacteristic.setValue(bytes);
                mBluetoothGatt.writeCharacteristic(bluetoothgattcharacteristic);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 设备中获取Freescale版本，用于判断是否升级成功
     */
    public void getFreescaleVersion() {
        byte[] bytes = new byte[]{(byte) 0x6F, (byte) 0x0E, (byte) 0x70, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x8F};

        lastSendCommandTime = System.currentTimeMillis();

        if (mBluetoothGatt != null) {
            isEnable_time = true;
            timeoutCount = 0;
            BluetoothGattCharacteristic bluetoothgattcharacteristic = null;
            try {
                bluetoothgattcharacteristic = mBluetoothGatt.getService(UUID_SERVICE).getCharacteristic(UUID_CHARACTERISTIC_1);
                Logger.d(TAG, ">>>> 8001 发送的数据是 : " + NumberUtils.binaryToHexString(bytes));
                bluetoothgattcharacteristic.setValue(bytes);
                mBluetoothGatt.writeCharacteristic(bluetoothgattcharacteristic);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 恢复出厂
     */
    public void resetDevice() {
        byte[] bytes = new byte[]{(byte) 0x6F, (byte) 0x0D, (byte) 0x71, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x8F};
        lastSendCommandTime = System.currentTimeMillis();

        if (mBluetoothGatt != null) {
            isEnable_time = true;
            timeoutCount = 0;
            BluetoothGattCharacteristic bluetoothgattcharacteristic = null;
            try {
                bluetoothgattcharacteristic = mBluetoothGatt.getService(UUID_SERVICE).getCharacteristic(UUID_CHARACTERISTIC_1);
                Logger.d(TAG, ">>>> 8001 发送的数据是 : " + NumberUtils.binaryToHexString(bytes));
                bluetoothgattcharacteristic.setValue(bytes);
                mBluetoothGatt.writeCharacteristic(bluetoothgattcharacteristic);
            } catch (Exception e) {
            }
        }
    }

    public void close() { // 这个调用接口是用来给以前短连接用的，
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        isConnected = false;
        isServiceDisvered = false;
        if (null != timer1) {
            timeoutCount = 0;
            timeOutCount2 = 0;
            isEnable_time = false;
        }
        if (mBluetoothGatt != null) {
            try {
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
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            } catch (Exception e) {
            }
        }
    }

    /**
     * 蓝牙消息回调
     */
    private BroadcastReceiver mbroadRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            Logger.i(TAG, "BroadcastReceiver.action=" + action);

            if (!NEED_BOND_FIRST)
                return;

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) { // 蓝牙状态改变
                BluetoothDevice dev1 = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Logger.d(TAG, "mac : " + dev1.getAddress() + "state change...");

                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (mDeviceAddress == null)
                    return;


                if (!mDeviceAddress.equals(dev1.getAddress())) {
                    Logger.d(TAG, "not operat on current dev.");
                    return;
                }

                Logger.d(TAG, "state:" + state + " prevState:" + prevState);

                if (state == BluetoothDevice.BOND_BONDED && ((prevState == BluetoothDevice.BOND_BONDING) || (prevState == BluetoothDevice.BOND_NONE))) {
                    if ((System.currentTimeMillis() - lastConnectTime) < 15000) // 15秒内配对完成
                    {
                        if (mBluetoothGatt != null) {
                            mHandler.postDelayed(new Runnable() {

                                @Override
                                public void run() {

                                    if (!isConnected) {
                                        if ((mDeviceAddress != null) && (mDeviceAddress.length() > 10)) {
                                            Logger.d(TAG, "conntinue connect.. ,");
                                            connect(mDeviceAddress, 0);
                                        }
                                    } else {
                                        Logger.d(TAG, "already connected..,not need connect,. Discoverservices.");
                                        boolean flag = mBluetoothGatt.discoverServices();

                                        // broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, null);
                                    }
                                }
                            }, 1000);
                        }

                    }
                    Logger.d(TAG, "Paired");
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    if (lastpairedble != null && dev1 != null) {
                        if (lastpairedble.getAddress().equals(dev1.getAddress())) {
                            lastpairedble = null;
                        }
                    }
                    // isPaired = false;
                    Logger.d(TAG, "Unpaired");

                } else {

                }
            }
        }
    };

    /**
     * 连接设备
     *
     * @param macOrDeviceName mac地址或设备名称
     * @param mode            0:通过mac地址连接模式 1:需要扫描到设备，再进行连接
     * @return
     */
    @SuppressLint("NewApi")
    public boolean connect(String macOrDeviceName, int mode) {
        if (TextUtils.isEmpty(macOrDeviceName)) {
            return false;
        }

        boolean flag = true;
        if (mode == 1) {
            Logger.i(TAG, "开始扫描设备...");
            deviceName = macOrDeviceName;
            scanLeDevice(true);
        } else if (mode == 0) {
            connectTimes = 1;
            if (isConnected && (macOrDeviceName.equals(mDeviceAddress))) {
                if ((!NEED_BOND_FIRST) || isDevPaird(macOrDeviceName)) {
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, null);
                    Logger.d(TAG, "already connected&paired... Not Need reconnect");
                } else {
                    makeBlePair();
                    Logger.d(TAG, "already connected .not paired....not need reconnect....");
                }
                return true;
            }

            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager != null)
                mBluetoothAdapter = mBluetoothManager.getAdapter();

            mDeviceAddress = macOrDeviceName;

            Logger.i(TAG, (new StringBuilder()).append("connect(final String address)=").append(macOrDeviceName).toString());
            if (mBluetoothAdapter == null || macOrDeviceName == null) {
                flag = false;
            } else {
                if ((mBluetoothGatt != null)) { // 如果上一次的mBluetoothGatt还没有关闭，则关闭
                    try {
                        mBluetoothGatt.disconnect();
                        mBluetoothGatt.close();
                    } catch (Exception e) {
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mBluetoothGatt = null;
                }

                int icount = 2;

                bluetoothdevice = null;
                while (true) {
                    bluetoothdevice = mBluetoothAdapter.getRemoteDevice(macOrDeviceName);
                    --icount;
                    if ((icount < 0) || (bluetoothdevice != null))
                        break;

                    Logger.d(TAG, "not find a  bluetooth device .................");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Logger.i(TAG, "-------------------------------conn............111 bluetoothdevice = " + (bluetoothdevice != null));

                if (bluetoothdevice == null) {
                    flag = false;
                } else {
                    timeoutCount = !IsOSKikat == true ? -8 : -5;
                    if ((bluetoothdevice != null) && NEED_BOND_FIRST) {
                        try {
                            if (!isDevPaird(bluetoothdevice)) {
                                makeBlePair();
                            }
                        } catch (Exception e) {
                        }

                    }
                    mBluetoothGatt = bluetoothdevice.connectGatt(this, IsOSKikat ? false : true, mGattCallback);

                    Logger.d(TAG, "SendTimeOut flag is :" + SendTimeOut);
                    Logger.d(TAG, (new StringBuilder()).append("device.getBondState==").append(bluetoothdevice.getBondState()).toString());
                }
            }
        }
        return flag;
    }

    /**
     * 通过蓝牙设备判断是否已经配对
     *
     * @param dev
     * @return
     */
    private boolean isDevPaird(BluetoothDevice dev) {
        if (dev == null)
            return false;

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices == null) {
            Logger.d(TAG, "pairedDeviceList is null");
            return false;
        }

        for (BluetoothDevice bledev : pairedDevices) {
            if (bledev.getAddress().equals(dev.getAddress())) {
                Logger.d(TAG, "isDevPaired : True");
                return true;
            }
        }
        return false;
    }

    /**
     * 通过mac判断是否已经配对
     *
     * @param macAddr
     * @return
     */
    private boolean isDevPaird(String macAddr) {
        if (TextUtils.isEmpty(macAddr))
            return false;

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices == null) {
            Logger.d(TAG, "pairedDeviceList is null");
            return false;
        }

        for (BluetoothDevice bledev : pairedDevices) {
            Logger.d(TAG, " Paired Dev addr : " + bledev.getAddress());
            if (bledev.getAddress().toUpperCase().equals(macAddr.toUpperCase())) {
                Logger.d(TAG, "isDevaddr Paired : True");
                return true;
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void makeBlePair() {
        if ((bluetoothdevice != null) && NEED_BOND_FIRST) {
            try {
                if (!isDevPaird(bluetoothdevice)) {
                    Logger.d(TAG, "not paired... exec paire Proc.");
                    bluetoothdevice.setPairingConfirmation(false); // 蓝牙配对申请
                    bluetoothdevice.createBond();
                    lastConnectTime = System.currentTimeMillis();
                }
            } catch (Exception e) {
            }
        }
    }

    public void makeBle() {
        Logger.i("", "bluetoothdevice=" + (bluetoothdevice == null ? true : false) + "-NEED_BOND_FIRST=" + NEED_BOND_FIRST);
        NEED_BOND_FIRST = true;
        makeBlePair();
    }

    @SuppressLint("NewApi")
    public boolean connect2(String s) {
        if (isConnected && (s.equals(mDeviceAddress))) {
            if ((!NEED_BOND_FIRST) || isDevPaird(s))
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, null);

            else
                makeBlePair();

            return true;
        }

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager != null)
            mBluetoothAdapter = mBluetoothManager.getAdapter();

        if ((null == s) || "".equals(s)) {
            scanLeDevice(true);
            s = "FF:FF:FF:FF:FF:FF";
            return false;
        }

        mDeviceAddress = s;

        boolean flag = true;
        Logger.i("DFUUpdateService", (new StringBuilder()).append("connect(final String address)=").append(s).toString());
        if (mBluetoothAdapter == null || s == null)
            flag = false;
        else {
            if ((mBluetoothGatt != null)) {
                try {
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                } catch (Exception e) {
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBluetoothGatt = null;
            }

            int icount = 2;

            bluetoothdevice = null;
            while (true) {
                bluetoothdevice = mBluetoothAdapter.getRemoteDevice(s);
                --icount;
                if ((icount < 0) || (bluetoothdevice != null))
                    break;
                Logger.d(TAG, "not find a  bluetooth device .................");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (bluetoothdevice == null) {
                flag = false;
            } else {
                if (!IsOSKikat)
                    timeoutCount = -8;
                else
                    timeoutCount = -5;

                if ((bluetoothdevice != null) && NEED_BOND_FIRST) {
                    try {
                        if (!isDevPaird(bluetoothdevice)) {
                            Logger.d(TAG, "device not paired, ");
                        } else if (bluetoothdevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                            Logger.d(TAG, "device not paired, ");
                        } else if (bluetoothdevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                            Logger.d(TAG, "device already paired, ");
                        }
                    } catch (Exception e) {
                    }

                }
                mBluetoothGatt = bluetoothdevice.connectGatt(this, IsOSKikat ? false : true, mGattCallback);
                Logger.d(TAG, "SendTimeOut flag is :" + SendTimeOut);
                Logger.d("DFUUpdateService", (new StringBuilder()).append("device.getBondState==").append(bluetoothdevice.getBondState()).toString());
            }
        }
        return flag;
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void onCreate() {
        super.onCreate();
        Logger.d(TAG, "service create");

        lastSendCommandTime = System.currentTimeMillis() + 20000;
        IsOSKikat = Build.VERSION.SDK_INT >= 19 ? true : false;
        timeoutCount = 0;

        if (null == timer1) {
            timer1 = new Timer();
            timer1.schedule(new TimerTask() {

                @Override
                public void run() {
                    if (!isEnable_time) {
                        timeoutCount = 0;
                        return;
                    } else {
                        timeoutCount++;
                    }

                    if (((!IsGalaxyS3) && (timeoutCount > MAXTIMEOUT)) || ((IsGalaxyS3) && (timeoutCount > (MAXTIMEOUT + 2))) || ((SendTimeOut) && (timeoutCount > MAXTIMEOUT - 1))) {

                        isEnable_time = false;
                        timeOutCount2++;
                        Logger.d(TAG, "...................TIMEOUT....................., timoutCount2:" + "SendTimeOut" + timeOutCount2);
                        if (SendTimeOut) {
                            timeoutCount = -2;
                            broadcastUpdate(ACTION_GATT_SERVICES_TIMEOUT, null);
                        } else {
                            timeoutCount = -2;
                            broadcastUpdate(ACTION_GATT_DISCONNECTED, null);
                        }
                        if (timeOutCount2 > 1) { // 连续超时2次
                            timeOutCount2 = 0;
                            Logger.e(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>timeOutCount2 over threshold " + timeOutCount2);
                            isConnected = false;
                            isServiceDisvered = false;
                            if (mBluetoothGatt != null) {
                                try {
                                    mBluetoothGatt.disconnect();
                                    mBluetoothGatt.close();
                                    mBluetoothGatt = null;
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                }
            }, 0, 300);
        }

        IsGalaxyS3 = false; // 这里不检查S3的型号了， 如果是Ｓ３，Ｎｏｔｅ２，必须用１.１６版本。

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager != null)
            mBluetoothAdapter = mBluetoothManager.getAdapter();

        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intent.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intent.setPriority(999);
        registerReceiver(mbroadRec, intent);
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG, "service destroy");
        Logger.d("bluetoothservice", "service destroy");
        unregisterReceiver(mbroadRec);
        if (null != timer1) {
            timer1.cancel();
            timer1 = null;
        }

        if (mBluetoothGatt != null) {
            Logger.d(TAG, "Close Bluetooth");
        }
    }

    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    /**
     * 发送数据到设备
     *
     * @param bytes      数据
     * @param is1531Flag true:1531 false:1532
     * @throws InterruptedException
     */
    public void sendDataToPedometer(byte[] bytes, boolean is1531Flag) {
        if (is1531Flag) {
            // sendMoreBytes(bytes1531);
            writeDataToCharateristic(bytes, true);
        } else {
            sendLargeBytes(bytes);
        }
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        // 在8002通道发送03到设备
        private void confirmByWriting0x03ToCharacteristic2() {
            Logger.d(TAG, "命令已经发送到设备了，写03到设备结束...");
            if (mBluetoothGatt == null)
                return;
            BluetoothGattCharacteristic bluetoothgattcharacteristic = mBluetoothGatt.getService(DFUUpdateService.UUID_SERVICE).getCharacteristic(
                    DFUUpdateService.UUID_CHARACTERISTIC_2);
            bluetoothgattcharacteristic.setValue(new byte[]{0x03});
            mBluetoothGatt.writeCharacteristic(bluetoothgattcharacteristic);
        }

        // 打开8002监听
        private void enableNotificationForCharacteristic2() {
            if (mBluetoothGatt == null)
                return;
            try {
                BluetoothGattCharacteristic bluetoothgattcharacteristic = mBluetoothGatt.getService(DFUUpdateService.UUID_SERVICE).getCharacteristic(
                        DFUUpdateService.UUID_CHARACTERISTIC_2);
                mBluetoothGatt.setCharacteristicNotification(bluetoothgattcharacteristic, true);

                BluetoothGattDescriptor bluetoothgattdescriptor = bluetoothgattcharacteristic.getDescriptor(DFUUpdateService.UUID_CHARACTERISTIC_2_CONFIG_DESCRIPTOR);
                bluetoothgattdescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(bluetoothgattdescriptor);
            } catch (Exception e) {
            }
        }

        // 打开1531监听
        private void enableNotificationFor1531() {
            if (mBluetoothGatt == null)
                return;
            try {
                BluetoothGattCharacteristic bluetoothgattcharacteristic = mBluetoothGatt.getService(DFUUpdateService.UUID_DFU_SERVICE).getCharacteristic(
                        DFUUpdateService.UUID_DFU_1531);
                mBluetoothGatt.setCharacteristicNotification(bluetoothgattcharacteristic, true);

                BluetoothGattDescriptor bluetoothgattdescriptor = bluetoothgattcharacteristic.getDescriptor(DFUUpdateService.UUID_CLIENT_CHARACTERISTIC_CONFIG);
                bluetoothgattdescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(bluetoothgattdescriptor);
            } catch (Exception e) {
            }
        }

        /**
         * 蓝牙状态改变
         *
         * @param bluetoothgatt
         * @param bluetoothgattcharacteristic
         */
        public void onCharacteristicChanged(BluetoothGatt bluetoothgatt, BluetoothGattCharacteristic bluetoothgattcharacteristic) {
            if (mBluetoothGatt == null)
                return;
            byte abyte0[] = bluetoothgattcharacteristic.getValue();

            Logger.i(TAG, ">>>onCharacteristicChanged : " + bluetoothgattcharacteristic.getUuid().toString());
            if (DFUUpdateService.UUID_CHARACTERISTIC_2.equals(bluetoothgattcharacteristic.getUuid()) || // 如果是8002发送完毕，发送ACTION_DATA_AVAILABLE
                    DFUUpdateService.UUID_DFU_1531.equals(bluetoothgattcharacteristic.getUuid()) || // 如果是1531发送完毕，发送ACTION_DATA_AVAILABLE
                    DFUUpdateService.UUID_DFU_1534.equals(bluetoothgattcharacteristic.getUuid())) { // 如果是1534发送完毕，发送ACTION_DATA_AVAILABLE
                Logger.i(TAG, "接收到设备发送过来的数据:" + NumberUtils.binaryToHexString(abyte0));
                if ((abyte0 != null) && (abyte0.length > 1)) {
                    timeOutCount2 = 0; // 收到数据就复位
                }
                timeoutCount = 0;
                isEnable_time = false;
                broadcastUpdate(ACTION_DATA_AVAILABLE, abyte0);
            }
        }

        /**
         * 读回调
         *
         * @param bluetoothgatt
         * @param bluetoothgattcharacteristic
         * @param i
         */
        public void onCharacteristicRead(BluetoothGatt bluetoothgatt, BluetoothGattCharacteristic bluetoothgattcharacteristic, int i) {
            Logger.d(TAG, "==>>onCharacteristicRead");
            if (mBluetoothGatt == null)
                return;
            if (DFUUpdateService.UUID_CHARACTERISTIC_1.equals(bluetoothgattcharacteristic.getUuid())) {
                if (!continueSendBytes()) { // 如果是大字节数组，则继续发送
                    confirmByWriting0x03ToCharacteristic2();
                }
            } else if (DFUUpdateService.UUID_DFU_1531.equals(bluetoothgattcharacteristic.getUuid())) {
                // continueSendMoreBytes();
            } else if (DFUUpdateService.UUID_DFU_1532.equals(bluetoothgattcharacteristic.getUuid())) {
                continueSendBytes();
            } else if (DFUUpdateService.UUID_DFU_1534.equals(bluetoothgattcharacteristic.getUuid())) {
            }
        }

        /**
         * 写回调
         *
         * @param bluetoothgatt
         * @param bluetoothgattcharacteristic
         * @param i
         */
        public void onCharacteristicWrite(BluetoothGatt bluetoothgatt, BluetoothGattCharacteristic bluetoothgattcharacteristic, int i) {
            Logger.d(TAG, "==>>onCharacteristicWrite : " + bluetoothgattcharacteristic.getUuid().toString());
            if (mBluetoothGatt == null)
                return;

            broadcastUpdate(ACTION_GATT_ONCHARACTERISTICWRITE, null);

            if (DFUUpdateService.UUID_CHARACTERISTIC_1.equals(bluetoothgattcharacteristic.getUuid())) {
                if (!continueSendBytes()) { // 如果是大字节数组，则继续发送
                    confirmByWriting0x03ToCharacteristic2();
                }
            } else if (DFUUpdateService.UUID_DFU_1531.equals(bluetoothgattcharacteristic.getUuid())) {
//                 continueSendMoreBytes();
            } else if (DFUUpdateService.UUID_DFU_1532.equals(bluetoothgattcharacteristic.getUuid())) {
                continueSendBytes();
            } else if (DFUUpdateService.UUID_DFU_1534.equals(bluetoothgattcharacteristic.getUuid())) {
            }
        }

        /**
         * 连接状态回调
         *
         * @param bluetoothgatt
         * @param i
         * @param j
         */
        @SuppressLint("NewApi")
        public void onConnectionStateChange(BluetoothGatt bluetoothgatt, int i, int j) {
            Logger.d(TAG, "connect state change..i:" + i + " j:" + j);
            if (j == STATE_DISCONNECTED) { // 断开连接
                isConnected = false;
                isServiceDisvered = false;
                SendTimeOut = false;
                sendBytes = null;
                sendBytesPacketCount = 0;
                timeoutCount = -2;
                if (mBluetoothGatt != null) {
                    try {
                        mBluetoothGatt.disconnect();
                        mBluetoothGatt.close();
                        mBluetoothGatt = null;
                    } catch (Exception e) {
                    }
                }
                broadcastUpdate(ACTION_GATT_DISCONNECTED, null);
            } else if ((j == STATE_CONNECTED) && (i == 0)) { // 已经连接
                Logger.d(TAG, " repeat connect..");

                SendTimeOut = false;
                isConnected = true;
                isServiceDisvered = false;
                timeoutCount = !IsOSKikat == true ? -10 : -8;

                if ((bluetoothdevice != null) && NEED_BOND_FIRST) {
                    try {
                        if (!isDevPaird(bluetoothdevice)) {
                            makeBlePair();
                            return;
                        } else {
                        }
                    } catch (Exception e) {
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                broadcastUpdate(ACTION_GATT_CONNECTED, null);
                mBluetoothGatt.discoverServices();
            } else if ((i == 133) && (j == 2)) { // 重新连接
                if (connectTimes < 4) {
                    Logger.d(TAG, " repeat connect.." + connectTimes);
                    connectTimes++;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            connect2(mDeviceAddress);
                        }
                    }, 3000);
                }
            }
        }

        public void onDescriptorRead(BluetoothGatt bluetoothgatt, BluetoothGattDescriptor bluetoothgattdescriptor, int i) {
            Logger.d(TAG, "==>>onDescriptorRead");
            if (DFUUpdateService.UUID_CHARACTERISTIC_2_CONFIG_DESCRIPTOR.equals(bluetoothgattdescriptor.getUuid())
                    || DFUUpdateService.UUID_CLIENT_CHARACTERISTIC_CONFIG.equals(bluetoothgattdescriptor.getUuid())) {
                isServiceDisvered = true;
                connect2(mDeviceAddress);
            }
        }

        public void onDescriptorWrite(BluetoothGatt bluetoothgatt, BluetoothGattDescriptor bluetoothgattdescriptor, int i) {
            Logger.d(TAG, "==>>onDescriptorWrite");
            bluetoothgatt.readDescriptor(bluetoothgattdescriptor);
        }

        /**
         * 发现BluetoothGatt服务
         *
         * @param bluetoothgatt
         * @param i
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt bluetoothgatt, int i) {
            Logger.d(TAG, ">>>> onServicesDiscovered");
            if (i == BluetoothGatt.GATT_SUCCESS) {
                timeoutCount = !IsOSKikat == true ? -6 : -4;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                enableNotificationForCharacteristic2();
                enableNotificationFor1531();
            }
        }

    };

    /**
     * 重新扫描或停止扫描
     *
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @SuppressLint("NewApi")
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, 10000);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
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

            try {
                String devname = device.getName();
                if (deviceName.contains("38I")) {
                    deviceName = deviceName.replace("38I", "91A");
                }
                Logger.i(TAG, "扫描到的设备是:" + devname + " 需要匹配的设备是:" + deviceName);  //38I00045
                if (deviceName.equals(devname)) {
                    mDeviceAddress = device.getAddress();
                    Logger.d(TAG, "重新扫描连接并找到设备,mac:" + mDeviceAddress);
                    scanLeDevice(false);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            connect(mDeviceAddress, 0);
                        }
                    }, 1000);
                }
//                    else {
////                        PublicData.SHARED_PRE_SAVE_FILE_NAME,
////                                PublicData.MAC_KEY
//                        mDeviceAddress = (String) ConfigHelper.getSharePref(DFUUpdateService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.MAC_KEY, ConfigHelper.DATA_STRING);
//                    }
            } catch (Exception e) {
            }

        }
    };

    public static String binaryToHexString(byte[] b) {
        String ret = "";
        if (b == null)
            return ret;
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    /**
     * 发送多字节包使用该接口(主要针对1532)
     *
     * @param bytes
     * 要发送的数据
     * @param is1531Flag
     * true:1531 false:1532
     */
    private byte[] sendBytes = null; // 发送的字节数组
    private int sendBytesPacketCount = 0; // 发送的包数
    private final int MAXSENDDATA = 20;

    public void sendLargeBytes(byte[] bytes) {
        sendBytes = null;
        sendBytesPacketCount = 0;
        if (bytes != null) {
            sendBytes = bytes;
            if (bytes.length <= MAXSENDDATA) {// 小包直接发走
                writeDataToCharateristic(bytes, false);
                return;
            }
            sendBytesPacketCount = sendBytes.length / MAXSENDDATA + (sendBytes.length % MAXSENDDATA == 0 ? 0 : 1);
            byte[] firstBytes = new byte[MAXSENDDATA];
            System.arraycopy(sendBytes, 0, firstBytes, 0, MAXSENDDATA);
            Logger.i(TAG, "大字节数组发送第一包:" + binaryToHexString(firstBytes) + " 共" + sendBytesPacketCount + "包数据!!!");
            sendBytesPacketCount--; // 发了一包，总数减一
            writeDataToCharateristic(firstBytes, false); // 先发第一包
        }
    }

    /**
     * 继续发送数据(主要针对1532)
     */
    private boolean continueSendBytes() {
        if (sendBytesPacketCount != 0) {
            byte[] tmpBytes = null;
            if (sendBytesPacketCount == 1) {// 是否最后一包，最后一包需检查要发送的字节数
                Logger.w("MyPushMsgService", "还有最后一包没有发...");
                tmpBytes = new byte[sendBytes.length % MAXSENDDATA == 0 ? MAXSENDDATA : (sendBytes.length % MAXSENDDATA)];
            } else {
                Logger.w("MyPushMsgService", "还有" + sendBytesPacketCount + "包没有发!!!");
                tmpBytes = new byte[MAXSENDDATA];
            }
            int count = sendBytes.length / MAXSENDDATA + (sendBytes.length % MAXSENDDATA == 0 ? 0 : 1);
            int index = count - sendBytesPacketCount;
            Logger.w("MyPushMsgService", "index : " + index + "   len : " + tmpBytes.length + "   totallen : " + sendBytes.length);
            System.arraycopy(sendBytes, MAXSENDDATA * index, tmpBytes, 0, tmpBytes.length);
            Logger.w("MyPushMsgService", "包数据是：" + binaryToHexString(tmpBytes));
            sendBytesPacketCount--;// 每发一包，减一包
            writeDataToCharateristic(tmpBytes, false);
            return true;
        }
        return false;
    }
}
