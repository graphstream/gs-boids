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
package org.graphstream.boids.forces.ntree;

import org.graphstream.boids.Boid;
import org.miv.pherd.ntree.Cell;

/**
 * A basic definition of forces for a boid.
 * 
 * <p>
 * The kind of forces exercising on a boid can be changed to either use a n-tree
 * or not, or to account for other kind of forces or another force model. This
 * is the default force system that matches the basic boid definition as defined
 * by Craig Reynolds.
 * </p>
 * 
 * @author Guilhelm Savin
 * @author Antoine Dutot
 */
public class NTreeForces3D extends NTreeForces {
	public NTreeForces3D(BoidParticle p) {
		super(p);
	}

	/**
	 * A rectangular intersection function, is the boid view area intersecting
	 * the given cell?. This provides a quick lookup function to test if a cell
	 * must be explored or not. Later, a better test according to a spherical
	 * view zone will be done.
	 * 
	 * @param cell
	 *            The cell to test for intersection with the boid rectangular
	 *            view area.
	 * @return True if there is an intersection.
	 */
	protected boolean intersection(Boid source, Cell cell) {
		double vz = source.getSpecies().getViewZone();

		double x1 = cell.getSpace().getLoAnchor().x;
		double y1 = cell.getSpace().getLoAnchor().y;
		double z1 = cell.getSpace().getLoAnchor().z;
		double x2 = cell.getSpace().getHiAnchor().x;
		double y2 = cell.getSpace().getHiAnchor().y;
		double z2 = cell.getSpace().getHiAnchor().z;

		double X1 = source.getPosition().x - vz;
		double Y1 = source.getPosition().y - vz;
		double Z1 = source.getPosition().z - vz;
		double X2 = source.getPosition().x + vz;
		double Y2 = source.getPosition().y + vz;
		double Z2 = source.getPosition().z + vz;

		// Only when the area is before or after the cell there cannot
		// exist an intersection (case a and b). Else there must be an
		// intersection (cases c, d, e and f).
		//
		// |-a-| +---------+ |-b-|
		// | |
		// |-c-| |-d-|
		// | |
		// | |-e-| |
		// | |
		// |-+----f----+-|
		// | |
		// +---------+

		if (X2 < x1 || X1 > x2)
			return false;

		if (Y2 < y1 || Y1 > y2)
			return false;

		if (Z2 < z1 || Z1 > z2)
			return false;

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.boids.BoidForces#is3D()
	 */
	public boolean is3D() {
		return true;
	}
}