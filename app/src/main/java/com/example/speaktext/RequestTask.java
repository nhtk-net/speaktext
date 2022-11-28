package com.example.speaktext;

import android.os.AsyncTask;
import android.util.Log;

import com.example.speaktext.util.Util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

class RequestTask extends AsyncTask<String, String, String> {
    private PostTaskListener<String> postTaskListener;
    protected RequestTask(PostTaskListener<String> postTaskListener){
        this.postTaskListener = postTaskListener;
    }
    @Override
    protected String doInBackground(String... uri) {
        String responseString = null, vsData;
        try {
            vsData = uri[0];
            if(!vsData.isEmpty()){
                if(vsData.toLowerCase().startsWith("http://") || vsData.toLowerCase().startsWith("https://")){
                    responseString = getDataInternet(vsData);
                }
            }

        } catch (Exception e) {
            String err;
            err = e.getMessage();
            Log.d("[doInBackground]", err == null ? e.toString() : err);
        }
        return responseString;
    }
    @Override
    protected void onPreExecute() {

    }
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //Do anything with response..
        if (postTaskListener != null)
            postTaskListener.onPostTask(result);
    }
    private String getDataInternet(String psUrl) {
        String responseString = null, inputLine;
        BufferedReader in = null;
        StringBuffer response;
        int statusCode;
        try {
            URL url = new URL(psUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //HttpURLConnection.setFollowRedirects(false);
            conn.setConnectTimeout(15 * 1000);//15 seconds timeout
            //conn.setRequestMethod("GET");
            //conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
            conn.connect();
            InputStream input = conn.getInputStream();

            statusCode = conn.getResponseCode();
            if(statusCode == HttpsURLConnection.HTTP_OK){
                // Do normal input or output stream reading
                //in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                in = new BufferedReader(new InputStreamReader(input));
                response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                responseString = response.toString();
            }
            else {
                Log.d("getResponseCode","FAILED");
                in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                Log.d("[getDataInternet]", Util.parserContent(response.toString(), null));
                responseString = ""; // See documentation for more info on response handling
            }
        } catch (Exception e) {
            String err;
            err = e.getMessage();
            Log.d("[doInBackground]", err == null ? e.toString() : err);
        }
        return responseString;
    }
}