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
import org.graphstream.boids.BoidForces;
import org.graphstream.boids.BoidGraph;
import org.graphstream.boids.BoidForcesFactory;
import org.graphstream.stream.ElementSink;
import org.graphstream.stream.GraphReplay;
import org.miv.pherd.ParticleBox;
import org.miv.pherd.geom.Point3;
import org.miv.pherd.ntree.Anchor;
import org.miv.pherd.ntree.CellSpace;
import org.miv.pherd.ntree.OctreeCellSpace;

public class NTreeForcesFactory implements BoidForcesFactory, ElementSink {

	protected CellSpace space;
	/**
	 * The particles.
	 */
	protected ParticleBox pbox;

	protected BoidGraph ctx;

	public NTreeForcesFactory(BoidGraph ctx) {
		double area = ctx.getArea();
		int maxParticlesPerCell = 10;

		this.space = new OctreeCellSpace(new Anchor(-area, -area, -area),
				new Anchor(area, area, area));
		this.pbox = new ParticleBox(maxParticlesPerCell, space,
				new BoidCellData());
		this.ctx = ctx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.boids.BoidForcesFactory#init()
	 */
	public void init() {
		GraphReplay replay = new GraphReplay("replay");
		replay.addElementSink(this);
		replay.replay(ctx);
		replay.removeElementSink(this);

		ctx.addElementSink(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.boids.BoidForcesFactory#createNewForces(org.graphstream
	 * .boids.Boid)
	 */
	public BoidForces createNewForces(Boid b) {
		BoidParticle p = new BoidParticle(ctx, b);
		NTreeForces f = new NTreeForces(p);

		return f;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.boids.BoidForcesFactory#step()
	 */
	public void step() {
		pbox.step();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.boids.BoidForcesFactory#resize(double, double,
	 * double, double, double, double)
	 */
	public void resize(Point3 low, Point3 high) {
		space.resize(low, high);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.boids.BoidForcesFactory#end()
	 */
	public void end() {
		ctx.removeElementSink(this);
		pbox.removeAllParticles();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.ElementSink#edgeAdded(java.lang.String, long,
	 * java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.ElementSink#edgeRemoved(java.lang.String,
	 * long, java.lang.String)
	 */
	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.ElementSink#graphCleared(java.lang.String,
	 * long)
	 */
	public void graphCleared(String sourceId, long timeId) {
		pbox.removeAllParticles();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.ElementSink#nodeAdded(java.lang.String, long,
	 * java.lang.String)
	 */
	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		Boid b = ctx.getNode(nodeId);
		pbox.addParticle(((NTreeForces) b.getForces()).p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.ElementSink#nodeRemoved(java.lang.String,
	 * long, java.lang.String)
	 */
	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		Boid b = ctx.getNode(nodeId);
		pbox.removeParticle(((NTreeForces) b.getForces()).p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.ElementSink#stepBegins(java.lang.String,
	 * long, double)
	 */
	public void stepBegins(String sourceId, long timeId, double step) {
	}
}
