package com.juhe.weather.adapter;

import java.util.List;
import com.juhe.weather.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CityListAdatper extends BaseAdapter{

	private List<String> list;
	private LayoutInflater mInflater;

	public CityListAdatper(Context context,List<String> list) {
		// TODO Auto-generated constructor stub
		this.list = list;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
    // TODO Auto-generated method stub
	public String getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		View rowView = null;
		if(convertView == null){
			rowView = mInflater.inflate(R.layout.item_city_list, null);
		}else{
			rowView = convertView;
		}
		TextView tv_city = (TextView) rowView.findViewById(R.id.tv_city);
		tv_city.setText(getItem(position));
		return rowView;
	}

}
