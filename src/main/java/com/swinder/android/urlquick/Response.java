package com.swinder.android.urlquick;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Response {
    private static final String TAG = UrlQuick.TAG;
    public static class Codes {
        public final static int HTTP_OK = 200;
    }
    public int status_code;
    public String message;
    public JSONObject json;
    public String doc;
    Map<String, String> header;

    Response() {
        status_code = -1;
        message = null;
        json = null;
        header = null;
    }
    Response(int c, String m) {
        status_code = c;
        message = m;
        json = null;
        header = new HashMap<>();
    }

    public String getString(String k) {
        String ret = "";
        try {
            ret = json.getString(k);
        } catch (Exception ex) {
            Log.e(TAG, "Response exception in get String for: " + k);
        }
        return ret;
    }

    public int getInt(String k) {
        int ret = -1;
        try {
            ret = json.getInt(k);
        } catch (Exception ex) {
            Log.e(TAG, "Response exception in get Int for: " + k);
        }
        return ret;
    }

    public Response getObject(String k) {
        Response ret = new Response();
        try {
            ret.json = json.getJSONObject(k);
        } catch (Exception ex) {
            Log.e(TAG, "exception in get object for: " + k);
        }
        return ret;
    }

    public JSONArray getArray(String k) {
        JSONArray ret = new JSONArray();
        try {
            ret = json.getJSONArray(k);
        } catch (Exception ex) {
            Log.e(TAG, "exception in get object for: " + k);
        }
        return ret;
    }
}
