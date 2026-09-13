/*
 * Feedback -- a Minecraft technology mod.
 * Copyright (C) 2026 soundgoodizerfan
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Assets under src/main/resources/assets are NOT covered by this licence.
 * See LICENSE-ASSETS.
 */
package io.github.cloby0.feedback.core;

/**
 * Every tunable number in the mod, in one place.
 *
 * <h2>Why this file exists</h2>
 * Almost none of these figures are defensible yet. Slice 1 fixes {@code Su} for a few machines
 * and never fixes a speed at all, and the philosophy is explicit that exact values are not
 * decided. They will stay fiction until the slice has been played.
 * <p>
 * So the point of centralising them is not tidiness, it is <em>turnaround</em>. Tuning should be
 * editing one list, not hunting through a dozen block entities. When the numbers start to mean
 * something, this becomes a config file; until then it is the single place a playtest note can
 * be applied without reading any code.
 * <p>
 * Anything here marked SLICE comes from feedback_slice_01.md. Everything else is invented.
 */
public final class FTuning {

    private FTuning() {
    }

    // --- sources --------------------------------------------------------------------------

    /** SLICE: the Hand Crank supplies 12 Su while somebody is turning it. */
    public static final float HAND_CRANK_CAPACITY_SU = 12f;
    /** Invented. */
    public static final float HAND_CRANK_RPM = 32f;
    /** Invented. How long one click keeps the crank turning; holding the button refreshes it. */
    public static final int HAND_CRANK_TICKS_PER_TURN = 12;

    /** SLICE: the Water Wheel supplies 256 Su, continuously, given flowing water. */
    public static final float WATER_WHEEL_CAPACITY_SU = 256f;
    /** Invented. Speed scales with how many faces have moving water against them. */
    public static final float WATER_WHEEL_RPM_PER_FLOW = 4f;
    /** Invented. Water does not change often enough to justify checking every tick. */
    public static final int WATER_WHEEL_FLOW_CHECK_INTERVAL = 20;

    // --- transmission ---------------------------------------------------------------------

    /**
     * Invented. Bearing friction per shaft, in Su per RPM.
     *
     * <h3>Why loss is Su, and why it scales with speed</h3>
     * Loss is Su rather than RPM because a rigid shaft turns at one speed along its whole length
     * -- a difference between its ends is torsion, not loss -- and what friction consumes is
     * torque. Because Su sums network-wide, loss is automatically distance-independent: moving a
     * machine nearer the generator saves nothing, since every bearing in the run turns either
     * way, including those on a branch nobody uses. Sprawl costs; distance does not.
     * <p>
     * Scaling with speed is what makes it interesting rather than a tax. Three things follow,
     * none of which had to be designed:
     * <ul>
     *   <li><b>A stopped shaft costs nothing.</b> An idle network is free, and an unpowered one
     *       is not "overstressed" -- it simply has no load.</li>
     *   <li><b>The network finds its own top speed.</b> It accelerates until surplus torque runs
     *       out, at {@code capacity = drag x rpm}. A long run does not hit a wall and refuse; it
     *       just turns more slowly, because friction ate the torque. This is deliberately unlike
     *       a hard per-shaft Su cap, which produces the absurdity of a generator being <em>too
     *       good</em> for its own shafting.</li>
     *   <li><b>Slow and wide becomes a real alternative to fast and narrow</b>, because speed is
     *       now something you pay for by the block.</li>
     * </ul>
     */
    public static final float SHAFT_DRAG_SU_PER_RPM = 0.1f;

    /**
     * Invented. How much a shaft resists a change in speed.
     *
     * <h3>How the inertia numbers were picked</h3>
     * Acceleration is net torque over moment of inertia -- {@code (capacity - load) / inertia}
     * RPM per tick -- which is the real relationship and not a fudge. That means the inertia
     * figures have to be calibrated against the Su figures, or momentum is invisible: at the
     * first values tried, a water wheel reached full speed in a single tick.
     * <p>
     * They are set so that a water wheel takes roughly two seconds to come up to speed and a
     * hand crank a little under one. The spread between them is large, and correctly so: a water
     * wheel is an enormous slab of timber and a crank handle is not.
     */
    public static final float SHAFT_INERTIA = 2f;

