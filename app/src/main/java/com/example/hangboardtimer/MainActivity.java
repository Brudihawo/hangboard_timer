package com.example.hangboardtimer;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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

    static final String STATE_HANG = "state_hang";
    static final String STATE_REST = "state_rest";
    static final String STATE_PAUSE = "state_pause";
    static final String STATE_ITER = "state_iter";

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

        if (savedInstanceState != null) {
            seconds_hang = savedInstanceState.getInt(STATE_HANG);
            seconds_rest = savedInstanceState.getInt(STATE_REST);
            minutes_pause = savedInstanceState.getInt(STATE_PAUSE);
            n_iterations = savedInstanceState.getInt(STATE_ITER);
            Log.d("onRestoreInstanceState", "restored state");
        } else {
            SharedPreferences settings = getSharedPreferences("preferences", Context.MODE_PRIVATE);
            seconds_hang = settings.getInt(STATE_HANG,7);
            seconds_rest = settings.getInt(STATE_REST, 5);
            minutes_pause = settings.getInt(STATE_PAUSE, 1);
            n_iterations = settings.getInt(STATE_ITER, 6);
            Log.d("onCreate", "used settings");
        }

        running = false;

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Context context = getApplicationContext();

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


        set_pickers();

        pickers = new NumberPicker[] {hang_picker, iter_picker, rest_picker, pause_picker};

        duration_preview.setText(get_set_duration_string());

        NumberPicker.OnScrollListener scroll_listener = (view, scrollState) -> {
            set_training_vars();
            duration_preview.setText(get_set_duration_string());
        };

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
                set_pickers();
                current_time_left.setText("");
                current_task.setText("");
                progress_bar.setProgress(0, true);
            } else {
                if ((seconds_hang > 0) || (seconds_rest > 0) || (minutes_pause > 0)) {
                    st_button.setText(getText(R.string.stop));
                    set_training_vars();
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

    @Override
    protected void onSaveInstanceState(Bundle save_state) {
        save_state.putInt(STATE_HANG, seconds_hang);
        save_state.putInt(STATE_REST, seconds_rest);
        save_state.putInt(STATE_PAUSE, minutes_pause);
        save_state.putInt(STATE_ITER, n_iterations);

        super.onSaveInstanceState(save_state);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences settings = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = settings.edit();
        edit.putInt(STATE_HANG, seconds_hang);
        edit.putInt(STATE_REST, seconds_rest);
        edit.putInt(STATE_PAUSE, minutes_pause);
        edit.putInt(STATE_ITER, n_iterations);
        edit.apply();

    }

    public String get_set_duration_string() {
        int total_seconds = (seconds_hang + seconds_rest) * n_iterations + minutes_pause * 60;
        return getApplicationContext().getString(R.string.total_set_time, total_seconds / 60, total_seconds % 60);
    }

    public void set_training_vars() {
        seconds_hang = hang_picker.getValue();
        seconds_rest = rest_picker.getValue();
        minutes_pause = pause_picker.getValue();
        n_iterations = iter_picker.getValue();
    }

    public void set_pickers() {
        hang_picker.setValue(seconds_hang);
        rest_picker.setValue(seconds_rest);
        pause_picker.setValue(minutes_pause);
        iter_picker.setValue(n_iterations);
    }
}