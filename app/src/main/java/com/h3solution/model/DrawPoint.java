package com.h3solution.model;

/**
 * Draw Point
 * Created by HHHai on 17-06-2017.
 */
public class DrawPoint {
    private double x;
    private double y;

    public DrawPoint() {
    }

    public DrawPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}