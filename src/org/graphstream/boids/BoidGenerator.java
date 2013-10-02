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

import org.graphstream.algorithm.generator.Generator;
import org.graphstream.stream.Sink;
import org.graphstream.stream.SourceBase;

/**
 * A generator using a boid graph to create a dynamic graph.
 * 
 * @author Guilhelm Savin
 * @author Antoine Dutot
 * 
 */
public class BoidGenerator extends SourceBase implements Generator {
	/**
	 * Boid graph used by generator.
	 */
	protected BoidGraph ctx;

	/**
	 * Optional configuration to load when generator begins. If null, a default
	 * species is created with one hundred of boids.
	 */
	protected String configuration;

	private Link proxy;

	/**
	 * Create a new boid generator with no configuration.
	 */
	public BoidGenerator() {
		this(null);
	}

	/**
	 * Create a new boid generator specifying a configuration to load when
	 * generation starts.
	 * 
	 * @param dgsConfiguration
	 *            the configuration
	 */
	public BoidGenerator(String dgsConfiguration) {
		this.configuration = dgsConfiguration;
		this.ctx = null;
		this.proxy = new Link();
	}

	/**
	 * Get the boid graph used by this generator to produce events. This graph
	 * only exists between calls to {@link #begin()} and {@link #end()}.
	 * 
	 * @return the boid graph
	 */
	public BoidGraph getBoidGraph() {
		return ctx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin() {
		if (ctx != null)
			throw new RuntimeException("generation already started");

		ctx = new BoidGraph();
		ctx.addSink(proxy);

		if (configuration != null) {
			try {
				ctx.loadDGSConfiguration(configuration);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} else {
			BoidSpecies species = ctx.addDefaultSpecies();
			species.setInitialCount(100);
			species.populate();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#end()
	 */
	public void end() {
		ctx.clearSinks();
		ctx.clear();

		ctx = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		ctx.step();
		return true;
	}

	private class Link implements Sink {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.graphstream.stream.AttributeSink#edgeAttributeAdded(java.lang
		 * .String, long, java.lang.String, java.lang.String, java.lang.Object)
		 */
		public void edgeAttributeAdded(String sourceId, long timeId,
				String edgeId, String attribute, Object value) {
			sendEdgeAttributeAdded(sourceId, timeId, edgeId, attribute, value);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.graphstream.stream.AttributeSink#edgeAttributeChanged(java.lang
		 * .String, long, java.lang.String, java.lang.String, java.lang.Object,
		 * java.lang.Object)
		 */
		public void edgeAttributeChanged(String sourceId, long timeId,
				String edgeId, String attribute, Object oldValue,
				Object newValue) {
			sendEdgeAttributeChanged(sourceId, timeId, edgeId, attribute,
					oldValue, newValue);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.graphstream.stream.AttributeSink#edgeAttributeRemoved(java.lang
		 * .String, long, java.lang.String, java.lang.String)
		 */
		public void edgeAttributeRemoved(String sourceId, long timeId,
				String edgeId, String attribute) {
			sendEdgeAttributeRemoved(sourceId, timeId, edgeId, attribute);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.graphstream.stream.AttributeSink#graphAttributeAdded(java.lang
		 * .String, long, java.lang.String, java.lang.Object)
		 */
		public void graphAttributeAdded(String sourceId, long timeId,
				String attribute, Object value) {
			sendGraphAttributeAdded(sourceId, timeId, attribute, value);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.graphstream.stream.AttributeSink#graphAttributeChanged(java.lang
		 * .String, long, java.lang.String, java.lang.Object, java.lang.Object)
		 */
		public void graphAttributeChanged(String sourceId, long timeId,
				String attribute, Object oldValue, Object newValue) {
			sendGraphAttributeChanged(sourceId, timeId, attribute, oldValue,
					newValue);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.graphstream.stream.AttributeSink#graphAttributeRemoved(java.lang
		 * .String, long, java.lang.String)
		 */
		public void graphAttributeRemoved(String sourceId, long timeId,
				String attribute) {
			sendGraphAttributeRemoved(sourceId, timeId, attribute);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.graphstream.stream.AttributeSink#nodeAttributeAdded(java.lang
		 * .String, long, java.lang.String, java.lang.String, java.lang.Object)
		 */
		public void nodeAttributeAdded(String sourceId, long timeId,
				String nodeId, String attribute, Object value) {
			sendNodeAttributeAdded(sourceId, timeId, nodeId, attribute, value);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.graphstream.stream.AttributeSink#nodeAttributeChanged(java.lang
		 * .String, long, java.lang.String, java.lang.String, java.lang.Object,
		 * java.lang.Object)
		 */
		public void nodeAttributeChanged(String sourceId, long timeId,
				String nodeId, String attribute, Object oldValue,
				Object newValue) {
			sendNodeAttributeChanged(sourceId, timeId, nodeId, attribute,
					oldValue, newValue);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.graphstream.stream.AttributeSink#nodeAttributeRemoved(java.lang
		 * .String, long, java.lang.String, java.lang.String)
		 */
		public void nodeAttributeRemoved(String sourceId, long timeId,
				String nodeId, String attribute) {
			sendNodeAttributeRemoved(sourceId, timeId, nodeId, attribute);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.graphstream.stream.ElementSink#edgeAdded(java.lang.String,
		 * long, java.lang.String, java.lang.String, java.lang.String, boolean)
		 */
		public void edgeAdded(String sourceId, long timeId, String edgeId,
				String fromNodeId, String toNodeId, boolean directed) {
			sendEdgeAdded(sourceId, timeId, edgeId, fromNodeId, toNodeId,
					directed);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.graphstream.stream.ElementSink#edgeRemoved(java.lang.String,
		 * long, java.lang.String)
		 */
		public void edgeRemoved(String sourceId, long timeId, String edgeId) {
			sendEdgeRemoved(sourceId, timeId, edgeId);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.graphstream.stream.ElementSink#graphCleared(java.lang.String,
		 * long)
		 */
		public void graphCleared(String sourceId, long timeId) {
			sendGraphCleared(sourceId, timeId);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.graphstream.stream.ElementSink#nodeAdded(java.lang.String,
		 * long, java.lang.String)
		 */
		public void nodeAdded(String sourceId, long timeId, String nodeId) {
			sendNodeAdded(sourceId, timeId, nodeId);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.graphstream.stream.ElementSink#nodeRemoved(java.lang.String,
		 * long, java.lang.String)
		 */
		public void nodeRemoved(String sourceId, long timeId, String nodeId) {
			sendNodeRemoved(sourceId, timeId, nodeId);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.graphstream.stream.ElementSink#stepBegins(java.lang.String,
		 * long, double)
		 */
		public void stepBegins(String sourceId, long timeId, double step) {
			sendStepBegins(sourceId, timeId, step);
		}
	}
}
