/*
 * Copyright (C) 2014 Thalmic Labs Inc.
 * Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
 */

package com.thalmic.android.sample.helloworld;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

import java.util.ArrayList;

public class HelloWorldActivity extends Activity implements SensorEventListener, View.OnClickListener {
    private long startTime = 0;

    private TextView state;

    private TextView mLockStateView;
    private TextView mTextView;

    //myo加速度に関する変数
    private TextView[] mAccelerometerTextView = new TextView[3];
    private boolean mFirst = true;
    private float[] lowpass_m = new float[3];
    private float[] m = new float[3];
    private float[] m2 = new float[3];
    private float M;
    ArrayList<Float> myoAccellist = new ArrayList<Float>();


    //myo角速度に関する変数
    private TextView[] mGyroTextView = new TextView[3];
    private boolean mgFirst = true;
    private float[] mg = new float[3];
    ArrayList<Float> myoGyrolist = new ArrayList<Float>();


    //センサの作成
    private SensorManager manager;
    private Sensor accelSensor;
    private Sensor gyroSensor;

    //Androidデータ格納配列
    private float[] accelerometerValues = new float[3];
    private float[] gyroValues = new float[3];

    //Android加速度に関する変数
    private TextView[] aAccelerometerTextView = new TextView[3];
    private boolean aFirst = true;
    private float[] lowpass_a = new float[3];
    private float[] a = new float[3];
    private float[] a2 = new float[3];
    private float A;
    ArrayList<Float> androidAccellist = new ArrayList<Float>();


