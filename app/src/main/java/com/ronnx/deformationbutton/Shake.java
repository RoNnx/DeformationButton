package com.ronnx.deformationbutton;

public class Shake {

    private float translateX;
    private float translateY;

    public Shake(float translateX, float translateY) {
        this.translateX = translateX;
        this.translateY = translateY;
    }

    public float getTranslateX() {
        return translateX;
    }

    public void setTranslateX(float translateX) {
        this.translateX = translateX;
    }

    public float getTranslateY() {
        return translateY;
    }

    public void setTranslateY(float translateY) {
        this.translateY = translateY;
    }
}
