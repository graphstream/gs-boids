package org.graphstream.glutil.geom;

import javax.media.opengl.GL2;

/**
 * A simple cube.
 * 
 * @author Antoine Dutot
 */
public class Cube implements Geometry
{
// Attributes
	
	protected int list = -1;
	
	protected float width;
	
// Constructors
	
	public Cube( float width )
	{
		this.width = width;
	}
	
// Access
	
	public float getWidth()
	{
		return width;
	}
	
// Commands
	
	public void setWidth( GL2 gl, float width )
	{
		delete( gl );
		this.width = width;
	}
	
	public void delete( GL2 gl )
	{
		if( list > 0 )
		{
			gl.glDeleteLists( list, 1 );
			list = -1;
		}
	}
	
	public void display( GL2 gl )
	{
		if( list < 0 )
		{
			buildCube( gl );
		}
		else
		{
			gl.glCallList( list );
		}
	}
	
	protected void buildCube( GL2 gl )
	{
		if( list < 0 )
		{
			list = gl.glGenLists( 1 );
		
			float w = width / 2;
			
			gl.glNewList( list, GL2.GL_COMPILE_AND_EXECUTE );

			gl.glBegin( GL2.GL_QUADS );
				// Front
				gl.glNormal3f( 0, 0, 1 );
				gl.glVertex3f( -w, -w,  w );
				gl.glVertex3f(  w, -w,  w );
				gl.glVertex3f(  w,  w,  w );
				gl.glVertex3f( -w,  w,  w );
				// Back
				gl.glNormal3f( 0, 0, -1 );
				gl.glVertex3f( -w, -w, -w );
				gl.glVertex3f(  w, -w, -w );
				gl.glVertex3f(  w,  w, -w );
				gl.glVertex3f( -w,  w, -w );
				// Left
				gl.glNormal3f( -1, 0, 0 );
				gl.glVertex3f( -w, -w, -w );
				gl.glVertex3f( -w, -w,  w );
				gl.glVertex3f( -w,  w,  w );
				gl.glVertex3f( -w,  w, -w );
				// Right
				gl.glNormal3f( 1, 0, 0 );
				gl.glVertex3f(  w, -w, -w );
				gl.glVertex3f(  w, -w,  w );
				gl.glVertex3f(  w,  w,  w );
				gl.glVertex3f(  w,  w, -w );
				// Top
				gl.glNormal3f( 0, 1, 0 );
				gl.glVertex3f( -w,  w,  w );
				gl.glVertex3f(  w,  w,  w );
				gl.glVertex3f(  w,  w, -w );
				gl.glVertex3f( -w,  w, -w );
				// Bottom
				gl.glNormal3f( 0, -1, 0 );
				gl.glVertex3f( -w, -w,  w );
				gl.glVertex3f(  w, -w,  w );
				gl.glVertex3f(  w, -w, -w );
				gl.glVertex3f( -w, -w, -w );
			gl.glEnd();
			
			gl.glEndList();
		}
	}
}