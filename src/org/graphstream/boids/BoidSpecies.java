package org.graphstream.boids;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;

import org.graphstream.boids.Context;

/**
 * Parameters for each boids species.
 * 
 * @author Guilhelm Savin
 * @author Antoine Dutot
 */
public class BoidSpecies implements Iterable<Boid> {
	private static final long serialVersionUID = 6005548670964581065L;

	/** Kinds of parameters. */
	public static enum Parameter {
		COUNT, ANGLE_OF_VIEW, VIEW_ZONE, SPEED_FACTOR, MAX_SPEED, MIN_SPEED, WIDTH, TRAIL, DIRECTION_FACTOR, ATTRACTION_FACTOR, REPULSION_FACTOR, INERTIA, FEAR_FACTOR
	}

	/**
	 * Shared settings.
	 */
	protected Context ctx;

	/**
	 * The species name.
	 */
	protected final String name;

	/**
	 * The distance at which a boid is seen.
	 */
	protected double viewZone = 0.15f;

	/**
	 * The boid angle of view, [-1..1]. This is the cosine of the angle between
	 * the boid direction and the direction toward another boid. -1 Means 360
	 * degrees, all is visible. 0 means 180 degrees only boids in front are
	 * visible, 0.5 means 90 degrees, 0.25 means 45 degrees.
	 */
	protected double angleOfView = -1;

	/**
	 * The boid speed at each step. This is the factor by which the speedFactor
	 * vector is scaled to move the boid at each step. This therefore not only
	 * accelerate the boid displacement, it also impacts the boid behavior
	 * (oscillation around the destination point, etc.)
	 */
	protected double speedFactor = 0.3f;

	/**
	 * Maximum speed bound.
	 */
	protected double maxSpeed = 1f;

	/**
	 * Minimum speed bound.
	 */
	protected double minSpeed = 0.04f;

	/**
	 * The importance of the other boids direction "overall" vector. The
	 * direction vector is the sum of all the visible boids direction vector
	 * divided by the number of boids seen. This factor is the importance of
	 * this direction in the boid direction.
	 */
	protected double directionFactor = 0.1f;

	/**
	 * How much other visible boids attract a boid. The barycenter of the
	 * visible boids attract a boid. The attraction is the vector between the
	 * boid an this barycenter. This factor is the importance of this vector in
	 * the boid direction.
	 */
	protected double attractionFactor = 0.5f;

	/**
	 * All the visible boids repulse the boid. The repulsion vector is the sum
	 * of all the vectors between all other visible boids and the considered
	 * boid, scaled by the number of visible boids. This factor is the
	 * importance of this vector in the boid direction.
	 */
	protected double repulsionFactor = 0.001f;

	/**
	 * The inertia is the importance of the boid previous direction in the boid
	 * direction.
	 */
	protected double inertia = 1.1f;

	/**
	 * Factor for repulsion on boids of other species. The fear that this
	 * species produces on other species.
	 */
	protected double fearFactor = 1;

	/**
	 * The species main color.
	 */
	protected Color color = new Color(1, 0, 0);

//	/**
//	 * The size of the trail in the GUI if any.
//	 */
	protected int trail = 0;

	/**
	 * The width of the particle in the GUI if any.
	 */
	protected int width = 4;

	protected HashMap<String, Boid> boids;
	private int currentIndex = 0;
	private long timestamp = System.nanoTime();
	/**
	 * New default species with a random color.
	 */
	public BoidSpecies(Context ctx, String name) {
		this.boids = new HashMap<String, Boid>();
		this.ctx = ctx;
		this.name = name;
		this.color = new Color(ctx.random.nextFloat(), ctx.random.nextFloat(),
				ctx.random.nextFloat());
	}

	public String getName() {
		return name;
	}
	public Iterator<Boid> iterator() {
		return boids.values().iterator();
	}

	public void set(String p, String val) throws IllegalArgumentException {
		Parameter param = Parameter.valueOf(p.toUpperCase());
		set(param, val);
	}

	public void set(Parameter p, String val) {
		System.out.printf("set %s of %s to %s\n", p.name(), name, val);

		switch (p) {
		case COUNT:
			setCount(Integer.parseInt(val));
			break;
		case VIEW_ZONE:
			viewZone = Double.parseDouble(val);
			break;
		case SPEED_FACTOR:
			speedFactor = Double.parseDouble(val);
			break;
		case MAX_SPEED:
			maxSpeed = Double.parseDouble(val);
			break;
		case MIN_SPEED:
			minSpeed = Double.parseDouble(val);
			break;
		case WIDTH:
			width = Integer.parseInt(val);
			break;
//		case TRAIL:
//			trail = Integer.parseInt(val);
//			break;
		case DIRECTION_FACTOR:
			directionFactor = Double.parseDouble(val);
			break;
		case ATTRACTION_FACTOR:
			attractionFactor = Double.parseDouble(val);
			break;
		case REPULSION_FACTOR:
			repulsionFactor = Double.parseDouble(val);
			break;
		case INERTIA:
			inertia = Double.parseDouble(val);
			break;
		case FEAR_FACTOR:
			fearFactor = Double.parseDouble(val);
			break;
		case ANGLE_OF_VIEW:
			angleOfView = Double.parseDouble(val);
			break;
		}
	}

