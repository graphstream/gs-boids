/*
 * Copyright 2006 - 2011 
 *     Julien Baudry	<julien.baudry@graphstream-project.org>
 *     Antoine Dutot	<antoine.dutot@graphstream-project.org>
 *     Yoann Pigné		<yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
 * 
 * This file is part of GraphStream <http://graphstream-project.org>.
 * 
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
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

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import org.graphstream.boids.Boid.BoidParticle;
import org.miv.pherd.Particle;
import org.miv.pherd.geom.Point3;
import org.miv.pherd.geom.Vector3;
import org.miv.pherd.ntree.Cell;

/**
 * Models the forces applied to a boid at each step.
 * 
 * <p>
 * This object is modified at each computation step to represent the forces applied to
 * a boid particle in the forces system. It is in charge of going down the n-tree used
 * to represent space and collect all the other boids in the field of view that could influence
 * this boid. It then integrates these forces and compute a direction and barycenter (the
 * point the boid tries to reach).
 * </p>
 *
 * @author Guilhelm Savin
 * @author Antoine Dutot
 */
public abstract class Forces {
	/** The position the boid tries to reach at each step. */
	public Point3 barycenter;
	
	/** The direction of the boid at each step. */
	public Vector3 direction;
	
	/** The integrated attraction toward other boids in view at each step. */
	public Vector3 attraction;
	
	/** The integrated repulsion from the other boids in view at each step. */
	public Vector3 repulsion;
	
	/** The number of boids we are attracted to. */
	public int countAtt;
	
	/** The number of boids we are repulsed from. */
	public int countRep;

