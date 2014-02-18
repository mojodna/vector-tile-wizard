package com.example.helloworld.core;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.awt.geom.PathIterator;
import java.util.List;

class VertexSymbolizer extends AbstractSymbolizer {
    private static final PApplet pa = new PApplet();

    public void apply(RenderingContext ctx, List<Feature> features) {
        ctx.getGraphics().blendMode(PConstants.ADD);

        super.apply(ctx, features);
    }

    public void apply(RenderingContext ctx, Feature feature) {
        PGraphics graphics = ctx.getGraphics();

        switch (ctx.getLayer().getName()) {
            case "landuse":
                graphics.noStroke();
                graphics.fill(0xffffcc00);
                break;

            case "road":
                graphics.stroke(0x804682b4);
                graphics.noFill();
                break;
        }

        graphics.strokeWeight(0.5f);

        double[] coords = new double[6];

        // TODO this is a hack and I have no idea how Ellipse2Ds (with h=0, w=0) are iterated over
        for (PathIterator iterator = feature.getShape().getPathIterator(ctx.getTransform()); !iterator.isDone(); iterator.next()) {
            int type = iterator.currentSegment(coords);

            switch (type) {
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_LINETO:
                    float x = (float) coords[0];
                    float y = (float) coords[1];

                    for (int i = 0; i < 25; i++) {
                        // graphics.point(x + 5 * (pa.randomGaussian() - 0.5f), y + 5 * (pa.randomGaussian() - 0.5f));
                        graphics.ellipse(x + 5 * (pa.randomGaussian() - 0.5f), y + 5 * (pa.randomGaussian() - 0.5f), 1, 1);
                    }

                    graphics.ellipse(x, y, 5, 5);

                    break;
            }
        }
    }
}
