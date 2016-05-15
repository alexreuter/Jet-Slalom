package com.example.alex_reuter.accelerometerdata;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//Need to change speed according to screen density
//DISABLE AUTOWIN
//POSITION CONSTANTALLY FUNCTION OF SCREEN ANGLE.
//Make new variables for the position



public class MainActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton =  (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ActualGame.class);
                startActivity(intent);
            }
        });

        Button store = (Button)findViewById(R.id.store);
        store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText)findViewById(R.id.editText);
                int foobar = Integer.parseInt(editText.getText().toString());

                SharedPreferences sharedpreferences;
                sharedpreferences = getSharedPreferences("bob", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                //int foobar = Integer.parseInt(editText.getText().toString());
               // Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG).show();

                editor.putInt("score", foobar);
                editor.commit();

                Toast.makeText(getApplicationContext(), "score has been stored", Toast.LENGTH_SHORT).show();
                editText.setText("");
            }
        });

        SharedPreferences prefs = getSharedPreferences("bob", MODE_PRIVATE);
        String disp = String.valueOf(prefs.getInt("score", 0));

        TextView bob = (TextView)findViewById(R.id.textView);
        bob.setText("High Score: " + disp);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        SharedPreferences prefs = getSharedPreferences("bob", MODE_PRIVATE);
        String disp = String.valueOf(prefs.getInt("score", 0));

        TextView bob = (TextView)findViewById(R.id.textView);
        bob.setText("High Score: " + disp);
    }
}