	/** Forces all set at zero. */
	public Forces() {
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
	public abstract void compute(Boid source, Cell startCell);

	/**
	 * Store each force vector as an attribute in the boid. This allows all
	 * listeners to retrieve the force vectors. This is particularly useful for
	 * the viewer for example. Each force is stored with the attribute name
	 * "force&lt;number&gt;" where &lt;number&gt; is a number that starts at
	 * zero.
	 * 
	 * @param boid
	 *            The boid to modify.
	 */
	protected void store(BoidParticle boid) {
		boid.setAttribute("force0", direction);
		boid.setAttribute("force1", attraction);
		boid.setAttribute("force2", repulsion);
	}

	/** Integrate a repulsion vector. */
	public void addRepulsion(Vector3 rep) {
		repulsion.add(rep);
		countRep++;
	}

	/** Integrate a direction influence. */
	public void addDirection(Vector3 dir) {
		direction.add(dir);
		countAtt++;
	}

	/** Integrate an attraction vector. */
	public void addAttraction(Vector3 att) {
		attraction.add(att);
		countAtt++;
	}

	/** Integrate a new point of influence. */
	public void moveBarycenter(Point3 p) {
		barycenter.move(p);
	}
	
	/** Is the another point is space in the field of view? */
	public abstract boolean isVisible(BoidParticle boid, Point3 other); 

	/**
	 * A basic definition of forces for a boid.
	 * 
	 * <p>
	 * The kind of forces exercising on a boid can be changed to either use a n-tree or not,
	 * or to account for other kind of forces or another force model. This is the default
	 * force system that matches the basic boid definition as defined by Craig Reynolds.
	 * </p>
	 * 
	 * @author Guilhelm Savin
	 * @author Antoine Dutot
	 */
	public static class BasicForces extends Forces {
		@Override
		public void compute(Boid source, Cell startCell) {
			barycenter.set(0, 0, 0);
			direction.fill(0);
			attraction.fill(0);
			repulsion.fill(0);
			countAtt = 0;
			countRep = 0;
			
			Set<BoidParticle> contacts = new HashSet<BoidParticle>();

			exploreTree(source, startCell, contacts);
			
			source.checkNeighborhood(contacts.toArray(new BoidParticle[contacts.size()]));
			
			if (countAtt > 0) {
				barycenter.scale(1f / countAtt, 1f / countAtt, 1f / countAtt);
				direction.scalarDiv(countAtt);
				attraction.set(barycenter.x - source.getPosition().x,
						barycenter.y - source.getPosition().y, barycenter.z
								- source.getPosition().z);
			}

			if (countRep > 0) {
				repulsion.scalarDiv(countRep);
			}
		}

		/**
		 * Recursively explore the n-tree to search for intersection cells, and
		 * the visible boids.
		 * 
		 * @param source
		 *            The boid the forces are computed on.
		 * @param cell
		 *            The cell to explore recursively.
		 */
		protected void exploreTree(Boid source, Cell cell, Set<BoidParticle> contacts) {
			if (intersection(source, cell)) {
				if (cell.isLeaf()) {
					forcesFromCell(source.getParticle(), cell, contacts);
				} else {
					int n = cell.getSpace().getDivisions();

					for (int i = 0; i < n; ++i)
						exploreTree(source, cell.getSub(i), contacts);
				}
			}
		}

		/**
		 * A leaf cell has been found that is in intersection with the boid
		 * area, computes the forces from this cell.
		 * 
		 * @param source
		 *            The boid the forces are computed on.
		 * @param cell
		 *            The cell.
		 */
		protected void forcesFromCell(BoidParticle source, Cell cell, Set<BoidParticle> contacts) {
			// BoidCellData data = (BoidCellData) cell.getData();
			Iterator<? extends Particle> particles = cell.getParticles();
			Vector3 rep = new Vector3();
			//LinkedList<BoidParticle> contacts = null;

			while (particles.hasNext()) {
				Particle particle = particles.next();

				if (particle instanceof BoidParticle) {
					//if (contacts == null)
					//	contacts = new LinkedList<BoidParticle>();

					if (source != particle
							&& isVisible(source, particle.getPosition())) {
						contacts.add((BoidParticle) particle);
						actionWithNeighboor(source, (BoidParticle) particle,
								rep);
					}
				}
			}

			// barycenter.move( data.getCenter() );
			// direction.add( data.getDirection() );
		}

		/**
		 * A rectangular intersection function, is the boid view area
		 * intersecting the given cell?. This provides a quick lookup function
		 * to test if a cell must be explored or not. Later, a better test
		 * according to a spherical view zone will be done.
		 * 
		 * @param cell
		 *            The cell to test for intersection with the boid
		 *            rectangular view area.
		 * @return True if there is an intersection.
		 */
		protected boolean intersection(Boid source, Cell cell) {
			double x1 = cell.getSpace().getLoAnchor().x;
			double y1 = cell.getSpace().getLoAnchor().y;
			double z1 = cell.getSpace().getLoAnchor().z;
			double x2 = cell.getSpace().getHiAnchor().x;
			double y2 = cell.getSpace().getHiAnchor().y;
			double z2 = cell.getSpace().getHiAnchor().z;

			double X1 = source.getPosition().x - source.getSpecies().viewZone;
			double Y1 = source.getPosition().y - source.getSpecies().viewZone;
			double Z1 = source.getPosition().z - source.getSpecies().viewZone;
			double X2 = source.getPosition().x + source.getSpecies().viewZone;
			double Y2 = source.getPosition().y + source.getSpecies().viewZone;
			double Z2 = source.getPosition().z + source.getSpecies().viewZone;

			// Only when the area is before or after the cell there cannot
			// exist an intersection (case a and b). Else there must be an
			// intersection (cases c, d, e and f).
			//
			// |-a-| +---------+ |-b-|
			//       |         |
			//     |-c-|     |-d-|
			//       |         |
			//       |  |-e-|  |
			//       |         |
			//     |-+----f----+-|
			//       |         |
			//       +---------+

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
		 * view. If so, it checks if the point is in the angle of view. The angle
		 * of view is specified as the cosine of the angle between the boid direction
		 * vector and the vector between the boid and the given point. This means
		 * that -1 is equal to a 360 degree of vision (the angle of view test is
		 * deactivated in this case), 0 means 180 degree angle, and 0.5 a 90 degree
		 * angle for example. 
		 * </p>
		 * 
		 * @param source
		 * 			  The source boid.
		 * @param point
		 *            The point to consider.
		 * 
		 * @return True if point is visible by source.
		 */
		@Override
		public boolean isVisible(BoidParticle source, Point3 point) {
			// Check both the distance and angle of view according to the direction
			// of the source.
			
			BoidSpecies species = source.getBoid().getSpecies();

			Point3 pos = source.getPosition();
			double d   = pos.distance(point);
			
			// At good distance.
			if(d <= species.viewZone) {
				// If there is an angle of view.
				if(species.angleOfView > -1) {
					Vector3 dir   = new Vector3(source.dir);
					Vector3 light = new Vector3(point.x - pos.x, point.y - pos.y, point.z - pos.z);//(pos.x - point.x, pos.y - point.y, pos.z - point.z);
					
					dir.normalize();
					light.normalize();
					
					double  angle = dir.dotProduct(light);
				
					// In the field of view.
					if(angle > species.angleOfView)
						return true;
				} else {
					return true;
				}
			}
			
			// Not in view.
			return false;
		}

		/**
		 * A boid particle p2 that is visible by p1 as been found, integrate it in the forces that
		 * apply to the boid p1.
		 * @param p1 The source boid.
		 * @param p2 the boid visible by p1.
		 * @param rep The repulsion to compute.
		 */
		protected void actionWithNeighboor(BoidParticle p1, BoidParticle p2,
				Vector3 rep) {
			Point3 pp = p2.getPosition();
			BoidSpecies p1Species = p1.getBoid().getSpecies();
			BoidSpecies p2Species = p2.getBoid().getSpecies();
			double v = p1.getBoid().getSpecies().viewZone;

			rep.set(p1.getPosition().x - pp.x, p1.getPosition().y - pp.y,
					p1.getPosition().z - pp.z);

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
}