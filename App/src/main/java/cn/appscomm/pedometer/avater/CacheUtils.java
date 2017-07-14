package cn.appscomm.pedometer.avater;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by Avater on 2016/10/16 0016.
 */

public class CacheUtils {

    /**
     * SharedPreferences中储存数据的路径
     **/
    public final static String DATA_URL = "/data/data/";

    public static String getString(Context context, String key) {
        String result = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                String fileName = MD5Encoder.encode(key);
                File file = new File(Environment.getExternalStorageDirectory() +
                        "/L91/file/local/" + fileName);
                if (file.exists()) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] data = new byte[1024];
                    int length = 0;
                    FileInputStream fis = new FileInputStream(file);
                    while ((length = fis.read(data)) != -1) {
                        bos.write(data, 0, length);
                    }
                    result = bos.toString();
                    fis.close();
                    bos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            SharedPreferences sp = context.getSharedPreferences("avater", Context.MODE_PRIVATE);
//            result = sp.getString(key, "");
            result = sp.getString(key, "0");
        }
        return result;
    }

    public static void putString(Context context, String key, String value) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                String fileName = MD5Encoder.encode(key);
                File file = new File(Environment.getExternalStorageDirectory() +
                        "/L91/file/local/" + fileName);
                File parent = file.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(value.getBytes());
                fos.flush();
                fos.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            SharedPreferences sp = context.getSharedPreferences("avater", Context.MODE_PRIVATE);
            sp.edit().putString(key, value).commit();
        }
    }

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences("avater", Context.MODE_PRIVATE);
        sp.edit().putBoolean(key, value).commit();
    }

    public static boolean getBoolean(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("avater", Context.MODE_PRIVATE);
        return sp.getBoolean(key, false);
    }

    public static boolean delAllFile(Context context) {
        boolean flag = false;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //TODO 删除sdcard中所有的数据
            String path = Environment.getExternalStorageDirectory() + "/L91/file/local";
            File file = new File(path);
            if (!file.exists()) {
                return flag;
            }
            if (!file.isDirectory()) {
                return flag;
            }
            String[] tempList = file.list();
            File temp = null;
            for (int i = 0; i < tempList.length; i++) {
                if (path.endsWith(File.separator)) {
                    temp = new File(path + tempList[i]);
                } else {
                    temp = new File(path + File.separator + tempList[i]);
                }
                if (temp.isFile()) {
                    temp.delete();
                }
            }
            flag = true;
        } else {
            //TODO 删除sp文件
            File file = new File(DATA_URL + context.getPackageName().toString()
                    + "/shared_prefs", "avater");
            if (file.exists()) {
                file.delete();
            }
            flag = true;
        }
        return flag;
    }

    public static void setUnit(Context context, String value) {
        SharedPreferences sp = context.getSharedPreferences("avater_unitss", Context.MODE_PRIVATE);
        String key = "unit_units";
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getUnit(Context context) {
        SharedPreferences sp = context.getSharedPreferences("avater_unitss", Context.MODE_PRIVATE);
        return sp.getString("unit_units", "0");
    }
}
