package org.graphstream.boids.forces.ntree;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.graphstream.boids.Boid;
import org.graphstream.boids.BoidForces;
import org.graphstream.boids.BoidSpecies;
import org.graphstream.boids.forces.ntree.NTreeForcesFactory.BoidParticle;
import org.miv.pherd.Particle;
import org.miv.pherd.geom.Point3;
import org.miv.pherd.geom.Vector3;
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
public class NTreeForces extends BoidForces {
	BoidParticle p;

	public NTreeForces(BoidParticle p) {
		this.p = p;
	}

	public Point3 getPosition() {
		return p.getPosition();
	}

	public void setPosition(double x, double y, double z) {
		p.setPosition(x, y, z);
	}

	public void compute() {
		Cell startCell = p.getCell().getTree().getRootCell();

		barycenter.set(0, 0, 0);
		direction.fill(0);
		attraction.fill(0);
		repulsion.fill(0);
		countAtt = 0;
		countRep = 0;

		Set<Boid> contacts = new HashSet<Boid>();

		exploreTree(startCell, contacts);
		p.b.checkNeighborhood(contacts.toArray(new Boid[contacts.size()]));

		if (countAtt > 0) {
			barycenter.scale(1f / countAtt, 1f / countAtt, 1f / countAtt);
			direction.scalarDiv(countAtt);
			attraction.set(barycenter.x - p.b.getPosition().x, barycenter.y
					- p.b.getPosition().y, barycenter.z - p.b.getPosition().z);
		}

		if (countRep > 0) {
			repulsion.scalarDiv(countRep);
		}
	}

	/**
	 * Recursively explore the n-tree to search for intersection cells, and the
	 * visible boids.
	 * 
	 * @param source
	 *            The boid the forces are computed on.
	 * @param cell
	 *            The cell to explore recursively.
	 */
	protected void exploreTree(Cell cell, Set<Boid> contacts) {
		if (intersection(p.b, cell)) {
			if (cell.isLeaf())
				forcesFromCell(cell, contacts);
			else {
				int n = cell.getSpace().getDivisions();

				for (int i = 0; i < n; ++i)
					exploreTree(cell.getSub(i), contacts);
			}
		}
	}

	/**
	 * A leaf cell has been found that is in intersection with the boid area,
	 * computes the forces from this cell.
	 * 
	 * @param source
	 *            The boid the forces are computed on.
	 * @param cell
	 *            The cell.
	 */
	protected void forcesFromCell(Cell cell, Set<Boid> contacts) {
		Iterator<? extends Particle> particles = cell.getParticles();
		Vector3 rep = new Vector3();

		while (particles.hasNext()) {
			Particle particle = particles.next();

			if (particle instanceof BoidParticle) {
				if (p != particle && isVisible(p.b, particle.getPosition())) {
					contacts.add(((BoidParticle) particle).b);
					actionWithNeighboor((BoidParticle) particle, rep);
				}
			}
		}

		// barycenter.move( data.getCenter() );
		// direction.add( data.getDirection() );
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

	/**
	 * True if the given position is visible by the boid.
	 * 
	 * <p>
	 * This method first check if the given point is under the max distance of
	 * view. If so, it checks if the point is in the angle of view. The angle of
	 * view is specified as the cosine of the angle between the boid direction
	 * vector and the vector between the boid and the given point. This means
	 * that -1 is equal to a 360 degree of vision (the angle of view test is
	 * deactivated in this case), 0 means 180 degree angle, and 0.5 a 90 degree
	 * angle for example.
	 * </p>
	 * 
	 * @param source
	 *            The source boid.
	 * @param point
	 *            The point to consider.
	 * 
	 * @return True if point is visible by source.
	 */
	@Override
	public boolean isVisible(Boid boid, Point3 point) {
		// Check both the distance and angle of view according to the
		// direction
		// of the source.
		BoidParticle source = ((NTreeForces) boid.getForces()).p;
		BoidSpecies species = source.getBoid().getSpecies();

		Point3 pos = source.getPosition();
		double d = pos.distance(point);

		// At good distance.
		if (d <= species.getViewZone()) {
			// If there is an angle of view.
			if (species.getAngleOfView() > -1) {
				Vector3 dir = new Vector3(source.dir);
				Vector3 light = new Vector3(point.x - pos.x, point.y - pos.y,
						point.z - pos.z);// (pos.x - point.x, pos.y
				// - point.y, pos.z -
				// point.z);

				dir.normalize();
				light.normalize();

				double angle = dir.dotProduct(light);

				// In the field of view.
				if (angle > species.getAngleOfView())
					return true;
			} else {
				return true;
			}
		}

		// Not in view.
		return false;
	}

	/**
	 * A boid particle p2 that is visible by p1 as been found, integrate it in
	 * the forces that apply to the boid p1.
	 * 
	 * @param p1
	 *            The source boid.
	 * @param p2
	 *            the boid visible by p1.
	 * @param rep
	 *            The repulsion to compute.
	 */
	protected void actionWithNeighboor(BoidParticle p2, Vector3 rep) {
		Point3 pp = p2.getPosition();
		BoidSpecies p1Species = p.b.getSpecies();
		BoidSpecies p2Species = p2.getBoid().getSpecies();
		double v = p.b.getSpecies().getViewZone();

		rep.set(p.getPosition().x - pp.x, p.getPosition().y - pp.y, p
				.getPosition().z
				- pp.z);

		double len = rep.length();

		if (len != 0) {
			if (p1Species != p2Species)
				rep.scalarMult(1 / (len * len) * p2Species.getFearFactor());
			else
				rep.scalarMult(1 / (len * len));
		}

		double a = Math.log(Math.min(len, v)) / Math.log(v);
		rep.scalarMult(a);

		repulsion.add(rep);
		countRep++;

		if (p1Species == p2Species) {
			barycenter.move(pp);
			direction.add(p2.dir);
			countAtt++;
		}
	}
}