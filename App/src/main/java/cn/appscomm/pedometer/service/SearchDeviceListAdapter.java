package cn.appscomm.pedometer.service;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import apps.utils.Logger;
import cn.appscomm.pedometer.model.SearchDevice;
import cn.l11.appscomm.pedometer.activity.R;

/**
 * Created by Summer on 2016/1/27.
 */
public class SearchDeviceListAdapter extends BaseAdapter {
    private static final String TAG = "SearchDeviceListAdapter";
    private List<SearchDevice> devicesList;
    private LayoutInflater inflater;
    private Holder holder = null;

    public SearchDeviceListAdapter(Context context, List<SearchDevice> devicesList) {
        this.devicesList = devicesList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (devicesList != null)
            return devicesList.size();
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (devicesList != null && devicesList.size() > 0)
            return devicesList.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SearchDevice device = devicesList.get(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.device_search_list, null);
            holder = new Holder();
            holder.tv_device = (TextView) convertView.findViewById(R.id.tv_device);
            holder.iv_select = (ImageView) convertView.findViewById(R.id.iv_select);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        if (device != null) {
            if (device.deviceName != null) {
                String device_Name = device.deviceName;
                String array[] = device_Name.split("#");
                device_Name = "Lefit Pro+#" + array[1];
//                holder.tv_device.setText(device.deviceName);
                holder.tv_device.setText(device_Name);

                /**将搜索到蓝牙名字变更**/
                if (device.deviceName.toString().substring(0, 5).equals("BASIC")) {
                    String deviceNameIs = "LITE" + device.deviceName.toString().substring(5, device.deviceName.toString().length());
                    Logger.w("Avater", "name == " + deviceNameIs);
                    holder.tv_device.setText(deviceNameIs);
                }
                /****/

            }

            if (device.isSelected) {
                holder.iv_select.setVisibility(View.VISIBLE);
            } else {
                holder.iv_select.setVisibility(View.INVISIBLE);
            }
        }
        return convertView;
    }

    public void refresh() {

    }

    static class Holder {
        TextView tv_device; // 设备名
        ImageView iv_select;    // 选择后显示的勾
    }
}
