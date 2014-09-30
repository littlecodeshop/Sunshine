package com.iglcs.sunshine;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by rribier on 30/09/2014.
 */
public class WeatherDataParser {

    public static String getCityName(String jsonString) throws JSONException {

        JSONObject jsonObject = new JSONObject(jsonString);
        String retString = jsonObject.getJSONObject("city").getString("name");
        Log.d("WEATHERDATAPARSER", retString);
        return retString;
    }

    public static ArrayList<String> getForcastArray(String jsonString) throws JSONException {

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray list = jsonObject.getJSONArray("list");

        ArrayList<String> arrayList = new ArrayList<String>();

        for (int i = 0; i < list.length(); i++) {
            JSONObject forecast = (JSONObject) list.get(i);
            long date = forecast.getLong("dt");
            JSONObject weather = forecast.getJSONArray("weather").getJSONObject(0);
            String s = weather.getString("description");
            arrayList.add(fomatDate(date)+" "+s);
        }

        return arrayList;

    }

    private static String fomatDate(long date) {
        Date adate = new Date(date * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(adate).toString();
    }



}
