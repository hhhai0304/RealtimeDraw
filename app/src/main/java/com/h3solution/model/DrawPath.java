package com.h3solution.model;

import java.util.ArrayList;

/**
 * Draw Path
 * Created by HHHai on 17-06-2017.
 */
public class DrawPath {
    private String color;
    private ArrayList<DrawPoint> points;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public ArrayList<DrawPoint> getPoints() {
        if (points == null) {
            points = new ArrayList<>();
        }
        return points;
    }

    public void setPoints(ArrayList<DrawPoint> points) {
        this.points = points;
    }
}