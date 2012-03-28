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

import java.util.Iterator;
import java.util.LinkedList;

import org.graphstream.graph.implementations.AbstractGraph;
import org.graphstream.graph.implementations.AdjacencyListNode;
import org.miv.pherd.geom.Point3;

/**
 * Represents a single bird-oid object.
 * 
 * <p>
 * A boid is both a particle in the forces system used to compute the position
 * and motion of the object, and a GraphStream node. This allows to consider a
 * graph made of all the boids.
 * </p>
 * 
 * <p>
 * The boid is in fact split in two parts, the {@link Boid} class itself and the
 * {@link BoidParticle} inner class that represents the boid in the forces
 * system. The boid particle in turn contains a {@link BoidForces} object that
 * represents all the forces exercising on the boid. Globally, the {@link Boid}
 * class acts on the graph and updates its position, creating links toward other
 * boids/nodes that it sees, whereas the {@link BoidParticle} and the
 * {@link BoidForces} are used to compute the boid position.
 * </p>
 * 
 * @author Guilhelm Savin
 * @author Antoine Dutot
 */
public class Boid extends AdjacencyListNode {

	protected final BoidSpecies species;

	/** Parameters of this group of boids. */

	/** The set of forces acting on this particle. */
	protected BoidForces forces;

	/**
	 * New boid as a node in the given graph.
	 * 
	 * @param graph
	 *            The graph this boids pertains to.
	 * @param id
	 *            The boid identifier in the graph and in the force system.
	 */
	public Boid(AbstractGraph graph, BoidSpecies species, String id) {
		super(graph, id);

		this.species = species;
		this.forces = null;
	}

	/**
	 * Force the position of the boid in space.
	 */
	public void setPosition(double x, double y, double z) {
		forces.setPosition(x, y, z);
	}

	/**
	 * Actual position of the boid in space.
	 */
	public Point3 getPosition() {
		return forces.getPosition();
	}

	/**
	 * Set of parameters used by this boid group.
	 */
	public BoidSpecies getSpecies() {
		return species;
	}

	public void setForces(BoidForces forces) {
		this.forces = forces;
	}

	public BoidForces getForces() {
		return forces;
	}

	public void checkNeighborhood(Boid... boids) {
		if (boids != null) {
			Iterator<Boid> it = getNeighborNodeIterator();
			LinkedList<Boid> toRemove = null;

			while (it.hasNext()) {
				boolean found = false;
				Boid b = it.next();

				for (Boid b2 : boids) {
					if (b == b2) {
						found = true;
						break;
					}
				}

				if (!found && !forces.isVisible(b, this.getPosition())) {
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

			for (Boid b2 : boids) {
				if (getEdgeBetween(b2) == null)
					getGraph().addEdge(getEdgeId(this, b2), this, b2);
			}
		}
	}

	/**
	 * Compute the edge identifier between two boids knowing their individual
	 * identifiers. This method ensures the identifiers are always in the same
	 * order so that we get the same edge whatever the order of the parameters
	 * b1 and b2.
	 */
	public static final String getEdgeId(Boid b1, Boid b2) {
		if (b1.hashCode() > b2.hashCode()) {
			Boid t = b1;
			b1 = b2;
			b2 = t;
		}

		return String.format("%s--%s", b1.getId(), b2.getId());
	}
}