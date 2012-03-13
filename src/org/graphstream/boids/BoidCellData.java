package org.graphstream.boids;

import java.util.Iterator;

import org.graphstream.boids.Boid.BoidParticle;
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
 * computation (actually the n-tree is used only to go faster in searching
 * neighbors).
 *
 * @author Guilhelm Savin
 * @author Antoine Dutot
 */
public class BoidCellData extends BarycenterCellData {
	/** Direction. */
	public Vector3 dir;

	public BoidCellData() {
		super();
		dir = new Vector3(0, 0, 0);
	}

	public Vector3 getDirection() {
		return dir;
	}

	@Override
	public CellData newCellData() {
		return new BoidCellData();
	}

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