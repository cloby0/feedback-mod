# Feedback — TODO

Working checklist. Source of truth for *what's next*; `feedback_philosophy.md` stays source of truth for *why*.

---

## 0. Housekeeping

- [x] Commit `feedback_philosophy.md` + `feedback_slice_01.md` (`7cafb5e`)
- [x] Fix stale items in `CLAUDE.md` "Outstanding work"; added the three new hard rules (discrete options, two-answer problems, workpiece state)

---

## 1. Decide first: build or spec

Slice 1 is fully drafted and every open decision in it is closed. Two ways forward, and this choice gates everything below.

- **Build slice 1.** Philosophy §20 argues emergent behavior can't be validated on paper. Slice was scoped small enough to implement (12 items, 7 machines, 2 process variables). Finds the problems no document will.
- **Write document 2 first.** Safer, but doc 2 written without a running slice means inventing numbers with nothing to test them against.

Recommendation: build. Write doc 2 from what the build forces you to decide.

**Decided: build.** Scaffold is done; next is beat 1 — Hand Crank, Water Wheel, Shaft, Crank Linkage, Mechanical Hammer, and the plate -> foil -> scrap overrun chain.

---

## 2. Project scaffold

Platform decided: **NeoForge, Minecraft 1.21.1.**

- [x] **ModDevGradle**, not NeoGradle — new mod, one MC version, Gradle config cache. NeoGradle only buys multi-version support
- [x] Package structure — root `io.github.cloby0.feedback`, tree follows the mod's separation of systems (see `CLAUDE.md` "Source layout")
- [x] Gradle scaffold, run configs, Parchment `2024.11.17`, `build-and-deploy.sh` to the PrismLauncher instance. NeoForge `21.1.250`, MDG `2.0.147`, Gradle `9.2.1`
- [ ] Decide data-driven recipe format — philosophy §15 wants compat authored as a table, not code

### Library decisions

Dev environment currently loads **JEI**, **PonderLib** and **Flywheel** (transitive via Ponder) out of `run/mods`, synced by the `syncDevMods` Gradle task. None are shipped dependencies yet.

- [x] **JEI** — dev only. Our machines have no recipe list, so there is little to register; it is in the dev env to find out what a recipe browser does with a recipe-less mod
- [x] **PonderLib** — the designated explanation channel (§8, *Documentation is a free sense*). Scenes teach verbs, never values or solutions. Promote to a real dependency when the first scene is written
- [x] **Flywheel** — **yes, will be a real dependency.** Instanced rendering; without it every spinning shaft is its own draw call. Create's `SingleAxisRotatingVisual` / `RotatingInstance` are the reference (MIT)
- [x] **GeckoLib** — **no.** It plays authored keyframe animations; our motion must be procedural, because a machine's visible state is a readout the player reasons from. A fixed-length animation decouples the visual from the mechanics, and a machine that visually lies contradicts §8
- [x] **Cloth Config** — **no.** NeoForge's built-in config is sufficient, and our config surface should stay small: numbers here are design decisions, not user preferences
- [x] **Registrate** — **no.** Saves registration boilerplate but hides what registration says. Legibility beats brevity on this project
- [ ] **Jade / TheOneProbe** — *probably yes, done carefully.* A HUD that prints `1247 Tu` on a bare crucible gives away the thermometer and guts §8. One that shows *"really hot!!!"* until an instrument is installed, and figures after, is the adjectives rule on a HUD. Design it, don't just enable it
- [x] **Mixin** — ships with NeoForge. The route for reworking the vanilla furnace/smoker/blast furnace (§15) without replacing the blocks

Also decided in passing: **`src/generated/resources` is committed.** Datagen output is reviewable, and a diff on it is the cheapest way to see what a registry change actually did.

---

## 3. Document 2 — mechanics

Not started. Philosophy defers to it **by name** in these places; each is a debt.

**Units & arithmetic**
- [ ] Exact ranges and arithmetic for every unit (§17)
- [ ] `Eu` — how voltage and current behave across a wire (§17)
- [ ] `Su` load figures; linkage `St` pairs per machine (§17)

**Thermal**
- [ ] Thermal model: `Tu` as state, `Work` delivery, thermal mass, ambient, insulation (§8, §9)
- [ ] Hot workpiece cooling rate; when a `Hot Steel Ingot` reverts (§9)
- [ ] Vanilla vessel bands — smoker ceiling, Crude Blast Furnace floor, plain furnace span (§15)
- [ ] Fallback smelting: 1/8-coal reference constant, cook-time scaling (§15)

