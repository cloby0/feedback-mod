# Feedback — Minecraft Tech Mod Design Notes

*Working title: "Feedback." A living document of design decisions made so far. Sections marked* ***\[OPEN\]*** *are unresolved and need further work.*

---

## Premise & Design Philosophy

**The core hook, precisely stated:** machines don't recognize when a recipe is complete. They keep performing their physical action — heating, spinning, agitating, reacting — for as long as they have input and power, regardless of whether the output is already finished. The player has to notice completion from *outside* the machine and act on it. Continued operation past completion doesn't just waste energy — it actively acts on the already-finished output, changing or ruining it.

This distinction matters: GregTech 6 already has machines that idle and burn fuel forever unless manually covered or unplugged. That's not the novel part. The novel part is that the *output itself is perishable to continued processing* — nothing in existing tech mods does that. Keep this exact framing when describing the mod's core pitch.

**Priorities, in order:** mechanical depth \> playfulness \> realism.

**"Scientific, not realistic"** — this is a deliberate distinction from mods like TFC or GregTech, which mostly strip out or ignore Minecraft's existing magic/fantasy elements in favor of real-world physics. This mod does the opposite: it treats everything that demonstrably exists in the Minecraft world (Blazes, Ender Pearls, lightning capture via Channeling, Creepers, Chorus Fruit) as real phenomena with untapped industrial potential, and asks "how would a scientifically-inclined mind industrialize this?" The goal is that new tech feels like a *diegetic discovery* — something that was always possible in this world but nobody had built yet — rather than a "tech layer" bolted onto a fantasy world.

**Reference material:** the user's own document, *A Speculative Physics and Biochemistry Compendium of Minecraft (Vanilla and Modded)*, is a **mindset reference**, not binding lore. The mod does not need to adhere to its specific lore, but its ideas are fair game to lift wholesale (it's the user's own work), and several already have been — see the Spatial Energy and material-exceptions sections below. If you're an agent reading this, and you want that document, just ask for it.

**Progression premise:** the player is progressively discovering what the Minecraft world actually does. The technology tree is therefore a history of understanding: observation → continuous processes → scale → multiple energy phenomena → measurement → controllable material properties → separation → controlled chemical transformation → extreme conditions → systematic study of Minecraft-exotic phenomena → engineering and reproducing those phenomena.

**Magic is baked into the science from the beginning.** Minecraft-exotic phenomena should appear wherever the player has enough understanding to notice, measure, or exploit them, rather than being reserved for an early-game gimmick or a final “magic tier.” A Blaze Rod is a strong model: if the player discovers a material that is extraordinarily good at producing or retaining useful heat, later technology should investigate *why* it behaves that way, characterize the mechanism, isolate the useful property, and eventually engineer better ways to reproduce or exploit it. The natural magical material is therefore not a disposable early-game fuel; it can become the starting point for an entire scientific discipline and later synthetic technologies. The guiding question is: **“What does this impossible thing actually do, and what can we build once we understand it?”**

This is the intended meaning of **“scientific, not realistic.”** The mod should use real scientific habits — measurement, controlled experiments, material properties, process conditions, repeatability, instrumentation, scale-up, and engineering — while treating Minecraft rules as the actual laws of nature.

---

## 

## 1\. Machines — Physical Capability vs. Observation

Machines should define the **physical process they perform**, not a magical recipe list. A machine is a way to repeatedly apply some physical operation — heat, grind, mix, press, separate, react, pump, etc. — under defined conditions. The machine itself does not necessarily know when the desired result has been reached.

**Upgrades are physical components, not abstract stat labels.** An upgrade should be something a person could point to on the machine: an **enlarged vessel**, thicker insulation, a larger flywheel, better bearings, a finer screen, a heat-resistant lining, a better seal, a precision valve, a stronger pressure-rated vessel, a more capable agitator, an improved heating element, etc. Components must make sense for the machine and respect its material limitations. “+50% throughput” is not a design concept by itself; the question is what physical change causes the machine to handle more throughput.

