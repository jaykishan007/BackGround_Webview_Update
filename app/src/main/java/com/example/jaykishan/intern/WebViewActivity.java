package com.example.jaykishan.intern;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView,duplicateWebView;
    private String webUrl;
    private Toast showToastMessage;
    private RelativeLayout layout;
    private Context context;

    private String cacheDir;

    private ProgressBar progress;

    private final String LOG_TAG = "WebView";
    private Handler handler = new Handler();
    private Intent intentservice;
    MyResultReceiver resultReceiver;
    private String webViewName;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        context = getApplicationContext();

        duplicateWebView = (WebView)findViewById(R.id.webviewDup);



        sharedPreferences = getSharedPreferences("ThumbnailCache",MODE_PRIVATE);
        editor = sharedPreferences.edit();



        layout=(RelativeLayout)findViewById(R.id.webViewLayout);//To pass layout to Snackbar as attribute

        Intent intent=getIntent();//Get intent object
        //get the extras which in this case is page URL to be loaded upon cliking the thumbnail(Image)
        webUrl=intent.getStringExtra(Intent.ACTION_MAIN);
        webViewName=intent.getStringExtra("webViewName");

        progress=(ProgressBar) findViewById(R.id.progressBar2);
        progress.setMax(100);

        webView=(WebView) findViewById(R.id.webview);
        //Initializes WebView
        init();

        webViewCacheMode();

        Log.v(LOG_TAG,sharedPreferences.getString("load",""));

        webView.loadUrl(sharedPreferences.getString("load",""));

    }

    private void webViewCacheMode()
    {
        if(sharedPreferences.contains(webViewName))
        {
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
            enableBackgroundService();

            Log.v(LOG_TAG,"Cache Already");

        }
        else
        {
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            editor.putString("load","https://www.youtube.com");
            editor.putString(webViewName,"Exists");
            editor.commit();
            Log.v(LOG_TAG,"FirstTime");
        }


    }

    private void enableBackgroundService()
    {

        resultReceiver = new MyResultReceiver(handler);

        intentservice = new Intent(context, WebViewService.class);
        intentservice.putExtra("receiver",resultReceiver);
        intentservice.putExtra("weburl",webUrl);

        Log.v(LOG_TAG,"service yet to be called");
        startService(intentservice);

        Log.v(LOG_TAG,"service called");

    }

    private void init() {

        cacheDir = getDir(webViewName, Context.MODE_PRIVATE).getAbsolutePath();


        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.setWebChromeClient(new AppCacheWebChromeClient());
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        // Set cache size to 8 mb by default. should be more than enough
        webView.getSettings().setAppCacheMaxSize(1024*1024*10);
        webView.getSettings().setAppCachePath(cacheDir);
        webView.getSettings().setAllowFileAccess(true);

        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new MyBrowser());


    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onStart() {
        super.onStart();

        if(showToastMessage!=null)
            showToastMessage.cancel();

    }

    @Override
    protected void onStop() {
        super.onStop();

        if(isMyServiceRunning(WebViewService.class))
        {
            stopService(intentservice);
        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }



    private class MyBrowser extends WebViewClient {

        private final String LOG_TAG = MyBrowser.class.getSimpleName();

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            Log.v(LOG_TAG,"Load Started");
            webView.setVisibility(View.INVISIBLE);
            return false;
        }



        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);

            showToastMessage = Toast.makeText(WebViewActivity.this, error.toString(), Toast.LENGTH_SHORT);
            showToastMessage.show();

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            progress.setVisibility(View.VISIBLE);
            //progress.setProgress(0);
            super.onPageStarted(view, url, favicon);

            Log.v(LOG_TAG,"Loading");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progress.setVisibility(View.GONE);
            //progress.setProgress(100);
            webView.setVisibility(View.VISIBLE);
            Log.v(LOG_TAG,"PageFinished");

        }
    }



    private class AppCacheWebChromeClient extends WebChromeClient {


        @SuppressWarnings("deprecation")
        @Override
        public void onReachedMaxAppCacheSize(long spaceNeeded, long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
            Log.e(LOG_TAG, "onReachedMaxAppCacheSize reached, increasing space: " + spaceNeeded);
            quotaUpdater.updateQuota(spaceNeeded * 2);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            progress.setProgress(newProgress);
        }
    }



    class MyResultReceiver extends ResultReceiver {
        public MyResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {

            Snackbar snackbar = Snackbar
                    .make(layout, "Fresh Content Available", Snackbar.LENGTH_LONG)
                    .setAction("Reload", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(resultCode==100)
                            {
                                webView.setVisibility(View.INVISIBLE);
                                webView.clearCache(true);

                                duplicateWebView.getSettings().setAppCachePath(cacheDir);
                                duplicateWebView.restoreState(resultData);
                                duplicateWebView.setVisibility(View.VISIBLE);

                                editor.putString("load","http://stackoverflow.com/questions/21797401/how-to-avoid-adding-duplicate-values-in-shared-prefernces-in-android");
                                editor.commit();

                            }

                        }
                    });

            snackbar.show();


        }

    }


}
