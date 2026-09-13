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

/**
 * The units of philosophy 17, as types the compiler can tell apart.
 *
 * <h2>What this package is for</h2>
 * Every physical figure in the mod was a {@code float}, and by the time the thermal model landed
 * that had become a real hazard rather than a tidiness complaint. A fire's temperature, a vessel's
 * thermal mass and its leak are three different kinds of thing, {@link Heat#tick} handles all
 * three in five lines, and nothing stopped any of them being passed where another belonged. The
 * mistake would not have been caught by a test, because the wrong number still produces a
 * plausible-looking temperature curve -- it would have been caught by a playtest, weeks later,
 * as "the crucible feels wrong".
 *
 * <h2>What a unit type does and deliberately does not do</h2>
 * Each type is a thin wrapper with a single accessor, <em>named for its dimension</em>. That
 * naming is the whole mechanism: {@link ThermalMass#workPerTu()} and
 * {@link Conductance#workPerTickPerTu()} are different methods on different types, so swapping a
 * mass for a leak is a compile error rather than a slow bug.
 * <p>
 * There is deliberately <b>no arithmetic on these types</b>. The alternative was considered and
 * rejected: an algebra of {@code Tu.minus}, {@code Work.over(ThermalMass)} and so on would catch
 * dimensional errors in intermediate expressions too, but it would cost six invented operators
 * and a fourth type to express five lines of physics, and it would stop {@link Heat#tick} looking
 * like the equation it is. That file's javadoc is the standing record of a physics bug that was
 * shipped once; if the maths there stops being readable, the documentation stops being checkable.
 * So the types guard the <em>plumbing</em> -- interfaces, tuning constants, the seams between
 * systems, which is where a value gets handed to the wrong parameter -- and the arithmetic is
 * done in plain floats, unwrapped at the point of use.
 * <p>
 * The convention that follows, and it is a convention rather than something the compiler enforces:
 * <b>unwrap inline, never into a local.</b> {@code body.getLeak().workPerTickPerTu()} inside an
 * expression is safe; {@code float leak = ...; float mass = ...;} on two lines puts the two back
 * within swapping distance of each other.
 *
 * <h2>How these are named</h2>
 * Philosophy 17 gives some quantities a unit and leaves others as compounds, and the names here
 * follow that split:
 * <ul>
 *   <li>A quantity with a named unit takes the unit's name -- {@link Tu}. Rates derived from one
 *       take the unit plus the rate, as the units table does when it writes {@code Tu/t} --
 *       {@link TuRate}.</li>
 *   <li>A compound with no unit of its own takes the name of its physical role, with the
 *       dimension stated in its javadoc -- {@link ThermalMass} is {@code Work/Tu},
 *       {@link Conductance} is {@code Work/t/Tu}.</li>
 * </ul>
 *
 * <h2>Two things this exercise found, which is the argument for having done it</h2>
 * <ol>
 *   <li><b>A vessel's leak and a fire's conductance are the same dimension.</b> Both are
 *       {@code Work/t/Tu} -- Work crossing a boundary per tick, per Tu of difference across it --
 *       and {@link Heat#equilibrium} has been adding them together correctly all along without
 *       anything in the code saying they were addable. They are now one type, {@link Conductance},
 *       playing two roles. Naming a leak as a conductance to the room is also just true.</li>
 *   <li><b>{@link Tu} covers both a temperature and a difference between two temperatures.</b>
 *       Strictly those are different things -- 1450 Tu and "16 Tu hotter" are a state and an
 *       interval -- and a stricter scheme would separate them. It is not worth a fifth type here:
 *       the only interval in the thermal model is an instrument's resolution, the arithmetic that
 *       would confuse the two is float arithmetic anyway, and the mod's own units table treats Tu
 *       as one thing. Recorded because it is the first place this scheme is knowingly loose, and
 *       the next person will otherwise re-derive it.</li>
 * </ol>
 *
 * <h2>Why there is no {@code Work} type yet</h2>
 * Nothing stores a quantity of Work. It appears only inside the dimensions of the two compounds
 * above, and as intermediate float terms within {@link Heat#tick} that never leave the method.
 * A type with no variable to hold would be scaffolding, and the standing rule is to create a
 * thing when there is something to put in it.
 *
 * <h2>Scope</h2>
 * Thermal only, on purpose. Rotation's {@code Su}, {@code RPM}, {@code Fu} and {@code St} are
 * still floats and are the next pass; the datapack records ({@code ThermalProcess},
 * {@code Quench}, {@code Deformation}) are still floats too, and their codecs are the reason --
 * retyping them means retyping wire formats, which is a separate change with a separate risk.
 * The dangerous triple the exercise was called for -- temperature, thermal mass, leak -- is
 * entirely inside the scope taken here.
 */
package io.github.cloby0.feedback.core.unit;

import io.github.cloby0.feedback.core.thermal.Heat;
