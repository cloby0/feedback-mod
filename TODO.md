# Feedback — TODO

Working checklist. Source of truth for *what's next*; `feedback_philosophy.md` stays source of truth for *why*.

---

## Pick-up list — costed, start cold

Candidates weighed on 2026-09-12 and deliberately not taken that night. Each one is scoped
here so a fresh session can start on it without re-deriving the scope. Ordered by value, not
by size.

- [x] **JEI take-over: hide vanilla's smelting/blasting/smoking, publish every pooled recipe as
  our own.** Built 2026-09-13, direct follow-on to §4f. `./gradlew build` passes; **unverified by
  eye.** `FeedbackJeiPlugin.onRuntimeAvailable` hides JEI's built-in `SMELTING`/`BLASTING`/`SMOKING`
  categories, since the Furnace, Smoker and Blast Furnace never touch a vanilla block class any
  more and the built-ins now describe permanently inert blocks.

  **Got a second pass the same session, after a look at the actual card.** First cut gave pooled
  recipes their own `VanillaFallbackCategory` and their own tab — wrong, and wrong for a reason
  worth keeping: a second tab whose only distinction is "we didn't author this one" is a
  distinction the player has no reason to care about, and it directly contradicted this file's own
  §2 read of `ThermalProcessCategory` as *the* place a fire's effect on a material is published.
  Corrected to one category:

  - Every pooled recipe (from `IRecipeManager.createRecipeLookup` across `SMELTING`, `BLASTING`,
    `SMOKING` — no second pooling pass, same recipes `VanillaFallback.find` already searches) is
    wrapped as a `ThermalProcess` in `FeedbackJeiPlugin.pooled`, filtered against
    `ClientThermalProcesses` so steel does not also show up as a generic smelting card, and
    published under `ThermalProcessCategory.TYPE` alongside the hand-authored entries.
  - **What's honest to wrap and what isn't.** Input, output, and whichever floor gates it
    (`FTuning.METAL_MIN_TU` or `FOOD_MAX_TU`) are real, universal, exact figures — same standing
    as a machine's own printed spec sheet (§17) — so they're printed as numbers, not softened into
    an adjective the way the first pass did. `holdTicks` is not honest to invent: metal counts an
    integrated Work total and food counts plain ticks, neither of which is "N ticks in this band",
    so a new sentinel `ThermalProcess.NO_HOLD` (`-1`) and `hasHold()` let the card leave the row
    off instead of printing a number nobody measured.
  - `ThermalProcessCategory` gained the display-only changes this forced anyway: a temperature
    band with no ceiling draws as `"800+ Tu"` rather than a bogus range against `Float.MAX_VALUE`,
    and the catalyst list for the whole category now includes the Furnace, Smoker and Blast
    Furnace alongside both crucibles — which turned up a real pre-existing gap, not just a
    consequence of the merge: those three vessels check `ThermalProcessTable` before falling back
    to `VanillaFallback` (§4e/§4f), so a hand-authored process like steel was already reachable in
    the Blast Furnace and JEI never said so.
  - Two small display bugs fixed on the same pass, from a screenshot of the Steel Ingot card:
    the `Temperature`/`Max heating` labels overlapped their values (longer label than the
    `LABEL_X`-to-`VALUE_X` gap allows) — shortened to `Tu` / `Max Tu`, matching lang keys
    `feedback.jei.temperature` / `.max_heating`. And the spoils row no longer names the spoiled
    result (`"> 1470 Tu -> Burnt Iron"` → `"> 1470 Tu"`), for hand-authored and pooled cards alike
    — one format, not two.

  **A third pass, from a screenshot of the Baked Potato card: real duplicate-recipe bug, not a
  display bug.** `pooled()` streamed `SMELTING`/`BLASTING`/`SMOKING` flat and wrapped every
  `RecipeHolder` on its own. Vanilla ships *two* cooking recipes for potato → baked potato, one
  `smelting` and one `smoking` — either appliance can cook food — so the flat stream produced two
  cards for one item, and because `isFood` reads off the specific recipe instance found, the
  `smelting` copy drew as an ungated metal card (`"800+ Tu"`, no spoils) right next to the correct
  food one (`"0-400 Tu"`, `"Spoils > 400 Tu"`). `VanillaFallback.find` already resolves this
  correctly at runtime — best of all three types, shortest cooking time wins — so the vessel
  itself never had this bug, only the card. Fixed by grouping pooled recipes by input item first
  in `FeedbackJeiPlugin.pooled`, keeping the shortest-cooking-time entry per item with the same
  tie-break `VanillaFallback.find` uses, so the card and the block agree on which single recipe an
  item runs as.

  **Already true, nothing to do here:** vanilla's own in-GUI recipe book (the bookmark-flip
  toggle) doesn't apply to these screens at all any more — `ThermalVesselScreen` never adds one,
  since it is not `AbstractFurnaceScreen` and never inherited `RecipeBookComponent`'s ghost-fill.
  There is nothing to remove; it was never there to begin with.

- [x] **`core/unit/` — the units of §17 as types.** Done for thermal. **The open question was
  answered: records, but only at the boundaries.** Four thin records — `Tu`, `TuRate`,
  `ThermalMass`, `Conductance` — each with one accessor *named for its dimension*
  (`workPerTu()` vs `workPerTickPerTu()`), which is the entire mechanism: swapping a mass for a
  leak is now a compile error. There is deliberately **no arithmetic on the types**; `Heat`
  unwraps inline to floats, so the five lines of physics still read like the equation the
  javadoc argues about. The third option — a static holder with no wrapper — was rejected as
  catching nothing while looking like the item had been closed.

  Verified by breaking it on purpose: substituting `getLeak()` for `getThermalMass()` in
  `Heat.tick` fails to compile, which is the bug the package was written to stop.

  Two findings worth keeping:
  - **A leak and a conductance are the same dimension** (`Work/t/Tu`), and `Heat.equilibrium`
    was already adding them. One type, two roles. This is the thing the exercise paid for.
  - **`Tu` covers both a temperature and a difference between two**, knowingly. The only
    interval in the model is an instrument's resolution; a fifth type is not worth it. First
    place the scheme is deliberately loose, recorded so it is not re-derived.

  Still floats, on purpose: rotation's `Su`/`RPM`/`Fu`/`St` (next pass), and the datapack
  records `ThermalProcess`/`Quench`/`Deformation` — retyping those means touching wire formats,
  which is a separate change with a separate risk. The dangerous triple the item was raised for
  is entirely inside what was done.

- [x] **Crafting recipes, one pass over the roster.** Done — 19 shaped recipes and 3 unlock
  advancements, hand-authored under `src/main/resources/data/feedback/`. Both beats are
  craftable in survival for the first time. It turned out *not* to be the decision-free pass
  this entry promised: see §3c. The Hand Hammer had to be built first, because a copper plate
  needed a Mechanical Hammer and a Mechanical Hammer needs copper plates.

- [x] **The Crude Blast Furnace and the vanilla thermal bands (§15).** Built. `./gradlew build`
  passes and a dedicated server mixes all three mixins into their targets and boots clean —
  **nothing has been seen running.** Detail in §4e.

- [ ] **The cover system (§4d).** Blocked on reading GregTech CEu Modern's first, which is a
  session's work by itself. Do not start the code before the read.

- [x] **Reading wear with calipers.** Built. `Quantity.CONDITION`, `CalipersItem` reading at 5%
  resolution, and a refusal while the machine is running. It was mostly typing, and the two places
  it was not are worth keeping:
  - **The seam is `onItemUseFirst`, not `useOn`.** NeoForge calls it ahead of any block
    interaction, which turns out to be load-bearing rather than tidy: `MechanicalHammerBlock`
    accepts whatever is held as a workpiece, so with a plain `useOn` the first act of a player
    measuring a hammer would be forging their calipers into a plate. Taking the click in the item
    also means no machine block knows instruments exist.
  - **The reading is composed server-side, and that is not a formality.** Condition syncs only
    when the *adjective* changes, so the client's copy is stale by up to a whole band — a 5%
    figure read from it would be fiction. `Instruments.signed` was pulled out of
    `Readout.reading` to make that possible: the attribution wording is one rule, and it now lives
    in the common package with the client path delegating to it.

  New: `machine/Wearing` — `getCondition()` and `isRunning()`, the read side only. Deliberately no
  `wear(float)`: absorbing force stays private to the machine that does it, because what the
  surplus *does* is the machine's own business.

