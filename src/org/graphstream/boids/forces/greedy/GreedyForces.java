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
package org.graphstream.boids.forces.greedy;

import java.util.Collection;
import java.util.LinkedList;

import org.graphstream.boids.Boid;
import org.graphstream.boids.BoidForces;
import org.graphstream.boids.BoidGraph;
import org.miv.pherd.geom.Point3;
import org.miv.pherd.geom.Vector3;

public class GreedyForces extends BoidForces {

	Point3 position;
	Vector3 direction;

	public GreedyForces(Boid b) {
		super(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.boids.BoidForces#compute()
	 */
	public void compute() {
		BoidGraph g = (BoidGraph) boid.getGraph();
		LinkedList<Boid> contacts = new LinkedList<Boid>();
		Vector3 rep = new Vector3();

		for (Boid b : g.<Boid> getEachNode()) {
			if (isVisible(boid, b.getPosition())) {
				actionWithNeighboor(b, rep);
				contacts.add(b);
			}
		}

		boid.checkNeighborhood(contacts.toArray(new Boid[contacts.size()]));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.boids.BoidForces#getPosition()
	 */
	public Point3 getPosition() {
		return position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.boids.BoidForces#setPosition(double, double, double)
	 */
	public void setPosition(double x, double y, double z) {
		position.set(x, y, z);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.boids.BoidForces#getDirection()
	 */
	public Vector3 getDirection() {
		return direction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.boids.BoidForces#getNeighborhood()
	 */
	public Collection<Boid> getNeighborhood() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.boids.BoidForces#getNextPosition()
	 */
	public Point3 getNextPosition() {
		// TODO Auto-generated method stub
		return null;
	}
}
