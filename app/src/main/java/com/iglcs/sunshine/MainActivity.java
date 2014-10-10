package com.iglcs.sunshine;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;



public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = "Sunshine MAINACT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        ArrayAdapter<String> m_adapter;

        public PlaceholderFragment() {
        }


        class WeatherUpdate extends AsyncTask<String, Void, String> {

            private final static String WEATHERAPI_AUTHORITY = "api.openweathermap.org";
            private final static String WEATHERAPI_FORCAST_SERVICE = "data/2.5/forecast/daily";
            private final static String DEFAULT_POSTCODE = "94370";


            @Override
            protected String doInBackground(String... strings) {
                HttpURLConnection urlConnection = null;
                BufferedReader reader           = null;

                //get the parameter -> postal code
                String postCode = DEFAULT_POSTCODE;
                if(strings.length>0)
                    postCode = strings[0];

                // Will contain the raw JSON response as a string.
                String forecastJsonStr = null;

                try {
                    // Construct the URL for the OpenWeatherMap query
                    // Possible parameters are available at OWM's forecast API page, at
                    // http://openweathermap.org/API#forecast

                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme("http").
                            authority(WEATHERAPI_AUTHORITY).
                            appendPath(WEATHERAPI_FORCAST_SERVICE).
                            appendQueryParameter("q", postCode).
                            appendQueryParameter("mode", "json").
                            appendQueryParameter("units", "metric").
                            appendQueryParameter("cnt", "7");

                    URL url = new URL(builder.build().toString());



                    // Create the request to OpenWeatherMap, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        forecastJsonStr = null;
                    }
                    assert inputStream != null;
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line).append("\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        forecastJsonStr = null;
                    }
                    forecastJsonStr = buffer.toString();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attempting
                    // to parse it.
                    forecastJsonStr = null;
                } finally{
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e("PlaceholderFragment", "Error closing stream", e);
                        }
                    }
                }
                return forecastJsonStr;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {

                    ArrayList<String> forcasts = WeatherDataParser.getForcastArray(s);
                    for(String str:forcasts) {
                        m_adapter.add(str);
                    }
                } catch (JSONException e) {
                   Log.d(LOG_TAG,e.getMessage());
                }


            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {


            new WeatherUpdate().execute();

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            ArrayList<String> data = new ArrayList<String>();

            m_adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, data);
            ListView l = (ListView) rootView.findViewById(R.id.listview_forecast);
            l.setAdapter(m_adapter);
            l.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    Context context = getActivity().getApplicationContext();

                    Intent detailIntent = new Intent(context,DetailActivity.class);
                    detailIntent.putExtra(Intent.EXTRA_TEXT,(String)adapterView.getAdapter().getItem(i));
                    detailIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(detailIntent);

//                    CharSequence toast_message =(String)adapterView.getAdapter().getItem(i) ;
//                    int duration = Toast.LENGTH_SHORT;
//                    Toast toast = Toast.makeText(context,toast_message,duration);
//                    toast.show();
//                    Log.v("TEST","Hello world !!");
                }
            });
            return rootView;
        }
    }
}
