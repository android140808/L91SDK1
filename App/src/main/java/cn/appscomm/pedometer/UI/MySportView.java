package cn.appscomm.pedometer.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import apps.utils.CommonUtil;
import apps.utils.ConfigHelper;
import apps.utils.Logger;
import apps.utils.PublicData;
import cn.appscomm.pedometer.activity.MainActivity;
import cn.appscomm.pedometer.avater.CacheUtils;
import cn.appscomm.pedometer.model.ISetViewVal;
import cn.l11.appscomm.pedometer.activity.R;

public class MySportView extends View {
    public static class Point {
        public static final Comparator<Point> X_COMPARATOR = new Comparator<Point>() {
            @Override
            public int compare(Point lhs, Point rhs) {
                Logger.i(TAGGE, "---------------Point compare");
                return (int) (lhs.x * 1000 - rhs.x * 1000);
            }
        };

        public float x;
        public float y;

        public Point(float x, float y) {
            Logger.i(TAGGE, "---------------Point Point");

            this.x = x;
            this.y = y;
        }

        public Point() {
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    private final static String TAGGE = "test_test" + "MySportView";
    private Context context;
    private int splitCount = 4; // Y轴上数据分为几部分
    private int viewKindType = DataViewChart.VIEW_STEP;
    private int viewDateType = DataViewChart.DATEVIEW_WEEK;
    private float goalVal = 0.0f; // 目标值

    private static final float CURVE_LINE_WIDTH = 4f;
    private static final float HALF_TIP_HEIGHT = 16;

    private static final String TAG = MySportView.class.getSimpleName();

    private Point[] adjustedPoints;
    private Paint borderPaint = new Paint();
    private Paint chartBgPaint = new Paint();
    // The rect of chart, x labels on the bottom are not included
    private Rect chartRect = new Rect();
    private Paint curvePaint = new Paint();
    private Paint fillPaint = new Paint();
    private Path fillPath = new Path();
    private Paint fillPaint1 = new Paint();
    private Path fillPath1 = new Path();
    private Paint textBg = new Paint();
    private Paint yTextColor = new Paint();
    private Paint ciclePaint = new Paint();

    private Paint gridPaint = new Paint();
    private Paint xLabelPaint = new Paint();
    private Paint yLabelPaint = new Paint();
    private String[] labels, yStrs;
    private float maxY; // 最大的高度

    private List<Point> originalList;
    private List<Point> originalList1;

    private float scaleY;
    private Rect textBounds = new Rect();
    private Paint tipLinePaint = new Paint();
    private Paint tipPaint = new Paint();
    private Rect tipRect = new Rect();
    private RectF tipRectF = new RectF();
    private Paint tipTextPaint = new Paint();

    {
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeCap(Paint.Cap.SQUARE);
        borderPaint.setStrokeWidth(4.0f);
        borderPaint.setAntiAlias(true);

        curvePaint.setStyle(Paint.Style.STROKE);
        curvePaint.setStrokeCap(Paint.Cap.ROUND);
        curvePaint.setStrokeWidth(CURVE_LINE_WIDTH);
        curvePaint.setColor(Color.rgb(0x00, 0x89, 0xd8));
        curvePaint.setAntiAlias(true);
        curvePaint.setAlpha(200);

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.rgb(0xF4, 0xA7, 0x20));
        fillPaint.setAlpha(170);
        fillPaint.setAntiAlias(true);

        ciclePaint.setStyle(Paint.Style.FILL);
        ciclePaint.setColor(Color.argb(0x77, 0xFF, 0xFF, 0xFF));
        ciclePaint.setAlpha(170);
        ciclePaint.setAntiAlias(true);

        fillPaint1.setStyle(Paint.Style.FILL);
        fillPaint1.setColor(Color.parseColor("#88D9D9D9"));
        fillPaint1.setAlpha(170);
        fillPaint1.setAntiAlias(true);

        yTextColor.setColor(Color.WHITE);
        yTextColor.setTextSize(20f);
        yTextColor.setAntiAlias(true);

        textBg.setStyle(Paint.Style.FILL);
        textBg.setColor(Color.rgb(0x92, 0x1e, 0x36));
        textBg.setAlpha(170);
        textBg.setAntiAlias(true);

        chartBgPaint.setStyle(Paint.Style.FILL);
        chartBgPaint.setColor(Color.argb(0x88, 0xDD, 0xDD, 0xDD));
        chartBgPaint.setAlpha(180);
        chartBgPaint.setAntiAlias(true);

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeCap(Paint.Cap.SQUARE);
        gridPaint.setColor(Color.argb(0x92, 0xD0, 0xD0, 0xD0));
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(3.0f);

        tipLinePaint.setStyle(Paint.Style.STROKE);
        tipLinePaint.setStrokeCap(Paint.Cap.SQUARE);
        tipLinePaint.setStrokeWidth(1.5f);
        tipLinePaint.setColor(Color.rgb(0x00, 0x89, 0xd8));
        tipLinePaint.setAntiAlias(true);
        tipLinePaint.setAlpha(220);

        tipPaint.setStyle(Paint.Style.FILL);
        tipPaint.setColor(Color.rgb(0x00, 0x89, 0xd8));
//        tipPaint.setColor(Color.parseColor("#99ffcc"));
        tipPaint.setAntiAlias(true);

        tipTextPaint.setColor(Color.WHITE);
        tipTextPaint.setTextSize(PublicData.TEXT_SIZE);
        tipTextPaint.setAntiAlias(true);

        xLabelPaint.setColor(Color.parseColor("#808080"));
        xLabelPaint.setTextSize(25f);
        xLabelPaint.setAntiAlias(true);

        yLabelPaint.setColor(Color.WHITE);
        yLabelPaint.setTextSize(PublicData.TEXT_SIZE_Y);
        yLabelPaint.setAntiAlias(true);
    }