**Instruments & control**
- [ ] The six apparatus properties as numbers — range, resolution, accuracy, control, response, stability (§8)
- [ ] Drift and recalibration mechanism (§8, §9)
- [ ] **[OPEN]** Hysteresis needs the controller to read its own output state back. Cleanest: actuator state is just another readable source. Confirm — without it a deadband can't be expressed and relay flapping stops being the player's fault (§13)
- [ ] Data links: 16-block range, client-drawn cable, cable item cost, no path check (§13)

**Process**
- [ ] Per-energy sensor/actuator pairings, full matrix (§10)
- [ ] Overrun band widths per machine and per material (§6)
- [ ] Noise model — irreducible floor, learnable-in-aggregate, distribution narrows but never collapses (§8)

---

## 3b. Beat 1 — in progress

Built and compiling: rotation engine (`RotationNode` / `RotationNetwork` / `RotationPropagator`), Shaft, Hand Crank, Water Wheel, and the three copper overrun items. Placeholder art throughout (vanilla textures).

- [x] **Mechanical Hammer** — 80 Su, `12 / 3 St`, one blow per stroke
- [x] **Crank Linkage** — short/long throw, swapped by right-clicking it; rotation to reciprocation
- [x] **The `Fu` process** — 30 Fu to a plate, then plate → foil → scrap, as a datapack table
- [x] **Workpiece progress is visible, in adjectives** — "Barely marked" → "Taking shape" → "Visibly worked" → "Nearly there". No figures: that is what calipers are for. The exact `Fu` is on the stack and deliberately not shown
- [ ] **Workpiece should be visible in the machine too** — a block entity renderer showing what is on the anvil. The tooltip covers the lesson, but you currently cannot see that the hammer is occupied without clicking it
- [ ] **Timer** — the slice's only automation before instruments
- [ ] **Calipers** — `16 / 20 Fu`, and deliberately *after* the timer
- [ ] Flywheel-based rendering for spinning shafts; currently the model does not visibly turn
- [ ] Real textures
- [x] **Shaft placement QoL** — clicking a shaft while holding a shaft extends the run along its axis, the way Create does. Clicking an end face grows that way; clicking a side grows away from the player. Sneak to suppress it and place normally
- [ ] **Delete the temporary debug readout** (`RotationNode#debugReport`, sneak-right-click). It hands out exact figures with no instrument, which is §8 backwards. It exists only because nothing visibly turns yet

### Slice discrepancies found while building

- **"Thin plate" is in the chain but not in the roster.** `feedback_slice_01.md` writes the overrun as `ingot → plate → thin plate → foil → scrap`, but the 12-item list has only Plate, Foil and Scrap. Implemented as three stages. Either add the item or fix the line.
- **"About 8 ticks, there is a plate" does not survive a coherent stroke model.** One blow per revolution puts a plate at ~30 ticks on a hand crank and ~120 on an 8 RPM water wheel. 8 ticks would need either enormous speeds or a blow that does the whole job. The slower figures are implemented; the slice line should probably change.
- **`St` and `Fu` were the same number twice, and are now related by material hardness.** Nothing is strong enough to dent steel yet fails to slam copper, so work per blow cannot be a machine stat. It is `St ÷ hardness`, and hardness does both jobs: the floor below which nothing lands, and the divisor for how much of a bigger blow gets through. One property per material instead of two.
- **Throw is power versus precision, and neither dominates.** An earlier model had throw change only `St`, which made the short throw strictly better — a dominated option, which is worse than no option. Now a hard blow on a soft material *overshoots*: short throw reaches a plate in 3 blows and ruins it by blow 5, long throw takes 10 and gives 7 blows of slack. The fast way is the one that is hard to stop in time, which is the mod's own thesis. Long throw is also what the bellows wants, so the component still reads two ways.
- **`feedback_philosophy.md` §17 rewritten** for the hardness model, including why the dominated-option version was tempting. The preamble now states that the document is revised as the mod is built — everything except the §1/§2 identity.

### Decisions taken while building, worth revisiting

