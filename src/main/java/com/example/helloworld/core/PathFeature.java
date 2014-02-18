package com.example.helloworld.core;

import mapnik.vector.VectorTile;

import java.awt.*;
import java.awt.geom.Path2D;

public class PathFeature extends Feature {
    private final Path2D path;

    public PathFeature(VectorTile.tile.GeomType type, Long id, Path2D path) {
        super(id, type);
        this.path = path;
    }

    public Shape getShape() {
        return getPath();
    }

    public Path2D getPath() {
        return this.path;
    }
}
