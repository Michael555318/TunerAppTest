package com.example.tunerapptest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    // Instance Variables
    Button recordButton;
    TextView display;
    MediaRecorder recorder;
    boolean mStartRecording = true;
    ArrayList<Notes> intensityFunction = new ArrayList<>();
    double[] guitarF = new double[] {82.41, 110, 146.8, 196, 246.9, 329.6}; // Add to note class later
    double maxCenterOfMass;
    double frequency;
    float time = 0;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RECORD_PERMISSION:
                permissionToRecordAccepted = grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();
    }


    // Constants
    final static int RECORD_PERMISSION = 100;
    final static String LOG_TAG = "Audio Test Prepare";
    private static String fileName = null;
    private static int AMP_REF = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.3gp";
        ActivityCompat.requestPermissions(this, permissions,
                RECORD_PERMISSION);

        // WireWidgets - record button and display textview
        recordButton = findViewById(R.id.button_main_record);
        display = findViewById(R.id.textView_main_display);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    recordButton.setText("Stop recording");
                    final Handler handler=new Handler();
                    handler.post(new Runnable(){
                        @Override
                        public void run() {
                            // upadte textView here
                            if (recorder != null) {
                                int amplitude = recorder.getMaxAmplitude();
                                double power_db = 20 * Math.log10(amplitude/AMP_REF); //Converter to dB (for now)
                                double intensity = Math.pow(10, power_db/10 -12);

                                //Here you can put condition (low/high)
                                Log.i("AMPLITUDE", new Integer(amplitude).toString());
                                //Log.i("AMPLITUDE", ""+intensity);
                                //display.setText("Intensity: " + intensity);

                                //Fourier Transform
                                if (amplitude >= 2000) {
                                    intensityFunction.add(new Notes(intensity, time));
                                    for (int f = 0; f < guitarF.length; f++) {
                                        if (calculateFT(intensityFunction, guitarF[f], time) > maxCenterOfMass
                                                && intensityFunction.size() >= 5) {
                                            maxCenterOfMass = calculateFT(intensityFunction, guitarF[f], time);
                                            frequency = f;
                                        }
                                    }
                                    time += 200;
                                    //display.setText(determineNote(frequency));
                                    display.setText(""+frequency);
                                } else {
                                    ArrayList<Notes> intensityFunction = new ArrayList<>();
                                    time = 0;
                                    maxCenterOfMass = 0;
                                }
                            }
                            handler.postDelayed(this,200); // set time here to refresh textView
                        }
                    });
                } else {
                    recordButton.setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        });

    }

    private String determineNote(double frequency) {
        if (frequency == guitarF[0]) {
            return "E";
        } else if (frequency == guitarF[1]) {
            return "A";
        } else if (frequency == guitarF[2]) {
            return "D";
        } else if (frequency == guitarF[3]) {
            return "G";
        } else if (frequency == guitarF[4]) {
            return "B";
        } else {
            return "E";
        }
    }

    private double calculateFT(ArrayList<Notes> intensityF, double frequency, float time) {
        ArrayList<Double> xCoordinate = new ArrayList<>();
        for (int i = 0; i < intensityF.size(); i++) {
            //xCoordinate.add(intensityF.get(i).getIntensity()*Math.pow(Math.E, -2*Math.PI*frequency*time));

        }
        double sum = 0;
        for (int i = 0; i < xCoordinate.size(); i++) {
            sum += xCoordinate.get(i);
        }
        return sum / intensityF.size();
    }

    private double calculateIntegral(float time, ArrayList<Notes> intensity, double interval) {
        double sum = 0;
        for (int i = 0; i < intensity.size(); i++) {
            if (i == 0) {
                sum+=intensity.get(i).getIntensity();
            } else if (i ==intensity.size()-1) {
                sum+=intensity.get(i).getIntensity();
            } else {
                sum+=2*intensity.get(i).getIntensity()
            }

        }
        return interval/2*(sum);

    }

    // Recording Methods
    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();;
        } else {
            stopRecording();
        }
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

}
