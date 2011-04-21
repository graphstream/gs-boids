package org.graphstream.boids.output;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.miv.millionboids.Context;
import org.miv.millionboids.ContextListener;
import org.miv.millionboids.boids.Boid;

public class StatsOutput
	extends BaseOutput
	implements ContextListener
{
	Context ctx;
	boolean	populationChanged = true;
	String	last = null;
	
	public StatsOutput(Context ctx, String filename)
		throws FileNotFoundException, IOException
	{
		super(false,false,filename);
		
		this.ctx = ctx;
		ctx.addContextListener(this);
	}

	public void close()
	{
		ctx.removeContextListener(this);
		
		if( last != null )
			println(last);
		
		super.close();
	}
	
	@Override
	public void boidAdded(Boid boid)
	{
		populationChanged = true;
	}

	@Override
	public void boidDeleted(Boid boid)
	{
		populationChanged = true;
	}

	@Override
	public void step(int time)
	{
		StringBuffer line = new StringBuffer();
		
		line.append(time);
		
		for( int i=0; i < ctx.getSpeciesCount(); i++ )
			line.append(' ').append(ctx.getOrCreateSpecies(i).getPopulation());
		
		if( populationChanged )
		{
			if( last != null )
			{
				println(last);
				last = null;
			}
			
			println(line.toString());
			flush();
		}
		else
		{
			last = line.toString();
		}
		
		populationChanged = false;
	}

}