    public MySportView(Context context) {
        super(context);
        Logger.i(TAGGE, "---------------MySportView1");
        this.context = context;
        this.setFocusable(true);
    }

    public MySportView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Logger.i(TAGGE, "---------------MySportView2");
        this.context = context;
        this.setFocusable(true);
    }

    private Point[] adjustPoints(List<Point> originalList, int chartWidth, int chartHeight, boolean flag) {
        Logger.i(TAGGE, "---------------adjustPoints");

        Logger.i(TAG, "---高:" + chartHeight + " maxY:" + maxY);
        scaleY = chartHeight / maxY;//缩放比例

        float axesSpan = originalList.get(originalList.size() - 1).x - originalList.get(0).x; // 最后一个点的x 减去 第一个点的x
        float startX = originalList.get(0).x; // 开始点的x
        float scaleX = chartWidth / axesSpan;//x轴的缩放
        Point[] adjustedPoints = new Point[originalList.size()];

        for (int i = 0; i < originalList.size(); i++) {
            Point p = originalList.get(i);
            Point newPoint = new Point();
            newPoint.x = (p.x - startX) * scaleX + chartRect.left;
            newPoint.y = p.y * scaleY;
            newPoint.y = chartHeight - newPoint.y;

            Logger.i(TAG, "---x:" + newPoint.x + " ---y:" + newPoint.y);

            adjustedPoints[i] = newPoint;
        }

        if (flag) {
            for (int i = 0, j = 0; i < originalList.size() - 1; i++) {
                Point curPoint = adjustedPoints[i];
                if (i % 2 == 1) {
                    infos[j].point = new Point(curPoint.x, curPoint.y);
                    infos[j].pos = adjustedPoints[i + 1].x;
                    j++;
                }
            }
        }

        return adjustedPoints;

        /*for (Info info : infos) {
            Logger.i(TAG, "整理好的信息是:msg:" + info.msg + " x:" + info.rect.left + " y:" + info.rect.top + " x1:" + info.rect.right + " y1:" + info.rect.bottom + " pos:" + info.pos);
        }*/
    }