- [x] **`core/unit/` for rotation, and the machinery for every unit after.** Done. `Su`, `Rpm`,
  `Fu` (an int — work arrives in blows, and "one more blow" has to be arithmetic rather than a
  question), `St`, plus the two compounds that were sitting two lines apart in `FTuning` and are
  the most swappable pair in the mod: `Drag` (`Su/RPM`, a steady-state cost) and `Inertia`
  (`Su·t/RPM`, a transient one). Verified the same way as thermal — substituting one for the
  other in `RotationNetwork.recalculate` fails to compile.

  **The infrastructure is the actual deliverable.** `Unit` (a common interface, `raw()` +
  `unitKey()`) and `Units` (codec, intCodec, streamCodec, intStreamCodec, `figure`). A new unit
  is now one small file and one lang key; the checklist lives in `Units`' javadoc. `Unit` is
  deliberately **not sealed** — sealing buys exhaustive switches nothing wants and charges a
  second file edit per unit, which is the cost the class exists to remove. So `Eu`, `Pu`, `Qu`,
  `mB` and a radiation unit cost an afternoon each rather than a pass over the codebase.

  Recorded honestly: `Unit.raw()` weakens the accessor-name guard, since every unit answers to
  it. The parameter-type guard — the strong one — is untouched, and the convention is that
  `raw()` belongs to codecs and display while hand-written physics uses the named accessor. The
  blunt name is so that reviewing for it is cheap.

- [ ] **[OPEN] Air has no unit.** Turned up by the retype and left standing at
  `BellowsBlockEntity.getStrength`. `StrengthPair` is typed in `St` because that is what a
  linkage delivers, and the bellows returns an air figure through it one-to-one — invisible
  while both were `float`. §17 has no unit for air at all. Either it gains one (a volume, so
  `mB`, with a stated conversion from the force compressing the bag) or the firebox is
  re-expressed in a unit that exists. A rename would hide it; this is a design decision.

- [ ] **`core/unit/` for the datapack records.** The deliberate remainder: `Deformation`,
  `ThermalProcess`, `Quench` and `Fuel` still hold floats, because retyping them touches wire
  formats and pack-facing JSON. `Units.codec` / `Units.streamCodec` were written for exactly
  this, so the JSON does not change — a pack written today keeps working. Small, and worth doing
  before a fifth table copies the untyped pattern.

- [ ] **Allocation pass on `RotationPropagator`.** Not urgent and not a bug — every rebuild
  trigger is correctly edge-triggered, so nothing runs per tick. But one `rebuildFrom` on an
  *n*-node network allocates **~3n objects**: `connectedNeighbours` builds a fresh
  `ArrayList<>(6)` per node and is called once from `floodFill` and again from `assignRatios`,
  and `Map<RotationNode, Float>` boxes every ratio. A 500-shaft rebuild is ~1500 objects.
  The fixes are mechanical — reuse a buffer in `connectedNeighbours`, swap the map for
  fastutil's `Object2FloatMap` (already on the classpath via Minecraft). Worth doing before
  networks get large, and worth measuring first rather than assuming.

- [ ] **Real models and textures**, for every block in both beats. Art, and the user's call —
  not something to start unprompted.

**No longer true:** beat 2 is committed (`96cea49`), and the GPL relicence with it (`c6f0a8f`).
The tree is clean, so a wide refactor is now safe to start.

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
- [x] Package structure — root `io.github.soundgoodizerfan.feedback`, tree follows the mod's separation of systems (see `CLAUDE.md` "Source layout")
- [x] Gradle scaffold, run configs, Parchment `2024.11.17`, `build-and-deploy.sh` to the PrismLauncher instance. NeoForge `21.1.250`, MDG `2.0.147`, Gradle `9.2.1`
- [x] **Data-driven recipe format — settled by precedent rather than decree.** Four datapack tables now exist (`deformation`, `thermal_process`, `quench`, `fuel`), each a `SimpleJsonResourceReloadListener` over a record with a `Codec` and a `StreamCodec`, none of them a vanilla `RecipeType`. §15's "compat authored as a table" is satisfied. The pattern is deliberately copy-pasteable: a fifth table should cost an afternoon

### Library decisions

Dev environment loads **JEI**, **PonderLib**, **Jade** and **Flywheel** out of `run/mods`, synced by the `syncDevMods` Gradle task. **Flywheel is now a shipped dependency** — `compileOnlyApi` on the API, `jarJar(runtimeOnly)` on the implementation, pinned to `1.0.4` with the range `[1.0.0,2.0)`. The rest are still dev-only.

- [x] **JEI** — dev only, now with a written plugin. Our machines still have no recipe list; what the browser shows is the *material* table, as process cards (see below)
- [x] **PonderLib** — the designated explanation channel (§8, *Documentation is a free sense*). Scenes teach verbs, never values or solutions. Promote to a real dependency when the first scene is written
- [x] **Flywheel** — **a real dependency, declared and jarJar'd.** Instanced rendering; without it every spinning shaft is its own draw call. Create's `SingleAxisRotatingVisual` / `RotatingInstance` were the reference (MIT). Pinned to the version Ponder already drags into the dev run, so the API we compile against is the implementation we test against
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

**Thermal** — the first two are now *implemented*, so doc 2 has something to describe rather than invent
- [x] Thermal model: `Tu` as state, `Work` delivery, thermal mass, ambient, insulation (§8, §9). Built — `core/thermal/`. Flow on a difference, not a flat rate; see `Heat` for the trap
- [x] Hot workpiece cooling rate (§9). Built — 1.5 Tu/t, linear, stamp-and-timestamp. There is no `Hot Steel Ingot` item to revert: heat is two components on the ordinary ingot
- [ ] Write both of the above up in doc 2, from the code rather than from scratch
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
- [x] **The anvil is a container** — one slot, exposed as an `IItemHandler` capability, so vanilla hoppers feed and empty the hammer and moving items stays vanilla's job. It leaks no completion detection: an extractor takes whatever is on the anvil, worked or not, so a hopper is a second machine racing the first rather than a sensor. A part-worked item keeps its `Fu` and can go back in
- [x] **Workpiece is visible in the machine too** — `client/MechanicalHammerRenderer` draws it lying on the anvil. Not decoration: beat 1 is meant to be readable by eye, and it cannot be if finding out what a machine holds means pulling the item out of it
### Information layer — Jade, JEI, and the debug helmet

- [x] **Every reading is signed by the instrument that took it** — `Calipers: 9 / 14 Fu`. A bare figure is unfalsifiable, and §8 promises instruments never lie while saying nothing about them being *accurate*. Once drift and recalibration are real, a player looking at a bad batch must know which instrument to distrust, and two tiers disagreeing has to read as informative rather than broken
- [x] **The `Instrument` interface** — `instrument/{Quantity,Instrument,Instruments}`. An item declares `canRead(Quantity)`, `resolution(Quantity)`, `label()`; `Instruments.best(player, quantity)` finds the finest one carried. Calipers and the Debug Helmet are declarations now. **A thermometer is one class and touches no display code.** `Instruments.quantise()` applies resolution, which is the whole of what a tier buys

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
- [x] **Calipers** — held tool; turns the workpiece adjective into `9 / 14 Fu`, on the item and on the hammer through Jade. No right-click to take a reading: applying calipers is a real action but as a mechanic it is a keystroke with no decision in it, and §3 cuts mechanics that are real, well-precedented and simply not fun
- [x] **The instrument seam is per-quantity now** — `Readout.instrumented(Quantity)`. Calipers answer for `WORK` only; owning one instrument must not sharpen every readout in the game. Still not per-*resolution*, which §8 eventually wants ("each tier buys significant figures") — the call sites are shaped for it
- [ ] **[OPEN] Calipers on an untouched ingot show nothing**, because an unworked item carries no `WORK_REQUIRED`. The client now has the deformation table (JEI sync), so they *could* read `0 / 14 Fu`. Low value — that figure is a requirement and JEI already gives it away for free
- [x] **Flywheel-based rendering for spinning shafts.** Shaft, Hand Crank, Water Wheel and Clutch now turn. One visual class (`client/RotatingVisual`) over `RotationNode`, spinning `Models.block(state)` about `Rotatable.getRotationAxis`, plus a plain `client/RotatingRenderer` for players who have the backend off. §8's claim that the player's own senses are free was a fiction until this landed — a running factory and a dead one looked identical
- [ ] Real textures
- [x] **Shaft placement QoL** — clicking a shaft while holding a shaft extends the run along its axis, the way Create does. Clicking an end face grows that way; clicking a side grows away from the player. Sneak to suppress it and place normally
- [x] **Delete the temporary debug readout** (`RotationNode#debugReport`, sneak-right-click). It hands out exact figures with no instrument, which is §8 backwards. Gone, along with the four other `debugReport`s and every sneak-click handler; the Debug Helmet is its replacement

