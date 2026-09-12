# Feedback — Vertical Slice 1

> **A working document, not one of the three.** This is the first playable slice, built by the method in `feedback_philosophy.md` §20. It is deliberately concrete: real materials, real machines, real numbers.
>
> It feeds the other two documents rather than replacing them. The rules it discovers become **document 2 (mechanics)**; the objects it names become **document 3 (content)**. Nothing here overrides the philosophy — where this document and the philosophy disagree, the philosophy is right and this slice is wrong.
>
> **Every number here is a placeholder.** They exist so the slice can be reasoned about and played, not because they are balanced. Philosophy §19 still holds: exact values are not decided.

---

## Scope

Two beats, one gimmick each.

> **Beat 1 — repetition.** *A machine will do this forever, and it does not know when to stop.*
>
> **Beat 2 — heat.** *You cannot control what you cannot see.*

Neither beat contains a controller. The player is the controller, until a bent strip of metal takes over the one job simple enough to hand off.

**One energy network, two process variables.** Mechanical power is the only distributed energy system in the slice — shafts, one generator, one consumer. Temperature is a *process variable*, not a network: heat comes from burning something in a firebox directly under the vessel. This satisfies §20's "one energy system" honestly while still allowing beat 2, and it defers the entire thermal-transport layer.

**Explicitly out of scope:** electricity, chemistry, spatial, fluids as a system, separation, covers as a general mechanism, the node-based controller from §13, multiblocks, and any second energy network.

**Budget:** 12 items, 7 machines, 2 process variables. Counted at the end.

---

## Beat 1 — Repetition

### The problem

The player wants copper plates. Plates are a real intermediate — they go into the machines and instruments that the rest of the slice needs — so the demand is structural rather than a fetch quest.

The process specification, per §5:

```
COPPER PLATE

Input          1 × Copper Ingot
Process        Mechanical deformation
Required work  30 Fu
Minimum force  1 St
Output         1 × Copper Plate
```

Note what the recipe does not mention: any machine at all.

### Doing it by hand

A hammer applies roughly 2 Fu per swing at 3 St. Fifteen swings, one plate. It works, and it is exactly as tedious as fifteen swings sounds.

This is §7's *possible* column, introduced in the first ten minutes and never taken away. A player can hand-hammer plates for the entire game if they want to. Nobody will.

**Copper is chosen because it is forgiving and abundant** (§6, §7). It is nearly impossible to actually destroy, and the player has stacks of it. The slice needs a material the player can afford to waste while learning that waste is possible.

### The machine that does not stop

The **Mechanical Hammer** strikes whatever is inside. It does not take rotation — it takes **strokes**, because a hammer goes up and down and a shaft goes round and round.

Converting one into the other is a separate component the player places:

```
CRANK LINKAGE
Converts rotation to reciprocation
Throw        short / long  (set by which crank is installed)
Stroke rate  from RPM
Force        from torque ÷ throw
```

```
MECHANICAL HAMMER
Applies      whatever the linkage delivers
Load         80 Su
```

**The hammer has no strength of its own.** A short throw gives few hard blows, a long throw gives many gentle ones, and the player chooses which by choosing a crank (§17). This is the slice's first real gearing decision, and it arrives about ninety seconds after the first machine.

Feed it a copper ingot and drive it. Copper needs only 1 St, so any linkage works — which is exactly why copper is the teaching material. The player can install the wrong crank and still succeed, and only find out later that the choice mattered.

After about 8 ticks, there is a plate.

At tick 9 it is still hammering.

```
ingot → plate → thin plate → foil → scrap
```

**Every one of those is a real item**, and the foil is genuinely useful — it goes into the thermometer in beat 2. So the first overrun the player ever experiences is a *sidegrade* (§6), which is the correct lesson to teach first: the machine is not malfunctioning, and the extra state is not damage. It is a place further along the same physical axis. The player is the one who decided which point on that axis they wanted.

Only past foil does it become scrap. The punishment exists, but it is not the introduction.

### Reading the workpiece

A part-worked ingot **looks** part-worked. It is visibly flattening, the player can see roughly how far along it is, and they can see when it has gone too far. Mechanical deformation announces itself, and the player needs no instrument to avoid ruining an ingot (§8).

So beat 1 deliberately ships **no measuring tool at all.** The player's eyes are sufficient for the problem beat 1 poses, and handing them an instrument here would teach the wrong lesson about what instruments are for.

### The timer

