package com.example.jaykishan.intern;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewService extends Service {

    String LOG_TAG=WebViewService.class.getName();

    WebView updatedWebView;
    ResultReceiver resultReceiver;
    String webUrl;
    Bundle resultData;
    String cachePath;
    String thumbnailClicked;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor edit;

    {
        resultData = new Bundle();

    }

    public WebViewService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.v(LOG_TAG,"Service called");
        updatedWebView = new WebView(this);

        resultReceiver = intent.getParcelableExtra("receiver");
        webUrl=intent.getStringExtra("weburl");
        cachePath = intent.getStringExtra("cachePath");
        thumbnailClicked = intent.getStringExtra("thumbnailClicked");


        sharedPref = getSharedPreferences(thumbnailClicked,MODE_PRIVATE);
        edit = sharedPref.edit();

        Log.v(LOG_TAG,webUrl);

        updatedWebView.getSettings().setLoadsImagesAutomatically(true);
        updatedWebView.getSettings().setDomStorageEnabled(true);
        updatedWebView.getSettings().setAppCacheEnabled(true);
        updatedWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//        // Set cache size to 8 mb by default. should be more than enough
        updatedWebView.getSettings().setAppCacheMaxSize(1024*1024*10);
        updatedWebView.getSettings().setAppCachePath(cachePath);
        updatedWebView.getSettings().setAllowFileAccess(true);

        updatedWebView.getSettings().setJavaScriptEnabled(true);

        updatedWebView.setWebViewClient(new MyBrowser());


        updatedWebView.loadUrl("https://www.youtube.com");



        return super.onStartCommand(intent, flags, startId);


    }


    private class MyBrowser extends WebViewClient {

        private final String LOG_TAG = MyBrowser.class.getSimpleName();

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            super.onPageStarted(view, url, favicon);
            Log.v(LOG_TAG,"started in Background");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
//
//            updatedWebView.saveState(resultData);


            edit.putString("load","https://www.youtube.com");
            edit.commit();

            resultReceiver.send(100,resultData);





        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