### Rendering — decisions taken while building

- **`ENTITYBLOCK_ANIMATED`, not `RenderShape.INVISIBLE`.** Both keep the block out of the chunk mesh, which is the thing that matters: Flywheel's `skipVanillaRender` suppresses only the *block entity renderer*, so without this the block would be drawn twice, once still and once spinning. But vanilla also gates block-breaking particles on `INVISIBLE` specifically, and a shaft that shatters silently loses a sense for nothing. Create uses `ENTITYBLOCK_ANIMATED` throughout for the same reason.
- **A fallback renderer is not optional.** Leaving the chunk mesh means the block entity is the *only* thing drawing a shaft, so with Flywheel's backend disabled every shaft in the world would simply vanish. `RotatingRenderer` guards on `VisualizationManager.supportsVisualization` exactly as Create's `KineticBlockEntityRenderer` does.
- **CPU transforms, not a rotation shader.** Create spins on the GPU with its own `rotating.vert`, which is an *asset* and All Rights Reserved even though Create's code is MIT. Flywheel's built-in `TRANSFORMED` instance type with a per-frame transform is still one draw call for the whole shaft run; a GPU-side clock is a later optimisation, not the entry price.
- **`0.3f` degrees per tick per RPM is now `RotationNode.DEGREES_PER_TICK_PER_RPM`,** and both the visual and the fallback extrapolate with it. The renderer runs per frame and the simulation per tick, so the two have to agree or a slow shaft lurches around the angle the simulation actually holds.
- **The Clutch gained a client ticker** it never had — it does all its work on right click and needs no server tick, but without a client tick its angle never advanced and it sat frozen in the middle of a run that was plainly turning, which reads as disengaged when it is not.
- **Nothing visual was verified.** `./gradlew build` passes and a dedicated server boots clean with no client-only class loaded, which is all that can be checked without a window. Still unconfirmed by eye: that the four blocks actually turn, that none is drawn twice, that none has vanished, that the speed on screen matches the RPM Jade reports, and that the fallback draws correctly with Flywheel's backend set to `OFF`.
- **The whole block spins, base and all.** `Models.block(state)` takes the block's own model, which for the Hand Crank includes its mounting plate and for the Clutch its housing. Correct art would split the turning part from the fixed one, which means partial models and real textures. Placeholder art, placeholder motion.

### Information layer — decisions taken while building

- **The requirement/state split is enforced in one file**, `client/Readout.java`. §8's rule is a rule about *wording*, and wording copied between a tooltip and a HUD drifts until one of them quietly starts printing a figure. Requirements and equipment specs are formatted at their call sites instead, because there is no rule about them to enforce.
- **Strain is on the free side of the line, and that is deliberate.** "Straining" / "Overloaded" names no figure — it is the same adjective the smoke and creak already give at the sources, and without it a stalled factory is indistinguishable from a broken mod. There is no puzzle in a problem you cannot locate.
- **Jade reads live figures through its own server data**, not through the block's render sync. The `Su` a `RotationNode` carries is a snapshot from the last recalculation, and target speed and inertia are never synced at all. Adjectives from stale figures would usually be right; the helmet could not be, and §8 says an instrument may become wrong but may never overstate its certainty. Consequence: **on a server without Jade, the HUD says nothing rather than guessing.**
- **The deformation table is shipped to the client by our own packet**, on `OnDatapackSyncEvent`, rather than through JEI's hooks. It is a missing-data problem, not a JEI problem — solving it in JEI would mean every future display has to solve it again. JEI is then an optional *reader* of a plain list.
- **JEI's cards are pushed from the runtime, not from `registerRecipes`.** JEI starts from the vanilla recipe sync, which lands in the configuration phase, while our table arrives in the play phase — so `registerRecipes` would see an empty table on a first join, and registering in both places would double every card.
- **The Debug Helmet implements `Equipable` rather than extending `ArmorItem`.** ArmorItem would demand a registered armour material and an armour-layer texture, then render a missing-texture helmet, all to describe an item that gives no protection and should be invisible.
- **Sneak-clicking the Mechanical Hammer now extracts** rather than printing a report. Placing a block against its face while sneaking never worked and still does not; nothing regressed, but it is a behaviour change worth knowing about.
- **Not built, deliberately: any instrument.** There are still no calipers and no thermometer. `Readout.instrumented()` is a boolean standing where a per-quantity, per-resolution question belongs; the call sites are already shaped for the real answer.

- [x] **Crafting recipes.** Done in one pass — see §3c. Nothing is creative-only any more except the Debug Helmet, which is supposed to be

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

## 3c. The Hand Hammer, and the recipe pass — built, unverified

Slice 1's item budget listed a hand hammer from the first draft and nothing ever implemented
it, which left §7's sharpest promise — *manual production stays theoretically possible* —
with nothing behind it in the first ten minutes. It also left a genuine circular dependency:
a copper plate needs a Mechanical Hammer, a Mechanical Hammer needs copper plates.

- [x] **`Deforming.strike`** — one blow, extracted out of `MechanicalHammerBlockEntity` so
  the machine and the hand land the *same* blow. It returns a `Blow(result, outcome)` rather
  than a boolean, because the two callers want different things from a failure: the machine
  absorbs the force and wears, the crafting grid simply offers no craft. Same rule, seen from
  the two ends of a handle. The cascade, the heat carry and the hardness floor all moved with
  it and none of them changed
- [x] **Hand Hammer** — 3 St, 250 durability. Deliberately the same figure as the Mechanical
  Hammer's long throw, so the gentle machine is doing exactly what the arm was doing without
  ever getting bored, which is the whole of what beat 1 has to say
- [x] **`HandToolItem`, the shared shape** — a hand tool is a strength, a sound, and the fact
  that it survives the craft one point worse, and all three are identical for every tool that
  will ever exist. Built before the second tool for the same reason `Instrument` was built
  before the thermometer. Subclasses decide almost nothing: `getStrength()` is the whole of
  what makes one tool different, because §17 says what an application accomplishes is
  `St / hardness` and therefore a property of the material. **There is no registry of which
  tools do which operations** — a tool that does not strike declares `0` St and is refused by
  the hardness floor, so the strength is the whole answer
- [x] **Hand work makes a noise** — `HandToolCrafting`, on `ItemCraftedEvent`. The Mechanical
  Hammer's own `ANVIL_LAND`, quieter and with a little pitch jitter, because the two are
  landing the same blow through the same code and should be recognisable as the same thing.
  Free under §8: a noise is an adjective, naming no figure, exactly like the smoke and creak a
  labouring network already makes. Throttled to one per player per tick, so shift-clicking a
  stack gives one sharp report instead of sixty-four at once
- [x] **It swings in a crafting grid**, not at a block. Hammer plus one workpiece, one craft
  per blow, the workpiece handed back with 3 more `Fu` on it. `HandDeformationRecipe` is an
  adapter over `DeformationTable` and not a fifth table — delete it and no fact about copper
  is lost
- [x] **19 shaped recipes + 3 unlock advancements**, hand-authored. Loads clean: 1310 recipes
  and 1402 advancements on a dedicated server, no parse errors
- [ ] **Nothing is verified by eye.** Unconfirmed: that five crafts make a plate and a sixth
  makes foil, that shift-click runs a stack into scrap, that the hammer wears one point a
  swing and dies at 250, that the clang fires once per craft and once per shift-click, and
  that the whole roster is reachable in survival.
  **A GameTest would settle most of this without a window** — the recipe is pure server logic,
  so a test that feeds a `CraftingInput` five times and asserts a plate is cheap. Not written

### Decisions taken while building

- **The grid, not a block in the world.** A block route needed a surface to hit, which is the
  bootstrap problem again, and it would have been a fifth sneak-click handler immediately
  after four were deleted. The grid also earns something the block could not: **shift-click
  runs a whole stack straight through plate into foil into scrap**, which is beat 1's entire
  lesson delivered by the player's own hand before they own a machine. Left sharp on purpose.
- **A vanilla `RecipeType`, and §2's rule survives it.** Everything else in the mod is a
  datapack table specifically so that no *machine* owns a recipe list — a machine that knew
  when it was finished would stop being this mod. A crafting table is vanilla's recipe
  machine and the player is the one doing the work, and a player is allowed to know what they
  are making. The result slot previewing the next blow is the player gaining information,
  which §7 is in favour of; the hammer on the anvil still cannot tell.
