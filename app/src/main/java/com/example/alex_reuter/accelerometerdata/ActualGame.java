package com.example.alex_reuter.accelerometerdata;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Random;


//Fix strange upside down plane flip.
//Need to change speed according to screen density
//DISABLE AUTOWIN
//MAKE POSITION CONSTANTALLY A COMPONANT OF THE SCREEN ANGLE.
//POSITION CONSTANTALLY FUNCTION OF SCREEN ANGLE.
//Make new variables for the position
//Boxangle change
//Fix collisions to make score work



public class ActualGame extends Activity implements SensorEventListener
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
    public ArrayList <Float> noLine = new ArrayList<Float>();
    public float derivitive = 0;
    public static int score = 0;
    public static int highScore = 0;

    //This is just used for the vibration code.
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_actual_game);

        //************************************ TOAST STUFF **************************************************
        SharedPreferences prefs = getSharedPreferences("bob", MODE_PRIVATE);
        highScore = prefs.getInt("score", 0);

        //THIS GETS RID OF THE VERY STRANGE CRASHING STRING RESOURCE ERROR
        String scoreS = String.valueOf(highScore);
        Toast.makeText(getApplicationContext(), "High Score: " + scoreS, Toast.LENGTH_LONG).show();


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
            boxes.add(new Box(width,height,planeHeight,planeWidth,boxSize,i));
        }

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
                //Color of the sky
                canvas.drawColor(0xFFFFFFFF);

                canvas.save();
                //This moves the canvas down by half/
                //After this is done 0,0 is still at the top left hand corner
                //It is just that the way rectangles are drawn compared to the box clas given values are from the center.
                canvas.translate(0, height / 2);
                //This is rotating the canvas round the center point
                canvas.rotate((float) currentRot, width / 2, 0);
                canvas.drawRect(-width, 0, 2 * width, height, ground);
                canvas.restore();

                for(int i = 0;i<boxes.size();i++)
                {
                    Box currentBox = boxes.get(i);
                    canvas.save();
                    canvas.rotate((float) currentRot, (width / 2) + currentBox.xChange - currentBox.sizer, height / 2 + currentBox.yChange - currentBox.sizer);
                    //OLD AND WORKING
                    //canvas.drawRect((width / 2) - (boxSize / 2) + currentBox.xChange - currentBox.sizer, height / 2 - (boxSize / 2) + currentBox.yChange - currentBox.sizer, (width / 2) + (boxSize / 2) + currentBox.xChange + currentBox.sizer, (height / 2) + (boxSize / 2) + currentBox.yChange + currentBox.sizer, black);
                    canvas.drawRect((width / 2) - (boxSize / 2) + currentBox.xChange - currentBox.sizer, (float)((height/2) + currentBox.initialX*boxAngle - (boxSize / 2) + currentBox.yChange - currentBox.sizer), (width / 2) + (boxSize / 2) + currentBox.xChange + currentBox.sizer, (float)((height/2) + currentBox.initialX*boxAngle + (boxSize / 2) + currentBox.yChange + currentBox.sizer), black);
                    canvas.restore();
                    currentBox.animate();
                }


                //MATH converts the dgree of tilt of horizon to slope
                //This converts from degreees to radians
                boxAngle = (currentRot/180)*Math.PI;
                boxAngle = Math.tan(boxAngle);

                //******************************************* DEBUG TEXT***************************************
                canvas.drawText("Multiplier: "+boxes.get(0).multiplier,30,30,black);
                canvas.drawText("initY" + boxes.get(0).initialY,30,40,black);
                canvas.drawText("yChange"+boxes.get(0).yChange,30,50,black);
                canvas.drawText("Bangle*xChange"+boxes.get(0).multiplier*boxes.get(0).xChange,30,70,black);
                canvas.drawText("Deriv: "+derivitive,30,90,black);
                canvas.drawText("Bangle"+boxAngle,30,110,black);
                canvas.drawText("Score: "+score,30,130,black);
                canvas.drawText("HScore: "+highScore,30,150,black);

                canvas.drawBitmap(Plane, width / 2 - (planeWidth / 2), planeHeight, white);

                //This is the rate that the view is re-drawn
                postInvalidateDelayed(1);
            }
        };
        setContentView(v);
        //End of onCreate
    }

    //Saves the score in shared prefs
    public void onPause()
    {
        super.onPause();
        if(score>highScore) {
            SharedPreferences sharedpreferences;
            sharedpreferences = getSharedPreferences("bob", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            //int foobar = Integer.parseInt(editText.getText().toString());
            // Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG).show();

            editor.putInt("score", score);
            editor.commit();
        }
        score = 0;
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
    public void onAccuracyChanged(Sensor sensor, int accuracy){}



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
        int initialX = 0;
        float offset = 0;
        int id = 0;
        float testX = 0;
        float testY = 0;


        public Box(int width,int height,int planeHeight,int planeWidth, int boxSize, int id)
        {
            this.height = height;
            this.boxSize = boxSize;
            this.width = width;
            xChange = randStart();
            initialX = (int)xChange;
            yChange = (float)((boxAngle*xChange));

            this.planeHeight = planeHeight;
            this.planeWidth = planeWidth;
            yChange = 0;
            sizer = 0;
            this.id = id;

            //THIS IS USED TO SPREAD OUT THE RANDOM SPREADING OF THE BOXES
            noLine.add(0,xChange);
        }

        public int randStart() {
            int returner;
            if (Math.random() >= 0.5) {
                returner =(int) (Math.random() * ((this.width / 2) - (boxSize / 2) + 1));
            } else {
                returner =-(int) (Math.random() * ((this.width / 2) - (boxSize / 2) + 1));
            }

            for(int i=0;i<noLine.size();i++)
            {
                if(noLine.get(i)+30<xChange&&noLine.get(i)-30>xChange)
                {
                    if(Math.random()<0.5)
                    {
                        returner = returner + 30;
                    }
                    else
                    {
                        returner = returner - 30;
                    }
                }
            }
            return returner;
        }

        public Color randColor()
        {
            Random rand = new Random();
            int r = rand.nextInt(255);
            int g = rand.nextInt(255);
            int b = rand.nextInt(255);
            Color bob = new Color();
            bob.rgb(r, g, b);
            return bob;
        }

        public void animate()
        {
            xChange = 40;
            yChange = (float)boxAngle*xChange;
//            xChange = xChange + (float)(boxAngle*multiplier);
//            derivitive = yChange-yChange + (1*this.multiplier);
//            //yChange = yChange + (1*this.multiplier);
//            yChange = (float)(boxAngle*xChange)+(1*this.multiplier);

//            xChange = xChange + (float)(boxAngle*multiplier);
//            yChange = (float)(boxAngle*xChange)+(1*this.multiplier);
//            this.multiplier = (1+(Math.abs(Math.abs(initialY)-yChange)/(height/2))*Mmultiplier);
//            xChange = xChange + (float)(boxAngle*multiplier);
//            yChange = 1*this.multiplier;

            //THIS SETUP IS THE OLD DUMB VERSION, BUG FIX IS IN THE DRAWING ITSELF
            //THIS.MULTIPLIER WORKS ONLY FOR POSITIVE VALUES!!!!!!!

//            xChange = xChange + (float)(boxAngle);
//            yChange = yChange + (1*this.multiplier);
//            this.multiplier = 5*Math.abs(initialY-yChange)/((height/2)+Math.abs(initialY));

            //xChange = xChange + (float)(boxAngle*multiplier);
            //testY = testY + 1;
            //yChange = (float)(boxAngle*xChange) + testY;

            //multiplier = (1+(Math.abs(Math.abs(initialY)-yChange)/(height/2))*Mmultiplier);

            //This sets the speed for all blocks
            //SET SPEED****************************************
            sizer = sizer + (float)0.05;

            //OUT OF BOUNDS
            if(xChange<-(width/2)||xChange>(width/2)||yChange>(height/2))
            {
                xChange = randStart();
                initialX = (int)xChange;
                yChange = (float)((boxAngle*xChange));
                initialY = (int)yChange;
                sizer = 0;
                score = score + 1;
            }

            if(yChange+height/2>planeHeight&&xChange+(width/2)>width / 2 - (planeWidth / 2)&&yChange+height/2<planeHeight+(planeWidth/2)&&xChange+(width/2)<width / 2 + planeWidth)
            {
                //ADD VIBRATION

                //score = 0;
                //Toast.makeText(getApplicationContext(), "HIIT", Toast.LENGTH_SHORT).show();
            }
        }
    }
}