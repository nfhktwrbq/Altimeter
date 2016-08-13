package ru.andrew.altimeter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final boolean D = true;
    private SensorManager mSensorManager;
    private Sensor mBarSensor;
    private TextView BarText;
    private TextView AltText;
    private float bar;
    private float temp_bar;
    private float buf_bar;
    private float arr_bar[];
    private int kolB;
    private int ptrB;

    private AlertDialog.Builder mBilderAD;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BarText = (TextView)findViewById(R.id.BarTextView);
        AltText = (TextView)findViewById(R.id.AtlitideText);
        arr_bar = new float[50];

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mBarSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (mBarSensor == null)
        {
            mBilderAD = new AlertDialog.Builder(this);
            mBilderAD.setTitle(getResources().getString(R.string.adTitleBar));
            mBilderAD.setPositiveButton(getResources().getString(R.string.adButtonText), new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int arg1)
                {
                    finish();
                }
            });
        }
        else
        {
            Toast.makeText(this, mBarSensor.getVendor(),
                  Toast.LENGTH_LONG).show();
            mSensorManager.registerListener(this, mBarSensor, mSensorManager.SENSOR_DELAY_NORMAL);
            mBilderAD  = new AlertDialog.Builder(this);
            mBilderAD.setTitle(getResources().getString(R.string.adTitleTemp));
            mBilderAD.
        }
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        temp_bar = event.values[0];
        // Do sliding average with this sensor value.
        if(kolB < getResources().getInteger(R.integer.SlidingAverageLength)) kolB++;
        else buf_bar -= arr_bar[ptrB];
        buf_bar += temp_bar;
        arr_bar[ptrB] = temp_bar;
        ptrB++; if(ptrB == getResources().getInteger(R.integer.SlidingAverageLength)) ptrB = 0;
        bar = buf_bar/(float)kolB;
        BarText.setText(String.format("%.3f",bar));
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mBarSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void FreezeButtonClick(View v)
    {

    }

    public void SetNullButtonClick(View v)
    {

    }

}