- **Steel is out of reach by hand with no rule about hands.** 3 St under hardness 15 lands
  nothing, so the recipe never appears. §7's hard gate arriving as arithmetic.
- **A wasted swing costs nothing.** The Mechanical Hammer wears on force that could not go
  into the work; a hand hammer has no equivalent, because the grid never offers the craft. You
  cannot mis-swing at something you were never able to lift.
- **The noise is an event, not a getter.** GregTech plays its tool sound from inside
  `getCraftingRemainingItem`, which works, but it is a side effect hidden in a getter that both
  logical sides call. `ItemCraftedEvent` fires once, when a craft actually happened, and hands
  over the grid to look at.
- **The level reaches `assemble` by ThreadLocal.** A workpiece computes its temperature from a
  stamp and a tick, so a blow needs a clock, and `assemble` is the one place vanilla does not
  pass one — `CraftingMenu.slotChangedCraftingGrid` holds the level and drops it. NeoForge has
  the identical problem with the crafting player and solves it the identical way
  (`CommonHooks.craftingPlayer`). Worth knowing the workaround is theirs, not an invention.
- **Bootstrap is vanilla-only, by necessity.** The Hand Hammer is 5 cobblestone and 2 sticks.
  Everything downstream can then cost worked copper, which is what the slice always claimed:
  plates are a real intermediate because they go into the machines.
- **The Bimetallic Strip is literally bimetallic** — 2 copper foil, 2 iron nuggets. That it
  consumes *foil* is the nicest accident in the pass: beat 1's first overrun mistake is the
  material beat 2's only sensor is built out of, so the wasted plate was never wasted.
- **Unlocks are three grouped advancements**, not nineteen. Bootstrap on cobblestone, beat 1
  on copper ingot, beat 2 on brick. §8 says a requirement is published data, so there is
  nothing to protect by staging them finely.

---

## 4. Document 3 — content

Downstream of doc 2. The roster: every item, material, machine, cover, process.

- [ ] Seed from slice 1's 12 items / 7 machines
- [ ] Then slice 2

---

## 4b. Next session — start here

Verified working in game: rotation network with inertia and coasting, shafts that visibly turn (Flywheel, jar-in-jar), Clutch + Timer, Mechanical Hammer with the overrun chain, Calipers, JEI process cards, Jade in adjectives, Debug Helmet.

Cogs, the gearbox, `St` gearing and the hammer's force ceiling are now verified in game too.

Also verified since: wear (battered/spent bands, spent-hammer-stops-steel, over-gear cap), calipers
condition reading, and the whole of beat 2 heat, one steel ingot made start to finish — see §4c.

### Cogs and a gearbox — built and verified in game

Small Cog, Large Cog and Gearbox exist, along with the two rules that make gearing mean something. `./gradlew build` passes and the mod loads on a dedicated server; **nothing is verified by eye yet** — see the unchecked items below.

- [x] **Small and large cog**, Create's ratios: small↔small `-1` (meshing reverses), large↔small `-2`, small↔large `-0.5`. `RotationPropagator.ratioBetween` was already written for arbitrary signed ratios, so the engine change was one hook on `Rotatable` — `meshRatioTowards` — and cogs were content after all
- [x] **Gearbox** — cross-axis transfer at 1:1. It presents a shaft on all six faces and answers a *sign* per face through the new `Rotatable.shaftSignTowards`, so `ratioBetween` stopped requiring a shared axis without knowing what a gearbox is
- [x] **Decided: gearing costs nothing beyond the cogs and their drag.** Friction and mass are now referred through the square of the gear ratio in `RotationNetwork.recalculate`, which is the standard result rather than a balance rule — so a geared-up branch pays continuously for turning faster, on its own. A surcharge on top would be a rule restating what the physics already says
- [x] **Gear ratios change `St`** — decided, written into §17, and implemented: `CrankLinkageBlockEntity` multiplies the machine's stated force by the reciprocal of its own speed ratio. Blows land harder and fall less often, work per second is unchanged, and what changes is whether a blow clears a material's hardness floor at all
- [x] **Every machine states a maximum `St`** — `StrengthPair.getMaxStrength()`, `24 St` on the Mechanical Hammer, and on its Jade card beside `12 / 3 St`. Twice the short throw, so gearing buys exactly one genuine doubling past the strongest setting the block has
  - **Closed.** The ceiling is no longer a silent clamp: the linkage now delivers the *raw* geared force and the machine clamps itself, so the surplus is a real quantity something can be done with. The hammer takes it as wear — see "Wear, and the one rule behind it" below. The other candidate, charging the surplus as `Su`, was declined: it makes over-gearing cost power for nothing, which is true but invisible, and it would have needed a second mechanism for the two mistakes that produce no surplus at all
- [x] **Verified by eye.** Meshed cogs turn opposite ways, a large cog beside a small one runs at half speed on screen, the gearbox drives the shaft round the corner, and a geared-down hammer lands blows a plain one cannot
- [ ] Real models. Both cogs are a placeholder disc on a shaft and the gearbox is a copper box with three shafts through it
- [ ] **[OPEN] Does gearing need an ongoing cost?** Narrowed by building, not closed — see §17. The referral above makes *speed* cost; a machine's working draw is still a flat figure, so gearing *down* to reach a hardness floor remains free apart from the cogs
- Taking Create's two cog sizes wholesale is fine and deliberate — the rotation layer is openly Create-inspired, their code is MIT, and the mod's originality is in overrun and instrumentation rather than in inventing a third cog. **Their assets are All Rights Reserved: models and textures must be ours.**

#### Decisions taken while building the cogs

- **Cogs mesh face to face, not diagonally.** Create requires large-to-small to be diagonal because its large cog is visibly bigger than one block and its teeth reach the corner. Every block here is still a one-metre cube of placeholder art, so a diagonal rule would be one the player cannot see and therefore cannot learn. Revisit with real models — it is the one part of cogs that *is* engine work, since the propagator would have to scan the twelve diagonal neighbours as well as the six faces.
- **A cog inherits the axis of whatever it is placed against**, sneak to override. Clicked-face placement is right for a shaft, whose axis is the direction you are building, and wrong for a cog, which almost always goes onto the line you just clicked or beside the cog you just placed. Without this, meshing two cogs meant fighting the placement rule.
- **In line along the axis is not meshing.** Two cogs stacked on one shaft are bolted to it and turn as one; only cogs set side by side in the same plane engage teeth. Both cases fall out of the same two methods and neither needed a special rule.
- **The gearbox reverses anything opposite and agrees or disagrees by corner.** Its faces are driven off a common crown, so the signs are geometry rather than preference — and there is no consistent rule where every ninety-degree turn reverses, because the three axis pairs cannot all be negative at once. Taking the sign from the face's own axis direction is consistent by construction. Getting the direction you want is therefore a placement problem, which is the intent: the gearbox has no setting on it (§3), and a player who needs the other way round adds a cog.
- **The gearbox does not visibly turn.** It is a housing; its gears are inside it. No visualizer, no fallback renderer, and it stays in the chunk mesh — which also means it is the one rotating block that cannot vanish when Flywheel's backend is off.
- **One block entity type serves both cog sizes.** Size is a property of the block, not of its state, so drag and inertia are read off the block rather than stored and kept in step.
- **A large cog is the closest thing beat 1 has to a flywheel** — four times a small cog's inertia — which was not the point of building it and is worth watching. §4b's hand-crank-drives-nothing `[OPEN]` wanted a flywheel as its interesting resolution, and one may have arrived early by accident.

### Wear, and the one rule behind it — built and verified in game

Closed two `[OPEN]`s that turned out to be one question. **Force that cannot go into the work
goes into the machine.**

- [x] **The linkage delivers raw force; the machine clamps itself.** `CrankLinkageBlockEntity`
  had been applying the driven machine's `getMaxStrength()` before handing the stroke over, which
  threw the surplus away where nothing could notice it. It now passes the geared figure through
  and keeps the clamped one only for display. The ceiling stays a *declaration* on `StrengthPair`
  and enforcing it moved to the only place that knows what the excess does
- [x] **One `wear()` method, four routes into it** — workpiece too cold to move, blow under the
  material's hardness floor, drive geared past the ceiling, workpiece with nothing further to
  become. Writing a consequence per case was the obvious shape and would have been four balance
  decisions dressed as physics; there is one conserved quantity instead and nothing needs to know
  which mistake it was
- [x] **A hammer beating air takes nothing.** Nothing resists it. Also the line that keeps wear
  from becoming an uptime tax — and running empty already has its answer in overrun
