# ICFP Programming Contest 2018 - A Storm of Minds

## Team

Jan Dreske and Christoph Breitkopf. We are based in Hannover, Germany and Vienna,
but met in Hannover for the contest.

## Implementation

Our solver is written in Java and requires a java 8 jvm to run.

Build from source using

    $ ./gradlew build

## Strategy

We implemented multiple solvers and ran them on all problems (where applicable)
to choose the best one. The solvers are all generic - that is, we did not
write special solvers based on a certain problem structure or even on
individual problems.

Our solvers started with very simple (basically the default trace with movements
combined and low harmonics when possible) and progressed to using fission and
group commands later on.

Our solvers performance is mostly limited by an inefficient way to maintain
the information about which voxels are grounded. This caused us some pain
on the larger problems. For some problems we had to deactivate the slower solvers
to generate valid traces in time.

### Why we deserve the judge's prize

We probably don't. We did have a lot of ideas what to do - have a special mode
where our solvers don't optimize for low energy, but generate nice bot movements,
e.g. a ballet or a aerobatic team flying through the Gateway Arch while trailing
filled voxels (in high harmonics mode, of course). But alas, limited time prevented
us from implementing this. A modest attempt is ''solutions/arch.nbt'', to be used
with ''FD147_src.mdl''.

## Running

Creating a trace:

    $ java -Xss200m -Xms1g -jar build/libs/icfpc2018-0.0.1.jar solve <model> <dest> <solver>

for assembly and deconstruction problems, and

    $ java -Xss200m -Xms1g -jar build/libs/icfpc2018-0.0.1.jar solve <sourcemodel> <targetmodel> <dest> <solver>

for reconstruction problems. The solver names are listed in the source file
''SolverFactory.java''.

The jar supports more commands, most notably ''solveAll'' to try a set of solvers on
all problems in a directory.