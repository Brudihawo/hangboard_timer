package com.example.hangboardtimer;

import android.util.Log;
import android.widget.NumberPicker;

import java.util.Timer;
import java.util.TimerTask;

public class TimerThread extends Thread {
    int state; // 0 = Rest, 1 = Hang, 2 = Pause
    int seconds_hang, seconds_rest, minutes_pause, n_iterations;
    NumberPicker hang_picker, rest_picker, pause_picker, iter_picker;
    int update_frequency;

    TimerThread(NumberPicker hang_picker, NumberPicker rest_picker,
                NumberPicker pause_picker, NumberPicker iter_picker) {
        this.seconds_hang = hang_picker.getValue();
        this.seconds_rest = rest_picker.getValue();
        this.minutes_pause = pause_picker.getValue();
        this.n_iterations = iter_picker.getValue();
        this.hang_picker = hang_picker;
        this.rest_picker = rest_picker;
        this.pause_picker = pause_picker;
        this.iter_picker = iter_picker;
        this.state = 0; // Initialise exercise on rest
        this.update_frequency = 100;
    }

    @Override
    public void run() {
        while (true) {
            // FIXME: IllegalMonitorStateException caused by timer.wait(long milliseconds)
            switch (state) {
                case 0: // Rest
                    Log.d("TimerThread", "Switching to State 0");
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
                            }
                            // TODO: Insert Progress Bar code here
                        }
                    }
                    rest_picker.setValue(seconds_rest);
                    state = 1;
                    break;

                case 1: // Hang
                    Log.d("TimerThread", "Switching to State 1");
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
                            if (i % 100 == 0) {
                                hang_picker.setValue(hang_picker.getValue() - 1);
                            }
                            // TODO: Insert Progress Bar code here
                        }
                    }
                    hang_picker.setValue(seconds_hang);
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

                            // TODO: Insert Progress Bar code here
                        }
                    }
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