- [x] **Condition scales the whole spec sheet**, both throws and the ceiling, floored at half.
  Scaling only the ceiling was tried first and punished exactly one of the four mistakes; a
  worn hammer went on hitting at a full 12 St, so two thirds of the wear was invisible. Gearing
  can now partly recover a worn hammer, up to a ceiling that has fallen too — a real trade, not
  a loophole
- [x] **Nothing breaks and nothing stops** (§7). A spent hammer still hits and copper still
  yields to it; steel stops clearing its hardness floor. Which material notices first is the
  material's business, which is the answer this mod gives everywhere
- [x] **Free adjective on the Jade card**, silent while the machine is sound. Damage is visible
  from across the room, so charging an instrument for it would be hiding a free sense (§8)
- [x] **Rejected prior art recorded** — GregTech's maintenance is a flat chance per runtime hour
  and carries no information about whether the factory was built well. Written up in
  `THIRD-PARTY-LICENSES.md`. Wear-caused-only-by-misuse appears to be genuinely unprecedented
- [x] **Verified by eye.** A hammer left beating a cold ingot visibly reports a marked then
  battered then spent head, a spent hammer stops making steel plate while still making copper
  plate, and an over-geared hammer wears without ever exceeding its stated ceiling
- [ ] **[OPEN] There is no repair, and no way back.** Condition only ever falls, and the only
  recovery is breaking the block and placing a new one — which works, and is unsatisfying for
  something the mod otherwise treats as a physical object. The right answer is almost certainly a
  **Hammer Head** item: an upgrade you could point at (§4), replaced rather than repaired, and
  the same slot a *better* head would eventually go in to raise the ceiling. Deliberately not
  built tonight — it is new content, not a fix, and it wants the roster to stop moving first
#### Reading wear — built and verified in game

**Calipers, by right-clicking the machine.** Decided in conversation; the reasoning is now in
§8 as *Some readings are free; some cost an action*. Verified: a stopped hammer reports a figure
on the action bar, a driven one refuses, and the calipers no longer end up inside the hammer.

- [x] **Calipers, not a new instrument.** A worn head has mushroomed — it is wider and shorter
  than it was — and reading how far a piece of metal has deformed is exactly what calipers
  already do to a workpiece. This is one measurement pointed at a second object, not a tool being
  given a second job. The adjective on the Jade card stays free; the *figure* costs the
  instrument, which is §8's standing split
- [x] **Right-click, and only for machines.** `CalipersItem`'s javadoc argued the opposite and it
  was right — about items. An item you are already holding should not demand a keystroke to
  produce a tooltip; that is §3's "real, well-precedented and not fun". A machine is different,
  and the difference is priced rather than thematic: **you cannot measure a machine that is
  running.** The reading costs a stopped line, so there is a real decision in whether to take it
  (§5), and a player who would rather keep producing can decline to know. Rule, stated generally:
  *a reading is passive when taking it is free, and an action when taking it costs something*
- [x] **The precedent is the Thaumometer**, which had this split right years ago: point it at
  things in the world, and with addons stop scanning items by hand. Nobody read the automation of
  the item case as lost content, because pointing a scanner at something in the world is fun and
  remembering to swap a hotbar slot before opening an inventory is not. Design precedent only,
  cited from memory — no code read and none available to read
- [x] **Wear is a percentage and not a unit.** Dimensionless, and §8 now says so explicitly so
  that nobody later mints `Wu`: the apparatus properties are expressed in units that already
  exist (§17), and a ratio of a machine to its own former self is not a quantity in that sense
- [x] **It reads as condition, not wear** — 100% as built, falling — because that figure
  multiplies the spec sheet directly: a hammer at 72% delivers 72% of the force stamped on it.
  Rescaling so that spent reads 0% would look tidier and would destroy the one property that
  makes the number worth having
- [x] **Built.** `Quantity.CONDITION`, `FTuning.CALIPERS_RESOLUTION_CONDITION` at 5%,
  `CalipersItem.onItemUseFirst`, `machine/Wearing`, and
  `CrankLinkageBlockEntity.isDriving(level, pos)`. `Instrument` did not have to move, as predicted.
  Worth noting the coarseness cuts the wrong way at exactly one end: the free bands are 2, 13, 20
  and 35 points wide, so 5% is finer than the eye everywhere except the *top* — a sound hammer
  reads 100% whether it has taken one wasted blow or none. Right failure for a crude pair; the
  figure is bought to watch the number move, not to catch the first mistake
- [x] **"Running" is drawing Su.** Decided — intuitive, and consistent without being a
  simulation. **But the code does not support it yet, and this is the thing to know before
  starting:** `getLoadSu()` is a flat figure, so `CrankLinkageBlockEntity` reports the hammer's
  80 Su whether the run is turning or not. A stopped-but-connected hammer books 80 Su on the
  ledger today, so "drawing Su" is currently always true and cannot refuse anything
- [x] **Took the cheap one.** `CrankLinkageBlockEntity.isDriving` asks the six neighbours whether a
  linkage is facing this block and turning above `STOPPED_RPM_THRESHOLD`. Building it clarified
  *why* that is the right shape and not merely the cheap one: a reciprocating machine has no idea
  whether it is working — it is told, one stroke at a time, and between strokes it looks exactly
  like a stopped one. "Is this hammer running" is a question about the shaft, the same way
  overstress is a question about the network rather than about any node
- [x] **The reading does not go stale. Decided: (a).** Two builds. **(a)** the click reports the live figure
  and forgets it. **(b)** the click stamps the figure *and the tick* onto the machine — the same
  two-component trick `ItemHeat` already uses — so the HUD thereafter shows what it read the last
  time the player stopped it, drifting out of date as the machine wears on. (b) is where §8's
  drift and recalibration land, and it has a property worth the wait: wear accrues only on misuse,
  so a correctly built line's reading **never goes stale**, and a bad one's rots fast. How quickly
  the player's knowledge decays is proportional to how wrong the factory is.
  **Taken: (a).** (b) is not shelved so much as reassigned — if a remembered reading is worth
  having it should be a *thing*, a logging instrument of its own, rather than a hidden field that
  makes the ordinary calipers behave strangely. Until then the player writes it in a book and
  quill, which is the same mechanic with better handwriting and none of our code

- [ ] **[OPEN] Wear has no visual.** It is a Jade line and nothing else. A block state at the
  battered and spent bands would make it readable without the HUD, which is where a free sense
  belongs; queued behind real models, since there is nothing to batter yet
- [ ] **[OPEN] Only the hammer wears — and now only half of that is open.** The *read* side is
  general: `machine/Wearing` exists, and a second wearing machine costs one `implements` and
  nothing at the instrument. The *write* side is still `MechanicalHammerBlockEntity.wear()`, private,
  with `HAMMER_`-prefixed constants. That split was deliberate rather than lazy — the consequence
  of a wasted blow is the machine's own business (a bellows takes the same surplus as nothing at
  all), so there is no shared `wear(float)` to write until a second machine disagrees about what
  absorbing force *means*

---

## 4c. Beat 2 — heat — built and verified in game

All of beat 2 compiles, boots on a dedicated server, loads its four datapack tables, and has now been played: one steel ingot made start to finish.