    private void buildPath(Canvas canvas, Path path, Paint paint, Point[] adjustedPoints) {
        Logger.i(TAGGE, "---------------buildPath");

        path.reset();
//        Path path = new Path();

        int pointSize = adjustedPoints.length; // 所有点的数量
        path.moveTo(adjustedPoints[0].x, adjustedPoints[0].y);

        Point lastPoint = adjustedPoints[0];
        for (int i = 0; i < pointSize - 1; i++) {
            if (i % 2 == 1) {
                if (adjustedPoints[i + 1].y != zeroPointY) {
                    continue;
                }
            }
            Point startp = lastPoint;
            Point endp = adjustedPoints[i + 1];
            float wt = (startp.x + endp.x) / 2;
            Point p3 = new Point();
            Point p4 = new Point();
            p3.y = startp.y;
            p3.x = wt;
            p4.y = endp.y;
            p4.x = wt;

            path.cubicTo(p3.x, p3.y, p4.x, p4.y, endp.x, endp.y);
//            Logger.i(TAG, "绘制的点有:" + (i + 1));
            lastPoint = endp;
            // canvas.drawLine(chartRect.left, endp.y, chartRect.right, endp.y, yTextColor);

        }
        canvas.drawPath(path, paint);
    }

    public float getTextHeight(Paint textPaint) {
        Logger.i(TAGGE, "---------------getTextHeight");
        FontMetrics fm = textPaint.getFontMetrics();
        return (float) Math.ceil(fm.descent - fm.ascent) - 2;
    }

    public float getTextWidth(Paint textPaint, String text) {
        Logger.i(TAGGE, "---------------getTextWidth");
        return textPaint.measureText(text);
    }

    private List<Point> getDataPoints(float[] datas, int xDataLen) {
        Logger.i(TAGGE, "---------------getDataPoints");

        List<Point> points = new ArrayList<>();
        float[] realDatas = new float[xDataLen];
        float[] sideDatas = new float[xDataLen + 1];
        for (int i = 0; i < xDataLen; i++) {
            realDatas[i] = datas[i];
        }
        sideDatas[0] = 0.0f;
        sideDatas[xDataLen] = 0.0f;
        for (int i = 1; i < xDataLen; i++) {
            if (realDatas[i - 1] > 0 && realDatas[i] > 0) {
                sideDatas[i] = 1;
            } else {
                sideDatas[i] = 0;
            }
        }
        for (int i = 0; i < xDataLen * 2 + 1; i++) {
            if (i % 2 == 0) {
                points.add(new Point(i, sideDatas[i / 2]));
            } else {
                points.add(new Point(i, realDatas[i / 2]));
            }
        }
        return points;
    }

    private String[] getYPoints(float maxValue) {
        Logger.i(TAGGE, "---------------getYPoints");
        if (viewKindType == DataViewChart.VIEW_SLEEP) {
            splitCount = 2;
        } else {
            splitCount = 4;
        }
        String[] yStr = new String[splitCount];
        int max = (int) (maxValue * 1.2);
        max = viewKindType == DataViewChart.VIEW_HEART ? 200 : max; // 心率的最大值是200
        int ma = max % splitCount;
        if (max % splitCount != 0) {
            max += (splitCount - max % splitCount);
        }
        if (viewKindType == DataViewChart.VIEW_SLEEP) {
            if (max > 720) {
                if (max > 1080) {
                    if (max > 1200) {
                        max = max + 120;
                    } else {
                        max = 1440;

                    }
                } else {
                    max = 1080;
                }
            } else {
                max = 720;
            }

        }
        for (int i = 0; i < splitCount; i++) {
            yStr[i] = max / splitCount * (i + 1) + "";
        }
        if (viewKindType == DataViewChart.VIEW_SLEEP) {
            for (int i = 0; i < yStr.length; i++) {
                int min = (int) Float.parseFloat(yStr[i]);
                String str = min / 60 + context.getString(R.string.h);
                Logger.i("test-ui", "睡眠 转换前:" + yStr[i] + " 转换后:" + str);
                yStr[i] = str;
            }

        }
        return yStr;
    }

