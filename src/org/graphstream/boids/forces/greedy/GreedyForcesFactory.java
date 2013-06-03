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

import org.graphstream.boids.Boid;
import org.graphstream.boids.BoidForces;
import org.graphstream.boids.BoidForcesFactory;
import org.graphstream.boids.BoidGraph;
import org.miv.pherd.geom.Point3;

public class GreedyForcesFactory implements BoidForcesFactory {
	protected BoidGraph ctx;

	public GreedyForcesFactory(BoidGraph ctx) {
		this.ctx = ctx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.boids.BoidForcesFactory#init()
	 */
	public void init() {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.boids.BoidForcesFactory#createNewForces(org.graphstream
	 * .boids.Boid)
	 */
	public BoidForces createNewForces(Boid b) {
		return new GreedyForces(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.boids.BoidForcesFactory#step()
	 */
	public void step() {
		for (Boid b : ctx.<Boid> getEachNode())
			b.getForces().compute();

		for (Boid b : ctx.<Boid> getEachNode()) {
			BoidForces f = b.getForces();
			f.getPosition().copy(f.getNextPosition());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.boids.BoidForcesFactory#resize(org.miv.pherd.geom.Point3,
	 * org.miv.pherd.geom.Point3)
	 */
	public void resize(Point3 low, Point3 high) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.boids.BoidForcesFactory#end()
	 */
	public void end() {
	}
}
