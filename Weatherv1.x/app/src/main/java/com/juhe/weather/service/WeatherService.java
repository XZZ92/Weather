package com.juhe.weather.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.juhe.weather.bean.FutureWeatherBean;
import com.juhe.weather.bean.HoursWeatherBean;
import com.juhe.weather.bean.PMBean;
import com.juhe.weather.bean.WeatherBean;
import com.thinkland.juheapi.common.JsonCallBack;
import com.thinkland.juheapi.data.air.AirData;
import com.thinkland.juheapi.data.weather.WeatherData;

public class WeatherService extends Service {

	private String city;
	private final String tag = "WeatherService";
	private WeatherServiceBinder binder = new WeatherServiceBinder();
	private boolean isRunning = false;
	private List<HoursWeatherBean> list;
	private PMBean pmBean;
	private WeatherBean weatherBean;
	private OnParserCallBack callBack;

	private final int REPEAT_MSG = 0x01;
	private final int CALLBACK_OK = 0x02;
	private final int CALLBACK_ERROR = 0x04;

	public interface OnParserCallBack {
		public void OnParserComplete(List<HoursWeatherBean> list, PMBean pmBean, WeatherBean weatherBean);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return binder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		city = "北京";
		mHandler.sendEmptyMessage(REPEAT_MSG);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.v(tag, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub

			switch (msg.what) {
			case REPEAT_MSG:

				getCityWeather();
				sendEmptyMessageDelayed(REPEAT_MSG, 30 * 60 * 1000);
				break;
			case CALLBACK_OK:
				if (callBack != null) {
					callBack.OnParserComplete(list, pmBean, weatherBean);
				}
				isRunning = false;
				break;
			case CALLBACK_ERROR:
				Toast.makeText(getApplicationContext(), "loading error", Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
		}

	};

	// 解析pm
	private PMBean parserPM(JSONObject json) {
		PMBean bean = null;
		try {
			int code = json.getInt("resultcode");
			int error_code = json.getInt("error_code");
			if (error_code == 0 && code == 200) {
				bean = new PMBean();
				JSONObject pmJSON = json.getJSONArray("result").getJSONObject(0).getJSONObject("citynow");
				bean.setAqi(pmJSON.getString("AQI"));
				bean.setQuality(pmJSON.getString("quality"));

			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bean;

	}

	public void setCallBack(OnParserCallBack callback) {
		this.callBack = callback;
	}

	public void removeCallBack() {
		callBack = null;
	}

	public void getCityWeather(String city) {
		this.city = city;
		getCityWeather();
	}

	public void getCityWeather() {
		if (isRunning) {
			return;
		}
		isRunning = true;
		final CountDownLatch countDownLatch = new CountDownLatch(3);
		WeatherData data = WeatherData.getInstance();

		data.getByCitys(city, 2, new JsonCallBack() {

			@Override
			public void jsonLoaded(JSONObject arg0) {
				// TODO Auto-generated method stub

				weatherBean = parserWeather(arg0);

				countDownLatch.countDown();
				// if (weatherBean != null) {
				// setWeatherViews(bean);
				// }

			}
		});

		data.getForecast3h(city, new JsonCallBack() {

			@Override
			public void jsonLoaded(JSONObject arg0) {
				// TODO Auto-generated method stub
				list = parserForecast3h(arg0);
				countDownLatch.countDown();
				// if (list != null && list.size() >= 5) {
				// setHourViews(list);
				// }
			}
		});

		AirData airData = AirData.getInstance();
		airData.cityAir(city, new JsonCallBack() {
			@Override
			public void jsonLoaded(JSONObject arg0) {
				// TODO Auto-generated method stub
				countDownLatch.countDown();
				pmBean = parserPM(arg0);
				// if (pmBean != null) {
				// setPMView(bean);
				// }
			}
		});

		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					countDownLatch.await();
					mHandler.sendEmptyMessage(CALLBACK_OK);
				} catch (InterruptedException ex) {
					mHandler.sendEmptyMessage(CALLBACK_ERROR);
					return;
				}
			}

		}.start();
	}

	// 解析城市查询接口
	private WeatherBean parserWeather(JSONObject json) {

		WeatherBean bean = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

		try {
			int code = json.getInt("resultcode");
			int error_code = json.getInt("error_code");
			if (error_code == 0 && code == 200) {
				JSONObject resultJson = json.getJSONObject("result");
				bean = new WeatherBean();

				// toady
				JSONObject todayJson = resultJson.getJSONObject("today");
				bean.setCity(todayJson.getString("city"));
				bean.setUv_index(todayJson.getString("uv_index"));
				bean.setTemp(todayJson.getString("temperature"));
				bean.setWeather_str(todayJson.getString("weather"));
				bean.setWeather_id(todayJson.getJSONObject("weather_id").getString("fa"));
				bean.setDressing_index(todayJson.getString("dressing_index"));

				// sk
				JSONObject skJson = resultJson.getJSONObject("sk");
				bean.setWind(skJson.getString("wind_direction") + skJson.getString("wind_strength"));
				bean.setNow_temp(skJson.getString("temp"));
				bean.setRelease(skJson.getString("time"));
				bean.setHumidity(skJson.getString("humidity"));

				// future

				Date date = new Date(System.currentTimeMillis());
				JSONArray futureArray = resultJson.getJSONArray("future");
				List<FutureWeatherBean> futureList = new ArrayList<FutureWeatherBean>();
				for (int i = 0; i < futureArray.length(); i++) {
					JSONObject futureJson = futureArray.getJSONObject(i);
					FutureWeatherBean futureBean = new FutureWeatherBean();
					Date datef = sdf.parse(futureJson.getString("date"));
					if (!datef.after(date)) {
						continue;
					}
					futureBean.setTemp(futureJson.getString("temperature"));
					futureBean.setWeek(futureJson.getString("week"));
					futureBean.setWeather_id(futureJson.getJSONObject("weather_id").getString("fa"));
					futureList.add(futureBean);
					if (futureList.size() == 3) {
						break;
					}
				}
				bean.setFutureList(futureList);

			} else {
				Toast.makeText(getApplicationContext(), "WEATHER_ERROR", Toast.LENGTH_SHORT).show();
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bean;

	}

	// 解析3小时预报
	private List<HoursWeatherBean> parserForecast3h(JSONObject json) {
		List<HoursWeatherBean> list = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
		Date date = new Date(System.currentTimeMillis());
		try {
			int code = json.getInt("resultcode");
			int error_code = json.getInt("error_code");
			if (error_code == 0 && code == 200) {
				list = new ArrayList<HoursWeatherBean>();
				JSONArray resultArray = json.getJSONArray("result");
				for (int i = 0; i < resultArray.length(); i++) {
					JSONObject hourJson = resultArray.getJSONObject(i);
					Date hDate = sdf.parse(hourJson.getString("sfdate"));
					if (!hDate.after(date)) {
						continue;
					}
					HoursWeatherBean bean = new HoursWeatherBean();
					bean.setWeather_id(hourJson.getString("weatherid"));
					bean.setTemp(hourJson.getString("temp1"));
					Calendar c = Calendar.getInstance();
					c.setTime(hDate);
					bean.setTime(c.get(Calendar.HOUR_OF_DAY) + "");
					list.add(bean);
					if (list.size() == 5) {
						break;
					}
				}

			} else {
				Toast.makeText(getApplicationContext(), "HOURS_ERROR", Toast.LENGTH_SHORT).show();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v(tag, "onDestroy");
	}

	public class WeatherServiceBinder extends Binder {

		public WeatherService getService() {
			return WeatherService.this;
		}

	}

}
