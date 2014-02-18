package com.example.helloworld.core;

import processing.core.PConstants;
import processing.core.PGraphics;

import java.awt.geom.PathIterator;

public class PathSymbolizer extends AbstractSymbolizer {
    public void apply(RenderingContext ctx, Feature feature) {
        // TODO check that feature.getShape() instanceof Path2D

        if (ctx.getLayer().getName().equals("road") &&
                (!feature.getTags().containsKey("class") ||
                        !feature.getTags().get("class").equals("street"))) {
            return;
        }

        PGraphics graphics = ctx.getGraphics();

        switch (ctx.getLayer().getName()) {
            case "landuse":
                graphics.noStroke();
                graphics.fill(0xff00ff00);
                break;

            case "road":
                graphics.stroke(0xffcccccc);
                graphics.noFill();
                break;
        }

        double[] coords = new double[6];

        graphics.beginShape();

        for (PathIterator iterator = feature.getShape().getPathIterator(ctx.getTransform()); !iterator.isDone(); iterator.next()) {
            int type = iterator.currentSegment(coords);

            switch (type) {
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_LINETO:
                    graphics.vertex((float) coords[0], (float) coords[1]);
                    break;

                case PathIterator.SEG_CLOSE:
                    graphics.endShape(PConstants.CLOSE);
                    break;
            }
        }

        graphics.endShape();
    }
}
