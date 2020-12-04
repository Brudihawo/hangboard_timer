package com.example.hangboardtimer;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Context;

import androidx.annotation.RequiresApi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TimerThread extends Thread {
    int state; // 0 = Rest, 1 = Hang, 2 = Pause
    int seconds_hang, seconds_rest, minutes_pause, n_iterations;
    NumberPicker hang_picker, rest_picker, pause_picker, iter_picker;
    ProgressBar progress_bar;
    TextView text_display, time_left;
    Context context;

    int update_frequency;
    protected MediaPlayer media_player;

    TimerThread(NumberPicker hang_picker, NumberPicker rest_picker,
                NumberPicker pause_picker, NumberPicker iter_picker,
                ProgressBar progress_bar, TextView text_display, TextView time_left,
                Context context) {
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
        this.context = context;
        this.state = 0; // Initialise exercise on rest
        this.update_frequency = 120; // in Hz
        progress_bar.setProgress(0);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void run() {
        float max;
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        while (true) {
            switch (state) {
                case 0: // Rest
                    max = seconds_rest * update_frequency;
                    progress_bar.setMax((int)max);
                    Log.d("TimerThread", "Switching to State 0");
                    text_display.post(()-> text_display.setText(R.string.rest_descr));
                    time_left.post(()-> time_left.setText(context.getString(R.string.time_left_rest_hang, (seconds_rest))));
                    vibrate_sound_change(vibrator);
                    for (int i = seconds_rest * update_frequency; i > 1; i--) {
                        if (this.isInterrupted()) {
                            return;
                        } else {
                            try {
                                Thread.sleep(1000 / update_frequency);
                            } catch (InterruptedException e) {
                                return;
                            }
                            if (i % update_frequency == 0) {
                                rest_picker.setValue(rest_picker.getValue() - 1);
                                //changeValueByOne(rest_picker, false);
                                int finalI = i;
                                time_left.post(()-> time_left.setText(context.getString(R.string.time_left_rest_hang, (finalI / update_frequency) - 1)));
                                if(i < update_frequency * seconds_rest && i > 2) {
                                    vibrate_sound_during(vibrator);
                                }
                            }
                            if (i % seconds_rest == 0) {
                                progress_bar.setProgress((int)(max - i), true);
                            }
                        }
                    }
                    progress_bar.setProgress(0);
                    rest_picker.setValue(seconds_rest);
                    state = 1;
                    break;

                case 1: // Hang
                    max = seconds_hang * update_frequency;
                    progress_bar.setMax((int)max);
                    Log.d("TimerThread", "Switching to State 1");
                    text_display.post(()->text_display.setText(R.string.hang_descr));
                    time_left.post(()-> time_left.setText(context.getString(R.string.time_left_rest_hang, (seconds_hang))));
                    vibrate_sound_change(vibrator);
                    for (int i = seconds_hang * update_frequency; i > 1; i--) {
                        if (this.isInterrupted()) {
                            return;
                        } else {
                            try {
                                Thread.sleep(1000 / update_frequency);
                            } catch (InterruptedException e) {
                                return;
                            }
                            if (i % update_frequency == 0) {
                                hang_picker.setValue(hang_picker.getValue() - 1);
                                //changeValueByOne(hang_picker, false);
                                int finalI = i;
                                time_left.post(() -> time_left.setText(context.getString(R.string.time_left_rest_hang, (finalI / update_frequency) - 1)));
                                if(i < update_frequency * seconds_hang && i > 2) {
                                    vibrate_sound_during(vibrator);
                                }
                            }
                            if (i % seconds_hang == 0) {
                                progress_bar.setProgress((int) (max - i), true);
                            }
                        }
                    }
                    hang_picker.setValue(seconds_hang);
                    progress_bar.setProgress(0);
                    if (iter_picker.getValue() - 1 > 0) {
                        iter_picker.setValue(iter_picker.getValue() - 1);
                        //changeValueByOne(iter_picker, false);
                        state = 0;
                    } else {
                        iter_picker.setValue(n_iterations);
                        state = 2;
                    }
                    break;

                case 2: // Pause
                    max = minutes_pause * 60 * update_frequency;
                    progress_bar.setMax((int)max);
                    Log.d("TimerThread", "Switching to State 2");
                    time_left.post(() -> time_left.setText(context.getString(R.string.time_left_pause, minutes_pause, 0)));
                    text_display.post(()->text_display.setText(R.string.pause_descr));
                    vibrate_sound_change(vibrator);
                    for (int i = minutes_pause * 60 * update_frequency; i > 1; i--) {
                        if (this.isInterrupted()) {
                            return;
                        } else {
                            try {
                                Thread.sleep(1000 / update_frequency);
                            } catch (InterruptedException e) {
                                return;
                            }
                            if (i % minutes_pause * 60 == 0) {
                                progress_bar.setProgress((int)(max - i), true);
                            }
                            if (i % update_frequency == 0) {
                                int finalI = i;
                                time_left.post(() -> time_left.setText(context.getString(R.string.time_left_pause, (int)(1.0 * finalI / (update_frequency * 60.0)), (finalI % (update_frequency * 60) / update_frequency))));
                            }
                            if (i % (update_frequency * 60) == 0) {
                                changeValueByOne(pause_picker, false);
                            }
                        }
                    }
                    progress_bar.setMax((int)max);
                    progress_bar.setProgress(0);
                    pause_picker.setValue(minutes_pause);
                    state = 0;
                    break;

                default:
                    throw new IllegalStateException("Yoo WTF, how did the state end up at: " + state);
            }
        }
    }

    void play_res(int resId) {
        if (media_player != null) {
            media_player.reset();
        }
        media_player = MediaPlayer.create(context, resId);
        media_player.start();
    }

    void vibrate_sound_change(Vibrator vibrator) {
        vibrator.vibrate(VibrationEffect.createWaveform(new long[]{75, 75, 75}, new int[]{255, 0, 255}, -1));
        play_res(R.raw.beep_high);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    void vibrate_sound_during(Vibrator vibrator) {
        vibrator.vibrate(VibrationEffect.createOneShot(100, 255));
        play_res(R.raw.beep_low);
    }

    void changeValueByOne(final NumberPicker np, final boolean increment) {
        Method method;
        try {
            // FIXME: IF BUGGY, LOOK AT THIS
            method = np.getClass().getDeclaredMethod("changeValueByOne", boolean.class);
            method.setAccessible(true);
            method.invoke(np, increment);
            method.setAccessible(false);
        } catch (final NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