    /**
     * Invented. A cog drags like a shaft; a large one drags half again as much.
     *
     * <h3>What gearing costs</h3>
     * Nothing beyond this, and deliberately. Friction and mass are already referred through the
     * square of the gear ratio (see {@link io.github.cloby0.feedback.core.rotation.RotationNetwork}),
     * so a geared-up branch charges the network more simply by turning faster -- and a gear train
     * costs cogs, space, and the drag of every one of them. A real gearbox takes no continuous
     * payment for the force it gives either; what it takes is capital. Adding a surcharge on top
     * would be a rule saying what the physics already says.
     */
    public static final float SMALL_COG_DRAG_SU_PER_RPM = 0.1f;
    public static final float LARGE_COG_DRAG_SU_PER_RPM = 0.15f;

    /** Invented. A large cog is a heavier wheel, and the only flywheel beat 1 has. */
    public static final float SMALL_COG_INERTIA = 3f;
    public static final float LARGE_COG_INERTIA = 12f;

    /** Invented. A gearbox is a crowded little box of bevels: more friction than a bare shaft. */
    public static final float GEARBOX_DRAG_SU_PER_RPM = 0.2f;
    public static final float GEARBOX_INERTIA = 4f;

    /** Invented. A generator is a lump of mass too; without this a bare source has no momentum. */
    public static final float HAND_CRANK_INERTIA = 4f;
    /** Invented, and deliberately large -- this is why a water wheel coasts so visibly. */
    public static final float WATER_WHEEL_INERTIA = 400f;

    /**
     * Floor on total network inertia, so a network of one weightless block still takes a moment
     * to change speed instead of snapping. Prevents a divide-by-zero as well.
     */
    public static final float MINIMUM_INERTIA = 1f;

    /**
     * Global multiplier on how quickly anything changes speed. 1 means the physics above is used
     * as written.
     * <p>
     * Raise it and machines feel light and twitchy; lower it and everything runs as though in
     * oil. This is the first number to reach for when the mod feels wrong but nothing specific
     * is wrong with it.
     */
    public static final float INERTIA_RESPONSE = 1f;

    /**
     * Floor on braking force, so an utterly frictionless network still eventually stops rather
     * than coasting for the rest of the save.
     */
    public static final float MINIMUM_BRAKING_SU = 0.5f;

    // --- reciprocation --------------------------------------------------------------------

    /**
     * Invented. Strokes delivered per RPM per tick, so a water wheel at 8 RPM lands about one
     * blow a second and a hand crank at 32 RPM about four.
     */
    public static final float STROKES_PER_RPM_PER_TICK = 0.00625f;

    /**
     * SLICE: the Mechanical Hammer delivers 12 St on a short throw and 3 on a long one.
     * <p>
     * Note there is deliberately no matching pair for work. A lever trades force against
     * distance and the work per stroke is the same either way, so throw changes how hard a blow
     * lands and not how fast the job goes. That is exactly why copper -- which yields to almost
     * nothing -- lets a player install the wrong crank and never find out.
     */
    /**
     * SLICE: a hand hammer swings at about 3 St.
     *
     * <p>Against copper's hardness of 1 that is 3 Fu a swing, so a 14 Fu plate is five swings and
     * the sixth starts making foil. It is deliberately the same figure as the Mechanical Hammer's
     * long throw: the gentle machine is doing exactly what the player's arm was doing, only without
     * ever getting bored, which is the whole of what beat 1 has to say.
     *
     * <p>It is also what keeps steel out of reach by hand with no rule about hands anywhere. Steel's
     * hardness is 15, and 3 St below a hardness of 15 lands nothing at all -- philosophy 7's hard
     * gate, a genuine impossibility rather than a slow route.
     */
    public static final float HAND_HAMMER_ST = 3f;

    /**
     * Invented. Blows a hand hammer lands before it is finished.
     *
     * <p>Sized as a working figure rather than a balance lever: 250 swings is fifty copper plates,
     * which is plenty to bootstrap a first Mechanical Hammer and nowhere near enough to want to
     * keep doing it. Philosophy 7 promises manual production stays <em>possible</em>, not that it
     * stays free.
     */
    public static final int HAND_HAMMER_DURABILITY = 250;

    public static final float HAMMER_ST_SHORT = 12f;
    public static final float HAMMER_ST_LONG = 3f;

    /** SLICE: 80 Su, against the water wheel's 256. Three hammers fit; four do not. */
    public static final float HAMMER_LOAD_SU = 80f;

