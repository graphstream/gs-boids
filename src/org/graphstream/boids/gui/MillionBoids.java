package org.graphstream.boids.gui;

import org.graphstream.boids.Context;

public class MillionBoids
{
    private static final long serialVersionUID = 1L;

// Attributes
    
    protected Context ctx;
    
// Constructors
    
    public static void main( String args[] )
    {
    	new MillionBoids( args );
    }
    
    public MillionBoids( String args[] )
    {
    	try
    	{
    		ctx = new Context();
    	
    		ctx.setup( args );
    		//ctx.addGUI();
    		ctx.loop();
    	}
    	catch( Exception e )
    	{
    		e.printStackTrace();
    	}
    }
    
// Access
    
// Commands
}