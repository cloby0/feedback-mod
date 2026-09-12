# Feedback — Design Conclusions
## Brainstorming Notes / Current Direction

> **Status:** None of this is final. These are the conclusions and design principles reached during brainstorming. Examples are illustrative unless explicitly stated otherwise.

---

## 1. Core Identity

Feedback is a Minecraft technology mod about **building and controlling physical processes**, rather than unlocking recipe-specific machines.

The central gimmick is:

> **Machines do not recognize when a recipe is complete.**

A machine continues performing its physical action while it has the necessary inputs and power. If the desired output has already formed, continued operation can change, degrade, spoil, or otherwise affect it.

The player therefore has to solve two separate problems:

1. **Make the process happen.**
2. **Know when the desired state has been reached and stop or redirect the process.**

This is the foundation of the mod's automation philosophy.
The mod should not feel like a conventional tech tree where each tier simply unlocks stronger machines. Progression should instead represent increasing ability to **observe, manipulate, control, scale, and reproduce physical processes**.

---

## 2. Design Philosophy

### Mechanical depth > playfulness > realism

Real science is inspiration, not the objective.

The mod should use scientific habits and reasoning:

- measurement
- controlled experimentation
- repeatability
- process conditions
- scale-up
- instrumentation
- engineering
- characterization

But it should not attempt to simulate every scientifically meaningful variable.

A variable belongs in the game because it creates an interesting engineering decision, not merely because it exists in real life.

### Scientific, not realistic

Minecraft's rules are treated as actual laws of nature.

If something strange demonstrably happens in Minecraft, it is a real phenomenon in the setting and can potentially be industrialized.

The player does not necessarily need to understand a phenomenon before exploiting it.

The general pattern is:

> **Observe → exploit → measure → understand → engineer**

A bizarre Minecraft phenomenon should not automatically be reserved for late-game "magic technology."

---

## 3. Progression Philosophy

Progression is a history of understanding rather than a rigid sequence of technological ages.

Broad developmental themes include:

1. Observation
2. Continuous processes
3. Scale
4. Multiple energy phenomena
5. Measurement
6. Controllable material properties
7. Separation
8. Controlled chemical transformation
9. Extreme conditions
10. Systematic study of Minecraft-exotic phenomena
11. Engineering/reproducing unusual phenomena

These are **capability bands**, not necessarily literal tiers.

Useful axes of progression include:

- physical manipulation
- throughput
- observation
- control
- materials
- separation
- transformation
- energy
- logistics
- exotic science

They should overlap rather than form a strict ladder.

"Tiers" can still be useful shorthand for broad eras of capability, but the player should not experience them as arbitrary technology locks.

---

## 4. The Most Important Progression Rule

A process has requirements.

An apparatus has capabilities.

The recipe is not necessarily locked just because the player lacks the appropriate apparatus.

Instead, distinguish:

### Possible

The player can physically perform the process somehow.

### Reliable

The player can perform it consistently enough to be useful.

### Economical

The player can perform it at a reasonable rate/resource cost/maintenance burden.

This distinction is extremely important.

A highly demanding process may technically be possible with crude equipment through manual work, guesswork, or enormous amounts of attention.

The player should be allowed to do surprising things early if they are willing to suffer for it.

A good shorthand for this philosophy is:

> **The recipe is not locked. The process is difficult.**

Hard progression gates should represent genuine physical limitations, such as:

- insufficient mechanical strength
- insufficient total work
- insufficient temperature
- insufficient pressure
- insufficient energy transfer
- material limits
- vessel limits
- other genuinely impossible conditions

Precision should generally **not** be a hard gate.

A player can attempt a precise process with a timer or manual intervention. They simply get worse reliability, throughput, or maintenance outcomes.

---

## 5. Manual Production

Manual production should remain theoretically possible for a surprising amount of the mod.

A "100% Feedback, no automation" or "Amish%" style playthrough should be technically viable for much more of the game than expected.

The tradeoff is:

- extremely low throughput
- high attention requirements
- poor repeatability
- difficult scaling
- potentially high waste

Automation should therefore represent a way of making physical processes practical, not merely a key that unlocks recipes.

---

## 6. Recipes Are Process Specifications

