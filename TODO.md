# Feedback — TODO

Working checklist. Source of truth for *what's next*; `feedback_philosophy.md` stays source of truth for *why*.

---

## 0. Housekeeping

- [ ] Commit `feedback_philosophy.md` + `feedback_slice_01.md` (modified since `4020934`)
- [ ] Fix stale item 3 in `CLAUDE.md` "Outstanding work" — says slice 1 has five open decisions incl. vanilla furnace; all five resolved

---

## 1. Decide first: build or spec

Slice 1 is fully drafted and every open decision in it is closed. Two ways forward, and this choice gates everything below.

- **Build slice 1.** Philosophy §20 argues emergent behavior can't be validated on paper. Slice was scoped small enough to implement (12 items, 7 machines, 2 process variables). Finds the problems no document will.
- **Write document 2 first.** Safer, but doc 2 written without a running slice means inventing numbers with nothing to test them against.

Recommendation: build. Write doc 2 from what the build forces you to decide.

---

## 2. Project scaffold

Platform decided: **NeoForge, Minecraft 1.21.1.**

- [ ] Decide ModDevGradle vs. NeoGradle
- [ ] Decide project layout / package structure
- [ ] Gradle scaffold, run configs, Parchment mappings
- [ ] Decide data-driven recipe format — philosophy §15 wants compat authored as a table, not code

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
