package org.graphstream.glutil;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.*;
import javax.imageio.*;

/**
 * Representation of the FrameBuffer.
 *
 * <p>
 * FrameBuffer objects represent the main addressable pixel space for OpenGL
 * commands.
 * </p>
 *
 * <p>
 * The FrameBuffer defines both a window (if not in full screen mode) and GL
 * canvas (representation of the frame buffer).
 * </p>
 *
 * @author Antoine Dutot
 * @since 20040423
 */
public class SwingBuffer
	extends
		BufferBase
	implements
		KeyListener,
		MouseListener,
		MouseMotionListener,
		MouseWheelListener,
		WindowListener
{
// Attributes -- GL

	/**
	 * Output window. If the windowed mode is requested, this defines a simple
	 * window to contain the GL canvas.
	 */
	protected SimpleFrame frame;

	/**
	 * Output screen.
	 */
	protected GraphicsDevice screen;

	/**
	 * Video surface.
	 */
	protected GLCanvas canvas;

	/**
	 * When full screen has been requested, this flag indicates if we are really
	 * in full screen.
	 */
	protected boolean really_fullscreen = false;

	/**
	 * Mouse buttons current state.
	 */
	protected int button;
	
	/**
	 * GLU implementation attached to this buffer.
	 */
	protected GLU glu;

	/**
	 * If true, not automatic repaint event is taken into account. Else each
	 * time the windowing system triggers a repaint (the window content has
	 * been erased and must be repainted), the display() method will be
	 * called.
	 */
	protected boolean ignoreRepaint = true;

// Constructors

	/**
	 * New frame buffer of given dimensions in a window. The window may be
	 * larger due to window-manager decorations. The buffer is assumed to
	 * renders in the current thread.
	 * @param title Title of the frame.
	 * @param width Width in pixels of the frame buffer.
	 * @param height Height in pixels of the frame buffer.
	 */
	public SwingBuffer( BufferListener listener, String title, int width, int height )
		throws GLException
	{
		this( Thread.currentThread(), listener, title, width, height, OutputMode.WINDOW, true );
	}
	
	/**
	 * New frame buffer of given dimensions in a window or in full screen. The
	 * window may be larger due to window-manager decorations. The buffer is
	 * assumed to renders in the current thread.
	 * @param title Title of the frame.
	 * @param width Width in pixels of the frame buffer.
	 * @param height Height in pixels of the frame buffer.
	 * @param outputMode Indicate the kind of buffer output, Window, Canvas or full screen.
	 * @param ignoreRepaint If true, automatic repaint triggered by the windowing system are ignored.
	 */
	public SwingBuffer( BufferListener listener, String title, int width, int height, OutputMode outputMode, boolean ignoreRepaint )
		throws GLException
	{
		this( Thread.currentThread(), listener, title, width, height, outputMode, ignoreRepaint );
	}

	/**
	 * New frame buffer of given dimensions in a window or in full screen. The
	 * window may be larger due to window-manager decorations. The buffer will
	 * render in <code>display_thread</code>.
	 * assumed to renders in the current thread.
	 * @param display_thread The thread where rendering will occur.
	 * @param title Title of the frame.
	 * @param width Width in pixels of the frame buffer.
	 * @param height Height in pixels of the frame buffer.
	 * @param outputMode Indicate the kind of buffer output, Window, Canvas or full screen.
	 * @param ignoreRepaint If true, automatic repaint triggered by the windowing system are ignored.
	 */
	public SwingBuffer( Thread display_thread, BufferListener listener, String title, int width, int height, OutputMode outputMode, boolean ignoreRepaint )
		throws GLException
	{
		this( new BufferFormat( 8, 8, 8, 8, 16, true ), display_thread, listener, title, width, height, outputMode, ignoreRepaint );
	}

	/**
	 * New frame buffer of given dimensions in a window or in full screen with
	 * the exact format specification. The window may be larger due to
	 * window-manager decorations. The buffer will render in
	 * <code>display_thread</code>. assumed to renders in the current thread.
	 * @param format Size of each component of the buffer in bit and settings.
	 * @param display_thread The thread where rendering will occur.
	 * @param title Title of the frame.
	 * @param width Width in pixels of the frame buffer.
	 * @param height Height in pixels of the frame buffer.
	 * @param outputMode Indicate the kind of buffer output, Window, Canvas or full screen.
	 * @param ignoreRepaint If true, automatic repaint triggered by the windowing system are ignored.
	 */
	public SwingBuffer( BufferFormat format, Thread display_thread, BufferListener listener, String title, int width, int height, OutputMode outputMode, boolean ignoreRepaint )
		throws GLException
	{
		if( listener != null )
			addBufferListener( listener );
		
		GLCapabilities cap = new GLCapabilities(GLProfile.getGL2ES2());
		this.ignoreRepaint = ignoreRepaint;
		this.format        = format.clone();
		this.glu           = new GLU();
		
		cap.setRedBits( format.getRedBits() );
		cap.setGreenBits( format.getGreenBits() );
		cap.setBlueBits( format.getBlueBits() );
		cap.setAlphaBits( format.getAlphaBits() );
		cap.setDepthBits( format.getDepthBits() );
		cap.setDoubleBuffered( format.isDoubleBuffered() );
		cap.setAccumRedBits( 0 );
		cap.setAccumGreenBits( 0 );
		cap.setAccumBlueBits( 0 );
		cap.setAccumAlphaBits( 0 );
		cap.setStencilBits( 0 );
		cap.setStereo( false );
		cap.setSampleBuffers( false );
		
//		System.err.printf( "CAPABILITIES[%s]%n", cap.toString() );

		if( outputMode == OutputMode.WINDOW || outputMode == OutputMode.FULLSCREEN )
		{
			frame  = new SimpleFrame( title, ignoreRepaint );
			frame.addWindowListener( this );
			frame.addMouseListener( this );
			frame.addMouseMotionListener( this );
			frame.addMouseWheelListener( this );
			frame.addKeyListener( this );
		}

		canvas = new MyGLCanvas( cap, ignoreRepaint );// GLAutoDrawableFactory.getFactory().createGLCanvas( cap );
		
		canvas.setAutoSwapBufferMode( true );		
		
		if( outputMode != OutputMode.CANVAS_NO_EVENTS )
		{
			canvas.addMouseListener( this );
			canvas.addMouseMotionListener( this );
			canvas.addMouseWheelListener( this );
			canvas.addKeyListener( this );
			canvas.requestFocus();
		}
		else
		{
			canvas.setFocusable( false );
		}

		canvas.addGLEventListener( this );
	
		if( frame != null )
		{
			frame.setSize( width, height );		// Set size a first time to have insets.
			frame.add( canvas );
			frame.setVisible( true );
			setSize( width, height );			// Now resize it adding insets to ensure the canvas is exactly the correct size.
			frame.setIgnoreRepaint( true );
		}
		else
		{
			canvas.setSize( width, height );
		}

		if( outputMode == OutputMode.FULLSCREEN )
		{
//			System.err.println( "Sorry, full screen not available yet in JOGL... :-(" );

			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice screens[] = ge.getScreenDevices();

			screen = ge.getDefaultScreenDevice();
	
			System.err.println( "** Default screen: "+screen.getIDstring()+" fullscreen available="+screen.isFullScreenSupported() );

			if( ! screen.isFullScreenSupported() )
			{
				for( int i=0; i<screens.length; ++i )
				{
					System.err.println( "** Screen "+i+": "+screens[i].getIDstring()+" fullscreen available="+screens[i].isFullScreenSupported() );

					if( screens[i].isFullScreenSupported() )
					{
						// ??? choose based on what?.
					}
				}
			}

			try
			{
				screen.setFullScreenWindow( frame );
				really_fullscreen = true;
				System.err.println( "FS = " + really_fullscreen );
			}
			catch( Exception e )
			{
				really_fullscreen = false;
			}
		}
	}

// Access

	public GL2 getGl()
	{
		return (GL2)canvas.getGL();
	}

	public GLU getGlu()
	{
		return glu;
	}

	public GLAutoDrawable getDrawable()
	{
		return canvas;
	}

	public Object getComponent()
	{
		return canvas;
	}

	/**
	 * Window (or null if none was requested).
	 */
	public Frame getFrame()
	{
		return frame;
	}

	public int getWidth()
	{
		return canvas.getWidth();
	}

	public int getHeight()
	{
		return canvas.getHeight();
	}

	public int getMemUsed()
	{
		int size = 0;

		// The frame buffer.

		size = format.getRedBits() + format.getGreenBits() + format.getBlueBits() + format.getAlphaBits();

		if( format.isDoubleBuffered() )
			size *= 2;

		size *= canvas.getWidth();
		size *= canvas.getHeight();

		// The Z-buffer.

		size += format.getDepthBits() * canvas.getWidth() * canvas.getHeight();

		return size;
	}

	@Override
	public BufferFormat getFormat()
	{
		return format;
	}

//	protected byte[] dumpBuf = null;
	protected ByteBuffer dumpBuf = null;

	/**
	 * Create a buffered image from the framebuffer. The image width and height is taken
	 * from the framebuffer dimensions.
	 * @param useAlpha Add an alpha channel to the image?.
	 * @return The buffered image.
	 */
	@Override
	@Deprecated
	public BufferedImage getImage( boolean useAlpha )
	{
//long t1 = System.currentTimeMillis();
		GL2 gl = (GL2)canvas.getGL();
		int w = canvas.getWidth();
		int h = canvas.getHeight();
		int s;

		if( useAlpha )
		     s = w * h * 4;			// w * h * RGB.
		else s = w * h * 3;			// w * h * RGB.

		if( dumpBuf == null || dumpBuf.capacity() != s )
		{
			dumpBuf = ByteBuffer.allocateDirect( s );
			dumpBuf.order( ByteOrder.nativeOrder() );
		}
		else
		{
			dumpBuf.rewind();
		}

		gl.glReadBuffer( GL.GL_BACK );

		if( useAlpha )
		     gl.glReadPixels( 0, 0, w, h, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, dumpBuf );
		else gl.glReadPixels( 0, 0, w, h, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, dumpBuf );
//long t2 = System.currentTimeMillis();

		BufferedImage bi;

		if( useAlpha )
		     bi = new BufferedImage( w, h, BufferedImage.TYPE_4BYTE_ABGR );
		else bi = new BufferedImage( w, h, BufferedImage.TYPE_3BYTE_BGR );

		WritableRaster ra = bi.getRaster();

		ra.setDataElements( 0, 0, w, h, dumpBuf );
//long t3 = System.currentTimeMillis();

//System.out.printf( "    buffer dump: GLread=%.4fms setDataElements=%.4fms%n", (t2-t1)/1000.0, (t3-t2)/1000.0 );

		return bi;
	}

	/**
	 * Dump the framebuffer contents to an image in the given filename. This method
	 * calls {@link #getImage(boolean)}.
	 * @param filename The filename of the image to create.
	 * @param format The output format (e.g. "PNG", "png", "JPG", "jpg", "JPEG", "jpeg").
	 * @param useAlpha Add an alpha channel to the image?.
	 * @throws IOException If the writing fails for any I/O error.
	 */
	public void dumpImage( String filename, DumpImageFormat format, boolean useAlpha )
		throws IOException
	{
		BufferedImage bi = getImage( useAlpha );

		File file = new File( filename+"."+format );
	
		switch( format )
		{
			case BMP:
				ImageIO.write( bi, "bmp", file );
				break;
			case JPG:
				ImageIO.write( bi, "jpg", file );
				break;
			case PNG:
				ImageIO.write( bi, "png", file );
				break;
			default:
				throw new RuntimeException( "Something wicked ?" );
		}
	}

	
	protected static final int TARGA_HEADER_SIZE = 18; 
	
	/**
	 * Like {@link #dumpImage(String, DumpImageFormat, boolean)} but really faster, with the restriction
	 * that the format is TARGA, and that the alpha channel cannot be saved.
	 * @param filename The output filename, with ".tga" appended.
	 */
	public void dumpImageFast( String filename )
		throws IOException
	{
		long T1 = System.currentTimeMillis(); 

		GL gl = canvas.getGL();
		int w = canvas.getWidth();
		int h = canvas.getHeight();
	
		// Taken from a post by cwei on the JOGL forums.
		// http://www.javagaming.org/forums/index.php?topic=8747.0
		
		
		RandomAccessFile out        = new RandomAccessFile( filename + ".tga", "rw" );
		FileChannel      ch         = out.getChannel();
		int              fileLength = TARGA_HEADER_SIZE + w * h * 3;
		MappedByteBuffer image      = ch.map( FileChannel.MapMode.READ_WRITE, 0, fileLength );
		
		out.setLength( fileLength );

		// Write the TARGA header.
		
		image.put(  0, (byte) 0 ).put( 1, (byte) 0 );
		image.put(  2, (byte) 2 );				// Uncompressed type.
		image.put( 12, (byte) ( w & 0xFF) );	// Width.
		image.put( 13, (byte) ( w >> 8 ) );		// Width.
		image.put( 14, (byte) ( h & 0xFF ) );	// Height.
		image.put( 15, (byte) ( h >> 8 ) );		// Height.
		image.put( 16, (byte) 24 );				// Pixel size.
		              
		// Go to image data position.
		
		image.position( TARGA_HEADER_SIZE );
		
		// Jogl needs a sliced buffer.
		
		ByteBuffer bgr = image.slice();
		
		// Read the BGR values into the image buffer.
		
		gl.glReadPixels( 0, 0, w, h, GL2.GL_BGR, GL.GL_UNSIGNED_BYTE, bgr );

		// close the file channel

		ch.close();
		
		long T2 = System.currentTimeMillis();
		
		System.err.printf( "dumpImageFast(%d x %d) = %d ms%n", w, h, (T2-T1) );
	}

// Commands

	/**
	 * Set the title of the windows if the frame buffer is windowed.
	 */
	public void setTitle( String title )
	{
		if( frame != null )
		{
			frame.setTitle( title );
		}
	}
	
	@Override
	public void display()
	{
		canvas.display();
	}

	/**
	 * The frame buffer has passively been resized.
	 */
	public void resized()
	{
/*
		Insets insets = frame.getInsets();
		frame.setSize( width + insets.left + insets.right, height + insets.top + insets.bottom );
		frame.validate();
		assert canvas.getWidth() == width   : "Canvas width " + canvas.getWidth() + " differ from exeptected one " + width;
		assert canvas.getHeight() == height : "Canvas height " + canvas.getHeight() + " differ from exeptected one " + height;
*/
		//System.err.println( "("+canvas.getWidth()+"x"+canvas.getHeight()+")" );
//		double mem = getMemUsed();
		//System.err.println( "FrameBuffer: " + mem + "bits (" + (mem/8) + "bytes, " + ((mem/8)/1024) +"Kbytes, " + (((mem/8)/1024)/1024) + "Mbytes)" );
	}

	@Override
	public void setSize( int width, int height )
	{
		// Trying to have a frame with the exact requested displayable size
		// under AWT is a true challenge...

		if( frame != null )
		{
			Insets insets = frame.getInsets();
			frame.setSize( width + insets.left + insets.right, height + insets.top + insets.bottom );
			frame.validate();
		}
		else
		{
			canvas.setSize( width, height );
		}

		assert canvas.getWidth() == width   : "Canvas width " + canvas.getWidth() + " differ from exeptected one " + width;
		assert canvas.getHeight() == height : "Canvas height " + canvas.getHeight() + " differ from exeptected one " + height;

//		double mem = getMemUsed();
		//System.err.println( "FrameBuffer: " + mem + "bits (" + (mem/8) + "bytes, " + ((mem/8)/1024) +"Kbytes, " + (((mem/8)/1024)/1024) + "Mbytes)" );
	}

	/**
	 * Close the frame buffer.
	 */
	public void closeFrame()
	{
		// We cannot close the AWT thread, naturally.

		if( frame != null )
		{
			frame.setVisible( false );

			if( really_fullscreen )
				screen.setFullScreenWindow( null );
		}
		
		for( BufferListener l : listeners )
			l.close( this );
	}

// Events -- WindowListener

	public void windowActivated( WindowEvent e ) 
	{
	}

	public void windowClosed( WindowEvent e ) 
	{
	}

	public void windowClosing( WindowEvent e ) 
	{
		closeFrame();
	}

	public void windowDeactivated( WindowEvent e ) 
	{
	}

	public void windowDeiconified( WindowEvent e ) 
	{
	}

	public void windowIconified( WindowEvent e ) 
	{
	}

	public void windowOpened( WindowEvent e )
	{
	}

// Events -- KeyListener

	public void keyPressed( KeyEvent e )
	{
//		System.err.printf( "KeyPress unicode=%c (%d) keycode=%d%n", (char)toUnicode(e), toUnicode(e), toKeyCode(e) );
		for( BufferListener l : listeners )
			l.key( this, toKeyCode( e ), (char)toUnicode( e ), true );
/*
		int k = toUnicode( e );

		if( k >= 65535 || k < 0 )
		{
			k = toKeyCode( e );

			for( BufferListener l : listeners )
				l.key( this, k, (char)0, true );
		}
		else
		{
			for( BufferListener l : listeners )
				l.key( this, -1, (char)k, true );
		}
*/
	}

	public void keyReleased( KeyEvent e )
	{
//		System.err.printf( "KeyReleased unicode=%c (%d) keycode=%d%n", (char)toUnicode(e), toUnicode(e), toKeyCode(e) );
		for( BufferListener l : listeners )
			l.key( this, toKeyCode( e ), (char)toUnicode( e ), false );
/*		int k = toUnicode( e );

		if( k >= 65535 || k < 0 )
		{
			k = toKeyCode( e );

			for( BufferListener l : listeners )
				l.key( this, k, (char)0, false );
		}
		else
		{
			for( BufferListener l : listeners )
				l.key( this, -1, (char)k, false );
		}
*/	}

	public void keyTyped( KeyEvent e )
	{
//		System.err.printf( "KeyTyped unicode=%c (%d)%n", e.getKeyChar(), (int) e.getKeyChar() );
//		int k = toUnicode( e );
//
		if( ! Character.isIdentifierIgnorable( e.getKeyChar() ) )
		{
			for( BufferListener l : listeners )
				l.keyTyped( this, e.getKeyChar(), e.getModifiersEx() );
		}
	}

// Events -- MouseListener

	public void mouseClicked( MouseEvent e )
	{
	} 

	public void mouseEntered( MouseEvent e )
	{
	} 

	public void mouseExited( MouseEvent e )
	{
	} 
	
	public void mousePressed( MouseEvent e )
	{
		button = e.getButton();

		for( BufferListener l: listeners )
			l.mouse( this, e.getX(), getHeight() - e.getY(), button );
/*
		int    h = ctx.get_buffer().get_height();
		Point3 p = new Point3( e.getX(), h - e.getY(), 0 );

		button_mask |= ( 1 << ( e.getButton() - 1 ) );

		peer.sense( Environment.MSG_SYS_MOUSE, peer.get_id(), p, button_mask );
		cursor.move_to( p );
*/
	} 

	public void mouseReleased( MouseEvent e )
	{
		button = -e.getButton();

		for( BufferListener l: listeners )
			l.mouse( this, e.getX(), getHeight() - e.getY(), button );
/*
		int    h = ctx.get_buffer().get_height();
		Point3 p = new Point3( e.getX(), h - e.getY(), 0 );

		button_mask &= ~( 1 << ( e.getButton() - 1 ) );

		peer.sense( Environment.MSG_SYS_MOUSE, peer.get_id(), p, button_mask );
		cursor.move_to( p );
*/
	} 

// Events -- MouseMotionListener

	public void mouseDragged( MouseEvent e )
	{
		for( BufferListener l: listeners )
			l.mouse( this, e.getX(), getHeight() - e.getY(), button );
/*
		int    h = ctx.get_buffer().get_height();
		Point3 p = new Point3( e.getX(), h - e.getY(), 0 );

		peer.sense( Environment.MSG_SYS_MOUSE, peer.get_id(), p, button_mask );
		cursor.move_to( p );
*/
	} 

	public void mouseMoved( MouseEvent e )
	{
		for( BufferListener l: listeners )
			l.mouse( this, e.getX(), getHeight() - e.getY(), 0 );
/*
		int    h = ctx.get_buffer().get_height();
		Point3 p = new Point3( e.getX(), h - e.getY(), 0 );

		peer.sense( Environment.MSG_SYS_MOUSE, peer.get_id(), p, button_mask );
		cursor.move_to( p );
*/
	} 

// Events -- MouseWheelListener

	public void mouseWheelMoved( MouseWheelEvent e )
	{
		int r = e.getWheelRotation();

		if( r > 0 )
		{
			for( BufferListener l: listeners )
				l.mouse( this, e.getX(), getHeight() - e.getY(), 4 );
		}
		else if( r < 0 )
		{
			for( BufferListener l: listeners )
				l.mouse( this, e.getX(), getHeight() - e.getY(), 5 );
		}
/*
		int    h = ctx.get_buffer().get_height();
		Point3 p = new Point3( e.getX(), h - e.getY(), 0 );

		int r = e.getWheelRotation();

		if( r > 0 )
		{
			for( int i=0; i<r; ++i )
				peer.sense( Environment.MSG_SYS_MOUSE, peer.get_id(), p, button_mask | ( 1 << 3 ) );
		}
		else if( r < 0 )
		{
			r = -r;

			for( int i=0; i<r; ++i )
				peer.sense( Environment.MSG_SYS_MOUSE, peer.get_id(), p, button_mask | ( 1 << 4 ) );
		}

		cursor.move_to( p );
*/
	} 

// Utilities

	protected int toKeyCode( KeyEvent e )
	{
		int keycode = e.getKeyCode();

		if( keycode >= 0 && keycode <= 523 )
		     return FROM_SWING_KEY[keycode];
		else if( keycode >= 61440 && keycode <= 61451 )
		     return FROM_SWING_KEY2[keycode];
//		else if( keycode == 65406 )
//		     return Keys.KEY_ALT_GRAPH;
		else return Keys.KEY_UNDEFINED; 
	}

	protected int toUnicode( KeyEvent e )
	{
		if( e.getModifiers() == 0 )
		{
			return e.getKeyChar();
		}
		else
		{
			return e.getKeyCode();		
		}

		//int keyChar = e.getKeyChar();
		//return keyChar;
	}

	protected static final int FROM_SWING_KEY[] =
	{
		Keys.KEY_UNDEFINED,				// 0 VK_UNDEFINED
		Keys.KEY_UNDEFINED,				// 1
		Keys.KEY_UNDEFINED,				// 2
		Keys.KEY_UNDEFINED,				// 3 VK_CANCEL
		Keys.KEY_UNDEFINED,				// 4
		Keys.KEY_UNDEFINED,				// 5
		Keys.KEY_UNDEFINED,				// 6
		Keys.KEY_UNDEFINED,				// 7
		Keys.KEY_BACK_SPACE,			// 8 VK_BACK_SPACE
		Keys.KEY_UNDEFINED,				// 9 VK_TAB
		Keys.KEY_UNDEFINED,				// 10 VK_ENTER
		Keys.KEY_UNDEFINED,				// 11
		Keys.KEY_UNDEFINED,				// 12 VK_CLEAR
		Keys.KEY_UNDEFINED,				// 13
		Keys.KEY_UNDEFINED,				// 14
		Keys.KEY_UNDEFINED,				// 15
		Keys.KEY_SHIFT,					// 16 VK_SHIFT
		Keys.KEY_CONTROL,				// 17 VK_CONTROL
		Keys.KEY_ALT,					// 18 VK_ALT
		Keys.KEY_PAUSE,					// 19 VK_PAUSE
		Keys.KEY_CAPS_LOCK,				// 20 VK_CAPS_LOCK
		Keys.KEY_UNDEFINED,				// 21 VK_KANA
		Keys.KEY_UNDEFINED,				// 22
		Keys.KEY_UNDEFINED,				// 23
		Keys.KEY_UNDEFINED,				// 24 VK_FINAL
		Keys.KEY_UNDEFINED,				// 25 VK_KANJI
		Keys.KEY_UNDEFINED,				// 26
		Keys.KEY_ESCAPE,				// 27 VK_ESCAPE
		Keys.KEY_UNDEFINED,				// 28 VK_CONVERT
		Keys.KEY_UNDEFINED,				// 29 VK_NONCONVERT
		Keys.KEY_UNDEFINED,				// 30 VK_ACCEPT
		Keys.KEY_UNDEFINED,				// 31 VK_MODECHANGE
		Keys.KEY_UNDEFINED,				// 32 VK_SPACE
		Keys.KEY_PAGE_DOWN,				// 33 VK_PAGE_UP
		Keys.KEY_PAGE_UP,				// 34 VK_PAGE_DOWN
		Keys.KEY_END,					// 35 VK_END
		Keys.KEY_HOME,					// 36 VK_HOME
		Keys.KEY_LEFT,					// 37 VK_LEFT
		Keys.KEY_UP,					// 38 VK_UP
		Keys.KEY_RIGHT,					// 39 VK_RIGHT
		Keys.KEY_DOWN,					// 40 VK_DOWN
		Keys.KEY_UNDEFINED,				// 41
		Keys.KEY_UNDEFINED,				// 42
		Keys.KEY_UNDEFINED,				// 43
		Keys.KEY_UNDEFINED,				// 44 VK_COMMA
		Keys.KEY_UNDEFINED,				// 45 VK_MINUS
		Keys.KEY_UNDEFINED,				// 46 VK_PERIOD
		Keys.KEY_UNDEFINED,				// 47 VK_SLASH
		Keys.KEY_UNDEFINED,				// 48 VK_0
		Keys.KEY_UNDEFINED,				// 49 VK_1
		Keys.KEY_UNDEFINED,				// 50 VK_2
		Keys.KEY_UNDEFINED,				// 51 VK_3
		Keys.KEY_UNDEFINED,				// 52 VK_4
		Keys.KEY_UNDEFINED,				// 53 VK_5
		Keys.KEY_UNDEFINED,				// 54 VK_6
		Keys.KEY_UNDEFINED,				// 55 VK_7
		Keys.KEY_UNDEFINED,				// 56 VK_8
		Keys.KEY_UNDEFINED,				// 57 VK_9
		Keys.KEY_UNDEFINED,				// 58
		Keys.KEY_UNDEFINED,				// 59 VK_SEMICOLON
		Keys.KEY_UNDEFINED,				// 60
		Keys.KEY_UNDEFINED,				// 61 VK_EQUALS
		Keys.KEY_UNDEFINED,				// 62
		Keys.KEY_UNDEFINED,				// 63
		Keys.KEY_UNDEFINED,				// 64
		Keys.KEY_UNDEFINED,				// 65 VK_A
		Keys.KEY_UNDEFINED,				// 66 VK_B
		Keys.KEY_UNDEFINED,				// 67 VK_C
		Keys.KEY_UNDEFINED,				// 68 VK_D
		Keys.KEY_UNDEFINED,				// 69 VK_E
		Keys.KEY_UNDEFINED,				// 70 VK_F
		Keys.KEY_UNDEFINED,				// 71 VK_G
		Keys.KEY_UNDEFINED,				// 72 VK_H
		Keys.KEY_UNDEFINED,				// 73 VK_I
		Keys.KEY_UNDEFINED,				// 74 VK_J
		Keys.KEY_UNDEFINED,				// 75 VK_K
		Keys.KEY_UNDEFINED,				// 76 VK_L
		Keys.KEY_UNDEFINED,				// 77 VK_M
		Keys.KEY_UNDEFINED,				// 78 VK_N
		Keys.KEY_UNDEFINED,				// 79 VK_O
		Keys.KEY_UNDEFINED,				// 80 VK_P
		Keys.KEY_UNDEFINED,				// 81 VK_Q
		Keys.KEY_UNDEFINED,				// 82 VK_R
		Keys.KEY_UNDEFINED,				// 83 VK_S
		Keys.KEY_UNDEFINED,				// 84 VK_T
		Keys.KEY_UNDEFINED,				// 85 VK_U
		Keys.KEY_UNDEFINED,				// 86 VK_V
		Keys.KEY_UNDEFINED,				// 87 VK_W
		Keys.KEY_UNDEFINED,				// 88 VK_X
		Keys.KEY_UNDEFINED,				// 89 VK_Y
		Keys.KEY_UNDEFINED,				// 90 VK_Z
		Keys.KEY_UNDEFINED,				// 91 VK_OPEN_BRACKET
		Keys.KEY_UNDEFINED,				// 92 VK_BACK_SLASH
		Keys.KEY_UNDEFINED,				// 93 VK_CLOSE_BRACKET
		Keys.KEY_UNDEFINED,				// 94
		Keys.KEY_UNDEFINED,				// 95
		Keys.KEY_UNDEFINED,				// 96 VK_NUMPAD0
		Keys.KEY_UNDEFINED,				// 97 VK_NUMPAD1
		Keys.KEY_UNDEFINED,				// 98 VK_NUMPAD2
		Keys.KEY_UNDEFINED,				// 99 VK_NUMPAD3
		Keys.KEY_UNDEFINED,				// 100 VK_NUMPAD4
		Keys.KEY_UNDEFINED,				// 101 VK_NUMPAD5
		Keys.KEY_UNDEFINED,				// 102 VK_NUMPAD6
		Keys.KEY_UNDEFINED,				// 103 VK_NUMPAD7
		Keys.KEY_UNDEFINED,				// 104 VK_NUMPAD8
		Keys.KEY_UNDEFINED,				// 105 VK_NUMPAD9
		Keys.KEY_UNDEFINED,				// 106 VK_MULTIPLY
		Keys.KEY_UNDEFINED,				// 107 VK_ADD
		Keys.KEY_UNDEFINED,				// 108 VK_SEPARATOR
		Keys.KEY_UNDEFINED,				// 109 VK_SUBTRACT
		Keys.KEY_UNDEFINED,				// 110 VK_DECIMAL
		Keys.KEY_UNDEFINED,				// 111 VK_DIVIDE
		Keys.KEY_F1,					// 112 VK_F1
		Keys.KEY_F2,					// 113 VK_F2
		Keys.KEY_F3,					// 114 VK_F3
		Keys.KEY_F4,					// 115 VK_F4
		Keys.KEY_F5,					// 116 VK_F5
		Keys.KEY_F6,					// 117 VK_F6
		Keys.KEY_F7,					// 118 VK_F7
		Keys.KEY_F8,					// 119 VK_F8
		Keys.KEY_F9,					// 120 VK_F9
		Keys.KEY_F10,					// 121 VK_F10
		Keys.KEY_F11,					// 122 VK_F11
		Keys.KEY_F12,					// 123 VK_F12
		Keys.KEY_UNDEFINED,				// 124
		Keys.KEY_UNDEFINED,				// 125
		Keys.KEY_UNDEFINED,				// 126
		Keys.KEY_DELETE,				// 127 VK_DELETE
		Keys.KEY_UNDEFINED,				// 128 VK_DEAD_GRAVE
		Keys.KEY_UNDEFINED,				// 129 VK_DEAD_ACUTE
		Keys.KEY_UNDEFINED,				// 130 VK_DEAD_CIRCUMFLEX
		Keys.KEY_UNDEFINED,				// 131 VK_DEAD_TILDE
		Keys.KEY_UNDEFINED,				// 132 VK_DEAD_MACRON
		Keys.KEY_UNDEFINED,				// 133 VK_DEAD_BREVE
		Keys.KEY_UNDEFINED,				// 134 VK_DEAD_ABOVEDOT
		Keys.KEY_UNDEFINED,				// 135 VK_DEAD_DIAERESIS
		Keys.KEY_UNDEFINED,				// 136 VK_DEAD_ABOVERING
		Keys.KEY_UNDEFINED,				// 137 VK_DEAD_DOUBLEACUTE
		Keys.KEY_UNDEFINED,				// 138 VK_DEAD_CARON
		Keys.KEY_UNDEFINED,				// 139 VK_DEAD_CEDILLA
		Keys.KEY_UNDEFINED,				// 140 VK_DEAD_OGONEK
		Keys.KEY_UNDEFINED,				// 141 VK_DEAD_IOTA
		Keys.KEY_UNDEFINED,				// 142 VK_DEAD_VOICED_SOUND
		Keys.KEY_UNDEFINED,				// 143 VK_DEAD_SEMIVOICED_SOUND
		Keys.KEY_NUM_LOCK,				// 144 VK_NUM_LOCK
		Keys.KEY_SCROLL_LOCK,			// 145 VK_SCROLL_LOCK
		Keys.KEY_UNDEFINED,				// 146
		Keys.KEY_UNDEFINED,				// 147
		Keys.KEY_UNDEFINED,				// 148
		Keys.KEY_UNDEFINED,				// 149
		Keys.KEY_UNDEFINED,				// 150 VK_AMPERSAND
		Keys.KEY_UNDEFINED,				// 151 VK_ASTERISK
		Keys.KEY_UNDEFINED,				// 152 VK_QUOTEDBL
		Keys.KEY_UNDEFINED,				// 153 VK_LESS
		Keys.KEY_PRINT_SCREEN,			// 154 VK_PRINTSCREEN
		Keys.KEY_INSERT,				// 155 VK_INSERT
		Keys.KEY_HELP,					// 156 VK_HELP
		Keys.KEY_META,					// 157 VK_META
		Keys.KEY_UNDEFINED,				// 158
		Keys.KEY_UNDEFINED,				// 159
		Keys.KEY_UNDEFINED,				// 160 VK_GREATER
		Keys.KEY_UNDEFINED,				// 161 VK_BRACELEFT
		Keys.KEY_UNDEFINED,				// 162 VK_BRACERIGHT
		Keys.KEY_UNDEFINED,				// 163
		Keys.KEY_UNDEFINED,				// 164
		Keys.KEY_UNDEFINED,				// 165
		Keys.KEY_UNDEFINED,				// 166
		Keys.KEY_UNDEFINED,				// 167
		Keys.KEY_UNDEFINED,				// 168
		Keys.KEY_UNDEFINED,				// 169
		Keys.KEY_UNDEFINED,				// 170
		Keys.KEY_UNDEFINED,				// 171
		Keys.KEY_UNDEFINED,				// 172
		Keys.KEY_UNDEFINED,				// 173
		Keys.KEY_UNDEFINED,				// 174
		Keys.KEY_UNDEFINED,				// 175
		Keys.KEY_UNDEFINED,				// 176
		Keys.KEY_UNDEFINED,				// 177
		Keys.KEY_UNDEFINED,				// 178
		Keys.KEY_UNDEFINED,				// 179
		Keys.KEY_UNDEFINED,				// 180
		Keys.KEY_UNDEFINED,				// 181
		Keys.KEY_UNDEFINED,				// 182
		Keys.KEY_UNDEFINED,				// 183
		Keys.KEY_UNDEFINED,				// 184
		Keys.KEY_UNDEFINED,				// 185
		Keys.KEY_UNDEFINED,				// 186
		Keys.KEY_UNDEFINED,				// 187
		Keys.KEY_UNDEFINED,				// 188
		Keys.KEY_UNDEFINED,				// 189
		Keys.KEY_UNDEFINED,				// 190
		Keys.KEY_UNDEFINED,				// 191
		Keys.KEY_UNDEFINED,				// 192 VK_BACK_QUOTE
		Keys.KEY_UNDEFINED,				// 193
		Keys.KEY_UNDEFINED,				// 194
		Keys.KEY_UNDEFINED,				// 195
		Keys.KEY_UNDEFINED,				// 196
		Keys.KEY_UNDEFINED,				// 197
		Keys.KEY_UNDEFINED,				// 198
		Keys.KEY_UNDEFINED,				// 199
		Keys.KEY_UNDEFINED,				// 200
		Keys.KEY_UNDEFINED,				// 201
		Keys.KEY_UNDEFINED,				// 202
		Keys.KEY_UNDEFINED,				// 203
		Keys.KEY_UNDEFINED,				// 204
		Keys.KEY_UNDEFINED,				// 205
		Keys.KEY_UNDEFINED,				// 206
		Keys.KEY_UNDEFINED,				// 207
		Keys.KEY_UNDEFINED,				// 208
		Keys.KEY_UNDEFINED,				// 209
		Keys.KEY_UNDEFINED,				// 210
		Keys.KEY_UNDEFINED,				// 211
		Keys.KEY_UNDEFINED,				// 212
		Keys.KEY_UNDEFINED,				// 213
		Keys.KEY_UNDEFINED,				// 214
		Keys.KEY_UNDEFINED,				// 215
		Keys.KEY_UNDEFINED,				// 216
		Keys.KEY_UNDEFINED,				// 217
		Keys.KEY_UNDEFINED,				// 218
		Keys.KEY_UNDEFINED,				// 219
		Keys.KEY_UNDEFINED,				// 220
		Keys.KEY_UNDEFINED,				// 221
		Keys.KEY_UNDEFINED,				// 222 VK_QUOTE
		Keys.KEY_UNDEFINED,				// 223
		Keys.KEY_UNDEFINED,				// 224 VK_KP_UP
		Keys.KEY_UNDEFINED,				// 225 VK_KP_DOWN
		Keys.KEY_UNDEFINED,				// 226 VK_KP_LEFT
		Keys.KEY_UNDEFINED,				// 227 VK_KP_RIGHT
		Keys.KEY_UNDEFINED,				// 228
		Keys.KEY_UNDEFINED,				// 229
		Keys.KEY_UNDEFINED,				// 230
		Keys.KEY_UNDEFINED,				// 231
		Keys.KEY_UNDEFINED,				// 232
		Keys.KEY_UNDEFINED,				// 233
		Keys.KEY_UNDEFINED,				// 234
		Keys.KEY_UNDEFINED,				// 235
		Keys.KEY_UNDEFINED,				// 236
		Keys.KEY_UNDEFINED,				// 237
		Keys.KEY_UNDEFINED,				// 238
		Keys.KEY_UNDEFINED,				// 239
		Keys.KEY_UNDEFINED,				// 240 VK_ALPHANUMERIC
		Keys.KEY_UNDEFINED,				// 241 VK_KATAKANA
		Keys.KEY_UNDEFINED,				// 242 VK_HIRAGANA
		Keys.KEY_UNDEFINED,				// 243 VK_FULL_WIDTH
		Keys.KEY_UNDEFINED,				// 244 VK_HALF_WIDTH
		Keys.KEY_UNDEFINED,				// 245 VK_ROMAN_CHARACTERS
		Keys.KEY_UNDEFINED,				// 246
		Keys.KEY_UNDEFINED,				// 247
		Keys.KEY_UNDEFINED,				// 248
		Keys.KEY_UNDEFINED,				// 249
		Keys.KEY_UNDEFINED,				// 250
		Keys.KEY_UNDEFINED,				// 251
		Keys.KEY_UNDEFINED,				// 252
		Keys.KEY_UNDEFINED,				// 253
		Keys.KEY_UNDEFINED,				// 254
		Keys.KEY_UNDEFINED,				// 255
		Keys.KEY_UNDEFINED,				// 256 VK_ALL_CANDIDATES
		Keys.KEY_UNDEFINED,				// 257 VK_PREVIOUS_CANDIDATE
		Keys.KEY_UNDEFINED,				// 258 VK_CODE_INPUT
		Keys.KEY_UNDEFINED,				// 259 VK_JAPANESE_KATAKANA
		Keys.KEY_UNDEFINED,				// 260 VK_JAPANESE_HIRAGANA
		Keys.KEY_UNDEFINED,				// 261 VK_JAPANESE_ROMAN
		Keys.KEY_UNDEFINED,				// 262 VK_KANA_LOCK
		Keys.KEY_UNDEFINED,				// 263 VK_INPUT_METHOD_ON_OFF
		Keys.KEY_UNDEFINED,				// 264
		Keys.KEY_UNDEFINED,				// 265
		Keys.KEY_UNDEFINED,				// 266
		Keys.KEY_UNDEFINED,				// 267
		Keys.KEY_UNDEFINED,				// 268
		Keys.KEY_UNDEFINED,				// 269
		Keys.KEY_UNDEFINED,				// 270
		Keys.KEY_UNDEFINED,				// 271
		Keys.KEY_UNDEFINED,				// 272
		Keys.KEY_UNDEFINED,				// 273
		Keys.KEY_UNDEFINED,				// 274
		Keys.KEY_UNDEFINED,				// 275
		Keys.KEY_UNDEFINED,				// 276
		Keys.KEY_UNDEFINED,				// 277
		Keys.KEY_UNDEFINED,				// 278
		Keys.KEY_UNDEFINED,				// 279
		Keys.KEY_UNDEFINED,				// 280
		Keys.KEY_UNDEFINED,				// 281
		Keys.KEY_UNDEFINED,				// 282
		Keys.KEY_UNDEFINED,				// 283
		Keys.KEY_UNDEFINED,				// 284
		Keys.KEY_UNDEFINED,				// 285
		Keys.KEY_UNDEFINED,				// 286
		Keys.KEY_UNDEFINED,				// 287
		Keys.KEY_UNDEFINED,				// 288
		Keys.KEY_UNDEFINED,				// 289
		Keys.KEY_UNDEFINED,				// 290
		Keys.KEY_UNDEFINED,				// 291
		Keys.KEY_UNDEFINED,				// 292
		Keys.KEY_UNDEFINED,				// 293
		Keys.KEY_UNDEFINED,				// 294
		Keys.KEY_UNDEFINED,				// 295
		Keys.KEY_UNDEFINED,				// 296
		Keys.KEY_UNDEFINED,				// 297
		Keys.KEY_UNDEFINED,				// 298
		Keys.KEY_UNDEFINED,				// 299
		Keys.KEY_UNDEFINED,				// 300
		Keys.KEY_UNDEFINED,				// 301
		Keys.KEY_UNDEFINED,				// 302
		Keys.KEY_UNDEFINED,				// 303
		Keys.KEY_UNDEFINED,				// 304
		Keys.KEY_UNDEFINED,				// 305
		Keys.KEY_UNDEFINED,				// 306
		Keys.KEY_UNDEFINED,				// 307
		Keys.KEY_UNDEFINED,				// 308
		Keys.KEY_UNDEFINED,				// 309
		Keys.KEY_UNDEFINED,				// 310
		Keys.KEY_UNDEFINED,				// 311
		Keys.KEY_UNDEFINED,				// 312
		Keys.KEY_UNDEFINED,				// 313
		Keys.KEY_UNDEFINED,				// 314
		Keys.KEY_UNDEFINED,				// 315
		Keys.KEY_UNDEFINED,				// 316
		Keys.KEY_UNDEFINED,				// 317
		Keys.KEY_UNDEFINED,				// 318
		Keys.KEY_UNDEFINED,				// 319
		Keys.KEY_UNDEFINED,				// 320
		Keys.KEY_UNDEFINED,				// 321
		Keys.KEY_UNDEFINED,				// 322
		Keys.KEY_UNDEFINED,				// 323
		Keys.KEY_UNDEFINED,				// 324
		Keys.KEY_UNDEFINED,				// 325
		Keys.KEY_UNDEFINED,				// 326
		Keys.KEY_UNDEFINED,				// 327
		Keys.KEY_UNDEFINED,				// 328
		Keys.KEY_UNDEFINED,				// 329
		Keys.KEY_UNDEFINED,				// 330
		Keys.KEY_UNDEFINED,				// 331
		Keys.KEY_UNDEFINED,				// 332
		Keys.KEY_UNDEFINED,				// 333
		Keys.KEY_UNDEFINED,				// 334
		Keys.KEY_UNDEFINED,				// 335
		Keys.KEY_UNDEFINED,				// 336
		Keys.KEY_UNDEFINED,				// 337
		Keys.KEY_UNDEFINED,				// 338
		Keys.KEY_UNDEFINED,				// 339
		Keys.KEY_UNDEFINED,				// 340
		Keys.KEY_UNDEFINED,				// 341
		Keys.KEY_UNDEFINED,				// 342
		Keys.KEY_UNDEFINED,				// 343
		Keys.KEY_UNDEFINED,				// 344
		Keys.KEY_UNDEFINED,				// 345
		Keys.KEY_UNDEFINED,				// 346
		Keys.KEY_UNDEFINED,				// 347
		Keys.KEY_UNDEFINED,				// 348
		Keys.KEY_UNDEFINED,				// 349
		Keys.KEY_UNDEFINED,				// 350
		Keys.KEY_UNDEFINED,				// 351
		Keys.KEY_UNDEFINED,				// 352
		Keys.KEY_UNDEFINED,				// 353
		Keys.KEY_UNDEFINED,				// 354
		Keys.KEY_UNDEFINED,				// 355
		Keys.KEY_UNDEFINED,				// 356
		Keys.KEY_UNDEFINED,				// 357
		Keys.KEY_UNDEFINED,				// 358
		Keys.KEY_UNDEFINED,				// 359
		Keys.KEY_UNDEFINED,				// 360
		Keys.KEY_UNDEFINED,				// 361
		Keys.KEY_UNDEFINED,				// 362
		Keys.KEY_UNDEFINED,				// 363
		Keys.KEY_UNDEFINED,				// 364
		Keys.KEY_UNDEFINED,				// 365
		Keys.KEY_UNDEFINED,				// 366
		Keys.KEY_UNDEFINED,				// 367
		Keys.KEY_UNDEFINED,				// 368
		Keys.KEY_UNDEFINED,				// 369
		Keys.KEY_UNDEFINED,				// 370
		Keys.KEY_UNDEFINED,				// 371
		Keys.KEY_UNDEFINED,				// 372
		Keys.KEY_UNDEFINED,				// 373
		Keys.KEY_UNDEFINED,				// 374
		Keys.KEY_UNDEFINED,				// 375
		Keys.KEY_UNDEFINED,				// 376
		Keys.KEY_UNDEFINED,				// 377
		Keys.KEY_UNDEFINED,				// 378
		Keys.KEY_UNDEFINED,				// 379
		Keys.KEY_UNDEFINED,				// 380
		Keys.KEY_UNDEFINED,				// 381
		Keys.KEY_UNDEFINED,				// 382
		Keys.KEY_UNDEFINED,				// 383
		Keys.KEY_UNDEFINED,				// 384
		Keys.KEY_UNDEFINED,				// 385
		Keys.KEY_UNDEFINED,				// 386
		Keys.KEY_UNDEFINED,				// 387
		Keys.KEY_UNDEFINED,				// 388
		Keys.KEY_UNDEFINED,				// 389
		Keys.KEY_UNDEFINED,				// 390
		Keys.KEY_UNDEFINED,				// 391
		Keys.KEY_UNDEFINED,				// 392
		Keys.KEY_UNDEFINED,				// 393
		Keys.KEY_UNDEFINED,				// 394
		Keys.KEY_UNDEFINED,				// 395
		Keys.KEY_UNDEFINED,				// 396
		Keys.KEY_UNDEFINED,				// 397
		Keys.KEY_UNDEFINED,				// 398
		Keys.KEY_UNDEFINED,				// 399
		Keys.KEY_UNDEFINED,				// 400
		Keys.KEY_UNDEFINED,				// 401
		Keys.KEY_UNDEFINED,				// 402
		Keys.KEY_UNDEFINED,				// 403
		Keys.KEY_UNDEFINED,				// 404
		Keys.KEY_UNDEFINED,				// 405
		Keys.KEY_UNDEFINED,				// 406
		Keys.KEY_UNDEFINED,				// 407
		Keys.KEY_UNDEFINED,				// 408
		Keys.KEY_UNDEFINED,				// 409
		Keys.KEY_UNDEFINED,				// 410
		Keys.KEY_UNDEFINED,				// 411
		Keys.KEY_UNDEFINED,				// 412
		Keys.KEY_UNDEFINED,				// 413
		Keys.KEY_UNDEFINED,				// 414
		Keys.KEY_UNDEFINED,				// 415
		Keys.KEY_UNDEFINED,				// 416
		Keys.KEY_UNDEFINED,				// 417
		Keys.KEY_UNDEFINED,				// 418
		Keys.KEY_UNDEFINED,				// 419
		Keys.KEY_UNDEFINED,				// 420
		Keys.KEY_UNDEFINED,				// 421
		Keys.KEY_UNDEFINED,				// 422
		Keys.KEY_UNDEFINED,				// 423
		Keys.KEY_UNDEFINED,				// 424
		Keys.KEY_UNDEFINED,				// 425
		Keys.KEY_UNDEFINED,				// 426
		Keys.KEY_UNDEFINED,				// 427
		Keys.KEY_UNDEFINED,				// 428
		Keys.KEY_UNDEFINED,				// 429
		Keys.KEY_UNDEFINED,				// 430
		Keys.KEY_UNDEFINED,				// 431
		Keys.KEY_UNDEFINED,				// 432
		Keys.KEY_UNDEFINED,				// 433
		Keys.KEY_UNDEFINED,				// 434
		Keys.KEY_UNDEFINED,				// 435
		Keys.KEY_UNDEFINED,				// 436
		Keys.KEY_UNDEFINED,				// 437
		Keys.KEY_UNDEFINED,				// 438
		Keys.KEY_UNDEFINED,				// 439
		Keys.KEY_UNDEFINED,				// 440
		Keys.KEY_UNDEFINED,				// 441
		Keys.KEY_UNDEFINED,				// 442
		Keys.KEY_UNDEFINED,				// 443
		Keys.KEY_UNDEFINED,				// 444
		Keys.KEY_UNDEFINED,				// 445
		Keys.KEY_UNDEFINED,				// 446
		Keys.KEY_UNDEFINED,				// 447
		Keys.KEY_UNDEFINED,				// 448
		Keys.KEY_UNDEFINED,				// 449
		Keys.KEY_UNDEFINED,				// 450
		Keys.KEY_UNDEFINED,				// 451
		Keys.KEY_UNDEFINED,				// 452
		Keys.KEY_UNDEFINED,				// 453
		Keys.KEY_UNDEFINED,				// 454
		Keys.KEY_UNDEFINED,				// 455
		Keys.KEY_UNDEFINED,				// 456
		Keys.KEY_UNDEFINED,				// 457
		Keys.KEY_UNDEFINED,				// 458
		Keys.KEY_UNDEFINED,				// 459
		Keys.KEY_UNDEFINED,				// 460
		Keys.KEY_UNDEFINED,				// 461
		Keys.KEY_UNDEFINED,				// 462
		Keys.KEY_UNDEFINED,				// 463
		Keys.KEY_UNDEFINED,				// 464
		Keys.KEY_UNDEFINED,				// 465
		Keys.KEY_UNDEFINED,				// 466
		Keys.KEY_UNDEFINED,				// 467
		Keys.KEY_UNDEFINED,				// 468
		Keys.KEY_UNDEFINED,				// 469
		Keys.KEY_UNDEFINED,				// 470
		Keys.KEY_UNDEFINED,				// 471
		Keys.KEY_UNDEFINED,				// 472
		Keys.KEY_UNDEFINED,				// 473
		Keys.KEY_UNDEFINED,				// 474
		Keys.KEY_UNDEFINED,				// 475
		Keys.KEY_UNDEFINED,				// 476
		Keys.KEY_UNDEFINED,				// 477
		Keys.KEY_UNDEFINED,				// 478
		Keys.KEY_UNDEFINED,				// 479
		Keys.KEY_UNDEFINED,				// 480
		Keys.KEY_UNDEFINED,				// 481
		Keys.KEY_UNDEFINED,				// 482
		Keys.KEY_UNDEFINED,				// 483
		Keys.KEY_UNDEFINED,				// 484
		Keys.KEY_UNDEFINED,				// 485
		Keys.KEY_UNDEFINED,				// 486
		Keys.KEY_UNDEFINED,				// 487
		Keys.KEY_UNDEFINED,				// 488
		Keys.KEY_UNDEFINED,				// 489
		Keys.KEY_UNDEFINED,				// 490
		Keys.KEY_UNDEFINED,				// 491
		Keys.KEY_UNDEFINED,				// 492
		Keys.KEY_UNDEFINED,				// 493
		Keys.KEY_UNDEFINED,				// 494
		Keys.KEY_UNDEFINED,				// 495
		Keys.KEY_UNDEFINED,				// 496
		Keys.KEY_UNDEFINED,				// 497
		Keys.KEY_UNDEFINED,				// 498
		Keys.KEY_UNDEFINED,				// 499
		Keys.KEY_UNDEFINED,				// 500
		Keys.KEY_UNDEFINED,				// 501
		Keys.KEY_UNDEFINED,				// 502
		Keys.KEY_UNDEFINED,				// 503
		Keys.KEY_UNDEFINED,				// 504
		Keys.KEY_UNDEFINED,				// 505
		Keys.KEY_UNDEFINED,				// 506
		Keys.KEY_UNDEFINED,				// 507
		Keys.KEY_UNDEFINED,				// 508
		Keys.KEY_UNDEFINED,				// 509
		Keys.KEY_UNDEFINED,				// 510
		Keys.KEY_UNDEFINED,				// 511
		Keys.KEY_UNDEFINED,				// 512 VK_AT
		Keys.KEY_UNDEFINED,				// 513 VK_COLON
		Keys.KEY_UNDEFINED,				// 514 VK_CIRCUMFLEX
		Keys.KEY_UNDEFINED,				// 515 VK_DOLLAR
		Keys.KEY_UNDEFINED,				// 516 VK_EURO_SIGN
		Keys.KEY_UNDEFINED,				// 517 VK_EXCLAMATION_MARK
		Keys.KEY_UNDEFINED,				// 518 VK_INVERTED_EXCLAMATION_MARK
		Keys.KEY_UNDEFINED,				// 519 VK_LEFT_PARENTHESIS
		Keys.KEY_UNDEFINED,				// 520 VK_NUMBER_SIGN
		Keys.KEY_UNDEFINED,				// 521 VK_PLUS
		Keys.KEY_UNDEFINED,				// 522 VK_RIGHT_PARENTHESIS
		Keys.KEY_UNDEFINED				// 523 VK_UNDERSCORE
	};

	protected static final int FROM_SWING_KEY2[] = 
	{
		Keys.KEY_F13,	// 61440 VK_F13
		Keys.KEY_F14,	// 61441 VK_F14
		Keys.KEY_F15,	// 61442 VK_F15
		Keys.KEY_F16,	// 61443 VK_F16
		Keys.KEY_F17,	// 61444 VK_F17
		Keys.KEY_F18,	// 61445 VK_F18
		Keys.KEY_F19,	// 61446 VK_F19
		Keys.KEY_F20,	// 61447 VK_F20
		Keys.KEY_F21,	// 61448 VK_F21
		Keys.KEY_F22,	// 61449 VK_F22
		Keys.KEY_F23,	// 61450 VK_F23
		Keys.KEY_F24	// 61451 VK_F24
	};


// Nested classes

public static class SimpleFrame extends Frame
{
	private static final long serialVersionUID = -9202886088488103176L;
	protected boolean ignoreRepaint = true;

	public SimpleFrame( String title, boolean ignoreRepaint )
	{
		super( title );
		this.ignoreRepaint = ignoreRepaint;
		setIgnoreRepaint( ignoreRepaint );
		setForeground( Color.WHITE );
	}

	@Override
	public void update( Graphics g )
	{
		if( ! ignoreRepaint )
			super.update( g );
	}

	@Override
	public void paint( Graphics g )
	{
		if( ! ignoreRepaint )
			super.paint( g );
	}
}

/**
 * Allow to ignore automatic redrawing done by the AWT paint(Graphics) method.
 *
 * @author Antoine Dutot
 * @since 20060507
 */
public static class MyGLCanvas extends GLCanvas
{
	private static final long serialVersionUID = -7295588206286301736L;
	protected boolean ignoreRepaint = true;

	public MyGLCanvas( GLCapabilities cap, boolean ignoreRepaint )
	{
		super( cap );
		this.ignoreRepaint = ignoreRepaint;
	}
	
	@Override
	public void paint( Graphics g )
	{
		if( ! ignoreRepaint )
			super.paint( g );
	}
}

public void dispose(GLAutoDrawable drawable) {
	// TODO Auto-generated method stub
	
}
}
