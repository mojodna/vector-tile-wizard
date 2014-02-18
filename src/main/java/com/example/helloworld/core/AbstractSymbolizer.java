package com.example.helloworld.core;

import java.util.List;

public abstract class AbstractSymbolizer implements Symbolizer {
    public abstract void apply(RenderingContext ctx, Feature feature);

    public void apply(RenderingContext ctx, List<Feature> features) {
        for (Feature feature: features) {
            this.apply(ctx, feature);
        }
    }
}