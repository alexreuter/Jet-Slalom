package com.example.alex_reuter.accelerometerdata;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Vibrator;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.hardware.SensorEventListener;

import java.util.ArrayList;


//Fix strange upside down plane flip.
//Need to change speed according to screen density
//DISABLE AUTOWIN
//Need perspecive exponential box
//Fix floating above screen, think has to do with spawning
//BUG IN INITIAL Y POSITION CODE.
//SOMETHING TO DO WITH THE Y POSITIONS WHEN THEY ARE NEGATIVE


public class MainActivity extends Activity implements SensorEventListener
{

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private Bitmap mBitmap;
    private Bitmap Plane;
    private Canvas mCanvas;
    public double currentRot = 0;
    public double targetRot = 0;
    public double boxAngle = 0;
    //This is used to detirmine when to turn smoothing on and off
    public double delta = 0;
    //This detirmines when smoothing is turned off for rapid movements
    public double threshold = 30;
    //This detirmines the speed that blocks manouver
    public float Mmultiplier = 5;

    //Set to 1 for no smoothing
    //20 is ridiculously slow response time.
    public int defaultSmoothRate = 10;
    public int smoothRate = 10;
    public ArrayList <Box> boxes = new ArrayList<Box>();
    public int counter = 0;
    public float derivitive = 0;


    //This is just used for the vibration code.
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get x and y of screen
        Display displayX = getWindowManager().getDefaultDisplay();
        final int width = displayX.getWidth();
        final int height = displayX.getHeight();

        //Sizes of stuff
        final int planeWidth = width/6;
        final int planeHeight = height-(planeWidth+(height/8));
        final int boxSize = 10;

        //Vibration
        final Vibrator vib = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(300);

        mBitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        Plane = BitmapFactory.decodeResource(getResources(), R.drawable.plane_outline);
        Plane = Bitmap.createScaledBitmap(Plane, planeWidth, planeWidth, false);
        mCanvas = new Canvas(mBitmap);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);


        //**************************************    Testing Box Class *************************

        //THIS DETIRMINES THE TOTAL AMOUNT OF BLOCKS.
        for(int i = 0;i<1;i++)
        {
            boxes.add(new Box(width,height,planeHeight,planeWidth,boxSize));
        }

