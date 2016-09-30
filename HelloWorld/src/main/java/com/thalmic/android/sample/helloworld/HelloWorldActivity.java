/*
 * Copyright (C) 2014 Thalmic Labs Inc.
 * Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
 */

package com.thalmic.android.sample.helloworld;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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

public class HelloWorldActivity extends Activity implements SensorEventListener, View.OnClickListener{

    private TextView mLockStateView;
    private TextView mTextView;

    //myo加速度表示に使うテキストビュー/配列
    private TextView mxAccelerometerTextView;
    private TextView myAccelerometerTextView;
    private TextView mzAccelerometerTextView;
    ArrayList<Float> myoAccellist = new ArrayList<Float>();


    //myoジャイロセンサデータ表示に使うテキストビュー/配列
    private TextView mxGyroTextView;
    private TextView myGyroTextView;
    private TextView mzGyroTextView;
    ArrayList<Float> myoGyrolist = new ArrayList<Float>();


    //センサの作成
    private SensorManager manager;
    private Sensor accelSensor;
    private Sensor gyroSensor;

    //Androidデータ格納配列
    private float[] accelerometerValues = new float[3];
    private float[] gyroValues = new float[3];

    //Android加速度データ表示に使うテキストビュー/配列
    private TextView axAccelerometerTextView;
    private TextView ayAccelerometerTextView;
    private TextView azAccelerometerTextView;
    ArrayList<Float> androidAccellist = new ArrayList<Float>();


    //Androidジャイロデータ表示に使うテキストビュー/配列
    private TextView axGyroTextView;
    private TextView ayGyroTextView;
    private TextView azGyroTextView;
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

            //myo加速度の配列への格納
            myoAccellist.add((float)timestamp);
            myoAccellist.add((float)accel.x());
            myoAccellist.add((float)accel.y());
            myoAccellist.add((float)accel.z());


            //myo加速度をテキストへセット
            mxAccelerometerTextView.setText(String.valueOf(accel.x()));
            myAccelerometerTextView.setText(String.valueOf(accel.y()));
            mzAccelerometerTextView.setText(String.valueOf(accel.z()));
        }

        //myoジャイロセンサデータの取得
        @Override
        public void onGyroscopeData(Myo myo, long timestamp, Vector3 gyro) {
            super.onGyroscopeData(myo, timestamp, gyro);

            //myo角速度の配列への格納
            myoAccellist.add((float)timestamp);
            myoAccellist.add((float)gyro.x());
            myoAccellist.add((float)gyro.y());
            myoAccellist.add((float)gyro.z());

            //myo角速度をテキストへセット
            mxGyroTextView.setText(String.valueOf(gyro.x()));
            myGyroTextView.setText(String.valueOf(gyro.y()));
            mzGyroTextView.setText(String.valueOf(gyro.z()));
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
        //mTextView = (TextView) findViewById(R.id.text);

        mxAccelerometerTextView= (TextView) findViewById(R.id.mxAccelerometerValue);
        myAccelerometerTextView= (TextView) findViewById(R.id.myAccelerometerValue);
        mzAccelerometerTextView= (TextView) findViewById(R.id.mzAccelerometerValue);

        mxGyroTextView= (TextView) findViewById(R.id.mxGyroValue);
        myGyroTextView= (TextView) findViewById(R.id.myGyroValue);
        mzGyroTextView= (TextView) findViewById(R.id.mzGyroValue);

        axAccelerometerTextView= (TextView) findViewById(R.id.axAccelerometerValue);
        ayAccelerometerTextView= (TextView) findViewById(R.id.ayAccelerometerValue);
        azAccelerometerTextView= (TextView) findViewById(R.id.azAccelerometerValue);

        axGyroTextView= (TextView) findViewById(R.id.axGyroValue);
        ayGyroTextView= (TextView) findViewById(R.id.ayGyroValue);
        azGyroTextView= (TextView) findViewById(R.id.azGyroValue);

        Button startbutton = (Button) findViewById(R.id.startbutton);
        Button stopbutton = (Button) findViewById(R.id.stopbutton);
        startbutton.setOnClickListener(this);
        stopbutton.setOnClickListener(this);



        manager = (SensorManager)getSystemService(SENSOR_SERVICE);
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

                //Androidの加速度を配列に格納
                androidAccellist.add((float) event.timestamp);
                androidAccellist.add(event.values[0]);
                androidAccellist.add(event.values[1]);
                androidAccellist.add(event.values[2]);
            }
            else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
                gyroValues = event.values.clone();

                //Androidの角速度を配列に格納
                androidGyrolist.add((float) event.timestamp);
                androidGyrolist.add(event.values[0]);
                androidGyrolist.add(event.values[1]);
                androidGyrolist.add(event.values[2]);

            }

            //Androidの加速度をテキストにセット
            axAccelerometerTextView.setText(String.valueOf(accelerometerValues[0]));
            ayAccelerometerTextView.setText(String.valueOf(accelerometerValues[1]));
            azAccelerometerTextView.setText(String.valueOf(accelerometerValues[2]));

            //Androidの角速度をテキストにセット
            axGyroTextView.setText(String.valueOf(gyroValues[0]));
            ayGyroTextView.setText(String.valueOf(gyroValues[1]));
            azGyroTextView.setText(String.valueOf(gyroValues[2]));

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_GAME);
        manager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.startbutton:
                androidAccellist.clear();
                androidGyrolist.clear();
                myoAccellist.clear();
                myoGyrolist.clear();
                break;
            case R.id.stopbutton:
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.example.yukinakajima.sheettest", "com.example.yukinakajima.sheettest.MainActivity");

                float[] aa = new float[androidAccellist.size()];
                for(int i=0; i<aa.length; i++){
                    aa[i] = androidAccellist.get(i);
                }

                float[] ag = new float[androidGyrolist.size()];
                for(int i=0; i<ag.length; i++){
                    ag[i] = androidGyrolist.get(i);
                }

                float[] ma = new float[myoAccellist.size()];
                for(int i=0; i<ma.length; i++){
                    ma[i] = myoAccellist.get(i);
                }

                float[] mg = new float[myoGyrolist.size()];
                for(int i=0; i<mg.length; i++){
                    mg[i] = myoGyrolist.get(i);
                }

                intent.putExtra("androidaccel",aa);
                intent.putExtra("androidgyro",ag);
                intent.putExtra("myoaccel",ma);
                intent.putExtra("myogyro",mg);

                try{
                    startActivity(intent);
                }catch(Exception e){
                    Toast.makeText(this,"対象のアプリがありません",Toast.LENGTH_SHORT).show();
            }
                break;
        }

    }
}
