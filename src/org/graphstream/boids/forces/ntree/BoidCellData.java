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

import java.util.Iterator;

import org.graphstream.boids.forces.ntree.NTreeForcesFactory.BoidParticle;
import org.miv.pherd.Particle;
import org.miv.pherd.geom.Vector3;
import org.miv.pherd.ntree.BarycenterCellData;
import org.miv.pherd.ntree.Cell;
import org.miv.pherd.ntree.CellData;
import org.miv.pherd.ntree.NTreeListener;

/**
 * Compute both the barycenter and average direction of the particles in the
 * box.
 * 
 * XXX TODO there are bugs here, verify this code and reuse it in the boid
 * computation (actually the ntree is used only to go faster in searching
 * neighbors).
 * 
 * @author Antoine Dutot
 * @since 2007
 */
public class BoidCellData extends BarycenterCellData {
	// Attributes

	public Vector3 dir;

	// Constructors

	public BoidCellData() {
		super();
		dir = new Vector3(0, 0, 0);
	}

	// Access

	public Vector3 getDirection() {
		return dir;
	}

	@Override
	public CellData newCellData() {
		return new BoidCellData();
	}

	// Commands

	@Override
	public void recompute() {
		float x = 0;
		float y = 0;
		float z = 0;
		float n = 0;

		dir.fill(0);

		weight = cell.getPopulation();

		if (cell.isLeaf()) {
			Iterator<? extends Particle> particles = cell.getParticles();

			while (particles.hasNext()) {
				Particle p = particles.next();

				if (p instanceof BoidParticle) {
					BoidParticle particle = (BoidParticle) p;

					x += particle.getPosition().x;
					y += particle.getPosition().y;
					z += particle.getPosition().z;

					dir.add(particle.dir);

					n++;
				}
			}

			if (n > 0) {
				x /= n;
				y /= n;
				z /= n;
			}

			center.set(x, y, z);
			// dir.normalize();

			if (n > 0)
				dir.scalarDiv(n);
		} else {
			float subcnt = cell.getSpace().getDivisions();
			float totpop = cell.getPopulation();
			int verif = 0;

			if (totpop > 0) {
				for (int i = 0; i < subcnt; ++i) {
					Cell subcell = cell.getSub(i);
					BoidCellData data = (BoidCellData) subcell.getData();
					float pop = subcell.getPopulation();

					verif += pop;

					x += data.center.x * pop;
					y += data.center.y * pop;
					z += data.center.z * pop;

					dir.add(data.dir);
				}

				assert verif == totpop : "Discrepancy in population counts ?";

				x /= totpop;
				y /= totpop;
				z /= totpop;
			}

			center.set(x, y, z);
			// dir.normalize();

			if (totpop > 0)
				dir.scalarDiv(totpop);
		}

		for (NTreeListener listener : cell.getTree().getListeners()) {
			listener.cellData(cell.getId(), "barycenter", this);
		}
	}
}