The major machine-development axes are **throughput** and **precision/controllability**, not arbitrary recipe unlocks or hard voltage gates. More advanced machines may process larger batches, maintain tighter conditions, react faster, tolerate more severe environments, or provide a better physical basis for instrumentation, but they should not simply become “the same machine in a new material.”

**Observation is a separate layer.** Machines do not automatically become smarter as they improve. Covers determine what the outside world can learn about the machine. A better machine can still be difficult to control if it has poor instrumentation, while a crude machine can become remarkably reliable once the player learns how to measure it.

**Manual production remains deliberately possible in principle.** Most recipes/processes should be theoretically reproducible by hand with enough knowledge, tools, workspace, and attention. Manual rates should usually be tiny — often one item at a time, or one successful result only occasionally — so that automation becomes necessary for practical scale rather than because the recipe is metaphysically impossible without a machine. A “100% Feedback, no automation” run should be funny and technically possible for a surprising portion of the game.

**Vanilla processing must obey the same underlying systems.** The vanilla furnace is not exempt from the mod’s thermal model. If the mod represents heat as a real continuous quantity and materials can accumulate temperature, the ordinary furnace must be a primitive ancestor of that system rather than an instant raw-item→finished-item black box. Vanilla mechanics should be reinterpreted through the same physical rules wherever necessary to avoid a visible contradiction between “the world” and “the machines.”

---

## 2\. The Core Loop

Four states define any machine's operating cycle:

- **Idle** — loaded, powered, waiting.  
- **Running** — consuming energy, performing its physical action, recipe progress accumulating.  
- **In-window** — the recipe has technically finished; the output exists, but the machine hasn't been told to stop.  
- **Overrun** — continued action past completion begins acting on the *output*, not just wasting energy.

### Overrun severity ladder

Overrun is **not** a single "ruined" outcome, nor is it always a friendly sidegrade — both extremes need to exist:

| Tier Outcome |  |
| :---- | :---- |
| **Sidegrade** | A different, still-useful output. Narrow window; requires the player to understand the machine well enough to aim for it deliberately. |
| **Degrade** | Same output, worse stats or yield. The "I was a little late" outcome. |
| **Spoil** | Total loss. The batch is gone — no byproduct, no partial credit. |
| **Hazard** | The overrun damages the machine itself, or produces something actively dangerous. |

The width of each band is a per-machine tuning knob — a forgiving machine has a long degrade band and no hazard band; an unforgiving one (e.g., an exothermic chemical reactor) might jump almost straight from in-window to hazard. That variance is free machine "personality" and teaches players to respect instrumentation rather than eyeballing everything.

**Examples sketched so far:**

- **Furnace**: past completion, keeps absorbing heat → could work-harden into a harder-but-more-brittle variant (sidegrade) within a narrow band, or burn to slag (spoil) beyond it.  
- **Mixer**: overmixing shear-thins the mixture, separates it back into components, or whips in air to produce a foam byproduct that's an input to a different recipe chain (sidegrade).  
- **Grinder**: continued grinding turns finished grit into dust, dust into powder — a legitimate, deliberate technique for finer-grade material, at the cost of yield (sidegrade if intentional).

---

## 

## 3\. Sensing & Cutoffs — One Mechanism Per Energy Type

