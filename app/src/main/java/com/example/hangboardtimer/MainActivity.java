package com.example.hangboardtimer;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.content.Context;

import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    int seconds_hang, seconds_rest, n_iterations, minutes_pause;
    boolean running;

    NumberPicker pause_picker, rest_picker, hang_picker, iter_picker;
    NumberPicker[] pickers;

    Button st_button;
    TimerThread timer_thread;

    TextView current_task, current_time_left, duration_preview;
    ProgressBar progress_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Context context = getApplicationContext();

        minutes_pause = 1;
        seconds_hang = 7;
        seconds_rest = 5;
        n_iterations = 6;

        running = false;

        st_button = findViewById(R.id.st_button);
        pause_picker = findViewById(R.id.pause_picker);
        rest_picker = findViewById(R.id.rest_picker);
        hang_picker = findViewById(R.id.hang_picker);
        iter_picker = findViewById(R.id.iterations_picker);
        current_task = findViewById(R.id.current_task);
        current_time_left = findViewById(R.id.current_time_left);
        duration_preview = findViewById(R.id.duration_text);

        progress_bar = findViewById(R.id.progress_bar);

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

        pickers = new NumberPicker[] {hang_picker, iter_picker, rest_picker, pause_picker};

        duration_preview.setText(get_total_duration(hang_picker, rest_picker, iter_picker, pause_picker));

        NumberPicker.OnScrollListener scroll_listener = (view, scrollState) -> duration_preview.setText(get_total_duration(hang_picker, rest_picker, iter_picker, pause_picker));
        for (NumberPicker picker : pickers) {
            picker.setOnScrollListener(scroll_listener);
        }

        st_button.setOnClickListener(v -> {
            if (running) {
                st_button.setText(getText(R.string.start));
                timer_thread.interrupt();
                running = false;
                for (NumberPicker p : pickers) {
                    p.setEnabled(true);
                }
                current_time_left.setText("");
                current_task.setText("");
                progress_bar.setProgress(0, true);
            } else {
                if ((hang_picker.getValue() > 0) || (rest_picker.getValue()> 0) || (pause_picker.getValue() > 0)) {
                    st_button.setText(getText(R.string.stop));
                    timer_thread = new TimerThread(hang_picker, rest_picker, pause_picker, iter_picker, progress_bar, current_task, current_time_left, context);
                    timer_thread.start();
                    running = true;
                    for (NumberPicker p : pickers) {
                        p.setEnabled(false);
                    }
                } else {
                    Snackbar snackbar = Snackbar.make(st_button, "Invalid Time Values", Snackbar.LENGTH_LONG);
                    snackbar.setAction("OK", v1 -> snackbar.dismiss());
                    snackbar.show();
                }
            }
        });


    }
    public String get_total_duration(NumberPicker hang_picker, NumberPicker rest_picker, NumberPicker iter_picker, NumberPicker pause_picker) {
        int total_seconds = (hang_picker.getValue() + rest_picker.getValue()) * iter_picker.getValue() + pause_picker.getValue() * 60;
        return getApplicationContext().getString(R.string.total_set_time, total_seconds / 60, total_seconds % 60);
    }
}