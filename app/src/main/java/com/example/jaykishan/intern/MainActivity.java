package com.example.jaykishan.intern;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private String api="https://api.myjson.com/bins/18qvg9";
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private ImageView imageView;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting an Listner to image to respond to click events
        // While fetching data from API,url for webpage has been plugged into Image content description
        imageView=(ImageView) findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.v(LOG_TAG, (String) imageView.getContentDescription());

                Intent intent=new Intent(getApplicationContext(),WebViewActivity.class);
                intent.putExtra(Intent.ACTION_MAIN,imageView.getContentDescription());
                startActivity(intent);

            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);


        execute();


    }

    //Checks the Network connectivity Service of System
    public static boolean hasActiveInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private void execute()
    {

        if(hasActiveInternetConnection(this)) {
            Log.v(LOG_TAG,"Internet Connected");
            new FetchAPI().execute(api);

        }
        else
        {
            Log.v(LOG_TAG,"No Internet");

        }


    }


    private class FetchAPI extends AsyncTask<String, Void, String[]>
    {

        private final String LOG_TAG = FetchAPI.class.getSimpleName();


        private String[] getDataFromJson(String jsonStr)
                throws JSONException {

            String[] resultStrs = new String[2];

            final String pageUrl="url";
            final String imageUrl = "default-image";

            JSONObject jObject = new JSONObject(jsonStr);

            String jsonUrl = jObject.getString(pageUrl);
            String jsonImageUrl=jObject.getString(imageUrl);


            resultStrs[0]=jsonUrl;
            resultStrs[1]=jsonImageUrl;

            Log.v(LOG_TAG,jsonUrl);
            Log.v(LOG_TAG,jsonImageUrl);

            return resultStrs;

        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String[] strings) {

            // todo: save back the url to shared preferences

            com.squareup.picasso.Picasso.with(MainActivity.this).
                    load(strings[1]).
                    placeholder(R.mipmap.ic_launcher).
                    into(imageView);

            imageView.setContentDescription(strings[0]);

            progressBar.setVisibility(View.GONE);

            super.onPostExecute(strings);
        }

        @Override
        protected String[] doInBackground(String... params) {

            if(params.length==0)
            {
                return null;
            }

                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String jsonStr=null;


                Uri uri=Uri.parse(params[0]);
                URL url;


                try {

                    url = new URL(uri.toString());

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return null;
                    }

                    jsonStr = buffer.toString();

                    Log.v(LOG_TAG,jsonStr);


                } catch (IOException e) {

                    Log.e(LOG_TAG,"Error in IO");

                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();

                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error closing stream", e);
                        }
                    }
                }

                try {
                    return getDataFromJson(jsonStr);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }

            return null;
        }
    }






}