The obvious fix. Power the hammer, wait, cut power.

And it works — **automation arrives before precision does**, which is worth the player discovering firsthand. But the hammer's rate depends on the rotation it is getting, and rotation from a water wheel is not perfectly steady. So the timer is a bet (§8), and it pays out something like *mostly plates, sometimes foil, occasionally scrap.*

The player's first instinct — set the timer short and accept a few under-worked ingots — is correct, and it is their first encounter with trading yield for safety.

### Calipers, and what they are actually for

Late in beat 1 — **after** the timer, not before — the player wants something their eyes cannot give them: not to avoid scrap, but to hit **foil deliberately and every time.**

```
CALIPERS
Held tool. Measuring a workpiece reports  16 / 20 Fu
```

That is the distinction §8 draws between an instrument that *reveals* and one that *refines*. Calipers refine. The player could always see the ingot flattening; what they could not do was aim. Hitting the narrow sidegrade band on purpose (§6) is the first thing in the game that requires a number rather than a glance.

It matters that this is the **weaker** of the slice's two instruments and arrives first. When the thermometer shows up in beat 2, the player already understands what an instrument is — and then meets a quantity they cannot see at all, which is a much bigger event. Calipers make the thermometer land harder by being the small version of it.

### Mechanical infrastructure

Rotation has to come from somewhere and travel through something.

```
HAND CRANK      12 Su     while the player holds Right Click
WATER WHEEL    256 Su     continuous, needs flowing water
SHAFT / GEARBOX           transmits, with loss
```

Connecting a second machine is not an unlock. It is an arithmetic problem: 256 Su of supply, 80 Su per hammer. Three hammers is fine. Four is not. Nothing says *requires tier 2*; the shaft simply cannot carry it.

This is where `Su` and `RPM` earn their keep (§17), and the player learns that infrastructure is a thing that exists before they learn anything about control.

---

## Beat 2 — Heat

### The problem

Steel. The player has iron in quantity, and a reason to want something better.

```
STEEL

Input          1 × Iron Ingot, 1 × Charcoal
Process        Carburizing
Temperature    1420–1480 Tu
Hold           600 t (30 s)
Max heating    25 Tu/t
Output         1 × Steel Ingot

Overshoot past ~1540 Tu → Burnt Iron (spoil)
Undershoot         → unchanged Iron Ingot (nothing happens)
```

**Iron is chosen for the same reason copper was: the player has piles of it.** Per §7, the cost of ignorance is paid in raw material, and that lesson only lands if the player can afford to pay it. Burning thirty iron ingots to make four steel is a formative experience. Burning thirty of something rare is a quit-the-modpack experience.

Note the asymmetry in the failure modes, per §6: undershooting does *nothing*, overshooting *destroys*. The player can fail safely in one direction and expensively in the other, which teaches them which way to err long before they can measure anything.

### Why copper did not teach this

Copper's window was "hot." Steel's window is 60 Tu wide and has a rate limit on the way in. The player's furnace can absolutely reach 1450 Tu — **the heat was never the problem** (§7). They simply have no idea what temperature they are at.

So they do the only thing available: guess, wait, pull it out, look at it. Six stacks in, two ingots out. The recipe was never locked and the player is not being punished. They are paying for information they do not have.

### The Crude Blast Furnace gets them most of the way there

The vanilla blocks are real thermal vessels now (§15), and one of them is genuinely good at this. The Crude Blast Furnace (from vanilla, given the word crude now to make space for a more sophisticated multiblock later down the line) has a high floor, a high ceiling, and enough thermal mass to sit near a temperature on its own.

So the player *can* make steel here, by hand, before owning a single instrument. It takes standing there, watching the glow, and accepting a poor conversion rate — but it works, and it should feel like a real accomplishment rather than a stopgap.

This is the slice being honest about §7. The demanding material was available the whole time. What the player lacked was never heat.

### The thermometer

Copper foil, a sealed tube, and something that expands.

```
THERMOMETER  (crude)
Range        0–2000 Tu
Accuracy     ±25 Tu
Resolution   25 Tu
Update       every 20 t
```

It controls nothing. It is a window (§8).

And it is immediately, obviously insufficient — ±25 Tu against a 60 Tu window means the player can be at 1450 or 1425 or 1475 and cannot tell. **But it is enough to work by hand.** Heat, look, wait, look, adjust, look. Miserable, slow, and it produces steel reliably enough to be worth doing.

