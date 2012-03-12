package org.graphstream.boids;

import java.awt.Color;
import java.util.HashMap;

import org.graphstream.boids.Context;

/**
 * Parameters for each boids species.
 * 
 * @author Antoine Dutot
 */
public class BoidSpecies extends HashMap<String, Boid> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6005548670964581065L;

	// Attributes

	/**
	 * Shared settings.
	 */
	protected Context ctx;

	public static enum Parameter {
		COUNT,
		VIEW_ZONE,
		SPEED_FACTOR,
		MAX_SPEED,
		MIN_SPEED,
		WIDTH,
		TRAIL,
		DIRECTION_FACTOR,
		ATTRACTION_FACTOR,
		REPULSION_FACTOR,
		INERTIA,
		FEAR_FACTOR
	}
	
	/**
	 * Initial number of boids of this species.
	 */
	public int count = 200;

	/**
	 * The species name.
	 */
	public String name = randomName();

	/**
	 * The distance at which a boid is seen.
	 */
	public double viewZone = 0.3f;

	/**
	 * The boid angle of view, [-1..1]. This is the cosine of the angle between the boid
	 * direction and the direction toward another boid. -1 Means 360 degrees, all is visible.
	 * 0 means 180 degrees only boids in front are visible, 0.5 means 90 degrees,
	 * 0.25 means 45 degrees.
	 */
	public double angleOfView = -1;

	/**
	 * The boid speed at each step. This is the factor by which the speedFactor
	 * vector is scaled to move the boid at each step. This therefore not only
	 * accelerate the boid displacement, it also impacts the boid behaviour
	 * (oscillation around the destination point, etc.)
	 */
	public double speedFactor = 0.5f;

	/**
	 * Maximum speed bound.
	 */
	public double maxSpeed = 1f;

	/**
	 * Minimum speed bound.
	 */
	public double minSpeed = 0.04f;

	/**
	 * The importance of the other boids direction "overall" vector. The
	 * direction vector is the sum of all the visible boids direction vector
	 * divided by the number of boids seen. This factor is the importance of
	 * this direction in the boid direction.
	 */
	public double directionFactor = 0.1f;

	/**
	 * How much other visible boids attract a boid. The barycenter of the
	 * visible boids attract a boid. The attraction is the vector between the
	 * boid an this barycenter. This factor is the importance of this vector in
	 * the boid direction.
	 */
	public double attractionFactor = 0.2f;

	/**
	 * All the visible boids repulse the boid. The repulsion vector is the sum
	 * of all the vectors between all other visible boids and the considered
	 * boid, scaled by the number of visible boids. This factor is the
	 * importance of this vector in the boid direction.
	 */
	public double repulsionFactor = 0.002f;

	/**
	 * The inertia is the importance of the boid previous direction in the boid
	 * direction.
	 */
	public double inertia = 1.1f;

	/**
	 * Factor for repulsion on boids of other species. The fear that this
	 * species produces on other species.
	 */
	public double fearFactor = 1;

	/**
	 * The species main colour.
	 */
	public Color color = new Color(1, 0, 0);

	/**
	 * The size of the trail in the GUI if any.
	 */
	public int trail = 0;

	/**
	 * The width of the particle in the GUI if any.
	 */
	public int width = 4;
	
	//protected DemographicManager demographicManager;

	// Constructors

	/**
	 * New default species with a random colour.
	 */
	public BoidSpecies(Context ctx, String name) {
		this.ctx = ctx;
		this.name = name;
		this.color = new Color(ctx.random.nextFloat(), ctx.random.nextFloat(),
				ctx.random.nextFloat());
	}

	// Access

	// Commands

	private int currentIndex = 0;
	private long timestamp = System.nanoTime();

	public void set(String p, String val) throws IllegalArgumentException {
		Parameter param = Parameter.valueOf(p.toUpperCase());
		set(param, val);
	}
	
	public void set(Parameter p, String val) {
		switch(p) {
		case COUNT:
			count = Integer.parseInt(val);
			break;
		case VIEW_ZONE:
			viewZone = Double.parseDouble(val);
			System.out.printf("%s view zone : %f\n", name, viewZone);
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
		case TRAIL:
			trail = Integer.parseInt(val);
			break;
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
		}
	}
	
	public String createNewId() {
		return String.format("%s_%x_%x", name, timestamp, currentIndex++);
	}

	public void register(Boid b) {
		put(b.getId(), b);
	}
	
	public void unregister(Boid b) {
		remove(b.getId());
	}

	public void terminateLoop() {
		// Do nothing.
		// Can be used by extending classes.
	}

	protected static String randomName() {
		return String.format("%c%c%c%c%c", (char) (64 + Math.random() * 26),
				(char) (64 + Math.random() * 26),
				(char) (64 + Math.random() * 26),
				(char) (64 + Math.random() * 26),
				(char) (64 + Math.random() * 26));
	}

	public int getPopulation() {
		return size();
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getViewZone() {
		return viewZone;
	}

	public void setViewZone(double viewZone) {
		this.viewZone = viewZone;
	}

	public double getSpeedFactor() {
		return speedFactor;
	}

	public void setSpeedFactor(double speedFactor) {
		this.speedFactor = speedFactor;
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public double getMinSpeed() {
		return minSpeed;
	}

	public void setMinSpeed(double minSpeed) {
		this.minSpeed = minSpeed;
	}

	public double getDirectionFactor() {
		return directionFactor;
	}

	public void setDirectionFactor(double directionFactor) {
		this.directionFactor = directionFactor;
	}

	public double getAttractionFactor() {
		return attractionFactor;
	}

	public void setAttractionFactor(double attractionFactor) {
		this.attractionFactor = attractionFactor;
	}

	public double getRepulsionFactor() {
		return repulsionFactor;
	}

	public void setRepulsionFactor(double repulsionFactor) {
		this.repulsionFactor = repulsionFactor;
	}

	public double getInertia() {
		return inertia;
	}

	public void setInertia(double inertia) {
		this.inertia = inertia;
	}

	public double getFearFactor() {
		return fearFactor;
	}

	public void setFearFactor(double fearFactor) {
		this.fearFactor = fearFactor;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setTrail(int trail) {
		this.trail = trail;
	}

	public void setWidth(int width) {
		this.width = width;
	}
}