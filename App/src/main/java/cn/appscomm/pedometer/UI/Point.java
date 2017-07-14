package cn.appscomm.pedometer.UI;

/**
 * Created by Administrator on 2015/11/26.
 */
public class Point {
    public int px, py;

    public Point() {
    }

    public Point(int px, int py) {
        this.px = px;
        this.py = py;
    }

    @Override
    public String toString() {
        return "px:" + px + ", py:" + py;
    }
}
