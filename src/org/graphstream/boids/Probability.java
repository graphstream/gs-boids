package org.graphstream.boids;

/**
 * Various probability distributions.
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

	double getProbability(BoidGraph ctx, Boid b, int age);
}