Sensing (noticing you've hit In-window) and stopping (acting on that knowledge) are treated as **separate problems**, each with distinct hardware per energy type. This means picking which energy type drives a machine is a real decision, not flavor.

| Energy Cutoff mechanism Response curve Sensor type |  |  |  |
| :---- | :---- | :---- | :---- |
| Electrical | Breaker/relay | Instant, but arcs and wears out under repeated load-cutting | Current clamp (reads current draw) |
| Mechanical | Clutch / declutch gear | Coasts — momentum keeps things moving briefly after disengagement | Torque/vibration tap (reads load/resistance signature) |
| Thermal | Damper / vent | Dissipates slowly — thermal mass gives a built-in soft-stop | Thermocouple probe (reads temperature directly) |
| Chemical | Valve on reagent feed | Stops the *feed*, not the reaction — existing reactants keep reacting until consumed | Reactivity/pH-style probe (reads a fluid/mixture property) |

**Sensors are covers** — they attach to a block face without occupying block space, similar to GregTech's cover system, and compete with power/redstone I/O for a machine's limited attachment slots.

**Timers exist as the cheap fallback**, and must be genuinely beatable, not just risky for its own sake. Recipe time should drift based on **real, learnable conditions** rather than random noise: fuel quality, machine wear, ambient temperature, and batch load all nudge actual completion time. A timer is a bet based on current conditions; sensors are how you stop betting. This keeps timers useful even for experienced players in the right context, rather than being purely a starter-tier stepping stone.

---

## 4\. Machine State Memory

Every machine carries **persistent, hidden state** between recipe runs, none of it directly readable without the matching dedicated sensor:

- **Thermal charge** — cold-start machines run their first recipe slower (heating up); preheated machines run faster. Creates a real strategic choice between idling a machine hot (wastes energy, saves time) and letting it go cold (saves energy, costs time to reheat).  
- **Contamination / residue (fouling)** — running different recipes through the same shared vessel leaves residue that affects subsequent runs, unless cleaned or unless the machine is dedicated to a single recipe/reagent family. This is the direct consequence of Section 5 (no generic filters): a shared multi-purpose machine is cheaper in space/capital but fouls; N dedicated single-purpose machines cost more but never contaminate. A genuine factory-layout tradeoff, not a lore justification.  
- **Wear** — accumulates with use, shows up as drift in a machine's sensor baseline over time (e.g., a vibration sensor's "normal" reading shifts as the machine wears).

---

## 5\. No Generic Filters → Physical Logistics

**Core rule:** nothing in this mod can do an identity check ("if item \== iron ingot, route here"). All sorting and quality control must exploit **real physical properties** instead:

- **Density/gravity separation** — heavier materials sink faster in a fluid column or centrifuge (real jigging/gravity separation from mineral processing).  
- **Magnetism** — an electromagnetic drum separator pulls ferrous materials; everything else passes through. (Magnetism lives under Electrical — see Section 6 — with this separator as its primary gameplay home.)  
- **Size/screening** — a mesh only passes particles under a certain size; relevant since deliberate overrun-grinding produces a size distribution rather than one clean output (Section 5).  
- **Optical/reflectivity** — a light sensor distinguishes visually distinct materials, but is genuinely fooled by visually similar ones — a real, exploitable weakness rather than an edge case to patch out.

This is a much bigger logistics layer than existing tech mods have, since sorting becomes a puzzle of exploiting property differences rather than gating on item identity.

**\[OPEN\]** — mapping the actual roster of vanilla \+ modded ores/materials onto these four separator types has not been done yet. To be designed collaboratively.

---

## 6\. Energy Types

Five types, structurally asymmetric — four behave as generate → store → transmit-with-loss → consume; Spatial does not.

| Energy Native source Transport Powers Role |  |  |  |  |
| :---- | :---- | :---- | :---- | :---- |
| Mechanical | Water wheels, windmills, treadmills, steam pistons | Physical linkage only (shafts/gears/belts); lossy to friction, needs lubrication upkeep | Mixers, grinders, presses, saws, pumps, drills | The industrial workhorse — most material transformation is a shaft turning something |
| Thermal | Fire/lava, contained Blazes, geothermal Nether taps, thermal-mass batteries | Conducts through connected blocks; **steam is the primary vehicle for moving it, not the energy itself** | Furnaces, boilers, evaporators, stills | Transformation-by-heat; feeds mechanical (via turbines) and vice versa (via heat pumps) |
| Chemical | Combustion fuels (coal, wood, oil) **and** reactive intermediates (unstable synthesized compounds) | Moves as physical substance (fluid/item), not a field | Reactors doing real synthesis: alloying, explosives, novel materials | Highest density, highest fouling risk, least forgiving |
| Electrical | Lightning capture (rod \+ capacitor array), or converted from the other three | Wires, lossy over distance without upgrades | Sensors, logic, precision control, electrolyzers | Mostly a control/precision currency, not the default industrial workhorse |
| Spatial (Ender) | Ender Pearls, Chorus Fruit (processed into Liquid Teleportant) | Doesn't transmit — consumed per discrete jump | Signal relay (mid-game), small item/fluid transport (late-game) | The premium, placement-independent escape hatch — never meant to scale |

