/*
 * Copyright 2006 - 2012
 *     Antoine Dutot	<antoine.dutot@graphstream-project.org>
 *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
 * 
 * This file is part of gs-boids <http://graphstream-project.org>.
 * 
 * gs-boids is a library whose purpose is to provide a boid behavior to a set of
 * particles.
 * 
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 */
package org.graphstream.boids;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;

import org.graphstream.boids.BoidGraph;
import org.graphstream.graph.Node;

/**
 * Parameters for each boids species.
 * 
 * @author Guilhelm Savin
 * @author Antoine Dutot
 */
public class BoidSpecies implements Iterable<Boid> {
	/**
	 * Kinds of parameters.
	 */
	public static enum Parameter {
		COUNT, ANGLE_OF_VIEW, VIEW_ZONE, SPEED_FACTOR, MAX_SPEED, MIN_SPEED, DIRECTION_FACTOR, ATTRACTION_FACTOR, REPULSION_FACTOR, INERTIA, FEAR_FACTOR, ADD_SPECIES_NAME_IN_UI_CLASS
	}

	/**
	 * Shared settings.
	 */
	protected BoidGraph ctx;

	/**
	 * The species name.
	 */
	protected final String name;

	/**
	 * The distance at which a boid is seen.
	 */
	protected double viewZone;

	/**
	 * The boid angle of view, [-1..1]. This is the cosine of the angle between
	 * the boid direction and the direction toward another boid. -1 Means 360
	 * degrees, all is visible. 0 means 180 degrees only boids in front are
	 * visible, 0.5 means 90 degrees, 0.25 means 45 degrees.
	 */
	protected double angleOfView;

	/**
	 * The boid speed at each step. This is the factor by which the speedFactor
	 * vector is scaled to move the boid at each step. This therefore not only
	 * accelerate the boid displacement, it also impacts the boid behavior
	 * (oscillation around the destination point, etc.)
	 */
	protected double speedFactor;

	/**
	 * Maximum speed bound.
	 */
	protected double maxSpeed;

	/**
	 * Minimum speed bound.
	 */
	protected double minSpeed;

	/**
	 * The importance of the other boids direction "overall" vector. The
	 * direction vector is the sum of all the visible boids direction vector
	 * divided by the number of boids seen. This factor is the importance of
	 * this direction in the boid direction.
	 */
	protected double directionFactor;

	/**
	 * How much other visible boids attract a boid. The barycenter of the
	 * visible boids attract a boid. The attraction is the vector between the
	 * boid an this barycenter. This factor is the importance of this vector in
	 * the boid direction.
	 */
	protected double attractionFactor;

	/**
	 * All the visible boids repulse the boid. The repulsion vector is the sum
	 * of all the vectors between all other visible boids and the considered
	 * boid, scaled by the number of visible boids. This factor is the
	 * importance of this vector in the boid direction.
	 */
	protected double repulsionFactor;

	/**
	 * The inertia is the importance of the boid previous direction in the boid
	 * direction.
	 */
	protected double inertia;

	/**
	 * Factor for repulsion on boids of other species. The fear that this
	 * species produces on other species.
	 */
	protected double fearFactor;

	/**
	 * The species main color.
	 */
	protected Color color = new Color(1, 0, 0);

	/**
	 * Collection of boids for this species.
	 */
	protected HashMap<String, Boid> boids;

	/**
	 * Specify a CSS class for the species name for the GraphStream viewer. 
	 */
	protected boolean addSpeciesNameInUIClass;

	/**
	 * Allow to create unique identifiers for boids.
	 */
	protected int currentIndex = 0;
	
	/**
	 * Allow to create unique identifiers for boids.
	 */
	protected long timestamp = System.nanoTime();
	
	/**
	 * Handle the addition or removal of boids.
	 */
	protected DemographicManager pop; 

	/**
	 * New default species with a random color.
	 * 
	 * @param ctx
	 *            context in which this species is evolving
	 * @param name
	 *            final name of this species
	 */
	public BoidSpecies(BoidGraph ctx, String name) {
		this.boids = new HashMap<String, Boid>();
		this.ctx = ctx;
		this.name = name;

		//
		// Default parameters
		//
		angleOfView = 0;
		viewZone = 0.15f;
		speedFactor = 0.3f;
		maxSpeed = 1f;
		minSpeed = 0.04f;
		directionFactor = 0.1f;
		attractionFactor = 0.5f;
		repulsionFactor = 0.001f;
		inertia = 1.1f;
		fearFactor = 1;
		addSpeciesNameInUIClass = true;
		pop = new DemographicManager.SpeciesDemographicManager(this, ctx, new Probability.ConstantProbability(0.01), new Probability.ConstantProbability(0.01));

		this.color = new Color(ctx.random.nextFloat(), ctx.random.nextFloat(),
				ctx.random.nextFloat());
	}

