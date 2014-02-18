package com.example.helloworld.core;

import com.yammer.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Renderer {
    private static final Logger logger = LoggerFactory.getLogger(Renderer.class);

    private static final int TILE_SIZE = 256;

//    private final ExecutorService executorService;

//    public Renderer(ExecutorService executorService) {
//        super();
//
//        this.executorService = executorService;
//    }

    @Timed
    public BufferedImage renderTile(int zoom, int x, int y, Tile tile) {
        // TODO PGraphicsFactory
        PGraphics canvas = new PGraphicsJava2D();
        canvas.setSize(TILE_SIZE, TILE_SIZE);
        canvas.hint(PConstants.ENABLE_RETINA_PIXELS);
        canvas.smooth();

        canvas.beginDraw();
        canvas.background(0xff333333);

        // TODO do this in a threadpool
        // TODO PGraphics, PImage, or BufferedImage?
        List<Future<PGraphics>> tasks = new ArrayList<>();

        for (Layer layer: tile.getLayers()) {
            if (!shouldRenderLayer(layer)) {
                continue;
            }

            // pool.submit() // returns a Future

            // TODO layer-dependent buffering
            // TODO and/or create a BufferedImage-backed Graphics2D object
            PGraphics pg = new PGraphicsJava2D();
            pg.setSize(TILE_SIZE, TILE_SIZE);
            pg.beginDraw();
            pg.smooth();

            RenderingContext ctx = new RenderingContext();
            ctx.setZoom(zoom);
            ctx.setX(x);
            ctx.setY(y);
            ctx.setGraphics(pg);
            ctx.setLayer(layer);
            ctx.setTransform(AffineTransform.getScaleInstance(TILE_SIZE / layer.getExtent(), TILE_SIZE / layer.getExtent()));

            List<Symbolizer> symbolizers = new ArrayList<>();
            symbolizers.add(new PathSymbolizer());
            // symbolizers.add(new VertexSymbolizer());

            logger.info("Rendering " + layer.getName());

            // TODO render symbolizers in parallel
            for (Symbolizer symbolizer: symbolizers) {
                symbolizer.apply(ctx, layer.getFeatures());
            }

            pg.endDraw();

            // TODO layer-dependent blending
            // canvas.blendMode(DIFFERENCE);

            // TODO buffer
            canvas.image(pg, 0, 0);
        }

        canvas.endDraw();

        return (BufferedImage) canvas.getNative();
    }

    private boolean shouldRenderLayer(Layer layer) {
        return layer.getName().equals("landuse") ||
                layer.getName().equals("road");
    }

    private final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    protected void renderFeature(PGraphics pg, Feature feature, AffineTransform at) {
        /*
        for (Map.Entry<String, Object> tag: feature.getTags().entrySet()) {
            println(tag.getKey() + "=" + tag.getValue());
        }
        */

        if (!feature.getTags().containsKey("class") ||
                !feature.getTags().get("class").equals("street")) {
            return;
        }

        /*
        for (Map.Entry<String, Object> tag: feature.getTags().entrySet()) {
            println(tag.getKey() + "=" + tag.getValue());
        }
        */

        // TODO what are the fun things that can be done now that we have geometry available to Processing?

        // TODO feature.getVertices() or is a PathIterator sufficient?

        // drawPath(pg, feature.getPath().getPathIterator(at));

        /*
        // TODO this is necessary for dashed lines
        BufferedImage bi = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) bi.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        // g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        // float dash[] = { 10.0f, 5.0f, 2.0f, 5.0f };
        float dash[] = { 2.0f };
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        g.setPaint(Color.yellow);

        // TODO should this be done transparently?
        g.draw(feature.getPath().createTransformedShape(at));

        g.dispose();

        pg.image(new PImage(bi), 0, 0);
        */

    }
}