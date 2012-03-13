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
import org.graphstream.stream.AttributeSink;
import org.graphstream.stream.ElementSink;
import org.graphstream.stream.Sink;

/**
 * A generator using a boid graph to create a dynamic graph.
 * 
 * @author Guilhelm Savin
 * @author Antoine Dutot
 * 
 */
public class BoidGenerator implements Generator {
	/**
	 * Boid graph used by generator.
	 */
	protected BoidGraph ctx;

	/**
	 * Optional configuration to load when generator begins. If null, a default
	 * species is created with one hundred of boids.
	 */
	protected String configuration;

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

		if (configuration != null) {
			try {
				ctx.loadDGSConfiguration(configuration);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} else {
			ctx.addDefaultSpecies().setCount(100);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#end()
	 */
	public void end() {
		clearSinks();

		ctx.pbox.removeAllParticles();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.stream.Source#addAttributeSink(org.graphstream.stream
	 * .AttributeSink)
	 */
	public void addAttributeSink(AttributeSink sink) {
		ctx.addAttributeSink(sink);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.graphstream.stream.Source#addElementSink(org.graphstream.stream.
	 * ElementSink)
	 */
	public void addElementSink(ElementSink sink) {
		ctx.addElementSink(sink);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.Source#addSink(org.graphstream.stream.Sink)
	 */
	public void addSink(Sink sink) {
		ctx.addSink(sink);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.Source#clearAttributeSinks()
	 */
	public void clearAttributeSinks() {
		ctx.clearAttributeSinks();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.Source#clearElementSinks()
	 */
	public void clearElementSinks() {
		ctx.clearElementSinks();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.Source#clearSinks()
	 */
	public void clearSinks() {
		ctx.clearSinks();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.stream.Source#removeAttributeSink(org.graphstream.stream
	 * .AttributeSink)
	 */
	public void removeAttributeSink(AttributeSink sink) {
		ctx.removeAttributeSink(sink);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.stream.Source#removeElementSink(org.graphstream.stream
	 * .ElementSink)
	 */
	public void removeElementSink(ElementSink sink) {
		ctx.removeElementSink(sink);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.stream.Source#removeSink(org.graphstream.stream.Sink)
	 */
	public void removeSink(Sink sink) {
		ctx.removeSink(sink);
	}
}
