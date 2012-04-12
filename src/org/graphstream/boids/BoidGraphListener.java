package org.graphstream.boids;

/**
 * Listener for boids specific events on the boid graph.
 * 
 * <p>
 * These are not GraphStream events.
 * </p>
 * 
 * @author Guilhelm Savin
 * @author Antoine Dutot
 */
public interface BoidGraphListener {
	/**
	 * One iteration passed. During an iteration, all boids are moved according to their
	 * forces model.
	 * @param time The current iteration time.
	 */
	void step(int time);

	/** 
	 * One boid was added.
	 * @param boid The added boid.
	 */
	void boidAdded(Boid boid);

	/**
	 * One boid was removed.
	 * @param boid The removed boid.
	 */
	void boidDeleted(Boid boid);
}