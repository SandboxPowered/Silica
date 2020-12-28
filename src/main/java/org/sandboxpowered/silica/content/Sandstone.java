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

    public String formatted(String name) {
        if (this.prefix != null) {
            return String.format("%s_%s", this.prefix, name);
        }
        return name;
    }

    public String getPrefix() {
        return prefix;
    }
}