package org.graphstream.boids.output;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

public class BaseOutput
{
	protected PrintWriter out;
	
	public BaseOutput( String filename )
		throws FileNotFoundException, IOException
	{
		this(false,false,filename);
	}
	
	public BaseOutput( boolean buffered, boolean compressed, String filename )
		throws FileNotFoundException, IOException
	{
		this(buffered,compressed,new FileOutputStream(filename));
	}
	
	public BaseOutput( OutputStream out )
		throws IOException
	{
		this( false, false, out );
	}
	
	public BaseOutput( boolean buffered, boolean compressed, OutputStream out )
		throws IOException
	{
		if( compressed )
			out = new GZIPOutputStream(out);
		
		if( buffered )
			out = new BufferedOutputStream(out);
		
		this.out = new PrintWriter(out);
	}
	
	public void printf( String pattern, Object ...objects )
	{
		out.printf( pattern, objects );
	}
	
	public void print( String str )
	{
		out.print(str);
	}
	
	public void println( String line )
	{
		out.println(line);
	}
	
	public void flush()
	{
		out.flush();
	}
	
	public void close()
	{
		flush();
		out.close();
	}
}
