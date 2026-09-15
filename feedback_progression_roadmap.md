# Feedback — Progression & Content Roadmap

> **Status: DRAFT / DISCUSSION SPEC — working document, not one of the three canonical documents.**
>
> Replaces `feedback_post_tpu_spec.md` (deleted; recoverable from git history). Its philosophy-level
> content (the world is one science, discovery precedes utility, old tech keeps its niche, materials
> have histories, geology should matter) was merged into `feedback_philosophy.md` §14. Everything
> here is the rest: candidate roster/progression content that feeds a future document 3, plus the
> energy-progression and geology detail that feeds document 2.
>
> **Nothing in this document is Established** in the sense of `feedback_philosophy.md` §19. Treat
> every slice, material, machine, and number below as a **Proposal** at best — see §0.1 for the
> category definitions. Do not implement any of it without asking the user first.

## 0. How to use this document

### 0.1 Categories

- **Established** — explicitly confirmed by the user. Nothing in this file starts here.
- **Strong direction** — consistent with confirmed design (`feedback_philosophy.md`), still adjustable.
- **Proposal** — creative content suggested by this document. Most of what follows is this.
- **Unknown** — not yet decided; see §7.

Only implement Established decisions and explicitly approved proposals. When a proposal is approved,
move the relevant content out of this document and into document 2 or document 3 (once it exists),
and mark it Established there — don't leave it dangling here.

