package com.example.alex_reuter.accelerometerdata;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Vibrator;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

//Can implement if go upside down then plane crashes.
//Get rid of groundRect Rect.
//Fix strange upside down plane flip.
//Get scaling correct for a simple rect.

public class MainActivity extends Activity implements SensorEventListener{

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private Bitmap mBitmap;
    private Bitmap Plane;
    private Canvas mCanvas;
    public double currentRot = 0;
    public double targetRot = 0;
    double offsetO = 0;
    double offsetT= 0;
    double offsetH = 0;
    double offsetF = 0;

    public int maxXchange = 6;
    public int planeSpeed = 1;
    //MAKE PLANE SPEED WORK

    //TEMP VARIABLES TO BE DELETED

    //Speed turning asssociated to rotation angle.

    public double xChange = 0;



    //How is drawing scalable rectangles gonna work? /*
    //
    // Have one method that is looping called on each box that calculates it's new position.
    // Goes through an array. Deletes any boxes that go outside the bounds with their position calculations.
    //
    // */


    //Set to 1 for no smoothing
    //20 is ridiculously slow response time.
    public int smoothRate = 10;

    //This is just used for the vibration code.
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //GEt x and y of screen
        Display displayX = getWindowManager().getDefaultDisplay();
        final int width = displayX.getWidth();
        final int height = displayX.getHeight();


        //Sizes of stuff
        final int planeWidth = width/3;
        final int planeHeight = height-(planeWidth+(height/8));
        final int boxSize = 40;

        //Vibration test

        Vibrator vib = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(1000);

        mBitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        Plane = BitmapFactory.decodeResource(getResources(), R.drawable.plane_outline);
        Plane = Bitmap.createScaledBitmap(Plane, planeWidth, planeWidth, false);
        mCanvas = new Canvas(mBitmap);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);


        View v = new View(this)
        {

            @Override
            protected void onDraw(Canvas canvas) {

                //USED FOR DISPLAY SMOOTHING
                currentRot = currentRot + (targetRot-currentRot)/smoothRate;
                xChange = currentRot/(maxXchange);

                //PAINT DECLARATION
                Paint white = new Paint();
                white.setColor(0xFFFFFFFF);
                white.setStrokeWidth(0);

                Paint black = new Paint();
                black.setColor(0xFF000000);
                black.setStrokeWidth(0);

                Paint ground = new Paint();
                ground.setColor(0xFF994D00);
                ground.setStrokeWidth(0);

                canvas.drawColor(0xFFFFFFFF);

                canvas.save();
                canvas.rotate((float) currentRot, width / 2, planeHeight + (planeWidth / 2));
                canvas.drawRect(-width, height / 2, 2 * width, 2 * height, ground);
                canvas.restore();


                canvas.drawRect(((width/2)-(boxSize/2)) + (float)offsetO,height/2 + (float)offsetT,(width/2)+(boxSize/2)+(float)offsetH,(height/2)+boxSize +(float)offsetF, black);
                offsetO = offsetO-1+xChange;
                offsetT = offsetT+2;
                offsetH = offsetH +1+xChange;
                offsetF = offsetF+4;

                if((height/2)+40+offsetF>planeHeight+(planeWidth/2)) {
                    offsetO = 0;
                    offsetT = 0;
                    offsetH = 0;
                    offsetF = 0;
                }


                canvas.drawBitmap(Plane, width / 2 - (planeWidth / 2), planeHeight, white);


                //This is the rate that the view is re-drawn
                postInvalidateDelayed(1);
            }
        };

        setContentView(v);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            //This changes where the plane will ultimately turn to.
            targetRot = Math.toDegrees(Math.atan2(sensorEvent.values[0],sensorEvent.values[1]));
            if(targetRot>45)
            {
                targetRot = 45;
            }
            else if(targetRot<-45)
            {
                targetRot = -45;
            }
        }
    }

//Just ignore this, needs to be overidden to implement SensorData
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
