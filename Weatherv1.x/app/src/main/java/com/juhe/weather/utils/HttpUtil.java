package com.juhe.weather.utils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by Rudy Steiner on 2015/6/8.
 */

public class HttpUtil {
    private static String BASE_URL = "http://www.163.com";
    private static AsyncHttpClient client;    //实例话对象

    public static void init(String baseUrl){

        client =new AsyncHttpClient();

        client.setTimeout(20000);   //设置链接超时，如果不设置，默认为10s

        BASE_URL=baseUrl;
    }

    public static void get(String path,AsyncHttpResponseHandler res)    //用一个完整url获取一个string对象
    {
        client.get(getAbsoluteUrl(path), res);
    }
    public static void get(String path,RequestParams params,AsyncHttpResponseHandler res)   //url里面带参数
    {
        client.get(getAbsoluteUrl(path), params,res);
    }
    public static void get(String path,JsonHttpResponseHandler res)   //不带参数，获取json对象或者数组
    {
        client.get(getAbsoluteUrl(path), res);
    }
    public static void get(String path,RequestParams params,JsonHttpResponseHandler res)   //带参数，获取json对象或者数组
    {
        client.get(getAbsoluteUrl(path), params,res);
    }
    public static void get(String path, BinaryHttpResponseHandler bHandler)   //下载数据使用，会返回byte数据
    {
        client.get(getAbsoluteUrl(path), bHandler);
    }
    public static AsyncHttpClient getClient()
    {
        return client;
    }

    public static String getAbsoluteUrl(String relativeUrl)
    {
        return BASE_URL + relativeUrl;
    }
}