JEI is assumed to be essential.

Recipes should describe **process specifications**, not machine-specific unlocks.

A recipe/process entry should communicate things such as:

- input
- output
- required process
- temperature
- pressure
- mechanical requirements
- duration
- flow/concentration/etc. where relevant
- tolerances
- important rates
- possible byproducts
- potentially relevant physical conditions

A recipe should answer:

> **"What must physically happen to this material?"**

rather than:

> **"Which machine do I put this into?"**

A process may therefore have multiple valid implementations.

---

## 7. "Infinite Ways to Skin the Cat"

A major design goal is a Create-like compositional sandbox, pushed further toward process engineering.

Give the player:

- physical constraints
- components
- process specifications
- energy systems
- sensors
- actuators
- control logic

Then let them construct solutions.

There should often be many ways to accomplish the same process.

Some solutions should be objectively bad but still technically valid.

The game should not prescribe a single intended machine chain whenever the underlying physical problem permits alternatives.

---

## 8. Machine Philosophy

Machines define **physical operations**, not magical recipe lists.

Examples of physical operations:

- heating
- cooling
- grinding
- pressing
- mixing
- pumping
- separating
- filtering
- reacting
- phase changing
- cutting

A machine should generally not say:

> "I know how to make Copper Plate."

It should say, in effect:

> "I apply this physical operation to whatever is placed here."

### Upgrades

Upgrades should be physical components whenever possible:

- larger vessel
- better insulation
- flywheel
- better bearings
- finer screen
- stronger vessel
- improved seal
- agitator
- larger heater
- etc.

A better machine should often improve old processes rather than merely unlocking new ones.

For example:

> A better saw should cut copper more efficiently.

It should not simply exist because copper is now a "Tier 3 material."

New capabilities can emerge naturally because improved equipment makes previously impractical processes economical.

---

## 9. Overrun

Machines have conceptual states:

- **Idle**
- **Running**
- **In-window** — the desired result technically exists, but the machine has not been stopped
- **Overrun** — continued operation is now acting on the finished/desired state

Overrun has a severity spectrum:

- Sidegrade
- Degrade
- Spoil
- Hazard

Overrun is not merely wasted energy.

Continued physical action can actively change the product.

Sometimes that change may even be useful.

For example, a grinder might produce:

> coarse → fine → powder

In that case, what looks like "overrun" can actually be another valid process.

This reinforces the principle that machines are performing physical operations rather than executing discrete recipes.

---

## 10. Timing Is a Crude Form of Control

Timers are cheap fallback automation.

A player can estimate that a process takes a certain amount of time and simply shut the machine off after that interval.

However, recipe/process times should not be perfectly deterministic.

Relevant real process conditions can cause learnable variation, such as:

- fuel quality
- machine wear
- ambient conditions
- batch load
- preheating
- thermal mass
- contamination

This variation should be understandable rather than arbitrary random noise.

Timers should be genuinely beatable by better measurement and control.

A timer can make a process economical before the player has sophisticated instrumentation, but precision automation should eventually outperform it.

---

## 11. Measurement and Control Are Separate

This is arguably the central architecture of Feedback.

There are four distinct concepts:

### Process physics

The machine changes state according to inputs and losses.

### Measurement

A sensor observes the state.

### Control

A controller interprets the measurement and decides what should happen.

### Actuation

An actuator changes the physical system.

The player constructs the connection between these pieces.

For example:

> Thermometer → Controller → Heat Pump → Furnace

The furnace itself has no target temperature.

The heat pump has no target temperature.

The thermometer does not control anything.

The controller does not magically "know" the correct furnace temperature.

The player builds a feedback loop.

---

## 12. Accuracy vs. Precision

This distinction should remain explicit.

### Accuracy

A property of the measurement instrument.

How close is the reported measurement to the actual state?

### Precision

A property of the complete measurement/control setup.

Can the system use sufficiently good information and respond quickly/sufficiently enough to maintain the desired state?

Therefore:

> If you cannot sense something, you cannot precisely control it.

But also:

> If you can sense something accurately but cannot react to changes quickly enough, you still cannot precisely control it.

Precision is therefore **emergent**.

