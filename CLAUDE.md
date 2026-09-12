# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repository is

This is the repository for **Feedback**, a Minecraft technology mod. It is a design-first project: the mod was specified before a line of it was written, and the specification documents live here alongside the source.

The design documents remain the spine of the project; the source is now being written against them.

**Platform: NeoForge on Minecraft 1.21.1**, built with **ModDevGradle**. 1.21.1 was chosen over a newer version deliberately — the newer backend's data-driven item work suits a mod built on "properties and tags matter more than the id," but 1.21.1 is where the players are.

## Build and run

```bash
./gradlew build          # jar -> build/libs/feedback-<version>.jar
./gradlew runClient      # dev client
./gradlew runData        # datagen -> src/generated/resources
./gradlew runGameTestServer
./build-and-deploy.sh    # build, then deploy to the Feedback PrismLauncher instance
```

`build-and-deploy.sh` clears its own stale `feedback-*.jar` from the instance `mods/` folder before copying, because two jars with one mod id is a launch crash. It touches nothing else in that folder and has no packwiz step — Feedback is a standalone mod, not a modpack coremod.

Java 21 is the machine default and the correct toolchain; no `JAVA_HOME` override, unlike the sibling ForgeGradle repos.

Versions live in `gradle.properties` — NeoForge `21.1.250`, ModDevGradle `2.0.147`, Parchment `2024.11.17`, Gradle `9.2.1` via the wrapper.

## Source layout

Root package `io.github.cloby0.feedback`. The package tree follows the mod's **separation of systems**, not Minecraft's registry categories — measurement, control and actuation are separate systems the player wires together, and the code says so:

| Package | Holds |
| --- | --- |
| `registry/` | every `DeferredRegister` — one place to look |
| `core/unit/` | the units of §17 as types. **Not built** — rotation uses named floats (`rpm`, `loadSu`). Worth doing once thermal lands and two units can be confused |
| `core/rotation/` | the mechanical network — `RotationNode`, `RotationNetwork`, `RotationPropagator` |
| `machine/` | physical operations. A machine class never names a recipe |
| `process/` | operation definitions, completion, and overrun behaviour |
| `instrument/` | sensors. Read-only by construction — an instrument has no way to act |
| `control/` | controllers, timers, data links |
| `actuator/` | things that start and stop a supply |
| `data/` | datagen |
| `client/` | renderers and screens |

Create a package when there is something to put in it; don't scaffold empty ones.

**Datagen is not written yet.** `src/generated/resources` is a resource root and is committed, but blockstates, models, loot tables and lang are hand-authored under `src/main/resources` for now. Convert when the roster is big enough that hand-editing starts going wrong — not before.

**All art is placeholder.** Every texture currently referenced is a vanilla one. No Create asset may ever be used here: Create's *code* is MIT, its *assets* are All Rights Reserved. `THIRD-PARTY-LICENSES.md` records what we owe and to whom.

### The rotation network, in one paragraph

A `RotationNode` is a block entity that knows its own speed and which neighbour drives it. It cannot tell on its own whether it is overloaded — that is a whole-network question, answered by `RotationNetwork` (the Su ledger) and pushed down. When anything changes, `RotationPropagator` **rebuilds the entire connected run**: flood fill, find the strongest source, walk outward assigning speeds. Create propagates incrementally instead, and needs a "flicker score" to break blocks caught in propagation loops; a rebuild visits each node once and cannot loop, so we don't. Overstress reads as zero speed everywhere, but propagation deliberately uses *theoretical* speed — otherwise an overloaded network would tear itself down and rebuild the instant the load came off.

## The documents

The design is intended to live in **three documents on a spectrum from idea to implementation**:

