package com.example.hangboardtimer;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.NumberPicker;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    int seconds_hang, seconds_rest, n_iterations, minutes_pause;
    boolean running;

    private NumberPicker pause_picker, rest_picker, hang_picker, iter_picker;

    private Button st_button;
    private TimerThread timer_thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        minutes_pause = 1;
        seconds_hang = 7;
        seconds_rest = 5;
        n_iterations = 3;

        running = false;

        pause_picker = findViewById(R.id.pause_picker);
        rest_picker = findViewById(R.id.rest_picker);
        hang_picker = findViewById(R.id.hang_picker);
        iter_picker = findViewById(R.id.iterations_picker);
        st_button = findViewById(R.id.st_button);

        pause_picker.setMinValue(0);
        pause_picker.setMaxValue(10);

        hang_picker.setMinValue(0);
        hang_picker.setMaxValue(45);

        rest_picker.setMinValue(0);
        rest_picker.setMaxValue(45);

        iter_picker.setMinValue(0);
        iter_picker.setMaxValue(10);

        hang_picker.setValue(seconds_hang);
        rest_picker.setValue(seconds_rest);
        pause_picker.setValue(minutes_pause);
        iter_picker.setValue(n_iterations);

        st_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (running) {
                    st_button.setText("Start");
                    timer_thread.interrupt();
                    running = false;
                } else {
                    st_button.setText("Stop");
                    timer_thread = new TimerThread(hang_picker, rest_picker, pause_picker, iter_picker);
                    timer_thread.start();
                    running = true;
                }
            }
        });
    }
}