It is not a stat on a machine and should not normally be a hard recipe requirement.

---

## 13. Example of Thermal Control

A process might require a material to remain around a particular temperature.

A player could manually:

1. heat the vessel
2. observe temperature
3. add/remove heat
4. wait
5. observe again
6. adjust again

This is possible but tedious.

Later they can construct:

> Thermometer → Controller → Heat Pump

For example, conceptually:

> Below 1450 Tu: turn heat pump on  
> Above 1550 Tu: turn heat pump off

The resulting temperature naturally oscillates within the chosen range.

The machine itself still has no idea what the target is.

The control system creates that behavior.

---

## 14. Thermal Behavior

Rates matter as much as amounts.

Having:

> 10000 Tu for 1 second

is not equivalent to:

> 1000 Tu for 10 seconds

if the process requires a sustained temperature.

Most thermal processes should therefore care about the actual state of the material over time.

Heating rate should be a property of the interaction/process/material rather than simply being a magical "furnace speed" stat.

### Thermal state

Machines/material systems can have thermal memory.

Important concepts include:

- preheat
- thermal mass
- heat loss
- insulation
- cooling rate

A cold apparatus can behave differently from a preheated one.

This creates a real tradeoff:

> Keep equipment hot and waste energy, or let it cool and accept a slower startup.

A larger thermal mass can be slower to heat but easier to stabilize.

A smaller thermal mass can respond quickly but be harder to control.

---

## 15. Machine State Memory

Not all machine state is transient.

Potential persistent/semipersistent state includes:

### Thermal charge

Preheating affects future operation.

### Contamination/residue/fouling

Using a machine for multiple materials can contaminate later processes.

This creates a tradeoff between:

- cheap multipurpose equipment
- expensive dedicated equipment

### Wear

Wear can alter behavior and sensor baselines.

This means maintenance can interact with measurement and control rather than being a completely separate durability system.

---

## 16. Energy Systems

Feedback should have multiple genuinely independent energy systems.

Electricity should not become the universal currency that all other systems secretly convert into.

The major proposed energy types are:

### Mechanical

Native sources may include:

- water wheels
- windmills
- treadmills
- steam pistons

Mechanical power is transmitted through:

- shafts
- gears
- belts

Mechanical systems experience losses such as friction and may require lubrication.

Typical applications:

- mixers
- grinders
- presses
- saws
- pumps
- drills

### Thermal

Sources may include:

- fire
- lava
- contained Blazes
- Nether/geothermal sources
- thermal mass

Heat conducts through connected systems.

Steam is primarily a **transport/working medium**, not itself the fundamental energy category.

Thermal ↔ mechanical conversion is possible through things such as turbines/engines.

Mechanical/electrical → thermal conversion can occur through things such as heat pumps.

### Chemical

Combustion fuels can become thermal energy.

Reactive intermediates are treated as actual physical substances.

They may:

- have shelf life
- decay
- become inert waste
- be difficult to transport

Chemistry uses the real periodic table as a backbone where useful, with Minecraft-exotic exceptions.

### Electrical

Possible sources include:

- lightning capture
- conversion from other energy systems

Electrical infrastructure provides:

- sensors
- logic
- precision/control
- electrolysis
- electromagnets
- other electrically useful phenomena

Electricity is important but is not intended to become the universal energy currency.

### Spatial

Ender/Chorus-derived teleportation technology is conceptually separate from normal energy transport.

Possible progression:

> Ender materials → Liquid Teleportant → discrete spatial jumps → Ender Relay

Spatial transport is deliberately capped so that it does not simply become infinite bulk logistics.

---

## 17. Mechanical Quantities

The current conceptual vocabulary includes:

### Fu

A cumulative mechanical application/work quantity.

Example:

> A process requires 30 Fu total.

### St

The strength of an individual mechanical application.

A process can therefore require both:

> 30 Fu total  
> Minimum 1 St per application

A stronger machine can satisfy the same total work requirement with fewer applications.

A harder material might require:

> 60 Fu total  
> Minimum 30 St per application

In that case, lots of tiny weak applications cannot substitute for sufficient instantaneous strength.

### Su

Likely **Stress Units**, following Create's established terminology for mechanical load/stress.

