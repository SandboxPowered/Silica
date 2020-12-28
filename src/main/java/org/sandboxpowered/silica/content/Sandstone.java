package org.sandboxpowered.silica.content;

public enum Sandstone {
    NORMAL(null),
    CUT("cut"),
    CHISELED("chiseled"),
    SMOOTH("smooth");

    private final String prefix;

    Sandstone(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}