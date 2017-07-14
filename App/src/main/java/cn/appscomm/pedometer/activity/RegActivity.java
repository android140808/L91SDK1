package cn.appscomm.pedometer.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bugtags.library.Bugtags;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import apps.utils.CommonUtil;
import apps.utils.ConfigHelper;
import apps.utils.DialogUtil;
import apps.utils.HttpUtil;
import apps.utils.Logger;
import apps.utils.PropertiesUtil;
import apps.utils.PublicData;
import cn.appscomm.pedometer.UI.CircularImage;
import cn.appscomm.pedometer.UI.NewHeightPop;
import cn.appscomm.pedometer.UI.NewWeightPop;
import cn.appscomm.pedometer.UI.SelectWheelPopupWindow;
import cn.appscomm.pedometer.application.AppManager;
import cn.appscomm.pedometer.avater.CacheUtils;
import cn.appscomm.pedometer.service.HttpResDataService;
import cn.l11.appscomm.pedometer.activity.R;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;

import static apps.utils.PublicData.arrgender;
import static cn.l11.appscomm.pedometer.activity.R.string.f;
import static cn.l11.appscomm.pedometer.activity.R.string.ft;

/**
 */
public class RegActivity extends Activity {
    private final static String TAG = "WelcomeActivity";

    private LinearLayout ll_baseinfo;
    private TextView tv_title, reg_birthday, tv_agree, tv_agree1, tv_gender;
    private String reg_birthS = "";
    //顶部时间、电量
    private TextView top_title_time, top_title_battery;
    private EditText reg_username, reg_email, reg_password, reg_height, reg_weight;
    private RadioGroup rbngp_sex, rbngp_unit;
    private RadioButton rbn_male, rbn_female, rbn_unit_US, rbn_unit_Metric;
    private Spinner spHeight, spWeight;
    private CheckBox cb_agree;
    private Button btn_login, btn_reg;
    private Uri photoUri;
    private TextView height_textview, height_textview_tmp, weight_textview, weight_textview_tmp, tv_country;
    private Intent mIntent;
    private ProgressDialog regProgressDialog = null;
    private Thread regThread;
    String heightUnit = "0";
    String weightUnit = "0";
    private HttpUtil httpUtil = new HttpUtil();

    private boolean isChose = true;
    private String sex = "";//0 男 1女
    private List<String> spinnerHeightList = new ArrayList<String>();
    private List<String> spinnerWeightList = new ArrayList<String>();
    private String[] spinnerHeightArray;
    private String[] spinnerWeightArray;
    private final int crop = 96;  //图片剪裁的大小
    public int lastPosition = -1;
    private Handler mHandler = new Handler();

    private List<String> countryList = null;


    private Spinner countrySpn = null;
    private Integer current_height_item;  // 保存的当前身高的第几个item参数
    private Integer current_weight_item;  // 保存的当前体重的第几个item参数

    //	private TextView height_textview; // 身高
//	private TextView weight_textview; // 体重
    private Integer cur_height_unit; // 身高单位 1=ft in 0=cm
    private Integer cur_weight_unit = 0;
    private String heightVal_s, weightVal_s;

    //自定义的身高、体重、出生年月日 弹出框类
    private NewHeightPop wheelWindowHeight;
    private NewWeightPop wheelWindowWeight;
    private SelectWheelPopupWindow wheelWindowgender;

    private LinearLayout layout_country;

    private ImageView imginfo_ht, imginfo_wg;

    private RelativeLayout layout_img = null;
    private CircularImage cover_user_photo;
    private String mHeight = "", mWeight = "", mHeightUnit = "", mWeightUnit = "";
    private Integer mTmpUnit = 0;
    private int countryCode = -1;

    private static final int IMAGE_REQUEST_CODE = 1302;
    private static final int CAMERA_REQUEST_CODE = 1303;
    private static final int RESULT_REQUEST_CODE = 1304;
    private static final int CROP_REQUEST_CODE = 1305;
    public String[] arrHeightUnit;
    public String[] arrWeightUnit;
    //private static 	 final String IMAGE_FILE_NAME = "001.jpg";


