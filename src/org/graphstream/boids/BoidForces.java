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

import org.miv.pherd.geom.Point3;
import org.miv.pherd.geom.Vector3;

/**
 * Models the forces applied to a boid at each step.
 * 
 * <p>
 * This object is modified at each computation step to represent the forces
 * applied to a boid particle in the forces system. It is in charge of going
 * down the n-tree used to represent space and collect all the other boids in
 * the field of view that could influence this boid. It then integrates these
 * forces and compute a direction and barycenter (the point the boid tries to
 * reach).
 * </p>
 * 
 * @author Guilhelm Savin
 * @author Antoine Dutot
 */
public abstract class BoidForces {
	/**
	 * The position the boid tries to reach at each step.
	 */
	public Point3 barycenter;

	/**
	 * The direction of the boid at each step.
	 */
	public Vector3 direction;

	/**
	 * The integrated attraction toward other boids in view at each step.
	 */
	public Vector3 attraction;

	/**
	 * The integrated repulsion from the other boids in view at each step.
	 */
	public Vector3 repulsion;

	/**
	 * The number of boids we are attracted to.
	 */
	public int countAtt;

	/**
	 * The number of boids we are repulsed from.
	 */
	public int countRep;

	/**
	 * Forces all set at zero.
	 */
	public BoidForces() {
		barycenter = new Point3();
		direction = new Vector3();
		attraction = new Vector3();
		repulsion = new Vector3();
		countAtt = 0;
		countRep = 0;
	}

	/**
	 * Compute the forces applied to a boid under the form of a barycenter that
	 * the boids tries to reach (attraction), an overall direction for all the
	 * surrounding boids, an overall direction of all the surrounding boids.
	 * 
	 * @param source
	 *            The boid the forces are computed on.
	 * @param startCell
	 *            The start cell (usually the root cell of the n-tree).
	 */
	public abstract void compute();

	/**
	 * Integrate a repulsion vector.
	 */
	public void addRepulsion(Vector3 rep) {
		repulsion.add(rep);
		countRep++;
	}

	/**
	 * Integrate a direction influence.
	 */
	public void addDirection(Vector3 dir) {
		direction.add(dir);
		countAtt++;
	}

	/**
	 * Integrate an attraction vector.
	 */
	public void addAttraction(Vector3 att) {
		attraction.add(att);
		countAtt++;
	}

	/**
	 * Integrate a new point of influence.
	 */
	public void moveBarycenter(Point3 p) {
		barycenter.move(p);
	}

	/**
	 * Is the another point is space in the field of view?
	 */
	public abstract boolean isVisible(Boid boid, Point3 other);

	public abstract void setPosition(double x, double y, double z);

	public abstract Point3 getPosition();
}