    /**
     * Invented. The most St this hammer can land, however it is geared.
     *
     * <h3>Why a ceiling exists</h3>
     * Gearing down multiplies force per blow without limit, so without a ceiling one long gear
     * train makes every hammer in the game equivalent and the force axis collapses into how many
     * cogs somebody was willing to place. A paper blade at a million RPM still will not cut steel:
     * what a machine can deliver is a property of its construction, not of its drive.
     * <p>
     * Twice the short throw, so gearing buys exactly one genuine doubling past the strongest
     * setting the block has -- enough for the trade to be worth making, not enough to replace
     * buying a better machine. Raising the ceiling is what a better machine is <em>for</em>.
     */
    public static final float HAMMER_MAX_ST = 24f;

    /**
     * How much condition one St of wasted force costs the machine that absorbed it.
     *
     * <h3>What wear is for here, and what it deliberately is not</h3>
     * It is not maintenance. GregTech charges a flat chance of a fault per hour a machine runs,
     * which is a tax on uptime and says nothing about whether the factory was built well --
     * looked at and rejected for exactly that. Wear here is caused <em>only</em> by misuse, so a
     * correctly built line never accrues any of it, ever. A worn hammer is therefore not an
     * upkeep bill; it is <em>evidence</em>, and the thing it is evidence of is the mistake.
     * <p>
     * The rule it implements is one sentence: force that cannot go into the work goes into the
     * machine. That covers all three ways a blow can accomplish nothing -- the workpiece is too
     * cold to move, the blow is under the material's hardness floor, or the drive is geared past
     * what the hammer's construction can take -- without a separate rule for any of them. Before
     * this, the first two were silently ignored and the third was a silent clamp, which meant the
     * three most instructive mistakes in beat 1 were the three the game said nothing about.
     *
     * <h3>Where the figure comes from</h3>
     * Condition falls from 1 to {@link #HAMMER_CONDITION_FLOOR} after about 600 wasted short-throw
     * blows -- {@code 0.5 / (600 x 12 St)}. A water wheel lands roughly a blow a second, so that
     * is ten minutes of a hammer beating metal that went cold. Long enough that a player who
     * notices and fixes it pays almost nothing, short enough that walking away from a broken setup
     * costs the hammer.
     */
    public static final float HAMMER_WEAR_PER_ST = 0.00007f;

    /**
     * The worst condition a hammer can reach. It never breaks and never stops.
     *
     * <h3>Why it degrades instead of failing</h3>
     * Philosophy 7: a hard gate is a physical impossibility, and a machine refusing to run is one
     * bolted on where none exists. A battered head still hits, it just delivers less of the blow
     * into the work -- so the failure is soft, gradual, and material-dependent. Copper carries on
     * yielding to a spent hammer; steel stops clearing its hardness floor and the line quietly
     * stops producing. Which material notices first is the material's business, which is the same
     * answer this mod gives everywhere else.
     * <p>
     * Half, so a fully spent hammer is worth exactly half a hammer -- bad enough to be the reason
     * a build stopped working, never bad enough to be unrecoverable.
     */
    public static final float HAMMER_CONDITION_FLOOR = 0.5f;

    /**
     * Bottoms of the condition bands, descending -- how battered a machine looks, in words.
     *
     * <h3>Why this is free, and has no instrument behind it</h3>
     * Damage to a machine is visible in a way its temperature is not. A mushroomed hammer head is
     * something you can see from across the room, so charging an instrument for it would be
     * hiding a free sense (§8). What the adjective withholds is the figure -- calipers exist for
     * that -- and the top band is silent entirely, so a sound machine adds no line to the HUD.
     */
    public static final float CONDITION_SOUND = 0.98f;
    public static final float CONDITION_MARKED = 0.85f;
    public static final float CONDITION_BATTERED = 0.65f;

    /**
     * Which condition band a figure falls in -- 0 sound, rising to 3 spent.
     * <p>
     * Lives here rather than in the readout because the server needs it too: wear happens blow by
     * blow and syncing every one of them would be a packet a tick for a line that changes four
     * times in the life of the machine. The server syncs when the <em>band</em> moves.
     */
    public static int conditionBand(float condition) {
        if (condition >= CONDITION_SOUND)
            return 0;
        if (condition >= CONDITION_MARKED)
            return 1;
        if (condition >= CONDITION_BATTERED)
            return 2;
        return 3;
    }