**Naming note:** the "Slice N" numbering below (18 candidate content eras) is unrelated to
`feedback_slice_01.md`'s "slice" — that document is a single **built, playable vertical increment**
(copper then steel, confirmed in-game per `TODO.md`), while a "slice" here is a **candidate content
package on the timeline**, most of it unbuilt. The word collision is inherited from this document's
own framing (`CLAUDE.md`'s table already calls these "18 speculative content slices") and isn't worth
a rename, but don't read "Slice 1 — Primitive Industry" below as a synonym for `feedback_slice_01.md`
— the built slice's roster (copper, steel, crucible, thermometer, bellows) is a proper subset of this
document's Slice 1, not the same object.

### 0.2 Ask before implementing

Before implementing any item, recipe, machine, progression gate, world-generation rule, or
energy-system detail described here, ask the user rather than silently picking the reasonable-looking
answer. This applies especially to: exact recipes and numbers, process temperatures and rates, machine
power requirements, energy conversion efficiencies, ore vein sizes and world-gen algorithms, material
properties, whether a Minecraft-native material has a given fictional property, progression gates,
whether a proposed machine/item should exist at all, how fictional phenomena interact with ordinary
physics, and terminology.

This is `CLAUDE.md`'s "ask about prior art first" rule applied to content instead of implementation
technique — the same reasoning applies: asking costs one question, guessing costs a rewrite.

### 0.3 Also apply, without repeating here

Everything in `CLAUDE.md`'s **Hard design rules** applies to every slice below without restatement —
no identity checks, machines define operations not recipes, discrete options not continuous knobs, a
problem should have more than one correct approach, upgrades are physical. A slice proposal that needs
`if (item == SOME_ORE && machine == BLAST_FURNACE) speed *= 2;` is wrong regardless of what section it
came from; let the physical systems produce the specialization the way §15 already does for the three
vanilla vessels.

---

## 1. Progression model

**Category: Strong direction.** Not itself new — it's §14's already-Established "capability bands,
not tiers" and "detect before you can act" claims, restated as a per-phenomenon sequence rather than
a single global list. Nothing below asks the user to accept a new principle, only to apply an existing
one uniformly.

Overlapping technological horizons, not rigid mutually exclusive tiers — this is the same claim
`feedback_philosophy.md` §14 makes about capability bands, applied per-phenomenon rather than
globally:

- A material can appear early while its useful applications appear much later.
- A phenomenon can be observed before it can be measured, measured before it can be controlled,
  controlled before it can be industrialized.
- A fictional phenomenon can accelerate an otherwise-ordinary technology, and ordinary science can
  make a fictional phenomenon exploitable.

A useful per-phenomenon sequence, complementary to §14's global order-of-understanding list:

```
unknown phenomenon
→ observation
→ measurement
→ repeatable experiment
→ controlled manipulation
→ material processing
→ reliable application
→ industrial application
```

---

## 2. Candidate progression / slice plan

**All Proposal-level.** A coding agent should not implement any of this simply because it appears
below — see §0.2.

### Slice 1 — Primitive Industry

- **Core materials:** copper, iron, coal, charcoal, clay, limestone, quartz, gold, redstone, nether
  quartz, amethyst, blaze rod, obsidian.
- **Manufactured materials:** copper ingot/plate/sheet/foil, iron ingot, cast iron, steel, glass,
  ceramic, firebrick, brass, bronze.
- **Machines/tooling:** forge, furnace, crucible, anvil, hammer, basic shaft, flywheel, clutch,
  bellows, casting molds.
- **Processes:** smelting, casting, hammering, hot working, steelmaking, glassmaking, ceramic firing.
- **Minecraft-native observations (not yet useful):** redstone behaves strangely; amethyst has unusual
  resonant behavior; blaze material produces extreme heat; nether quartz appears unusually pure /
  thermally useful; ender pearls are anomalous but poorly understood.
- **Milestone:** steel becomes the first major machine-construction material.

**Grounding note on "amethyst resonance":** this isn't decorative pseudoscience. Amethyst is
structurally α-quartz (SiO₂ with trace Fe³⁺ substitution for color), and α-quartz is a real
piezoelectric crystal — the same physical fact that makes quartz crystal oscillators, phonograph
cartridges, and push-button piezo igniters work. Treating amethyst as an anomalous-but-real resonant
material is "scientific, not realistic" (`feedback_philosophy.md` §2) rather than crystal-healing
flavor text, and it's the same real-physics anchor `speculative_physics_inspo_doc.md` already uses for
piezoelectric ignition and enchanting (§1.4, §5.2 there). What's still fictional and Proposal-level is
*how far* the resonance goes past real quartz's Curie-point/voltage limits — that's the anomaly worth
characterizing per §14's "detect before you can act," not the underlying mechanism.

### Slice 2 — Heat Engineering

- **Materials:** firebrick, high-purity clay/kaolinite-like material, graphite, cast iron, brass,
  bronze, lead, zinc, refractory ceramic, high-temperature glass/tubing, copper tubing.
- **Minecraft-native:** blaze rods as a high-temperature experimental fuel/source; nether quartz enters
  refractory/optical/high-purity-silica experiments; amethyst becomes a resonance-experiment material.
- **Machines:** improved furnace, kiln, annealing furnace, blast furnace, smoker, heat exchanger,
  thermometer, pressure vessel, boiler.
- **Processes:** tempering, annealing, controlled cooling, brass/bronze alloying, glass tubing, ceramic
  firing/glazing.
- **Milestone:** the thermal environment itself becomes an engineering target.

### Slice 3 — Machine Shop

- **Materials:** steel, cast iron, brass, bronze, lead, copper, tin.
- **Products:** gears, shafts, bearings, springs, valves, pistons, flywheels, pulleys, threaded rod,
  copper rod/wire/fine wire, steel cable.
- **Machines:** lathe, drill press, mechanical press, rolling mill, wire drawer, grinding wheel, saw,
  gear cutter.
- **Processes:** turning, drilling, wire drawing, rolling, grinding, precision boring.
- **Minecraft-native:** amethyst as a primitive resonant material — tuning fork, resonant rod, crude
  vibration sensor, limited immediate utility.
- **Milestone:** fine copper wire becomes possible.

### Slice 4 — Electricity Exists

Electricity becomes observable and measurable before it becomes a practical factory-wide energy
system.

- **Materials:** copper, gold, zinc, lead, tin, glass, ceramic, redstone, amethyst.
- **Phenomena:** static electricity, conductivity, potential difference, electromagnetic behavior,
  redstone anomalies, amethyst resonance.
- **Machines/items:** electroscope, electrostatic generator, Leyden-jar-like capacitor, galvanometer,
  electromagnet apparatus, electroplating bath, primitive galvanic cell.
- **Key principle:** the player can run electrical experiments without a convenient grid yet.
- **Milestone:** a galvanic cell provides continuous electrical potential.

### Slice 5 — Electrochemistry and Industrial Refining

Chemistry has existed since the beginning; this slice makes it deliberate, measurable, and useful at
scale.

- **Materials:** copper, zinc, lead, tin, sulfur, salt, limestone, charcoal, quartz, redstone.
- **Fluids:** brine, acidic/alkaline solutions, electrolytes.
- **Machines:** electrolytic cell, chemical bath, distillation apparatus, reaction vessel, filter
  press, condenser.
- **Processes:** electrolysis, electroplating, acid leaching, precipitation, distillation, metal
  purification.
- **Outputs:** high-purity copper/zinc/lead, sulfur compounds.
- **Minecraft-native:** redstone, glowstone, blaze powder, nether quartz gain deliberately investigated
  chemical/electrical properties.
- **Milestone:** pure, controlled feedstock becomes available for advanced metallurgy and electronics.

### Slice 6 — Steam Industry

Steam becomes the first serious industrial prime mover.

- **Materials:** steel, copper, brass, bronze, graphite, refractory ceramics.
- **Machines:** boiler, steam engine, condenser, pump, pressure relief valve, centrifugal overspeed
  trip, steam valve, flywheel.
- **Power chain:** `fuel → boiler → steam → engine → shaft`.
- **Mechanical specialization:** steam is a large, robust, relatively slow power source; a mechanical
  network can drive crushers, rollers, pumps, hammers, generators simultaneously subject to load.
- **Minecraft-native:** blaze material as an unusually hot thermal source, ahead of its deeper uses.
- **Milestone:** industrial-scale mechanical power becomes practical.

**Why not a literal governor:** a real flyball governor continuously throttles the steam valve as a
function of speed — exactly the proportional "dial" `CLAUDE.md`'s hard design rules ban (§13: "the
controller is a switch, never a dial"). The historical part worth keeping is the failure character —
a runaway engine needs a physical, speed-sensed cutoff — so it's rebuilt as a **centrifugal overspeed
trip**: flyweights that snap a single bang-bang linkage (shut the valve / release the clutch) past a
fixed RPM, the same shape as the bimetallic strip thermostat (`feedback_philosophy.md` §13) one level
up in energy type. Fine proportional speed regulation, if wanted at all, stays a player-built deadband
of sensor + comparator + actuator (§13), never a machine stat.

### Slice 7 — Petrochemistry

Petroleum as a major system, not decorative fuel.

- **Geology:** hydrocarbons in large sedimentary basins/reservoirs; possible associated resources
  (crude oil, natural gas, sulfur, salt, limestone, shale); surface seeps through deep reservoirs.
- **Products:** crude oil, naphtha, kerosene, diesel, heavy oil, lubricants, asphalt, solvents, polymer
  feedstocks.
- **Machines:** refinery column, fractionator, cracker, chemical reactor, condenser, gas compressor,
  storage tanks.
- **Polymers:** polyethylene, polypropylene, synthetic rubber/fibers, electrical insulation polymers.
- **Power generation:** `diesel → combustion → shaft`, `natural gas → turbine → shaft` — distinct
  engineering niches, not just bigger steam engines.
- **Milestone:** dense liquid/gaseous fuels and industrial polymers become available.

### Slice 8 — Basic Electronics

Target point for the first genuinely recognizable electronic circuit.

- **Prerequisites:** fine copper wire, insulation, glass, ceramic substrates, refined metals,
  batteries, electromagnets, precision machine tools, polymer insulation, basic electrical
  measurement.
- **Components:** wire, insulated wire, electrode, resistor, capacitor, relay, diode-like component,
  transistor-like component, circuit substrate, solder alloy.
- **Machine:** circuit assembly station, plus wire bonder, soldering station, component tester, signal
  generator, oscilloscope-like instrument.
- **Major item — Basic Electronic Circuit:** ceramic/polymer substrate + copper traces + passive
  components + switching component + soldered connections.
- **Minecraft-native:** redstone evolves from strange material to deliberately exploitable electrical
  material; amethyst for precise oscillation/resonance; glowstone in optical/electrical experiments.
- **Milestone:** controlled electronics becomes manufacturable.

### Slice 9 — Industrial Chemical Engineering

- **Materials:** sulfur, sodium compounds, chlorine, hydrogen, oxygen, nitrogen, silicon-bearing
  feedstock, carbon, phosphates, nitrates.
- **Machines:** large chemical reactor, distillation tower, absorption tower, electrolyzer, compressor,
  gas separator, heat exchanger, pressure vessel, industrial pumps.
- **Products:** sulfuric/hydrochloric/nitric acid, caustic soda, hydrogen, oxygen, chlorine, ammonia,
  fertilizers, catalysts, industrial solvents.
- **Minecraft-native:** blaze, glowstone, redstone, nether quartz as unusual materials, catalysts,
  reagents, or energy-related substances.
- **Milestone:** chemistry becomes a factory-scale material-processing discipline.

### Slice 10 — Advanced Materials

- **Materials:** aluminum, nickel, chromium, silicon, graphite, titanium, tungsten, cobalt, manganese,
  rare-earth materials.
- **Products:** aluminum alloys, stainless steel, nichrome, high-speed steel, tungsten carbide
  (cutting-tool inserts), titanium alloys, graphite electrodes, silicon wafers.
- **Machines:** vacuum furnace, arc furnace, induction furnace, crystal-growth system,
  powder-metallurgy press, sintering furnace, vacuum pump.
- **Processes:** vacuum treatment, high-purity refining, crystal growth, advanced alloying, powder
  metallurgy, sintering, surface treatment.
- **Minecraft-native:** nether quartz as silica/silicon feedstock; amethyst and redstone gain
  specialized material-science applications.
- **Milestone:** high-purity materials and engineered material properties become practical.

### Slice 11 — Radiation Is Discovered

Uranium and related materials appear *before* nuclear power is useful.

- **Materials:** uranium ore/uraninite, thorium-bearing ore, lead, boron-bearing material,
  radiation-resistant glass/ceramics.
- **Instruments:** radiation detector, Geiger-counter-like instrument, dosimeter, scintillator/
  detection crystal, neutron detector.
- **Products:** refined uranium compounds, sealed radioactive source, radiation shielding.
- **Core principle:** the player discovers some materials are intrinsically unstable/radioactive —
  another instance of measurable-before-useful.
- **Minecraft-native (speculative):** whether Ender-related materials respond unusually to
  high-energy/radioactive conditions. Not locked.
- **Milestone:** radiation becomes a measurable physical phenomenon.

### Slice 12 — Nuclear Fission

- **Materials:** uranium, graphite/moderator material, heavy-water-like coolant/moderator (if
  desired), boron, zirconium-like cladding, steel, high-temperature ceramics, lead.
- **Machines:** fuel fabrication press, nuclear reactor, control rods, reactor vessel, coolant loop,
  steam generator, turbine, radiation shielding.
- **Power chain:** `fission → heat → steam/working fluid → turbine → mechanical power → generator →
  electricity`. The reactor produces heat, never directly electricity.
- **Design principle:** nuclear power is another energy-conversion chain, so it participates in Work
  conservation (`feedback_philosophy.md` §10).
- **Control rods stay a discrete-options control, not a continuous depth slider** — a small named set
  of insertion positions (e.g. withdrawn / working / SCRAM), each a physical stop the player or a
  bang-bang actuator selects, per the hard design rule against continuous knobs and §13's "switch,
  never a dial." A real reactor's rod position is continuous; Feedback's is not, the same departure
  slice 6's steam governor already makes.
- **Milestone:** extremely high energy-density thermal power becomes practical.

### Slice 13 — Nuclear / Ender Coupling

The first deliberately high-concept fusion of "ordinary" and Minecraft-native physics.

- **Hypothesis to explore:** under sufficiently energetic conditions, Ender-related materials may
  exhibit behaviors ordinary material physics does not predict — anomalous particle behavior, spatial
  distortion, unusual inertia/radiation response, matter displacement, resonance with high-energy
  systems.
- **Candidate materials:** ender pearl derivatives, end stone derivatives, chorus-derived materials,
  exotic crystals, ender-reactive ceramics.
- **Candidate equipment:** ender-material analyzer, high-energy resonance chamber, spatial detector,
  anomaly containment vessel.
- **IMPORTANT — Unknown, not Proposal.** This is intentionally speculative fiction. Ask the user to
  decide the actual mechanics, properties, and terminology before implementing anything here.
- **Milestone:** the player discovers some Minecraft-native phenomena couple to high-energy physical
  systems.

### Slice 14 — Sculk / Experience Engineering

Sculk as a scientific subject, not a final-tier magic resource.

**Partly superseded by `feedback_mechanics.md` §1, which is further along than "Unknown."** That
section (speculative, not built, but a real working model — see `TODO.md` §1) already settles: XP is
a physical byproduct with a PneumaticCraft-style two-dimensional quantity(`XPu`)/capacity relationship
(§1.2), sculk is the **capture/store/transmit conduit, not the source** (§1.3 — deliberately
separable from "sculk resonance," i.e. vibration sensing, which stays its own domain), and XP is
explicitly **not a fifth power currency** — no engine converts `XPu` to `Work`; it's only a
conditioning input (repair, catalysis, material conditioning) on top of one of the four real energy
domains (§1.4-§1.6). Anything in this slice should build on that framework rather than re-opening it.
What mechanics.md §1 does *not* cover, and this slice still owns: the vibration-sensing/signal-
propagation side of Sculk (a genuinely separate phenomenon per §1.3's own note), Echo Shards, and
memory-like behavior.

- **Materials:** sculk, echo shards, amethyst, redstone, quartz, experience-derived materials if
  desired.
- **Phenomena to investigate:** vibration sensing, signal propagation, energy absorption, resonance,
  memory-like behavior. (XP quantity/level/capacity itself is `feedback_mechanics.md` §1's job, not
  this slice's — see above.)
- **Candidate machines:** sculk analyzer, resonance transducer, XP reservoir (the
  `feedback_mechanics.md` §1.2 reservoir, not a new concept), sculk sensor package, echo memory device.
- **Candidate outputs:** vibration sensor, resonant transducer, experience-storage component,
  echo-memory component.
- **IMPORTANT — Unknown.** Sculk's vibration/signal/memory behavior and Echo Shards' physical
  interpretation are not finalized. Ask before implementation. (XP's own model is drafted in
  `feedback_mechanics.md` §1, still with its own `[OPEN]` items there — don't re-derive it here.)
- **Milestone:** sculk becomes an engineered information/energy material.

### Slice 15 — Advanced Electronics

- **Materials:** high-purity silicon, germanium/other semiconductor materials if desired, copper,
  gold, aluminum, ceramics, polymers, redstone, amethyst, rare-earth materials.
- **Processes:** semiconductor purification, doping, oxidation, lithography-like patterning,
  deposition, etching, wafer processing.
- **Products:** diode, transistor, integrated circuit, memory chip, sensor chip, power transistor,
  oscillator, microcontroller.
- **Machines:** semiconductor fabrication station, doping apparatus, lithography apparatus, wafer
  processing tools, advanced electronic tester.
- **Controller progression — superseded by the built spec, not a competing proposal.** This slice
  used to name its own four stages (`mechanical/punch card → relay logic → electronic logic →
  programmable controller`), but `feedback_controller_spec.md` §4 already settled and partly built
  this: **Punch Card (Su-driven, hard-rewrite) → Circuit Board (electrical, hard-rewrite) → Floppy
  Disk (electrical, soft-rewrite) → USB/ender-networked (electrical, soft-rewrite, program lives on
  the controller itself)**, with Punch Card built (`control/controller/`, `control/program/`,
  `control/programmer/`) and the rest still just the table. Slice 15's electronics roster (diode,
  transistor, integrated circuit) is the material precondition for the Circuit Board/Floppy Disk
  media, not a separate tier scheme — defer to `feedback_controller_spec.md` for names and stages.
  Same principle either way, and it is Established there, not just Strong direction: capability never
  tiers (a punch card can express anything a late controller can, per `feedback_philosophy.md` §13),
  only iteration cost falls and how much the controller can hold in mind at once grows, and older
  media keep their niche (mechanical control has no electricity dependency, so it survives an outage
  that takes an electronic controller down with it).
- **Milestone:** compact programmable electronics become manufacturable.

### Slice 16 — Ender Engineering

The Ender phenomenon becomes industrial technology only after long observation and instrumentation.

- **Materials:** ender pearls, end stone, chorus-derived materials, resonant crystals,
  ender-infused ceramics, exotic conductors.
- **Machines:** ender analyzer, spatial stabilizer, ender relay, spatial transmitter/receiver,
  dimensional anchor, ender pump/transport apparatus.
- **Application progression:** information transmission → small-item transport → fluid transport →
  larger logistics. Normal belts/pipes stay useful for cheap, high-throughput transport (§1.3); spatial
  transport needs a real niche — distance, precision, immediacy — not blanket superiority. This is the
  same shape as the existing spatial energy type in `feedback_philosophy.md` §10.
- **Milestone:** distance itself becomes an engineering variable.

### Slice 17 — Exotic Materials / Netherite Engineering

- **Materials:** ancient debris, netherite components, advanced alloys, blaze-derived materials,
  redstone/amethyst/ender composites.
- **Candidate interpretation:** netherite is not "better steel" — an exotic engineered material with
  unusual thermal, mechanical, or energy properties. Candidate chain (Unknown, confirm before
  implementing): `ancient debris → chemical extraction → refractory component → gold/alloy processing
  → controlled pressure/thermal treatment → netherite`.
- **Milestone:** minecraft-native materials become deliberately engineered rather than merely
  harvested.

### Slice 18 — Fusion / Ultimate Energy Systems

Only after a mature industrial, electrical, thermal, and materials base exists.

- **Materials:** tungsten, titanium, niobium-like superconducting material, advanced ceramics,
  specialized conductors, superconducting materials, exotic ender materials.
- **Machines:** vacuum vessel, magnetic confinement system, superconducting magnets, plasma injector,
  fusion reactor, fusion control system, advanced heat exchanger.
- **Power chain:** `fusion → thermal energy → working fluid → turbine → mechanical power → generator →
  electricity`.
- **Ender interaction (Unknown):** Ender materials providing anomalous confinement/stabilization
  conventional materials can't. Proposal only — confirm the fictional physics before implementing.
- **Milestone:** the player engineers an extreme energy-density system combining ordinary and
  minecraft-native physics.

---

## 3. Energy / power progression

**Category split:** the *principle* that sources keep separate niches instead of forming a strict
replacement ladder is Established (`feedback_philosophy.md` §10, §14 "old technology keeps its
niche"). The *specific ordering* below — which source shows up before which — is Proposal, same as
everything else in §2.

Not a straight "better generator" ladder — different sources keep different engineering niches
(`feedback_philosophy.md` §1.3/§14). Candidate progression, overlapping rather than a strict
replacement chain:

```
manual mechanical power
    ↓
water / wind / primitive mechanical sources
    ↓
steam
    ↓
combustion engines
    ↓
mechanical transmission
    ↓
generators
    ↓
electricity
    ↓
gas turbines / advanced engines
    ↓
nuclear heat
    ↓
high-efficiency electrical generation
    ↓
exotic nuclear / ender coupling
    ↓
fusion
```

Work conservation across this whole chain is already Established — see `feedback_philosophy.md` §10.
No new principle here, only the candidate slice order above.

### 3.1 Fuel

Fuel should eventually represent finite chemical energy rather than "burns for N ticks at T
temperature." The current fallback (`feedback_philosophy.md` §15, "The fallback") is an accepted,
explicitly non-physical compatibility shim, not a target state. Do not redesign fuel accounting without
asking the user first about the intended energy quantities and conservation rules — this is exactly
the kind of Work/fuel accounting question §7 below flags as unresolved.

---

## 4. Geology and ore generation

Direction: large, irregular, geologically meaningful deposits instead of isolated ore-block scatter
(`feedback_philosophy.md` §14, "Geology should matter", **[OPEN]**).

### 4.1 Candidate ore families (Proposal — mineral names, associations, and sizes all unconfirmed)

| Metal | Candidate ore mineral(s) |
| --- | --- |
| Copper | Chalcopyrite, malachite/oxide copper minerals |
| Zinc | Sphalerite |
| Tin | Cassiterite |
| Lead | Galena |
| Nickel | Pentlandite |
| Chromium | Chromite |
| Aluminum | Bauxite |
| Iron | Hematite, magnetite |
| Sulfur | Pyrite and sulfur-bearing deposits |
| Silica | Quartz-rich deposits |
| Uranium | Uraninite |
| Thorium/rare earths | Monazite/thorium-bearing analogues, if desired |
| Hydrocarbons | Crude oil/natural gas reservoirs, shale, tar/bitumen |

### 4.2 Deposit design goal

A deposit should be more than a random cluster of ore blocks:

```
surface indicators
→ low-grade peripheral material
→ primary ore zone
→ richer central zone
→ deeper associated minerals
```

Different deposit types may have different geometries and associated resources.

### 4.3 Prospecting progression (Proposal)

```
find visible indicator
→ crude prospecting
→ estimate deposit existence
→ estimate size
→ estimate grade
→ assay composition
→ model deposit
```

---

## 5. Concrete cross-domain milestones

Candidate "big moments," Proposal-level pacing markers rather than locked content:

first steel · first controlled high-temperature process · first fine copper wire · first electrical
experiment · first galvanic cell · first steam engine · first large ore-processing plant · first
refinery · first basic electronic circuit · first radiation measurement · first nuclear reactor · first
successful ender-material experiment · first integrated exotic system (an ordinary physical system and
a minecraft-native system deliberately combined into one engineered process).

---

## 6. Cross-domain interaction testing

Every new subsystem here should be tested against existing systems, not only in isolation:
thermal↔mechanical, thermal↔electrical, chemical↔thermal, chemical↔electrical, electrical↔mechanical,
nuclear↔thermal, nuclear↔ender, sculk↔electronics, redstone↔electrical, spatial transport↔control/data.

---

## 7. Things intentionally left unresolved

Ask the user rather than deciding silently:

1. The exact Work/fuel accounting model (§3.1).
2. Exact energy units and conversion efficiencies for future energy systems (§3).
3. Exact material properties for alloys and advanced materials (slices 2, 10, 17).
4. Exact geology/world-generation rules, mineral roster, and deposit sizes (§4).
5. Exact petroleum formation and reservoir generation (slice 7).
6. Exact radiation model (slice 11).
7. Exact nuclear fuel-cycle complexity (slice 12).
8. Exact relationship between nuclear phenomena and Ender phenomena (slice 13).
9. Exact physical interpretation of Redstone, Sculk's vibration/signal/memory behavior, and Amethyst's
   full useful-property ceiling (slices 4, 8, 14, 15) — XP itself has a drafted model already
   (`feedback_mechanics.md` §1), narrower open items tracked there, not here.
10. Exact Netherite metallurgy (slice 17).
11. Exact fusion implementation (slice 18).
12. Whether any proposed slice 9+ system should exist in the final mod at all.

(Mechanical-network behavior at splits/joins with momentum/inertia, and TPu's rate equation/decay/
monotonicity, are tracked in `feedback_mechanics.md` and `tpu_spec_doc.md` respectively — not
duplicated here.)