    private String[] getXPoints(int viewDateType) {
        Logger.i(TAGGE, "---------------getXPoints");

        String[] xStr = new String[]{"", context.getString(R.string.sunday), "", context.getString(R.string.monday), "", context.getString(R.string.tuesday), "", context.getString(R.string.wednesday), "",
                context.getString(R.string.thursday), "", context.getString(R.string.friday), "", context.getString(R.string.saturday), ""};
        if (viewDateType == DataViewChart.DATEVIEW_MONTH) {
            xStr = new String[63];
            for (int i = 0, j = 1; i < 63; i++) {
                if (i % 2 == 0) {
                    xStr[i] = "";
                } else {
                    xStr[i] = j + "";
                    j++;
                }
            }
        }
        return xStr;
    }

    public void setDatas(int viewKindType, int viewDateType, float[] datas, float[] totalDatas, float maxValue, int curPos, float goalVal) {
//        datas[6] = 3590; 布局适配调试
//        totalDatas[6] = 3590 / 5;
//        maxValue = 3590;
        if (maxValue > 7000 && maxValue < 7999 && viewKindType == 5) {
            maxValue = 8000;
        }
        if (maxValue > 3000 && maxValue < 4499) {
            maxValue = 4500;
        }
        Logger.e("", "被遮挡的数据的最大值 == " + maxValue);
        Logger.i(TAGGE, "---------------setDatas");
        Logger.i("test-ui", "setDatas : " + "viewKindType=" + viewKindType + "---viewDateType=" + viewDateType + "---datas=" + datas + "---totalDatas" + totalDatas);
        Logger.i("test-ui", "setDatas : " + "maxValue=" + maxValue + "---curPos=" + curPos + "---goalVal=" + goalVal);

        this.curPos = curPos;
        this.viewKindType = viewKindType;
        this.viewDateType = viewDateType;
        this.goalVal = goalVal;
        if (viewKindType == 5) {
            boolean isCent = false;
//            isCent = (boolean) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.IS_CENT_KILOMETERS_KEY, ConfigHelper.DATA_BOOLEAN);
//            String a = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.UNIT_KEY, ConfigHelper.DATA_STRING);
            String a = CacheUtils.getString(context, "unit_value");
            if (a.contains("1")) {
                isCent = false;
            } else {
                isCent = true;
            }
            Logger.i("test-ui", "isCent" + isCent);
            if (isCent) {
                maxValue = (float) maxValue / 1000;
                if (maxValue < goalVal) {
                    maxValue = goalVal;
                }
                Logger.i("test-ui", "maxValue" + maxValue);
                for (int i = 0; i < datas.length; i++) {
                    datas[i] = datas[i] / 1000;
                    Logger.i("test-ui", "datas" + datas[i]);

                }
                for (int i = 0; i < totalDatas.length; i++) {
                    totalDatas[i] = totalDatas[i] / 1000;
                    Logger.i("test-ui", "datas" + totalDatas[i]);

                }

//                float dis =(float) CommonUtil.Mile2KM(this.goalVal) ;
//                this.goalVal = ((int) (dis * 100)) / 100.0f;
            }
        }
        Logger.i("test-ui", "isCent" + this.goalVal);
        int xDataLen = viewDateType == DataViewChart.DATEVIEW_WEEK ? 7 : datas.length;
        if (viewDateType == DataViewChart.DATEVIEW_WEEK || viewDateType == DataViewChart.DATEVIEW_MONTH) { // DATEVIEW_WEEK:1 DATEVIEW_MONTH:2 DATEVIEW_DAY:0

            List<Point> points = new ArrayList<>();
            List<Point> points1 = new ArrayList<>();
            points = getDataPoints(datas, xDataLen);
            points1 = getDataPoints(totalDatas, xDataLen);

            String[] yStr = getYPoints(maxValue);
            String[] xStr = getXPoints(viewDateType);

            int max = (int) (maxValue * 1.2);
            if (max % splitCount != 0) {
                max += (splitCount - max % splitCount);
            }
            if (viewKindType == DataViewChart.VIEW_SLEEP) {
                if (max > 720) {
                    if (max > 1080) {
                        if (max > 1200) {
                            max = max + 120;
                        } else {
                            max = 1440;

                        }
                    } else {
                        max = 1080;
                    }
                } else {
                    max = 720;
                }

            }
            max = viewKindType == DataViewChart.VIEW_HEART ? 200 : max; // 心率的最大值是200

            Logger.i("test-ui", "x轴数据 : " + Arrays.toString(xStr));
            Logger.i("test-ui", "y轴数据 : " + Arrays.toString(yStr));
            init(points, points1, xStr, yStr, "", max);
        }
    }