### Mechanical

Physical linkage only. **\[OPEN\]** — whether to split into reciprocating vs. rotational sub-currencies (à la GregTech's KU/RU) for extra depth is explicitly shelved; revisit once the rest of the skeleton is built.

### Thermal

**Resolved decision:** heat itself is not internally "steam" — steam is just the primary *vehicle* for moving it, the same way electricity is real regardless of which metal it's traveling through.

Two conversion directions, physically opposite:

- **Turbine/engine (thermal → mechanical):** lets heat flow naturally downhill (hot to cold), skimming usable work off the flow. Destructive to the gradient — efficient at *large* temperature gaps.  
- **Heat pump (mechanical/electrical → thermal):** forces heat *uphill* (cold to hot) via a refrigerant cycle (evaporate → compress → condense → expand). Only efficient across *small* gradients — terrible for smelting-level heat, excellent for cheaply maintaining a modest preheat (ties directly into the cold-start/preheat mechanic in Section 4). Also enables real heat-integration gameplay: pump waste heat from something that needs to stay cool into something that wants to stay warm.  
- Left unattended, a heat pump overruns in **both directions at once** — an over-cooled source and an over-heated sink from the same neglected machine. The core loop generalizes cleanly to a new device type.

**Contained Blaze** as a renewable thermal source: a flame-permeable containment cage keeps a living Blaze captive while radiating heat outward. Mismanaging containment (running past thermal tolerance, poor maintenance) doesn't just "break" — it escapes and turns hostile, a hazard-tier outcome with real teeth. (GT6's community successor already has a similar "Infernal Boiler" using a contained Blaze with a heat/charge duality — validates the direction; our differentiator is letting thermal drive machines *directly*, not funneling everything through to electricity the way GT6 does.)

### Chemical

Split into two distinct behaviors:

- **Combustion fuels** (coal, wood, oil) — chemical energy deliberately converted to heat. Just feeds Thermal; doesn't need its own transmission network.  
- **Reactive intermediates** (e.g., a Creeper-precursor compound, or any synthesized unstable compound mid-chain) — stays chemical, moves as a physical, unstable substance, and has a **shelf-life**: it decays into inert waste if it sits too long or travels too far before the next reactor consumes it. A real process-chemistry constraint (many industrial intermediates can't be stored or shipped, only used on-site immediately) that gives Chemical a distinct failure mode — it *expires*, rather than dissipating (Thermal) or resisting (Electrical). Also explains reactor fouling: a dedicated reactor only ever handles one intermediate, so nothing lingers to interfere with the next batch; a shared one accumulates residue from whatever it processed previously.

**Materials backbone:** the real periodic table (conductivity, reactivity families, density) is the default framework for material properties, matching GregTech's approach. Existing Minecraft-exotic materials that don't map to real elements (redstone, Blaze rod material, Ender Pearl material) are treated the way the reference document treats Redstone: not derived from real chemistry, but a **deliberately flagged exception** with its own defined properties, sitting alongside the real table rather than being forced into fake chemistry or left as unexplained magic.

### Electrical

Primary wild source: **lightning capture** via a rod-and-capacitor array — industrializing an ability the world already demonstrates works (Trident \+ Channeling redirects lightning reliably). Bursty and storm-dependent; needs capacitor banks to smooth into something usable — a nice parallel to how unglamorous real electrical infrastructure actually is (mostly buffering, not generation).

Serves as the **default backbone for control-signal transmission** — cheap and wired, but distance-limited like redstone, needing repeaters.

**Magnetism folds into Electrical** as an application (electromagnets), not its own energy type. Its primary gameplay home is the magnetic separator (Section 5).

### Spatial (Ender)

Structurally different from the other four: **consumed per discrete jump**, no generate/store/transmit staging.

**Mid-game — Ender Relay:** bind a Transmitter/Receiver pair using an Ender Pearl *consumed in the binding* (a one-time capital cost, like a lodestone binding a compass). Once bound, the pair passes **data** between them wirelessly, instantly, at any distance or across dimensions.

**Continuous operating cost:** the bound link needs a steady trickle of **Liquid Teleportant** to stay stable — not to power the jump, but to suppress "premature transition events." This directly reuses the reference document's existing industrial teleportation liquid chain:

1. Macerate Chorus Fruit into a raw slurry.  
2. Fractionally distill out the active alkaloid.  
3. **Damping** (Chemical Reactor): stabilize with Lapis-derived silicates and heavy water, reducing premature transition events.  
4. **Catalysis** (Plasma Arc Reactor): introduce a Redstone-derived superconducting phase, producing the final Liquid Teleportant / Ender Medium.

Let the fluid run dry and the link **destabilizes** rather than cleanly switching off — dropped/corrupted signals for the data-relay tier, and a genuine hazard-tier outcome for the matter-transport tier (whatever's mid-transit has nowhere clean to go).

**Late-game — matter transport:** the same bound pair, scaled up, carries a small, hard-capped trickle of items or fluid — enough to keep a remote outpost topped up or sample material back to a lab, deliberately **not** enough to replace belts/pipes as bulk logistics.

This gives Spatial the same two-layer cost structure as everything else (capital \+ operating cost) and the same per-type cutoff pattern (stop the fluid feed → link goes dormant safely, rather than dangerously).

---

## 7\. GregTech 6 — Research Notes & Points of Departure

Researched directly (FTB wiki, "Getting Started (GregTech 6)") since GT6 is the closest existing precedent for a multi-energy tech mod.

**GT6's actual chain:** Heat (HU) boils water into Steam (a real physical intermediate) → splits into **KU** (kinetic/reciprocating, via piston engine) or **RU** (rotational, via turbine) → **EU** (electric, via Dynamo from RU) → **MU** (magnetic, via electromagnets) / **LU** (laser, via electric CO2 lasers) / **QU** (quantum, endgame, converted from laser, powers the Matter Replicator chain). Each machine natively accepts only *one* input type and internally normalizes it to a generic unit (GU, always 1:1) for its own operation — you must build the correct conversion chain to reach whatever type a given machine wants.

**Key findings that reshaped this design:**

- **GT6 machines already run continuously by default**, wasting fuel when idle unless manually covered or disconnected. This means "always-on machines" alone isn't unclaimed territory — it sharpened the core pitch (Section 2\) into specifically "continued operation ruins an already-completed output," which GT6 does not do.  
- **Boiler calcification** (mineral buildup degrades efficiency unless using distilled water; cleanable only when cooled/depressurized) — direct precedent validating the Machine State Memory concept (Section 4).  
- **Boiler overpressure explosions** (restricting steam flow too much builds dangerous pressure, real blast radius) — direct precedent validating the Hazard tier of Overrun (Section 2).  
- **GT6U's "Infernal Boiler"** already uses a contained Blaze for steam production with a heat/charge duality — validates the contained-Blaze thermal source idea (Section 6), though GT6 still funnels everything to EU as the one true currency.

**Philosophical point of departure:** GT ultimately treats EU as one true currency with everything else (heat, steam, chemistry) as an on-ramp toward it. This mod keeps Mechanical, Thermal, and Chemical as genuinely independent, natively-consumable currencies that never *have* to become Electricity — Electrical is one option among several, not the mandatory final form.

---

## 8\. Control & Logic Layer

**Inspiration:** *Steve's Factory Manager* — a visual, flowchart-based programming interface (drag trigger/action nodes, wire them together), not a text scripting language. Its spiritual successor, *Super Factory Manager*, only added an optional text-scripting mode much later, keeping the original flowchart approach available too.

**Design principle:** the difficulty should live in **acquiring and routing the right sensor data**, not in learning to code. The logic itself should be trivially simple.

**Controller block** — an in-world node editor with three node categories:

- **Input nodes:** one per sensor type (thermal, vibration/torque, chemical reactivity, current draw), plus plain redstone input and a clock/timer.  
- **Logic nodes:** comparisons (above/below/within-range), AND/OR/NOT, and a **threshold-with-deadband** (hysteresis) node.  
- **Output nodes:** one per cutoff type (breaker toggle, clutch engage, damper open/close, valve open/close, ender-relay pulse).

**Sensor tiers gate data richness, not just accuracy:** a cheap sensor outputs only a boolean with a fixed built-in threshold; a better sensor outputs the real continuous value, letting the Controller do its own math and thresholds.

**Data transmission reuses the energy transmission rules:** wired data is cheap and local but distance-limited like redstone (needs repeaters); Ender Relay is wireless but throughput-capped and costs the continuous Liquid Teleportant trickle — well-suited to coarse, occasional cross-site signaling, not tight local feedback loops. This naturally produces the expected pattern without forcing it: a small local Controller wired directly to its own machine's sensors for fast reflexes, with Ender Relay reserved for inter-site coordination.

**Real stakes for bad logic:** a Controller without a deadband node causes a sensor reading that hovers near its threshold to flap a relay on and off rapidly — and breakers already wear out from repeated cutoffs under load (Section 3). Sloppy control logic doesn't just work poorly, it actively destroys downstream hardware. This ties programming quality directly to the mechanical stakes the energy system already established, rather than inventing a separate "code quality" mechanic.

---

## 

## 9\. Progression / Technology-Understanding Philosophy

The progression is **not** a set of literal tiers and is explicitly not a GT-style voltage ladder. “Tiers” are useful shorthand for roughly grouped eras of understanding, but the player should experience them as discoveries, problems, and changing industrial practice. A progression step is valuable when the player understands something new about the world, gains a new way to observe or control it, or learns how to apply an old phenomenon at a fundamentally greater level of control.

### Order of understanding — start to end

1. **Materials have physical behavior.** The player learns through direct experience that fire, water, stone, wood, metals, weight, motion, hardness, melting, and similar properties behave consistently. The initial science is phenomenological: “this thing does that.” Minecraft rules are already present here; they are not postponed until an endgame magic layer.  
     
2. **Processes are continuous.** Heating, grinding, mixing, reacting, pressing, pumping, and similar operations do not end automatically when a desired result first appears. This is where the core Feedback loop becomes the player’s model of the world: matter has state, and continued processing moves that state onward.  
     
3. **Processes can be scaled.** Once the player can make a process happen continuously, throughput becomes the next problem. Larger vessels, larger work surfaces, stronger drives, better bearings, pumps, boilers, shafts, and similar physical improvements follow naturally.  
     
4. **The world contains multiple useful energy phenomena.** Water, wind, fire, steam, fuels, Blaze-derived heat, lightning, redstone behavior, and other native phenomena are all recognized as things that can cause useful effects. The player is not yet required to collapse them into one unified “energy currency.” Multiple fundamentally different forms of energy remain real and useful.  
     
5. **Energy can be measured and managed.** The player moves from watching machines to instrumenting them. Temperature, torque/load, vibration, pressure, flow, electrical behavior, chemical state, and similar quantities become observable through covers and sensors. A machine is not smarter because it gained a new body; the player has gained a new sense.  
     
6. **Materials have hidden, exploitable properties.** Better instruments reveal that materials differ in ways that are not obvious by eye. Some properties are useful for separation, control, energy conversion, or process design. Minecraft-exotic materials can begin revealing behavior that is plainly impossible by ordinary real-world expectations, but it is still measured and characterized rather than labeled “magic.”  
     
7. **Mixtures can be separated and purified.** The player learns to exploit density, magnetism, size, optical properties, phase behavior, solubility, and other physical differences. Screening, gravity separation, centrifugation, filtration, magnetic separation, distillation, crystallization, extraction, and similar processes become the backbone of increasingly elaborate material logistics.  
     
8. **Matter can be deliberately transformed.** The player moves beyond merely combining or heating substances and begins recognizing controlled chemical transformation: synthesis, decomposition, redox, precipitation, neutralization, polymerization, alloying, thermal decomposition, etc. Chemistry becomes a process discipline only after enough measurement and process control exists to make it meaningful.  
     
9. **Chemical and physical processes have measurable rates and states.** Pressure, temperature, concentration, residence time, mixing, atmosphere, catalysts, electrical potential, and related variables can be deliberately held within useful ranges. Sophisticated reactors and multi-stage process chains become natural rather than arbitrary recipe gates.  
     
10. **Extreme conditions reveal new phenomena.** Once ordinary process variables can be controlled tightly, the player can intentionally explore high temperature, high pressure, strong electrical or magnetic fields, unusual atmospheres, vacuum-like environments, intense mechanical stress, and other extreme states. New processes should arise from what those conditions reveal.  
      
11. **Minecraft-exotic phenomena become subjects of systematic science.** Blaze material, redstone, Ender phenomena, Chorus phenomena, Nether-specific materials, lightning behavior, and eventually other fantasy systems are treated as coherent parts of nature. The player does not “enter the magic era”; they discover that their existing scientific framework was incomplete.  
      
12. **Natural anomalies can be engineered.** The player moves from using naturally occurring impossible materials to characterizing their mechanism, isolating the useful property, reproducing it under controlled conditions, and finally manufacturing superior analogues or devices that create the phenomenon directly. A natural Blaze Rod is therefore not the end of thermal technology; it is an existence proof that motivates an entire branch of engineering.

### Progression events should have meaning

A major progression step should ideally satisfy the four-part test:

- **Sense** — introduce a genuinely new sensor, measurement capability, or way to observe a phenomenon.  
- **Make** — enable a process that was categorically impractical or impossible before, not merely a faster recipe.  
- **Need** — introduce a problem that specifically rewards or requires the new capability.  
- **Return** — by the end of the era, earlier investments start paying off, while the player sees the outline of the next unresolved problem.

A particularly strong pattern is **“detect before you can act.”** The player may first obtain a way to detect a phenomenon well before they can exploit it industrially. For example, a radiation sensor can teach the player that certain materials emit something unusual long before nuclear machinery becomes available. This makes the world feel like a continuous field of discoveries instead of a sequence of recipe unlock screens.

### Energy progression should be visually obvious

The energy systems deliberately produce a visible industrial progression without relying on recolored machine tiers. The important change is not simply that machines get faster; the **physical infrastructure around them changes**.

**Early game:** essentially no electricity. Factories are mechanically and thermally legible: shafts, gears, belts, water wheels, windmills, pistons, flames, boilers, hot vessels, and direct material handling dominate the visual language. A small early workshop should visibly look like a machine shop or primitive industrial plant.

**Middle game:** electricity exists, but it is introduced because some jobs genuinely benefit from or require it — especially sensing, precision control, electrochemistry, and electromagnets. Electricity may itself still be produced from thermal/mechanical systems, such as fuel → heat → steam → turbine → generator. A mid-game factory can therefore contain both moving mechanical infrastructure and electrical cables. Steam and cogs do not disappear; they share the factory with wiring, instrumentation, relays, and more controlled process equipment.

**Late game:** electricity becomes the dominant method of transporting usable energy because it is compact, controllable, and convenient for distributed machinery. Mechanical and thermal processes do not cease to exist, but large-scale transport of energy is increasingly electrical, with localized conversion near the machine. Starting around the mid-to-late game, covers can participate in limited energy conversion and transmission roles — e.g. electrical → mechanical via a motor or electrical → thermal via a heater — so the player can put the right energy form next to the process that actually needs it instead of building a giant new factory around every conversion.

The intended visual result is **mechanical/thermal → hybrid mechanical/electrical → predominantly electrical with local conversions**, while the underlying physical processes remain recognizably connected. A late-game factory should not merely be an early-game factory with faster machines and different textures.

### Visual progression is part of the design goal

Many technology mods organize around one dominant aesthetic or power system — steampunk, industrial-revolution, or sci-fi — and consequently preserve the same basic machine forms throughout the game. *Feedback* should instead make technological advancement visible through changing infrastructure, process conditions, and energy transport. New understanding should change what the player physically builds.

The intended progression is therefore not “wooden machine → copper machine → steel machine → titanium machine,” with the same block doing the same job throughout. It is “primitive process → scaled process → instrumented process → controlled process → exotic process,” with different machine architectures and infrastructure becoming appropriate along the way. Materials still matter for construction and limits, but they should not be the only thing that visually communicates progression.

### Major process families

The machine roster should grow out of physical process families rather than arbitrary recipe categories. Useful families already identified include:

- mechanical size reduction: grinding, crushing, milling, cutting;  
- mixing and homogenization: blending, suspension, emulsification, gas incorporation, foaming;  
- separation: screening, gravity separation, magnetic separation, centrifugation, filtration, sedimentation, flotation, distillation, crystallization;  
- phase change: melting, freezing, evaporation, condensation, sublimation, deposition;  
- chemical reaction: combination, decomposition, substitution/displacement, redox, precipitation, neutralization, polymerization;  
- extraction and purification;  
- electrochemical processing: electrolysis, electroplating, electrolytic refining, electrodeposition;  
- thermal treatment: annealing, hardening, tempering, calcination, sintering, roasting, carbonization, thermal decomposition;  
- Minecraft-native transformations involving Blaze, redstone, Ender, Chorus, lightning, Nether-specific phenomena, and other exotic effects, all expressed through measurable internal rules.

The first major machine families should feel like physical inventions that make these processes continuous and reliable, not like a list of one-block recipes.

### Empowerment as a parallel progression

Player empowerment should not be forced to track industrial progression one-to-one. It is useful to think of industrial capability, instrumentation, automation, and personal capability as partially independent axes. The player should gradually become more capable in the world as technology advances, culminating eventually in very high-end personal equipment, but the endpoint should feel like **technology being industrialized onto the player** rather than an abrupt jump from ordinary armor to an inexplicably immortal sci-fi Halo Doom Slayer 9000 Mecha-body. The exact empowerment curve can be designed after the industrial framework is established.

### Worked opening transition

The existing opening transition remains useful as a validation case, but it is an example of the general pattern rather than a commitment to literal numbered tiers.

- **Bare-handed / primitive stage:** the player relies on eyes, timing, touch, and repeated manual experimentation. Wide in-window recipes are feasible and narrow windows are difficult because attention is the bottleneck.  
- **First instrumentation stage:** cheap single-purpose sensor covers externalize something the player was already trying to observe. Narrower windows become manageable, and a limited Controller appears because manually coordinating several sensor signals becomes the next bottleneck.

The same rhythm should repeat at larger scales: the player first notices a problem manually, then gains a sensor or process control mechanism, then gains a machine that can act on that information, then discovers the next phenomenon that is still beyond their ability to control.

---

## 10\. Open Threads (Not Yet Decided)

- Whether Mechanical energy splits into reciprocating vs. rotational sub-currencies (GT-style KU/RU). Shelved as a depth dial to revisit later.  
- Mapping the physical-sorting toolkit (Section 5\) onto the actual roster of vanilla \+ modded materials — to be designed collaboratively.  
- Exact machine roster and physical component/upgrading paths for each process family.  
- How far vanilla furnace/other vanilla processing mechanics need to be rewritten to share the same underlying physical state model.  
- Exact mechanism and progression of Electrical → Mechanical / Electrical → Thermal (and potentially other) local conversions through covers.  
- Exact visual architecture of the mechanical/thermal, hybrid, and predominantly electrical factory eras.  
- Where specific Minecraft-exotic phenomena first appear, what is measurable about them, and how their natural forms evolve into engineered/synthetic forms.  
- The full era/tier arc beyond the opening example — how many broad eras there are, where major “detect before you can act” bridges land, and which discoveries pay off later.  
- Personal empowerment progression, to be designed after the industrial framework is established.

---

## 11\. Reference Notes

- **Mindset framework:** the user's own document, *A Speculative Physics and Biochemistry Compendium of Minecraft (Vanilla and Modded)* — used as a methodological reference (minimal-assumption, in-world-grounded reasoning) and as a direct source of reusable material (the Liquid Teleportant processing chain; the "flagged exception" treatment of Redstone). Not binding lore.  
- **Comparison mod researched:** GregTech 6, via the FTB Wiki's "Getting Started (GregTech 6)" page.  
- **Comparison mod referenced:** Steve's Factory Manager and its successor Super Factory Manager, for the control-layer philosophy.
