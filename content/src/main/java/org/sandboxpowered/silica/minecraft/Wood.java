package org.sandboxpowered.silica.minecraft;

public enum Wood {
    OAK("oak"),
    BIRCH("birch"),
    SPRUCE("spruce"),
    JUNGLE("jungle"),
    DARK_OAK("dark_oak"),
    ACACIA("acacia");
    private final String prefix;

    Wood(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}