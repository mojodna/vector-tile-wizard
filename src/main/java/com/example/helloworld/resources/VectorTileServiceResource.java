package com.example.helloworld.resources;

import com.example.helloworld.core.Renderer;
import com.example.helloworld.core.Tile;
import com.example.helloworld.core.TileUtils;
import com.example.helloworld.service.HelloWorldService;
import com.google.common.collect.Lists;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;
import com.yammer.dropwizard.jersey.params.IntParam;
import com.yammer.metrics.annotation.Timed;
import mapnik.vector.VectorTile;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

@Path("/")
public class VectorTileServiceResource {
    private final static Logger logger = LoggerFactory.getLogger(HelloWorldService.class);
    private final HttpClient httpClient;
    private final String source;

    public VectorTileServiceResource(HttpClient httpClient, String source) {
        this.httpClient = httpClient;
        this.source = source;
    }

    protected VectorTile.tile fetch(int zoom, int x, int y) throws IOException {
        // overzooming
        if (zoom > 14) {
            x = (int) Math.floor(x / Math.pow(2, zoom - 14));
            y = (int) Math.floor(y / Math.pow(2, zoom - 14));
            zoom = 14;

            // TODO clip the resulting geometry
        }

        String url = source
                .replace("{z}", Integer.toString(zoom))
                .replace("{x}", Integer.toString(x))
                .replace("{y}", Integer.toString(y));

        logger.info("Fetching " + url);

        HttpGet get = new HttpGet(url);

        // TODO user-agent

        HttpResponse response = httpClient.execute(get);

        HttpEntity entity = response.getEntity();

        // responses won't always be deflated (and may not report whether they are or not)
        try {
            // assume that they're probably compressed
            return VectorTile.tile.parseFrom(CodedInputStream.newInstance(new InflaterInputStream(entity.getContent())));
        } catch (InvalidProtocolBufferException e) {
            // it was either uncompressed or was actually invalid
            return VectorTile.tile.parseFrom(CodedInputStream.newInstance(entity.getContent()));
        }
    }

    protected VectorTile.tile reverseFeatures(VectorTile.tile tile) {
        final List<VectorTile.tile.layer> layers = new ArrayList<>();

        for (VectorTile.tile.layer layer : tile.getLayersList()) {
            layers.add(VectorTile.tile.layer
                    .newBuilder(layer)
                    .clearFeatures()
                    .addAllFeatures(Lists.reverse(layer.getFeaturesList()))
                    .build());
        }

        return VectorTile.tile
                .newBuilder(tile)
                .clearLayers()
                .addAllLayers(layers)
                .build();
    }

    @GET
    @Path("{z}/{x}/{y}.vector.pbf")
    @Produces("application/x-protobuf")
    @Timed
    public Response get(@PathParam("z") IntParam zoom, @PathParam("x") IntParam x, @PathParam("y") IntParam y) throws Exception {
        final VectorTile.tile tile = reverseFeatures(fetch(zoom.get(), x.get(), y.get()));

        return Response
                .status(200)
                .entity(new StreamingOutput() {
                    @Override
                    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                        DeflaterOutputStream deflater = new DeflaterOutputStream(outputStream);
                        tile.writeTo(deflater);
                        // generates smaller output (and is faster?) but Mapnik chokes
                        // tile.writeTo(CodedOutputStream.newInstance(deflater));
                        deflater.close();
                    }
                })
                .build();
    }

    @GET
    @Path("{z}/{x}/{y}.txt")
    @Produces(MediaType.TEXT_PLAIN)
    @Timed
    public Response getName(@PathParam("z") IntParam zoom, @PathParam("x") IntParam x, @PathParam("y") IntParam y) throws Exception {
        final VectorTile.tile vtile = fetch(zoom.get(), x.get(), y.get());

        Tile tile = TileUtils.decode(zoom.get(), x.get(), y.get(), vtile);

        return Response
                .status(200)
                .entity(tile.getLayers().get(0).getName())
                .build();
    }

    @GET
    @Path("{z}/{x}/{y}.png")
    @Produces("image/png")
    @Timed
    public Response render(@PathParam("z") IntParam zoom, @PathParam("x") IntParam x, @PathParam("y") IntParam y) throws Exception {
        final VectorTile.tile vtile = fetch(zoom.get(), x.get(), y.get());

        Tile tile = TileUtils.decode(zoom.get(), x.get(), y.get(), vtile);

        Renderer renderer = new Renderer();

        final BufferedImage image = renderer.renderTile(zoom.get(), x.get(), y.get(), tile);

        return Response
                .status(200)
                .entity(new StreamingOutput() {
                    @Override
                    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                        ImageIO.write(image, "png", outputStream);
                    }
                })
                .build();
    }
}