//        final box Fred = new box(width,height,boxSize);
//        final box John = new box(width,height,boxSize);

        View v = new View(this)
        {
            @Override
            protected void onDraw(Canvas canvas) {

                //USED FOR DISPLAY SMOOTHING
                delta = (targetRot-currentRot);
                if(Math.abs(delta)>threshold)
                {
                    smoothRate = 2;
                    currentRot = currentRot + (delta/smoothRate);
                    smoothRate = defaultSmoothRate;
                }
                else
                {
                    currentRot = currentRot + (delta/smoothRate);
                }

                //PAINT DECLARATION
                Paint white = new Paint();
                white.setColor(0xFFFFFFFF);
                white.setStrokeWidth(0);

                Paint black = new Paint();
                black.setColor(0xFF000000);
                black.setStrokeWidth(0);

                Paint ground = new Paint();
                ground.setColor(0xFF37BF28);
                ground.setStrokeWidth(0);

                canvas.drawColor(0xFFFFFFFF);

                canvas.save();
                //This moves the canvas down by half
                canvas.translate(0, height / 2);
                canvas.rotate((float) currentRot, width / 2, 0);
                canvas.drawRect(-width, 0, 2 * width, height, ground);
                canvas.restore();

                for(int i = 0;i<boxes.size();i++)
                {
                    Box currentBox = boxes.get(i);
                    canvas.save();
                    canvas.rotate((float) currentRot, (width / 2) + currentBox.xChange - currentBox.sizer, height / 2 + currentBox.yChange - currentBox.sizer);
                    canvas.drawRect((width / 2) - (boxSize / 2) + currentBox.xChange - currentBox.sizer, height / 2 - (boxSize / 2) + currentBox.yChange - currentBox.sizer, (width / 2) + (boxSize / 2) + currentBox.xChange + currentBox.sizer, (height / 2) + (boxSize / 2) + currentBox.yChange + currentBox.sizer, black);
                    canvas.restore();
                    currentBox.animate();
                }


                //MATH converts the dgree of tilt of horizon to slope
                //This converts from degreees to radians
                boxAngle = (currentRot/180)*Math.PI;
                boxAngle = Math.tan(boxAngle);

                //******************************************* DEBUG TEXT***************************************
                canvas.drawText(""+boxes.get(0).yChange,30,30,black);
                canvas.drawText(""+height/2,30,50,black);
                canvas.drawText(""+boxes.get(0).multiplier,30,70,black);
                canvas.drawText("Deriv: "+derivitive,30,90,black);
                canvas.drawText("Bangle"+boxAngle,30,110,black);





                //********************************************* COLLISIONS ****************************************
//                if(Fred.yChange+height/2>planeHeight&&Fred.xChange+(width/2)>width / 2 - (planeWidth / 2)&&Fred.yChange+height/2<planeHeight+(planeWidth/2)&&Fred.xChange+(width/2)<width / 2 + planeWidth)
//                {
//                    vib.vibrate(30);
//                }

                canvas.drawBitmap(Plane, width / 2 - (planeWidth / 2), planeHeight, white);

                //This is the rate that the view is re-drawn
                postInvalidateDelayed(1);
            }
        };

        setContentView(v);

        //End of onCreate
    }



    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {

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



//********************************* MAIN ANIMATION CLASS ********************************************************
    public class Box
    {
        float xChange = 0;
        float yChange = 0;
        float sizer = 0;
        float width = 0;
        float height = 0;
        float boxSize = 0;
        float multiplier = 1;
        int planeHeight = 0;
        int planeWidth = 0;
        int initialY = 0;
        float offset = 0;

        public Box(int width,int height,int planeHeight,int planeWidth, int boxSize)
        {
            this.height = height;
            this.boxSize = boxSize;
            this.width = width;
            xChange = randStart();
            this.planeHeight = planeHeight;
            this.planeWidth = planeWidth;
            yChange = 0;
            sizer = 0;

        }

        public int randStart()
        {
            if(Math.random()>=0.5)
            {
                return (int)(Math.random()*((this.width/2)-(boxSize/2)+1));
            }
            else
            {
                return -(int)(Math.random()*((this.width/2)-(boxSize/2)+1));
            }
        }

        public void animate()
        {
            xChange = xChange + (float)(boxAngle*multiplier);
            derivitive = yChange-yChange + (1*this.multiplier);
            yChange = yChange + (1*this.multiplier);

            this.multiplier = (1+(Math.abs(Math.abs(initialY)-yChange)/(height/2))*Mmultiplier);

            //This sets the speed for all blocks
            //SET SPEED****************************************
            sizer = sizer + (float)0.05;

            if(xChange<-(width/2)||xChange>(width/2)||yChange>(height/2))
            {
                xChange = randStart();
                //xChange = -40;
                yChange = (float)((boxAngle*xChange));
                initialY = (int)yChange+1;
                sizer = 0;
            }
            
            if(yChange+height/2>planeHeight&&xChange+(width/2)>width / 2 - (planeWidth / 2)&&yChange+height/2<planeHeight+(planeWidth/2)&&xChange+(width/2)<width / 2 + planeWidth)
            {
               //ADD VIBRATion
            }
        }
    }
}





//TRYING TO ROTATE RECTANGLE
//canvas.save();
//canvas.rotate((int)currentRot,(int)offsetO+(boxSize/2),(int)offsetT+(boxSize/2));


//THIS IS THE ORIGIONALLLL **************************************************
//canvas.drawRect(((width / 2) - (boxSize / 2)) + (float) offsetO - (float) sizer, height / 2 - (boxSize / 2) + (float) offsetT - (float) sizer, (width / 2) + (boxSize / 2) + (float) offsetH + (float) sizer, (height / 2) + (boxSize / 2) + (float) offsetF + (float) sizer, black);
// THIS IS WITH BOX CLASS
//canvas.drawRect(((width / 2) - (boxSize / 2)) + (float)Fred.animate().get(0) - (float)Fred.animate().get(2),height / 2 - (boxSize / 2) + (float)Fred.animate().get(1) - (float)Fred.animate().get(2),(width / 2) + (boxSize / 2) + (float)Fred.animate().get(0) + (float)Fred.animate().get(2),(height / 2) + (boxSize / 2) + (float)Fred.animate().get(1) + (float)Fred.animate().get(2),black);
//Fred.animate();
//canvas.drawRect((width/2) - (boxSize/2) + Fred.xChange - Fred.sizer,height/2 - (boxSize/2) + Fred.yChange - Fred.sizer, (width/2) + (boxSize/2) +Fred.xChange + Fred.sizer, (height/2) + (boxSize/2) + Fred.yChange+Fred.sizer, black);
//canvas.restore();