Do not use Su for speed.

### RPM

Use RPM for rotational speed rather than inventing another fictional speed unit.

Clockwise/counterclockwise should matter where the actual mechanical operation cares about rotational direction, particularly for systems involving reciprocal motion.

Rotary and reciprocal motion are not separate energy types.

---

## 18. Other Units

The general rule is:

> Use a fictional unit when Minecraft gameplay benefits from an abstract game-specific quantity; use familiar modding conventions when interoperability and readability matter more.

Current candidates:

- `Tu` — Temperature Units
- `Pu` — Pressure Units
- `Fu` — cumulative mechanical work/application quantity
- `St` — mechanical application strength
- `Su` — mechanical stress/load
- `RPM` — rotational speed
- `mB` — fluid volume
- `mB/t` — fluid flow rate
- `Eu` — currently being reconsidered; should not be framed as generic "electricity" and may instead represent a general work/energy quantity if retained
- a fictional mass unit is likely useful, but exact name/abbreviation remains undecided
- a concentration/amount-per-volume quantity may be useful for chemistry, but its exact name/abbreviation remains undecided

Avoid inventing a bespoke unit for every derivative.

Examples like:

> Work/t  
> Tu/t  
> mB/t

are sufficient.

Time should be displayed in both ticks and seconds where useful:

> 600 t (30 s)

---

## 19. Fluids and Flow

Use Minecraft's established `mB` convention.

Fluid direction generally does not need to be represented as a player-facing vector.

Pipes and connections already establish where fluid can travel.

Fluid flow can be represented with familiar quantities such as:

> mB/t

Only add more complicated flow variables if they create an actual gameplay decision.

---

## 20. Mechanical Direction

The mod should care about clockwise/counterclockwise rotation where it creates real mechanical consequences.

This is especially relevant to reciprocal/back-and-forth mechanisms.

Do not turn this into literal 3D vector simulation.

The intended model is mostly scalar/simple:

- how much
- how fast
- how strong
- which rotational direction, where relevant

The game should not require players to do physics homework to operate a machine.

---

## 21. Separation

There should be no generic magical identity filter.

Sorting and quality control should exploit physical properties.

Potential mechanisms include:

- density/gravity
- magnetism
- particle size/screening
- optical properties
- reflectivity

Optical systems can be fooled by visually similar materials.

This makes quality control a physical engineering problem rather than an item-ID problem.

---

## 22. Chemistry

Chemistry should allow multiple reaction paths without becoming a full chemistry simulator.

Tags can express broad reagent categories such as:

> any strong acid  
> any chloride salt

However, byproducts should remain meaningful.

Different reaction paths should potentially produce different byproducts, and those byproducts should matter.

The goal is a useful abstraction of chemistry rather than a general-purpose molecular simulation.

The real periodic table can serve as the backbone where it improves gameplay, with Minecraft-specific exceptions where Minecraft's actual behavior demands them.

---

## 23. Control/Logic

The control layer is conceptually inspired by systems such as Steve's Factory Manager.

A node-based in-world controller can connect:

### Inputs

- sensors
- redstone
- timers

### Logic

- comparisons
- AND
- OR
- NOT
- thresholds
- deadbands
- hysteresis

### Outputs

- breaker
- clutch
- damper
- valve
- Ender relay
- other actuators

Sensor tiers can determine how much information is available.

A basic sensor might only provide:

> below threshold / above threshold

A better sensor might provide a continuous value.

Bad control logic should be capable of causing problems such as relay flapping and unnecessary wear.

---

## 24. Sensor/Actuator Separation by Energy Type

Sensing and stopping are separate problems.

Different physical systems require different kinds of actuators and sensors.

Examples:

### Electrical

Actuator: breaker/relay  
Sensor: current clamp

Electrical stopping can be nearly instantaneous, but switching hardware may wear or arc.

### Mechanical

Actuator: clutch/declutch gear  
Sensor: torque/vibration measurement

Stopping is not necessarily instantaneous because rotating machinery can coast.

### Thermal

Actuator: damper/vent or controllable heat source  
Sensor: thermocouple/thermometer

Stopping heat input does not instantly remove stored thermal energy.

### Chemical

