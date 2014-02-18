package com.example.helloworld.core;

import java.util.List;

public interface Symbolizer {
    public void apply(RenderingContext ctx, Feature feature);
    public void apply(RenderingContext ctx, List<Feature> features);
}
