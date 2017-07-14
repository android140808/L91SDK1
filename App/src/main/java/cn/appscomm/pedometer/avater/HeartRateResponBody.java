package cn.appscomm.pedometer.avater;

import java.util.List;

/**
 * Created by Administrator on 2017/3/21.
 */

public class HeartRateResponBody {

    /**
     * personId : 5
     * deviceId : FCL11A1506059991010x
     * deviceType : L38A
     * details : [{"heartMin":80,"heartMax":100,"heartAvg":90,"timeZone":"","startTime":"2015-07-13 12:00:00","endTime":"2015-07-13 13:30:00"}]
     */

    private String personId;
    private String deviceId;
    private String deviceType;
    private List<DetailsBean> details;

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public List<DetailsBean> getDetails() {
        return details;
    }

    public void setDetails(List<DetailsBean> details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "{" +"''"+
                "personId:'" + personId + '\'' +
                ", deviceId:'" + deviceId + '\'' +
                ", deviceType:'" + deviceType + '\'' +
                ", details:[" + details +
                '}';
    }

    public static class DetailsBean {
        /**
         * heartMin : 80
         * heartMax : 100
         * heartAvg : 90
         * timeZone :
         * startTime : 2015-07-13 12:00:00
         * endTime : 2015-07-13 13:30:00
         */

        private String heartMin;
        private String heartMax;
        private String heartAvg;
        private String timeZone;
        private String startTime;
        private String endTime;

        public String getHeartMin() {
            return heartMin;
        }

        public void setHeartMin(String heartMin) {
            this.heartMin = heartMin;
        }

        public String getHeartMax() {
            return heartMax;
        }

        public void setHeartMax(String heartMax) {
            this.heartMax = heartMax;
        }

        public String getHeartAvg() {
            return heartAvg;
        }

        public void setHeartAvg(String heartAvg) {
            this.heartAvg = heartAvg;
        }

        public String getTimeZone() {
            return timeZone;
        }

        public void setTimeZone(String timeZone) {
            this.timeZone = timeZone;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        @Override
        public String toString() {
            return "{" +
                    "heartMin:'" + heartMin + '\'' +
                    ", heartMax:'" + heartMax + '\'' +
                    ", heartAvg:'" + heartAvg + '\'' +
                    ", timeZone:'" + timeZone + '\'' +
                    ", startTime:'" + startTime + '\'' +
                    ", endTime:'" + endTime + '\'' +
                    '}' + "]";
        }
    }
}
