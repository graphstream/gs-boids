package org.graphstream.glutil;

/**
 * Buffer listener.
 * 
 * <p>
 * A buffer listener allows to know, when to render thanks to the {@link
 * #display(Buffer)} method, when the buffer has been initialised and is ready
 * for rendering using the {@link #init(Buffer)} method, when the buffer changes
 * size and position thanks to the {@link #reshape(Buffer,int,int,int,int)}
 * method and when the buffer is closed or disposed using the
 * {@link #close(Buffer)} method.
 * </p>
 * 
 * <p>
 * Additionally, the buffer sends keyboard and mouse events using the {@link
 * #mouse(Buffer,int,int,int)} and {@link #key(Buffer,int,char,boolean)}
 * methods.
 * </p>
 * 
 * @author Antoine Dutot
 * @since 20040704
 */
public interface BufferListener
{
	/**
	 * Called to initiate rendering on the given buffer.
	 * @param buffer The buffer where rendering can occur.
	 */
	void
	display( Buffer buffer );

	/**
	 * Called after the OpenGL context is initialised for the first time on
	 * the given buffer.
	 * @param buffer The initialised buffer.
	 */
	void
	init( Buffer buffer );

	/**
	 * Called when the buffer has been resized and/or moved.
	 * @param buffer The reshaped buffer.
	 * @param x New abscissa in pixels.
	 * @param y New ordinate in pixels.
	 * @param width New width in pixels.
	 * @param height New height in pixels.
	 */
	void
	reshape( Buffer buffer, int x, int y, int width, int height );

	/**
	 * The buffer has been terminated. No more rendering can occur in it.
	 * @param buffer The terminated buffer.
	 */
	void
	close( Buffer buffer );

	/**
	 * Sent for every mouse event in the given buffer. The position of the mouse
	 * at the time of the event is the pixel located at (x,y), and the button
	 * value indicates which button is pressed or released at this time. Buttons
	 * 1 to 3 are the three usual mouse buttons, whereas buttons 4 ,5 and 6, 7
	 * are used for wheels (4 means wheel down, 5 means wheel up for example). 0
	 * Means that no button is pressed and negative values (-1, to -7) are used
	 * to mean the corresponding button is released.
	 * @param buffer The buffer owning the mouse focus, where the mouse changed
	 *        state.
	 * @param x The abscissa of the mouse pointer.
	 * @param y The ordinate of the mouse pointer.
	 * @param button Which button is pressed (0 means no button, negative
	 *        numbers mean that a button has been released).
	 */
	void
	mouse( Buffer buffer, int x, int y, int button );

	/**
	 * Sent for every key press or release in the
	 * given buffer. If the key has an Unicode counterpart, its value is
	 * returned in the unicode parameter. This is the low level access to the
	 * keyboard. It gives values for every keys, even the control ones like
	 * shift and alt for example. In this case the unicode value is 0 and only
	 * the key value is given (see the Keys class).
	 * @param buffer The buffer where (owning the keyboard focus) the key was pressed.
	 * @param key A constant of the {@link org.miv.glutil.Keys} class or -1 if this is an unicode character.
	 * @param unicode The unicode value, if available, else 0.
	 * @param pressed True if the key is pressed, false if released.
	 */
	void
	key( Buffer buffer, int key, char unicode, boolean pressed );
	
	/**
	 * High level key event that represent a key or a combination of keys that
	 * have been typed. This does not yield any control character alone.
	 * @param buffer The buffer where the key was pressed.
	 * @param unicode The unicode value of the key.
	 * @param modifiers A bit mask of modifiers.
	 */
	void
	keyTyped( Buffer buffer, char unicode, int modifiers );
}
