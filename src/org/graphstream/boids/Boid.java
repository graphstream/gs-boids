/*
 * Copyright 2006 - 2011 
 *     Julien Baudry	<julien.baudry@graphstream-project.org>
 *     Antoine Dutot	<antoine.dutot@graphstream-project.org>
 *     Yoann Pigné		<yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
 * 
 * This file is part of GraphStream <http://graphstream-project.org>.
 * 
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
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

import java.util.Iterator;
import java.util.LinkedList;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.AbstractGraph;
import org.graphstream.graph.implementations.AdjacencyListNode;
import org.miv.pherd.Particle;
import org.miv.pherd.geom.Point3;
import org.miv.pherd.geom.Vector3;

/**
 * Represents a single bird-oid object.
 * 
 * <p>
 * A boid is both a particle in the forces system used to compute the position and motion
 * of the object, and a GraphStream node. This allows to consider a graph made of
 * all the boids.
 * </p>
 * 
 * <p>
 * The boid is in fact split in two parts, the {@link Boid} class itself and the 
 * {@link BoidParticle} inner class that represents the boid in the forces system. The
 * boid particle in turn contains a {@link Forces} object that represents all the forces
 * exercising on the boid. Globally, the {@link Boid} class acts on the graph and updates
 * its position, creating links toward other boids/nodes that it sees, whereas the
 * {@link BoidParticle} and the {@link Forces} are used to compute the boid position. 
 * </p>
 * 
 * @author Guilhelm Savin
 * @author Antoine Dutot
 */
public class Boid extends AdjacencyListNode {

	protected final BoidSpecies species;
	protected BoidParticle particle;
	
	/** Parameters of this group of boids. */
	
	/** The set of forces acting on this particle. */
	protected Forces forces;

	/**
	 * New boid as a node in the given graph.
	 *
	 * @param graph The graph this boids pertains to.
	 * @param id The boid identifier in the graph and in the force system.
	 */
		super((AbstractGraph) graph, id);

