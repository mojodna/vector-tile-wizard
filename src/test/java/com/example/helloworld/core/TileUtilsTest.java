package com.example.helloworld.core;


import mapnik.vector.VectorTile;
import org.junit.Test;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class TileUtilsTest {
    @Test
    public void encodeAnEmptyTile() {
        Tile tile = new Tile(0, 0, 0);

        VectorTile.tile vtile = TileUtils.encode(tile);

        assertThat(vtile.getLayersCount(), equalTo(0));
    }

    @Test
    public void encodeTileWithEmptyLayer() {
        final String name = "data";

        Tile tile = new Tile(0, 0, 0);
        tile.addLayer(new Layer(name, 256));
        VectorTile.tile vtile = TileUtils.encode(tile);

        assertThat(vtile.getLayersCount(), equalTo(1));

        VectorTile.tile.layer layer = vtile.getLayers(0);

        assertThat(layer.getName(), equalTo(name));
        assertThat(layer.getFeaturesCount(), equalTo(0));
    }

    @Test
    public void encodeTileWithAPointFeature() {
        Tile tile = new Tile(0, 0, 0);
        Layer layer = tile.addLayer(new Layer("data", 256));
        Feature feature = new PointFeature(1L, new Point(3, 6));
        layer.addFeature(feature);
        VectorTile.tile vtile = TileUtils.encode(tile);

        assertThat(vtile.getLayersCount(), equalTo(1));

        VectorTile.tile.layer vlayer = vtile.getLayers(0);

        assertThat(vlayer.getFeaturesCount(), equalTo(1));

        VectorTile.tile.feature vfeature = vlayer.getFeatures(0);

        assertThat(vfeature.getId(), equalTo(feature.getId()));
        assertThat(vfeature.getType(), equalTo(feature.getType()));
        assertThat(vfeature.getGeometryCount(), equalTo(3));

        assertThat(vfeature.getGeometry(0), equalTo(9)); // MOVE_TO (1) + 1 vertex
        assertThat(vfeature.getGeometry(1), equalTo(3));
        assertThat(vfeature.getGeometry(2), equalTo(6));
    }

    @Test
    public void encodeTileWithAPathFeature() {
        Tile tile = new Tile(0, 0, 0);
        Layer layer = tile.addLayer(new Layer("data", 256));

        Path2D path = new Path2D.Double();
        path.moveTo(3, 6);
        path.lineTo(5, 6);
        path.lineTo(12, 22);
        path.closePath();

        Feature feature = new PathFeature(VectorTile.tile.GeomType.Polygon, 1L, path);
        layer.addFeature(feature);
        VectorTile.tile vtile = TileUtils.encode(tile);

        assertThat(vtile.getLayersCount(), equalTo(1));

        VectorTile.tile.layer vlayer = vtile.getLayers(0);

        assertThat(vlayer.getFeaturesCount(), equalTo(1));

        VectorTile.tile.feature vfeature = vlayer.getFeatures(0);

        assertThat(vfeature.getId(), equalTo(feature.getId()));
        assertThat(vfeature.getType(), equalTo(feature.getType()));
        assertThat(vfeature.getGeometryCount(), equalTo(10));

        assertThat(vfeature.getGeometry(0), equalTo(9)); // [00001 001] = command type 1 (MoveTo), length 1
        assertThat(vfeature.getGeometry(1), equalTo(5)); // relative MoveTo(+3, +6) (5 = zigzag encoded)
        assertThat(vfeature.getGeometry(2), equalTo(11));

        assertThat(vfeature.getGeometry(3), equalTo(10)); // [00010 001] = command type 2 (LineTo), length 1
        assertThat(vfeature.getGeometry(4), equalTo(3)); // relative LineTo(+5, +6) == LineTo(8, 12)
        assertThat(vfeature.getGeometry(5), equalTo(0));

        assertThat(vfeature.getGeometry(6), equalTo(10)); // [00010 001] = command type 2 (LineTo), length 1
        assertThat(vfeature.getGeometry(7), equalTo(13)); // relative LineTo(+12, +22) == LineTo(20, 34)
        assertThat(vfeature.getGeometry(8), equalTo(31));

        assertThat(vfeature.getGeometry(9), equalTo(15)); // command type 7 (ClosePath), length 1
    }

    @Test
    public void encodeTileWithAnOptimalPathFeature() {
        Tile tile = new Tile(0, 0, 0);
        Layer layer = tile.addLayer(new Layer("data", 256));

        Path2D path = new Path2D.Double();
        path.moveTo(3, 6);
        path.lineTo(5, 6);
        path.lineTo(12, 22);
        path.closePath();

        Feature feature = new PathFeature(VectorTile.tile.GeomType.Polygon, 1L, path);
        layer.addFeature(feature);
        VectorTile.tile vtile = TileUtils.encode(tile);

        assertThat(vtile.getLayersCount(), equalTo(1));

        VectorTile.tile.layer vlayer = vtile.getLayers(0);

        assertThat(vlayer.getFeaturesCount(), equalTo(1));

        VectorTile.tile.feature vfeature = vlayer.getFeatures(0);

        assertThat(vfeature.getId(), equalTo(feature.getId()));
        assertThat(vfeature.getType(), equalTo(feature.getType()));
        assertThat(vfeature.getGeometryCount(), equalTo(9));

        assertThat(vfeature.getGeometry(0), equalTo(9)); // [00001 001] = command type 1 (MoveTo), length 1
        assertThat(vfeature.getGeometry(1), equalTo(5)); // relative MoveTo(+3, +6)
        assertThat(vfeature.getGeometry(2), equalTo(11));

        assertThat(vfeature.getGeometry(3), equalTo(18)); // [00010 010] = command type 2 (LineTo), length 2
        assertThat(vfeature.getGeometry(4), equalTo(3)); // relative LineTo(+5, +6) == LineTo(8, 12)
        assertThat(vfeature.getGeometry(5), equalTo(0));
        assertThat(vfeature.getGeometry(6), equalTo(13)); // relative LineTo(+12, +22) == LineTo(20, 34)
        assertThat(vfeature.getGeometry(7), equalTo(31));

        assertThat(vfeature.getGeometry(8), equalTo(15)); // command type 7 (ClosePath), length 1
    }

    @Test
    public void encodeTileWithAPointFeatureContainingTags() {
        Tile tile = new Tile(0, 0, 0);
        Layer layer = tile.addLayer(new Layer("data", 256));

        Feature feature = new PointFeature(1L, new Point(4, 5));
        feature.addTag("spinny", "yes");
        feature.addTag("horse", "yes");
        layer.addFeature(feature);

        VectorTile.tile vtile = TileUtils.encode(tile);

        assertThat(vtile.getLayersCount(), equalTo(1));

        VectorTile.tile.layer vlayer = vtile.getLayers(0);
        List<String> keys = vlayer.getKeysList();
        List<VectorTile.tile.value> values = vlayer.getValuesList();

        assertThat(vlayer.getFeaturesCount(), equalTo(1));
        assertThat(vlayer.getKeysCount(), equalTo(2));
        assertThat(vlayer.getValuesCount(), equalTo(1));

        VectorTile.tile.feature vfeature = vlayer.getFeatures(0);

        assertThat(vfeature.getTagsCount(), equalTo(4));

        int i = 0;
        String key;
        VectorTile.tile.value value;

        // Note: since Feature.tags is a Map, the order of tag pairs in the vfeature won't necessarily be in the same
        // order

        key = keys.get(vfeature.getTags(i++));
        value = values.get(vfeature.getTags(i++));

        assertThat(key, equalTo("spinny"));
        assertThat(value.getStringValue(), equalTo("yes"));

        key = keys.get(vfeature.getTags(i++));
        value = values.get(vfeature.getTags(i++));

        assertThat(key, equalTo("horse"));
        assertThat(value.getStringValue(), equalTo("yes"));
    }
}