    public void init(List<Point> originalList, List<Point> originalList1, String[] labels, String[] yStrs, String tipText, int maxY) {
        Logger.i(TAGGE, "---------------init" + originalList.size());

        infos = new Info[originalList.size() / 2];
        for (int i = 0, j = 0; i < originalList.size(); i++) {
            if (i % 2 == 1) {
                infos[j] = new Info();
                if (viewKindType == DataViewChart.VIEW_STEP
                        || viewKindType == DataViewChart.VIEW_CALORIES
                        || viewKindType == DataViewChart.VIEW_ACTIVITY
                        || viewKindType == DataViewChart.VIEW_HEART) {
                    infos[j].msg = (int) originalList.get(i).y + "";
                } else {
                    infos[j].msg = originalList.get(i).y + "";
                }
                j++;
            }
        }
        if (viewKindType == DataViewChart.VIEW_DISTANCE) { // 如果单位是英制，转换为英制数据
//            String unit = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.UNIT_KEY, ConfigHelper.DATA_STRING);
            String unit = CacheUtils.getString(context, "unit_value");
            if (unit.equals("1")) {
                for (int i = 0; i < originalList.size(); i++) {
                    if (i % 2 == 1) {
                        Point point = originalList.get(i);
                        float dis = (float) CommonUtil.KM2Mile(point.y / 1000L);
                        point.y = ((int) (dis * 100)) / 100.0f;
                    }
                }
                for (int i = 0; i < originalList1.size(); i++) {
                    if (i % 2 == 1) {
                        Point point = originalList1.get(i);
                        float dis = (float) CommonUtil.KM2Mile(point.y / 1000L);
                        point.y = ((int) (dis * 100)) / 100.0f;
                    }
                }
            }
        }

        this.originalList = originalList;
        this.originalList1 = originalList1;
        this.labels = labels;
        this.yStrs = yStrs;
        this.maxY = maxY;
        adjustedPoints = new Point[originalList.size()];

        // order by x coodinate ascending
        Collections.sort(originalList, Point.X_COMPARATOR);
        Collections.sort(originalList1, Point.X_COMPARATOR);
        super.invalidate();
    }

    private float zeroPointY = 0.0f;
    private int curPos = 0; // 目前显示的上标
    private Info[] infos; // 所有的信息

    private class Info {
        public String msg;
        public Point point;
        public float pos;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Logger.i(TAGGE, "---------------onDraw");

        getDrawingRect(chartRect); // 获取view的区域 获取View的绘制范围，即左、上、右、下边界相对于此View的左顶点的距离（偏移量），即0、0、View的宽、View的高

        Logger.d(TAG, chartRect.toString());

