package com.example.helloworld.core;

import java.util.ArrayList;
import java.util.List;

public class Layer {
    private final int extent;
    private final String name;
    private final List<Feature> features = new ArrayList<>();

    public Layer(String name, int extent) {
        this.name = name;
        this.extent = extent;
    }

    public int getExtent() {
        return this.extent;
    }

    public Feature addFeature(Feature feature) {
        this.features.add(feature);

        return feature;
    }

    public List<Feature> getFeatures() {
        return this.features;
    }

    public String getName() {
        return this.name;
    }
}
