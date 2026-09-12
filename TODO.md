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

- [x] **JEI** — dev only, now with a written plugin. Our machines still have no recipe list; what the browser shows is the *material* table, as process cards (see below)
- [x] **PonderLib** — the designated explanation channel (§8, *Documentation is a free sense*). Scenes teach verbs, never values or solutions. Promote to a real dependency when the first scene is written
- [x] **Flywheel** — **yes, will be a real dependency.** Instanced rendering; without it every spinning shaft is its own draw call. Create's `SingleAxisRotatingVisual` / `RotatingInstance` are the reference (MIT)
- [x] **GeckoLib** — **no.** It plays authored keyframe animations; our motion must be procedural, because a machine's visible state is a readout the player reasons from. A fixed-length animation decouples the visual from the mechanics, and a machine that visually lies contradicts §8
- [x] **Cloth Config** — **no.** NeoForge's built-in config is sufficient, and our config surface should stay small: numbers here are design decisions, not user preferences
- [x] **Registrate** — **no.** Saves registration boilerplate but hides what registration says. Legibility beats brevity on this project
- [x] **Jade** in the dev environment (15.10.6). Compat not written yet — see below
- [x] **Jade / TheOneProbe** — **Jade, yes; TheOneProbe, no.** A HUD that prints `1247 Tu` on a bare crucible gives away the thermometer and guts §8. One that shows *"really hot!!!"* until an instrument is installed, and figures after, is the adjectives rule on a HUD. Written that way; still `compileOnly`, still not shipped. TOP would be a second copy of the same policy to keep honest, for no new capability
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
### Information layer — Jade, JEI, and the debug helmet

Governed by §8's *what you need is free, what you have is a cost*. Write these together; they are one design, not three features.

- [x] **JEI: process cards, not recipes.** Render a `Deformation` as a spec sheet with **exact units**, because a requirement is published data:

  ```
  COPPER PLATE
  Input     1 × Copper Ingot
  Work      14 Fu
  Hardness  1
  Output    1 × Copper Plate
  ```

  The process name (*Mechanical deformation*) is the **category tab**, not a line in the card. JEI's `IRecipeCategory<T>` takes any type, so this needs no vanilla `RecipeType` and no pretend recipes in the recipe book — which matters, because the hammer genuinely does not know how to make anything. Built as `compat/jei/`; the hammer is listed as the category's catalyst, which says *this machine deforms* and not *this machine makes copper plate*
- [x] **Jade: adjectives only.** *"really hot!!!"* on a bare crucible; figures once a thermometer is installed, at that instrument's resolution. A machine's own card (`12 / 3 St`, `80 Su`) is fair game — §17 says those are printed on the block, not measured. Built as `compat/jade/`: speed bands and strain as words, the linkage's installed throw and the hammer's `St`/`Su` as exact figures
- [x] **A labouring network smokes and creaks** at its sources — every 40 ticks when overloaded, every 80 when above 90% of capacity. Free under §8 because it is an adjective: it says *this one, and it is struggling* without handing over the Su ledger. Fixes the same silent-failure class as the mis-oriented linkage
- [x] **Debug Helmet, replacing all the scattered debug hooks.** One creative-only item that overrides every qualitative readout with the exact value the simulation holds. Delete `RotationNode#debugReport`, `MechanicalHammerBlockEntity#debugReport` and the three sneak-right-click handlers when it lands.

  It is a better dev tool than sneak-clicking every block, and it is thematically exact: the helmet is *perfect instrumentation*, the one thing §8 says a player may never actually buy. Keep it out of the normal creative tab so it never reads as a tier of thermometer.
