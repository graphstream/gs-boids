package org.graphstream.glutil.geom;

import javax.media.opengl.GL2;

/**
 * A simple geometry object that can be rendered to a OGL buffer.
 * 
 * @author Antoine Dutot
 */
public interface Geometry
{
	/**
	 * Render the geometry.
	 * @param gl The open gl.
	 */
	void display( GL2 gl );
	
	/**
	 * Clean up any resources used by the geometry.
	 * @param gl
	 */
	void delete( GL2 gl );
}
