package com.example.helloworld.core;

import geomerative.RShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;

import java.awt.geom.PathIterator;
import java.util.List;

public class HachureSymbolizer extends AbstractSymbolizer {
    private static final Logger logger = LoggerFactory.getLogger(Renderer.class);

    @Override
    public void apply(RenderingContext ctx, List<Feature> features) {
        PGraphics graphics = ctx.getGraphics();

        graphics.stroke(0xff666666);
        graphics.noFill();

        for (Feature feature: features) {
            if (feature.getTags().get("ISOLINE_TY").equals("800 - Normal")) {
                this.apply(ctx, feature);
            }
        }
    }

    public void apply(RenderingContext ctx, Feature feature) {
        logger.info("elevation: " + feature.getTags().get("ELEVATION"));

        PGraphics graphics = ctx.getGraphics();

        double[] coords = new double[6];

        RShape path = new RShape();

        for (PathIterator iterator = feature.getShape().getPathIterator(ctx.getTransform()); !iterator.isDone(); iterator.next()) {
            int type = iterator.currentSegment(coords);
            float x = (float) coords[0];
            float y = (float) coords[1];

            switch (type) {
                case PathIterator.SEG_MOVETO:
                    path.addMoveTo(x, y);
                    break;

                case PathIterator.SEG_LINETO:
                    path.addLineTo(x, y);
                    break;

                case PathIterator.SEG_CLOSE:
                    path.addClose();
                    break;
            }
        }

        path.draw(graphics);
    }
}
