package org.graphstream.glutil;

import java.io.IOException;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;

/**
 * Represents a pixel buffer.
 * 
 * <p>
 * A buffer is an addressable set of pixels associated with an output where
 * OpenGL rendering can occur. The output makes the link between the abstract
 * OGL frame buffer and a toolkit like AWT, Swing, QTJambi or SWT for example.
 * </p>
 * 
 * <p>
 * The pixel buffer can be associated with a surface in the graphical toolkit
 * that is part of a complete UI (the CANVAS mode), or be a window of its own
 * (the WINDOW mode) or even be full screen (the FULLSCREEN mode).
 * </p>
 * 
 * @author Antoine Dutot
 * @since 20040423
 */
public interface Buffer
{
// Constants
	
	/**
	 * Output modes. These are the way the link is done between the GL and a
	 * toolkit output.
	 */
	public static enum OutputMode
	{
		/**
		 *  Output to a canvas component.
		 */
		CANVAS,
		
		/**
		 * Output to a canvas, this canvas will not capture any event.
		 */
		CANVAS_NO_EVENTS,
		
		/**
		 *  Output to a top-level window.
		 */
		WINDOW,
		
		/**
		 *  Output to a full-screen window.
		 */
		FULLSCREEN
	}
	
	/**
	 * Image formats supported by the {@link #dumpImage(String, DumpImageFormat, boolean)}
	 * method.
	 */
	public static enum DumpImageFormat
	{
		PNG,
		JPG,
		BMP
	}

// Accessors
	
	/**
	 * JOGL GL instance.
	 * @return A graphic context and a way to make calls to it.
	 */
	GL2 getGl();

	/**
	 * JOGL GLU instance.
	 * @return A graphic context and a way to make calls to it.
	 */
	GLU getGlu();

	/**
	 * Buffer width in pixels.
	 * @return The exact width of the buffer (not of a possible window around).
	 */
	int getWidth();

	/**
	 * Buffer height in pixels.
	 * @return The exact height of the buffer (not of a possible window around).
	 */
	int getHeight();

	/**
	 * Size of the memory used by this buffer in bytes.
	 * @return Sum of all the buffers bytes used.
	 */
	int getMemUsed();

	/**
	 * Format of this buffer.
	 * @return A description of this buffer.
	 */
	BufferFormat getFormat();

	/**
	 * Number of registered listeners.
	 * @return Count of listeners.
	 */
	int getBufferListenerCount();

	/**
	 * I-th listener.
	 * @return A buffer listener.
	 */
	BufferListener getBufferListener( int i );
	
	/**
	 * Return the instance of graphical component that contains the GL canvas
	 * where graphics are rendered. As a Buffer can be implemented using several
	 * graphical toolkits (e.g. Swing, SWT or QTJambi) the returned object is
	 * not typed. This method returns null if the underlying toolkit does not
	 * allow to embed the GL buffer into another component.
	 * @return The instance of graphical component that contains the GL canvas
	 * where graphics are rendered, or null if unavailable.
	 */
	Object getComponent();

// Commands

	/**
	 * Make the associated GL context current and launch a display on all
	 * BufferListeners.
	 */
	void display();

	/**
	 * Change the buffer dimensions (width,height).
	 */
	void setSize( int width, int height );

	/**
	 * Register a listener.
	 * @param listener The listener to register.
	 */
	void addBufferListener( BufferListener listener );

	/**
	 * Remove a registered listener.
	 * @param listener The listener to remove.
	 */
	void removeBufferListener( BufferListener listener );
	
	/**
	 * Output the frame buffer contents to a file.
	 * @param fileName The file name.
	 * @param format The file format (See ).
	 * @param useAlpha
	 * @throws IOException
	 */
	void dumpImage( String fileName, DumpImageFormat format, boolean useAlpha )
		throws IOException;
}
