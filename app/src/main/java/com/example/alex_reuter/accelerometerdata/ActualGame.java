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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.hardware.SensorEventListener;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Random;

public class ActualGame extends Activity implements SensorEventListener
{

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private Bitmap Plane;
    //This is brought slowly towards the target rot as detirmined by smoothing
    public double currentRot = 0;
    //This is detirmined by the raw accelerometer numbers
    public double targetRot = 0;
    public double boxAngle = 0;
    //This is used to detirmine when to turn smoothing on and off
    public double delta = 0;
    //This detirmines when smoothing is turned off for rapid movements
    public double threshold = 50;
    public int defaultSmoothRate = 10;
    //This is changed depending on the rapidity of the movement to make the game more responsice
    public int smoothRate = 10;
    //Array that stores box objects
    public ArrayList <Box> boxes = new ArrayList<Box>();
    //Array that stores x positions to prevent blocks spawning in a line
    public ArrayList <Float> noLine = new ArrayList<Float>();

    //Starts at one so the modulo if statement later on doesn't spawn infinite boxes
    public static int score = 1;
    public static int highScore = 0;

    //Used to speed the game up as it goes on
    float gameSpeed = (float)0.5;

    //This is just used for the vibration code.
    Context context = this;

    //This method gets the raw accelerometer values and converts it to an angle in degrees.
    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {

            //This changes where the plane will ultimately turn to.
            targetRot = Math.toDegrees(Math.atan2(sensorEvent.values[0],sensorEvent.values[1]));

            //This makes the maximum angle on both sides 45 degrees
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actual_game);

        //This gets the stores high score from memory
        SharedPreferences prefs = getSharedPreferences("bob", MODE_PRIVATE);
        highScore = prefs.getInt("score", 0);

        //Get x and y of screen for the relative widths
        Display displayX = getWindowManager().getDefaultDisplay();
        final int width = displayX.getWidth();
        final int height = displayX.getHeight();

        //Screen Density
        //These values are used to detirmine an arbitrary universal unit compared to the screen density and size that makes the game
        //run at the same speed across different devices
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        final float xDpi = dm.xdpi;
        float yDpi = dm.ydpi;

        final float pixel = height/yDpi;

        //Sizes of stuff that are dependant on the screen size
        final int planeWidth = width/8;
        final int planeHeight = height-(planeWidth+(height/8));
        final int boxSize = (int)xDpi/20;

        //Vibration at the loading of the game scene
        final Vibrator vib = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(300);

        //Loads the plane icon
        Plane = BitmapFactory.decodeResource(getResources(), R.drawable.plane_outline);
        //Resizes the plane icon
        Plane = Bitmap.createScaledBitmap(Plane, planeWidth, planeWidth, false);


        //This is just to get to the accelerometer data
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);


        //This creates the initial two blocks
        boxes.add(new Box(width,height,planeHeight,planeWidth,boxSize,pixel));
        boxes.add(new Box(width,height,planeHeight,planeWidth,boxSize,pixel));

        View v = new View(this)
        {
            @Override
            protected void onDraw(Canvas canvas) {

                //USED FOR DISPLAY SMOOTHING
                //How rapid the movement was
                delta = (targetRot-currentRot);

                if(Math.abs(delta)>threshold)
                {
                    //If it was rapid, decrease the smoothrate so the transition happens faster
                    smoothRate = 2;
                    currentRot = currentRot + (delta/smoothRate);
                    smoothRate = defaultSmoothRate;
                }
                else
                {
                    currentRot = currentRot + (delta/smoothRate);
                }

                //Set background of bitmap
                //Color of the sky
                canvas.drawColor(0xFFFFFFFF);

                //PAINT DECLARATION
                Paint white = new Paint();
                white.setColor(0xFFFFFFFF);
                white.setStrokeWidth(0);

                Paint black = new Paint();
                black.setColor(0xFF000000);
                black.setStrokeWidth(0);
                black.setTextSize(pixel*5);

                Paint ground = new Paint();
                ground.setColor(0xFF37BF28);
                ground.setStrokeWidth(0);


                //This paint is used to set the color of the current box
                Paint current = new Paint();
                current.setColor(0xFF000000);
                current.setStrokeWidth(0);

                //Every 20 points a new block is added and the game gets a little faster
                if((score%10) == 0)
                {
                    boxes.add(new Box(width, height, planeHeight, planeWidth, boxSize, pixel));
                    gameSpeed = gameSpeed +(float)0.25;
                    score = score + 1;
                }

                canvas.save();
                //This moves the canvas down by half
                canvas.translate(0, height / 2);
                //This is rotating the canvas round the center point to rotate the grouns
                canvas.rotate((float) currentRot, width / 2, 0);
                canvas.drawRect(-width, 0, 2 * width, height, ground);
                canvas.restore();



                //Loops through all the boxes
                for(int i = 0;i<boxes.size();i++)
                {
                    Box currentBox = boxes.get(i);
                    canvas.save();
                    //Rotates around middle of box so aligns with ground
                    canvas.rotate((float) currentRot, (width / 2) + (boxSize / 2) + currentBox.xChange - currentBox.sizer, ((height / 2) + (boxSize / 2) + (float)boxAngle*currentBox.xChange+currentBox.yChange - currentBox.sizer));
                    current.setARGB(255, currentBox.Color.get(0), currentBox.Color.get(1), currentBox.Color.get(2));
                    canvas.drawRect((width / 2) - (boxSize / 2) + currentBox.xChange - currentBox.sizer, height / 2 - (boxSize / 2) + (float) boxAngle * currentBox.xChange + currentBox.yChange - currentBox.sizer, (width / 2) + (boxSize / 2) + currentBox.xChange + currentBox.sizer, (height / 2) + (boxSize / 2) +(float)boxAngle*currentBox.xChange + currentBox.yChange + currentBox.sizer, current);
                    canvas.restore();
                    //Calculates new x and y
                    currentBox.animate();
                }

                //This converts from degreees to radians
                boxAngle = (currentRot/180)*Math.PI;
                //This converts the degree of tilt of horizon to slope
                boxAngle = Math.tan(boxAngle);

                //Displaying scores
                canvas.drawText("Score: "+score,10,90,black);
                canvas.drawText("High Score: "+highScore,10,60,black);

                //This draws the plane icon
                canvas.drawBitmap(Plane, width / 2 - (planeWidth / 2), planeHeight, white);

                //This is the rate that the view is re-drawn
                postInvalidateDelayed(10);
            }
        };
        setContentView(v);
        //End of onCreate
    }

    //Saves the score in shared prefs
    public void onPause()
    {
        super.onPause();
        //Only stores the score if it is greater than the high score
        if(score>highScore) {
            SharedPreferences sharedpreferences;
            sharedpreferences = getSharedPreferences("bob", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putInt("score", score);
            editor.commit();
        }
        score = 1;
    }




    //Just ignore this, needs to be overidden to implement SensorData
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){}



