package com.ronnx.deformationbutton.anim;

public class Rect {

    private float radius;
    private float width;
    private float alpha;

    public Rect(float radius, float width, float alpha) {
        this.radius = radius;
        this.width = width;
        this.alpha = alpha;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }
}
