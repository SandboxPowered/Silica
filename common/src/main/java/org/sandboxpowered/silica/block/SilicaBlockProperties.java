package org.sandboxpowered.silica.block;

import org.sandboxpowered.api.util.*;
import org.sandboxpowered.api.world.state.property.BooleanProperty;
import org.sandboxpowered.api.world.state.property.EnumProperty;
import org.sandboxpowered.api.world.state.property.IntProperty;
import org.sandboxpowered.api.world.state.property.Property;

public class SilicaBlockProperties {
    public static final Property<Boolean> ATTACHED = BooleanProperty.of("attached");
    public static final Property<Boolean> BOTTOM = BooleanProperty.of("bottom");
    public static final Property<Boolean> CONDITIONAL = BooleanProperty.of("conditional");
    public static final Property<Boolean> DISARMED = BooleanProperty.of("disarmed");
    public static final Property<Boolean> DRAG = BooleanProperty.of("drag");
    public static final Property<Boolean> ENABLED = BooleanProperty.of("enabled");
    public static final Property<Boolean> EXTENDED = BooleanProperty.of("extended");
    public static final Property<Boolean> EYE = BooleanProperty.of("eye");
    public static final Property<Boolean> FALLING = BooleanProperty.of("falling");
    public static final Property<Boolean> HANGING = BooleanProperty.of("hanging");
    public static final Property<Boolean> HAS_BOTTLE_0 = BooleanProperty.of("has_bottle_0");
    public static final Property<Boolean> HAS_BOTTLE_1 = BooleanProperty.of("has_bottle_1");
    public static final Property<Boolean> HAS_BOTTLE_2 = BooleanProperty.of("has_bottle_2");
    public static final Property<Boolean> HAS_RECORD = BooleanProperty.of("has_record");
    public static final Property<Boolean> HAS_BOOK = BooleanProperty.of("has_book");
    public static final Property<Boolean> INVERTED = BooleanProperty.of("inverted");
    public static final Property<Boolean> IN_WALL = BooleanProperty.of("in_wall");
    public static final Property<Boolean> LIT = BooleanProperty.of("lit");
    public static final Property<Boolean> LOCKED = BooleanProperty.of("locked");
    public static final Property<Boolean> OCCUPIED = BooleanProperty.of("occupied");
    public static final Property<Boolean> OPEN = BooleanProperty.of("open");
    public static final Property<Boolean> PERSISTENT = BooleanProperty.of("persistent");
    public static final Property<Boolean> POWERED = BooleanProperty.of("powered");
    public static final Property<Boolean> SHORT = BooleanProperty.of("short");
    public static final Property<Boolean> SIGNAL_FIRE = BooleanProperty.of("signal_fire");
    public static final Property<Boolean> SNOWY = BooleanProperty.of("snowy");
    public static final Property<Boolean> TRIGGERED = BooleanProperty.of("triggered");
    public static final Property<Boolean> UNSTABLE = BooleanProperty.of("unstable");
    public static final Property<Boolean> WATERLOGGED = BooleanProperty.of("waterlogged");
    public static final Property<Boolean> UP = BooleanProperty.of("up");
    public static final Property<Boolean> DOWN = BooleanProperty.of("down");
    public static final Property<Boolean> NORTH = BooleanProperty.of("north");
    public static final Property<Boolean> EAST = BooleanProperty.of("east");
    public static final Property<Boolean> SOUTH = BooleanProperty.of("south");
    public static final Property<Boolean> WEST = BooleanProperty.of("west");
    public static final Property<Integer> FLUID_LEVEL = IntProperty.of("level", 1, 8);
    public static final Property<Integer> AGE_1 = IntProperty.of("age", 0, 1);
    public static final Property<Integer> AGE_2 = IntProperty.of("age", 0, 2);
    public static final Property<Integer> AGE_3 = IntProperty.of("age", 0, 3);
    public static final Property<Integer> AGE_5 = IntProperty.of("age", 0, 5);
    public static final Property<Integer> AGE_7 = IntProperty.of("age", 0, 7);
    public static final Property<Integer> AGE_15 = IntProperty.of("age", 0, 15);
    public static final Property<Integer> AGE_25 = IntProperty.of("age", 0, 25);
    public static final Property<Integer> BITES = IntProperty.of("bites", 0, 6);
    public static final Property<Integer> DELAY = IntProperty.of("delay", 1, 4);
    public static final Property<Integer> DISTANCE_1_7 = IntProperty.of("distance", 1, 7);
    public static final Property<Integer> EGGS = IntProperty.of("eggs", 1, 4);
    public static final Property<Integer> HATCH = IntProperty.of("hatch", 0, 2);
    public static final Property<Integer> LAYERS = IntProperty.of("layers", 1, 8);
    public static final Property<Integer> LEVEL_3 = IntProperty.of("level", 0, 3);
    public static final Property<Integer> LEVEL_8 = IntProperty.of("level", 0, 8);
    public static final Property<Integer> LEVEL_1_8 = IntProperty.of("level", 1, 8);
    public static final Property<Integer> LEVEL_15 = IntProperty.of("level", 0, 15);
    public static final Property<Integer> HONEY_LEVEL = IntProperty.of("honey_level", 0, 5);
    public static final Property<Integer> MOISTURE = IntProperty.of("moisture", 0, 7);
    public static final Property<Integer> NOTE = IntProperty.of("note", 0, 24);
    public static final Property<Integer> PICKLES = IntProperty.of("pickles", 1, 4);
    public static final Property<Integer> POWER = IntProperty.of("power", 0, 15);
    public static final Property<Integer> STAGE = IntProperty.of("stage", 0, 1);
    public static final Property<Integer> DISTANCE_0_7 = IntProperty.of("distance", 0, 7);
    public static final Property<Integer> ROTATION = IntProperty.of("rotation", 0, 15);
    public static final Property<Direction> FACING = EnumProperty.of("facing", Direction.class);
    public static final Property<Direction> HORIZONTAL_FACING = EnumProperty.of("facing", Direction.class, Direction.Type.HORIZONTAL);
    public static final Property<Direction> HOPPER_FACING = EnumProperty.of("facing", Direction.class, direction -> direction != Direction.UP);
    public static final Property<Direction.Axis> HORIZONTAL_AXIS = EnumProperty.of("axis", Direction.Axis.class, Direction.Axis::isHorizontal);
    public static final Property<Direction.Axis> AXIS = EnumProperty.of("axis", Direction.Axis.class);
    public static final Property<SlabHalf> SLAB_TYPE = EnumProperty.of("type", SlabHalf.class);
    public static final Property<Half> HALF = EnumProperty.of("half", Half.class);
    public static final Property<Hinge> HINGE = EnumProperty.of("hinge", Hinge.class);
    public static final Property<BedHalf> BED_HALF = EnumProperty.of("bed_half", BedHalf.class);
    public static final Property<StairShape> STAIR_SHAPE = EnumProperty.of("shape", StairShape.class);
}