That is the progression moment, and no recipe unlocked. The player gained *information*, and information became capability.

### And then they discover they cannot attach it

The thermometer is a cover. The Crude Blast Furnace takes no covers — it is sealed, and everything good about it follows from being closed (§15).

**This is the wall the slice is actually built around.** The player's best thermal vessel and their first instrument are mutually exclusive, and no amount of iron fixes it. They cannot measure the thing that works. They cannot automate the thing they can measure.

The way out is not a better furnace. It is a vessel that was *designed to be measured* — which is the crucible, and which is why it exists.

```
CRUCIBLE
Thermal mass   high, tunable by size
Instrument     accepts covers — it has a thermowell
Batch          larger than the blast furnace
Speed          slower than the blast furnace
```

The crucible is not an upgrade. It is **worse at the thing the blast furnace is good at**, and it is the only one of the two that can ever be part of a loop.

### The actuator

A **Bellows** driven by the mechanical network — the first place the two beats touch. It squeezes, so like the hammer it wants strokes rather than rotation, and it needs a linkage of its own.

```
POWERED BELLOWS
Effect    air per stroke × stroke rate  →  Tu/t into the firebox
Load      40 Su
```

Here the throw choice reads differently than it did on the hammer: a long throw moves more air per stroke. The player has already met this component and now finds it means something else entirely, which is the first time in the slice that one part composes two ways.

It has no target temperature. It has no thermometer. Given power it blows air; given none it stops. That is the entire device.

And that is the **only** actuator in the slice. Nothing removes heat. The bellows is the player's single verb and it points one way.

This is deliberate, and it is the slice's most productive frustration. Overshoot the window and there is nothing to do but wait, on a vessel chosen for stability and therefore equally stubborn about coming back down (§10 — thermal stops slowly whether or not you want it to). The player cannot ask for cooling. They can only stop asking for heat.

### Closing the loop

```
Thermometer → Controller → Bellows
```

There is **no controller in this slice.** There does not need to be one, and putting one here would waste it.

Beat 2 has exactly one condition to watch, and a single condition needs no logic. A **bimetallic strip** — a sensor cover that trips at one fixed temperature — wired straight to the bellows is a thermostat, and it closes the loop with no programmable block anywhere:

```
below its trip point   →  bellows on
above it               →  bellows off
```

The trip point is set by the strip the player crafted. Changing it means crafting a different one.

This is the honest first automation, and it keeps the real controller in reserve for the moment it is genuinely needed — when one condition stops being enough (§13). Until then, and for the whole of the slice before this point, **the player is the controller.** That is the job every later tier is trying to take off their hands.

The temperature now oscillates in a band. Nothing in the system knows that 1450 Tu matters. The furnace does not, the bellows does not, the thermometer does not. **The player built the only thing that knows** (§8).

And then it does not quite work, which is the real lesson. The bellows is strong and the thermometer updates every 20 ticks, so the loop overshoots — it reads 1415, fires the bellows, and by the next reading it is at 1490. The player has good enough information and still cannot hold the condition.

That is §8's second half, delivered as a bug the player has to fix rather than a paragraph they have to read: **knowing the state is not enough; you have to be able to respond to it.**

The fixes available are all physical, per §4: a smaller bellows, a faster thermometer, a wider deadband, or more thermal mass.

### Thermal mass becomes a choice

The last of those is the interesting one, and it is where beat 2 stops being a tutorial.

```
SMALL CRUCIBLE    heats fast, cools fast, hard to hold steady
LARGE CRUCIBLE    heats slow, cools slow, very stable
INSULATION        (upgrade) slows both directions further
```

Neither is better. Steel wants 30 seconds inside a 60 Tu band, so the large insulated crucible wins — its sluggishness is the feature, and it will hold the band almost by itself once it gets there. A process that needs to *move* fast would prefer the small one.

This is §4's rule in its purest available form. The upgrade is not *Crucible Mk II, +50% speed.* It is **more mass and better insulation**, and whether that helps depends entirely on what the player is doing.

### The blast furnace never becomes obsolete

Once the loop is running, the player owns two ways to make steel, and neither one wins (§15).

|       | Crucible + loop | Crude Blast Furnace |
| :---  | :---            |                :--- |
| Speed | slow            | fast                |
| Batch | large           | small               |
| Costs | capital, infrastructure, a control loop | **standing there** |

A minute of AFK, or thirty seconds of full attention. The right answer changes depending on what else the player is doing, and it keeps changing for the rest of the game.