    private AlertDialog dialog = null;
    private ArrayAdapter<String> countryAdapter;
    private String regImgUrl = " ";  //注册时头像的路径
//	private ImageView iv_photo;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//0是公制1是英制
//		//去掉标题栏
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		//全屏显示
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);
        AppManager.activityStack.add(this);
        setContentView(R.layout.reg_view);
        sp = getSharedPreferences("unit_avaters", Context.MODE_PRIVATE);
        editor = sp.edit();

        arrHeightUnit = new String[]{getString(R.string.cm), getString(R.string.ft)};
        arrWeightUnit = new String[]{getString(R.string.kg), getString(R.string.lbs)};
        httpUtil = new HttpUtil(this);

        ConfigHelper.setCommonSharePref(this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                PublicData.CURRENT_DDID_ITEM_KEY, -1);

        findView();
        init();
        setListeners();

        File f1 = new File(PublicData.SAVE_IMG_PATH, BaseSettingActivity.CROPED_FACE_IMG);
        try {

            f1.delete();
        } catch (Exception e) {
        }
        ;


    }

    private void findView() {
        tv_title = (TextView) findViewById(R.id.title);
        cover_user_photo = (CircularImage) findViewById(R.id.cover_user_photo);
        tv_gender = (TextView) findViewById(R.id.tvgender1);
        top_title_time = (TextView) findViewById(R.id.top_title_time);
        top_title_battery = (TextView) findViewById(R.id.top_title_battery);

        btn_reg = (Button) findViewById(R.id.btn_reg);

        height_textview = (TextView) findViewById(R.id.height_textview);
//        height_textview.setText("3" + "'" + "0" + "\" " + getString(ft));
        height_textview.setText("170" + getString(R.string.cm));
        height_textview_tmp = (TextView) findViewById(R.id.height_textview_tmp);
        height_textview_tmp.setText("170");
//        height_textview_tmp.setText("36");

        weight_textview = (TextView) findViewById(R.id.weight_textview);
        weight_textview_tmp = (TextView) findViewById(R.id.weight_textview_tmp);
        weight_textview.setText("60" + " " + getString(R.string.kg));
        weight_textview_tmp.setText("60");
//        weight_textview.setText("70" + " " + getString(R.string.lbs));
//        weight_textview_tmp.setText("70");
        PublicData.heightVal_unit = 1;
        reg_username = (EditText) findViewById(R.id.reg_username);
        reg_email = (EditText) findViewById(R.id.reg_email);
        reg_password = (EditText) findViewById(R.id.reg_password);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int mScreenWidth = dm.widthPixels;
        Logger.i(TAG, "==>>dm.widthPixels = " + mScreenWidth);
        if (mScreenWidth <= 480)
            reg_password.setTextSize(12);
        reg_height = (EditText) findViewById(R.id.reg_height);
        reg_weight = (EditText) findViewById(R.id.reg_weight);
        reg_birthday = (TextView) findViewById(R.id.reg_birthday);

        cb_agree = (CheckBox) findViewById(R.id.cb_agree);
        tv_agree = (TextView) findViewById(R.id.tv_agree);
        tv_agree.setMovementMethod(LinkMovementMethod.getInstance());
        tv_agree1 = (TextView) findViewById(R.id.tv_agree1);
        tv_agree1.setMovementMethod(LinkMovementMethod.getInstance());

        ll_baseinfo = (LinearLayout) findViewById(R.id.ll_baseinfo);

        rbngp_sex = (RadioGroup) findViewById(R.id.rbngp_sex);
        rbngp_unit = (RadioGroup) findViewById(R.id.rbngp_unit);

        rbn_male = (RadioButton) findViewById(R.id.rbn_male);
        rbn_female = (RadioButton) findViewById(R.id.rbn_female);

        rbn_unit_Metric = (RadioButton) findViewById(R.id.rbn_unitMetric);
        rbn_unit_US = (RadioButton) findViewById(R.id.rbn_unitUS);

        spHeight = (Spinner) findViewById(R.id.spHeight);
        spWeight = (Spinner) findViewById(R.id.spWeight);


        imginfo_ht = (ImageView) findViewById(R.id.img_infoht);
        imginfo_wg = (ImageView) findViewById(R.id.img_infowg);


        layout_country = (LinearLayout) findViewById(R.id.layout_country);

        tv_country = (TextView) findViewById(R.id.tv_contry);

        countrySpn = (Spinner) findViewById(R.id.spnlist1);


        layout_img = (RelativeLayout) findViewById(R.id.layout_img);
        layout_img.setOnClickListener(new RelativeLayout.OnClickListener() {


            @Override
            public void onClick(View v) {


                if (null == dialog) {
                    dialog = new AlertDialog.Builder(RegActivity.this).setItems(new String[]{getResources().getString(R.string.camera),
                            getResources().getString(R.string.album)}, new AlertDialog.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            switch (which) {
                                case 0:
                                    Intent cameraintent = new Intent(
                                            MediaStore.ACTION_IMAGE_CAPTURE);


                                    SimpleDateFormat timeStampFormat = new SimpleDateFormat(
                                            "yyyy_MM_dd_HH_mm_ss");
                                    String filename = timeStampFormat.format(new Date());
                                    ContentValues values = new ContentValues();
                                    values.put(Media.TITLE, filename);

                                    photoUri = getContentResolver().insert(
                                            Media.EXTERNAL_CONTENT_URI, values);

                                    cameraintent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                                    startActivityForResult(cameraintent, CAMERA_REQUEST_CODE);
                                    break;


                                case 1:

                                    Intent intent = new Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI);


                                    intent.setType("image/*");

                                    startActivityForResult(intent, IMAGE_REQUEST_CODE);

                                    break;


                                default:
                                    break;
                            }
                            // TODO Auto-generated method stub

                        }
                    }).create();


                }


                if (!dialog.isShowing()) {
                    dialog.show();
                }

            }
        });

        reg_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                temp = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                editStart = reg_username.getSelectionStart();
                editEnd = reg_username.getSelectionEnd();
                if (reg_username.getText().toString().length() > 25) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.to_long_username), Toast.LENGTH_LONG).show();
                    s.delete(editStart - 1, editEnd);
                    int tempSelection = editStart;
                    reg_username.setText(s);
                    reg_username.setSelection(tempSelection);
                }
            }
        });

    }

    private CharSequence temp;//监听前的文本
    private int editStart;//光标开始位置
    private int editEnd;//光标结束位置


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case IMAGE_REQUEST_CODE:
                case CAMERA_REQUEST_CODE:
                    // sdcardTempFile = new File(PublicData.SAVE_IMG_PATH,"ZeCircle001.jpg");
              /*
                Uri selectedImage = data.getData();
		        String[] filePathColumn = {MediaStore.Images.Media.DATA};

		        Cursor cursor = getContentResolver().query(
		                           selectedImage, filePathColumn, null, null, null);
		        cursor.moveToFirst();

		        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		        String filePath = cursor.getString(columnIndex);
		        
		        Logger.d(TAG, "filePath is : " + filePath);
		        cursor.close();
		        
		        if ((filePath !=null) && (!filePath.isEmpty()))
		        {
		        	startPhotoZoom(Uri.fromFile(new File(filePath)));
		        }
		       */


                    if ((data != null) && (null != data.getData())) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {Media.DATA};

                        Cursor cursor = getContentResolver().query(
                                selectedImage, filePathColumn, null, null, null);
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String filePath = cursor.getString(columnIndex);

                        Logger.d(TAG, "filePath is : " + filePath);
                        cursor.close();

                        if ((filePath != null) && (!filePath.isEmpty())) {
                            startPhotoZoom(Uri.fromFile(new File(filePath)));
                        }
                    } else {

                        if (photoUri != null) {
                            startPhotoZoom(photoUri);

                        }

                    }

			/*	 
            Bitmap bitmap=null;
			try {
				bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),  data.getData());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
				if (sdcardTempFile.exists())
				{
					Logger.d(TAG, sdcardTempFile.getAbsolutePath() + "  exist");
				}
				else Logger.d(TAG, sdcardTempFile.getAbsolutePath() + "  not exist");
				cover_user_photo.setImageBitmap(bitmap);
				
				*/
                    //startPhotoZoom(data.getData());
                    break;

				
		/*	case CAMERA_REQUEST_CODE :

				 Uri selectedImage2 = data.getData();
			        String[] filePathColumn2 = {MediaStore.Images.Media.DATA};

			        Cursor cursor2 = getContentResolver().query(
			                           selectedImage2, filePathColumn2, null, null, null);
			        cursor2.moveToFirst();

			        int columnIndex2 = cursor2.getColumnIndex(filePathColumn2[0]);
			        String filePath2 = cursor2.getString(columnIndex2);
			        
			        Logger.d(TAG, "filePath is : " + filePath2);
			        cursor2.close();
				
				break;*/
                case CROP_REQUEST_CODE: // 图片缩放完成后
                    if (data != null) {
                        getImageToView(data);
                    }
                    break;
            }
        }
    }


    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", crop);
        intent.putExtra("outputY", crop);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_REQUEST_CODE);
    }

    /**
     * 保存裁剪之后的图片数据
     *
     * @param
     */
    private void getImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(this.getResources(), photo);
            cover_user_photo.setImageDrawable(drawable);
            layout_img.setBackground(null);

            File file_phto = new File(PublicData.SAVE_IMG_PATH, BaseSettingActivity.CROPED_FACE_IMG);

            if (file_phto.exists()) {
                file_phto.delete();
            }

            Logger.d(TAG, "file_phto : " + file_phto.getAbsolutePath());

            OutputStream stream = null;
            try {
                stream = new FileOutputStream(file_phto);
                photo.compress(Bitmap.CompressFormat.JPEG, 80, stream);


                Logger.d(TAG, "file save : " + file_phto.getAbsolutePath());
                try {
                    stream.close();


                } catch (IOException e) {
                    Logger.d(TAG, "error close");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                Logger.d(TAG, "error decode");
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }


    private void init() {

        countryCode = 0;
        countryList = getCountryData();
        countryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, countryList);

        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        countrySpn.setVisibility(View.INVISIBLE);


        File imgfile2 = new File(PublicData.SAVE_IMG_PATH, BaseSettingActivity.SAVED_FACE_IMG);

        if (imgfile2.exists()) {
            imgfile2.delete();
        }


        countrySpn.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                countryCode = arg2 + 1;
                Logger.d(TAG, "ContryCode selected is :" + countryCode);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


        tv_title.setText(getString(R.string.sign_up_title));

        tv_agree.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        tv_agree1.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

        spinnerHeightArray = new String[201];
        int sHeight = 90;
        for (int i = 0; i < 201; i++) {
            sHeight += i;
            spinnerHeightList.add(sHeight + "");
            spinnerHeightArray[i] = sHeight + "";

            sHeight = 90;
        }

        spinnerWeightArray = new String[221];
        int sWeight = 30;
        for (int i = 0; i < 221; i++) {
            sWeight += i;
            spinnerWeightList.add(sWeight + "");
            spinnerWeightArray[i] = sWeight + "";

            sWeight = 30;
        }


        String weight_unit = (String) ConfigHelper.getSharePref(RegActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.WEIGHT_UNIT_KEY, ConfigHelper.DATA_STRING);
        //0是公制1是英制
        Logger.i("", "默认单位  =  " + weight_unit);
        if ("英磅".equals(weight_unit) || "Pounds".equals(weight_unit)
                || "Livres".equals(weight_unit)) {

            weight_unit = getString(R.string.pounds);
        } else {
            weight_unit = getString(R.string.kg);
        }
        //0是公制1是英制

        current_height_item = (Integer) ConfigHelper.getSharePref(RegActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.HEIGHT_ITEM_KEY, ConfigHelper.DATA_INT);
        current_weight_item = (Integer) ConfigHelper.getSharePref(RegActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.WEIGHT_ITEM_KEY, ConfigHelper.DATA_INT);

        //注册默认
//        current_height_item = 80;
//        current_weight_item = 36;
        current_height_item = 90;
        current_weight_item = 30;


//		ArrayAdapter<String> serverAdapter = new ArrayAdapter<String>(RegActivity.this,
//				android.R.layout.simple_spinner_item, spinnerHeightList);
//		serverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//		spHeight.setAdapter(serverAdapter);

        SpinnerAdapter adapterHeight = new SpinnerAdapter(this,
                android.R.layout.simple_spinner_item, spinnerHeightArray);
        spHeight.setAdapter(adapterHeight);

        SpinnerAdapter adapterWeight = new SpinnerAdapter(this,
                android.R.layout.simple_spinner_item, spinnerWeightArray);
        spWeight.setAdapter(adapterWeight);


        rbn_unit_US.setChecked(false);
        rbn_unit_Metric.setChecked(true);
        rbn_unit_Metric.setChecked(true);
//        rbn_unit_US.setChecked(true);
//        rbn_unit_Metric.setChecked(false);
        rbn_male.setChecked(true);
        rbn_female.setChecked(false);
        //0是公制1是英制
        sex = "0";
        PublicData.heightVal_unit = 0;//默认公制
        PublicData.weightVal_unit = 0;
        mTmpUnit = 0;
//        PublicData.heightVal_unit = 1;//默认英制
//        PublicData.weightVal_unit = 1;
//        mTmpUnit = 1;

        CacheUtils.putString(this, "unit_value", "0");//默认公制
        CacheUtils.setUnit(this, "0");

    }


    public void setListeners() {
        btn_reg.setOnClickListener(new ClickListener());
        reg_birthday.setOnClickListener(new ClickListener());
        ll_baseinfo.setOnClickListener(new ClickListener());
        tv_agree.setOnClickListener(new ClickListener());
        tv_agree1.setOnClickListener(new ClickListener());
//		cb_agree.setOnCheckedChangeListener(mCheckBoxChanged);


        imginfo_ht.setOnClickListener(new ClickListener());
        imginfo_wg.setOnClickListener(new ClickListener());

		/*rbngp_sex.setOnCheckedChangeListener(mRadioGroupChanged);
        rbngp_unit.setOnCheckedChangeListener(mRadioGroupChanged);*/

        rbn_unit_US.setOnClickListener(mrbnls);
        rbn_unit_Metric.setOnClickListener(mrbnls);
        rbn_female.setOnClickListener(mrbnls);
        rbn_male.setOnClickListener(mrbnls);


        spHeight.setOnItemSelectedListener(mSelectedHeightEvent);
        spWeight.setOnItemSelectedListener(mSelectedWeightEvent);

        layout_country.setOnClickListener(new LinearLayout.OnClickListener() {

            @Override
            public void onClick(View v) {

			/*	if (countrySpn.VISIBLE==View.INVISIBLE)
                {
					tv_country.setVisibility(View.GONE);
					countrySpn.setVisibility(View.VISIBLE);
				
					
				}*/
                countrySpn.setAdapter(countryAdapter);
                tv_country.setVisibility(View.GONE);
                countrySpn.setVisibility(View.VISIBLE);
                countrySpn.performClick();

                // TODO Auto-generated method stub

            }
        });
    }


    // 返回按钮响应事件：
    public void btn_return_clicked(View view) {
        finish();
    }

    private boolean check() {
        if ("".equals(reg_username.getText().toString())) {
            DialogUtil.commonDialog(RegActivity.this,
                    getString(R.string.app_name),
                    getString(R.string.reg_username_null));

            return false;
        }

        if ("".equals(reg_email.getText().toString())) {
            DialogUtil.commonDialog(RegActivity.this,
                    getString(R.string.app_name),
                    getString(R.string.reg_email_null));

            return false;
        }

        if ((reg_email.getText().toString().indexOf("@") == -1) || (!CommonUtil.emailFormat(reg_email.getText().toString().trim()))) {
            DialogUtil.commonDialog(RegActivity.this,
                    getString(R.string.app_name),
                    getString(R.string.reg_email_wrong));

            return false;
        }

        if ("".equals(reg_password.getText().toString())) {
            DialogUtil.commonDialog(RegActivity.this,
                    getString(R.string.app_name),
                    getString(R.string.reg_password_null));

            return false;
        }

        if (reg_password.getText().toString().length() < 6 || reg_password.getText().toString().length() > 16) {
            DialogUtil.commonDialog(RegActivity.this,
                    getString(R.string.app_name),
                    getString(R.string.password_length));

            return false;
        }

//		if ("".equals(sex)) {
//			DialogUtil.commonDialog(RegActivity.this,
//					getString(R.string.app_name),
//					getString(R.string.reg_gender_null));
//
//			return false;
//		}


        if ("".equals(height_textview_tmp.getText().toString()) || height_textview_tmp.getText().toString().equals("0")) {
            DialogUtil.commonDialog(RegActivity.this,
                    getString(R.string.app_name),
                    getString(R.string.reg_height_null));

            return false;
        }

        if ("".equals(weight_textview_tmp.getText().toString()) || weight_textview_tmp.getText().toString().equals("0")) {
            DialogUtil.commonDialog(RegActivity.this,
                    getString(R.string.app_name),
                    getString(R.string.reg_weight_null));

            return false;
        }


        //	if ("".equals(reg_birthday.getText().toString())) {
        if ("".equals(reg_birthS)) {
            DialogUtil.commonDialog(RegActivity.this,
                    getString(R.string.app_name),
                    getString(R.string.reg_birthday_null));

            return false;
        }

//		if (!isChose) {
//			DialogUtil.commonDialog(RegActivity.this,
//					getString(R.string.app_name),
//					getString(R.string.reg_protocol));
//			return false;
//		}


//		if (countryCode<1) {
//			DialogUtil.commonDialog(RegActivity.this,
//					getString(R.string.app_name),
//					getString(R.string.mustselectcountry));
//			return false;
//		}

        return true;
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

    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int level = intent.getIntExtra("level", 0); //获得当前电量
                int scale = intent.getIntExtra("scale", 100); //获得总电量

                int percent = level * 100 / scale;
                Logger.d(TAG, "===电量：" + percent + "%");
                top_title_battery.setText(percent + "%");

                SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm");
                String date = sDateFormat.format(new Date());
                top_title_time.setText(date);
            }
        }

    };

    /**
     * 复选框事件
     */
    public CheckBox.OnCheckedChangeListener mCheckBoxChanged = new CheckBox.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {

            if (cb_agree.isChecked()) {
                isChose = true;
            } else {
                isChose = false;
            }
        }

    };


    private List<String> getCountryData() {
        //	 ountryList = new ArrayList<String>();

        List<String> list1 =
                Arrays.asList(new String[]{getString(R.string.Algeria),
                        getString(R.string.Andorra), getString(R.string.Argentina),
                        getString(R.string.Armenia), getString(R.string.Australia),
                        getString(R.string.Austria), getString(R.string.Azerbaijan),
                        getString(R.string.Bahamas), getString(R.string.Bahrain),
                        getString(R.string.Belgium), getString(R.string.Belarus),
                        getString(R.string.Brazil),
                        getString(R.string.Bulgaria), getString(R.string.Cambodia),
                        getString(R.string.Canada), getString(R.string.Chile),
                        getString(R.string.China), getString(R.string.Colombia),
                        getString(R.string.Costa_Rica), getString(R.string.Croatia),
                        getString(R.string.Cyprus), getString(R.string.Czech_Republic),
                        getString(R.string.Denmark), getString(R.string.Deutschland),
                        getString(R.string.Dominican_Republic),
                        getString(R.string.Ecuador), getString(R.string.Egypt),
                        getString(R.string.El_Salvador), getString(R.string.Estonia),
                        getString(R.string.Finland), getString(R.string.France),
                        getString(R.string.Greece), getString(R.string.Guatemala),
                        getString(R.string.Honduras), getString(R.string.Hong_Kong),
                        getString(R.string.Hungary), getString(R.string.Iceland),
                        getString(R.string.India), getString(R.string.Indonesia),
                        getString(R.string.Iran), getString(R.string.Ireland),
                        getString(R.string.Israel), getString(R.string.Italy),
                        getString(R.string.Jamaica), getString(R.string.Japan),
                        getString(R.string.Jordan), getString(R.string.Kazakhstan),
                        getString(R.string.Kenya), getString(R.string.Kuwait),
                        getString(R.string.Kyrgyzstan), getString(R.string.Latvia),
                        getString(R.string.Lebanon), getString(R.string.Liechtenstein),
                        getString(R.string.Lithuania), getString(R.string.Luxembourg),
                        getString(R.string.Madagascar), getString(R.string.Malaysia),
                        getString(R.string.Malta), getString(R.string.Mauritania),
                        getString(R.string.Mauritius), getString(R.string.Mexico),
                        getString(R.string.Morocco), getString(R.string.Netherlands),
                        getString(R.string.New_Zealand), getString(R.string.Nicaragua),
                        getString(R.string.Niger), getString(R.string.Nigeria),
                        getString(R.string.Norway), getString(R.string.Oman),
                        getString(R.string.Panama), getString(R.string.Paraguay),
                        getString(R.string.Peru), getString(R.string.Philippines),
                        getString(R.string.Poland), getString(R.string.Portugal),
                        getString(R.string.Qatar), getString(R.string.Romania),
                        getString(R.string.Russia), getString(R.string.Saudi_Arabia),
                        getString(R.string.Senegal), getString(R.string.Serbia),
                        getString(R.string.Seychelles),
                        getString(R.string.Sierra_Leone),
                        getString(R.string.Singapore), getString(R.string.Slovakia),
                        getString(R.string.Slovenia), getString(R.string.South_Africa),
                        getString(R.string.South_Korea), getString(R.string.Spain),
                        getString(R.string.Sweden), getString(R.string.Switzerland),
                        getString(R.string.Syria), getString(R.string.Taiwan),
                        getString(R.string.Thailand), getString(R.string.Tunisia),
                        getString(R.string.Turkey), getString(R.string.Ukraine),
                        getString(R.string.United_Arab_Emirates),
                        getString(R.string.United_Kingdom),
                        getString(R.string.United_States_of_America),
                        getString(R.string.Uruguay), getString(R.string.Venezuela),
                        getString(R.string.Vietnam), getString(R.string.Puerto_Rico)

                });

        return list1;
    }

    private boolean isFirst = true;

    private RadioGroup.OnClickListener mrbnls = new RadioGroup.OnClickListener() {


        @Override
        public void onClick(View v) {

            boolean isChecked = false;

            int id = v.getId();
            switch (id) {
                case R.id.rbn_male:


                    rbn_male.setChecked(true);
                    rbn_female.setChecked(false);


                    Logger.d(TAG, "sex is male.");
                    sex = "0";


                    break;

                case R.id.rbn_female:

                    rbn_male.setChecked(false);
                    rbn_female.setChecked(true);

                    Logger.d(TAG, "sex is female.");
                    sex = "1";

                    break;
                case R.id.rbn_unitMetric:
                    //0是公制1是英制
                    PublicData.heightVal_unit = 0;
                    PublicData.weightVal_unit = 0;
                    mTmpUnit = 0;
//                    editor.putString("unit_value", "0");
//                    editor.commit();
//                    CacheUtils.putString(RegActivity.this, "unit_value", "0");
//                    CacheUtils.setUnit(RegActivity.this, "0");
                    Logger.d(TAG, "unit is FT.");
                    String lastUnit = (String) ConfigHelper.getCommonSharePref(RegActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.UNIT_KEY,
                            ConfigHelper.DATA_STRING);
                    if ("1".equals(lastUnit) || lastUnit.equals("")) {//上一次是英制,当前选择为公制,需切换单位
                        switchUnit(1);
                    }
                    ConfigHelper.setSharePref(RegActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.UNIT_KEY, "0");   // 公制
                    rbn_unit_Metric.setChecked(true);
                    rbn_unit_US.setChecked(false);
                    //switchUnit(0);
                    break;

                case R.id.rbn_unitUS:
                    //0是公制1是英制
                    PublicData.heightVal_unit = 1;
                    PublicData.weightVal_unit = 1;
                    mTmpUnit = 1;
//                    editor.putString("unit_value", "1");
//                    editor.commit();
//                    CacheUtils.putString(RegActivity.this, "unit_value", "1");
//                    CacheUtils.setUnit(RegActivity.this, "1");
                    Logger.d(TAG, "unit is US.");
                    String lastUnit1 = (String) ConfigHelper.getCommonSharePref(RegActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.UNIT_KEY,
                            ConfigHelper.DATA_STRING);
                    if ("0".equals(lastUnit1)) {//上一次是公制,当前选择为英制,需切换单位
                        switchUnit(0);
                        isFirst = false;
                    }
                    if (isFirst) {
                        switchUnit(0);
                        isFirst = false;
                    }
                    ConfigHelper.setSharePref(RegActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.UNIT_KEY, "1");   // 英制
                    rbn_unit_Metric.setChecked(false);
                    rbn_unit_US.setChecked(true);
                    //switchUnit(1);

                    break;


            }

        }
    };

    private void switchUnit(int target) {
        if (target == 0 && PublicData.heightVal_unit == 1) {
            // 公制转英制
            PublicData.heightVal_unit = 1;
            PublicData.weightVal_unit = 1;
            int tmp = (int) (float) Float.valueOf(height_textview_tmp.getText().toString()); //cm
            tmp = (int) (double) (tmp * 0.3937008); //cm > ft in
            int a1 = tmp / 12;
            int a2 = tmp % 12;
            if (a1 > 7) {
                a1 = 7;
            } else if (a1 < 3) {
                a1 = 3;
            }
            tmp = a1 * 12 + a2;
            height_textview.setText(a1 + "'" + a2 + "\" " + arrHeightUnit[PublicData.heightVal_unit]);
            height_textview_tmp.setText("" + tmp);


            double tmp2 = (float) Float.valueOf(weight_textview_tmp.getText().toString()); //kg
            tmp2 = (double) (tmp2 * 2.2046226); //kg > lbs
            String str0 = Double.toString(tmp2);
            String str = PublicData.arrHeight_Int[PublicData.heightVal_int];
            int start = str0.indexOf(".");
            String a0 = "" + (int) tmp2;
            try {
                a0 = str.substring(0, start + 2);
                Logger.d(TAG, "-----a0:" + a0);
            } catch (Exception e) {
                a0 = "" + (int) tmp2;
                Logger.d(TAG, "-----a0..EXCEpion");
            }
            weight_textview.setText(a0 + " " + arrWeightUnit[PublicData.weightVal_unit]);
            weight_textview_tmp.setText(a0);
        } else if (target == 1 && PublicData.heightVal_unit == 0) {
            // 英制转公制
            PublicData.heightVal_unit = 0;
            PublicData.weightVal_unit = 0;
            int tmp = (int) (float) Float.valueOf(height_textview_tmp.getText().toString()); //ft in
            tmp = (int) (double) (tmp * 2.54); //ft in > cm
            height_textview.setText(tmp + " " + arrHeightUnit[PublicData.heightVal_unit]);
            height_textview_tmp.setText("" + tmp);


            double tmp2 = (float) Float.valueOf(weight_textview_tmp.getText().toString()); //lbs
            tmp2 = (double) (tmp2 * 0.4535924); //lbs > kg
            String str = Double.toString(tmp2);
            int start = str.indexOf(".");
            String a0 = "" + (int) tmp2;
            try {
                a0 = str.substring(0, start + 2);
                Logger.d(TAG, "-----a0:" + a0);
            } catch (Exception e) {
                a0 = "" + (int) tmp2;
                Logger.d(TAG, "-----a0..EXCEpion");
            }
            weight_textview.setText(a0 + " " + arrWeightUnit[PublicData.weightVal_unit]);
            weight_textview_tmp.setText(a0);
        }
    }