    // Work per blow is deliberately NOT a figure here. It is force divided by the material's
    // hardness -- see Deformation. A machine states the force it can deliver; what that
    // accomplishes is the material's business.

    /**
     * Below this, a coasting network is called stopped. Without a floor, speed approaches zero
     * asymptotically and machines tick forever at 0.0001 RPM.
     */
    public static final float STOPPED_RPM_THRESHOLD = 0.05f;

    // --- thermal ---------------------------------------------------------------------------

    /**
     * Invented. The temperature everything decays towards, in Tu.
     *
     * <h3>Why one figure and not a biome lookup</h3>
     * A desert crucible holding two degrees hotter than a taiga one is a variable the player
     * cannot act on -- they are not going to move the factory -- and philosophy 8 is explicit
     * that a variable nobody can act on is not worth modelling. One number, everywhere.
     */
    public static final float AMBIENT_TU = 20f;

    /**
     * How fast a loose workpiece sheds heat, in Tu per tick, cooling in a straight line.
     *
     * <h3>What this number actually sets</h3>
     * It sets the size of the player's workshop. A steel ingot leaves the crucible around 1450 Tu
     * and hot working stops below 900 Tu, so the walk from fire to anvil is
     * {@code 550 / rate} ticks -- about fourteen seconds here. Halve the rate and placement stops
     * mattering; double it and the hammer has to be bolted to the crucible.
     * <p>
     * Straight-line rather than exponential, which was the first version. See
     * {@link io.github.cloby0.feedback.core.thermal.Heat#cooled} for the argument -- briefly, an
     * exponential never arrives, needs an arbitrary floor to stop it, and hides the whole working
     * window in a flat tail. A constant rate makes "you have fourteen seconds" literally true,
     * which is what the player is budgeting against.
     * <p>
     * Deliberately one figure for every item rather than per-material. Specific heat is real and
     * TerraFirmaCraft gives every item its own; the test here is whether it creates a choice, and
     * with one hot material in the slice it does not. It becomes worth having the moment a player
     * must decide <em>which</em> of two hot things to carry first.
     */
    public static final float ITEM_COOLING_TU_PER_TICK = 1.5f;

    /**
     * Invented. Below this, a workpiece is close enough to ambient to stop being described as hot,
     * and its heat components are dropped.
     *
     * <h3>Why a threshold survived the switch to linear cooling</h3>
     * It is no longer load-bearing -- straight-line cooling reaches ambient exactly, so nothing
     * would linger forever. It stays because components are part of a stack's identity: an ingot
     * at 21 Tu and one at exactly 20 Tu are different items and will not merge, so the player's
     * chest fills with piles of one. Clearing slightly early makes a cooled ingot byte-identical
     * to one that was never heated.
     */
    public static final float WARM_TU = 60f;

    /**
     * Invented. How readily heat crosses from a fire into the vessel above it, in Work per tick
     * per Tu of difference.
     *
     * <h3>Why heat flows on a difference rather than at a flat rate</h3>
     * A flat {@code Work/t} was the first thing tried and it is wrong in a way that matters: with
     * it, a vessel's final temperature is {@code fire output / leak}, so insulating a crucible
     * raises the temperature it settles at <em>without limit</em>. Insulation became a trap
     * rather than an upgrade, which contradicts the slice.
     * <p>
     * Driving the flow on {@code (fire - vessel)} fixes it the way the real relationship does: a
     * vessel approaches its fire's temperature and can never pass it. Insulation then does
     * exactly what it should -- it gets you closer to the flame and holds you there, and the only
     * way past the flame is a hotter flame.
     * <p>
     * It also pays for the slice's lava for free. Lava is a fire fixed at 1200 Tu, so a crucible
     * over lava sits just under 1200 Tu forever: perfectly stable, and permanently too cool for
     * steel. No rule had to be written for that.
     */
    public static final float FIRE_CONDUCTANCE = 0.6f;

    /**
     * Invented. How fast a vessel bleeds heat to the room, in Work per tick per Tu above ambient.
     * One figure for every vessel: what differs between a small crucible and a large one is mass,
     * not surface.
     */
    public static final float VESSEL_LEAK = 0.025f;

