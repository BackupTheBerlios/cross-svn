/*
 * Created on Aug 7, 2004
 *
 */
package fr.cnrs.liris.cross;

import java.util.Iterator;

/**
 * @author Pierre-Antoine Champin
 *
 * This interface provides methods used by a DbInfo to generate URIs from the
 * SQL identifiers of the elements (tables, columns, etc.) of the database it
 * describes.
 * 
 * @see fr.cnrs.liris.cross.DbInfo
 */
public interface Encoding {
	
	/**
	 * The default encoding.
	 * This is a PaEncoding, <em>but this is <strong>not</strong> stable for
	 * the moment, and should not be relied on</em>.
	 * @see fr.cnrs.liris.cross.PaEncoding
	 */
	public static Encoding DEFAULT = new PaEncoding ();
	
	/**
	 * Encode an SQL name into a URI component.
	 * @param str the SQL name
	 * @return an encoded URI component
	 */
	public String encode (String str);
	
	/**
	 * Decode a URI component into an SQL name.
     * @param str the URI component
     * @return a decoded SQL name
	 */
	public String decode (String str);
	
	/**
	 * Return the URI for the given Table, based on the given base URI.
	 * @param table a table
	 * @return the URI of table
	 */
	public String uri (Table table, String base);

	/**
	 * Return the URI for the given Column, based on the given base URI.
	 * @param column a column
	 * @return the URI of column
	 */
	public String uri (Column column, String base);

	/**
	 * Return the URI for the given ForeignKey, belongin to the given table,
     * based on the given base URI.
	 * @param fk a foreign key
     * @param table the table containing this foreign key
	 * @return the URI of this foreign key
	 */
	public String uri (ForeignKey fk, Table table, String base);

	/**
	 * Return the URI for the given ForeignKey pair, both foreign keys
     * belonging to the given table, based on the given base URI.
     * @param fk1 the first foreign key
     * @param fk2 the second foreign key
	 * @param table the table containing both foreign keys
	 * @return the URI of this foreign key pair
	 */
	public String uri (ForeignKey fk1, ForeignKey fk2, Table table, String base);
	
	/**
	 * Return the URI for some row of the given table.
     * It is assumed that
     * <li>the columns enumerated in columnNames do identify rows of the given
     *     table,</li>
     * <li>the given result set has such columns,</li>
     * <li>the values of these columns in the current row of the result set
     *     actually exist in the given table.</li>
     * </ul>
     * A typical use is to for table to be the table this result set has been
     * produced from, and columNames iterate over its primary key.
     * However, other uses are possible, e.g. to get the URI of the row
     * referenced by a foreign key in the result set.
     * @param rs a result set
     * @param columnNames an iterator over the columns identifying this row
     * @param table the table containing the row to be identified
     * @return the URI of the row
	 */
	public String uri (java.sql.ResultSet rs, Iterator columnNames, Table table, String base);
	
	/**
	 * Return the object from the given RdbIndexto corresponding to the given URI.
	 * The returned object can be <ul>
	 * <li>a Table</li>
	 * <li>a Column</li>
	 * <li>a ForeignKey</li>
	 * <li>a ForeignKey array of size 2 (URI of a ForeignKey pair)</li>
	 * <li>a String array whose first element is a table name,
	 * and the following elements are values for its primary key
	 * (URI of a row)</li>
	 * </ul> 
	 * @param uri the URI to find an object for
	 * @param info the DbInfo assumed to contain the object
	 * @return the object this URI identifies
	 */
	public Object decodeUri (String uri, DbInfo info);
}
