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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import org.graphstream.boids.forces.ntree.NTreeForcesFactory;
import org.graphstream.graph.Graph;
import org.graphstream.graph.NodeFactory;
import org.graphstream.graph.implementations.AbstractNode;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.util.Camera;
import org.miv.pherd.geom.Point3;
import org.miv.pherd.geom.Vector3;

import java.util.Random;

/**
 * Represents a boid simulation and their underlying interaction graph.
 * 
 * @author Damien Olivier
 * @author Guilhelm Savin
 * @author Antoine Dutot
 */
public class BoidGraph extends AdjacencyListGraph {

	public static enum Parameter {
		MAX_STEPS, AREA, SLEEP_TIME, STORE_FORCES_ATTRIBUTES, NORMALIZE_MODE, RANDOM_SEED, FORCES_FACTORY
	}

	/**
	 * Number of steps to run the simulation, 0 means infinity.
	 */
	protected int maxSteps;

	/**
	 * The radius of the explored area. The real area range is [-area..area] in
	 * all three dimensions.
	 */
	protected double area;

	/**
	 * Number of milliseconds to sleep between each particle computation step.
	 */
	protected int sleepTime;

	/**
	 * Store the forces as attributes so that each listener can retrieve the
	 * force vectors.
	 */
	protected boolean storeForcesAttributes;

	/**
	 * Normalize boids attraction/repulsion vectors (make the boids move
	 * constantly, since very small vectors can be extended).
	 */
	protected boolean normalizeMode;

	/**
	 * The fixed random seed.
	 */
	protected long randomSeed;

	/**
	 * Species for boids.
	 */
	protected HashMap<String, BoidSpecies> boidSpecies;

	/**
	 * The main loop condition.
	 */
	protected boolean loop;

	/**
	 * Current step.
	 */
	protected int step;

	/**
	 * Random number generator.
	 */
	protected Random random;

	/**
	 * Factory for the forces model used in boids.
	 */
	protected BoidForcesFactory forcesFactory;

	/**
	 * Lower point in space.
	 */
	protected Point3 lowAnchor;

	/**
	 * Higher point in space.
	 */
	protected Point3 highAnchor;

	/**
	 * Listeners for boid-graph specific events.
	 */
	protected ArrayList<BoidGraphListener> listeners = new ArrayList<BoidGraphListener>();
	
	/**
	 * New boids simulation represented as an interaction graph.
	 * 
	 * <p>All parameters are set to defaults.</p>
	 */
	public BoidGraph() {
		super("boids-context");
		setNodeFactory(new BoidFactory());

		random = new Random();
		randomSeed = random.nextLong();
		random = new Random(randomSeed);
		lowAnchor = new Point3(-1, -1, -1);
		highAnchor = new Point3(1, 1, 1);
		loop = false;
		normalizeMode = true;
		storeForcesAttributes = false;
		sleepTime = 20;
		area = 1;
		maxSteps = 0;
		boidSpecies = new HashMap<String, BoidSpecies>();

		setForcesFactory(new NTreeForcesFactory(this));
	}

	/**
	 * New boids simulation represented as an interaction graph.
	 * 
	 * <p>This pre-load a configuration from a DGS file.</p>
	 * 
	 * @param dgsConfig The initial configuration in DGS format.
	 * @throws IOException If the DGS cannot be read.
	 */
	public BoidGraph(String dgsConfig) throws IOException {
		this();
		loadDGSConfiguration(dgsConfig);
	}

	/**
	 * Load configuration from a dgs file or resource. See 'configExample.dgs'
	 * for an example of dgs configuration. The loader try to open the file
	 * first. If file is not found, loader try to get the resource using
	 * {@link java.lang.ClassLoader#getResourceAsStream(String)}.
	 * 
	 * @param dgs
	 *            path to the DGS file or resource containing the configuration.
	 * @throws IOException
	 *             if something wrong happens with io.
	 */
	public void loadDGSConfiguration(String dgs) throws IOException {
		InputStream in;

		try {
			in = new FileInputStream(dgs);
		} catch (FileNotFoundException e) {
			in = getClass().getResourceAsStream(dgs);

			if (in == null)
				throw e;
		}

		loadDGSConfiguration(in);
		in.close();
	}

	/**
	 * Load configuration from a dgs file. See 'configExample.dgs' for an
	 * example of dgs configuration.
	 * 
	 * @param in
	 * @throws IOException
	 *             if something wrong happens with io.
	 */
	public void loadDGSConfiguration(InputStream in) throws IOException {
		FileSourceDGS config = new FileSourceDGS();

		config.addSink(this);
		config.readAll(in);
		config.removeSink(this);
	}

