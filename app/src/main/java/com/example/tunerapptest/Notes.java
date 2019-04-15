package com.example.tunerapptest;

public class Notes {

    double intensity;
    float time;

    public Notes(double intensity, float time) {
        this.intensity = intensity;
        this.time = time;
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }
}
