# feedback

a minecraft tech mod about building and controlling physical processes, not unlocking recipe-specific machines.

## the pitch

machines don't know when a recipe is done. a furnace, a mixer, a stamp, whatever, it keeps doing its physical action for as long as it has power and input. it has no idea whether the thing inside is finished.

other tech mods already have machines that idle forever. that's not the new part. the new part is what happens if you let it keep going: **continued operation past completion acts on the already-finished output.** it doesn't just waste power, it degrades, burns, overworks, or ruins the thing you just made.

so you've actually got two problems, always:

1. make the process happen
2. notice when it's done, and stop or redirect it before it eats its own output

everything in the mod serves one of those two jobs. sensing is one system, control is another, actuation is a third, and none of them know about each other. a sensor doesn't control anything. an actuator doesn't know anything. the controller is a switch, not a dial, it can only start or stop a supply.

## what it isn't

not a tier ladder where the tier-3 furnace is a faster tier-2 furnace. a machine states a physical operation ("apply heat," "apply force"), never a recipe. there's no `if item == copper ingot` anywhere, sorting and processing work off real properties like density, magnetism, and particle size instead.

precision never locks a recipe behind a tier. if you have the raw physical ability, you can attempt it by hand, badly, at a tiny rate, forever. automation makes things practical. it doesn't unlock them.

mechanical, thermal, and chemical power are all usable on their own, none of them have to funnel through electricity first.

## status

design-first. the philosophy was written before the code. `feedback_philosophy.md` is the why-document, and it's still being actively revised as the build finds things the design got wrong. slice 1 (copper tooling, then a heat-controlled steel process) is the current target, see `TODO.md` for what's actually done versus in progress.

platform is neoforge, minecraft 1.21.1.

## building

```
./gradlew build       # jar to build/libs
./gradlew runClient    # dev client
```

## license

code is GPL-3.0-or-later. assets are all rights reserved. see `LICENSING.md`.