This is the slice's quiet thesis, and it is worth being deliberate about: **the player did not upgrade past the blast furnace. They bought the option to walk away from it.** Automation in Feedback purchases attention, not capability — which is §7's whole *possible / reliable / economical* distinction arriving as a thing the player feels rather than reads.

### The taste of path dependence

Once the player can hold a temperature, one more step is available — and it is the hook into everything after the slice:

```
HARDENED STEEL

Input       1 × Steel Ingot (at ≥ 1400 Tu)
Process     Quench
Requires    Cooling rate ≥ 200 Tu/t
Output      1 × Hardened Steel  — hard, brittle

Cooled slower → ordinary Steel Ingot. No loss, no progress.
```

The *same ingot* at the *same final temperature* is a different item depending on how it got there. Nothing about the end state distinguishes them.

This is §7's history-sensitivity, and it is the first requirement in the slice that cannot be satisfied by getting a number right. It is also real metallurgy, which is the point of §2 — the mod did not invent this rule, it noticed it.

**Deliberately left dangling:** hardened steel is brittle, and brittleness is a problem.

---

## The fantastical thread

Per §2, this cannot be a footnote. In this slice it lives in the most mundane possible place: **the fuel.**

Every heat source the player meets is also a small anomaly, and none of them are explained.

**Charcoal** — mundane, and the slice's honest noise source (§8). Its quality genuinely varies, so the firebox wanders, so timers drift. The irreducible noise floor enters the game as *the fuel is not always the same*, which is both true to life and free to implement.

**Lava** — the player has seen it since day one and never asked. It is 1200 Tu and **it does not cool.** Not slowly. At all. A bucket of it under a crucible is an infinite, perfectly stable heat source that is too cool for steel and too hot to turn off. No explanation is offered. The player just notices that the most stable thermal environment available to them is a rock that should have frozen centuries ago.

Lava carries this thread alone, and that is enough, because the payoff is the slice's exit.

None of it is characterized. There is no lore entry and no analysis machine. There is only a player who now owns a thermometer and has started pointing it at things — which is the correct first step of §2's sequence, **observe → exploit**, and the entire rest of the chain is somebody else's slice.

### The slice ends at the Nether door

Flint and steel takes **steel**. Steel is beat 2. So the Nether is not reachable until the player has done everything above, which gives the slice a diegetic ending rather than an arbitrary one: *you have learned to control heat, and the reward is a door.*

On the other side is a **Blaze Rod** — a solid object, cool to hold, that releases more heat than its mass can account for. It burns hotter than charcoal and, far more disturbingly, it burns *steadier*. It is the first thing the player's newly-built physical intuition flatly cannot accommodate, and they now own exactly the instrument required to confirm that it makes no sense.

That belongs to slice 2. It is named here only so the slice is built to arrive at it.

---

## What slice 2 opens on

Three things arrive together, and each one makes the others necessary. That is why none of them are here.

**Tempering.** Hardened steel is brittle and the fix is a third controlled thermal step — but tempering is not a *hold*, it is a **controlled cool**. The player needs to bring a temperature down deliberately, at a rate, which is the first thing in the game they have no way to ask for.

**The damper.** Which is the way to ask for it. A vent that bleeds heat off on demand, and the slice's one-way bellows finally gets an opposite.

**The controller.** Because a bellows and a damper are two actuators and two conditions — *heat when low, vent when high* — and a bimetallic strip cannot express that. One condition has stopped being enough, which is exactly the moment §13 says a controller should arrive.

Slice 1 earns all three by withholding them. The player ends it able to hold a temperature and unable to steer one, and they should feel precisely which of those they are missing.

---

## The convergence

The two beats meet in one process, which is how the slice ends:

```
STEEL PLATE

Input          1 × Steel Ingot
Process        Hot working
Temperature    900–1100 Tu   ← must hold DURING the work
Required work  60 Fu
Minimum force  15 St
Output         1 × Steel Plate

Below 900 Tu → too cold, work does nothing, hammer wears
```

Cold steel cannot be worked at all — 15 St is beyond the primitive hammer regardless — so the player must keep the workpiece hot *while* hammering it. Temperature and mechanical work, coupled, in one process.

This is the first **coupled** difficulty in the game (§7), and it is the slice's graduation: the player is no longer running two independent systems. They are operating a process.