    /**
     * Invented. Work required to raise a vessel by one Tu -- its thermal mass.
     *
     * <h3>The two figures are the whole of "upgrades are physical"</h3>
     * Mass sets the time constant, {@code mass / (conductance + leak)}: 40 ticks for the small
     * crucible and 400 for the large. Everything the slice claims about the two follows from
     * that one number and nothing else had to be written:
     * <ul>
     *   <li>The small one reaches its fire's temperature in about two seconds. Inside steel's
     *       window on a full fire it is still climbing at <b>7.9 Tu/t</b>, past carburizing's
     *       5 Tu/t limit, so every stroke of the bellows resets its hold and it cannot carburize
     *       on a full draught at all. The rate limit is not a balance rule bolted on; it is what
     *       makes a bigger vessel necessary. Note what it is <em>not</em>: a lock. A small
     *       crucible on a carefully geared, gentle draught still works, which is the standing rule
     *       that precision never gates hard (§7).</li>
     *   <li>The large one climbs at <b>0.79 Tu/t</b> there, so it cannot breach the limit however
     *       it is fired, and a 20-tick-old thermometer reading is still worth acting on -- it will
     *       have moved 16 Tu, comfortably inside steel's 60 Tu window. The same loop on the small
     *       crucible swings 159 Tu and peaks at 1599, <b>past the 1540 Tu at which iron burns</b>.
     *       That is the slice's thermostat bug, and it is a burnt batch rather than a wobble.</li>
     * </ul>
     * Neither is better. A process that needs to <em>move</em> would much rather have the small
     * one.
     */
    public static final float CRUCIBLE_SMALL_MASS = 25f;
    public static final float CRUCIBLE_LARGE_MASS = 250f;

    /** Invented. How many items a crucible holds. Two, because carburizing needs iron and carbon. */
    public static final int CRUCIBLE_SLOTS = 2;

    /**
     * Invented. What one block of insulation packed against a vessel multiplies its leak by, and
     * how many are counted.
     *
     * <h3>Why this is a block and not an upgrade slot</h3>
     * Philosophy 4: an upgrade is a physical component you could point at. Insulation is the most
     * literal case in the mod -- you build it around the thing, and how well it works depends on
     * how much of the thing you covered. Six faces, of which one is the fire and one the lid, so
     * four is the practical maximum and the cap is where the geometry already put it.
     */
    public static final float INSULATION_LEAK_FACTOR = 0.8f;
    public static final int INSULATION_MAX_BLOCKS = 4;

    /**
     * What a full draught of air multiplies a fuel's flame temperature by.
     *
     * <h3>Air multiplies, it does not replace</h3>
     * A cool fuel blown hard is still a cool fire, which is both true and necessary: a bellows
     * that set an absolute temperature would make every fuel identical and the fuel table would
     * stop meaning anything. What air sells is a <em>proportion</em> more out of whatever is
     * already burning.
     *
     * <h3>This factor is beat 2's difficulty</h3>
     * Charcoal burns at 1220 Tu (see the {@code fuel} table), which is glowing, obviously fierce,
     * and <b>below steel's 1420 Tu window</b>. So the fire alone cannot make steel and no amount
     * of patience changes that -- philosophy 7's hard gate, a genuine impossibility rather than a
     * slow version. Air is the only way across, and air is the only thing the bellows sells.
     * <p>
     * Fully blown, charcoal reaches about 1830 Tu, which is well past the 1540 Tu at which iron
     * burns. That asymmetry is deliberate (§6): the player's one actuator points at a temperature
     * that destroys the work, so holding the window means knowing when to <em>stop</em> asking,
     * which is the entire beat.
     */
    public static final float FULL_AIR_TEMPERATURE_FACTOR = 1.5f;

    /**
     * SLICE: lava is 1200 Tu and it does not cool. Not slowly. At all.
     *
     * <h3>No explanation is offered, deliberately</h3>
     * Philosophy 2 says the fantastical cannot be a footnote, and in this slice it lives in the
     * most mundane place available -- the fuel. A bucket of lava under a crucible is an infinite,
     * perfectly stable heat source that is <b>too cool for steel and impossible to turn off</b>,
     * and the player has walked past it since day one without asking. There is no lore entry and
     * no analysis machine. There is a player who now owns a thermometer and has started pointing
     * it at things, which is the correct first step of §2's observe-then-exploit and the whole of
     * somebody else's slice.
     * <p>
     * Sat deliberately between a bare charcoal fire and the steel window, so it is genuinely
     * useful and genuinely not enough.
     */
    public static final float FIRE_TU_LAVA = 1200f;

