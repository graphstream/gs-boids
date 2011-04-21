package org.graphstream.boids.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.graphstream.boids.Context;

public class GUI extends JFrame implements ActionListener
{
// Attributes
	
    private static final long serialVersionUID = 1L;

	/**
	 * Shared information.
	 */
	protected Context ctx;
	
	/**
	 * The swing timer.
	 */
	protected Timer timer;

	/**
	 * Viewer of the particles.
	 */
	protected ParticleViewer viewer;
	
// Constructors
	
	public GUI( Context context )
	{
		ctx    = context;
		viewer = new ParticleViewer( context );
		
		build();
	}
	
	public void disconnect()
	{
		viewer.disconnect();
	}
	
	public void reconnect()
	{
		viewer.reconnect();
	}
	
	protected void build()
	{
		setLayout( new BorderLayout() );

		add( viewer, BorderLayout.CENTER );
	
		timer = new Timer( ctx.sleepTime, this );
		
		Dimension dim = new Dimension( 770, 770 );
		
		
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		setMinimumSize( dim );
		setPreferredSize( dim );
		setSize( dim );
		setVisible( true );
		timer.start();
	}

	public void actionPerformed( ActionEvent e )
    {
		if( e.getSource() == timer )
		{
			viewer.display();
		}
    }
}