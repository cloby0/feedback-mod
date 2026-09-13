# Third-Party Licenses

## Feedback is GPL-3.0 (code) and All Rights Reserved (assets)

See `LICENSING.md` for why. The consequence for this file is that **the answer changed**:
the earlier version of this table said no copyleft source could be used at all, because the
project was All Rights Reserved by default and ARR cannot satisfy any copyleft licence's
terms. That was true, and it was the wrong position to be in by accident.

Under GPL-3.0 all three reference mods are legally available to adapt. What has not changed
is the working rule, which is stricter than the law on purpose: **prefer reading the design
and writing the code.** A borrowed implementation carries borrowed assumptions, and this mod
departs from all three of these deliberately.

## The table

| Mod | Licence | May we adapt its **code**? | Route, and what it obliges |
| :--- | :--- | :--- | :--- |
| **Create** | MIT | **Yes** | Permissive. Preserve the notice, state the origin |
| **GregTech CEu Modern** | LGPL-3.0 | **Yes** | LGPL-3.0 §2 permits conveying under GPL-3.0. Note the one-way street: they could not take ours |
| **TerraFirmaCraft** | EUPL-1.2 | **Yes** | Via EUPL Article 5 — its Appendix lists GPL v3 as a Compatible Licence, so a derivative may be distributed under GPL-3.0. **Record the election** wherever it is used |

**Assets are a separate question and the answer there is still no, universally.** Create's are
All Rights Reserved. GregTech's are third-party resource-pack imports (Gregtech: Refreshed,
ZedTech, TecTech) carrying their own terms. No texture, model or sound from any of them may
be used here, ever, whatever their code licence says.

Nothing in this repository is currently adapted from any of them. Every entry below records a
design debt, not copied code — which is worth keeping true, and worth noticing if it stops
being true.

## Create

Parts of Feedback's rotation network are derived from **Create**, specifically the
algorithm in `RotationPropagator` and `KineticNetwork` — source ownership, network
overpowering, conflict handling and the flicker counter. The code is rewritten rather
than copied, but the design is theirs and the debt is real.

Create's **code** is MIT licensed. Create's **assets** (everything under
`src/main/resources/assets/`) are *All Rights Reserved* — **no texture, model or sound
from Create may be used in this project, ever.**

Reference checkout lives outside this repository at `../Create-reference`
(branch `mc1.21.1/dev`). It is reference material, not a dependency.

```
MIT License

Copyright (c) The Create Team / The Creators of Create

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## TerraFirmaCraft

**EUPL-1.2 — adaptable, but only through Article 5, and the election must be recorded.**

EUPL-1.2 is strong copyleft: adapting TFC source obliges the derivative to be released under
the EUPL *or* under one of the Compatible Licences its Appendix lists. GPL v3 is on that list
— see `LICENSING.md`.

So the route exists, and it has a condition attached: relicensing a EUPL derivative to
GPL-3.0 is an **election this project makes**, and it should be traceable to the file that
prompted it rather than assumed globally. Nothing here has needed it yet.

Everything below was taken before that route was open, and is therefore what was never
copyrightable in the first place: the **ideas** — that an item's heat is best stored as a temperature plus
a timestamp and computed lazily, that forge colours make the right adjective scale, that
fuel is data rather than code. Those are read, understood, and then written from scratch
against this mod's own model, which is a materially different thing from adapting source.

Concretely, what TFC informed, with the reasoning recorded at each site:

- `ItemHeat` — the stamp-and-timestamp approach to workpiece heat. Arrived at
  independently here and then **confirmed** by TFC's `HeatComponent`, which stores
  `(capacity, lastTemperature, lastTick)` for the same reason. Convergence on an
  obvious-in-hindsight solution, not derivation.
- `Heat.cooled` — cooling switched from exponential to **linear** after reading TFC's
  `HeatCapability.adjustTemp`. See that method's own note for why; it is a game-design
  argument, not a physics one.
- `Readout.temperature` — the band structure and the idea of a **maximum visible
  temperature** come from TFC's `Heat` enum. The bands themselves are re-derived from
  blacksmithing colour references rather than copied, and the count and boundaries differ.
- `Fuel` / the `fuel/` datapack table — the shape `(ingredient, duration, temperature)`
  is TFC's `Fuel` record. It is also the obvious shape, and it is the one §15 already
  wanted for compat authored as a table.

TFC's **assets** are, as with Create, not ours to use.

Reference checkout lives outside this repository at `../TFC-reference`. It is reference
material, not a dependency, and it must never become one without the licence question
being settled first.


## GregTech CEu Modern

**Read for design. LGPL-3.0, which is weak copyleft — and "weak" is about linking, not
about copying.**

This is the distinction that catches people out, so it is worth stating plainly. The LGPL
exists so that a closed or differently-licensed program may **link against** a library
without itself becoming LGPL. That permission is about *use across an interface*. It is not
permission to paste the library's source into your own tree: copied or adapted code is a
"modified version of the Library" under §2, and it carries the LGPL with it.

So the practical answer is the same as TerraFirmaCraft's — **write every line from scratch**
— but the legal route differs, and one genuinely different option exists that does not for
TFC: Feedback *could* legitimately take GTCEu as a jar dependency without relicensing. We do
not want that, for the same reason we do not depend on Create: this mod's point of departure
from GregTech is architectural, and depending on GregTech to describe it would be absurd.

And note the interaction with the section at the top of this file: while Feedback is All
Rights Reserved, even the linking permission is unusable. ARR and LGPL cannot be combined
in a distributed work at all.

### What is worth studying, and why it is worth studying early

The cover system, for `instrument/` and whatever a Feedback cover eventually becomes. GTCEu
splits it three ways, and the split is the lesson rather than any individual class:

- `api/cover/CoverDefinition` — the registry entry. What kinds of cover exist.
- `api/cover/CoverBehavior` — one attached instance, which knows its **holder** and its
  **attached side**, and nothing else about the world.
- `ICoverable` — the host. What it means for a block to accept covers at all.
- `IIOCover`, `IUICover` — optional capability interfaces a cover opts into, rather than a
  base class that every cover pays for.

That last point is the one that maps straight onto this project. It is the same shape as
`Instrument`: declare a small number of things, and every existing display already knows what
to do with you. The reason to build it **before** a second cover needs it is the same reason
`Instrument` was built before the thermometer existed — a high fixed cost paid once beats no
fixed cost and a medium variable cost paid per feature, and the crossover is earlier than it
feels.

### What was looked at and rejected

**Machine maintenance.** GregTech charges a flat chance of a fault per hour a machine runs,
repaired with a wrench and a duct tape. Considered as the model for Feedback's hammer wear
and declined, because it is a tax on *uptime* and carries no information: a perfectly built
line accrues it at exactly the same rate as a badly built one, so the only thing the player
learns from a maintenance fault is that time has passed.

Feedback's wear is the inverse and is deliberately unprecedented as far as either of us
knows. It accrues **only** on force that could not go into the work — a workpiece too cold to
move, a blow under the material's hardness floor, a drive geared past what the machine's
construction can take. A correctly built line therefore never wears at all, ever, and a worn
hammer is not a bill but *evidence*, pointing at a specific mistake the player can go and
find. That is the same thing this mod does everywhere else: consequence follows from the
physics being wrong rather than from a clock.

Reference checkout lives outside this repository at `../GregTech-Modern-7.5.3`. Reference
material, never a dependency.
