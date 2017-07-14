package cn.appscomm.pedometer.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Telephony;
import android.support.v4.print.PrintHelper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bugtags.library.Bugtags;

import java.io.File;
import java.util.List;

import apps.utils.FileUtils;
import apps.utils.Logger;
import apps.utils.PublicData;
import cn.l11.appscomm.pedometer.activity.R;


/**
 * Created by glin on 4/7/2015.
 */
public class ShareDialogActivity extends Activity implements View.OnClickListener {

    private LinearLayout layout_share_msg, layout_share_email, layout_share_twitter, layout_share_fackbook,
            layout_share_save, layout_share_contact, layout_share_copy, layout_share_print;
    private ClipboardManager clipboard = null;
    private String picPath = "";


    private final String TAG = "ShareDialogActivity";


    @Override
    protected void onDestroy() {

        try {
            File fsrc = new File(picPath);
            fsrc.delete();
        }
        catch ( Exception e) {}
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_view);

        try {
            picPath = getIntent().getStringExtra("picpath");

        } catch (Exception e) {
        }


        layout_share_msg = (LinearLayout) findViewById(R.id.layout_share_msg);
        layout_share_email = (LinearLayout) findViewById(R.id.layout_share_email);
        layout_share_twitter = (LinearLayout) findViewById(R.id.layout_share_twitter);
        layout_share_fackbook = (LinearLayout) findViewById(R.id.layout_share_fackbook);
        layout_share_save = (LinearLayout) findViewById(R.id.layout_share_save);
        layout_share_contact = (LinearLayout) findViewById(R.id.layout_share_contact);
        layout_share_copy = (LinearLayout) findViewById(R.id.layout_share_copy);
        layout_share_print = (LinearLayout) findViewById(R.id.layout_share_print);

        layout_share_msg.setOnClickListener(this);
        layout_share_email.setOnClickListener(this);
        layout_share_twitter.setOnClickListener(this);
        layout_share_fackbook.setOnClickListener(this);
        layout_share_save.setOnClickListener(this);
        layout_share_contact.setOnClickListener(this);
        layout_share_copy.setOnClickListener(this);
        layout_share_print.setOnClickListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }


    private void lanuchShareActivity(final String lanuchPackage) {


        boolean b = false;
        Intent intent = new Intent(Intent.ACTION_SEND);

        String url = "file://" + picPath;
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(url));
        intent.setType("image/png");


        List<ResolveInfo> activities = getPackageManager().queryIntentActivities(intent, 0);

        if (activities == null) return;

        for (ResolveInfo info : activities) {
            String name = info.activityInfo.applicationInfo.packageName;


            Logger.d(TAG, "name :" + name + "  " + info.activityInfo.applicationInfo.className + "   packetname :" + info.activityInfo.applicationInfo.packageName);


            if (name.trim().toLowerCase().contains(lanuchPackage)) {
                b = true;
                Logger.d(TAG, info.activityInfo.applicationInfo.className + "   packetname :" + info.activityInfo.applicationInfo.packageName);
                ComponentName name1 = new ComponentName(
                        info.activityInfo.applicationInfo.packageName, info.activityInfo.name);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setComponent(name1);


                i.putExtra(Intent.EXTRA_STREAM, Uri.parse(url));
                i.setType("image/png");
                i.setAction(Intent.ACTION_SEND);
                Logger.d(TAG, "load pic:" + url);
                startActivity(i);
                break;
            }

        }

      if (!b)
      {
            if  (lanuchPackage.contains("facebook") )  Toast.makeText(this,R.string.needfacebook,Toast.LENGTH_LONG).show();
            else if (lanuchPackage.contains("twitter") )  Toast.makeText(this,R.string.needtwitter,Toast.LENGTH_LONG).show();
        }


    }


    @Override
    public void onClick(View v) {


        Intent intent = null;
        String url = null;
        int id = v.getId();
        switch (id) {
            case R.id.layout_share_email:

                intent = new Intent(Intent.ACTION_SEND);

                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + picPath));
                intent.setType("image/png");
                intent.setType("message/rfc882");
                Intent.createChooser(intent, "Choose Email Client");
                startActivity(intent);
                break;

            case R.id.layout_share_msg:


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(this); //Need to change the build to API 19


                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    url = "file://" + picPath;
                    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(url));
                    sendIntent.setType("image/png");


                    if (defaultSmsPackageName != null)//Can be null in case that there is no default, then the user would be able to choose any app that support this intent.
                    {
                        sendIntent.setPackage(defaultSmsPackageName);
                        startActivity(sendIntent);
                    }
                } else {

                    Intent sendIntent = new Intent(Intent.ACTION_SEND);

                    url = "file://" + picPath;
                    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(url));
                    sendIntent.setType("image/png");


                    startActivity(Intent.createChooser(sendIntent, "select"));

                }


                break;

            case R.id.layout_share_twitter:

                lanuchShareActivity("twitter");

                break;


            case R.id.layout_share_fackbook:
                lanuchShareActivity("com.facebook.katana");
                break;

            case R.id.layout_share_save:


                File fsrc = new File(picPath);
                File fdec = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/" + FileUtils.getFileName(picPath));
                FileUtils.copy(fsrc, fdec);


                Intent intent1 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(fdec);
                intent1.setData(uri);
                sendBroadcast(intent1);

              //  fsrc.delete();

                Toast.makeText(PublicData.appContext2, getString(R.string.success), Toast.LENGTH_SHORT).show();

                break;

            case R.id.layout_share_contact:


                break;

            case R.id.layout_share_copy:

                Uri copyUri = Uri.parse("file://" + picPath);
                ClipData clipUri = ClipData.newUri(getContentResolver(), "URI", copyUri);
                clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(clipUri);

              /*  ClipboardManager mClipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ContentValues values = new ContentValues(2);
                values.put(MediaStore.Images.Media.MIME_TYPE, "Image/jpg");
                values.put(MediaStore.Images.Media.DATA, picPath);
                ContentResolver theContent = getContentResolver();
                Uri imageUri = theContent.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                ClipData theClip = ClipData.newUri(getContentResolver(), "Image", imageUri);
                mClipboard.setPrimaryClip(theClip);*/


                Toast.makeText(PublicData.appContext2, getString(R.string.success), Toast.LENGTH_SHORT).show();
                break;

            case R.id.layout_share_print:

                PrintHelper photoPrinter = new PrintHelper(this);
                photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                Bitmap bitmap = BitmapFactory.decodeFile(picPath);
                photoPrinter.printBitmap("print", bitmap);



                break;


            default:
                break;
        }

        //  finish();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Bugtags.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Bugtags.onPause(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Bugtags.onDispatchTouchEvent(this, ev);
        return super.dispatchTouchEvent(ev);
    }
}