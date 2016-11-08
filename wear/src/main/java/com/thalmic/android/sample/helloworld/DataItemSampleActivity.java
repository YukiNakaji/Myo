package com.thalmic.android.sample.helloworld;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

public class DataItemSampleActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SensorEventListener {

    private GoogleApiClient mGoogleApiClient;

    private TextView stateText;

    private long starttime = 0;

    SensorManager sensorManager;
    Sensor accelSensor;
    Sensor gyroSensor;
    Sensor pressureSensor;
    private ArrayList<Float> watchAccellist = new ArrayList<>();
    private ArrayList<Float> watchGyrolist = new ArrayList<>();
    private ArrayList<Float> watchPresslist = new ArrayList<>();

    private float[] accValues = new float[3];
    private float[] lowpass_acc = new float[3];
    private float[] acc;

    private float[] gyroValues = new float[3];
    private float[] gyro;

    private float[] pressValues;
    private float press;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_item_sample);

        stateText = (TextView) findViewById(R.id.state);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    public void start(View v) {
        watchAccellist.clear();
        watchGyrolist.clear();
        watchPresslist.clear();

        starttime = System.nanoTime();
        stateText.setText("計測中");
    }

    public void stop(View v) {
        //加速度
        float[] wa = new float[watchAccellist.size()];
        for (int i = 0; i < wa.length; i++) {
            wa[i] = watchAccellist.get(i);
        }

        //角速度
        float[] wg = new float[watchGyrolist.size()];
        for (int i = 0; i < wg.length; i++) {
            wg[i] = watchGyrolist.get(i);
        }

        //気圧
        float[] wp = new float[watchPresslist.size()];
        for (int i = 0; i < wp.length; i++) {
            wp[i] = watchPresslist.get(i);
        }


        //API
        PutDataMapRequest dataMap = PutDataMapRequest.create("/dataitem/data");
        dataMap.getDataMap().putFloatArray("watch_accel", wa);
        dataMap.getDataMap().putFloatArray("watch_gyro", wg);
        dataMap.getDataMap().putFloatArray("watch_press", wp);

        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, request);
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.d("TAG", "onResult: " + dataItemResult.getStatus().toString());
            }
        });

        stateText.setText("計測終了");

        watchAccellist.clear();
        watchGyrolist.clear();
        watchPresslist.clear();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("TAG", "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("TAG", "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("TAG", "onConnectionFailed: " + connectionResult);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //加速度
            accValues = event.values.clone();
            acc = new float[3];

            //生データの格納
            watchAccellist.add((float) ((System.nanoTime() - starttime) * 0.000001));
            for (int i = 0; i < accValues.length; i++) {
                watchAccellist.add(accValues[i]);
            }

            //ローパスフィルタ
            for (int i = 0; i < accValues.length; i++) {
                lowpass_acc[i] = lowpass_acc[i] * 0.9f + accValues[i] * 0.1f;
            }

            //ハイパスフィルタをかけることで重力除去
            for (int i = 0; i < accValues.length; i++) {
                acc[i] = accValues[i] - lowpass_acc[i];
            }

            //フィルタをかけた後のWATCH加速度データの格納
            for (int i = 0; i < acc.length; i++) {
                watchAccellist.add(acc[i]);
            }

        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroValues = event.values.clone();
            gyro = new float[3];

            //生データの格納
            watchGyrolist.add((float) ((System.nanoTime() - starttime) * 0.000001));
            for (int i = 0; i < gyroValues.length; i++) {
                watchGyrolist.add(gyroValues[i]);
            }

            //ローパスフィルタをかける
            for (int i = 0; i < gyro.length; i++) {
                gyro[i] = gyro[i] * 0.9f + gyroValues[i] * 0.1f;
            }

            //フィルタをかけた後のWATCH角速度データの格納
            for (int i = 0; i < gyro.length; i++) {
                watchGyrolist.add(gyro[i]);
            }
        } else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            pressValues = event.values.clone();

            //生データの格納
            watchPresslist.add((float) ((System.nanoTime() - starttime) * 0.000001));
            watchPresslist.add(pressValues[0]);

            //ローパスフィルタ
            press = press * 0.9f + pressValues[0] * 0.1f;

           //フィルタをかけた後のWATCH気圧データの格納
            watchPresslist.add(press);
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