	/**
	 * Getter for the name of the species.
	 * 
	 * @return name of this species
	 */
	public String getName() {
		return name;
	}

	/**
	 * Same than {@link #set(Parameter, String)} but the enum constant is given
	 * as a string.
	 * 
	 * @param p
	 *            name of the enum constant associated with a parameter. Note
	 *            that this name is case-independent since it is converted to
	 *            upper case in the method
	 * @param val
	 *            string value of the parameter
	 * @throws IllegalArgumentException
	 *             if the enum constant does not exist
	 */
	public void set(String p, String val) throws IllegalArgumentException {
		Parameter param = Parameter.valueOf(p.toUpperCase());
		set(param, val);
	}

	/**
	 * Utility function to set parameter from an enum constant and a string
	 * value.
	 * 
	 * @param p
	 *            an enum constant associated with a parameter
	 * @param val
	 *            string value of the parameter
	 */
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
		case ADD_SPECIES_NAME_IN_UI_CLASS:
			addSpeciesNameInUIClass = Boolean.parseBoolean(val);
			break;
		}
	}

	/**
	 * Create a new unique id specific to this species. It can be used to create
	 * a new boid.
	 * 
	 * @return a new unique id
	 */
	public String createNewId() {
		return String.format("%s.%x_%x", name, timestamp, currentIndex++);
	}

	/**
	 * Create a new boid with an automatic id created using
	 * {@link #createNewId()}.
	 * 
	 * @return a new boid
	 */
	public Boid createBoid() {
		return createBoid(createNewId());
	}

	/**
	 * Create a new boid.
	 * 
	 * @param id
	 *            id of the new boid
	 * @return the new boid
	 */
	public Boid createBoid(String id) {
		return new Boid(ctx, this, id);
	}

	void register(Boid b) {
		boids.put(b.getId(), b);

		if (addSpeciesNameInUIClass) {
			String uiClass = b.getAttribute("ui.class");

			if (uiClass == null)
				uiClass = name;
			else
				uiClass = uiClass + " " + name;

			b.setAttribute("ui.class", uiClass);
		}
	}

	void unregister(Boid b) {
		boids.remove(b.getId());

		if (addSpeciesNameInUIClass) {
			String uiClass = b.getAttribute("ui.class");

			if (uiClass != null && uiClass.indexOf(name) != -1) {
				uiClass = uiClass.replaceAll("(^|\\s)" + name + "($|\\s)", " ");
				uiClass = uiClass.trim();

				b.setAttribute("ui.class", uiClass);
			}
		}
	}

	/**
	 * This method is called by {@link org.graphstream.boids.BoidGraph} at the
	 * end of each step. It can be used by sub-classes to add some code.
     * @param time The current boid graph time.
	 */
	public void terminateStep(double time) {
System.err.printf("## pop for %s%n", getName());
		pop.step(time);
	}

	/**
	 * Count of boids of this species.
	 * 
	 * @return boids count
	 */
	public int getPopulation() {
		return boids.size();
	}

	/**
	 * Create boids until population is at least count. If population is already
	 * greater than count, nothing happens.
	 * 
	 * @param count
	 *            the minimum boid count for this species
	 */
	public void setCount(int count) {
		while (boids.size() < count)
			ctx.addNode(createNewId());
	}

	/**
	 * The distance at which a boid is seen.
	 * 
	 * @return
	 */
	public double getViewZone() {
		return viewZone;
	}

	/**
	 * Change the distance at which a boid is seen.
	 * 
	 * @param viewZone
	 */
	public void setViewZone(double viewZone) {
		this.viewZone = viewZone;
	}

	/**
	 * The boid speed at each step. This is the factor by which the speedFactor
	 * vector is scaled to move the boid at each step. This therefore not only
	 * accelerate the boid displacement, it also impacts the boid behavior
	 * (oscillation around the destination point, etc.)
	 * 
	 * @return
	 */
	public double getSpeedFactor() {
		return speedFactor;
	}

	/**
	 * Change the boid speed at each step.
	 * 
	 * @param speedFactor
	 */
	public void setSpeedFactor(double speedFactor) {
		this.speedFactor = speedFactor;
	}

	/**
	 * Maximum speed bound.
	 * 
	 * @return
	 */
	public double getMaxSpeed() {
		return maxSpeed;
	}

	/**
	 * Change the maximum speed bound.
	 * 
	 * @param maxSpeed
	 */
	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	/**
	 * Minimum speed bound.
	 * 
	 * @return
	 */
	public double getMinSpeed() {
		return minSpeed;
	}

	/**
	 * Change the minimum speed bound.
	 * 
	 * @param minSpeed
	 */
	public void setMinSpeed(double minSpeed) {
		this.minSpeed = minSpeed;
	}

	/**
	 * The importance of the other boids direction "overall" vector. The
	 * direction vector is the sum of all the visible boids direction vector
	 * divided by the number of boids seen. This factor is the importance of
	 * this direction in the boid direction.
	 * 
	 * @return
	 */
	public double getDirectionFactor() {
		return directionFactor;
	}

	/**
	 * Change the importance of the other boids direction "overall" vector.
	 * 
	 * @param directionFactor
	 */
	public void setDirectionFactor(double directionFactor) {
		this.directionFactor = directionFactor;
	}

	/**
	 * How much other visible boids attract a boid. The barycenter of the
	 * visible boids attract a boid. The attraction is the vector between the
	 * boid an this barycenter. This factor is the importance of this vector in
	 * the boid direction.
	 * 
	 * @return
	 */
	public double getAttractionFactor() {
		return attractionFactor;
	}

	/**
	 * Change how much other visible boids attract a boid.
	 * 
	 * @param attractionFactor
	 */
	public void setAttractionFactor(double attractionFactor) {
		this.attractionFactor = attractionFactor;
	}

	/**
	 * All the visible boids repulse the boid. The repulsion vector is the sum
	 * of all the vectors between all other visible boids and the considered
	 * boid, scaled by the number of visible boids. This factor is the
	 * importance of this vector in the boid direction.
	 * 
	 * @return
	 */
	public double getRepulsionFactor() {
		return repulsionFactor;
	}

	/**
	 * Change how all the visible boids repulse the boid.
	 * 
	 * @param repulsionFactor
	 */
	public void setRepulsionFactor(double repulsionFactor) {
		this.repulsionFactor = repulsionFactor;
	}

	/**
	 * The inertia is the importance of the boid previous direction in the boid
	 * direction.
	 * 
	 * @return
	 */
	public double getInertia() {
		return inertia;
	}

	/**
	 * Change the inertia is the importance of the boid previous direction in
	 * the boid direction.
	 * 
	 * @param inertia
	 */
	public void setInertia(double inertia) {
		this.inertia = inertia;
	}

	/**
	 * Factor for repulsion on boids of other species. The fear that this
	 * species produces on other species.
	 * 
	 * @return
	 */
	public double getFearFactor() {
		return fearFactor;
	}

	/**
	 * Change the factor for repulsion on boids of other species.
	 * 
	 * @param fearFactor
	 */
	public void setFearFactor(double fearFactor) {
		this.fearFactor = fearFactor;
	}

	/**
	 * The species main color.
	 * 
	 * @return
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Change the species main color.
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * The boid angle of view, [-1..1]. This is the cosine of the angle between
	 * the boid direction and the direction toward another boid. -1 Means 360
	 * degrees, all is visible. 0 means 180 degrees only boids in front are
	 * visible, 0.5 means 90 degrees, 0.25 means 45 degrees.
	 * 
	 * @return angle of view for boid of this species
	 */
	public double getAngleOfView() {
		return angleOfView;
	}

	/**
	 * Set the angle of view for boids of this species.
	 * 
	 * @param aov
	 *            new angle of view
	 */
	public void setAngleOfView(double aov) {
		angleOfView = aov;
	}
	
	/**
	 * The probability that a boids clones itself.
	 * @param probability A probability instance.
	 */
	public void setReproductionProbability(Probability probability) {
		pop.setReproduceCondition(probability);
	}
	
	/**
	 * The probability that a boids die.
	 * @param probability A probability instance.
	 */
	public void setDeathCondition(Probability probability) {
		pop.setDeathProbability(probability);
	}
	
	/**
	 * Unregister this species from the boids graph.
	 * 
	 * This removes all boids pertaining to this species, and release the link with the graph.
	 */
	public void release() {
		pop.release();
		
		Iterator<Node> i = ctx.getNodeIterator();
		
		while(i.hasNext()) {
			Boid b = (Boid) i.next();
			if(b.getSpecies() == this) {
				i.remove();
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<Boid> iterator() {
		return boids.values().iterator();
	}
}