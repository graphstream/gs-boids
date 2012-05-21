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

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Handles the appearance and disappearance of boids.
 * 
 * @author Guilhelm Savin
 * @author Antoine Dutot
 */
public class DemographicManager implements BoidGraphListener {
	
	/**
	 * The boid graph.
	 */
	protected BoidGraph ctx;
	
	/**
	 * Allow to count time.
	 */
	protected int currentDate = 0;

	/**
	 * List of boids marked for removal at each step.
	 * @see #check()
	 * @see #killAll()
	 */
	protected LinkedList<Boid> toRemove = new LinkedList<Boid>();
	
	/**
	 * List of boids marked for reproduction at each step.
	 * @see #check()
	 * @see #makeLove()
	 */
	protected LinkedList<Boid> futureParents = new LinkedList<Boid>();
	
	/**
	 * Date of birth of each active boid, according to {@link #currentDate}.
	 * @see #currentDate
	 */
	protected HashMap<Boid, Integer> birthdays = new HashMap<Boid, Integer>();
	
	/**
	 * Probability function for boid reproduction.
	 */
	protected Probability reproduceProbability;
	
	/**
	 * Probability function for boid death.
	 */
	protected Probability deathProbability;

	public DemographicManager(BoidGraph ctx) {
		this(ctx, new Probability.ConstantProbability(1), new Probability.DeathProbability());
	}

	public DemographicManager(BoidGraph ctx, Probability reproduceProbability, Probability deathProbability) {
		this.ctx = ctx;
		this.reproduceProbability = reproduceProbability;
		this.deathProbability = deathProbability;

		ctx.addBoidGraphListener(this);
	}

	/**
	 * Set the probability function for boid reproduction.
	 * @param rc The new probability.
	 */
	public void setReproduceCondition(Probability rc) {
		this.reproduceProbability = rc;
	}

	/**
	 * Set the probability function for boid death.
	 * @param dp The new probability.
	 */
	public void setDeathProbability(Probability dp) {
		this.deathProbability = dp;
	}

	/**
	 * Called by the boid graph each time a boid is added.
	 * @param b The boid to add.
	 */
	protected void register(Boid b) {
		birthdays.put(b, currentDate);
	}

	/**
	 * Called by the boid graph each time a boid is removed.
	 * @param b The boid to remove.
	 */
	protected void unregister(Boid b) {
		birthdays.remove(b);
	}

	/**
	 * Kill one boid.
	 * @param b The boid to remove.
	 */
	protected void kill(Boid b) {
		unregister(b);
		ctx.removeNode(b.getId());
	}

	/**
	 * Genocide all the boids marked for removed by {@link #check()}.
	 */
	protected void killAll() {
		/*
		 * if( toRemove.size() > 0 ) System.err.printf( "[DM] kill %d boids\n",
		 * toRemove.size() );
		 */
		while (toRemove.size() > 0)
			kill(toRemove.poll());
	}

	/**
	 * Create boids according to the list of future parents created by {@link #check()}.
	 */
	protected void makeLove() {
		Boid b;
//		int i = 0;
		while (futureParents.size() > 0) {
			b = futureParents.poll();
			
			String id = b.getSpecies().createNewId();
			ctx.addNode(id);
//			ctx.addBoid(b.getSpecies(), b.getPosition().x, b.getPosition().y, b
//					.getPosition().z);
//			i++;
		}
		/*
		 * if( i > 0 ) System.err.printf("[DM] born %d boids\n", i );
		 */
	}

	/**
	 * identifies boids succeptible to disapear or reproduce and remove/add them.
	 */
	protected void check() {
		int age;

		for (Boid b : birthdays.keySet()) {
			age = currentDate - birthdays.get(b);

			if (ctx.random.nextFloat() < deathProbability.getProbability(ctx, b, age)) {
				toRemove.add(b);
			} else if (ctx.random.nextFloat() < reproduceProbability .getProbability(ctx, b, age)) {
				futureParents.add(b);
			}
		}

		makeLove();
		killAll();

		currentDate++;
	}

	public void boidAdded(Boid boid) {
		register(boid);
	}

	public void boidDeleted(Boid boid) {
		unregister(boid);
	}

	/**
	 * Call this method after each step of boid computation, to add or remove boids according to
	 * reproduction rules. 
	 */
	public void step(double time) {
		check();
	}
	
	/**
	 * Release this manager, and remove as a listener for the graph.
	 */
	public void release() {
		ctx.removeBoidGraphListener(this);
	}

	/**
	 * A demographic manager that handles boids species.
	 * 
	 * @author Guilhelm Savin
	 * @author Antoine Dutot
	 */
	public static class SpeciesDemographicManager extends DemographicManager {
		BoidSpecies species;

		public SpeciesDemographicManager(BoidSpecies species, BoidGraph ctx) {
			super(ctx);

			this.species = species;
		}

		public SpeciesDemographicManager(BoidSpecies species, BoidGraph ctx,
				Probability r, Probability d) {
			super(ctx, r, d);

			this.species = species;
		}

		@Override
		protected void register(Boid b) {
			if (b.getSpecies() == species)
				super.register(b);
		}

		@Override
		protected void unregister(Boid b) {
			if (b.getSpecies() == species)
				super.unregister(b);
		}
	}
}