package org.sandboxpowered.silica.minecraft;

import java.util.Locale;

public enum Colour {
    WHITE,
    ORANGE,
    MAGENTA,
    LIGHT_BLUE,
    YELLOW,
    LIME,
    PINK,
    GRAY,
    LIGHT_GRAY,
    CYAN,
    PURPLE,
    BLUE,
    BROWN,
    GREEN,
    RED,
    BLACK;

    private final String name;

    Colour() {
        this.name = name().toLowerCase(Locale.ENGLISH);
    }

    Colour(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
