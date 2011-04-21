package org.graphstream.boids.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.util.Collections;
import java.util.LinkedList;

public class MergeStats
{
	static class Entry
		implements Comparable<Entry>
	{
		Integer date;
		LinkedList<Integer> values = new LinkedList<Integer>();
		
		public int compareTo(Entry o)
		{
			return date.compareTo(o.date);
		}
	}
	
	static class Entries
		extends LinkedList<Entry>
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -3077241584969915774L;
		
	}
	
	static class TypeFileNameFilter
		implements FilenameFilter
	{
		String pattern;
		
		public TypeFileNameFilter( String prefix )
		{
			pattern = String.format( "%s\\d", prefix );
		}
		
		public boolean accept(File dir, String name)
		{
			return name.matches(pattern);
		}
	}
	
	Entries				merged	= new Entries();
	LinkedList<Entries> entries = new LinkedList<Entries>();
	String dir;
	
	public MergeStats( String dir, String prefix )
	{
		this.dir = dir;
		
		readEntries(prefix);
		merge();
		output(dir,prefix);
	}
	
	public void readEntries( String prefix )
	{
		File directory = new File(dir);
		TypeFileNameFilter filter = new TypeFileNameFilter(prefix);
		
		for( File f : directory.listFiles(filter))
		{
			System.err.printf( "reading file \"%s\"\t", f.getName() );
			entries.add(readFile(f));
			System.err.printf( "ok\n" );
		}
	}
	
	public void output( String dir, String prefix )
	{
		try
		{
			PrintStream out = new PrintStream( String.format("%s%s%smean",dir, File.separator, prefix) );
			
			for( int i = 0; i < merged.size(); i++ )
			{
				Entry e = merged.get(i);
				out.printf( "%d ", e.date );
				for( int j = 0; j < e.values.size(); j++ )
					out.printf( "%d ", e.values.get(j) );
				out.printf("\n");
			}
			
			out.flush();
			out.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	protected void merge()
	{
		merged.clear();
		checkDates();
		
		int size = entries.get(0).size();
		int i = 0;
		int sum = 0;
		
		while(i<size)
		{
			Entry e = new Entry();
			e.date = entries.get(0).get(i).date;
			
			for( int j = 0; j < entries.get(0).get(0).values.size(); j++ )
			{
				sum = 0;
			
				for( Entries en : entries )
					sum += en.get(i).values.get(j);
				
				e.values.addLast( sum / entries.size() );
			}
			
			merged.addLast(e);
			
			i++;
		}
	}
	
	protected void checkDates()
	{
		for( Entries e : entries )
			Collections.sort(e);
		
		//int i = 0;
		/*
		for( int k = 0; k < entries.size(); k++ )
		{
			for( int l = 1; l < entries.get(k).size(); l++ )
			{
				while( entries.get(k).get(l).date != entries.get(k).get(l-1).date )
				{
					Entry e = new Entry();
					e.date = entries.get(k).get(l).date + 1;
					e.values = new LinkedList<Integer>();
					for( int m = 0; m < entries.get(k).get(l).values.size(); m++ )
						e.values.addLast( (int) (entries.get(k).get(l).values.get(m) + 
								0.5*( entries.get(k).get(l+1).values.get(m) - entries.get(k).get(l).values.get(m) )) );
				}
			}
		}
		*/
		for( int k = 0; k < entries.size(); k++ )
		{
			while( entries.get(k).size() < getMaxSize() )
			{
				Entry e = new Entry();
				e.date = entries.get(k).peekLast().date + 1;
				e.values = entries.get(k).peekLast().values;
				entries.get(k).addLast(e);
			}
		}
		/*
		do
		{
			if( ! checkDatesAt(i) )
				fixDateEntriesAt(i);
			
			if( ! checkValuesAt(i) )
				fixValuesEntriesAt(i);
			
			i++;
		}
		while( i < getMaxSize() );
		*/
	}
	
	protected boolean checkDatesAt( int index )
	{
		boolean r = true;
		int date = entries.get(0).get(index).date;
		
		for( int i = 1; i < entries.size(); i++ )
			r = r && ( entries.get(i).get(index).date == date );
		
		return r;
	}
	
	protected boolean checkValuesAt( int index )
	{
		boolean r = true;
		int values = entries.get(0).get(index).values.size();
		
		for( int i = 1; i < entries.size(); i++ )
			r = r && ( entries.get(i).get(index).values.size() == values );
		
		return r;
	}
	
	protected int getMaxSize()
	{
		int max = 0;
		
		for( Entries e : entries )
			max = Math.max(max,e.size());
		
		return max;
	}
	
	protected int getMinSize()
	{
		int min = Integer.MAX_VALUE;
		
		for( Entries e : entries )
			min = Math.min(min,e.size());
		
		return min;
	}
	
	protected void fixDateEntriesAt( int index )
	{
		int min = 0;
		
		for( int i = 1; i < entries.size(); i++ )
		{
			if( entries.get(i).get(index).date < entries.get(min).get(index).date )
				min = i;
		}
		
		for( int i = 0; i < entries.size(); i++ )
		{
			if( i != min )
			{
				Entries en = entries.get(i);
				Entry e = new Entry();
				e.date = entries.get(min).get(index).date;
				
				for( int j = 0; j < en.get(index-1).values.size(); j++ )
					e.values.addLast( ( en.get(index).values.get(j)- en.get(index-1).values.get(j) ) / 2 );
				
				en.add(index,e);
			}
		}
	}
	
	protected void fixValuesEntriesAt( int index )
	{
		
	}
	
	protected Entries readFile( File f )
	{
		try
		{
			StreamTokenizer in = new StreamTokenizer( new FileReader(f) );
			in.eolIsSignificant(true);
			
			Entries	entries = new Entries();
			Entry 	entry = null;
			
			int token;
			
			while( (token=in.nextToken()) != StreamTokenizer.TT_EOF )
			{
				switch(token)
				{
				case StreamTokenizer.TT_EOF:
				case StreamTokenizer.TT_EOL: 
					entries.addLast(entry);
					entry = null;
					break;
				case StreamTokenizer.TT_NUMBER:
					if( entry == null )
					{
						entry = new Entry();
						entry.date = (int) in.nval;
					}
					else
					{
						entry.values.addLast( (int) in.nval );
					}
					break;
				}
			}
			
			if( entry != null )
				entries.addLast(entry);
			
			return entries;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void main( String [] args )
	{
		new MergeStats(args[0],args[1]);
	}
}