    //Android角速度に関する変数
    private TextView[] aGyroTextView = new TextView[3];
    private boolean agFirst = true;
    private float[] ag = new float[3];
    ArrayList<Float> androidGyrolist = new ArrayList<Float>();


    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {

        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            // Set the text color of the text view to cyan when a Myo connects.
            mTextView.setTextColor(Color.CYAN);
        }

        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            // Set the text color of the text view to red when a Myo disconnects.
            mTextView.setTextColor(Color.RED);
        }

        // onArmSync() is called whenever Myo has recognized a Sync Gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.
        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            mTextView.setText(myo.getArm() == Arm.LEFT ? R.string.arm_left : R.string.arm_right);
        }

        // onArmUnsync() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            mTextView.setText(R.string.hello_world);
        }

        // onUnlock() is called whenever a synced Myo has been unlocked. Under the standard locking
        // policy, that means poses will now be delivered to the listener.
        @Override
        public void onUnlock(Myo myo, long timestamp) {
            mLockStateView.setText(R.string.unlocked);
        }

        // onLock() is called whenever a synced Myo has been locked. Under the standard locking
        // policy, that means poses will no longer be delivered to the listener.
        @Override
        public void onLock(Myo myo, long timestamp) {
            mLockStateView.setText(R.string.locked);
        }

        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
            float roll = (float) Math.toDegrees(Quaternion.roll(rotation));
            float pitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
            float yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));

            // Adjust roll and pitch for the orientation of the Myo on the arm.
            if (myo.getXDirection() == XDirection.TOWARD_ELBOW) {
                roll *= -1;
                pitch *= -1;
            }

            // Next, we apply a rotation to the text view using the roll, pitch, and yaw.
            mTextView.setRotation(roll);
            mTextView.setRotationX(pitch);
            mTextView.setRotationY(yaw);
        }

        //myo加速度データの取得
        @Override
        public void onAccelerometerData(Myo myo, long timestamp, Vector3 accel) {
            super.onAccelerometerData(myo, timestamp, accel);
            //Log.d(TAG, "onAccelerometerData: "+accel.x()+"\t"+accel.y()+"\t"+accel.z());

            if (mFirst) {
                lowpass_m[0] = (float) accel.x();
                lowpass_m[1] = (float) accel.y();
                lowpass_m[2] = (float) accel.z();

                mFirst = false;
            } else {
                //ローパスフィルタをかける
                lowpass_m[0] = (float) (lowpass_m[0] * 0.9 + accel.x() * 0.1);
                lowpass_m[1] = (float) (lowpass_m[1] * 0.9 + accel.y() * 0.1);
                lowpass_m[2] = (float) (lowpass_m[2] * 0.9 + accel.z() * 0.1);

                //ハイパスフィルタで重力除去
                m[0] = (float) accel.x() - lowpass_m[0];
                m[1] = (float) accel.y() - lowpass_m[1];
                m[2] = (float) accel.z() - lowpass_m[2];
            }

            for (int i = 0; i < m.length; i++) {
                m2[i] = m[i] * m[i];
            }

            M = (float) (Math.sqrt(m2[0] + m2[1] + m2[2]));

            //myo加速度の配列への格納
            myoAccellist.add((float) ((System.nanoTime() - startTime) * 0.000001));
            for (int i = 0; i < m.length; i++) {
                myoAccellist.add(m[i]);
            }
            myoAccellist.add(M);

            //myo加速度をテキストへセット
            for (int i = 0; i < m.length; i++) {
                mAccelerometerTextView[i].setText(String.valueOf(m[i]));
            }
        }

        //myoジャイロセンサデータの取得
        @Override
        public void onGyroscopeData(Myo myo, long timestamp, Vector3 gyro) {
            super.onGyroscopeData(myo, timestamp, gyro);

            if (mgFirst) {
                mg[0] = (float) gyro.x();
                mg[1] = (float) gyro.y();
                mg[2] = (float) gyro.z();

                mgFirst = false;
            } else {
                //ローパスフィルタをかける
                mg[0] = (float) ((mg[0] * 0.9 + gyro.x() * 0.1) * 0.01745);
                mg[1] = (float) ((mg[1] * 0.9 + gyro.y() * 0.1) * 0.01745);
                mg[2] = (float) ((mg[2] * 0.9 + gyro.z() * 0.1) * 0.01745);
            }

            //myo角速度の配列への格納
            myoGyrolist.add((float) ((System.nanoTime() - startTime) * 0.000001));
            for (int i = 0; i < mg.length; i++) {
                myoGyrolist.add(mg[i]);
            }

            //myo角速度をテキストへセット
            for (int i = 0; i < mg.length; i++) {
                mGyroTextView[i].setText((String.valueOf(mg[i])));
            }
        }


        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.
            switch (pose) {
                case UNKNOWN:
                    mTextView.setText(getString(R.string.hello_world));
                    break;
                case REST:
                case DOUBLE_TAP:
                    int restTextId = R.string.hello_world;
                    switch (myo.getArm()) {
                        case LEFT:
                            restTextId = R.string.arm_left;
                            break;
                        case RIGHT:
                            restTextId = R.string.arm_right;
                            break;
                    }
                    mTextView.setText(getString(restTextId));
                    break;
                case FIST:
                    mTextView.setText(getString(R.string.pose_fist));
                    break;
                case WAVE_IN:
                    mTextView.setText(getString(R.string.pose_wavein));
                    break;
                case WAVE_OUT:
                    mTextView.setText(getString(R.string.pose_waveout));
                    break;
                case FINGERS_SPREAD:
                    mTextView.setText(getString(R.string.pose_fingersspread));
                    break;
            }

            if (pose != Pose.UNKNOWN && pose != Pose.REST) {
                // Tell the Myo to stay unlocked until told otherwise. We do that here so you can
                // hold the poses without the Myo becoming locked.
                myo.unlock(Myo.UnlockType.HOLD);

                // Notify the Myo that the pose has resulted in an action, in this case changing
                // the text on the screen. The Myo will vibrate.
                myo.notifyUserAction();
            } else {
                // Tell the Myo to stay unlocked only for a short period. This allows the Myo to
                // stay unlocked while poses are being performed, but lock after inactivity.
                myo.unlock(Myo.UnlockType.TIMED);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world);

        //mLockStateView = (TextView) findViewById(R.id.lock_state);
        mTextView = (TextView) findViewById(R.id.text);

        state = (TextView) findViewById(R.id.state);

        mAccelerometerTextView[0] = (TextView) findViewById(R.id.mxAccelerometerValue);
        mAccelerometerTextView[1] = (TextView) findViewById(R.id.myAccelerometerValue);
        mAccelerometerTextView[2] = (TextView) findViewById(R.id.mzAccelerometerValue);

        mGyroTextView[0] = (TextView) findViewById(R.id.mxGyroValue);
        mGyroTextView[1] = (TextView) findViewById(R.id.myGyroValue);
        mGyroTextView[2] = (TextView) findViewById(R.id.mzGyroValue);

        aAccelerometerTextView[0] = (TextView) findViewById(R.id.axAccelerometerValue);
        aAccelerometerTextView[1] = (TextView) findViewById(R.id.ayAccelerometerValue);
        aAccelerometerTextView[2] = (TextView) findViewById(R.id.azAccelerometerValue);

        aGyroTextView[0] = (TextView) findViewById(R.id.axGyroValue);
        aGyroTextView[1] = (TextView) findViewById(R.id.ayGyroValue);
        aGyroTextView[2] = (TextView) findViewById(R.id.azGyroValue);

        Button startbutton = (Button) findViewById(R.id.startbutton);
        Button stopbutton = (Button) findViewById(R.id.stopbutton);
        startbutton.setOnClickListener(this);
        stopbutton.setOnClickListener(this);


        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // First, we initialize the Hub singleton with an application identifier.
        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // We don't want any callbacks when the Activity is gone, so unregister the listener.
        Hub.getInstance().removeListener(mListener);

        if (isFinishing()) {
            // The Activity is finishing, so shutdown the Hub. This will disconnect from the Myo.
            Hub.getInstance().shutdown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (R.id.action_scan == id) {
            onScanActionSelected();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onScanActionSelected() {
        // Launch the ScanActivity to scan for Myos to connect to.
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values.clone();

                //ローパスフィルタをかける
                for (int i = 0; i < accelerometerValues.length; i++) {
                    lowpass_a[i] = lowpass_a[i] * 0.9f + accelerometerValues[i] * 0.1f;
                }

                //ハイパスフィルタをかけることで重力除去
                for (int i = 0; i < a.length; i++) {
                    a[i] = accelerometerValues[i] - lowpass_a[i];
                }

                for (int i = 0; i < a.length; i++) {
                    a2[i] = a[i] * a[i];
                }

                A = (float) (Math.sqrt(a2[0] + a2[1] + a2[2])); //合成加速度

                //Androidの加速度を配列に格納
                androidAccellist.add((float) ((System.nanoTime() - startTime) * 0.000001));
                for (int i = 0; i < a.length; i++) {
                    androidAccellist.add(a[i]);
                }
                androidAccellist.add(A);

            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                gyroValues = event.values.clone();

                if (agFirst) {
                    for (int i = 0; i < gyroValues.length; i++) {
                        ag[i] = gyroValues[i];
                    }
                    agFirst = false;
                } else {
                    //ローパスフィルタをかける
                    for (int i = 0; i < ag.length; i++) {
                        ag[i] = ag[i] * 0.9f + gyroValues[i] * 0.1f;
                    }
                }

                //Androidの角速度を配列に格納
                androidGyrolist.add((float) ((System.nanoTime() - startTime) * 0.000001));
                for (int i = 0; i < ag.length; i++) {
                    androidGyrolist.add(ag[i]);
                }
            }

            //Androidの加速度をテキストにセット
            for (int i = 0; i < aAccelerometerTextView.length; i++) {
                aAccelerometerTextView[i].setText(String.valueOf(a[i]));
            }

            //Androidの角速度をテキストにセット
            for (int i = 0; i < aGyroTextView.length; i++) {
                aGyroTextView[i].setText(String.valueOf(ag[i]));
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_FASTEST);
        manager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startbutton:
                state.setTextColor(Color.CYAN);
                state.setText("計測中");
                androidAccellist.clear();
                androidGyrolist.clear();
                myoAccellist.clear();
                myoGyrolist.clear();
                Toast.makeText(this, "計測を開始します", Toast.LENGTH_SHORT).show();
                startTime = System.nanoTime();
                break;
            case R.id.stopbutton:
                state.setTextColor(Color.WHITE);
                state.setText("計測終了");
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.example.yukinakajima.sheettest", "com.example.yukinakajima.sheettest.MainActivity");

                float[] aa = new float[androidAccellist.size()];
                for (int i = 0; i < aa.length; i++) {
                    aa[i] = androidAccellist.get(i);
                }

                float[] ag = new float[androidGyrolist.size()];
                for (int i = 0; i < ag.length; i++) {
                    ag[i] = androidGyrolist.get(i);
                }

                float[] ma = new float[myoAccellist.size()];
                for (int i = 0; i < ma.length; i++) {
                    ma[i] = myoAccellist.get(i);
                }

                float[] mg = new float[myoGyrolist.size()];
                for (int i = 0; i < mg.length; i++) {
                    mg[i] = myoGyrolist.get(i);
                }

                intent.putExtra("androidaccel", aa);
                intent.putExtra("androidgyro", ag);
                intent.putExtra("myoaccel", ma);
                intent.putExtra("myogyro", mg);

                try {
                    startActivity(intent);
                    Toast.makeText(this, "計測を終了します", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "対象のアプリがありません", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }
}
