package cn.appscomm.pedometer.model;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2015/12/20.
 */
public class StrangerData {
    private String TAG = "StrangerData";
    public Bitmap bitmap;      // 头像
    public int ddId;
    public int memberId;
    public String accountId;
    public String userName;
    public String iconUrl;
    public boolean isFriendFlag;

    public StrangerData(int ddId, int memberId, String accountId, String userName, String iconUrl, Bitmap bitmap){
        this.ddId = ddId;
        this.memberId = memberId;
        this.accountId = accountId;
        this.userName = userName;
        this.iconUrl = iconUrl;
        this.bitmap = bitmap;
    }

    public StrangerData(int ddId, int memberId, String accountId, String userName, String iconUrl, Bitmap bitmap, boolean isFriendFlag) {
        this.ddId = ddId;
        this.memberId = memberId;
        this.accountId = accountId;
        this.userName = userName;
        this.iconUrl = iconUrl;
        this.bitmap = bitmap;
        this.isFriendFlag = isFriendFlag;
    }
    @Override
    public String toString() {
        return  "StrangerData( ddId: " + ddId + ", memberId: " + memberId + ", accountId: " + accountId + ", userName: " + userName + ", iconUrl: "
                + iconUrl + ", isFriendFlag: " + isFriendFlag + ", bitmap: " + bitmap + " )";
    }
}