- [x] Refine the workpiece tooltip once this exists: *needs 14 Fu* is a requirement and may be stated for free; *has 9 Fu* is state and waits for calipers.
- [x] **Clutch + Timer** — two blocks, not one. Stopping a shaft and deciding when are different jobs (§13), so the Timer is crude control and the Clutch is the mechanical actuator. Costs one block over the slice's budget and buys two things: the player can throw the clutch by hand before building any control at all, and slice 2's controller *replaces* the Timer instead of being a new idea. Disengaging splits the run, so the far side coasts — "clutches coast" needed no code, it falls out of the inertia model
- [ ] **Calipers** — `16 / 20 Fu`, and deliberately *after* the timer
- [ ] Flywheel-based rendering for spinning shafts; currently the model does not visibly turn
- [ ] Real textures
- [x] **Shaft placement QoL** — clicking a shaft while holding a shaft extends the run along its axis, the way Create does. Clicking an end face grows that way; clicking a side grows away from the player. Sneak to suppress it and place normally
- [x] **Delete the temporary debug readout** (`RotationNode#debugReport`, sneak-right-click). It hands out exact figures with no instrument, which is §8 backwards. Gone, along with the four other `debugReport`s and every sneak-click handler; the Debug Helmet is its replacement

### Information layer — decisions taken while building

- **The requirement/state split is enforced in one file**, `client/Readout.java`. §8's rule is a rule about *wording*, and wording copied between a tooltip and a HUD drifts until one of them quietly starts printing a figure. Requirements and equipment specs are formatted at their call sites instead, because there is no rule about them to enforce.
- **Strain is on the free side of the line, and that is deliberate.** "Straining" / "Overloaded" names no figure — it is the same adjective the smoke and creak already give at the sources, and without it a stalled factory is indistinguishable from a broken mod. There is no puzzle in a problem you cannot locate.
- **Jade reads live figures through its own server data**, not through the block's render sync. The `Su` a `RotationNode` carries is a snapshot from the last recalculation, and target speed and inertia are never synced at all. Adjectives from stale figures would usually be right; the helmet could not be, and §8 says an instrument may become wrong but may never overstate its certainty. Consequence: **on a server without Jade, the HUD says nothing rather than guessing.**
- **The deformation table is shipped to the client by our own packet**, on `OnDatapackSyncEvent`, rather than through JEI's hooks. It is a missing-data problem, not a JEI problem — solving it in JEI would mean every future display has to solve it again. JEI is then an optional *reader* of a plain list.
- **JEI's cards are pushed from the runtime, not from `registerRecipes`.** JEI starts from the vanilla recipe sync, which lands in the configuration phase, while our table arrives in the play phase — so `registerRecipes` would see an empty table on a first join, and registering in both places would double every card.
- **The Debug Helmet implements `Equipable` rather than extending `ArmorItem`.** ArmorItem would demand a registered armour material and an armour-layer texture, then render a missing-texture helmet, all to describe an item that gives no protection and should be invisible.
- **Sneak-clicking the Mechanical Hammer now extracts** rather than printing a report. Placing a block against its face while sneaking never worked and still does not; nothing regressed, but it is a behaviour change worth knowing about.
- **Not built, deliberately: any instrument.** There are still no calipers and no thermometer. `Readout.instrumented()` is a boolean standing where a per-quantity, per-resolution question belongs; the call sites are already shaped for the real answer.

### Slice discrepancies — reconciled

`feedback_slice_01.md` has been updated to match what is running. Kept here as a record of what moved and why:

- **Copper work 30 → 14 / 9 / 20 Fu.** 30 made the overshoot lesson impossible to express; the plate→foil step has to cost less than one strong blow or the short throw cannot skip it.
- **"Either crank works on copper" → the strong crank cannot make a plate at all.** Much better lesson, and it makes gearing a product selector rather than a speed setting.
- **"Thin plate"** removed from the chain; it was never in the roster.
- **"About 8 ticks, there is a plate"** dropped. One blow per revolution puts a plate at five blows on the gentle setting, two on the strong one.
- **"Fifteen hammer swings"** → five, since a hand hammer's 3 St against copper's hardness 1 lands 3 Fu a swing.
- **`Minimum force N St`** → `Hardness N` on every process card. One material property doing both jobs (§17).
- **Hand crank drives nothing** is now stated in the slice as an `[OPEN]`, with the flywheel as the interesting resolution, rather than reading as a balance bug.
- **Four lessons added** to the slice's table — all four emerged from building rather than from the draft.

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
