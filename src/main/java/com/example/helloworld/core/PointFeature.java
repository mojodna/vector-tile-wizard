package com.example.helloworld.core;

import mapnik.vector.VectorTile;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class PointFeature extends Feature {
    private final Point point;

    public PointFeature(Long id, Point point) {
        super(id, VectorTile.tile.GeomType.Point);
        this.point = point;
    }

    public Shape getShape() {
        // TODO this is kind of a hack
        return new Ellipse2D.Double(getPoint().getX(), getPoint().getY(), 0, 0);
    }

    public Point getPoint() {
        return point;
    }
}
