package com.example.jaykishan.intern;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

    private BroadcastReceiver receiver;


    private String cacheDir;

    private ProgressBar progress;

    private final String LOG_TAG = "WebView";
    private Handler handler = new Handler();
    private Intent intentservice;
    MyResultReceiver resultReceiver;
    private String thumbNailClicked;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public WebViewActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        context = getApplicationContext();

        //webview initially that loads cache content if available
        webView=(WebView) findViewById(R.id.webview);

        //Duplicate layout to load fresh content when loaded in background
        duplicateWebView = (WebView)findViewById(R.id.webviewDup);

        //To pass layout to Snackbar as attribute
        layout=(RelativeLayout)findViewById(R.id.webViewLayout);

        //Get intent object
        Intent intent=getIntent();
        //get the extras which in this case is page URL to be loaded upon cliking the thumbnail(Image)
        webUrl=intent.getStringExtra(Intent.ACTION_MAIN);


        thumbNailClicked=intent.getStringExtra(String.valueOf(R.string.webViewName));


        sharedPreferences = getSharedPreferences(thumbNailClicked,MODE_PRIVATE);
        editor = sharedPreferences.edit();


        progress=(ProgressBar) findViewById(R.id.progressBar2);
        progress.setMax(100);


        //Initializes WebView
        init(webView);

        webViewCacheMode();


        webView.loadUrl(sharedPreferences.getString("load",""));

    }

    private void registerNetworkReceiver()
    {
        String net = "android.net.conn.CONNECTIVITY_CHANGE";

        IntentFilter filter = new IntentFilter();
        filter.addAction(net);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                Log.v(LOG_TAG,"Broad caster");

                if(MainActivity.hasActiveInternetConnection(context))
                {
                    enableBackgroundService();
                    Log.v(LOG_TAG,"Network presetnt Broadcast");
                }
                else
                {
                    Log.v(LOG_TAG,"Network not present Broadcast");
                }


            }
        };
        registerReceiver(receiver, filter);
    }

    private void webViewCacheMode()
    {
        if(sharedPreferences.contains("cached"))
        {
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);

            // Register and tried to listen to broadcast of network changes
            registerNetworkReceiver();

//            if(MainActivity.hasActiveInternetConnection(this))
//            {
//                enableBackgroundService();
//                Log.v(LOG_TAG,"NetConnected");
//            }
//            else
//            {
//                Toast.makeText(WebViewActivity.this, "Oops..Check Network", Toast.LENGTH_SHORT).show();
//            }


            Log.v(LOG_TAG,"Cache Already");

        }
        else
        {
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            Log.v(LOG_TAG,"cache Doesnt Exist");
            editor.putString("load","http://www.amazon.in/");
            editor.putString("cached","Exists");
            editor.commit();
        }


    }

    public void enableBackgroundService()
    {

        resultReceiver = new MyResultReceiver(handler);

        intentservice = new Intent(context, WebViewService.class);
        intentservice.putExtra("receiver",resultReceiver);
        intentservice.putExtra("weburl",webUrl);
        intentservice.putExtra("cachePath",cacheDir);
        intentservice.putExtra("thumbnailClicked",thumbNailClicked);

        Log.v(LOG_TAG,"service yet to be called");
        startService(intentservice);

        Log.v(LOG_TAG,"service called");

    }

    private void init(WebView web) {

        cacheDir = getDir(thumbNailClicked, Context.MODE_PRIVATE).getAbsolutePath();


        web.getSettings().setLoadsImagesAutomatically(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAppCacheEnabled(true);
        web.setWebChromeClient(new AppCacheWebChromeClient());
        web.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        // Set cache size to 8 mb by default. should be more than enough
        web.getSettings().setAppCacheMaxSize(1024*1024*10);
        web.getSettings().setAppCachePath(cacheDir);
        web.getSettings().setAllowFileAccess(true);

        web.getSettings().setJavaScriptEnabled(true);

        web.setWebViewClient(new MyBrowser());


    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

        if(isMyServiceRunning(WebViewService.class))
        {
            stopService(intentservice);
        }

        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
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
//
//            showToastMessage = Toast.makeText(WebViewActivity.this, error.toString(), Toast.LENGTH_SHORT);
//            showToastMessage.show();

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
                    .make(layout, "Fresh Content Available", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Reload", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(resultCode==100)
                            {
//                                webView.clearCache(true);

//                                duplicateWebView.restoreState(resultData);

                                webView.setVisibility(View.INVISIBLE);

                                init(duplicateWebView);
                                duplicateWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);

                                duplicateWebView.loadUrl(sharedPreferences.getString("load",""));

                                duplicateWebView.setVisibility(View.VISIBLE);


                            }

                        }
                    });


            snackbar.show();


        }

    }




}
