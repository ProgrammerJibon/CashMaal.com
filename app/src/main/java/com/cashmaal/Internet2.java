package com.cashmaal;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;

import com.google.android.datatransport.BuildConfig;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Internet2 extends AsyncTask<Void, Void, JSONObject> {
    private final TaskListener taskListener;
    public Activity context;
    public String url;
    private String $_POST;
    private Integer code = 0;

    public Internet2(Activity context, String url, String $_POST, TaskListener listener) {
        this.context = context;
        this.url = url;
        this.taskListener = listener;
        this.$_POST = $_POST;
        if (this.$_POST == null || this.$_POST.equals("")) {
            this.$_POST = "bool=true";
        }
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        try {
            super.onPostExecute(result);
            if (this.taskListener != null) {
                if (result.has("error")) {
                    String versionName = BuildConfig.VERSION_NAME;
                    new CustomTools(context).toast(result.getString("error"), R.drawable.ic_baseline_clear_24);
                }
                this.taskListener.onFinished(code, result);
            }
        } catch (Exception e) {
            Log.e("errnos", e.toString());
        }
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        try {
            String urlParameters = $_POST;
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;
            String lines = "";
            String allLines = "";
            URL newLink = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) newLink.openConnection();
            // starting point for post method
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("charset", "utf-8");
            httpURLConnection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            httpURLConnection.setUseCaches(false);
            try (DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream())) {
                wr.write(postData);
            }
            // Fetch and set cookies in requests
            CookieManager cookieManager = CookieManager.getInstance();
            String cookie = cookieManager.getCookie(httpURLConnection.getURL().toString());
            if (cookie != null) {
                httpURLConnection.setRequestProperty("Cookie", cookie);
            }
            httpURLConnection.connect();
            this.code = httpURLConnection.getResponseCode();
            // Get cookies from responses and save into the cookie manager
            List cookieList = httpURLConnection.getHeaderFields().get("Set-Cookie");
            if (cookieList != null) {
                for (Object cookieTemp : cookieList) {
                    cookieManager.setCookie(httpURLConnection.getURL().toString(), String.valueOf(cookieTemp));
                }
            }
            httpURLConnection.getErrorStream();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuffer = new StringBuilder();
            while ((lines = bufferedReader.readLine()) != null) {
                stringBuffer.append(lines);
            }
            allLines = stringBuffer.toString();
            JSONObject jsonObject = new JSONObject(allLines);
            return jsonObject;
        } catch (Exception e) {
            new CustomTools(context).toast("Unnable to connect...", R.drawable.ic_baseline_clear_24);
            Log.e("errnos", this.url + " - Internet2 error:" + e);
            return null;
        }
    }

    public interface TaskListener {
        void onFinished(Integer code, JSONObject result);
    }
}
