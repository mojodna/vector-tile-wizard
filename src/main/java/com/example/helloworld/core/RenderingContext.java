package com.example.helloworld.core;

import processing.core.PGraphics;

import java.awt.geom.AffineTransform;

public class RenderingContext {
    private Layer layer;
    private double zoom;
    private double x;
    private double y;
    private PGraphics graphics;
    private AffineTransform transform;

    public AffineTransform getTransform() {
        return transform;
    }

    public void setTransform(AffineTransform transform) {
        this.transform = transform;
    }

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
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

    public PGraphics getGraphics() {
        return graphics;
    }

    public void setGraphics(PGraphics graphics) {
        this.graphics = graphics;
    }
}