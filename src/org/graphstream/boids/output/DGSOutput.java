package org.graphstream.boids.output;

import org.graphstream.stream.file.FileSinkDGS;

public class DGSOutput
{
	FileSinkDGS output;
	long	timeId;
	
	public DGSOutput()
	{
		
	}
	
	public void init( String fileDGS )
	{
		output = new FileSinkDGS();
		
		try
		{
			output.begin(fileDGS);
		}
		catch( Exception e ) { e.printStackTrace(); }
		
		timeId = 0;
	}
	
	public void end()
	{
		try
		{
			output.end();
		}
		catch( Exception e ) { e.printStackTrace(); }
	}
	
	public void boidAdded( String id )
	{
		output.nodeAdded("boids", timeId++, id );
	}
	
	public void boidRemoved( String id )
	{
		output.nodeRemoved("boids", timeId++, id );
	}
	
	public void boidInteraction( String id1, String id2, boolean on )
	{
		if( on )
		{
			output.edgeAdded("boids",timeId++,getInteractionId(id1,id2),id1,id2,false);
		}
		else
		{
			output.edgeRemoved("boids",timeId++,getInteractionId(id1,id2));
		}
	}
	
	public void step( int step )
	{
		output.stepBegins("boids",timeId++,step);
	}
	
	protected static String getInteractionId( String id1, String id2 )
	{
		if( id1.compareTo(id2) < 0 )
			return String.format("%s---%s",id1,id2);
		
		return String.format("%s---%s",id2,id1);
	}
}
