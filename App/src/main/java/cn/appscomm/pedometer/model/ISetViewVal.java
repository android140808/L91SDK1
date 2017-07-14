package cn.appscomm.pedometer.model;

/**
 * Created by glin on 7/10/15.
 */
public interface ISetViewVal {

    public void setCurVal(float curVal);

    public void setSleepRange(String begin, String end);        // summer; add

    public void setTimeDisplay(String s );
    
    void setCurIndex(int curIndex);
}
