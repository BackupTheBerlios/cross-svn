/*
 * Created on Aug 5, 2004
 *
 */
package fr.cnrs.liris.cross;

import java.util.HashMap;
import java.util.Iterator;



/**
 * @author Pierre-Antoine Champin  
 *
 * The description of a foreign key in a DbInfo.
 */
class ForeignKey extends ColumnSet {
    /**
     * The name of this foreign key.
     */
	final String name;
    /**
     * The URI of this foreign key.
     */
    final String uri;
    /**
     * The name of the foreign key as used in SQL queries, i.e. fully qualified
     * and quoted.
     */
    final String sqlname;
    /**
     * A mapping whose keys are columns of the foreign key (as Column
     * instances), and values are the corresponding columns in the table
     * referenced by the foreign key (as Column instances).
     */
	final HashMap mapping = new HashMap ();
    /**
     * Whether this foreign key's value has to be unique for each row.
     * Note: this is set by DbInfo constructor, <em>not</em> by the
     * Column constructor.
     */
	boolean unique = false;
	
    /**
     * @param info the DbInfo this foreign key belongs to
     * @param table the table this foreign key belongs to
     * @param name the name of the foreign key
     */
	ForeignKey (DbInfo info, Table table, String name)
	{
		this.name = name;
        this.uri = info.uri (this, table);
        this.sqlname = info.quote + name + info.quote;
	}
	
    /**
     * Unlike other ColumnSets, addColumn needs an additional parameter: the
     * column in the foreign table to which the added column is mapped.
     * So this inherited method raises an UnsupportedOperationExcepton.
     * @throws java.lang.UnsupportedOperationException always
     * @see #addColumn(fr.cnrs.liris.cross.Column, int, fr.cnrs.liris.cross.Column) 
     */
	void addColumn (Column c, int position) throws UnsupportedOperationException {
		throw new UnsupportedOperationException ();
	}
	
    /**
     * Add column c to this foreign key, mapped to foreignColumn.
     * Note that it is assumed that all foreign columns belong to the same
     * table, and of course to the same DbInfo. 
     * @param c the column to add
     * @param position the position of the column in this column set
     * @param foreignColumn the column mapped to c
     */
	void addColumn (Column c, int position, Column foreignColumn) {
		super.addColumn (c, position);
		mapping.put (c, foreignColumn);
	}
	
    /**
     * Return a column set corresponding to the columns of the foreign table
     * this foreign keys maps to.
     * @return a subset of the foreign table
     */
	ColumnSet foreignColumnSet () {
		ColumnSet r = new ColumnSet () {};
		Iterator i = columnMap.values().iterator();
		int pos = 1;
		while (i.hasNext ()) {
			Column foreignColumn = (Column) mapping.get (i.next ());
			r.addColumn (foreignColumn, pos);
			pos += 1;
		}
		return r;
	}
	
    /**
     * Shortcut method to get the Column instance mapped to the column with
     * the given name. 
     * @param columnName the name of the local column
     * @return the foreign column
     */
	Column mappedColumn (String columnName) {
		return (Column) mapping.get (columnMap.get (columnName));
	}
	
    /**
     * Shortcut method to get the Column instance mapped to the given column
     * instance. 
     * @param c the local column
     * @return the foreign column
     */
	Column mappedColumn (Column c) {
		return (Column) mapping.get (c);
	}
	
    /**
     * Return whether some column of this foreign key can be null.
     * Note that referential integrity does not apply for a row if some values
     * of the foreign key are NULL.
     * @return <code>true</code> if so, else <code>false</code>
     */
	boolean canBeNull () {
		Iterator i = columnList.iterator();
		while (i.hasNext ()) {
			if (((Column) i.next ()).canBeNull) return true;
		}
		return false;
	}
	
    /**
     * Return whether this foreign key subsumes the primary key of its table,
     * i.e. if it can not be NULL and if it is a superset of the primary key.
     * If so, then each row of the table has a corresponding row in the foreign
     * table, which can be interpreted as a subclass relation between the
     * classes corresponding to the tables (rule 3b).
     * @return <code>true</code> if so, else <code>false</code>
     */
	boolean subsumesPrimaryKey () {
        if (table() == null) throw new RuntimeException ("table is null");
        if (table().primaryKey == null) throw new RuntimeException (table().name+" has no primary key");
		return !this.canBeNull() && table ().primaryKey.subsetOf (this);
	}
}