        if (originalList != null) {
            drawXYaxis(canvas); // 画x、y轴

            chartRect.left += getTextWidth(yLabelPaint, "1") * 8;
            chartRect.bottom -= getTextHeight(xLabelPaint) * 3;

            int chartHeight = chartRect.bottom - chartRect.top; // 高
            int chartWidth = chartRect.right - chartRect.left; // 宽
            zeroPointY = chartHeight;

            Point[] points = adjustPoints(originalList, chartWidth, chartHeight, true); // 调整各个点
            Point[] points1 = adjustPoints(originalList1, chartWidth, chartHeight, false); // 调整各个点

            buildPath(canvas, fillPath, fillPaint1, points1); // 5周/5月平均值

            buildPath(canvas, fillPath, fillPaint, points); // 周/月


            if (viewKindType != DataViewChart.VIEW_HEART) // 心率中不画目标虚线
                drawDottedLine(canvas, chartHeight);

            // 画上标
            if (infos != null && curPos < infos.length) {
                if (Float.parseFloat(infos[curPos].msg) > 0.0) {

                    String showData = infos[curPos].msg;
                    ((ISetViewVal) context).setCurVal(Float.parseFloat(showData));

                    Logger.i("test-ui", "绘制前:" + showData + "-----viewKindType=" + viewKindType);
                    if (viewKindType == DataViewChart.VIEW_DISTANCE) {
//                        String unit = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.UNIT_KEY, ConfigHelper.DATA_STRING);
                        String unit = CacheUtils.getString(context, "unit_value");
                        if (unit.equals("0")) {
                            ((ISetViewVal) context).setCurVal(Float.parseFloat(showData) * 1000);
                            Logger.i("test-ui", "绘制前:" + showData + "-----viewKindType=" + viewKindType);
                        }
                        float dis = unit.equals("1") ? (float) CommonUtil.KM2Mile(Float.parseFloat(showData) / 1000L) : Float.parseFloat(showData);
                        dis = ((int) (dis * 100)) / 100.0f;
                        showData = dis + "";
                    }
                    if (viewKindType == DataViewChart.VIEW_SLEEP) { // 睡眠转换为:x时x分
                        int min = (int) Float.parseFloat(showData);
                        Logger.i("test-ui", "绘制中:" + min);
                        float mins = Float.parseFloat(showData);

                        min = min + ((mins - min) % 10 * 10 > 5 ? 1 : 0);
                        Logger.i("test-ui", "绘制中:" + min);
                        showData = min / 60 + context.getString(R.string.h) + min % 60 + context.getString(R.string.m);
                    } else {                                        // 其他数值转换为例如 123,456
                        NumberFormat nf1 = NumberFormat.getInstance();
                        if (showData.contains(".")) {
                            showData = nf1.format(Double.parseDouble(showData));
                        } else {
                            showData = nf1.format(Integer.parseInt(showData));
                        }
                    }

//                    if(PublicData.selectDeviceName.equals(PublicData.L38I)&&viewKindType==7){
//                        int time=Integer.parseInt(showData)/60;
//                        showData= Integer.toString(time);
//                        Logger.i("test-ui", "更改后绘制前:" + time);
//
////                        showData=time.toString();
//                    }
                    Logger.i("test-ui", "更改后绘制前:" + showData);
                    float x, y;
                    float textWidth = getTextWidth(tipTextPaint, showData);
                    float textHeight = getTextHeight(tipTextPaint);
                    float cicleHeight = textHeight;
                    x = infos[curPos].point.x - textWidth / 2;
                    y = infos[curPos].point.y - cicleHeight; // 蓝色框等上移大圆的半径
                    float heightOffset = textWidth * 3 / 10;
                    y -= heightOffset; // 显示上移5分之一的高度
                    drawYDottedLine(canvas, (int) infos[curPos].point.x);
                    Logger.i("", "canvas.drawRect=" + (x - textWidth / 8));
                    Logger.i("", "canvas.drawRect=" + (y - textHeight));
                    Logger.i("", "canvas.drawRect=" + (x + textWidth * 1.25f));
                    Logger.i("", "canvas.drawRect=" + (y));
                    Logger.i("", "canvas.drawRect=" + (x));
                    Logger.i("", "canvas.drawRect=" + (textWidth));
                    Logger.i("", "canvas.drawRect=" + (textHeight));
                    Logger.i("", "canvas.drawRect=" + (canvas.getHeight()));
                    Logger.i("", "canvas.drawRect=" + (canvas.getWidth()));
                    if ((x + textWidth * 1.25f) > canvas.getWidth()) {
                        float gapX = ((x + textWidth * 1.25f) - canvas.getWidth());
                        x = x - gapX;
                    }

                    // 画蓝色框
                    Logger.d("", "坐标drawRect位置 left = " + (x - textWidth / 8));
                    Logger.d("", "坐标drawRect位置 top = " + (y - textHeight));
                    Logger.d("", "坐标drawRect位置 right = " + (x + textWidth * 1.25f));
                    Logger.d("", "坐标drawRect位置 bottom = " + y);
                    canvas.drawRect((x - textWidth / 8), y - textHeight, x + textWidth * 1.25f, y + 6, tipPaint);
//                    canvas.drawRoundRect(new RectF(x - textWidth / 4, y - textHeight, x + textWidth * 1.25f, y), textHeight, textHeight, tipPaint);
                    // 画蓝色框中的文字
                    Logger.d("", "坐标drawText位置 float x = " + (x + 5));
                    Logger.d("", "坐标drawText位置 float y = " + (y - textHeight / 12));
                    Logger.e("", "坐标 x = " + x + " 坐标 y = " + y);
                    Logger.e("", "坐标 当前这个自定义View的最大高度是 = " + maxY);
                    canvas.drawText(showData, x + 5, y - textHeight / 12, tipTextPaint);
//                    canvas.drawText(showData, 50, 10, tipTextPaint);
                    // 画蓝色框下的三角形
                    drawTriangle(canvas, new Point(infos[curPos].point.x, infos[curPos].point.y - heightOffset - cicleHeight), heightOffset);

                    canvas.drawCircle(infos[curPos].point.x, infos[curPos].point.y, cicleHeight, ciclePaint);
                    ciclePaint.setColor(Color.argb(0xFF, 0xFF, 0xFF, 0xFF));
                    canvas.drawCircle(infos[curPos].point.x, infos[curPos].point.y, cicleHeight / 2, ciclePaint);
                    ciclePaint.setColor(Color.argb(0x77, 0xFF, 0xFF, 0xFF));

                }
            }

        }
    }


