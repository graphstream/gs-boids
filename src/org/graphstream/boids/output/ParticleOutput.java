package org.graphstream.boids.output;

import java.io.IOException;

import org.miv.millionboids.Context;
import org.miv.pherd.ParticleBoxListener;
import org.miv.pherd.ntree.Anchor;
import org.miv.pherd.ntree.NTreeListener;

public class ParticleOutput
	extends BaseOutput
	implements ParticleBoxListener, NTreeListener
{
// Attribute
	
	/**
	 * Shared information.
	 */
	protected Context ctx;
	
	/**
	 * The output.
	 */
	//protected PrintStream out;
	
// Construction
	
	public ParticleOutput( Context ctx, String fileName )
		throws IOException
	{
		/*
		 * Init a buffered uncompressed output.
		 */
		super( true, false, fileName );
		
		this.ctx = ctx;
		
		outputHeader();
		ctx.getPbox().addParticleBoxListener( this );
		ctx.getPbox().getNTree().addListener( this );
	}
	
// Access

// Command
	
	public void close()
	{
		ctx.getPbox().removeParticleBoxListener( this );
		ctx.getPbox().getNTree().removeListener( this );
		
		super.close();
	}
	
	protected void outputHeader()
	{
		printf( "BOIDSBAND01%n" );
		printf( "%f %f%n",
			ctx.getPbox().getNTree().getRootCell().getSpace().getLoAnchor().x,
			ctx.getPbox().getNTree().getRootCell().getSpace().getHiAnchor().x );
	}
	
// Command -- Listener
	
	public void particleAdded( Object id, float x, float y, float z )
    {
		printf( "ap '%s' %f %f %f%n", id, x, y, z );
    }

	public void particleAttributeChanged( Object id, String attribute, Object newValue,
            boolean removed )
    {
		if( removed )
		     printf( "par '%s' '%s'%n", id, attribute );
		else printf( "paa '%s' '%s' '%s'%n", id, attribute, newValue );
    }

	public void particleMoved( Object id, float x, float y, float z )
    {
		printf( "pm '%s' %f %f %f%n", id, x, y, z );
    }

	public void particleRemoved( Object id )
    {
		printf( "pr '%s'%n", id );
    }

	public void stepFinished( int time )
    {
		printf( "step %d%n", time );
    }

	public void cellAdded( Object id, Object parentId, Anchor lo, Anchor hi, int depth, int index )
    {
    }

	public void cellData( Object id, String message, Object data )
    {
    }

	public void cellRemoved( Object id )
    {
    }
}