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
import org.graphstream.boids.BoidSpecies;
import org.graphstream.boids.BoidForcesFactory;
import org.graphstream.stream.ElementSink;
import org.miv.pherd.Particle;
import org.miv.pherd.ParticleBox;
import org.miv.pherd.geom.Vector3;
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
		double area = 1;
		int maxParticlesPerCell = 10;

		this.space = new OctreeCellSpace(new Anchor(-area, -area, -area),
				new Anchor(area, area, area));
		this.pbox = new ParticleBox(maxParticlesPerCell, space,
				new BoidCellData());
		this.ctx = ctx;

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
		BoidParticle p = new BoidParticle(b);
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
	public void resize(double minx, double miny, double minz, double maxx,
			double maxy, double maxz) {
		Anchor lo = new Anchor(minx, miny, minz);
		Anchor hi = new Anchor(maxx, maxy, maxz);

		space.resize(lo, hi);
	}

	/**
	 * Internal representation of the boid position, and direction in the forces
	 * system.
	 * 
	 * @author Guilhelm Savin
	 * @author Antoine Dutot
	 */
	protected class BoidParticle extends Particle {
		/**
		 * Direction of the boid.
		 */
		protected Vector3 dir;

		/**
		 * Number of boids in view at each step.
		 */
		protected int contacts = 0;

		/**
		 * Number of boids of my group in view at each step.
		 */
		protected int mySpeciesContacts = 0;

		protected Boid b;

		/**
		 * New particle.
		 * 
		 * @param ctx
		 *            The set of global parameters.
		 */
		public BoidParticle(Boid b) {
			super(b.getId(), ctx.getRandom().nextDouble() * (ctx.getArea() * 2)
					- ctx.getArea(), ctx.getRandom().nextDouble()
					* (ctx.getArea() * 2) - ctx.getArea(), 0);

			this.b = b;
			this.dir = new Vector3(ctx.getRandom().nextDouble(), ctx
					.getRandom().nextDouble(), 0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.miv.pherd.Particle#move(int)
		 */
		@Override
		public void move(int time) {
			BoidSpecies species = b.getSpecies();
			contacts = 0;
			mySpeciesContacts = 0;

			b.getForces().compute();

			b.getForces().direction.scalarMult(species.getDirectionFactor());
			b.getForces().attraction.scalarMult(species.getAttractionFactor());
			b.getForces().repulsion.scalarMult(species.getRepulsionFactor());
			dir.scalarMult(species.getInertia());

			dir.add(b.getForces().direction);
			dir.add(b.getForces().attraction);
			dir.add(b.getForces().repulsion);

			if (ctx.isNormalizeMode()) {
				double len = dir.normalize();
				if (len <= species.getMinSpeed())
					len = species.getMinSpeed();
				else if (len >= species.getMaxSpeed())
					len = species.getMaxSpeed();

				dir.scalarMult(species.getSpeedFactor() * len);
			} else {
				dir.scalarMult(species.getSpeedFactor());
			}

			checkWalls();
			nextPos.move(dir);

			b.setAttribute("xyz", pos.x, pos.y, pos.z);

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

		/**
		 * Check the boid does not go out of the space walls.
		 */
		protected void checkWalls() {
			// /float area = ctx.area;
			float aarea = 0.000001f;

			if (nextPos.x + dir.data[0] <= space.getLoAnchor().x + aarea) {
				nextPos.x = space.getLoAnchor().x + aarea;
				dir.data[0] = -dir.data[0];
			} else if (nextPos.x + dir.data[0] >= space.getHiAnchor().x - aarea) {
				nextPos.x = space.getHiAnchor().x - aarea;
				dir.data[0] = -dir.data[0];
			}
			if (nextPos.y + dir.data[1] <= space.getLoAnchor().y + aarea) {
				nextPos.y = space.getLoAnchor().y + aarea;
				dir.data[1] = -dir.data[1];
			} else if (nextPos.y + dir.data[1] >= space.getHiAnchor().y - aarea) {
				nextPos.y = space.getHiAnchor().y - aarea;
				dir.data[1] = -dir.data[1];
			}
			if (nextPos.z + dir.data[2] <= space.getLoAnchor().z + aarea) {
				nextPos.z = space.getLoAnchor().z + aarea;
				dir.data[2] = -dir.data[2];
			} else if (nextPos.z + dir.data[2] >= space.getHiAnchor().z - aarea) {
				nextPos.z = space.getHiAnchor().z - aarea;
				dir.data[2] = -dir.data[2];
			}
		}
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
