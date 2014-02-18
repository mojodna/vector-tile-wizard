package com.example.helloworld.core;

import java.util.ArrayList;
import java.util.List;

public class Tile {
    private final List<Layer> layers = new ArrayList<>();
    private final int zoom;
    private final int x;
    private final int y;

    public Tile(int zoom, int x, int y) {
        this.zoom = zoom;
        this.x = x;
        this.y = y;
    }

    public Layer addLayer(Layer layer) {
        this.layers.add(layer);

        return layer;
    }

    public List<Layer> getLayers() {
        return this.layers;
    }
}
