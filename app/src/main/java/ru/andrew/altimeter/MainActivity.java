package ru.andrew.altimeter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final boolean D = true;
    private boolean flagStarStop = false;
    private SensorManager mSensorManager;
    private Sensor mBarSensor;
    private TextView BarText;
    private TextView AltText;
    private TextView TempText;
    private Button FreezeBut;
    private Button SetTempBut;
    private Button CancelTempBut;
    private RadioButton RBPos;
    private CheckBox CheckBoxPress;
    private float bar;
    private float temperature;
    private float temp_bar;
    private float alt;
    private float bar2;
    private float buf_bar;
    private float arr_bar[];
    private int kolB;
    private int ptrB;
    private boolean PressVal;

    private AlertDialog.Builder mBilderAD;
    private NumberPicker np;
    private Dialog mDialog;
    private DisplayMetrics dm;

    private SharedPreferences.Editor edtr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPref = this.getPreferences(this.MODE_PRIVATE);
        edtr = sharedPref.edit();
        PressVal = sharedPref.getBoolean(getString(R.string.saved_press_data), true);
        BarText = (TextView)findViewById(R.id.BarTextView);
        AltText = (TextView)findViewById(R.id.AtlitideText);
        TempText = (TextView)findViewById(R.id.textViewTempData);
        FreezeBut = (Button)findViewById(R.id.FreezeBut);
        CheckBoxPress = (CheckBox)findViewById(R.id.checkBoxPress);
        CheckBoxPress.setChecked(PressVal);
        arr_bar = new float[getResources().getInteger(R.integer.SlidingAverageLength)];
        dm = new DisplayMetrics();
        WindowManager wm = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        AltText.setTextSize(TypedValue.COMPLEX_UNIT_PX, dm.heightPixels/10);
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
            mBilderAD.show();
        }
        else
        {
            Toast.makeText(this, mBarSensor.getVendor(),
                  Toast.LENGTH_LONG).show();
            mSensorManager.registerListener(this, mBarSensor, mSensorManager.SENSOR_DELAY_NORMAL);
            mBilderAD  = new AlertDialog.Builder(this);
            mBilderAD.setTitle(getResources().getString(R.string.adTitleTemp));

            mBilderAD.setPositiveButton(getResources().getString(R.string.adButtonText), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    temperature = getResources().getInteger(R.integer.defaultValueofTemp);
                    TempText.setText(String.format("%.0f",temperature) + " " +(char)176 + "C");
                }
            });
        }
        temperature = getResources().getInteger(R.integer.defaultValueofTemp);
        TempText.setText(String.format("%.0f", temperature) + " " + (char) 176 + "C");
        show();
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
        if(CheckBoxPress.isChecked())
        {
            BarText.setText(String.format("%.3f",bar*0.75006375541921));
        }
        else
        {
            BarText.setText(String.format("%.3f",bar));
        }


        //estimate altitude
        if(!flagStarStop)
        {
            alt = 18400*(1+temperature/(float)273)*(float)Math.log10(bar2/bar);
            AltText.setText(String.format("%.1f", alt)/* + "\n" + getResources().getString(R.string.m)*/);
        }

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
        if(flagStarStop)
        {
            flagStarStop = false;
            FreezeBut.setText(getResources().getString(R.string.freeze));
        }
        else
        {
            flagStarStop = true;
            FreezeBut.setText(getResources().getString(R.string.unfreeze));
        }
    }

    public void SetNullButtonClick(View v)
    {
        bar2 = bar;
    }

    public void onClickCheckBox(View v)
    {
        if(CheckBoxPress.isChecked())
        {
            CheckBoxPress.setText(getResources().getString(R.string.bar));
            edtr.putBoolean(getString(R.string.saved_press_data), true);
            edtr.commit();
        }
        else
        {
            CheckBoxPress.setText(getResources().getString(R.string.mmhg));
            edtr.putBoolean(getString(R.string.saved_press_data), false);
            edtr.commit();
        }
    }

    public void onTempClick(View v)
    {
        mDialog.show();
    }


    public void show()
    {
        mDialog = new Dialog(this);
        mDialog.setTitle(getResources().getString(R.string.temp_set));
        mDialog.setContentView(R.layout.dialog);
        mDialog.setCanceledOnTouchOutside(false);
        SetTempBut = (Button) mDialog.findViewById(R.id.SetButton);
        CancelTempBut = (Button) mDialog.findViewById(R.id.CancelButton);
        RBPos = (RadioButton) mDialog.findViewById(R.id.radioButtonPos);
        np = (NumberPicker) mDialog.findViewById(R.id.numberPicker1);
        np.setMaxValue(getResources().getInteger(R.integer.maxValueofTemp) - getResources().getInteger(R.integer.minValueofTemp));
        np.setMinValue(0);
        np.setValue(getResources().getInteger(R.integer.defaultValueofTemp) - getResources().getInteger(R.integer.minValueofTemp));
        np.setWrapSelectorWheel(false);
        SetTempBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RBPos.isChecked()) {
                    temperature = (float) (np.getValue() + getResources().getInteger(R.integer.minValueofTemp));
                } else {
                    temperature = -(float) (np.getValue() + getResources().getInteger(R.integer.minValueofTemp));
                }
                TempText.setText(String.format("%.0f", temperature) + " " + (char) 176 + "C");
                mDialog.dismiss();
            }
        });
        CancelTempBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDialog.dismiss();
            }
        });
        mDialog.show();
    }
}
