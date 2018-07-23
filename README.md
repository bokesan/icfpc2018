# ICFP Programming Contest 2018 - A Storm of Minds

## Team

Jan Dreske and Christoph Breitkopf. We are based in Hannover, Germany and Vienna,
but met in Hannover for the contest.

## Implementation

Our solver is written in Java and requires a java 8 jvm to run.

Build from source using

    $ ./gradlew build

## Strategy

TODO

### Why we deserve the judge's prize

We probably don't. We did have a lot of ideas what to do - have a special mode
where out solvers don't optimize for low energy, but generate nice bot movements,
e.g. a ballet or a aerobatic team flying through the Gateway Arch while trailing
filled voxels (in high harmonics mode, of course). But alas, limited time prevented
us from implementing this.

## Running

Creating a trace:

    $ java -Xss200m -Xms1g -jar build/libs/icfpc2018-0.0.1.jar solve <model> <dest> <solver>

for assembly and deconstruction problems, and

    $ java -Xss200m -Xms1g -jar build/libs/icfpc2018-0.0.1.jar solve <sourcemodel> <targetmodel> <dest> <solver>

for reconstruction problems.
