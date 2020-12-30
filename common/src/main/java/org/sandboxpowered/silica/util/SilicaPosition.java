package org.sandboxpowered.silica.util;

import org.sandboxpowered.api.util.Direction;
import org.sandboxpowered.api.util.math.Position;

public class SilicaPosition implements Position {
    private final int x, y, z;

    public SilicaPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public Mutable toMutable() {
        return null;
    }

    @Override
    public Position toImmutable() {
        return this;
    }

    @Override
    public Position offset(Direction direction, int amount) {
        return new SilicaPosition(getX() + (direction.getOffsetX() * amount), getY() + (direction.getOffsetY() * amount), getZ() + (direction.getOffsetZ() * amount));
    }

    @Override
    public Position add(int x, int y, int z) {
        return new SilicaPosition(getX() + x, getY() + y, getZ() + z);
    }

    @Override
    public Position sub(int x, int y, int z) {
        return new SilicaPosition(getX() - x, getY() - y, getZ() - z);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }
}