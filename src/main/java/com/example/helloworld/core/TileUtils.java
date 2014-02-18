package com.example.helloworld.core;

import mapnik.vector.VectorTile;

import java.awt.geom.Path2D;
import java.util.List;

public class TileUtils {
    public static Tile decode(int zoom, int col, int row, VectorTile.tile vtile) {
        Tile tile = new Tile(zoom, col, row);

        for (VectorTile.tile.layer vlayer: vtile.getLayersList()) {
            Layer layer = tile.addLayer(new Layer(vlayer.getName(), vlayer.getExtent()));

            for (VectorTile.tile.feature vfeature: vlayer.getFeaturesList()) {
                Path2D path = new Path2D.Double();
                PathFeature feature = new PathFeature(vfeature.getType(), vfeature.getId(), path);
                layer.addFeature(feature);

                for (int i = 0; i < vfeature.getTagsCount();) {
                    String key = vlayer.getKeys(vfeature.getTags(i++));
                    Object value = getValue(vlayer.getValues(vfeature.getTags(i++)));

                    feature.addTag(key, value);
                }

                // TODO points

                int prevX = 0;
                int prevY = 0;

                for (int i = 0; i < vfeature.getGeometryCount();) {
                    int instruction = vfeature.getGeometry(i++);
                    int command = instruction & ((1 << Commands.BIT_LENGTH) - 1);
                    int length = instruction >> Commands.BIT_LENGTH;

                    switch (command) {
                        case Commands.MOVE_TO:
                        case Commands.LINE_TO:
                            int next = i + 2 * length;
                            List<Integer> coordinates = vfeature.getGeometryList().subList(i, next);

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
            }
        }

        return tile;
    }

    public static VectorTile.tile encode(Tile tile) {
        return null;
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
}
