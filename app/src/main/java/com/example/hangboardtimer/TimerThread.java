package com.example.hangboardtimer;

import android.media.MediaPlayer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Context;

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
        this.update_frequency = 100;
        progress_bar.setProgress(0);
    }

    @Override
    public void run() {
        float max = progress_bar.getMax();
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        while (true) {
            switch (state) {
                case 0: // Rest
                    Log.d("TimerThread", "Switching to State 0");
                    text_display.post(()-> text_display.setText(R.string.rest_descr));
                    time_left.post(()-> time_left.setText(context.getString(R.string.time_left_rest_hang, (seconds_rest))));
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
                                int finalI = i;
                                time_left.post(()-> time_left.setText(context.getString(R.string.time_left_rest_hang, (finalI / update_frequency) - 1)));
                                if (i < update_frequency * 10) {
                                    vibrator.vibrate(VibrationEffect.createOneShot(100, 128));
                                    play_res(R.raw.beep_low);
                                }
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
                    text_display.post(()->text_display.setText(R.string.hang_descr));
                    time_left.post(()-> time_left.setText(context.getString(R.string.time_left_rest_hang, (seconds_hang))));
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
                                int finalI = i;
                                time_left.post(()-> time_left.setText(context.getString(R.string.time_left_rest_hang, (finalI / update_frequency) - 1)));
                                if (i < update_frequency * 10) {
                                    vibrator.vibrate(VibrationEffect.createOneShot(100, 128));
                                    play_res(R.raw.beep_high);
                                }
                            }
                            progress_bar.setProgress((int)(max - i * max / (float)(seconds_hang * update_frequency)), true);
                        }
                    }
                    hang_picker.setValue(seconds_hang);
                    progress_bar.setProgress(0);
                    if (iter_picker.getValue() - 1 > 0) {
                        iter_picker.setValue(iter_picker.getValue() - 1);
                        state = 0;
                    } else {
                        iter_picker.setValue(n_iterations);
                        state = 2;
                    }
                    break;

                case 2: // Pause
                    Log.d("TimerThread", "Switching to State 2");
                    time_left.post(() -> time_left.setText(context.getString(R.string.time_left_pause, minutes_pause, 0)));
                    text_display.post(()->text_display.setText(R.string.pause_descr));
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
                                int finalI = i;
                                time_left.post(() -> time_left.setText(context.getString(R.string.time_left_pause, (int)(1.0 * finalI / (update_frequency * 60.0)), (finalI % (update_frequency * 60) / update_frequency) - 1)));
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

    public void play_res(int resId) {
        if (media_player != null) {
            media_player.reset();
        }
        media_player = MediaPlayer.create(context, resId);
        media_player.start();
    }
}
