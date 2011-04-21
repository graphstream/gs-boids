package org.graphstream.boids.output;

import java.io.File;
import java.io.IOException;

import javax.media.opengl.GLException;

import org.miv.millionboids.Context;

import com.sun.opengl.util.Screenshot;

public class ImageOutput
{
	protected int width;
	protected int height;
	
	protected boolean alpha;
	
	protected String dir;
	protected String prefix;
	protected String ext;
	
	protected int current;
	
	public ImageOutput()
	{
		current = -1;
		dir		= ".";
		prefix	= "image_output_";
		ext		= "png";
		alpha	= false;
		width	= 800;
		height	= 600;
	}
	
	public ImageOutput( Context ctx, String dir, String prefix )
	{
		this();
		
		this.dir	= dir;
		this.prefix = prefix;
	}
	
	public void setWidth( int width )
	{
		this.width = width;
	}
	
	public void setHeight( int height )
	{
		this.height = height;
	}
	
	protected void takeImage()
	{
		File out = new File( String.format( "%s%s%s%05d.%s", dir, File.separator, prefix, current, ext ) );
		
		try
		{
			Screenshot.writeToFile(out, width, height, alpha);
		}
		catch (GLException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void step( int step )
	{
		if( step > current )
		{
			current = step;
			takeImage();
		}
	}
	
	public int getStep()
	{
		return current;
	}
}
