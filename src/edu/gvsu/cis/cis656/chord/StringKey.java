package edu.gvsu.cis.cis656.chord;

import de.uniba.wiai.lspi.chord.service.Key;

public class StringKey implements Key {

	private String theString ;
	
	public StringKey ( String theString )
	{		
		this. theString = theString ;
	}
	
	public byte [] getBytes (){
		return this. theString . getBytes ();
	}
	
	public int hashCode (){
		return this. theString . hashCode ();
	}
	
	public boolean equals ( Object o){
		if (o instanceof StringKey )
		{
			(( StringKey)o).theString.equals (this.theString );
		}
		return false;
	}
}
