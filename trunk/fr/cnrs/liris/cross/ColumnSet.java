/*
 * Created on Aug 5, 2004
 *
 */
package fr.cnrs.liris.cross;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * @author Pierre-Antoine Champin  
 *
 * The common implementation of the description of tables, primary keys and
 * foreign keys.
 */
abstract class ColumnSet {
	final HashMap columnMap = new HashMap ();
	final ArrayList columnList = new ArrayList ();

    /**
     * Add column c to this column set.
     * Note that it is assumed that column c belongs to the same DbInfo, and
     * that it belongs to the correct table.
     * @param c the column to add
     * @param position the position of the column in this column set
     */
	void addColumn (Column c, int position) {
		columnMap.put (c.name, c);
		while (columnList.size () < position) columnList.add (null); 
		columnList.set (position-1, c);
	}
	
    /**
     * Return whether this column set contains a column of the given name.
     * @param columnName the name of the column to look for
     * @return <code>true</code> if so, else <code>fase</code>
     */
    boolean contains (String columnName) {
        return columnMap.containsKey (columnName);
    }

    /**
     * Return whether this column set contains a given Column instance.
     * @param c the Column instance to check
     * @return <code>true</code> if so, else <code>fase</code>
     */
    boolean contains (Column c) {
        return c.equals (columnMap.get (c.name));
    }
    
    /**
     * Get the Column instance at the given position in this column set.
     * @param position the position of the column to get
     * @return the corresponding Column instance
     */
	Column getColumn (int position) {
		return (Column) columnList.get (position - 1);
	}
	
    /**
     * Get the Column instance with the given name in this column set.
     * @param name the name of the column to get
     * @return the corresponding Column instance
     */
	Column getColumn (String name) {
		return (Column) columnMap.get (name);
	}
	
    /**
     * Return the number of columns in this column set.
     * @return the number of columns
     */
    int size () {
        return columnList.size();
    }

    /**
     * Return the table this column set is a subset of.
     * This method assumes that all columns do belong to the same table.
     * @return the corresponding Table instance
     */
	Table table () {
		return ((Column) columnList.get (0)).table;
	}
	
    /**
     * Return whether this column set is a subset of another given column set.
     * Note that this method does not check that both column set are subsets
     * of the same table, nor of the same DbInfo. Only column names are
     * compared.
     * @param other another column set
     * @return <code>true</code> if so, else <code>fase</code>
     */
	boolean subsetOf (ColumnSet other) {
		Iterator i = this.columnMap.keySet().iterator();
		while (i.hasNext ()) {
			if (!other.columnMap.containsKey (i.next ())) return false;
		}
		return true;
	}
    
    /**
     * Return whether this column set equals another given column set.
     * Note that this method is implemented by checking that both column sets
     * are subsets of each other. It follows that this method has the same
     * restrictions as subset.
     * @param other another column set
     * @return <code>true</code> if so, else <code>fase</code>
     * @see fr.cnrs.liris.cross.ColumnSet#subsetOf(fr.cnrs.liris.cross.ColumnSet)
     */
	public boolean equals (Object o) {
		if (o instanceof ColumnSet) {
			ColumnSet other = (ColumnSet) o;
			return this.subsetOf (other) && other.subsetOf (this);
		}
		return false;
	}
}