package org.graphstream.glutil;

/**
 * Various settings for a {@link org.miv.glutil.Buffer}.
 *
 * @author Antoine Dutot
 * @since 20040704
 */
public class BufferFormat
{
// Attributes

	/**
	 * Is alpha channel (&gt;0) enabled and on how many bits?.
	 */
	protected int alphabits = 8;

	/**
	 * Size of a Z-buffer component in bits.
	 */
	protected int depthbits = 16;

	/**
	 * Size of a red component of the frame buffer in bits.
	 */
	protected int redbits = 8;

	/**
	 * Size of a green component of the frame buffer in bits.
	 */
	protected int greenbits = 8;

	/**
	 * Size of a blue component of the frame buffer in bits.
	 */
	protected int bluebits = 8;

	/**
	 * Create a back buffer?.
	 */
	protected boolean double_buffered = true;

// Constructors

	/**
	 * Use a default of 8 bits for red, green, blue, alpha and depth components
	 * and double-buffering.
	 */
	public BufferFormat()
	{
		this( true );
	}

	/**
	 * Use a default of 8 bits for red, green, blue, alpha and depth components.
	 * @param double_buffered Are buffers doubled to avoid flickering?.
	 */
	public BufferFormat( boolean double_buffered )
	{
		this( 8, 8, 8, 8, 16, double_buffered );
	}

	/**
	 * New model specifying all sizes.
	 * @param red_bits Number of bits for the red component in the colour buffer.
	 * @param green_bits Number of bits for the green component in the colour buffer.
	 * @param blue_bits Number of bits for the blue component in the colour buffer.
	 * @param alpha_bits Number of bits for the alpha component in the colour buffer.
	 * @param depth_bits NUmber of bits for the depth buffer.
	 * @param double_buffered Are buffers doubled to avoid flickering?.
	 */
	public BufferFormat( int red_bits, int green_bits, int blue_bits, int alpha_bits, int depth_bits, boolean double_buffered )
	{
		set( red_bits, green_bits, blue_bits, alpha_bits, depth_bits, double_buffered );
	}

// Access

	/**
	 * Copy of this.
	 */
	@Override
	public BufferFormat clone()
	{
		return new BufferFormat( redbits, greenbits, bluebits, alphabits, depthbits, double_buffered );
	}

	/**
	 * Number of bits for red components.
	 */
	public int getRedBits()
	{
		return redbits;
	}

	/**
	 * Number of bits for green components.
	 */
	public int getGreenBits()
	{
		return greenbits;
	}

	/**
	 * Number of bits for blue components.
	 */
	public int getBlueBits()
	{
		return bluebits;
	}

	/**
	 * Number of bits for alpha components.
	 */
	public int getAlphaBits()
	{
		return alphabits;
	}

	/**
	 * Number of bits for depth components.
	 */
	public int getDepthBits()
	{
		return depthbits;
	}

	/**
	 * Using double-buffering?.
	 */
	public boolean isDoubleBuffered()
	{
		return double_buffered;
	}

// Commands

	/**
	 * Specify the size of all components of the buffer in bits.
	 * @param red_bits Number of bits for the red component in the colour buffer.
	 * @param green_bits Number of bits for the green component in the colour buffer.
	 * @param blue_bits Number of bits for the blue component in the colour buffer.
	 * @param alpha_bits Number of bits for the alpha component in the colour buffer.
	 * @param depth_bits NUmber of bits for the depth buffer.
	 * @param double_buffered Are buffers doubled to avoid flickering?.
	 */
	public void set( int red_bits, int green_bits, int blue_bits, int alpha_bits, int depth_bits, boolean double_buffered )
	{
		this.redbits         = red_bits;
		this.greenbits       = green_bits;
		this.bluebits        = blue_bits;
		this.alphabits       = alpha_bits;
		this.depthbits       = depth_bits;
		this.double_buffered = double_buffered;
	}
}
