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
import java.util.Random;

import org.graphstream.boids.Boid;
import org.graphstream.boids.BoidForces;
import org.graphstream.boids.BoidGraph;
import org.miv.pherd.geom.Point3;

public class GreedyForces extends BoidForces {

	Point3 position;
	Point3 nextPosition;

	public GreedyForces(Boid b) {
		super(b);

		BoidGraph ctx = (BoidGraph) b.getGraph();
		Random r = ctx.getRandom();
		Point3 lo = ctx.getLowAnchor();
		Point3 hi = ctx.getHighAnchor();

		position = new Point3();
		nextPosition = new Point3();

		position.x = r.nextDouble() * (hi.x - lo.x) + lo.x;
		position.y = r.nextDouble() * (hi.y - lo.y) + lo.y;
		position.z = 0;

		nextPosition.copy(position);
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
	 * @see org.graphstream.boids.BoidForces#getNeighborhood()
	 */
	public Collection<Boid> getNeighborhood() {
		BoidGraph g = (BoidGraph) boid.getGraph();
		LinkedList<Boid> contacts = new LinkedList<Boid>();

		for (Boid b : g.<Boid> getEachNode()) {
			if (isVisible(boid, b.getPosition()))
				contacts.add(b);
		}

		return contacts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.boids.BoidForces#getNextPosition()
	 */
	public Point3 getNextPosition() {
		return nextPosition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.boids.BoidForces#is3D()
	 */
	public boolean is3D() {
		return false;
	}
}
