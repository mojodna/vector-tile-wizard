package com.example.helloworld.core;

import mapnik.vector.VectorTile;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public abstract class Feature {
    protected final Long id;
    // TODO create an enum
    protected final VectorTile.tile.GeomType type;
    private final Map<String, Object> tags = new HashMap<>();

    public Feature(Long id, VectorTile.tile.GeomType type) {
        this.id = id;
        this.type = type;
    }

    public abstract Shape getShape();

    public Long getId() {
        return this.id;
    }

    public VectorTile.tile.GeomType getType() {
        return this.type;
    }

    public void addTag(String key, Object value) {
        this.tags.put(key, value);
    }

    public Map<String, Object> getTags() {
        return this.tags;
    }
}