And it is where the linkage stops being a curiosity. Steel demands 15 St, and the long-throw crank that has been perfectly adequate for copper since beat 1 **cannot deliver it at any speed.** No amount of RPM substitutes for force (§17).

The fix is not a better hammer. It is a **shorter crank** — the player re-gears the machine they already own, and the same hammer that was making copper plates becomes a forging press. That is §14's difficulty curve in one component: the new material did not require new equipment, it required understanding the equipment.

---

## Budget check

**Items (12):** Copper Plate · Copper Foil · Copper Scrap · Steel Ingot · Burnt Iron · Hardened Steel · Steel Plate · Hammer · Calipers · Thermometer · Bimetallic Strip · Shaft
*(Copper Ingot, Iron Ingot, Charcoal and Lava are vanilla. Blaze Rod is slice 2.)*

**Machines (7 new):** Hand Crank · Water Wheel · Crank Linkage (short/long throw) · Mechanical Hammer · Crucible (small/large/insulated) · Firebox · Powered Bellows · Timer
*(No controller and no damper. One actuator, one condition — see §13 and "What slice 2 opens on".)*
*(Furnace, Smoker and Crude Blast Furnace are reworked vanilla blocks, not additions — see §15.)*

**Process variables (2):** `Fu`/`St` mechanical work · `Tu` temperature

Within §20's budget. Nothing in it exists to pad the tech tree.

---

## What the slice teaches

Mapped to the philosophy, because if a beat teaches nothing it should be cut:

| Lesson | Where it lands | Philosophy |
| :--- | :--- | :--- |
| Machines perform operations, not recipes | Hammer has no recipe list | §4 |
| Machines do not know when to stop | Plate → foil → scrap | §1, §6 |
| Overrun is sometimes another process | Foil is useful | §6 |
| Manual production is real but miserable | 15 hammer swings | §7 |
| Timers automate before precision exists | The plate timer | §8 |
| The recipe is not locked; the process is hard | Steel, attempted blind | §7 |
| Ignorance is paid in raw material | Six stacks, two ingots | §7 |
| Measurement is separate from control | Thermometer controls nothing | §8 |
| Knowing is not enough — you must respond | The overshooting loop | §8 |
| Upgrades are physical | Thermal mass, insulation | §4 |
| Better machines improve old work | 20 St hammer trivializes copper | §4, §14 |
| Process history matters, not just state | Quenching | §7 |
| Infrastructure has real limits | 256 Su, 80 Su per hammer | §10 |
| Noise is real and learnable | Charcoal quality | §8 |
| Strange things are useful before understood | Lava | §2 |
| Senses give adjectives; numbers cost instruments | Glow vs. thermometer, flattening vs. calipers | §8 |
| Revealing beats refining | Calipers refine; the thermometer reveals | §8 |
| You are the controller until something replaces you | No logic block in the whole slice | §13 |
| Motion has a shape, and force is geared not bought | Crank throw sets St on hammer and bellows | §10, §17 |
| Specialization is emergent, not declared | Smoker ceiling, blast furnace floor | §15 |
| Automation buys attention, not capability | Crucible vs. blast furnace | §7, §15 |

Twenty lessons, twelve items, seven new machines, one actuator, and no controller. Every one of the philosophy's load-bearing ideas appears at least once, in play, without a single tooltip explaining it.

---

## Open decisions

- ~~Vanilla furnace~~ **Resolved** (§15) — reworked, not removed; beat 2 rebuilt around it.
- ~~Where the Controller comes from~~ **Resolved** (§13) — a tape writer and a tape reader, punched card in the early game. The cheap sensor being a boolean is now concrete too: it is a piece of bent metal that trips at one temperature.
- ~~Number or needle~~ **Resolved** (§8) — free senses report adjectives, instruments report figures, and each tier buys significant figures.
- ~~`Fu` progress display~~ **Resolved** (§8) — visibly flattened for free, `16 / 20 Fu` with calipers.
- ~~Blaze Rod availability~~ **Resolved** — flint and steel takes steel, so the Nether closes the slice and Blaze opens slice 2.

Still open:

- ~~What does a punched tape hold?~~ **Resolved** (§13) — everything a late-game controller can. Capability never tiers; only iteration cost and source count do.
- ~~Does the slice need a controller?~~ **No.** A bimetallic strip wired to the bellows carries beat 2; the tape pair belongs to slice 2.
- ~~Does the damper survive?~~ **No.** It is a second actuator, and a second actuator is what a controller is for. Both move to slice 2.