Actuator: reagent/feed valve  
Sensor: reactivity/pH-like probe

Stopping reagent input does not necessarily stop a reaction already underway.

This reinforces the central idea:

> **The correct control mechanism depends on the physical process being controlled.**

---

## 25. Earlygame Shape

A potential first stage can be described as a progression in capability rather than a list of technology tiers.

The player begins with manual processes.

Then:

> **Manual action**  
> "I can do this myself."

→

> **Primitive apparatus**  
> "I can make something repeatedly do this."

→

> **Continuous operation**  
> "It won't stop unless I stop it."

→

> **Timer automation**  
> "I can make it stop approximately when I expect it to."

→

> **Measurement**  
> "I can actually observe the process."

→

> **Control**  
> "I can make another component respond to that observation."

→

> **Precision**  
> "I can construct a system capable of maintaining a specific process condition."

This gives the first stage a coherent identity:

> **The player moves from performing processes to building apparatus that performs processes.**

The exact machines/materials filling these roles remain open.

---

## 26. A Possible Earlygame Example

One possible implementation might begin with forgiving processes such as copper processing.

A simple process can have:

- broad temperature tolerance
- low temperature requirements
- relatively unimportant heating rate
- simple manual implementation

This means vanilla-like equipment can accomplish it.

The player then encounters mechanical material working, initially done manually.

A primitive mechanical machine automates the repetitive action.

It does not recognize completion.

The player first controls it manually, then with a timer.

Later, a more demanding thermal process requires a narrow temperature range and sustained conditions.

It is technically possible to attempt with primitive equipment, but unreliable and inefficient.

The player obtains measurement equipment.

They can now observe the process.

Eventually they obtain control components and can construct a feedback loop.

The exact fictional materials, numbers, machines, and recipes used for this example are not established by this document.

---

## 27. What the First Stage Should Teach

By the end of the first stage, the player should understand:

- machines perform physical operations
- machines do not inherently know when they are finished
- continuous operation can alter finished products
- manual production is possible but slow
- timers provide crude automation
- measurements are separate from control
- sensors do not automatically control anything
- actuators do not automatically know what they should do
- precision is a property of the entire system
- process conditions matter
- rates can matter as much as quantities
- machine state can persist
- physical upgrades change behavior
- better machines improve old processes as well as new ones
- multiple physical solutions can satisfy the same process
- strange Minecraft phenomena can be useful before they are understood

The player should leave the stage thinking less:

> "What machine do I need for this recipe?"

and more:

> "What physical process is this, what does it require, and how can I build something that makes it happen reliably?"

---

## 28. Design Constraint: Don't Accidentally Build a Physics Simulator

Feedback should abstract aggressively wherever additional realism does not create interesting decisions.

Do not add a variable simply because real engineering has that variable.

The test for a mechanic should be:

> **Does modeling this create a meaningful engineering choice for the player?**

If not, abstract it away.

The goal is not to simulate reality.

The goal is to make Minecraft's physical processes feel coherent enough that players can reason about them and construct their own solutions.

---

## 29. Current Next Step

The broad identity is sufficiently established that the next useful design step is to begin assigning **concrete content** to the first stage.

However, this should be done from the observed progression structure rather than by inventing a giant item list.

For each capability in the earlygame, determine:

1. What actual problem does the player encounter?
2. What physical process solves it?
3. What are that process's requirements?
4. What can the player already do manually?
5. What primitive apparatus can automate it?
6. What makes that apparatus imperfect?
7. What measurement capability eventually improves it?
8. What control capability eventually improves it?
9. What existing processes become better as a consequence?
10. What new processes become economical without being artificially "unlocked"?

The first concrete content pass should therefore fill in the **first complete playable vertical slice**, not the entire mod.

---

## 30. Important Non-Conclusions

The following should **not** be treated as decided:

- exact tier names
- exact first machines
- exact first materials
- McGuffnium as a real material — it was only a metaphor/example
- exact unit names beyond the tentative ones above
- exact numerical values
- exact chemistry implementation
- exact separator mapping
- exact energy-generation recipes
- exact progression boundaries
- exact control-block implementation

The design is currently strong enough to constrain those decisions, but not strong enough to have made them yet.
