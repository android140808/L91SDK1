package apps.utils;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.widget.TextView;
import android.widget.Toast;

import cn.l11.appscomm.pedometer.activity.R;


public class DialogUtil {

    /**
     * 通用警告对话框
     *
     * @param context 应用上下文
     * @param title   对话框标题
     * @param message 对话框信息内容
     */
    public static void commonDialog(Context context, String title, String message) {
        if (context == null)
            return;
        if (true) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle(title);
            TextView t1 = new TextView(context);
            //alertDialog.setView(t1,0,0,0,0);
            alertDialog.setMessage(message);
            alertDialog.setIcon(R.drawable.ic_launcher);
            alertDialog.setButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alertDialog.show();
        }

    }

    /**
     * 通用进度条对话框
     *
     * @param context 应用上下文
     * @param title   对话框标题
     * @param message 对话框信息内容
     * @return
     */
    public static ProgressDialog comProDialog(Context context, String title, String message) {
        ProgressDialog prDialog = new ProgressDialog(context);
        prDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prDialog.setTitle(title);
        prDialog.setMessage(message);
        prDialog.setIcon(R.drawable.ic_launcher);
//        prDialog.setCancelable(true);
        prDialog.setCancelable(false);

        return prDialog;

    }

    /**
     * 网络连接错误
     *
     * @param context
     */
    public static void conectionError(Context context) {
        String title = context.getString(R.string.app_name);
        String message = "Conection Error";
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon(R.drawable.ic_launcher);
        alertDialog.setButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });
        alertDialog.show();

    }

    public static ProgressDialog showMsg(Context context, String msg) {
        String title = context.getString(R.string.app_name);
        String message = msg;
        ProgressDialog prDialog = new ProgressDialog(context);
        prDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prDialog.setTitle(title);
        prDialog.setMessage(message);
        prDialog.setIcon(R.drawable.ic_launcher1);
        prDialog.setCancelable(true);
        return prDialog;
    }

    /**
     * 用户名或密码错误
     *
     * @param context
     */
    public static void userOrPwdError(Context context) {
        String title = context.getString(R.string.app_name);
        String message = context.getString(R.string.login_username_wrong);
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon(R.drawable.ic_launcher);
        alertDialog.setButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });
        alertDialog.show();

    }

    /**
     * 登陆
     *
     * @param context
     * @return
     */
    public static ProgressDialog logining(Context context) {
        String title = context.getString(R.string.app_name);
        String message = context.getString(R.string.login_loading);
        ProgressDialog prDialog = new ProgressDialog(context);
        prDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prDialog.setTitle(title);
        prDialog.setMessage(message);
        prDialog.setIcon(R.drawable.ic_launcher);
        prDialog.setCancelable(true);

        return prDialog;

    }

    /**
     * 同步
     *
     * @return
     */
    public static ProgressDialog syncing(Context context) {
        String title = context.getString(R.string.app_name);
        String message = context.getResources().getString(R.string.syndata);
        ProgressDialog prDialog = new ProgressDialog(context);
        prDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prDialog.setTitle(title);
        prDialog.setMessage(message);
        prDialog.setIcon(R.drawable.ic_launcher);
        prDialog.setCancelable(true);

        return prDialog;

    }


    public static void showNeedUpdateFirmware(final Context mContext) {

        AlertDialog.Builder ad = new AlertDialog.Builder(mContext);

        ad.setCancelable(false);
        ad.setMessage(mContext.getString(R.string.FirmwareUpgradeRequired));
        ad.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        ad.setPositiveButton(R.string.Download_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mContext.getString(R.string.update_firmware_website)));
                mContext.startActivity(intent);

            }
        });

        ad.show();


    }


    /**
     * @param mContext
     * @param isBindStep 是否在绑定步骤
     * @return
     */
    /* *  确保固件大于等于2.04
     * @param mContext
     */
    public static boolean checkFw204(Context mContext, boolean isBindStep, boolean isShowDiag) {

        int majorVer = 0, minorVer = 0;


        try {
            majorVer = (Integer) ConfigHelper.getSharePref(mContext, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.CURRENT_VER_MAJOR_ITEM_KEY, ConfigHelper.DATA_INT);
            minorVer = (Integer) ConfigHelper.getSharePref(mContext, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.CURRENT_VER_MINOR_ITEM_KEY, ConfigHelper.DATA_INT);
        } catch (Exception e) {

            e.printStackTrace();
        }


        if (isBindStep) {

            if (((majorVer < 2) || ((majorVer == 2) && (minorVer < 4)))) {

                if (isShowDiag) DialogUtil.showNeedUpdateFirmware(mContext);
                return true;
            } else return false;

        } else {
            if (((majorVer < 2) || ((majorVer == 2) && (minorVer < 4))) && ((majorVer + minorVer > 0))) {

                if (isShowDiag) DialogUtil.showNeedUpdateFirmware(mContext);
                return true;
            } else return false;


        }

    }

    /**
     * 升级
     *
     * @param context
     * @return
     */

    public static ProgressDialog updateing(Context context) {
        String title = context.getResources().getString(R.string.app_name) + "";
        String message = context.getResources().getString(R.string.updateing) + "";
        ProgressDialog prDialog = new ProgressDialog(context);
        prDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prDialog.setTitle(title);
        prDialog.setMessage(message);
        prDialog.setIcon(R.drawable.ic_launcher);
        prDialog.setCancelable(true);

        return prDialog;

    }


}
