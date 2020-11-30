package com.example.hangboardtimer;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

public class MainActivity extends AppCompatActivity {

    int seconds_hang;
    int seconds_rest;
    int n_iterations;
    int minutes_pause;

    boolean running;

    private NumberPicker pause_picker;
    private NumberPicker rest_picker;
    private NumberPicker hang_picker;
    private NumberPicker iter_picker;
    private Button st_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        running = false;

        pause_picker = findViewById(R.id.pause_picker);
        rest_picker = findViewById(R.id.rest_picker);
        hang_picker = findViewById(R.id.hang_picker);
        iter_picker = findViewById(R.id.iterations_picker);
        st_button = findViewById(R.id.st_button);

        pause_picker.setMinValue(1);
        pause_picker.setMaxValue(10);

        hang_picker.setMinValue(1);
        hang_picker.setMaxValue(45);

        rest_picker.setMinValue(1);
        rest_picker.setMaxValue(45);

        iter_picker.setMinValue(1);
        iter_picker.setMaxValue(10);

        st_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (running) {
                    running = false;
                    st_button.setText("Start");
                } else {
                    running = true;
                    st_button.setText("Stop");
                }

            }
        });

    }
}