		this.particle = new BoidParticle((Context) graph);
		this.species = species;
		this.forces = getDefaultForces();
	}

	/** Force the position of the boid in space. */
	public void setPosition(double x, double y, double z) {
		particle.setPosition(x, y, z);
	}

	/** Actual position of the boid in space. */
	public Point3 getPosition() {
		return particle.getPosition();
	}

	/** Set of parameters used by this boid group. */
	public BoidSpecies getSpecies() {
		return species;
	}

	/** Change boid group and set of parameters. */
	/** The underlying particle of the force system this boids is linked to. */
	public BoidParticle getParticle() {
		return particle;
	}

	/**
	 * The forces acting on the boids, this is a set of vectors and parameters
	 * computed at each time step.
	 */
	public Forces getDefaultForces() {
		return new Forces.BasicForces();
	}

	protected void checkNeighborhood(BoidParticle... particles) {
		if (particles != null) {
			Iterator<Boid> it = getNeighborNodeIterator();
			LinkedList<Boid> toRemove = null;

			while (it.hasNext()) {
				boolean found = false;
				Boid b = it.next();

				for (BoidParticle p : particles) {
					if (p.getId().equals(b.getParticle().getId())) {
						found = true;
						break;
					}
				}

				if (!found && !forces.isVisible(b.particle, this.getPosition())) {
					if (toRemove == null)
						toRemove = new LinkedList<Boid>();

					toRemove.add(b);
				}
			}

			if (toRemove != null) {
				for (Boid b : toRemove)
					getGraph().removeEdge(getEdgeId(this, b));

				toRemove.clear();
				toRemove = null;
			}
			for (BoidParticle p : particles) {
				if (getEdgeBetween(p.getBoid().getId()) == null) {
					getGraph().addEdge(getEdgeId(this, p.getBoid()), getId(),
							p.getBoid().getId());
				}
			}
		}
	}

	/**
	 * Compute the edge identifier between two boids knowing their individual identifiers.
	 * This method ensures the identifiers are always in the same order so that we get the
	 * same edge whatever the order of the parameters b1 and b2. 
	 */
	public static final String getEdgeId(Boid b1, Boid b2) {
		if (b1.hashCode() > b2.hashCode()) {
			Boid t = b1;
			b1 = b2;
			b2 = t;
		}

		return String.format("%s--%s", b1.getId(), b2.getId());
	}

	/**
	 * Internal representation of the boid position, and direction in the forces system.
	 * 
	 * @author Guilhelm Savin
	 * @author Antoine Dutot
	 */
	class BoidParticle extends Particle {
		/** Direction of the boid. */
		protected Vector3 dir;
		
		/** Set of global parameters. */
		protected Context ctx;

		/** Number of boids in view at each step. */
		protected int contacts = 0;
		
		/** Number of boids of my group in view at each step. */
		protected int mySpeciesContacts = 0;

		/** 
		 * New particle.
		 * @param ctx The set of global parameters.
		 */
		public BoidParticle(Context ctx) {
			super(Boid.this.getId(), ctx.random.nextDouble() * (ctx.area * 2)
					- ctx.area, ctx.random.nextDouble() * (ctx.area * 2)
					- ctx.area, 0);

			this.dir = new Vector3(ctx.random.nextDouble(), ctx.random
					.nextDouble(), 0);
			this.ctx = ctx;
		}

		@Override
		public void move(int time) {
			contacts = 0;
			mySpeciesContacts = 0;

			forces.compute(Boid.this, cell.getTree().getRootCell());

			forces.direction.scalarMult(species.directionFactor);
			forces.attraction.scalarMult(species.attractionFactor);
			forces.repulsion.scalarMult(species.repulsionFactor);
			dir.scalarMult(species.inertia);

			dir.add(forces.direction);
			dir.add(forces.attraction);
			dir.add(forces.repulsion);

			if (ctx.normalizeMode) {
				double len = dir.normalize();
				if (len <= species.minSpeed)
					len = species.minSpeed;
				else if (len >= species.maxSpeed)
					len = species.maxSpeed;

				dir.scalarMult(species.speedFactor * len);
			} else {
				dir.scalarMult(species.speedFactor);
			}

			if (ctx.storeForcesAttributes)
				forces.store(this);

			checkWalls();
			nextPos.move(dir);

			Boid.this.setAttribute("xyz", pos.x, pos.y, pos.z);

			moved = true;
		}

		@Override
		public void inserted() {
		}

		@Override
		public void removed() {
		}

		public Boid getBoid() {
			return Boid.this;
		}

		public void setPosition(double x, double y, double z) {
			initPos(x, y, z);
		}

		/**
		 * Check the boid does not go out of the space walls.
		 */
		protected void checkWalls() {
			// /float area = ctx.area;
			float aarea = 0.000001f;

			if (nextPos.x + dir.data[0] <= ctx.getSpace().getLoAnchor().x
					+ aarea) {
				nextPos.x = ctx.getSpace().getLoAnchor().x + aarea;
				dir.data[0] = -dir.data[0];
			} else if (nextPos.x + dir.data[0] >= ctx.getSpace().getHiAnchor().x
					- aarea) {
				nextPos.x = ctx.getSpace().getHiAnchor().x - aarea;
				dir.data[0] = -dir.data[0];
			}
			if (nextPos.y + dir.data[1] <= ctx.getSpace().getLoAnchor().y
					+ aarea) {
				nextPos.y = ctx.getSpace().getLoAnchor().y + aarea;
				dir.data[1] = -dir.data[1];
			} else if (nextPos.y + dir.data[1] >= ctx.getSpace().getHiAnchor().y
					- aarea) {
				nextPos.y = ctx.getSpace().getHiAnchor().y - aarea;
				dir.data[1] = -dir.data[1];
			}
			if (nextPos.z + dir.data[2] <= ctx.getSpace().getLoAnchor().z
					+ aarea) {
				nextPos.z = ctx.getSpace().getLoAnchor().z + aarea;
				dir.data[2] = -dir.data[2];
			} else if (nextPos.z + dir.data[2] >= ctx.getSpace().getHiAnchor().z
					- aarea) {
				nextPos.z = ctx.getSpace().getHiAnchor().z - aarea;
				dir.data[2] = -dir.data[2];
			}
		}
	}
}