    private void drawXYaxis(Canvas canvas) {
        Logger.i(TAGGE, "---------------drawXYaxis");

        xLabelPaint.setTextSize(viewDateType == DataViewChart.DATEVIEW_MONTH ? PublicData.TEXT_SIZE_M : PublicData.TEXT_SIZE);
        // 画y轴
        if (yStrs != null) {
            float oneTextWidth = getTextWidth(yLabelPaint, "1") * 3; //一个文字的宽度
            Rect viewRect = new Rect();
            getDrawingRect(viewRect);
            viewRect.bottom -= getTextHeight(xLabelPaint) * 3; // 预留3个汉字高度给x轴画背景
            viewRect.left += oneTextWidth; // 离屏幕一个汉字开始写文字

            int yLen = yStrs.length; // Y轴上的文字数量
            float viewLen = viewRect.bottom - viewRect.top + 3; // y轴的高度
            float unitHeight = viewLen / yLen; // 每一单元部分的高度
            for (int i = 0; i < yLen; i++) {
                Logger.i("test-test", "ystr : " + i);
                float x = viewRect.left;
                float y = viewRect.bottom - unitHeight * (i + 1) + getTextHeight(yLabelPaint) / 2;
                if (i == (yLen - 1)) {
                    y = viewRect.bottom - unitHeight * (i + 1) + getTextHeight(yLabelPaint); // 最后一个数字，需要向下一个字的高度才能显示
                }
                canvas.drawText(yStrs[i], x, y, yLabelPaint);
            }
        }
        Logger.i("", "curPos=" + curPos);

        int index = 0;
        if (viewDateType == DataViewChart.DATEVIEW_MONTH) {
            index = (curPos + 1) + (curPos);
            Logger.i("", "index-" + index);
        } else {
            index = curPos + (curPos + 1);
        }
        // 画x轴
        if (labels != null) {
            // 画x轴的背景
            canvas.drawRect(chartRect.left, chartRect.bottom - getTextHeight(xLabelPaint) * 3, chartRect.right, chartRect.bottom, textBg);
            // 画x轴上的文字
            Rect viewRect = new Rect();
            getDrawingRect(viewRect);
            viewRect.left += viewDateType == DataViewChart.DATEVIEW_MONTH ? getTextWidth(yLabelPaint, "1") * 8 : getTextWidth(yLabelPaint, "1") * 7; // 留出y轴文字显示区域的宽度
            float viewLen = viewRect.right - viewRect.left;
            float unitWidth = viewLen / (labels.length - 1);

            Logger.i("", "index=" + index);
            Logger.i("", "labels=" + labels.length);

            for (int i = 0; i < labels.length; i++) {
                if (i % 2 == 1) {
                    float x = viewRect.left + unitWidth * i - getTextWidth(xLabelPaint, "1") / 2;
                    float y = viewRect.bottom - getTextHeight(xLabelPaint);
                    Logger.i("", "labels=" + labels[i]);
                    Logger.i("", "labels=" + i);
                    if (index == i && Float.parseFloat(infos[curPos].msg) > 0.0) {
                        Logger.i("", "labels=" + labels[i]);
                        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
                        xLabelPaint.setTypeface(font);
                        xLabelPaint.setColor(Color.parseColor("#ffffff"));
                        canvas.drawText(labels[i], x, y, xLabelPaint);
                        font = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
                        xLabelPaint.setTypeface(font);
                        xLabelPaint.setColor(Color.parseColor("#808080"));

                    } else {
                        canvas.drawText(labels[i], x, y, xLabelPaint);
                    }
                }
            }
        }


    }