| File | Role |
| --- | --- |
| `feedback_philosophy.md` | **Document 1 — authoritative.** Identity, principles, constraints. Worked examples only where they prove a principle is real. Open questions are marked `**[OPEN]**` inline and collected in §18; §19 lists things that look decided and are not. |
| *(document 2 — mechanics)* | **Does not exist yet.** How each principle is implemented: unit arithmetic, the sensor/actuator matrix, the thermal model, energy networks, overrun band tuning, the control node set. |
| *(document 3 — content)* | **Does not exist yet.** The roster: every item, material, machine, cover and process, and what each does. |
| `feedback_slice_01.md` | **Working document, not one of the three.** The first playable vertical slice, built by `feedback_philosophy.md` §20's method — two beats (mechanical repetition, then thermal control), copper then steel. Concrete but explicitly placeholder-numbered. Its rules feed document 2; its objects feed document 3. Where it and the philosophy disagree, the philosophy wins. |
| `speculative_physics_inspo_doc.md` | *A Speculative Physics and Biochemistry Compendium of Minecraft (Vanilla and Modded)* — the user's own worldbuilding document, ~2800 lines. Explicitly **not canon**. A **mindset reference** (how to reason about Minecraft phenomena scientifically) and a parts bin — the Liquid Teleportant chain and the "flagged exception" treatment of Redstone were already lifted from it. |
| *(`feedback_notes_i.md`, `feedback_notes_ii.md`)* | **Deleted in `4020934`**, recoverable from `404a0c3`. Write-ups of earlier design conversations with a different AI, fully merged into `feedback_philosophy.md` with their conflicts resolved. Don't restore them; don't cite them as current design. |

## The core pitch — state it precisely

Machines do not recognize when a recipe is complete. They keep performing their physical action for as long as they have input and power. Continued operation past completion **acts on the already-finished output**, changing, degrading, spoiling, or endangering it.

The last clause is the whole differentiator. GregTech 6 already has machines that idle and burn fuel forever; "always-on machines" is not the novel part. *The output is perishable to continued processing* is. Preserve this framing when describing the mod.

## Hard design rules

These are settled and constrain any proposal:

