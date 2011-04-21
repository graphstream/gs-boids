package org.graphstream.boids.output;

import java.io.IOException;

import org.miv.millionboids.Context;

/**
 * Allows to run a boids simulation with only text output.
 * 
 * @author Damien Olivier
 * @author Guilhelm Savin
 * @author Antoine Dutot
 */
public class Batch
{
	public static void main( String args[] )
	{
		try
		{
			new Batch( args );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public Batch( String args[] )
		throws IOException
	{
		Context ctx = new Context();
		
		ctx.setup( args );
		ctx.loop();
	}
}