//********************************* MAIN ANIMATION CLASS ********************************************************

    public class Box
    {
        //All of these just initialising
        float xChange = 0;
        float yChange = 0;
        float sizer = 0;
        float width = 0;
        float height = 0;
        float boxSize = 0;
        int planeHeight = 0;
        int planeWidth = 0;
        int initialY = 0;
        float ySpeed = 0;
        float pixel = 0;
        float ySpeedChange = 0;
        //This is the array that stores the rgb values for the color of teh box
        ArrayList <Integer> Color = new ArrayList<Integer>();

        public Box(int width,int height,int planeHeight,int planeWidth, int boxSize, float pixel)
        {
            //This is part of the workaround in getting variables from the onCreate activity
            this.height = height;
            this.boxSize = boxSize;
            this.width = width;
            this.pixel = pixel;
            this.planeHeight = planeHeight;
            this.planeWidth = planeWidth;

            xChange = randStart();
            yChange = (float)boxAngle*xChange;

            //Starts out normal sized
            sizer = 0;

            //This creates an initial random color
            randColor();

            //This adds the position to the array that prevents overlap
            noLine.add(noLine.size(),xChange);
        }

        public int randStart() {
            int returner;
            //This is how big a gap the program should leave
            int threshold = 30;

            //Picks randomly if it will be on the left or right
            if (Math.random() >= 0.5) {
                returner =(int) (Math.random() * ((this.width / 2) - (boxSize / 2) + 1));
            } else {
                returner =-(int) (Math.random() * ((this.width / 2) - (boxSize / 2) + 1));
            }

            //Loops through an array of xPositions of other blocks to check it won't be too close
            for(int i=0;i<noLine.size();i++)
            {
                if(noLine.get(i)+threshold<xChange&&noLine.get(i)-threshold>xChange)
                {
                    //If its too close, try again
                    randStart();
                }
            }
            return returner;
        }

        //This changes the values in a color array which is used in the actual drawing
        //It is an array because decalring a paint won't take a java color, only r,g,b values
        public void randColor()
        {
            Color.clear();
            Random rand = new Random();
            int r = rand.nextInt(256);
            int g = rand.nextInt(256);
            int b = rand.nextInt(256);
            Color.add(0,r);
            Color.add(0,g);
            Color.add(0,b);
        }

        public void animate()
        {
            //The xChange is the run of the slope of the line over one
            //So if it is 45 degrees, xChange is 0.5
            //The pixel changes the speed according to screen density
            xChange = xChange + ((float)(boxAngle*pixel)*gameSpeed);
            yChange = yChange+(pixel)*gameSpeed*ySpeedChange;

            //This makes the box accelerate as it goes down, making it seem to have perspective.
            ySpeedChange = (float)(Math.abs(initialY-yChange)/((height/2)+Math.abs(initialY))+0.5);

            //This is how much the box grows per cycle
            sizer = sizer + (pixel/15*gameSpeed);

            //Out of bounds if statement
            if(xChange<-(width/2)||xChange>(width/2)||yChange>(height/2))
            {
                //Re-generates a new position and increases score for successfully avoiding a block
                xChange = randStart();
                //This puts the new position into the anti-overlap array
                noLine.remove(0);
                noLine.add(noLine.size(),xChange);

                //Resets variables to 0
                yChange = 0;
                initialY = (int)yChange;
                sizer = 0;
                ySpeed = 0;
                score = score + 1;

                //Assigns a new color
                randColor();
            }

            //This checks if the box hits the plane
            if(yChange+height/2>planeHeight&&xChange+(width/2)>width / 2 - (planeWidth / 2)&&yChange+height/2<planeHeight+(planeWidth/2)&&xChange+(width/2)<width / 2 + planeWidth)
            {
                score = 1;

                //This makes the box the same color as the ground
                Color.set(0,55);
                Color.set(1,191);
                Color.set(2,40);

                //Resets to the initial amount of boxes
                boxes.clear();
                boxes.add(new Box((int) width, (int)height, planeHeight, planeWidth, (int) boxSize, pixel));
                boxes.add(new Box((int)width,(int)height,planeHeight,planeWidth,(int)boxSize,pixel));

                //Makes the game its initial speed
                gameSpeed = (float)0.5;
            }
        }
    }
}