//	/**
//	 * 单选框事件
//	 */
//	public RadioGroup.OnCheckedChangeListener mRadioGroupChanged = new RadioGroup.OnCheckedChangeListener() {
//
//		@Override
//		public void onCheckedChanged(RadioGroup arg0, int arg1) {
//            //获取变更后的选中项的ID
//            int radioButtonId = arg0.getCheckedRadioButtonId();
//            //根据ID获取RadioButton的实例
//
//
//			boolean isChecked =false;
//
//            switch(radioButtonId) {
//            case R.id.rbn_male:
//
//
//				isChecked = rbn_male.isChecked();
//
//
//            	Logger.d(TAG, "sex is male.");
//            	sex = "0";
//
//
//            	break;
//
//            case R.id.rbn_female:
//            	Logger.d(TAG, "sex is female.");
//            	sex = "1";
//
//
//				case R.id.rbn_unitFt:
//					Logger.d(TAG, "unit is FT.");
//					break;
//
//				case R.id.rbn_unitUS:
//					Logger.d(TAG, "unit is US.");
//					break;
//
//
//
//
//
//
//            }
//            //RadioButton rb = (RadioButton)RegActivity.this.findViewById(radioButtonId);
//
//            //更新文本内容，以符合选中项
//            //tv.setText("您的性别是：" + rb.getText());
//		}
//
//	};

    class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.btn_reg:

                    if (check()) {
                        if (reg_username.getText().toString().length() > 25) {
                            Toast.makeText(RegActivity.this, getResources().getString(R.string.basesettingusername), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (httpUtil.isNetworkConnected()) {
                            regProgressDialog = DialogUtil.logining(RegActivity.this);
                            regProgressDialog.show();
                            CacheUtils.putString(RegActivity.this, "dialog_year_item", "");
                            CacheUtils.putString(RegActivity.this, "dialog_month_item", "");
                            CacheUtils.putString(RegActivity.this, "dialog_day_item", "");

                            new Thread(UpLoadimgRunnable).start();

						/*regThread = new Thread(regRunnable);
                        regThread.start();
*/
                        } else {
                            regHandler.obtainMessage(3, getString(R.string.NetWorkError)).sendToTarget();
                        }
                    }
                    break;

                case R.id.ll_baseinfo:
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(reg_username.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(reg_email.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(reg_password.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(reg_height.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(reg_weight.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(reg_birthday.getWindowToken(), 0);

                    break;

                case R.id.reg_birthday:
                    getDate();

                    break;

                case R.id.tv_agree:
                    PropertiesUtil pu = new PropertiesUtil();
                    pu.initResRawPropFile(RegActivity.this, R.raw.server);
                    Properties props = pu.getPropsObj();

                    String url = props.getProperty("server.terms.service.address", "http://www.mykronoz.com/credits-legal-mentions/");

                    Logger.d(TAG, "请求地址：" + url);

                    mIntent = new Intent();
                    mIntent.setClass(RegActivity.this, ShowWebActivity.class);

                    Bundle bundle = new Bundle();
                    bundle.putString("loadingUrl", url);

                    mIntent.putExtras(bundle);
                    startActivity(mIntent);

                    break;


                case R.id.img_infoht:
                    DialogUtil.commonDialog(RegActivity.this,
                            "",
                            getString(R.string.height_tips));
                    break;

                case R.id.img_infowg:

                    DialogUtil.commonDialog(RegActivity.this,
                            "",
                            getString(R.string.weight_tips));
                    break;

                default:
                    break;
            }

        }

    }

    private static final int DATE_DIALOG_ID = 1;
    private Integer current_year_item; // 当前保存的 年 的item参数
    private Integer current_month_item; // 当前保存的 月 的item参数
    private Integer current_day_item; // 当前保存的 日 的item参数
    private boolean mBrithDayInvalid = false;

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, mDateSetListener, current_year_item, current_month_item - 1,
                        current_day_item);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            view.setMaxDate(new Date().getTime());
            current_year_item = year;
            current_month_item = monthOfYear + 1;
            current_day_item = dayOfMonth;
            CacheUtils.putString(RegActivity.this, "dialog_year_item", current_year_item + "");
            CacheUtils.putString(RegActivity.this, "dialog_month_item", current_month_item + "");
            CacheUtils.putString(RegActivity.this, "dialog_day_item", current_day_item + "");
            Calendar cal = Calendar.getInstance();
            cal.set(year, monthOfYear, dayOfMonth);
            if (cal.getTimeInMillis() > System.currentTimeMillis()) {
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(year).append("-").append(monthOfYear + 1).append("-").append(dayOfMonth);
            if ("en".equals(PublicData.currentLang)) {
                reg_birthday.setText((monthOfYear + 1) + " / " + dayOfMonth + " / " + year);
            } else
                reg_birthday.setText(dayOfMonth + " / " + (monthOfYear + 1) + " / " + year);
            reg_birthS = sb.toString();

        }
    };

    public void getDate() {
        String strDate = "";
        String current_year_item = CacheUtils.getString(RegActivity.this, "dialog_year_item");
        if (current_year_item.equals("") || current_year_item == null) {
            this.current_year_item = Integer.parseInt(CommonUtil.getCurYear(new Date()));
            current_month_item = Integer.parseInt(CommonUtil.getCurMonthOfYear(new Date()));
            current_day_item = Integer.parseInt(CommonUtil.getCurDayOfMonth(new Date()));
        } else {
            this.current_year_item = Integer.parseInt(CacheUtils.getString(RegActivity.this, "dialog_year_item"));
            this.current_month_item = Integer.parseInt(CacheUtils.getString(RegActivity.this, "dialog_month_item"));
            this.current_day_item = Integer.parseInt(CacheUtils.getString(RegActivity.this, "dialog_day_item"));
        }
        showDialog(DATE_DIALOG_ID);
//        new DatePickerDialog(this,
//                new DatePickerDialog.OnDateSetListener() {
//                    // 这三个参数就是用户选择完成时的时间
//                    @Override
//                    public void onDateSet(DatePicker view, int year, int monthOfYear,
//                                          int dayOfMonth) {
//                        if (!view.isShown()) return;
//
//
//                        Calendar cal = Calendar.getInstance();
//
//                        cal.set(year, monthOfYear, dayOfMonth);
//
//                        if (cal.getTimeInMillis() > System.currentTimeMillis()) return;
//
//
//                        StringBuilder sb = new StringBuilder();
//
//
//                        sb.append(year).append("-").append(monthOfYear + 1).append("-").append(dayOfMonth);
//
//
//                        if ("en".equals(PublicData.currentLang)) {
//                            reg_birthday.setText((monthOfYear + 1) + " / " + dayOfMonth + " / " + year);
////							reg_birthday.setText(PublicData.english_monthL[monthOfYear] + " "+ dayOfMonth +" " + year);
//                        } else
//                            reg_birthday.setText(dayOfMonth + " / " + (monthOfYear + 1) + " / " + year);
//
//                        reg_birthS = sb.toString();
//
//                        //	reg_birthday.setText(sb.toString());
//                        Logger.d(TAG, ">>date:" + sb.toString());
//
//                    }
//
//                },
//                year, month, day).show();

        Logger.d(TAG, "----选择返回:" + strDate);
    }

    /**
     * 注册线程
     */
    Runnable regRunnable = new Runnable() {

        @Override
        public void run() {
            Logger.d(TAG, "---regRunnable---");

            PropertiesUtil pu = new PropertiesUtil();
            pu.initResRawPropFile(RegActivity.this, R.raw.server);

            Properties props = pu.getPropsObj();
            String url = props.getProperty("server.reg.address", "http://app.appscomm.cn/sport/api/reg_for_sunshine");


            String height = height_textview_tmp.getText().toString(); // mHeight;//reg_height.getText().toString().split(" ")[0];
            String weight = weight_textview_tmp.getText().toString(); // mWeight;//reg_weight.getText().toString().split(" ")[0];


            if (PublicData.heightVal_unit == 1) { // ft in
                heightUnit = "1";
            }
            if (PublicData.weightVal_unit == 1) { // lbs
                weightUnit = "1";
            }

            String method = "post";
//            String params = "userName=" + reg_username.getText().toString() + "&email=" + reg_email.getText().toString() + "&password=" + CommonUtil.MD5(reg_password.getText().toString()) + "&gender=" + sex
//                    + "&birthDay=" + reg_birthS + "&height=" + height + "&weight=" + weight + "&heightUnit=" + heightUnit + "&weightUnit=" + weightUnit + "&countryCode=" + countryCode
//                    + "&imgUrl=" + regImgUrl + "&encryptMode=1" + "&customer=" + "sunshine" + "&encryptMode=0" + "&client=" + "android";
            String params = "userName=" + reg_username.getText().toString() + "&email=" + reg_email.getText().toString() + "&password=" + CommonUtil.MD5(reg_password.getText().toString()) + "&gender=" + sex
                    + "&birthDay=" + reg_birthS + "&height=" + height + "&weight=" + weight + "&heightUnit=" + heightUnit + "&weightUnit=" + weightUnit + "&countryCode=" + countryCode
                    + "&imgUrl=" + regImgUrl + "&encryptMode=1" + "&customer=" + "appscomm" + "&encryptMode=0" + "&client=" + "android";

            Logger.d(TAG, "请求地址url：" + url + " params: " + params);

            int respondStatus = httpUtil.httpReq(method, url, params);
            String respondBody = httpUtil.httpResponseResult;


            HttpResDataService httpResDataService = new HttpResDataService(getApplicationContext());

            int i = httpResDataService.commonParse(respondStatus, respondBody, "1");

            Logger.i(TAG, "------------->>>:" + i);

//			int i = httpUtil.reg(reg_username.getText().toString(), reg_password.getText().toString(), 
//					reg_email.getText().toString(), "1", 
//					reg_birthday.getText().toString(), 
//					reg_height.getText().toString(), 
//					reg_weight.getText().toString());

            switch (i) {
                case 0: //注册成功
                    regHandler.obtainMessage(0, "REG SUCCESS!").sendToTarget();

                    break;

                case 1: //服务器返回的错误归类
                    String resultCode = httpResDataService.getResultCode();
                    //String msg = httpResDataService.getMessage();
                    String msg = "";

                    if ("1101".equals(resultCode)) {
                        msg = getString(R.string.login_username_wrong);

                    } else if ("1102".equals(resultCode)) {
                        msg = getString(R.string.login_username_exist);

                    } else if ("1105".equals(resultCode)) {
                        msg = getString(R.string.email_exist);

                    }

                    if ("".equals(msg)) {
                        msg = "[" + resultCode + "]ERROR!";
                    }
                    Logger.e(TAG, "msg=>>" + msg);

                    regHandler.obtainMessage(1, msg).sendToTarget();
                    break;

                case 2: //错误的响应信息码
                    regHandler.obtainMessage(2, "ERROR RESPOND INFO!").sendToTarget();
                    break;

                case 3: //JSONException
                    regHandler.obtainMessage(3, "JSONException!").sendToTarget();
                    break;

                case -1: //服务器未响应
                    regHandler.obtainMessage(-1, "SERVER IS NOT RESPOND!").sendToTarget();
                    break;

                case -2: //ClientProtocolException
                    regHandler.obtainMessage(-2, "ClientProtocolException!").sendToTarget();
                    break;

                case -3: //ParseException
                    regHandler.obtainMessage(-3, "ParseException!").sendToTarget();
                    break;

                case -4: //IOException
                    regHandler.obtainMessage(-4, "IOException!").sendToTarget();
                    break;

            }

        }

    };

    Runnable UpLoadimgRunnable = new Runnable() {

        @Override
        public void run() {
            Logger.d(TAG, "---uploadImgRunnable---");

            PropertiesUtil pu = new PropertiesUtil();
            pu.initResRawPropFile(RegActivity.this, R.raw.server);

            Properties props = pu.getPropsObj();
            String url = props.getProperty("server.uploadimg", "http://app.appscomm.cn/sport/api/reg_for_france");

            Logger.d(TAG, "请求地址：" + url);


            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(url);
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            /*    try {
                    reqEntity.addPart("userId", new StringBody(""));
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}  */  //无需传userid,传空会异常

            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                File f1 = new File(PublicData.SAVE_IMG_PATH, BaseSettingActivity.CROPED_FACE_IMG);

                if (!f1.exists()) {
                    regImgUrl = "";

                    regThread = new Thread(regRunnable);
                    regThread.start();
                    return;
                }


                FileInputStream ins = new FileInputStream(f1);
                byte[] data = new byte[(int) f1.length()];

                ins.read(data, 0, (int) f1.length());

                //f1.length()
                //  Bit.compress(CompressFormat.JPEG, 75, bos);
                //byte[] data = bos.toByteArray();
                ByteArrayBody bab = new ByteArrayBody(data, "kfc.jpg");
                reqEntity.addPart("photo", bab);
            } catch (Exception e) {
                try {
                    reqEntity.addPart("photo", new StringBody("image error"));
                } catch (UnsupportedEncodingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            postRequest.setEntity(reqEntity);
            HttpResponse response = null;
            try {
                response = httpClient.execute(postRequest);
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String sResponse;
            StringBuilder s = new StringBuilder();
            try {
                while ((sResponse = reader.readLine()) != null) {
                    s = s.append(sResponse);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            String resultS = s.toString();

            if (resultS.indexOf("\"result\"") != -1 && resultS.indexOf("\"message\"") != -1
                    && resultS.indexOf("\"data\"") != -1) {

                JSONObject jsonObj;
                try {
                    jsonObj = new JSONObject(resultS);
                    String result = jsonObj.getString("result");

                    if (result.equals("0")) {
                        JSONObject jsonObj2 = jsonObj.getJSONObject("data");
                        regImgUrl = jsonObj2.getString("imgUrl");


                        Logger.d(TAG, "upload sucessful,:" + regImgUrl);

                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }


            regThread = new Thread(regRunnable);
            regThread.start();
            Logger.d(TAG, "upLoadImg result:" + s);
        }

    };

    private Handler regHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 0: // 注册成功
                    Logger.d(TAG, (String) msg.obj);
                    String params = "userName=" + reg_username.getText().toString() + "&email=" + reg_email.getText().toString() + "&password=" + CommonUtil.MD5(reg_password.getText().toString()) + "&gender=" + sex
                            + "&birthDay=" + reg_birthday.getText().toString() + "&height=" + reg_height.getText().toString() + "&weight=" + reg_weight.getText().toString() + "&validCode=&encryptMode=1";

                    Context context = RegActivity.this;
                    boolean isMale = false;
                    if ("0".equals(sex)) {
                        isMale = true;
                    }

                    String obj[] = reg_birthS.split("-");
                    int year = 1980;
                    int month = 1;
                    int day = 1;
                    if (obj.length == 3) {
                        year = Integer.parseInt(obj[0]);
                        month = Integer.parseInt(obj[1]);
                        day = Integer.parseInt(obj[2]);
                    }
                    int height = 170;
                    int weight = 60;
                    if (!"".equals(reg_height.getText().toString()) && CommonUtil.isNumeric(reg_height.getText().toString())) {
                        height = Integer.parseInt(reg_height.getText().toString());
                    }
                    if (!"".equals(reg_weight.getText().toString()) && CommonUtil.isNumeric(reg_weight.getText().toString())) {
                        weight = Integer.parseInt(reg_weight.getText().toString());
                    }

//				height = (int) Double.parseDouble(mHeight);
//				weight = (int) Double.parseDouble(mWeight);

                    //保存邮箱
                    ConfigHelper.setSharePref(RegActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.CURRENT_EMAIL_ITEM_KEY, reg_email.getText().toString().trim());
                    //保存用户名
                    ConfigHelper.setSharePref(RegActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.CURRENT_NAME_ITEM_KEY, reg_username.getText().toString());
                    ConfigHelper.setSharePref(RegActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.LOGIN_USERNAME_KEY, reg_email.getText().toString());


                    //保存性别
                    ConfigHelper.setSharePref(RegActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.SEX_ITEM_KEY, isMale);
                    //保存出生年
                    ConfigHelper.setSharePref(RegActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.YEAR_ITEM_KEY, year);
                    //保存出生月
                    ConfigHelper.setSharePref(RegActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.MONTH_ITEM_KEY, month);
                    //保存出生日
                    ConfigHelper.setSharePref(RegActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.DAY_ITEM_KEY, day);
                    //保存身高
                    ConfigHelper.setSharePref(RegActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.HEIGHT_ITEM_KEY, height);
                    //保存体重
                    ConfigHelper.setSharePref(RegActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.WEIGHT_ITEM_KEY, weight);
                    //保存邮箱
                /*ConfigHelper.setSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                        PublicData.CURRENT_EMAIL_ITEM_KEY, mail);
				ConfigHelper.setSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, 
						PublicData.CURRENT_EMAIL_ITEM_KEY+ "1", mail);
				//保存用户名
				ConfigHelper.setSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, 
						PublicData.CURRENT_NAME_ITEM_KEY, userName);
				//保存加密后的密码
*/
                    ConfigHelper.setSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.CURRENT_PASSWORD_ITEM_KEY, reg_password.getText().toString());
                    //保存性别


                    ConfigHelper.setSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.LOGIN_PASSWORD_KEY, reg_password.getText().toString());


			/*	//保存身高
                ConfigHelper.setSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
						PublicData.HEIGHT_ITEM_KEY, Math.round(Float.parseFloat(height)));
				//保存体重
				ConfigHelper.setSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, 
						PublicData.WEIGHT_ITEM_KEY, Math.round(Float.parseFloat(weight)));*/

                    ConfigHelper.setSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INFO_HEIGHT, String.valueOf(height));
                    ConfigHelper.setSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INFO_WEIGHT, String.valueOf(weight));
                    Logger.i("", "注册成功保存的身高 = " + height);
                    Logger.i("", "注册成功保存的体重 = " + weight);
                    Logger.i("", "注册成功保存的身高 = " + ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INFO_HEIGHT, ConfigHelper.DATA_STRING));
                    Logger.i("", "注册成功保存的体重 = " + ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INFO_WEIGHT, ConfigHelper.DATA_STRING));


                    ConfigHelper.setSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, "heightunit", String.valueOf(PublicData.heightVal_unit));
                    ConfigHelper.setSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, "weightunit", String.valueOf(PublicData.weightVal_unit));

                    ConfigHelper.setSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.COUNTRY_CODE_KEY, countryCode);
                    ConfigHelper.setSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.IMG_FACE_KEY, regImgUrl);


                    ConfigHelper.setSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.UNIT_KEY, heightUnit);
                    Logger.i("", "注册成功身高单位 = " + heightUnit);
                    Logger.i("", "注册成功单位数值 = " + ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.UNIT_KEY, ConfigHelper.DATA_STRING));


                    File imgfile2 = new File(PublicData.SAVE_IMG_PATH, BaseSettingActivity.SAVED_FACE_IMG);

                    if (imgfile2.exists()) {
                        imgfile2.delete();
                    }

                    if (regProgressDialog != null)
                        regProgressDialog.dismiss();


                    Intent intent = new Intent();
                    intent.setClass(RegActivity.this, LoginActivity.class);

				/*//注册时把之前有可能绑定的清空  ??
                ConfigHelper.setSharePref(RegActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
							PublicData.CURRENT_BIND_ID_ITEM, "");*/

                    Bundle bundle = new Bundle();
                    bundle.putString("result", "REG");
                    intent.putExtras(bundle);

                    //TODO 保存选择的单位