	/**
	 * Set the factory used to instantiate the force system to use.
	 * @param bff The force system factory.
	 */
	public void setForcesFactory(BoidForcesFactory bff) {
		if (forcesFactory != null)
			forcesFactory.end();

		forcesFactory = bff;

		for (Boid b : this.<Boid> getEachNode()) {
			Point3 p = b.getPosition();
			Vector3 d = b.getForces().getDirection();

			b.setForces(bff.createNewForces(b));
			b.setPosition(p.x, p.y, p.z);
			b.getForces().getDirection().copy(d);
		}

		forcesFactory.init();

		System.out.printf("forces factory is now %s\n", bff.getClass()
				.getName());
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;

		lowAnchor.set(-area, -area, -area);
		highAnchor.set(area, area, area);

		forcesFactory.resize(lowAnchor, highAnchor);
	}

	public Point3 getLowAnchor() {
		return lowAnchor;
	}

	public Point3 getHighAnchor() {
		return highAnchor;
	}

	public long getRandomSeed() {
		return randomSeed;
	}

	public void setRandomSeed(long randomSeed) {
		this.randomSeed = randomSeed;
		this.random = new Random(randomSeed);
	}

	public int getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}

	public boolean isNormalizeMode() {
		return normalizeMode;
	}

	public void setNormalizeMode(boolean on) {
		normalizeMode = on;
	}

	public boolean isForcesAttributesStored() {
		return storeForcesAttributes;
	}

	public void setStoreForcesAttributes(boolean storeForcesAttributes) {
		this.storeForcesAttributes = storeForcesAttributes;
	}

	public int getMaxSteps() {
		return maxSteps;
	}

	public void setMaxSteps(int maxSteps) {
		this.maxSteps = maxSteps;
	}

	/**
	 * The random number generator used.
	 * @return The random number generator.
	 */
	public Random getRandom() {
		return random;
	}

	public BoidSpecies getOrCreateSpecies(String name) {
		return getOrCreateSpecies(name, null);
	}

	public BoidSpecies getOrCreateSpecies(String name, String clazz) {
		BoidSpecies species = boidSpecies.get(name);

		if (species == null) {
			if (clazz == null)
				species = new BoidSpecies(this, name);
			else {
				try {
					Class<?> classObj = Class.forName(clazz);
					Object obj = classObj.getConstructor(BoidGraph.class,
							String.class).newInstance(this, name);

					if (obj instanceof BoidSpecies) {
						species = (BoidSpecies) obj;
					} else {
						String msg = String.format(
								"not a species class : '%s'", clazz);

						throw new RuntimeException(msg);
					}
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				} catch (InstantiationException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (SecurityException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e);
				} catch (NoSuchMethodException e) {
					throw new RuntimeException(e);
				}
			}

			boidSpecies.put(name, species);
			System.out.printf("new species : %s\n", name);
		}

		return species;
	}

	/**
	 * The species with the given name.
	 * @param name The species name.
	 * @return The corresponding species or null if not found.
	 */
	public BoidSpecies getSpecies(String name) {
		return boidSpecies.get(name);
	}

	/**
	 * The number of boid species actually.
	 * @return The number of boid species.
	 */
	public int getSpeciesCount() {
		return boidSpecies.size();
	}

	/**
	 * The species whose name is "default".
	 * @return The default species.
	 */
	public BoidSpecies getDefaultSpecies() {
		return getSpecies("default");
	}

	/**
	 * Add a species with name "default" if does not yet exists.
	 * @return The created default species or the old one if it was already present. 
	 */
	public BoidSpecies addDefaultSpecies() {
		return getOrCreateSpecies("default");
	}

	/**
	 * Remove a species.
	 * 
	 * This also removed all the boids of this species. You cannot remove the "default" species.
	 * 
	 * @param name The species name.
	 */
	public void deleteSpecies(String name) {
		if (!name.equals("default")) {
			BoidSpecies species = boidSpecies.get(name);
			if(species != null) {
				species.release();
				boidSpecies.remove(name);
			}
		}
	}

	// Commands

	public void set(String paramName, String value)
			throws IllegalArgumentException {
		Parameter param = Parameter.valueOf(paramName.toUpperCase());
		set(param, value);
	}

	public void set(Parameter param, String value) {
		switch (param) {
		case MAX_STEPS:
			setMaxSteps(Integer.parseInt(value));
			break;
		case AREA:
			setArea(Float.parseFloat(value));
			break;
		case SLEEP_TIME:
			setSleepTime(Integer.parseInt(value));
			break;
		case STORE_FORCES_ATTRIBUTES:
			setStoreForcesAttributes(Boolean.parseBoolean(value));
			break;
		case NORMALIZE_MODE:
			setNormalizeMode(Boolean.parseBoolean(value));
			break;
		case RANDOM_SEED:
			setRandomSeed(Long.parseLong(value));
			break;
		case FORCES_FACTORY:
			Class<?> ffClass;
			Object obj = null;

			try {
				ffClass = Class.forName(value);
			} catch (ClassNotFoundException e) {
				System.err
						.printf("ForcesFactory class '%s' not found\n", value);
				return;
			}

			try {
				Constructor<?> c = ffClass.getConstructor(BoidGraph.class);
				obj = c.newInstance(this);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				System.err.printf("no constructor %s(BoidGraph) found.\n",
						value);
				System.err.printf("try to use default constructor.\n");

				try {
					obj = ffClass.newInstance();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

			if (obj == null) {
				System.err.printf("unable to create a %s forces factory\n",
						value);
				return;
			}

			if (obj instanceof BoidForcesFactory)
				setForcesFactory((BoidForcesFactory) obj);
			else
				System.err.printf("%s is not a forces factory\n", value);

			break;
		}
	}

	/**
	 * Stop the main simulation loop.
	 */
	public void stopLoop() {
		loop = false;
	}

	/**
	 * Run the simulation in a loop.
	 */
	public void loop() {
		loop = true;

		while (loop) {
			step();

			if (maxSteps > 0 && step > maxSteps)
				loop = false;

			sleep(sleepTime);
		}
	}

	public void step() {
		step++;
		stepBegins(step);
	}

	@Override
	public void stepBegins(double step) {
		for (BoidSpecies sp : boidSpecies.values()) {
			sp.terminateStep(step);
		}
		for (BoidGraphListener listener : listeners) {
			listener.step(step);
		}

		forcesFactory.step();
		
		super.stepBegins(step);
	}

	public boolean isLooping() {
		return loop;
	}

	@Override
	public Viewer display(boolean autoLayout) {
		Viewer v = super.display(autoLayout);
		Camera cam = v.getDefaultView().getCamera();
		cam.setGraphViewport(-area, -area, area, area);

		return v;
	}

	protected void sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Register a listener for boid specific events.
	 * 
	 * @param listener
	 *            The listener to register.
	 */
	public void addBoidGraphListener(BoidGraphListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Unregister a listener for boid specific events.
	 * @param listener The listener to remove.
	 */
	public void removeBoidGraphListener(BoidGraphListener listener) {
		int index = listeners.indexOf(listener);
		if(index >= 0) {
			listeners.remove(index);
		}
	}

	@Override
	protected void addNodeCallback(AbstractNode node) {
		Boid b = (Boid) node;
		b.getSpecies().register(b);

		super.addNodeCallback(node);

		for (BoidGraphListener listener : listeners) {
			listener.boidAdded(b);
		}
	}

	@Override
	protected void removeNodeCallback(AbstractNode node) {
		Boid b = (Boid) node;
		b.getSpecies().unregister(b);

		super.removeNodeCallback(node);

		for (BoidGraphListener listener : listeners) {
			listener.boidDeleted(b);
		}
	}

	@Override
	protected void attributeChanged(String sourceId, long timeId,
			String attribute, AttributeChangeEvent event, Object oldValue,
			Object newValue) {
		String key = attribute;

		if (key.startsWith("boids.")) {
			key = key.substring("boids.".length());

			if (key.startsWith("species.")) {
				key = key.substring("species.".length());
				String name;

				if (key.indexOf('.') > 0) {
					name = key.substring(0, key.indexOf('.'));
					key = key.substring(name.length() + 1);
				} else {
					name = key;
					key = null;
				}

				switch (event) {
				case REMOVE:
					if (boidSpecies.containsKey(name))
						deleteSpecies(name);

					break;
				case ADD:
				case CHANGE:
					BoidSpecies species;

					if (key == null && newValue != null
							&& newValue instanceof String)
						species = getOrCreateSpecies(name, (String) newValue);
					else
						species = getOrCreateSpecies(name);

					if (key != null) {
						try {
							species.set(key, newValue == null ? null : newValue
									.toString());
						} catch (IllegalArgumentException e) {
							System.err.printf("(WW) invalid parameter '%s'\n",
									key);
							System.err.printf("     ignoring it.\n");
						}
					}

					break;
				}
			} else
				set(key, newValue == null ? null : newValue.toString());
		}

		super.attributeChanged(sourceId, timeId, attribute, event, oldValue,
				newValue);
	}

	private class BoidFactory implements NodeFactory<Boid> {
		public Boid newInstance(String id, Graph graph) {
			BoidSpecies species = null;

			if (id.indexOf('.') != -1)
				species = boidSpecies.get(id.substring(0, id.indexOf('.')));

			if (species == null)
				species = getDefaultSpecies();

			Boid b = species.createBoid(id);
			BoidForces f = forcesFactory.createNewForces(b);

			b.setForces(f);

			return b;
		}
	}

	public static void main(String... args) {
		BoidGraph ctx = new BoidGraph();

		try {
			ctx.loadDGSConfiguration(BoidGraph.class
					.getResourceAsStream("configExampleWithTwoSpecies.dgs"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		ctx.display(false);
		ctx.loop();
	}
}