package com.juhe.weather;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.util.Log;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.juhe.weather.adapter.CityListAdatper;
import com.juhe.weather.utils.HttpUtil;
import com.juhe.weather.utils.TextSort;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.thinkland.juheapi.common.JsonCallBack;
import com.thinkland.juheapi.data.weather.WeatherData;

public class CityActivity extends Activity {

	private ListView lv_city;
	private static List<String> cityNameList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city);
		initViews();
	    //getCities();
        // getCityList();
        initCityList();

	}
	private void initViews() {
		findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {

            @Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		lv_city = (ListView) findViewById(R.id.lv_city);
	}

    //被封装的数据请求
	private void getCities() {
		WeatherData data = WeatherData.getInstance();
		data.getCities(new JsonCallBack() {
			@Override
			public void jsonLoaded(JSONObject json) {
				// TODO Auto-generated method stub
				try {
					int code = json.getInt("resultcode");
					int error_code = json.getInt("error_code");
					if (error_code == 0 && code == 200) {

						cityNameList = new ArrayList<String>();
						JSONArray resultArray = json.getJSONArray("result");
						Set<String> citySet = new HashSet<String>();
						for (int i = 0; i < resultArray.length(); i++) {
							String city = resultArray.getJSONObject(i).getString("city");
							citySet.add(city);
						}
						cityNameList.addAll(citySet);
						CityListAdatper adatper = new CityListAdatper(CityActivity.this, cityNameList);
						lv_city.setAdapter(adatper);
						lv_city.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
								// TODO Auto-generated method stub
								Intent intent = new Intent();
								intent.putExtra("city", cityNameList.get(arg2));
								setResult(1, intent);
								finish();
							}
						});

					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

    private void  initCityList(){

        if(cityNameList!=null){                                                  //防止多次请求
           Log.i(Constant.LOG_TAG," cityNameList not null");
            cityNameList= TextSort.TextListSort(cityNameList);
            CityListAdatper adatper = new CityListAdatper(CityActivity.this, cityNameList);
            lv_city.setAdapter(adatper);
            lv_city.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent();
                    intent.putExtra("city", cityNameList.get(arg2));
                    setResult(1, intent);
                    finish();
                }
            });
        }else{
            Log.i(Constant.LOG_TAG," cityNameList  null");
            getCityList();
        }
    }
    //  用android-async-http.jar 做http请求
    private void getCityList(){

        RequestParams params=new RequestParams();
        params.put("key",Constant.APP_WEATHER_KEY);
        HttpUtil.get(Constant.WEATHER_CITYS, params, new TextHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String s) {
                        try {
                            JSONObject json = new JSONObject(s);
                            Log.i(Constant.LOG_TAG, json.toString());
                            int code = json.getInt("resultcode");
                            int error_code = json.getInt("error_code");
                            if (error_code == 0 && code == 200) {
                                cityNameList = new ArrayList<String>();
                                JSONArray resultArray = json.getJSONArray("result");
                                Set<String> citySet = new HashSet<String>();
                                for (int i = 0; i < resultArray.length(); i++) {
                                    String city = resultArray.getJSONObject(i).getString("city");
                                    citySet.add(city);
                                }
                                cityNameList.addAll(citySet);
                                cityNameList= TextSort.TextListSort(cityNameList);
                                CityListAdatper adatper = new CityListAdatper(CityActivity.this, cityNameList);
                                lv_city.setAdapter(adatper);
                                lv_city.setOnItemClickListener(new OnItemClickListener() {

                                    @Override
                                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                        // TODO Auto-generated method stub
                                        Intent intent = new Intent();
                                        intent.putExtra("city", cityNameList.get(arg2));
                                        setResult(1, intent);
                                        finish();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String s, Throwable throwable) {

                        Toast.makeText(CityActivity.this, "网络或者服务器错误", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

}
