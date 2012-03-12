package org.graphstream.glutil;

import java.util.*;
import java.awt.image.*;
import javax.media.opengl.*;


/**
 * Helper class to implement buffers.
 * 
 * <p>
 * It mainly implements the handling of buffer listeners.
 * </p>
 *
 * @author Antoine Dutot
 * @since 2007
 */
abstract class BufferBase implements Buffer, GLEventListener
{
// Attributes

	/**
	 * List of listeners.
	 */ 
	protected List<BufferListener> listeners = new ArrayList<BufferListener>();

	/**
	 * Format of this buffer.
	 */
	protected BufferFormat format;
	
	protected boolean inited = false;

// Access

	/**
	 * Format of this buffer.
	 */
	public BufferFormat getFormat()
	{
		return format;
	}

	/**
	 * Number of registered listeners.
	 */
	public int getBufferListenerCount()
	{
		return listeners.size();
	}

	/**
	 * I-th listener.
	 */
	public BufferListener getBufferListener( int i )
	{
		return listeners.get( i );
	}

	/**
	 * Dump the buffer to an image.
	 * @param useAlpha true if the image must contain an alpha channel.
	 */
	public abstract BufferedImage getImage( boolean useAlpha );

// Commands

	/**
	 * Make the associated GL context current and launch a display on all
	 * BufferListeners.
	 */
	public abstract void display();

	/**
	 * Set the buffer dimensions (<code>width</code>,<code>height</code>).
	 */
	public abstract void setSize( int width, int height );

	/**
	 * Register a listener.
	 * @param listener The listener to register.
	 */
	public void addBufferListener( BufferListener listener )
	{
		listeners.add( listener );
	}

	/**
	 * Remove a registered listener.
	 * @param listener The listener to remove.
	 */
	public void removeBufferListener( BufferListener listener )
	{
		int i = listeners.indexOf( listener );

		if( i >= 0 )
			listeners.remove( i );
	}

// GLEventListener

	public void display( GLAutoDrawable drawable )
	{
//		if( inited )
//		{
			for( BufferListener l : listeners )
				l.display( this );
//		}
	}

	public void displayChanged( GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged )
	{
		// Ignored!
	}

	public void init( GLAutoDrawable drawable )
	{
		inited = true;
		
		for( BufferListener l : listeners )
			l.init( this );
	}

	public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height )
	{
		for( BufferListener l : listeners )
			l.reshape( this, x, y, width, height );
	}
	
// Utility
	
	/**
	 * Utility method to check that, at the point of calling it, no error is
	 * stored in the OpenGL machine.
	 * @param msg A prefix to print before an eventual error, usually indicating
	 * 	from where the error originates.
	 */
	public void checkGlError( String msg )
	{
		GL  gl    = getGl();
		int error = gl.glGetError();
			
		if( msg == null )
			msg = "";
		
		if( error != 0 )
		{
			switch( error )
			{
				case GL.GL_INVALID_VALUE:
					System.err.printf( "Invalid value [%s] (error code %d)%n", msg, error );
					break;
				case GL.GL_INVALID_ENUM:
					System.err.printf( "Invalid enum [%s] (error code %d)%n", msg, error );
					break;
//				case GL.GL_INVALID_FRAMEBUFFER_OPERATION_EXT:
//					System.err.printf( "Invalid framebuffer operation ext [%s] (error code %d)%n", msg, error );
//					break;
				case GL.GL_INVALID_OPERATION:
					System.err.printf( "Invalid operation [%s] (error code %d)%n", msg, error );
					break;
				default:
					System.err.printf( "Unknown error [%s] (error code %d)%n", msg, error );
					break;
			}
		}
	}
}
