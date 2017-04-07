package com.example.jaykishan.intern;

import android.app.Service;
import android.content.Intent;
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

        Log.v(LOG_TAG,webUrl);

        updatedWebView.setWebViewClient(new MyBrowser());
        updatedWebView.loadUrl("http://stackoverflow.com/questions/21797401/how-to-avoid-adding-duplicate-values-in-shared-prefernces-in-android");


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

            updatedWebView.saveState(resultData);
            resultReceiver.send(100,resultData);

        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