	public String createNewId() {
		return String.format("%s.%x_%x", name, timestamp, currentIndex++);
	}

	public Boid createBoid() {
		return createBoid(createNewId());
	}

	public Boid createBoid(String id) {
		return new Boid(ctx, this, id);
	}

	void register(Boid b) {
		boids.put(b.getId(), b);
	}

	void unregister(Boid b) {
		boids.remove(b.getId());
	}

	public void terminateLoop() {
		// Do nothing.
		// Can be used by extending classes.
	}

	public int getPopulation() {
		return boids.size();
	 */
	}

	/**
	 * Change the initial number of boids of this species.
	 */
	public void setCount(int count) {
		while (boids.size() < count)
			ctx.addNode(createNewId());
	/**
	 * The species name.
	 */
	/**
	 * Change the species name.
	 */
	}

	/**
	 * The distance at which a boid is seen.
	 */
	public double getViewZone() {
		return viewZone;
	}

	/**
	 * Change the distance at which a boid is seen.
	 */
	public void setViewZone(double viewZone) {
		this.viewZone = viewZone;
	}

	/**
	 * The boid speed at each step. This is the factor by which the speedFactor
	 * vector is scaled to move the boid at each step. This therefore not only
	 * accelerate the boid displacement, it also impacts the boid behavior
	 * (oscillation around the destination point, etc.)
	 */
	public double getSpeedFactor() {
		return speedFactor;
	}

	/**
	 * Change the boid speed at each step.
	 */
	public void setSpeedFactor(double speedFactor) {
		this.speedFactor = speedFactor;
	}

	/**
	 * Maximum speed bound.
	 */
	public double getMaxSpeed() {
		return maxSpeed;
	}

	/**
	 * Change the maximum speed bound.
	 */
	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	/**
	 * Minimum speed bound.
	 */
	public double getMinSpeed() {
		return minSpeed;
	}

	/**
	 * Change the minimum speed bound.
	 */
	public void setMinSpeed(double minSpeed) {
		this.minSpeed = minSpeed;
	}

	/**
	 * The importance of the other boids direction "overall" vector. The
	 * direction vector is the sum of all the visible boids direction vector
	 * divided by the number of boids seen. This factor is the importance of
	 * this direction in the boid direction.
	 */
	public double getDirectionFactor() {
		return directionFactor;
	}

	/**
	 * Change the importance of the other boids direction "overall" vector.
	 */
	public void setDirectionFactor(double directionFactor) {
		this.directionFactor = directionFactor;
	}

	/**
	 * How much other visible boids attract a boid. The barycenter of the
	 * visible boids attract a boid. The attraction is the vector between the
	 * boid an this barycenter. This factor is the importance of this vector in
	 * the boid direction.
	 */
	public double getAttractionFactor() {
		return attractionFactor;
	}

	/**
	 * Change how much other visible boids attract a boid.
	 */
	public void setAttractionFactor(double attractionFactor) {
		this.attractionFactor = attractionFactor;
	}

	/**
	 * All the visible boids repulse the boid. The repulsion vector is the sum
	 * of all the vectors between all other visible boids and the considered
	 * boid, scaled by the number of visible boids. This factor is the
	 * importance of this vector in the boid direction.
	 */
	public double getRepulsionFactor() {
		return repulsionFactor;
	}

	/**
	 * Change how all the visible boids repulse the boid.
	 */
	public void setRepulsionFactor(double repulsionFactor) {
		this.repulsionFactor = repulsionFactor;
	}

	/**
	 * The inertia is the importance of the boid previous direction in the boid
	 * direction.
	 */
	public double getInertia() {
		return inertia;
	}

	/**
	 * Change the inertia is the importance of the boid previous direction in the boid
	 * direction.
	 */
	public void setInertia(double inertia) {
		this.inertia = inertia;
	}

	/**
	 * Factor for repulsion on boids of other species. The fear that this
	 * species produces on other species.
	 */
	public double getFearFactor() {
		return fearFactor;
	}

	/**
	 * Change the factor for repulsion on boids of other species.
	 */
	public void setFearFactor(double fearFactor) {
		this.fearFactor = fearFactor;
	}

	/**
	 * The species main color.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Change the species main color.
	 */
	public void setColor(Color color) {
		this.color = color;
	}

//	public void setTrail(int trail) {
//		this.trail = trail;
//	}

	/**
	 * Change the width of the particle in the GUI if any.
	 */
	public void setWidth(int width) {
		this.width = width;
	}
}