- **Propagation is a full rebuild, not Create's incremental update.** Flood fill the run, find the strongest source, walk outward. O(n) per change instead of O(change), which is worse on paper and fine at our scale — and it removes the need for Create's "flicker score", because a rebuild cannot loop. Revisit only if a profiler complains.
- **Inertia is implemented.** Speed belongs to the network, not the block; each node keeps a *ratio*. The network has a target speed and a current speed chasing it, at `(capacity − load) ÷ inertia` RPM per tick — real angular acceleration, not a fudge. Create has no equivalent: its networks snap to speed, which is why its flywheel visibly coasts while the network does not.
- **Shaft loss is implemented, as `Su`.** Each shaft charges bearing friction to the network. Loss is `Su` rather than `RPM` because a rigid shaft turns at one speed along its length — a speed difference between its ends is torsion, not loss, and what friction eats is torque. Because `Su` sums network-wide this is automatically distance-independent: moving a machine nearer the generator saves nothing, since every bearing turns either way. **Sprawl costs, distance doesn't.**
- **All numbers live in `core/FTuning.java`.** Everything is fiction until playtesting, so the point is turnaround: one file to edit, not twelve block entities. Becomes a config when the figures start meaning something.

### Found by building, not by designing

- **Headroom became a real decision.** A network at 98% of capacity (three hammers on a 256 Su wheel) takes **81 seconds** to reach speed, because acceleration is surplus torque over inertia and there is almost no surplus. Two hammers reach speed in two seconds. Nobody installed this — it falls out of the acceleration relationship — and it is §5's test passing: the same capital-vs-attention trade, in a third unrelated place. **Keep it.** Possibly soften the magnitude; do not remove the shape.
- **Shaft loss scales with RPM.** Resolved, and it turned out to fix two bugs at the root. A stopped shaft costs nothing, so an unpowered run is no longer reported as overstressed. And a network now accelerates until surplus torque runs out, so it **finds its own top speed**: a hand crank rated 32 RPM drives 3 shafts at 32, 5 shafts at 24, 10 shafts at 12. Deliberately unlike a hard per-shaft Su cap, which produces the absurdity of a generator being *too good* for its own shafting.
- **Overstress means something narrower now.** Only when a source is present *and* static load exceeds capacity before anything turns. A run with no source is unpowered, not overloaded; a run that merely cannot reach its target speed is not faulty, it is just slow.
- **Accelerating force deliberately ignores friction**, with the terminal speed applied as a clamp instead. Using live surplus is more literal but behaves badly — surplus reaches zero exactly at terminal speed, so a run creeps the last revolution for fifteen seconds.
- **Bearings are the obvious first physical upgrade** — a bushed or greased shaft with lower `Su` cost. Fits "upgrades are physical components" exactly. Not built.
- **Water wheel speed scales with how many sides have flowing water**, so siting it is a decision rather than a placement. Not from the slice doc — an invention, and cheap to remove.

---

## 4. Document 3 — content

Downstream of doc 2. The roster: every item, material, machine, cover, process.

- [ ] Seed from slice 1's 12 items / 7 machines
- [ ] Then slice 2

---

## 5. Slice 2

Scope already known. Three things arrive together, each making the others necessary:

- [ ] **Tempering** — a controlled *cool*, not a hold. First thing the player can't ask for
- [ ] **Damper** — the way to ask for it. Second actuator
- [ ] **Controller** — tape writer + tape reader. Needed because two actuators means two conditions
- [ ] **Blaze Rod** — Nether opens at the end of slice 1 (flint and steel takes steel)

---

## 6. Long-range open questions

From `feedback_philosophy.md` §18. Not urgent; do not answer early — answering these now means inventing the tech tree, which is the trap.

- [ ] Separation mechanisms mapped to the actual material roster (§11)
- [ ] Machine roster + physical upgrade path per process family (§14)
- [ ] How far vanilla reinterpretation goes beyond smelting (§15)
- [ ] Create compatibility — ship it or not, and how deep (§16)
- [ ] Local conversion covers: electrical to mechanical, electrical to thermal (§10)
- [ ] Visual architecture of each factory era (§14)
- [ ] Where each exotic phenomenon first appears and how it becomes engineered (§2, §14)
- [ ] Full era arc; where the "detect before you can act" bridges land (§14)
- [ ] Personal empowerment curve (§14)

---

## 7. Parked

- **Food as a real thermal process** (§18a) — cooking meat to different degrees for different hunger/saturation. Fits the model almost too well. Scope creep against a slice about metal. Worst case, a good addon. **Not now.**

---

## Running check

From §5. Apply to every new system before building it:

> **When a trade the mod already believes in shows up somewhere it was never installed, the systems are right. When every interesting choice has to be placed by hand, they are not yet.**

Ask of anything new: *does this create a second viable answer to a problem that already had one?*