    /**
     * Invented. How much air the firebox can hold, and how fast it consumes it.
     *
     * <h3>Why a buffer exists</h3>
     * Strokes arrive in lumps and a fire does not flare and die between them. A small buffer
     * smooths the gap; a large one would let the player bank air and blast on demand, which turns
     * a continuous actuator into a battery and removes the reason to keep the bellows running.
     * Ten ticks of full burn is enough for the first and not the second.
     */
    public static final float FIREBOX_MAX_AIR = 40f;
    public static final float FIREBOX_AIR_PER_TICK = 4f;

    // --- the bellows ------------------------------------------------------------------------

    /**
     * SLICE: 10 air on a short throw, 30 on a long one.
     *
     * <h3>The same part, meaning the opposite thing</h3>
     * On the hammer the short throw is the strong option, because a lever trades distance for
     * force. A bellows wants displaced air rather than force, so the trade runs the other way and
     * the <em>long</em> throw is the useful one. The player has met this component already and
     * finds it inverted, which is the first time in the slice that one part composes two ways.
     * <p>
     * Sustaining a full fire takes {@link #FIREBOX_AIR_PER_TICK} air a tick, which is a long
     * throw at about 21 RPM -- or a short throw at 64 RPM, which beat 1 cannot reach. So the
     * wrong throw is not merely worse here, it cannot do the job at all.
     */
    public static final float BELLOWS_AIR_SHORT = 10f;
    public static final float BELLOWS_AIR_LONG = 30f;

    /**
     * SLICE: 40 Su, half a hammer.
     * <p>
     * Which is what makes the convergence tight. Beat 1's water wheel carries 256 Su; a hammer
     * fast enough to forge steel, a bellows keeping the fire in, and the shafting to reach both
     * is the first time the slice asks for most of that budget at once.
     */
    public static final float BELLOWS_LOAD_SU = 40f;

    /**
     * The most air one stroke can move, which is the long throw's figure and deliberately so.
     *
     * <h3>Gearing buys nothing here, and that is the physics</h3>
     * {@link io.github.cloby0.feedback.machine.linkage.CrankLinkageBlockEntity} multiplies a
     * machine's stated force by the gear advantage and then clamps to its ceiling. Setting the
     * ceiling <em>at</em> the long throw makes that clamp bite immediately, so no gear train ever
     * moves more air per stroke -- which is correct rather than a restriction. A bellows holds
     * what it holds; squeezing it harder does not find more air inside it.
     * <p>
     * So on the hammer gearing buys force, and on the bellows it buys only stroke rate. One
     * mechanism, two honest answers, because the ceiling is a property of the machine.
     */
    public static final float BELLOWS_MAX_AIR = BELLOWS_AIR_LONG;

    /**
     * Invented. The temperature at which the slice's bimetallic strip snaps over, in Tu.
     *
     * <h3>One figure, on the part, not on a dial</h3>
     * Philosophy 3 and 13: a strip is a piece of bent metal that trips where its two metals say
     * it trips. It has no setting, and wanting a different trip point means crafting a different
     * strip -- which is how this family grows and why the controller in slice 2 is a genuine
     * upgrade rather than a convenience.
     * <p>
     * Sat a third of the way up steel's 1420-1480 window rather than at either end, and the
     * figure was arrived at by simulating the loop rather than by taste. A large crucible under
     * full air climbs at 0.79 Tu/t and the strip reads every 20 ticks, so the loop oscillates
     * about 16 Tu either side of its trip point: 1440 gives 1424 to 1456, which fits. At 1425 the
     * bottom of the swing falls out of the window and the hold stalls for half of every cycle; at
     * 1470 the top of it does, and on a small crucible the top of the swing is 1599 and burns the
     * batch outright.
     */
    public static final float BIMETALLIC_TRIP_TU = 1440f;

    /**
     * SLICE: the crude thermometer resolves to 25 Tu.
     *
     * <h3>The number is chosen to be not quite enough</h3>
     * Steel's window is 60 Tu wide, so this splits it into two and a bit -- the player can tell
     * they are roughly in it and never exactly where. That is the point. An instrument that made
     * the window trivial would end the beat on purchase, and philosophy 8 is explicit that better
     * equipment narrows the distribution and never collapses it. A later thermometer buys
     * significant figures here and nothing else; it is one number.
     */
    public static final float THERMOMETER_RESOLUTION_TU = 25f;

