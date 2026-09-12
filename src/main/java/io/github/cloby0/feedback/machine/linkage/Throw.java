package io.github.cloby0.feedback.machine.linkage;

import net.minecraft.util.StringRepresentable;

/**
 * How far the crank throws, which is the only setting it has.
 *
 * <h2>Two options, deliberately</h2>
 * Philosophy 3: a crank throw that is short or long is a decision, while one that is any number
 * from 1 to 64 is a spreadsheet with a correct answer hidden in it.
 *
 * <h2>What a throw actually trades</h2>
 * A lever exchanges force for distance and the work per stroke is the same either way. So throw
 * does <em>not</em> change how fast a hammer flattens anything -- it changes only how hard each
 * blow lands, which matters when a material has a minimum force below which nothing happens.
 * <p>
 * That is why copper is the teaching material: it yields to almost nothing, so both throws work
 * identically and the player can install the wrong one and never find out. It is also why the
 * same part reads backwards on a bellows, where the useful output is displaced air rather than
 * work, and a long stroke moves more of it.
 */
public enum Throw implements StringRepresentable {

    SHORT("short"),
    LONG("long");

    private final String name;

    Throw(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