    // 画三角形
    private void drawTriangle(Canvas canvas, Point point, float heightOffset) {
        Logger.i(TAGGE, "---------------drawXYaxis");

        Point[] p = new Point[3];
        heightOffset -= heightOffset / 4;
        p[0] = new Point(point.x + heightOffset / 2, point.y);
        p[1] = new Point(point.x, point.y + heightOffset);
        p[2] = new Point(point.x - heightOffset / 2, point.y);
        Path path = new Path();
        path.moveTo(p[0].x, p[0].y);
        path.quadTo(p[0].x, p[0].y, p[1].x, p[1].y);
        path.quadTo(p[1].x, p[1].y, p[2].x, p[2].y);
        canvas.drawPath(path, tipPaint);
    }

    private void drawDottedLine(Canvas canvas, int chartHeight) {
        Logger.i(TAGGE, "---------------drawDottedLine");

        Logger.i("test-ui", "chartHeight=" + chartHeight + "-----goalVal=" + goalVal);
        float goalY = goalVal * (chartHeight / maxY);
        goalY = chartHeight - goalY;

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE); // 颜色重叠
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(1.0f);
        Path path = new Path();
        Rect chartRect = new Rect();
        getDrawingRect(chartRect);
        path.moveTo(chartRect.left, goalY);
        path.lineTo(chartRect.right, goalY);
        PathEffect effects = new DashPathEffect(new float[]{5, 5, 20, 5}, 1);
        paint.setPathEffect(effects);
        canvas.drawPath(path, paint);
    }

    private void drawYDottedLine(Canvas canvas, int chartHeight) {
//        Logger.i(TAGGE, "---------------drawDottedLine");
//
//        Logger.i("test-ui", "chartHeight=" + chartHeight + "-----goalVal=" + goalVal);
//        float goalY = goalVal * (chartHeight / maxY);
//        goalY = chartHeight - goalY;
//
//        paint.setStyle(Paint.Style.STROKE); // 颜色重叠
//        paint.setColor(Color.WHITE);
//        paint.setStrokeWidth(1.0f);
//        Path path = new Path();
//        getDrawingRect(chartRect);
//        path.moveTo(chartRect.top, chartHeight);
//        path.lineTo(chartRect.bottom, chartHeight);
//        PathEffect effects = new DashPathEffect(new float[]{5, 5, 20, 5}, 1);
//        paint.setPathEffect(effects);
//        canvas.drawPath(path, paint);
        Paint paint = new Paint();
        Rect chartRect = new Rect();
        getDrawingRect(chartRect);
        paint.setColor(Color.rgb(0xF4, 0xA7, 0x20));                    //设置画笔颜色chartRect.bottom - getTextHeight(xLabelPaint) * 3
        paint.setStrokeWidth((float) 1.0);              //设置线宽
        canvas.drawLine(chartHeight, chartRect.top, chartHeight, chartRect.bottom - getTextHeight(xLabelPaint) * 3, paint);        //绘制直线

    }

    private int lastPos = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Logger.i(TAGGE, "---------------onTouchEvent");

        curPos = 0;
        if (infos != null) {
            for (Info info : infos) {
                if (info.pos > event.getX()) {
                    break;
                }
                curPos++;
            }
            if (curPos > infos.length - 1) {
                curPos = infos.length - 1;
            }
            if (lastPos != curPos) { // 如果上一次的位置和这次的位置不一样,则刷新
                lastPos = curPos;
                invalidate();
            }
        }
        getParent().requestDisallowInterceptTouchEvent(true); // 禁止viewpage换页

        return true;
    }

}