    // --- readouts, thermal ------------------------------------------------------------------

    /**
     * Above this, the eye stops distinguishing anything, in Tu.
     *
     * <h3>A free sense has a range, like any other instrument</h3>
     * Philosophy 8 gives instruments six properties and range is the first of them, so it would be
     * strange for the one apparatus every player owns to have none. Past brilliant white, hotter
     * simply looks the same -- which matters in beat 2, because iron's spoil point at 1540 Tu is
     * <em>above</em> this. The player cannot see themselves crossing the line that ruins the
     * batch. That is not a trick; it is why the thermometer exists, and it is the difference
     * between a mod that hides a number and one that models why you cannot have it.
     */
    public static final float MAX_VISIBLE_TU = 1600f;

    /**
     * Where the free, qualitative temperature bands fall, in Tu -- each entry is the <em>top</em>
     * of a band, and {@link io.github.cloby0.feedback.client.Readout} names them in order.
     *
     * <h3>Why they read as colours, and why there are this many</h3>
     * These are the bands a blacksmith actually works to, and that is not decoration -- it is the
     * honest answer to what a player's own senses give them. You cannot feel 1450 Tu; you can see
     * that iron has gone from bright red to orange, and you can be badly wrong about which.
     * <p>
     * The first version had six wide bands and steel's entire 60 Tu window sat inside one of them,
     * which made the free sense useless rather than coarse. TerraFirmaCraft uses eleven, and the
     * reason is worth stating: the bands should be fine enough that a player can <em>get close</em>
     * by eye and never fine enough to land a 60 Tu window reliably. That is the gap the
     * thermometer is sold into, and a gap that is merely "hot or not" is not a gap, it is a wall.
     * So the bands crowd where the work happens and are wide where nothing does.
     */
    public static final float[] HEAT_BAND_TOPS = {
            200f,    // warm
            480f,    // hot
            580f,    // faint red
            730f,    // dark red
            930f,    // bright red
            1100f,   // orange
            1300f,   // yellow
            1400f,   // pale yellow
            1500f,   // white
            MAX_VISIBLE_TU
    };


    // --- control --------------------------------------------------------------------------

    /**
     * How long one wind of the Timer lasts, in ticks.
     * <p>
     * A short list of positions rather than a free number (§3). There is no correct value hiding
     * in here to be ground out -- the run being timed is not repeatable enough for one to exist
     * -- but a device with a scalar on it teaches players to hunt for one anyway, and that is a
     * habit worth not teaching.
     * <p>
     * The range is set against beat 1: a water wheel lands roughly a blow a second, and a plate
     * is five blows on the gentle crank. So the useful settings sit either side of that, and the
     * long ones exist mostly to demonstrate what overrun looks like.
     */
    public static final int[] TIMER_SETTINGS = { 40, 100, 200, 400 };

    // --- readouts -------------------------------------------------------------------------

    // Where the free, qualitative speed bands fall. Philosophy 8 says a player standing next to
    // a shaft can see roughly how fast it is going and nothing more precise than that, so these
    // are the only speed figures the game exposes without an instrument -- as words.
    //
    // The boundaries are set against what beat 1 can actually produce: a water wheel makes 4 RPM
    // per flowing side (4 to 16), a hand crank 32. So a one-sided wheel reads slow, a well sited
    // one reads as turning, and only the crank spins fast. Bands that all collapsed onto one
    // adjective would tell the player nothing.

    /** Below this a turning run reads as "turning slowly". */
    public static final float TURNING_RPM = 8f;
    /** At or above this a run reads as "spinning fast". */
    public static final float SPINNING_FAST_RPM = 24f;

    /**
     * Fraction of capacity above which a network reads as "straining".
     *
     * <h3>Why a warning band exists at all</h3>
     * Acceleration is surplus torque over inertia, so a network at 98% of capacity is not
     * slightly worse than one at 80% -- it takes eighty seconds to reach speed instead of two.
     * That is a real and useful behaviour, and it was completely invisible: the factory simply
     * felt broken. An adjective is the right granularity to fix that with, because it says
     * <em>where to look</em> without handing over the Su figures that calipers are for.
     */
    public static final float STRAINING_LOAD_FRACTION = 0.9f;
}