- **No identity checks.** Nothing in the mod may route or sort on item identity ("if item == iron ingot"). All sorting exploits physical properties — density, magnetism, particle size, optical/reflectivity — each with real, exploitable weaknesses.
- **Electricity is not the universal currency.** Mechanical, Thermal, and Chemical are natively consumable and never *have* to convert to Electrical. This is the explicit point of departure from GregTech, where everything funnels to EU.
- **Machines define physical operations, not recipes.** A machine says "I apply this operation to whatever is here," never "I know how to make Copper Plate."
- **Discrete options, not continuous knobs** (§3). Where a setting would be a free scalar, it is a small set of named choices instead — crank throw is short or long, never 1–64. A scalar has a correct answer in it; that leaves only grinding for it or knowingly playing suboptimally. Depth comes from stacking choices, never from tuning one finely.
- **A problem with one answer is not a problem** (§5). Any automation problem should have at least two correct approaches that cost different things — capital against attention, throughput against reliability, capability against walking away.
- **Workpieces carry state too** (§9). A heated item is hot wherever it is, cooling toward ambient in hand, in a chest, in transit. Machine adjacency is physical, not cosmetic, and consequences of an item being hot are allowed to follow anywhere.
- **Upgrades are physical components** you could point at — larger vessel, thicker insulation, flywheel, finer screen, better seal. "+50% throughput" is not a design concept; the question is what physical change causes it.
- **Measurement, control, and actuation are separate systems** the player wires together. A sensor controls nothing; an actuator knows nothing; the controller has no built-in target. Precision is emergent from the whole loop, never a machine stat.
- **Precision is never a hard gate.** Hard gates are genuine physical impossibilities (insufficient temperature, strength, work, pressure, material limits). Anything precision-limited stays *possible* — just unreliable and uneconomical. The shorthand: *the recipe is not locked, the process is difficult.*
- **Manual production stays theoretically possible** for a surprising share of the game, at tiny rates. Automation makes processes practical; it does not unlock them.
- **Sensing and stopping are separate problems, with distinct hardware per energy type** — and each cutoff has its own failure character (breakers arc and wear, clutches coast, thermal mass dissipates slowly, closing a reagent valve doesn't stop the reaction already underway).
- **Don't build a physics simulator.** The test for any variable: *does modeling this create a meaningful engineering choice?* If not, abstract it away. Real engineering having the variable is not a reason.

## Conventions

- **Units:** `Tu` temperature — a *state*, not an amount of heat — `Pu` pressure, `Fu` cumulative mechanical work, `St` per-application strength (the two are **not independent** — work delivered per blow is `St ÷ material hardness`, so what a blow accomplishes is a property of the material, never a machine stat), `Su` stress/load (Create's meaning — never speed), `RPM` rotational speed, `mB`/`mB/t` fluids, `Mu` mass (density is `Mu/mB`), `Qu` amount of substance (concentration is `Qu/mB`; a mole stand-in with no atomic implications), `Eu` electrical — voltage *and* current, the one non-scalar unit. Generic untyped energy is `Work`, spelled out and deliberately unabbreviated so it doesn't read as another currency; `Eu` is electricity's unit, not the mod's. Resistance is deliberately not modelled. Derive rates rather than invent them — `Tu/t`, `Eu/t`, `mB/t`. Show time as both: `600 t (30 s)`. Handy: 1 mB is exactly 1 litre (1000 mB fills a 1 m³ block), so `Mu` anchors to the kilogram and water is exactly `1 Mu/mB`.
- **`[OPEN]`** marks an unresolved question inside the notes. Adding one is a legitimate outcome; silently resolving one is not.
- **Hard gate vs. soft gate** (§7) is the load-bearing distinction: a hard gate is physical impossibility, a soft gate is a *reproducibility* gate — you can still succeed by luck. Precision only ever gates softly. If a proposal needs "requires tier N," it's wrong.
- **Instrumentation is a yield technology, not a key** (§7). It unlocks nothing; it's bought because it improves the conversion ratio. Corollary that makes this work: failed batches must consume their inputs — the waste *is* the gate.
- **The world resolves on truth, not on readings** (§8). Success is computed from real conditions; instruments never enter the calculation — a bad sensor costs you reproducibility, never success.
- **There is an irreducible noise floor** (§8). No apparatus reaches zero variance, deliberately: if a factory could become fully deterministic, the optimal endgame is timers and the whole sensing layer becomes a discardable scaffold. Better equipment narrows the distribution, never collapses it. Model a variable only where the player can act on it; the rest is honest noise, not a simulated stand-in for noise.
- **Difficulty stays flat; novelty goes up** (§14). Tolerances tighten, but tools improve in step — difficulty is the *ratio* of required to achievable precision, held roughly constant. What changes is the *kind* of hard (narrow → coupled → unstable → path-dependent), not the amount. A proposal that makes old work harder is wrong.
- **Specialization should be emergent, never enforced** (§15). The vanilla Smoker and Crude Blast Furnace keep their traditional niches purely through thermal behavior — the recipe-type whitelists come off. If a proposal needs a whitelist to produce the right outcome, the physics isn't doing its job.
- **The controller is a switch, never a dial** (§13). Its only output is starting or stopping a supply — no proportional control exists. Oscillation is fixed with physical mass, not a better algorithm. Capability never tiers; only iteration cost and how many sources it can read.
- **"Which axis does this advance?"** (§14) is the question to ask of any proposed technology — progression is a profile across ten axes, not a rank.
- **`feedback_philosophy.md` §19 lists explicit non-conclusions** — tier names, first machines, first materials, numbers, chemistry implementation, separator mapping, progression boundaries, control block implementation. Treat these as deliberately open. ("McGuffnium" appears in the old notes as a metaphor only; it is not a material.)
- These are the user's own documents and a live design conversation. Propose and argue for changes; don't rewrite settled sections unprompted.

## Outstanding work

1. **Write document 2 (mechanics).** Not started. `feedback_philosophy.md` defers to it by name throughout — unit arithmetic, per-energy sensor/actuator pairings, the thermal model, overrun band tuning, the vanilla vessel thermal bands.
2. **Write document 3 (content).** Not started, and downstream of 2.
3. **Project scaffold is done** — Gradle, run configs, Parchment, deploy script, mod entrypoint. The remaining §2 item is the data-driven recipe format (§15 wants compat authored as a table, not code).
4. **Slice 1 is fully drafted** in `feedback_slice_01.md` and every open decision at its end is now closed. The next choice is build-vs-spec — implement the slice, or write document 2 first. Slice 2 opens on tempering, damper and controller, which `feedback_slice_01.md` deliberately leaves dangling.
5. **`TODO.md` is the working checklist** — source of truth for *what's next*, where `feedback_philosophy.md` stays source of truth for *why*. Keep it current as items close.
