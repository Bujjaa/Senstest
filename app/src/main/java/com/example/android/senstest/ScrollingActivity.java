package com.example.android.senstest;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ScrollingActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);



    }

    public void buttonClicked (View view){
        Button b = (Button)view;
        b.getText().toString();
        Log.d("Scrolling clicked: ", b.getText().toString());
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra("DIENST", b.getText().toString());
        startActivity(intent);

    }
    int buttonName ;
    @Override
    public void onClick(View view) {

    }
    @Override
    public void onBackPressed() {
        finish();
       }

}
