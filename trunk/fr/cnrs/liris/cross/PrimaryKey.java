/*
 * Created on Aug 5, 2004
 *
 */
package fr.cnrs.liris.cross;


/**
 * @author Pierre-Antoine Champin
 *
 * The description of a foreign key in a DbInfo.
 */
class PrimaryKey extends ColumnSet {
    /**
     * The name of this foreign key.
     */
	final String name;
	
    /**
     * @param name name of this foreign key.
     */
	PrimaryKey (String name)
	{
		this.name = name;
	}
	
}