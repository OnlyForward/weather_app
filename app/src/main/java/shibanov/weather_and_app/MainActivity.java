package shibanov.weather_and_app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    final String APP_ID = "8ba35c1cddf9714c3ec833da4dace670";//8ba35c1cddf9714c3ec833da4dace670
    final long MIN_TIME = 5000;
    final float MIN_DISTANCE = 1000;
    final int REQUEST_CODE = 123;

    String Location_Provider = LocationManager.GPS_PROVIDER;


    TextView mCityName;
    ImageView mWeatherImage;
    TextView mTemperature;
    String city;

    LocationManager mLocationManager;
    LocationListener mLocationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_weather);
AppCenter.start(getApplication(), "a0c3ce17-881a-4df6-b70a-d4df84f905bc",
                  Analytics.class, Crashes.class);
        mCityName = (TextView) findViewById(R.id.fetching_city);
        mWeatherImage = (ImageView) findViewById(R.id.weather_symboll);
        mTemperature = (TextView) findViewById(R.id.temp);
        ImageView changeCityButton = (ImageView) findViewById(R.id.change_city_btn);
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ChangeCity.class);
                startActivityForResult(intent,REQUEST_CODE);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK){
            if(requestCode==REQUEST_CODE){
                city = data.getStringExtra("City");
                mCityName.setText(city);
            }
        }else{
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(city==null) {
            getWeatherForCurrentLocation();
        }else{
            getWeatherFromCity(city);
        }
    }

    private void getWeatherFromCity(String city) {
        RequestParams params = new RequestParams();
        params.put("q",city);
        params.put("appid",APP_ID);
        NetWorking(params);
    }

    private void getWeatherForCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());

                RequestParams params = new RequestParams();
                params.put("lat",latitude);
                params.put("lon",longitude);
                params.put("appid",APP_ID);

                NetWorking(params);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d("DebugVar", "Что-то отключено");
            }
        };
        checkPermission();
        mLocationManager.requestLocationUpdates(Location_Provider, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    private void NetWorking(RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL,params,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                super.onSuccess(statusCode, headers, response);
                Log.d("DebugVar",response.toString());
                WeatherData weatherData = WeatherData.fromJson(response);
                mUpdate(weatherData);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("DebugVar",String.valueOf(statusCode));
            }
        });
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, REQUEST_CODE);
                return;
            }
        }
    }

    private void mUpdate(WeatherData weatherData){
        mTemperature.setText(weatherData.getTemperature());
        mCityName.setText(weatherData.getCity());
        int resource = getResources().getIdentifier(weatherData.getIconName(),"drawable",getPackageName());
        mWeatherImage.setImageResource(resource);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mLocationManager!=null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }else{
                    getWeatherForCurrentLocation();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }
}
