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

- [ ] **Mechanical Hammer** — 80 Su, `12 / 3 St`, one blow per cycle
- [ ] **Crank Linkage** — short/long throw variants; rotation to reciprocation
- [ ] **The `Fu` process** — 30 Fu to a plate, then plate → foil → scrap on continued striking
- [ ] **Timer** — the slice's only automation before instruments
- [ ] **Calipers** — `16 / 20 Fu`, and deliberately *after* the timer
- [ ] Flywheel-based rendering for spinning shafts; currently the model does not visibly turn
- [ ] Real textures
- [ ] **Delete the temporary debug readout** (`RotationNode#debugReport`, sneak-right-click). It hands out exact figures with no instrument, which is §8 backwards. It exists only because nothing visibly turns yet

### Decisions taken while building, worth revisiting

- **Propagation is a full rebuild, not Create's incremental update.** Flood fill the run, find the strongest source, walk outward. O(n) per change instead of O(change), which is worse on paper and fine at our scale — and it removes the need for Create's "flicker score", because a rebuild cannot loop. Revisit only if a profiler complains.
- **No rotational inertia.** **[OPEN]** Create has none either — its network snaps to a new speed instantly. But §13 says oscillation is fixed with physical mass, and the energy-cutoff rule says clutches *coast*. Both need spin-up and spin-down time. Nothing in beat 1 needs it; slice 2's actuators probably do.
- **Shafts are lossless.** **[OPEN]** Slice 1 says "SHAFT / GEARBOX — transmits, **with loss**". Not implemented. Needs deciding what is lost: `RPM`, `Su`, or both, and whether loss is per block or per run. A per-block loss makes layout matter; a flat loss is just a tax.
- **RPM figures are invented.** Slice 1 fixes `Su` for every machine and never fixes a speed. Hand Crank 32 RPM, Water Wheel 4 RPM per flowing side. Placeholders, flagged as such in the code.
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
