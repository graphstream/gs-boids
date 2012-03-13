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

The gs-boids package provide an easy way to create a boid-like simulation and extract from it a dynamic graph. The basic idea is to consider one or more species of boids, each with its own parameters as defined by Craig Reynolds (citation !!), and to associate with each of these boids a node in a graph. Then for each boid that actually sees another we create an edge. As the boids move in space they see different other boids and therefore the edge set evolves.