- [x] **Thermal core** — `core/thermal/`. `ThermalBody` (temperature + mass), `HeatSource` (flame temperature, no mass), `Heat` (the maths), `ItemHeat` (a workpiece's own heat). Flow is driven by a *difference*, not a flat rate — see `Heat` for why the flat version made insulation a trap
- [x] **Workpiece heat, everywhere** — a stamp plus a timestamp on the stack, computed on demand. Cools in a chest, a hopper, an unloaded chunk, or a mod we have never heard of, because nothing has to remember to cool it. Cooling is linear, not exponential; the reasoning is in `Heat.cooled`
- [x] **Firebox** — burns fuel from a datapack table, publishes a flame temperature, takes air. It heats nothing and has no idea what is above it
- [x] **Crucible, small and large** — one class, one block entity type, and the *only* difference is thermal mass. Everything the slice claims about the pair falls out of that number
- [x] **Insulation** — a block you stack against a vessel. No block entity, no behaviour; the vessel counts its neighbours
- [x] **Powered Bellows** — `Reciprocating`, so it reuses beat 1's crank linkage, and the long throw is the useful one. Its `getMaxStrength()` is its long throw, so gearing buys stroke rate and never more air per stroke — which is the physics, not a restriction
- [x] **Thermometer** — a carried instrument, not a cover. One class, zero display code touched, exactly as the `Instrument` interface promised
- [x] **Bimetallic Strip** — reads an adjacent vessel, switches any adjacent `Switchable`. No setting on it (§3), and it closes the loop with no logic block anywhere
- [x] **Carburizing, and burning the batch** — band, hold, max heating rate, spoil temperature. Four fields, four genuinely different failure modes
- [x] **Quench** — no machine. Throw the hot ingot in any water. A quench tank was drafted and cut for having no decision inside it
- [x] **Hot working** — the hammer gates on the workpiece's own temperature, which is where the two beats meet
- [x] **Four datapack tables** — `deformation`, `thermal_process`, `quench`, `fuel`. This settles §2's data-driven recipe format by precedent
- [x] **Jade and JEI** — thermal cards, adjectives on the HUD, exact requirements in the browser. A sealed vessel says *sealed*, not nothing
- [x] **Verified by eye.** A crucible over a lit firebox climbs, the bellows raises it past 1420, the strip cuts a clutch, a small crucible visibly burns the batch where a large one does not, an ingot cools on the walk to the hammer, and quenching in water produces hardened steel
- [ ] Real models. Every beat 2 block is a vanilla texture on a box

### Numbers, simulated but not played

Worked out arithmetically and recorded here because they are the whole balance argument:

| | small crucible | large crucible |
| :--- | ---: | ---: |
| time constant | 40 t | 400 t |
| equilibrium, no air | 1172 Tu | 1172 Tu |
| equilibrium, full air | 1758 Tu | 1758 Tu |
| climb rate in band, full air | 7.9 Tu/t | 0.79 Tu/t |
| swing per 20 t read interval | **159 Tu, peaks 1599** | 16 Tu, peaks 1456 |

Steel's window was 1420–1480 Tu and iron burned above 1540 at the time this table was measured; both have since moved down 70 Tu (to 1350–1410 / 1470) because the Crude Blast Furnace, rebuilt as an independent block with its own fuel table, could not reliably reach the old window even fully bellows-blown on its hottest fuel (blaze rod, 1400 Tu unblown) -- see the "total conversion" section below. The table's own figures are frozen at the old window and not re-simulated. So a bare charcoal fire **cannot** make steel at any patience (1172 Tu, a real hard gate), air is the only way across, and the same thermostat that holds a large crucible in the window drives a small one straight past the spoil point. Carburizing's 5 Tu/t heating limit then makes the small crucible reset its hold on every stroke — it is not *locked out*, it needs a gentler, carefully geared draught, which is §7's rule that precision never gates hard.

### Decisions taken while building beat 2

- **Heat flows on a difference, and the flat-rate version is the trap worth recording.** A fire delivering a flat `Work/t` is the obvious first implementation and it looks fine until insulation exists: the vessel settles at `supply ÷ leak`, so halving the leak *doubles* the final temperature and insulating a crucible makes it run away. The slice's "large insulated crucible wins" became "large insulated crucible burns the batch". Driving on `(fire − vessel)` fixes it by construction and pays for lava for free.
- **Lava needed no code.** It is a fire fixed at 1200 Tu, so a crucible over lava sits just under 1200 Tu forever — the most stable heat source in the game, and permanently too cool for steel. The slice asked for exactly that and no rule was written.
- **Item cooling is linear, reversing an exponential.** See `Heat.cooled`. The argument is game design, not physics: an exponential never arrives, needs an arbitrary floor, and hides the working window in a flat tail.
- **The bellows' force ceiling is its own long throw**, so the linkage's gear-advantage clamp bites immediately. A bellows holds what it holds; squeezing harder finds no more air inside it. One mechanism, two honest answers — gearing buys force on the hammer and stroke rate here.
- **The strip reads every 20 ticks**, and that lag is load-bearing rather than an optimisation. A sensor with no response time would make the loop tighter than any physical part could be, which would quietly delete the problem the block exists to hand the player. Response time is one of §8's six apparatus properties and this is the first place it has cost anything.
- **A free sense has a range.** The adjective scale ends at 1600 Tu, and iron's spoil point is 1540 — no, *above* what the eye resolves cleanly. A player watching the glow cannot see themselves crossing the line that ruins the batch. That is not a trick; it is why the thermometer exists.
- **Fuel is a table, not a constant**, and vanilla burn times are deliberately not a fallback — a burn time counts items smelted and carries no temperature.

### Read TerraFirmaCraft, and the licence is not Create's

Reference checkout at `../TFC-reference`. **TFC is EUPL-1.2 — strong copyleft, unlike Create's MIT, but adaptable.** EUPL-1.2's Article 5 Appendix lists GPL v3 as a Compatible Licence, so a derivative may be distributed under GPL-3.0 — which is what Feedback now is. The condition is that the election is *recorded* at the site. It is still read for *design* only, because the working rule stays stricter than the law; every place it informed one is named in `THIRD-PARTY-LICENSES.md`.

What came of reading it:

- **Confirmed, not derived:** TFC stores item heat as `(capacity, lastTemperature, lastTick)` and computes it lazily. That is what was already built here, arrived at independently. Worth knowing the shape is load-tested rather than clever.
- **Changed:** item cooling went exponential → linear, after seeing TFC's `adjustTemp`.
- **Changed:** the adjective scale went from 6 wide bands to 10 plus a range limit, after seeing TFC's `Heat` enum. Six bands put steel's entire window inside one adjective, which makes the free sense useless rather than coarse — and a gap that is merely "hot or not" is a wall, not a gap.
- **Changed:** fuel became a datapack table, shaped like TFC's `Fuel`. Which is also the shape §15 already wanted.
- **[OPEN] Not taken: per-material heat capacity.** TFC gives every item its own. Deferred because slice 1 has one hot material, so it creates no choice — it becomes worth having the moment a player must decide *which* of two hot things to carry first.
- **[OPEN] Not taken: catch-up for unloaded devices.** TFC has a calendar and burns fuel across time skips. Our *items* get this free from the stamp; our *blocks* do not, so a crucible in an unloaded chunk freezes. Known gap.

### Still open after beat 2

- [ ] **[OPEN] The Crude Blast Furnace is not built.** §15's vanilla rework — thermal bands on the furnace, smoker and blast furnace via mixin, and the fallback smelting constant — is a whole separate subsystem and was left out of this pass rather than done badly. Beat 2 works without it: the crucible route stands alone. What is missing is the *lesson* — that the demanding material was available the whole time, by hand, before any instrument. That is §7's sharpest claim in the slice and it currently has nothing to land on
- [ ] **[OPEN] Nothing removes heat, deliberately** — and the player has no way to ask for cooling. That is slice 2's damper, earned by withholding it
- [ ] **[OPEN] Steel has no further thermal overrun.** Once it is steel it sits in the fire indefinitely. Iron burning is the only thermal overrun in the slice, where the mechanical chain has plate → foil → scrap. Probably fine — one beat has to teach that overrun is sometimes simply a loss — but worth a second look
- [x] **The hammer wears on a cold workpiece** — and it was indeed the same answer as the `St` ceiling's silent clamp, so both closed together. See "Wear, and the one rule behind it" in §4b

---

## 4d. The cover system — queued, and deliberately early

The thermometer **should** become a cover eventually. Right now it is a carried instrument,
which is the right call for one instrument and the wrong shape for five.

Build the infrastructure **before** it is necessary, for the same reason `Instrument` was
written before any instrument existed: a high fixed cost paid once beats no fixed cost and a
medium variable cost paid per feature, and the crossover is earlier than it feels. The
thermometer then becomes one class that touches no display code, exactly as it did the first
time.

- [ ] **Read GregTech CEu Modern's cover system first.** Checkout at
  `../GregTech-Modern-7.5.3`. **LGPL-3.0 — design only, no code.** See
  `THIRD-PARTY-LICENSES.md` for why "weak copyleft" does not mean what it sounds like.
- [ ] The split worth studying is three-way, and the split is the lesson, not any class:
  `CoverDefinition` (what kinds exist) · `CoverBehavior` (one attached instance, knowing only
  its holder and its side) · `ICoverable` (what it means for a block to accept covers) ·
  `IIOCover` / `IUICover` (optional capability interfaces a cover opts into, rather than a
  base class every cover pays for)
- [ ] The last of those maps straight onto `Instrument` and is the bit to get right
- [ ] **What already wants to be a cover:** the thermometer, the bimetallic strip (currently a
  block, and it was a cover in the slice draft), and whatever slice 2's damper turns out to
  need. Three is enough to justify the fixed cost; one was not
- [ ] `ThermalBody.hasThermowell()` is already the right seam — a vessel declaring whether it
  can be got at is exactly "does this block accept covers", narrowed to one quantity. Widen it
  rather than replacing it
- [x] **Narrowed once already, before the cover system exists.** It used to gate *any* instrument
  outright; it now gates only passive/ambient reading (a HUD card, and eventually a cover's
  auto-attach). A carried thermometer can still take one manual reading of a sealed vessel —
  see `ThermometerItem.onItemUseFirst` — the same shape as calipers reading a wearing machine.
  Worth remembering when covers arrive: the cover is the *ambient* watcher this flag was always
  actually about, not a second gate on top of the manual dip

---

## 4e. The Crude Blast Furnace and vanilla thermal bands — superseded, see §4f

**This whole section describes a mixin pass that has since been ripped out and replaced.** Every
mixin it names below was deleted on 2026-09-13, the same day it was first actually played: the
first real in-game test found that redoing a vanilla block's GUI from inside a mixin meant
fighting one vanilla-internals assumption at a time, and each one only surfaced by crashing
(`Slot.index` never set on a substituted slot, `canPlaceItem` hardcoded per index, an arrow baked
into the background texture rather than drawn dynamically, and more). §4f is the replacement —
three independent blocks, no vanilla class touched at all — and is the current source of truth for
this system. The bullets below are kept as a record of what was *learned* (the food/metal split,
the recipe pooling, the Smoker's ceiling) which all carried over into §4f unchanged; only the
*implementation* they describe is gone.

- [x] **One mixin on the shared abstract base, two small siblings.**
  `mixin/AbstractFurnaceVesselMixin` targets `AbstractFurnaceBlockEntity` — the one place in the
  mod something is genuinely mixed in rather than added, because there is no other seam. It
  cancels the vanilla tick outright and hands the whole thing to
  `core/thermal/VanillaVessels.tick`, which knows nothing about Mixin. `SmokerVesselMixin` and
  `BlastFurnaceVesselMixin` are plain method overrides on the two concrete subclasses — no
  `implements` clause, no shadow, just a same-signature method the JVM dispatches normally once
  woven in
- [x] **The vessel is its own fire.** Unlike the crucible, there is no separate firebox — the
  fuel slot is lit straight off the same `FuelTable` the firebox already reads, so a pack that
  teaches its own coke a hotter flame teaches it to the Blast Furnace for free. `HeatSource` is
  implemented too, which means a crucible someone places on top of a lit vanilla furnace now
  reads its live flame temperature automatically, through `HeatSource.below`'s existing
  `instanceof` check — nobody wrote that, it fell out of the interface being generic
- [x] **Recipe-type whitelists actually came off.** `process/VanillaFallback` pools vanilla's
  `smelting`, `blasting` and `smoking` recipe types and searches all three for any vessel's
  input, returning the shortest-cooking-time match. Which block it is sitting in decides nothing;
  only temperature does. A recipe's own type is read exactly once after that, as a heuristic for
  "is this food" (`isFood`) — a fact vanilla already published about the recipe, not an identity
  check on the item
- [x] **The fallback, computed rather than guessed at.** `FTuning.FALLBACK_WORK_PER_200_TICKS`
  (348,000) is philosophy 15's "one-eighth of one coal's total heat" taken literally: the `coal`
  fuel entry (1180 Tu, 2400 ticks) held at its own flame temperature for its whole burn yields
  `(1180-20)*2400 = 2,784,000` Work; an eighth of that is what is here. A recipe's own cooking
  time scales it proportionally, per the same section
- [x] **Two classes, not a per-material table.** `FOOD_MAX_TU` (400) and `METAL_MIN_TU` (800) are
  the whole of the food/metal split, straight off vanilla's own `smoking` vs. everything-else
  distinction. Crossing `FOOD_MAX_TU` destroys the item outright, on the tick it crosses, same
  rule as steel's spoil point. Below `METAL_MIN_TU` a metal-type recipe simply does not progress
  — no refusal, no message, matching §6
- [x] **The Smoker gets one real clamp; the Furnace and Blast Furnace get none.**
  `SMOKER_CEILING_TU` (600) is a genuine hard wall, because `FIRE_CONDUCTANCE` (0.6) dominates any
  leak this file could reasonably pick — every vessel's equilibrium sits close to its fire's own
  temperature almost regardless of leak, so a Smoker specialised by leak alone would still smelt
  ore given a hot fuel. The Furnace and Blast Furnace differ from each other by mass and leak
  only, which is the genuinely emergent half of the claim
- [x] **The Blast Furnace's floor is time, not a wall.** Considered and rejected: mirroring the
  Smoker with a second explicit clamp. What high mass buys is checked, not assumed — the vessel's
  climb rate is highest near ambient and falls as it nears equilibrium, so at typical fuel
  temperatures the dwell time below `FOOD_MAX_TU` comes out shorter than a food recipe's cooking
  time, so food is destroyed on the way past exactly as §15 says, with no bistable "roaring or
  out" state machine underneath it. `feedback_philosophy.md` §15 now records this arithmetic —
  see "Built as one wall, not two"
- [x] **Fuel table extended for parity**, since the reworked furnace only lights off `FuelTable`
  and no longer consults vanilla's own fuel map: `planks` (700 Tu), `lava_bucket` (1200 Tu,
  matching `FIRE_TU_LAVA`, zero spread — a lava-fed furnace is as stable as the puddle it come
  from), `blaze_rod` (1400 Tu, hotter than charcoal, a real reason to bring one back from the
  Nether). Everything vanilla considered fuel but that has no entry here — wool, saplings, tools,
  note blocks — simply does not light one of these three blocks any more, the same rule the
  firebox already lives by
- [x] **A furnace can genuinely fail to smelt ore now**, and it was not tuned to be a lesson — it
  fell out of picking one number for each side. A plain furnace fed only `logs` (760 Tu) settles
  under `METAL_MIN_TU` (800) and cannot smelt at all; fed charcoal or coal it clears the line
  easily. Worth keeping regardless of whether it was intended twice over
- [x] **An unwatched furnace burns its own food**, on any fuel, given enough time — a charcoal
  equilibrium sits over 1100 Tu, far past `FOOD_MAX_TU`. Nobody wrote that rule either; it is the
  mod's own core pitch landing on a block the player has walked past since their first night
- [x] **`block.minecraft.blast_furnace` renamed** to "Crude Blast Furnace" via a lang key
  override, per the slice doc — room left for a fancier multiblock later without a naming
  collision
- [x] **Mixin wiring is new to this project and is now the precedent.** `feedback.mixins.json` +
  a `[[mixins]]` block in `neoforge.mods.toml`. No refmap, no annotation processor, no extra
  Gradle plugin — confirmed against ModDevGradle's own `testproject` rather than assumed, because
  the toolchain docs do not cover mixins at all. `compatibilityLevel: JAVA_21` is accepted even
  though Mixin 0.8.7 only formally supports up to `JAVA_17` (logged as a downgrade, not an error);
  Ponder's own mixins hit the same ceiling already, so this is a pre-existing condition of the
  dependency set and not something this pass introduced
- [ ] **Unverified by eye.** Nothing confirmed yet: that a lit furnace actually climbs and shows
  fire/light, that a Blast Furnace clears steel's window on bellows air the way the crucible does,
  that a Smoker genuinely refuses to smelt ore no matter the fuel, that food left too long
  anywhere burns, and that `logs` alone really cannot smelt ore in a plain furnace
- [x] **The GUI is redone: input and fuel only, no output slot, a real heat gauge.** Closes the
  flame-icon/arrow placeholder above. `mixin/AbstractFurnaceMenuMixin` and
  `mixin/AbstractFurnaceScreenMixin` (+ `AbstractContainerScreenAccessor`, an accessor mixin for
  the position/size fields `@Shadow` can't reach one class up without a refmap). Nothing was
  found to change in vanilla's own 3-slot container — slot 2 (vanilla's output) is moved
  off-screen for the Furnace and Smoker, and turned into a real second *input* for the Blast
  Furnace, at the same position, so it can hold charcoal alongside iron. `litTime`/`litDuration`
  are real (repurposed, not new plumbing) and drive the flame icon honestly for the first time;
  `cookingProgress` is repurposed to carry temperature to the client instead of a cook-tick count
  nothing outside vanilla's own arrow needed. The gauge itself is banded, not a smooth fill — the
  same ten `HEAT_BAND_TOPS` `Readout` already names in words, so it is no more precise than the
  free sense it stands in for (§8)
- [x] **Every finished item now replaces its input in place — no result slot, on any of the
  three.** Follows straight from the core pitch: a machine cannot tell when it's done, so it was
  always wrong for the "done" item to travel somewhere safe. A batch heats and turns over as one
  mass (no spare slot to park a partial result in, unlike the crucible's open inventory), so a
  bigger stack costs proportionally more accrued work rather than finishing item-by-item
- [x] **Steel is reachable in a Blast Furnace now.** `core/thermal/VanillaVessels.tick` checks
  `ThermalProcessTable` (iron + charcoal, hold-time, spoil — the same multi-ingredient path the
  crucible runs) before falling back to `VanillaFallback`'s single-item vanilla recipes. The
  Furnace and Smoker never expose the second slot, so it stays permanently empty for them and
  they fall straight through to the vanilla pool — no vessel needed to be told it is the ore
  machine (§15)
- [x] **`ThermalBody.hasThermowell()` narrowed, and the Furnace stopped being sealed.** See the
  cover-system section above for the seam change. The Furnace has an open door you can watch the
  fire through and was never actually sealed like the Smoker or the Blast Furnace — treating all
  three identically was a simplification that didn't survive contact with the real objects
- [ ] **[OPEN] The fuel slot still accepts anything vanilla considers fuel.** `canPlaceItem` was
  not touched, so an item with no `FuelTable` entry can still be inserted and will simply sit
  there inertly rather than being rejected. Cosmetic UX gap, not a correctness one
- [ ] **[OPEN] The heat gauge has no hover tooltip.** Calipers and the thermometer both narrate a
  reading through chat; the gauge is silent about which band it's showing beyond the colour.
  Probably fine — it's the free sense, not an instrument — but worth a second look once a cover
  can actually sit on one of these and something needs to react to a number
- [ ] Real models — none of this changed any art, and none needed to

---

## 4f. Total conversion: independent vessel blocks — built and verified in game

2026-09-13. Feedback stopped reworking vanilla's Furnace/Smoker/Blast Furnace in place and started
replacing them outright — `feedback:furnace`, `feedback:smoker`, `feedback:blast_furnace`, three
new blocks with vanilla's own textures (placeholder art, unchanged) and none of vanilla's classes
in their hierarchy. Vanilla's own three blocks are still in the game, inert: their crafting
recipes are disabled (`data/minecraft/recipe/{furnace,smoker,blast_furnace}.json`, overridden with
a `neoforge:false` condition), so a normal playthrough never obtains one. This is a genuinely
different mod now, not a rebalance of vanilla's, and that is the direction this was pointed at on
purpose (see the session log if the phrase "total conversion" needs the context).

**Verified by a player, in game, same session:** placing all three, cooking food to completion in
the Smoker (previously impossible, see below), making steel in the Blast Furnace with a bellows
and a blaze rod, pulling a workpiece out and reading its temperature by hovering it.

- [x] **One class, one `VesselKind` enum.** `machine/vessel/ThermalVesselBlockEntity` implements
  `Container`, `MenuProvider`, `ThermalBody`, `HeatSource`, `Blown` directly — no vanilla
  furnace class anywhere in its ancestry. `VesselKind` (`FURNACE`/`SMOKER`/`BLAST_FURNACE`) is the
  entire difference between the three, same as §15 always described it: mass, leak, ceiling,
  `hasThermowell`. One `BlockEntityType`, shared by all three blocks, exactly like the crucible's
  two sizes already shared one.
- [x] **Nine generic slots, not three fixed ones.** No slot means input, reagent or output any
  more — `ThermalProcessTable` is checked against the whole nine-slot inventory first (steel can
  be satisfied by any two of them, and there is finally a free slot to place a result in, so this
  went back to the crucible's own per-unit completion model rather than the whole-stack workaround
  the old three-slot mixin needed), and whatever it doesn't touch falls through to
  `VanillaFallback` independently, per slot. The fuel slot is separate and set apart in the GUI —
  with nine equivalent workpiece slots there is no one slot for fuel to visually feed.
- [x] **Food actually cooks now.** Two compounding bugs, both fixed: (1) food was completing on
  the same accrued-temperature-above-ambient integral as metal, scaled off a metal-smelting
  constant (`FALLBACK_WORK_PER_200_TICKS`, "an eighth of a coal's total heat") that no food recipe
  could ever satisfy before the vessel's own climb crossed `FOOD_MAX_TU` and destroyed it — food
  now counts plain ticks in band instead, exactly vanilla's own cook-time model, just gated by
  temperature instead of a lit-fire boolean. (2) `SMOKER_CEILING_TU` was 600, *above*
  `FOOD_MAX_TU` (400) — the original reasoning for 600 ("comfortably above FOOD_MAX_TU") was
  backwards; a fire this size closes most of the gap to its own flame temperature in well under a
  second, so a wall merely below metal's floor still leaves the whole gap for the climb to blow
  through on the way past. Lowered to 380, under food's own ceiling, so the Smoker settles there
  and holds rather than crossing it in transit.
- [x] **Steel's window moved down 70 Tu — 1350–1410, spoils at 1470** (was 1420–1480 / 1540).
  Verified in-game reason, not simulated: even a blaze rod (1400 Tu unblown, the hottest fuel in
  `FuelTable`) fully bellows-blown couldn't reliably *hold* inside the old window rather than
  swinging past it — the numbers in §4c's simulated table are frozen at the old window and were
  not re-derived. `feedback_slice_01.md` and `feedback_philosophy.md` updated to match.
- [x] **The bellows works on a vessel directly.** `ThermalVesselBlockEntity implements Blown` —
  a vessel is its own fire (§15), so it takes air the same way `FireboxBlockEntity` does, without
  a separate firebox as the middleman. Caught two bugs building this: the interface was simply
  missing at first (bellows had nothing to call `addAir` on), and once added, `tickServer` was
  still computing its own `fireTu` inline from the raw unblown flame temperature instead of calling
  the now-air-aware `getFireTu()` — so the multiplier existed and was never actually read. Worth
  knowing for tuning a rig: at `STROKES_PER_RPM_PER_TICK` (0.00625) and `BELLOWS_AIR_LONG` (30),
  8 RPM only sustains ~1.5 air/tick against the `FIREBOX_AIR_PER_TICK` (4) needed for a full blow
  — the flame swings between unblown and heavily-blown rather than holding steady at low RPM.
- [x] **A workpiece is stamped with real temperature when it leaves, not before.** A custom
  `HeatStampingSlot` on the nine workpiece slots (`ThermalVesselMenu`) calls `ItemHeat.set` in
  `onTake`, same "stamped at the moment it leaves" rule the crucible's own `removeItem` already
  follows. A mid-cook item has no component of its own for exactly this reason, which is why the
  GUI's own tooltip override (below) has to read the vessel's live reading instead for those slots.
- [x] **Item temperature is visible by hovering it, anywhere — a new global tooltip listener, not
  a GUI feature.** `client/ItemHeatTooltip` (`ItemTooltipEvent`) adds `Readout.temperatureReading`
  to any stack carrying the `TEMPERATURE` component, in any inventory, any mod's screen. This is
  deliberately the free sense (§8) an eye already has, not something bought by owning a
  thermometer — a carried thermometer only sharpens the same line from an adjective to a figure,
  through the same `Readout` call every other reading already goes through.
- [x] **Two Screen bugs, both "missing a call vanilla makes for you when you extend its class,"
  and both easy to miss because nothing else broke.** `AbstractContainerScreen.render()` does
  *not* call `renderTooltip()` on its own — every vanilla screen overrides `render()` and calls it
  explicitly after `super.render()` (see `AbstractFurnaceScreen`) — so `ThermalVesselScreen` never
  showed *any* tooltip, for anything, until `render()` was overridden to add that one call. The
  temperature-tooltip feature above shipped first and looked broken; the actual bug had nothing to
  do with temperature.
- [x] **JEI's own smelting/blasting/smoking categories, hidden.** See "JEI take-over" near the top
  of this file for the full record, including a same-session correction.
- [ ] **[OPEN] The fuel slot still accepts anything `FuelTable` recognises but nothing vanilla
  doesn't** — the inverse of the old mixin pass's gap. A `FuelSlot.mayPlace` override already
  exists (`ThermalVesselMenu`); nothing further needed unless a fuel item wants to be *rejected*
  for a reason `FuelTable` itself does not already encode.
- [ ] **[OPEN] Making the raw vanilla furnace/smoker/blast furnace block itself inert** (rather
  than merely uncraftable) was considered and deliberately not done — it would mean mixin-patching
  `newBlockEntity`/`getTicker`/`openContainer` on three vanilla block classes, exactly the fragile,
  one-crash-at-a-time surgery this whole rewrite exists to get away from, to guard against an edge
  case (another mod or a datapack handing the player a vanilla furnace) that disabling the
  recipe already covers for a normal playthrough.

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
