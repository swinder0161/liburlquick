package com.swinder.android.urlquick;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class UrlQuick {
    public static String TAG = "UrlQuick";
    private Response ret = null;
    public static Response post(String uri, String json, Map<String, String> header) {
        return run(uri, json, header);
    }

    public static Response get(String uri, Map<String, String> header) {
        return run(uri, null, header);
    }

    private static Response run(final String uri, final String payload, final Map<String, String> header) {
        Log.i(TAG, "> urlquick run() " + (null == payload ? "GET" : "POST") +
                " uri: " + uri + ", payload: " + payload + ", header: " + header);
        UrlQuick obj = new UrlQuick();
        try {
            Thread th = new Thread(() -> obj.execute(uri, payload, header));
            th.start();
            th.join();
        } catch(Exception ex) {
            Log.e(TAG, ". urlquick run() exception: " + ex.getMessage());
            ex.printStackTrace();
        }
        Log.i(TAG, "< urlquick run()");
        return obj.ret;
    }

    private void execute(String uri, String payload, Map<String, String> header) {
        //Log.i(TAG, "> urlquick execute()");
        HttpURLConnection conn = null;
        try {
            URL url = new URL(uri);
            conn = (HttpURLConnection) url.openConnection();
            //conn.setRequestMethod("POST");
            conn.setDoInput(true);
            if(null != header) {
                for (String k:header.keySet())
                    conn.setRequestProperty(k, header.get(k));
            }
            if(null != payload) {
                conn.setRequestMethod("POST");
                if (payload.startsWith("{")) {
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                } else {
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                }
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(payload);
                os.flush();
                os.close();
            } else {
                conn.setRequestMethod("GET");
            }

            ret = new Response(conn.getResponseCode(), conn.getResponseMessage());

            //Log.i(TAG, ". urlquick execute() STATUS: " + ret.status_code);
            //Log.i(TAG, ". urlquick execute() MSG: " + ret.message);
            try {
                InputStream is = conn.getInputStream();
                //Log.i(TAG, ". urlquick execute() IS: " + is);
                if (is != null) {
                    StringBuilder sb = new StringBuilder();
                    BufferedReader in = new BufferedReader(new InputStreamReader(is));
                    String str;
                    while ((str = in.readLine()) != null) {
                        str = str + "\n";
                        //Log.i(TAG, ". urlquick execute() is str(" + str.length() + "): " + str);
                        sb.append(str);
                    }
                    in.close();
                    str = sb.toString();
                    //Log.i(TAG, ". urlquick execute() is sb length: " + str.length() + ", char[0]: " + str.substring(0,2));

                    if (str.length() > 2 && str.substring(0,5).contains("{")) {
                        ret.json = new JSONObject(str);
                    } else {
                        ret.doc = str;
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, ". urlquick execute() exception in reading InputStream: " + ex.getMessage());
                Log.e(TAG, ". urlquick execute() STATUS: " + ret.status_code);
                Log.e(TAG, ". urlquick execute() MSG: " + ret.message);
                ex.printStackTrace();
                try {
                    InputStream es = conn.getErrorStream();
                    Log.i(TAG, ". urlquick execute() ES: " + es);
                    if (es != null) {
                        StringBuilder sb = new StringBuilder();
                        BufferedReader in = new BufferedReader(new InputStreamReader(es));
                        String str;
                        while ((str = in.readLine()) != null) {
                            Log.i(TAG, ". urlquick execute() es str: " + str);
                            sb.append(str).append("\n");
                        }
                        in.close();
                        Log.i(TAG, ". urlquick execute() es sb length: " + sb.length());

                        if (sb.length() > 15 &&
                                sb.substring(0,15).equalsIgnoreCase("<!doctype html>"))
                        {
                            ret.doc = sb.toString();
                        } else if (sb.length() > 2 && sb.charAt(0) == '{') {
                            ret.json = new JSONObject(sb.toString());
                        }
                    }
                } catch (Exception ex1) {
                    Log.e(TAG, ". urlquick execute() exception in reading ErrorStream: " + ex1.getMessage());
                    ex1.printStackTrace();
                }
            }

            for (int i = 0;; i++) {
                String name = conn.getHeaderFieldKey(i);
                String value = conn.getHeaderField(i);
                if (name == null && value == null) {
                    //Log.i(TAG, ". urlquick execute() break at: " + i);
                    break;
                }
                if (name == null) {
                    Log.e(TAG, ". urlquick execute() RESPONSECODE: " + value);
                } else {
                    ret.header.put(name, value);
                }
            }
            //Log.i(TAG, ". urlquick execute() ret header: " + ret.header);
        }  catch (Exception ex) {
            Log.e(TAG, ". urlquick execute() exception: " + ex.getMessage());
            ex.printStackTrace();
            ret = new Response(-1, null);
        } finally {
            if(conn != null) // Make sure the connection is not null.
                conn.disconnect();
        }
        //Log.i(TAG, "< urlquick execute() ret: " + ret);
    }
}

