package com.example.helloworld.core;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import mapnik.vector.VectorTile;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TileUtils {
    public final static int SUPPORTED_VERSION = 1;

    public static Tile decode(int zoom, int col, int row, VectorTile.tile vtile) {
        Tile tile = new Tile(zoom, col, row);

        for (VectorTile.tile.layer vlayer : vtile.getLayersList()) {
            Layer layer = tile.addLayer(new Layer(vlayer.getName(), vlayer.getExtent()));

            System.out.println("Layer version: " + vlayer.getVersion());

            if (vlayer.getVersion() != SUPPORTED_VERSION) {
                throw new RuntimeException("Layer version reported as " + vlayer.getVersion() + ", which is unsupported.");
            }

            for (VectorTile.tile.feature vfeature : vlayer.getFeaturesList()) {
                Feature feature;

                switch (vfeature.getType()) {
                    case Point:
                        feature = decodePoint(vfeature);
                        break;

                    case LineString:
                    case Polygon:
                    case Unknown:
                        feature = decodePath(vfeature);

                        break;

                    default:
                        throw new RuntimeException("Unrecognized feature type: " + vfeature.getType());
                }

                for (int i = 0; i < vfeature.getTagsCount(); ) {
                    String key = vlayer.getKeys(vfeature.getTags(i++));
                    Object value = getValue(vlayer.getValues(vfeature.getTags(i++)));

                    feature.addTag(key, value);
                }

            }
        }

        return tile;
    }

    public static VectorTile.tile encode(Tile tile) {
        VectorTile.tile.Builder vtile = VectorTile.tile.newBuilder();

        for (Layer layer : tile.getLayers()) {
            VectorTile.tile.layer.Builder vlayer = VectorTile.tile.layer.newBuilder();

            vlayer.setExtent(layer.getExtent());
            vlayer.setName(layer.getName());

            vlayer.setVersion(1);

            List<String> keys = new ArrayList<>();
            // use the Object form of the value to avoid converting duplicate values *AND* because
            // VectorTile.tile.vector doesn't implement equals() and hashCode() according to our needs
            List<Object> values = new ArrayList<>();

            for (Feature feature : layer.getFeatures()) {
                VectorTile.tile.feature.Builder vfeature = VectorTile.tile.feature.newBuilder();

                vfeature.setId(feature.getId());
                vfeature.setType(feature.getType());
                vfeature.addAllGeometry(encodeGeometry(feature.getShape()));

                for (Map.Entry<String, Object> tags : feature.getTags().entrySet()) {
                    String key = tags.getKey();
                    Object value = tags.getValue();

                    if (!keys.contains(key)) {
                        keys.add(key);
                    }

                    if (!values.contains(value)) {
                        values.add(value);
                    }

                    vfeature.addTags(keys.indexOf(key));
                    vfeature.addTags(values.indexOf(value));
                }

                vlayer.addFeatures(vfeature);
            }

            vlayer.addAllKeys(keys);
            vlayer.addAllValues(Collections2.transform(values, new Function<Object, VectorTile.tile.value>() {
                @Override
                public VectorTile.tile.value apply(final Object v) {
                    return makeValue(v);
                }
            }));

            vtile.addLayers(vlayer);
        }

        return vtile.build();
    }

    protected static PathFeature decodePath(VectorTile.tile.feature feature) {
        Path2D path = new Path2D.Double();

        int prevX = 0;
        int prevY = 0;

        for (int i = 0; i < feature.getGeometryCount(); ) {
            int instruction = feature.getGeometry(i++);
            int command = instruction & ((1 << Commands.BIT_LENGTH) - 1);
            int length = instruction >> Commands.BIT_LENGTH;

            switch (command) {
                case Commands.MOVE_TO:
                case Commands.LINE_TO:
                    int next = i + 2 * length;
                    List<Integer> coordinates = feature.getGeometryList().subList(i, next);

                    for (int j = 0; j < coordinates.size(); j += 2) {
                        int x = coordinates.get(j);
                        int y = coordinates.get(j + 1);

                        // convert to signed ints
                        x = prevX + ((x >> 1) ^ (-(x & 1)));
                        y = prevY + ((y >> 1) ^ (-(y & 1)));

                        prevX = x;
                        prevY = y;

                        switch (command) {
                            case Commands.MOVE_TO:
                                path.moveTo(x, y);
                                break;

                            case Commands.LINE_TO:
                                path.lineTo(x, y);
                                break;
                        }
                    }

                    i = next;
                    break;

                case Commands.CLOSE_PATH:
                    path.closePath();
                    break;

                default:
                    throw new RuntimeException("Unknown command: " + command);
            }
        }

        return new PathFeature(feature.getType(), feature.getId(), path);
    }

    protected static PointFeature decodePoint(VectorTile.tile.feature feature) {
        // TODO MultiPoints (multiple MOVE_TOs within a Point feature)

        assert feature.getGeometryCount() == 3;

        int i = 0;
        int instruction = feature.getGeometry(i++);
        int command = instruction & ((1 << Commands.BIT_LENGTH) - 1);
        int length = instruction >> Commands.BIT_LENGTH;

        assert command == Commands.MOVE_TO;
        assert length == 1;

        int x = feature.getGeometry(i++);
        int y = feature.getGeometry(i++);

        // convert to signed ints
        x = ((x >> 1) ^ (-(x & 1)));
        y = ((y >> 1) ^ (-(y & 1)));

        return new PointFeature(feature.getId(), new Point(x, y));
    }

    protected static Object getValue(VectorTile.tile.value value) {
        if (value.hasBoolValue()) {
            return value.getBoolValue();
        }

        if (value.hasDoubleValue()) {
            return value.getDoubleValue();
        }

        if (value.hasFloatValue()) {
            return value.getFloatValue();
        }

        if (value.hasIntValue()) {
            return value.getIntValue();
        }

        if (value.hasSintValue()) {
            return value.getSintValue();
        }

        if (value.hasStringValue()) {
            return value.getStringValue();
        }

        if (value.hasUintValue()) {
            return value.getUintValue();
        }

        return null;
    }

    protected static VectorTile.tile.value makeValue(Object value) {
        VectorTile.tile.value.Builder val = VectorTile.tile.value.newBuilder();

        if (value instanceof Boolean) {
            val.setBoolValue((Boolean) value);
        }

        if (value instanceof Double) {
            val.setDoubleValue((Double) value);
        }

        if (value instanceof Float) {
            val.setFloatValue((Float) value);
        }

        if (value instanceof Integer) {
            val.setIntValue((Integer) value);
        }

        if (value instanceof Long) {
            val.setIntValue((Long) value);
        }

        if (value instanceof String) {
            val.setStringValue((String) value);
        }

        // Note: no signed or unsigned ints because Java doesn't really distinguish between them

        return val.build();
    }

    protected static List<Integer> encodeGeometry(Shape shape) {
        if (shape instanceof Ellipse2D.Double) {
            return encodeGeometry((Ellipse2D.Double) shape);
        }

        if (shape instanceof Path2D.Double) {
            return encodeGeometry((Path2D.Double) shape);
        }

        throw new RuntimeException("Unsupported Shape class: " + shape.getClass());
    }

    protected static List<Integer> encodeGeometry(Ellipse2D.Double point) {
        List<Integer> commands = new ArrayList<>();

        commands.add((1 << Commands.BIT_LENGTH) | Commands.MOVE_TO); // [00001 001] = command type 1 (MoveTo), length 1
        commands.add((int) point.getX());
        commands.add((int) point.getY());

        return commands;
    }

    protected static List<Integer> encodeGeometry(Path2D.Double path) {
        List<Integer> commands = new ArrayList<>();

        int x = 0;
        int y = 0;
        double[] coords = new double[6];

        for (PathIterator iterator = path.getPathIterator(null); !iterator.isDone(); iterator.next()) {
            int type = iterator.currentSegment(coords);

            int dx = x - (int) coords[0];
            int dy = y - (int) coords[1];

            switch (type) {
                case PathIterator.SEG_MOVETO:
                    // TODO optimize by grouping MOVE_TOs
                    commands.add((1 << Commands.BIT_LENGTH) | (Commands.MOVE_TO & ((1 << Commands.BIT_LENGTH) - 1)));
                    commands.add((dx << 1) ^ (dx >> 31));
                    commands.add((dy << 1) ^ (dy >> 31));

                    break;

                case PathIterator.SEG_LINETO:
                    // TODO optimize by grouping LINE_TOs
                    commands.add((1 << Commands.BIT_LENGTH) | (Commands.LINE_TO & ((1 << Commands.BIT_LENGTH) - 1)));
                    commands.add((dx << 1) ^ (dx >> 31));
                    commands.add((dy << 1) ^ (dy >> 31));

                    break;

                case PathIterator.SEG_CLOSE:
                    // command type 7 (ClosePath), length 1
                    commands.add((1 << Commands.BIT_LENGTH) | (Commands.CLOSE_PATH & ((1 << Commands.BIT_LENGTH) - 1)));
                    break;
            }

            x = (int) coords[0];
            y = (int) coords[1];
        }

        return commands;
    }
}