//                    mTmpUnit = 1;
                    editor.putString("unit_value", mTmpUnit + "");
                    editor.commit();
                    CacheUtils.putString(RegActivity.this, "unit_value", mTmpUnit + "");
                    CacheUtils.setUnit(RegActivity.this, mTmpUnit + "");

                    startActivity(intent);
                    regHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            finish();
                        }
                    }, 1000);


                    break;

                case 1: //服务器返回错误码归类说明
                    Logger.d(TAG, (String) msg.obj);
                    DialogUtil.commonDialog(RegActivity.this,
                            RegActivity.this.getString(R.string.app_name),
                            (String) msg.obj);
                    if (regProgressDialog != null)
                        regProgressDialog.dismiss();

                    break;

                case -1: // 服务器无响应
                    Logger.d(TAG, (String) msg.obj);
                    DialogUtil.commonDialog(RegActivity.this,
                            RegActivity.this.getString(R.string.app_name),
                            "Server not respond");
                    if (regProgressDialog != null)
                        regProgressDialog.dismiss();

                    break;

                case -2: // ClientProtocolException
                    Logger.d(TAG, (String) msg.obj);
                    DialogUtil.commonDialog(RegActivity.this,
                            RegActivity.this.getString(R.string.app_name),
                            "ClientProtocolException");
                    if (regProgressDialog != null)
                        regProgressDialog.dismiss();

                    break;

                case -3: // ParseException
                    Logger.d(TAG, (String) msg.obj);
                    DialogUtil.commonDialog(RegActivity.this,
                            RegActivity.this.getString(R.string.app_name),
                            "ParseException");
                    if (regProgressDialog != null)
                        regProgressDialog.dismiss();

                    break;

                case -4: // IOException
                    Logger.d(TAG, (String) msg.obj);
                    DialogUtil.commonDialog(RegActivity.this,
                            RegActivity.this.getString(R.string.app_name),
                            RegActivity.this.getString(R.string.IOException));
                    if (regProgressDialog != null)
                        regProgressDialog.dismiss();

                    break;

                case 3: // 没有网络连接
                    Logger.d(TAG, (String) msg.obj);
                    DialogUtil.commonDialog(RegActivity.this,
                            RegActivity.this.getString(R.string.app_name),
                            getString(R.string.NetWorkError));
                    if (regProgressDialog != null)
                        regProgressDialog.dismiss();

                    break;


            }
            super.handleMessage(msg);
        }

    };

    private class SpinnerAdapter extends ArrayAdapter<String> {
        Context context;
        String[] items = new String[]{};

        public SpinnerAdapter(final Context context,
                              final int textViewResourceId, final String[] objects) {
            super(context, textViewResourceId, objects);
            this.items = objects;
            this.context = context;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(
                        android.R.layout.simple_spinner_item, parent, false);
            }

            TextView tv = (TextView) convertView
                    .findViewById(android.R.id.text1);
            tv.setText(items[position]);
            tv.setTextColor(Color.parseColor("#000000"));
            tv.setTextSize(15);
            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(
                        android.R.layout.simple_spinner_item, parent, false);
            }

            // android.R.id.text1 is default text view in resource of the android.
            // android.R.layout.simple_spinner_item is default layout in resources of android.

            TextView tv = (TextView) convertView
                    .findViewById(android.R.id.text1);
            tv.setText(items[position]);
            tv.setTextColor(Color.parseColor("#000000"));
            tv.setTextSize(15);
            return convertView;
        }
    }

    /**
     * 下拉框身高事件
     */
    public OnItemSelectedListener mSelectedHeightEvent = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> adapter, View view, int position,
                                   long id) {
            Logger.d(TAG, ">>>>>>>>>>>>>>>>>>>>position:" + position + "   id:" + id);
            reg_height.setText((String) spHeight.getAdapter().getItem(position));

        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {

        }
    };


    /**
     * 下拉框体重事件
     */
    public OnItemSelectedListener mSelectedWeightEvent = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> adapter, View view, int position,
                                   long id) {
            Logger.d(TAG, ">>>>>>>>>>>>>>>>>>>>position:" + position + "   id:" + id);
            reg_weight.setText((String) spWeight.getAdapter().getItem(position));

        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {

        }
    };

    // 输入框失去焦点及隐藏键盘
    private void edittext_hide_input() {
//		edittext_name.setFocusable(false);
//		inputManager.showSoftInput(edittext_name, 0);
//		inputManager.hideSoftInputFromInputMethod(edittext_name.getWindowToken(),InputMethodManager.RESULT_HIDDEN);
    }


    public void gender_clicked(View view) {
        edittext_hide_input();
        if (wheelWindowgender == null) {
            wheelWindowgender = new SelectWheelPopupWindow(this, arrgender, 8, 0,
                    getResources().getString(R.string.gender), new OnWheelScrollListener() {
                public void onScrollingStarted(WheelView wheel) {

                }

                public void onScrollingFinished(WheelView wheel) {
                    int index = wheel.getCurrentItem();
                    //ConfigHelper.setSharePref(RegActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.HEIGHT_ITEM_KEY, current_height_item);
                    tv_gender.setText(arrgender[index]);
                    sex = Integer.toString(index);
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.WheelGender:
                            wheelWindowgender.dismiss();
                            break;
                        case R.id.WheelDone:
                            wheelWindowgender.dismiss();
                            break;
                    }
                }
            });

            sex = Integer.toString(0); //初始化是femal
            tv_gender.setText(arrgender[0]);

        }
        //显示窗口 //设置layout在PopupWindow中显示的位置
        wheelWindowgender.showAtLocation(this.findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

    }

    // 身高
    public void height_clicked(View view) {
        edittext_hide_input();
        cur_height_unit = PublicData.heightVal_unit;
        Logger.d(TAG, "<<===mTmpUnit:" + mTmpUnit + "/cur_height_unit=" + PublicData.heightVal_unit);

        if (wheelWindowHeight != null) {
            wheelWindowHeight.dismiss();
            wheelWindowHeight = null;
        }

        String sheight = height_textview_tmp.getText().toString();
        String sweight = weight_textview_tmp.getText().toString();

        int height_index1 = 0, height_index2 = 0;

        Logger.e(TAG, "item_sheight=" + sheight);
        try {
            if (cur_height_unit == 1) {
                //英制单位
                height_index1 = Integer.parseInt(sheight) / 12 - 3;
                height_index2 = Integer.parseInt(sheight) % 12;
            } else {
                //公制单位
                String[] s1 = sheight.split("\\.");
                if (s1 != null) {
                    if (s1.length == 1) {
                        height_index1 = Integer.parseInt(s1[0]) - 90;
                        height_index2 = 0;
                    } else if (s1.length == 2) //may have decimal..
                    {
                        height_index1 = Integer.parseInt(s1[0]) - 90;
                        height_index2 = Integer.parseInt(s1[1].substring(0, 1));

                    }
                }
            }
        } catch (Exception e) {
        }

        if (wheelWindowHeight == null)
            // wheelWindowHeigh = new SelectWheelPopupWindow(this, arrHeight, 8,
            // current_height_item, new OnWheelScrollListener() {

            wheelWindowHeight = new NewHeightPop(this, PublicData.arrHeight_Int, height_index1, null, height_index2, null, mTmpUnit, 8,
                    new View.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            // TODO Auto-generated method stub

                            switch (arg0.getId()) {
                                case R.id.HeightWheelSave:

                                    Logger.e(TAG, "heightVal_unit=" + PublicData.heightVal_unit);
                                    if (PublicData.heightVal_unit == 1) {
                                        PublicData.heightVal_unit = 1;
                                        height_textview.setText(PublicData.arrHeightFt_Int[PublicData.heightVal_int] + "" + PublicData.heightVal_dec + "\" "
                                                + arrHeightUnit[PublicData.heightVal_unit]);
                                        current_height_item = PublicData.heightVal_int;
                                        heightVal_s = PublicData.arrHeightFt_Int[current_height_item];

                                        mHeight = String.valueOf(Integer.parseInt(heightVal_s.split("'")[0]) * 12 + PublicData.heightVal_dec);// ft
                                        height_textview_tmp.setText(mHeight);

                                        mTmpUnit = 1;

                                        // 联动更新对应体重显示及隐藏值
//									if (PublicData.weightVal_unit == 0) { // 是kg
//										PublicData.weightVal_unit = 1;
//                                        Logger.d(TAG,"textview.text is :" + weight_textview_tmp.getText().toString());
//										double tmp = (float) Float.valueOf(weight_textview_tmp.getText().toString()); // kg
//										tmp = (double) (tmp * 2.2046226); // kg
//																			// >
//																			// lbs
//										/*
//										 * String str = Double.toString(tmp);
//										 * 
//										 * int start = str.indexOf(".");
//										 * 
//										 * String a0 = "" + (int) tmp; try { a0
//										 * = str.substring(0, start + 2);
//										 * Logger.d(TAG, "-----a0:" + a0); }
//										 * 
//										 * catch (Exception e) { a0 = "" + (int)
//										 * tmp; Logger.d(TAG, "-----a0..EXCEpion");
//										 * // TODO: handle exception }
//										 */
//
//										String a0 = String.format("%.2f", tmp);
//                                        Logger.d(TAG,".............."+ a0);
//                                     a0=   a0.replace(',','.');
//                                        Logger.d(TAG,".............."+ a0);
//										weight_textview.setText(a0 + " " + PublicData.arrWeightUnit[PublicData.weightVal_unit]);
//										weight_textview_tmp.setText(a0);
//
//									}

                                        PublicData.weightVal_unit = 1;
                                    } else {
                                        PublicData.heightVal_unit = 0;
                                        height_textview.setText(PublicData.arrHeight_Int[PublicData.heightVal_int] + '.' + PublicData.heightVal_dec + " "
                                                + arrHeightUnit[PublicData.heightVal_unit]);

                                        current_height_item = PublicData.heightVal_int;
                                        heightVal_s = PublicData.arrHeight_Int[current_height_item];

                                        mHeight = heightVal_s + "." + PublicData.heightVal_dec;
                                        height_textview_tmp.setText(mHeight);

                                        mTmpUnit = 0;

//									// 联动更新对应体重显示及隐藏值
//									if (PublicData.weightVal_unit == 1) { // 是lbs
//										PublicData.weightVal_unit = 0;
//										double tmp = (float) Float.valueOf(weight_textview_tmp.getText().toString()); // lbs
//										tmp = (double) (tmp * 0.4535924); // lbs
//																			// >
//																			// kg
//										/*
//										 * String str = Double.toString(tmp);
//										 * 
//										 * int start = str.indexOf(".");
//										 * 
//										 * String a0 = "" + (int) tmp; try { a0
//										 * = str.substring(0, start + 2);
//										 * Logger.d(TAG, "-----a0:" + a0); }
//										 * 
//										 * catch (Exception e) { a0 = "" + (int)
//										 * tmp; Logger.d(TAG, "-----a0..EXCEpion");
//										 * // TODO: handle exception }
//										 */
//
//										String a0 = String.format("%.2f", tmp);
//                                        Logger.d(TAG,".............."+ a0);
//                                        a0=     a0.replace(',','.');
//                                        Logger.d(TAG,".............."+ a0);
//										weight_textview.setText(a0 + " " + PublicData.arrWeightUnit[PublicData.weightVal_unit]);
//										weight_textview_tmp.setText(a0);
//
//									}

                                        PublicData.weightVal_unit = 0;
                                    }

                                    Logger.d(TAG, "<<===onclick save /mTmpUnit:" + mTmpUnit);
                                    wheelWindowHeight.dismiss();
                                    break;

                                case R.id.HeightWheelCancel:
                                default:
                                    wheelWindowHeight.dismiss();
                                    break;
                            }

                            // ConfigHelper.setSharePref(BaseSettingActivity.this,
                            // PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            // PublicData.HEIGHT_ITEM_KEY, current_height_item);

                        }
                    });

        // 显示窗口 //设置layout在PopupWindow中显示的位置
        wheelWindowHeight.showAtLocation(this.findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

    }

    // 身高
    public void height_clicked1(View view) {
        edittext_hide_input();

        if (wheelWindowHeight != null) {
            wheelWindowHeight.dismiss();
            wheelWindowHeight = null;
        }


        if (wheelWindowHeight == null)


            wheelWindowHeight = new NewHeightPop(this, PublicData.arrHeight_Int, current_height_item, null, 0, null, PublicData.heightVal_unit, 8,
                    new View.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            // TODO Auto-generated method stub

                            switch (arg0.getId()) {
                                case R.id.HeightWheelSave:

                                    if (PublicData.heightVal_unit == 1) {
                                        rbn_unit_Metric.setChecked(false);        // summer: add
                                        rbn_unit_US.setChecked(true);

                                        height_textview.setText(PublicData.arrHeightFt_Int[PublicData.heightVal_int] + "'" + PublicData.heightVal_dec + "\"   " + arrHeightUnit[PublicData.heightVal_unit]);
                                        reg_height.setText(PublicData.arrHeightFt_Int[PublicData.heightVal_int]);

                                        String str = PublicData.arrHeightFt_Int[PublicData.heightVal_int];
                                        mHeight = String.valueOf(Integer.parseInt(str.split("'")[0]) * 12 + PublicData.heightVal_dec);//ft in

                                        height_textview_tmp.setText(mHeight);

                                        //联动更新对应体重显示及隐藏值
                                        if (PublicData.weightVal_unit == 0) { //是kg
                                            PublicData.weightVal_unit = 1;
                                            double tmp = (float) Float.valueOf(weight_textview_tmp.getText().toString()); //kg
                                            tmp = (double) (tmp * 2.2046226); //kg > lbs
                                            String str0 = Double.toString(tmp);


                                            int start = str0.indexOf(".");

                                            String a0 = "" + (int) tmp;
                                            try {
                                                a0 = str.substring(0, start + 2);
                                                Logger.d(TAG, "-----a0:" + a0);
                                            } catch (Exception e) {
                                                a0 = "" + (int) tmp;
                                                Logger.d(TAG, "-----a0..EXCEpion");
                                                // TODO: handle exception
                                            }


                                            weight_textview.setText(a0 + " " + arrWeightUnit[PublicData.weightVal_unit]);
                                            weight_textview_tmp.setText(a0);

                                        }
                                    } else {
                                        rbn_unit_Metric.setChecked(true);        // summer: add
                                        rbn_unit_US.setChecked(false);

                                        height_textview.setText(PublicData.arrHeight_Int[PublicData.heightVal_int] + '.' + PublicData.heightVal_dec + "   " + arrHeightUnit[PublicData.heightVal_unit]);
                                        reg_height.setText(PublicData.arrHeight_Int[PublicData.heightVal_int]);

                                        mHeight = PublicData.arrHeight_Int[PublicData.heightVal_int] + "." + PublicData.heightVal_dec;

                                        height_textview_tmp.setText(mHeight);

                                        //联动更新对应体重显示及隐藏值
                                        if (PublicData.weightVal_unit == 1) { //是lbs
                                            PublicData.weightVal_unit = 0;
                                            double tmp = (float) Float.valueOf(weight_textview_tmp.getText().toString()); //lbs
                                            tmp = (double) (tmp * 0.4535924); //lbs > kg
                                            String str = Double.toString(tmp);

                                            int start = str.indexOf(".");

                                            String a0 = "" + (int) tmp;
                                            try {
                                                a0 = str.substring(0, start + 2);
                                                Logger.d(TAG, "-----a0:" + a0);
                                            } catch (Exception e) {
                                                a0 = "" + (int) tmp;
                                                Logger.d(TAG, "-----a0..EXCEpion");
                                                // TODO: handle exception
                                            }


                                            weight_textview.setText(a0 + " " + arrWeightUnit[PublicData.weightVal_unit]);
                                            weight_textview_tmp.setText(a0);

                                        }
                                    }

                                    wheelWindowHeight.dismiss();
                                    break;

                                case R.id.HeightWheelCancel:
                                default:
                                    wheelWindowHeight.dismiss();
                                    break;
                            }


                            //ConfigHelper.setSharePref(BaseSettingActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.HEIGHT_ITEM_KEY, current_height_item);


                        }
                    });


        //显示窗口 //设置layout在PopupWindow中显示的位置
        wheelWindowHeight.showAtLocation(this.findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

    }

    // 体重
    public void weight_clicked(View view) {
        edittext_hide_input();
        cur_weight_unit = PublicData.weightVal_unit;
        Logger.d(TAG, "<<===mTmpUnit:" + mTmpUnit);

        if (wheelWindowWeight != null) {
            wheelWindowWeight.dismiss();
            wheelWindowWeight = null;

        }


        String sheight = height_textview_tmp.getText().toString();
        String sweight = weight_textview_tmp.getText().toString();

        int weight_index1 = 0, weight_index2 = 0;

        try {
            String[] s1 = sweight.split("\\.");

            if (s1 != null) {
                if (s1.length == 1) {
                    if (cur_weight_unit == 1) { //1-英制
                        weight_index1 = Integer.parseInt(s1[0]) - 70;
                    } else {
                        weight_index1 = Integer.parseInt(s1[0]) - 30;
                    }

                    weight_index2 = 0;
                } else if (s1.length == 2) {//may have decimal..
                    if (cur_weight_unit == 1) {    //1-英制
                        weight_index1 = Integer.parseInt(s1[0]) - 70;
                    } else {
                        weight_index1 = Integer.parseInt(s1[0]) - 30;
                    }

                    weight_index2 = Integer.parseInt(s1[1].substring(0, 1));
                }
            }
        } catch (Exception e) {
        }


        if (wheelWindowWeight == null)
            wheelWindowWeight = new NewWeightPop(this, PublicData.arrWeight_Int, weight_index1, null, weight_index2, null, mTmpUnit, 8,
                    new View.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            // TODO Auto-generated method stub

                            switch (arg0.getId()) {
                                case R.id.WeightWheelSave:

                                    if (PublicData.weightVal_unit == 1) {//1-英制
                                        PublicData.weightVal_unit = 1;
                                        weight_textview.setText(PublicData.arrWeightlbs_Int[PublicData.weightVal_int] + "." + PublicData.weightVal_dec + " "
                                                + arrWeightUnit[PublicData.weightVal_unit]);
                                        current_weight_item = PublicData.weightVal_int;
                                        weightVal_s = PublicData.arrWeightlbs_Int[current_weight_item];

                                        mWeight = PublicData.arrWeightlbs_Int[PublicData.weightVal_int] + "." + PublicData.weightVal_dec;
                                        weight_textview_tmp.setText(mWeight);

                                        mTmpUnit = 1;
//									// 联动更新对应身高显示及隐藏值
//									if (PublicData.heightVal_unit == 0) { // 是cm
//										PublicData.heightVal_unit = 1;
//										int tmp = (int) (float) Float.valueOf(height_textview_tmp.getText().toString()); // cm
//										tmp = (int) (double) (tmp * 0.3937008); // cm
//																				// >
//																				// ft
//																				// in
//										int a1 = tmp / 12;
//										int a2 = tmp % 12;
//
//										if (a1 > 7) {
//										a1 = 7;
//									} else if (a1 < 3) {
//										a1 = 3;
//									}
//									tmp = a1 * 12 + a2;
//									height_textview.setText(a1 + "'" + a2 + "\" " + PublicData.arrHeightUnit[PublicData.heightVal_unit]);
//									height_textview_tmp.setText("" + tmp);
//
//								}

                                        PublicData.heightVal_unit = 1;

                                    } else {
                                        PublicData.weightVal_unit = 0;
                                        weight_textview.setText(PublicData.arrWeight_Int[PublicData.weightVal_int] + '.' + PublicData.weightVal_dec + " "
                                                + arrWeightUnit[PublicData.weightVal_unit]);
                                        current_weight_item = PublicData.weightVal_int;
                                        weightVal_s = PublicData.arrWeight_Int[current_weight_item];

                                        mWeight = PublicData.arrWeight_Int[PublicData.weightVal_int] + "." + PublicData.weightVal_dec;
                                        weight_textview_tmp.setText(mWeight);

                                        mTmpUnit = 0;
//									// 联动更新对应身高显示及隐藏值
//									if (PublicData.heightVal_unit == 1) { // 是ft
//																			// in
//										PublicData.heightVal_unit = 0;
//										int tmp = (int) (float) Float.valueOf(height_textview_tmp.getText().toString()); // ft
//																															// in
//										tmp = (int) (double) (tmp * 2.54); // ft
//																			// in
//																			// >
//																			// cm
//
//										height_textview.setText(tmp + " " + PublicData.arrHeightUnit[PublicData.heightVal_unit]);
//										height_textview_tmp.setText("" + tmp);
//
//									}

                                        PublicData.heightVal_unit = 0;
                                    }
                                    Logger.d(TAG, "<<===onclick save /mTmpUnit:" + mTmpUnit);

                                    wheelWindowWeight.dismiss();
                                    break;

                                case R.id.WeightWheelCancel:
                                default:
                                    wheelWindowWeight.dismiss();
                                    break;

                            }
                            wheelWindowWeight.dismiss();
                        }
                    });

        // 显示窗口 //设置layout在PopupWindow中显示的位置
        wheelWindowWeight.showAtLocation(this.findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    // 体重
    public void weight_clicked1(View view) {
        edittext_hide_input();

        if (wheelWindowWeight != null) {
            wheelWindowWeight.dismiss();
            wheelWindowWeight = null;

        }

        if (wheelWindowWeight == null)
            wheelWindowWeight = new NewWeightPop(this, PublicData.arrWeight_Int, current_weight_item, null, 0, null, PublicData.weightVal_unit, 8, new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub

                    switch (arg0.getId()) {
                        case R.id.WeightWheelSave:

                            if (PublicData.weightVal_unit == 1) {
                                rbn_unit_Metric.setChecked(false);        // summer: add
                                rbn_unit_US.setChecked(true);

                                weight_textview.setText(PublicData.arrWeightlbs_Int[PublicData.weightVal_int] + "." + PublicData.weightVal_dec + "   " + arrWeightUnit[PublicData.weightVal_unit]);
                                reg_weight.setText(PublicData.arrWeightlbs_Int[PublicData.weightVal_int]);

                                mWeight = PublicData.arrWeightlbs_Int[PublicData.weightVal_int] + "." + PublicData.weightVal_dec;

                                weight_textview_tmp.setText(mWeight);
                                //联动更新对应身高显示及隐藏值
                                if (PublicData.heightVal_unit == 0) { //是cm
                                    PublicData.heightVal_unit = 1;
                                    int tmp = (int) (float) Float.valueOf(height_textview_tmp.getText().toString()); //cm
                                    tmp = (int) (double) (tmp * 0.3937008); //cm > ft in
                                    int a1 = tmp / 12;
                                    int a2 = tmp % 12;

                                    if (a1 > 7) {
                                        a1 = 7;
                                    } else if (a1 < 3) {
                                        a1 = 3;
                                    }
                                    tmp = a1 * 12 + a2;
                                    height_textview.setText(a1 + "'" + a2 + "\" " + arrHeightUnit[PublicData.heightVal_unit]);
                                    height_textview_tmp.setText("" + tmp);

                                }
                            } else {
                                rbn_unit_Metric.setChecked(true);        // summer: add
                                rbn_unit_US.setChecked(false);

                                weight_textview.setText(PublicData.arrWeight_Int[PublicData.weightVal_int] + '.' + PublicData.weightVal_dec + "   " + arrWeightUnit[PublicData.weightVal_unit]);
                                reg_weight.setText(PublicData.arrWeight_Int[PublicData.weightVal_int]);

                                mWeight = PublicData.arrWeight_Int[PublicData.weightVal_int] + "." + PublicData.weightVal_dec;

                                weight_textview_tmp.setText(mWeight);

                                //联动更新对应身高显示及隐藏值
                                if (PublicData.heightVal_unit == 1) { //是ft in
                                    PublicData.heightVal_unit = 0;
                                    int tmp = (int) (float) Float.valueOf(height_textview_tmp.getText().toString()); //ft in
                                    tmp = (int) (double) (tmp * 2.54); //ft in > cm

                                    height_textview.setText(tmp + " " + arrHeightUnit[PublicData.heightVal_unit]);
                                    height_textview_tmp.setText("" + tmp);

                                }
                            }
                            wheelWindowWeight.dismiss();
                            break;

                        case R.id.WeightWheelCancel:
                        default:
                            wheelWindowWeight.dismiss();
                            break;


                    }
                    wheelWindowWeight.dismiss();
                }
            }
            );

        //显示窗口 //设置layout在PopupWindow中显示的位置
        wheelWindowWeight.showAtLocation(this.findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

    }

    @Override
    protected void onDestroy() {
        CacheUtils.putString(RegActivity.this, "dialog_year_item", "");
        CacheUtils.putString(RegActivity.this, "dialog_month_item", "");
        CacheUtils.putString(RegActivity.this, "dialog_day_item", "");
        AppManager.activityStack.remove(this);

        super.onDestroy();
    }
}
