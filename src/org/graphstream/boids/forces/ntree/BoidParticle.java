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
import org.graphstream.boids.BoidGraph;
import org.miv.pherd.Particle;
import org.miv.pherd.geom.Point3;

/**
 * Internal representation of the boid position, and direction in the forces
 * system.
 * 
 * @author Guilhelm Savin
 * @author Antoine Dutot
 */
public class BoidParticle extends Particle {
	protected Boid b;

	/**
	 * New particle.
	 * 
	 * @param ctx
	 *            The set of global parameters.
	 */
	public BoidParticle(BoidGraph ctx, Boid b) {
		super(b.getId(), ctx.getRandom().nextDouble() * (ctx.getArea() * 2)
				- ctx.getArea(), ctx.getRandom().nextDouble()
				* (ctx.getArea() * 2) - ctx.getArea(), 0);

		this.b = b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.miv.pherd.Particle#move(int)
	 */
	@Override
	public void move(int time) {
		b.getForces().compute();
		moved = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.miv.pherd.Particle#inserted()
	 */
	@Override
	public void inserted() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.miv.pherd.Particle#removed()
	 */
	@Override
	public void removed() {
	}

	public Boid getBoid() {
		return b;
	}

	public void setPosition(double x, double y, double z) {
		initPos(x, y, z);
	}

	public Point3 getNextPosition() {
		return nextPos;
	}
}