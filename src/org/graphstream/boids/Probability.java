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

/**
 * Various probability distributions.
 * 
 * <p>The goal is to associate a probability to a boid according to various factors at a given
 * time.</p>
 * 
 * @author Guilhelm Savin
 */
public interface Probability {
	public static class ConstantProbability implements Probability {
		double p;

		public ConstantProbability(double p) {
			this.p = p;
		}

		public double getProbability(BoidGraph ctx, Boid b, int age) {
			return p;
		}
	}

	public static abstract class SigmoidProbability implements Probability {
		double lambda = 1;
		double seuil = 0;

		public SigmoidProbability(double lambda, double seuil) {
			this.lambda = lambda;
			this.seuil = seuil;
		}

		protected double getSigmoidValue(double x) {
			return 1.0 / (1.0 + Math.exp(-lambda * (x - seuil)));
		}

		protected double getSigmoidValue(double x, double lambda, double seuil) {
			return 1.0 / (1.0 + Math.exp(-lambda * (x - seuil)));
		}
	}

	public static class DeathProbability extends SigmoidProbability {
		public DeathProbability() {
			super(0.3, 80);
		}

		public DeathProbability(int averageLifeHopeness) {
			super(0.3, averageLifeHopeness);
		}

		public double getProbability(BoidGraph ctx, Boid b, int age) {
			return getSigmoidValue(age);
		}
	}

//	public static class EnergyDependentReproduceProbability extends
//			Probability.SigmoidProbability {
//		float reproduce;
//
//		public EnergyDependentReproduceProbability(float reproduce) {
//			super(0.1, 0);
//			this.reproduce = reproduce;
//		}
//
//		public double getProbability(BoidGraph ctx, Boid b, int age) {
//			return reproduce * getSigmoidValue(b.getForces().getEnergy());
//		}
//	}
//
//	public static class EnergyDependentDeathProbability extends
//			DeathProbability {
//		double lunchAlpha = 0.4;
//
//		public EnergyDependentDeathProbability(int averageLifeHopeness) {
//			seuil = averageLifeHopeness;
//		}
//
//		@Override
//		public double getProbability(BoidGraph ctx, Boid b, int age) {
//			if (b.getForces().getEnergy() <= 0)
//				return 1;
//			else
//				return super.getProbability(ctx, b, age);// Math.max(1-getSigmoidValue(b.getEnergy(),0.1,0),super.getProbability(ctx,b,age));
//		}
//	}

	/**
	 * Associate a probability with a given boid according to various parameters. 
	 * @param graph The boid graph.
	 * @param b The boid.
	 * @param age The boid age.
	 * @return The probability.
	 */
	double getProbability(BoidGraph graph, Boid b, int age);
}