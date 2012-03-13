::

	 _______ _______       ______  _______ _________ ______  _______ 
	(  ____ \  ____ \     (  ___ \(  ___  )\__   __/(  __  \(  ____ \
	| (    \/ (    \/     | (   ) ) (   ) |   ) (   | (  \  ) (    \/
	| |     | (_____ _____| (__/ /| |   | |   | |   | |   ) | (_____ 
	| | ____(_____  )_____)  __ ( | |   | |   | |   | |   | |_____  )
	| | \_  )     ) |     | (  \ \| |   | |   | |   | |   ) |     ) |
	| (___) |\____) |     | )___) ) (___) |___) (___| (__/  )\____) |
	(_______)_______)     |/ \___/(_______)\_______/(______/\_______)


A framework for boids simulation in GraphStream
===============================================

The gs-boids package provide an easy way to create a boid-like simulation and extract from it a dynamic graph. The basic idea is to consider one or more species of boids, each with its own parameters as defined by `Craig Reynolds <http://www.red3d.com/cwr/boids/>`_, and to associate with each of these boids a node in a graph. Then for each boid that actually sees another we create an edge. As the boids move in space they see different other boids and therefore the edge set evolves.

Boids
-----

The term boid comes from bird-oid, that is something that mimics the behavior of a real bird flying in flock. They where first imagined by Craig Reynolds as a way to simulate realistic flocks of birds, school of fiches, herds of animals, etc. 

The principle is quite simple: each boid has three main behaviors:

* Separation: it tries to avoid colliding others.
* Cohesion: it tries to remain grouped with the others.
* Alignment: it tries to remain in the overall direction of others.

Boids have a limited angle and distance of vision, and therefore only consider others in a small area.

The simulation is iterative, at each step, each boid will consider its neighbors in its vision area and will try to apply the three main behavior laws to determine its new direction and eventually speed.

The result is a simulation where individuals form groups or flocks that fly together, can be split in subgroups or at the contrary merge in one big group.

Some variations can be added to such a simulation, like adding the notion of species, with several groups of boids having distinct parameters, some of them eventually fleeing when seeing another group (like a prey and a predator).

This boid simulation
--------------------

This boid simulation uses this same model, adding the idea of species, as well as some more parameters to fine tune the way groups appear, evolve and disappear. 

Graphs from the simulation
--------------------------

By representing each boid with a node, and the visual interaction by an edge (a boid that sees another creates such an edge), we can define an evolving dynamic graph where edges are added or removed dynamically as groups form or disappear.

This is the basic idea of this module. This allows to build a large set of dynamic graph with various properties for testing algorithms on them.

Building
--------

As a facility a Maven POM file is provided to build this module. You will need the gs-core module to use it.

Using
-----

TODO.
