/*
 * Created on Aug 5, 2004
 *
 */
package fr.cnrs.liris.cross;



/**
 * @author Pierre-Antoine Champin
 *
 * The description of a column in a DbInfo.
 */
class Column {
    /**
     * The Table instance representing the table containing this column.
     */
	final Table table;
    /**
     * The name of this column.
     */
	final String name;
    /**
     * The URI of this column.
     */
    final String uri;
    /**
     * The name of the column as used in SQL queries, i.e. fully qualified and
     * quoted.
     */
    final String sqlname;
    /**
     * The JDBC code for the datatype of the column.
     */
	final int type;
    /**
     * Whether this column allows NULL value.
     * Note: this is set by DbInfo constructor, <em>not</em> by the
     * Column constructor.
     */
	boolean canBeNull = true;
    /**
     * Whether this column's value has to be unique for each row.
     * Note: this is set by DbInfo constructor, <em>not</em> by the
     * Column constructor.
     */
	boolean unique = false;
	
    /**
     * @param table the table containing the column
     * @param name the name of the column
     * @param type the JDBC code for the column's datatype
     */
	Column (Table table, String name, int type)
	{
		this.table = table;
		this.name = name ;
		this.type = type ;
        DbInfo info = table.info;
        this.uri = info.uri (this);
        this.sqlname = info.quote + name + info.quote;
	}
}