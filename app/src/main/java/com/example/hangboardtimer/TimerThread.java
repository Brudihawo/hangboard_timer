package com.example.hangboardtimer;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

public class TimerThread extends Thread {
    int state; // 0 = Rest, 1 = Hang, 2 = Pause
    int seconds_hang, seconds_rest, minutes_pause, n_iterations;
    NumberPicker hang_picker, rest_picker, pause_picker, iter_picker;
    ProgressBar progress_bar;
    TextView text_display, time_left;
    int update_frequency;

    TimerThread(NumberPicker hang_picker, NumberPicker rest_picker,
                NumberPicker pause_picker, NumberPicker iter_picker,
                ProgressBar progress_bar, TextView text_display, TextView time_left) {
        this.seconds_hang = hang_picker.getValue();
        this.seconds_rest = rest_picker.getValue();
        this.minutes_pause = pause_picker.getValue();
        this.n_iterations = iter_picker.getValue();
        this.hang_picker = hang_picker;
        this.rest_picker = rest_picker;
        this.pause_picker = pause_picker;
        this.iter_picker = iter_picker;
        this.progress_bar = progress_bar;
        this.text_display = text_display;
        this.time_left = time_left;
        this.state = 0; // Initialise exercise on rest
        this.update_frequency = 100;
        progress_bar.setProgress(0);
    }

    @Override
    public void run() {
        float max = progress_bar.getMax();
        while (true) {
            // FIXME: IllegalMonitorStateException caused by timer.wait(long milliseconds)
            switch (state) {
                case 0: // Rest
                    Log.d("TimerThread", "Switching to State 0");
                    text_display.post(()-> text_display.setText("REST"));
                    for (int i = seconds_rest * update_frequency; i > 1; i--) {
                        if (this.isInterrupted()) {
                            before_return();
                            return;
                        } else {
                            try {
                                Thread.sleep(1000 / update_frequency);
                            } catch (InterruptedException e) {
                                before_return();
                                return;
                            }
                            if (i % update_frequency == 0) {
                                rest_picker.setValue(rest_picker.getValue() - 1);
                                final String str = "" + (int) ( i / update_frequency) + " s";
                                time_left.post(() -> time_left.setText(str));
                            }
                            progress_bar.setProgress((int) (max - max * i / (seconds_rest * update_frequency)), true);
                        }
                    }
                    progress_bar.setProgress(0);
                    rest_picker.setValue(seconds_rest);
                    state = 1;
                    break;

                case 1: // Hang
                    Log.d("TimerThread", "Switching to State 1");
                    text_display.post(()->text_display.setText("HANG"));
                    for (int i = seconds_hang * update_frequency; i > 1; i--) {
                        if (this.isInterrupted()) {
                            before_return();
                            return;
                        } else {
                            try {
                                Thread.sleep( 1000 / update_frequency);
                            } catch (InterruptedException e) {
                                before_return();
                                return;
                            }
                            if (i % update_frequency == 0) {
                                hang_picker.setValue(hang_picker.getValue() - 1);
                                final String str = "" + (int) ( i / update_frequency) + " s";
                                time_left.post(() -> time_left.setText(str));
                            }
                            progress_bar.setProgress((int)(max - i * max / (float)(seconds_hang * update_frequency)), true);
                        }
                    }
                    hang_picker.setValue(seconds_hang);
                    progress_bar.setProgress(0);
                    if (iter_picker.getValue() - 1 > 0) {
                        Log.d("hang", "Iter picker Value = " + iter_picker.getValue());
                        iter_picker.setValue(iter_picker.getValue() - 1);
                        state = 0;
                    } else {
                        iter_picker.setValue(n_iterations);
                        state = 2;
                    }
                    break;

                case 2: // Pause
                    Log.d("TimerThread", "Switching to State 2");
                    text_display.post(()->text_display.setText("PAUSE"));
                    for (int i = minutes_pause * 60 * update_frequency; i > 1; i--) {
                        if (this.isInterrupted()) {
                            before_return();
                            return;
                        } else {
                            try {
                                Thread.sleep(1000 / update_frequency);
                            } catch (InterruptedException e) {
                                before_return();
                                return;
                            }
                            if (i % update_frequency == 0) {
                                final String str = "" +  (int)(1.0 * i / (update_frequency * 60)) + " min " + (int)(i % (update_frequency * 60) / update_frequency) + " s";
                                time_left.post(() -> time_left.setText(str));
                            }
                            progress_bar.setProgress((int)(max - max * i / (60.0 * minutes_pause * update_frequency)), true);
                        }
                    }
                    progress_bar.setProgress(0);
                    pause_picker.setValue(minutes_pause);
                    state = 0;
                    break;

                default:
                    throw new IllegalStateException("Yoo WTF, how did the state end up at: " + state);
            }
        }
    }

    private void before_return() {
        hang_picker.setValue(seconds_hang);
        rest_picker.setValue(seconds_rest);
        pause_picker.setValue(minutes_pause);
        iter_picker.setValue(n_iterations);
    }
}
