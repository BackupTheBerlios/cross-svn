/*
 * Created on Jul 31, 2004
 *
 */
package fr.cnrs.liris.cross;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * @author Pierre-Antoine Champin
 *
 * <p>This class is a central component of Cross: it provides to other classes
 * specialized description of the database to be converted to OWL.</p>
 */
public class DbInfo {
    	
	/**
	 * @author Pierre-Antoine Champin
	 *
	 * The set of parameters used to initialize a DbInfo.
	 */
	public static class Parameters {

		/**
		 * The name of the JDBC driver class to be used.
		 * If unset, the driver is assumed to be already registered.
		 */
      	public String jdbcDriver = null;

		/**
		 * The JDBC URL of the relational data source.
		 * This parameter <em>must</em> be set.
		 * @see #aboxBaseUri
         * @see #tboxBaseUri
		 */
		public String jdbcUrl;
		
		/**
		 * The JDBC user name to be used.
		 * Default to <code>null</code>.
		 */
		public String jdbcUsername = null;
		
		/**
		 * The JDBC password to be used.
		 * Default to <code>null</code>.
		 */
		public String jdbcPassword = null;
		
		/**
		 * The catalog in which to retrieve the tables.
		 * Default to <code>null</code>.
		 */
		public String catalog = null;
		/**
		 * A pattern matching the schema names in which to retrieve the tables.
		 * Default to <code>"%"</code>.
		 */
		public String schemaPattern = "%";
		/**
		 * A pattern matching the names of the table to be retrieved.
		 * Default to <code>"%"</code>.
		 */
		public String tablePattern = "%";
		/**
		 * An array of the types of the tables to be retrieved.
		 * Default to <code>{ "TABLE" }</code>.
		 */
		public String tableTypes[] = { "TABLE" };
		
		/**
		 * The base URI used to generate OWL URIs for classes and properties.
		 * If unset, an URI is generated based on the jdbcUrl.
		 * @see #jdbcUrl
         * @see #aboxBaseUri
		 */
		public String tboxBaseUri = null;

        /**
         * The base URI used to generate OWL URIs for instances.
         * If unset, an URI is generated based on the jdbcUrl.
         * @see #jdbcUrl
         * @see #tboxBaseUri
         */
        public String aboxBaseUri = null;
        
        /**
         * The URI of the TBox imported by the ABox.
         * This is typically a manually defined ontology, importing and
         * extending the generated TBox.    
         * If unset, the tboxBaseUri is used.
         */
        public String importedTboxUri = null;

		/**
		 * The encoding to be used to convert between SQL names and URIs.
		 * If unset, the default encoding will be used.
		 * @see fr.cnrs.liris.cross.Encoding#DEFAULT
		 */
		public Encoding enc = Encoding.DEFAULT;

        /**
         * The verbosity level of the messages on standard error.
         * 0 means no message at all; this is default.
         */
        public int verbosity = 0;
	}

    private final Parameters params;
    
    /**
     * Whether in the database, the catalog name appears at the start a fully
     * qualified table name. If not, it appears at the end.
     */
    final boolean catalogAtStart;
    /**
     * The string that the database uses as a seperator between a catalog name
     * and a table name.
     */
    final String catalogSep;
    /**
     * The string that the database uses to quote SQL identifiers.
     */
    final String quote;
    /**
     * A map whose keys are (unqualified) table names and whose values are
     * Table instances.
     */
    final HashMap tables = new HashMap ();

    /**
     * Create a DbInfo with the given parameters.
     * In particular, the database this DbInfo describes is given by
     * <code>params.jdbcUrl</code>. 
     * 
     * @param params the parameters
     */
    public DbInfo (Parameters params)
    throws ClassNotFoundException, java.sql.SQLException
    {
    	this.params = params;
    	
    	if ( params.jdbcDriver != null ) {
			Class.forName(params.jdbcDriver);
    	}
        
        if ( params.tboxBaseUri == null) {
            params.tboxBaseUri = params.jdbcUrl + "/cross/tbox#";
        }
        
        if ( params.aboxBaseUri == null) {
            params.aboxBaseUri = params.jdbcUrl + "/cross/abox#";
        }
        
        if ( params.importedTboxUri == null) {
            params.importedTboxUri = ns2uri(params.tboxBaseUri);
        }

		java.sql.Connection cx = openConnection ();
		DatabaseMetaData md = cx.getMetaData();
		
        catalogAtStart = md.isCatalogAtStart();
        catalogSep = md.getCatalogSeparator();
        quote = md.getIdentifierQuoteString();
        
		
		// create tables
		ResultSet rs = md.getTables (params.catalog,
									 params.schemaPattern,
									 params.tablePattern,
									 params.tableTypes);
		while (rs.next ()) {
            String cat = rs.getString("table_cat");
            String schem = rs.getString("table_schem");
            String name = rs.getString("table_name");
            if (checkPriviledge(md, cat, schem, name)) {
                Table table = new Table (this, cat, schem, name);
                tables.put (table.name, table);
                verbose (2, "table "+name+" created");
            } else {
                verbose (1, "table "+name+" not accessible");
            }
		}

		// for each table
		Iterator i = tables.values().iterator();
		while (i.hasNext()) {
			Table table = (Table) i.next ();
			
			// create columns
			rs = md.getColumns (table.catalog, table.schema, table.name, "%");
			while (rs.next ())
			{
                String name = rs.getString ("column_name");
                if (checkPriviledge(md, table, name)) {
                    Column column = new Column (table,
                                                rs.getString ("column_name"),
                                                rs.getInt ("data_type"));
                    table.addColumn (column, rs.getInt ("ordinal_position"));
                    if (rs.getInt("nullable")==DatabaseMetaData.columnNoNulls)
                    {
                        column.canBeNull = false;
                    }
                    verbose (4, "column "+table.name+"."+name+" created");
                } else {
                    verbose (3, "column "+table.name+"."+name+" not accessible");
                }
			}
			
			// create primary key
			rs = md.getPrimaryKeys(table.catalog, table.schema, table.name);
			while (rs.next ()) {
				if (table.primaryKey == null) {
					table.primaryKey =
						new PrimaryKey (rs.getString ("pk_name"));
				} 
				Column c = table.getColumn (rs.getString ("column_name"));
                if (c == null) {
                    // TODO LATER any better idea?
                    throw new RuntimeException ("Column "
                        + table.name +"." + rs.getString ("column_name")
                        + " is not accessible, though part of the primary key.\n"
                        + "Cross does not know (yet?) how to handle this.");
                }
				table.primaryKey.addColumn (c, rs.getInt ("key_seq"));
                verbose (3, "primary key for "+table.name+" created");
			}
            if (table.primaryKey == null) {
                verbose (1, "table "+table.name+" removed, because it has no primary key");
                // TODO LATER find another way of identifying rows so that we
                // can accept tables without a primary key
                i.remove();
            }
		}		

		// now that all tables are created with all their columns
		// for each table
		i = tables.values().iterator();
		while (i.hasNext()) {
			Table table = (Table) i.next ();
			
			// create foreign keys
			rs = md.getImportedKeys(table.catalog, table.schema, table.name);
			ForeignKey foreignKey = null;
			Table foreignTable = null;
            boolean skipCurrent = false;
			while (rs.next ()) {
                int key_seq = rs.getInt ("key_seq");
				String fk_name = rs.getString ("fk_name");
                
				if (key_seq == 1) {
                    if (foreignKey != null && !skipCurrent) {
                        table.foreignKeys.put (foreignKey.name, foreignKey);
                        verbose (3, "foreign key "+table.name+"."+foreignKey.name+" created");
                    }
                    
                    if (fk_name == null
                     // MySQL JDBC driver has the strange habit of returning
                     // the string "not available" instead of null
                     || fk_name.equals ("not_available")) {
                        // may be not the better possible ID, but...
                        fk_name = "__fk_"+rs.getString ("pktable_name")+"_"+rs.getString ("pkcolumn_name");
                    }
					foreignKey = new ForeignKey (this, table, fk_name);
					foreignTable = (Table)
					    tables.get (rs.getString ("pktable_name"));
                    skipCurrent = (foreignTable == null);
                    if (skipCurrent) verbose (2, "no foreign table for "+table.name+"."+fk_name);                        
				}
                
                if (skipCurrent) continue;
                
				Column c = table.getColumn (rs.getString ("fkcolumn_name"));
				Column fc =
				    foreignTable.getColumn (rs.getString ("pkcolumn_name"));
                if (c != null && fc != null) {
                    foreignKey.addColumn (c, rs.getInt ("key_seq"), fc);
                    verbose (5, "foreign key column "+table.name+"."+foreignKey.name+"."+rs.getString("fkcolumn_name")+" created");
                } else {
                    skipCurrent = true;
                    verbose (3, "column unreachable for "+table.name+"."+fk_name); // DEBUG                        
                }
			}
            // add last foreign key if needed 
            if (foreignKey != null && !skipCurrent) {
                table.foreignKeys.put (foreignKey.name, foreignKey);
                verbose (3, "foreign key "+table.name+"."+foreignKey.name+" created");
            }
			
			// create foreign key pairs
			TreeMap tm = new TreeMap ();
			Iterator j = table.foreignKeys.values ().iterator();
			while (j.hasNext ()) {
				ForeignKey fk = (ForeignKey) j.next ();
				if (!fk.subsumesPrimaryKey()) tm.put (fk.name, fk);
			}
			int s = tm.size();
			table.fkPairs.ensureCapacity( (s*(s-1))/2 );
			j = tm.values ().iterator();
			ForeignKey pair[] = new ForeignKey[2];
			while (j.hasNext ()) {
				pair[0] = (ForeignKey) j.next ();
				Iterator k = tm.tailMap (pair[0].name).values ().iterator ();
				k.next (); // skip 1st element, which is pair[0]
				while (k.hasNext ()) {
					pair[1] = (ForeignKey) k.next ();
					table.fkPairs.add (pair.clone ());
				}
			}
			
			// detect unique columns and unique foreign keys
			rs = md.getIndexInfo(table.catalog, table.schema, table.name, true, false);
            skipCurrent = false;
			ColumnSet index = null;
			while (rs.next ()) {
				int position = rs.getInt ("ordinal_position");
				if (position == 0) continue; // TODO LATER are those indices useful? 
				if (position == 1) {
					if (index != null && !skipCurrent) {
                         register_unique_index (index, table);
                    }
                    skipCurrent = false;
					index = new ColumnSet () {};
				}
				Column c = (Column) table.getColumn (rs.getString ("column_name"));
                if (c != null) {
                    index.addColumn (c, position);
                } else {
                    skipCurrent = true;
                }
			}
			if (index != null) register_unique_index (index, table);
		}
		
		cx.close();
    }
    
    /**
     * @return the value of this parameter
     * @see fr.cnrs.liris.cross.DbInfo.Parameters
     */
    public String getJdbcDriver () { return params.jdbcDriver; }
    /**
     * @return the value of this parameter
     * @see fr.cnrs.liris.cross.DbInfo.Parameters
     */
	public String getJdbcUrl () { return params.jdbcUrl; }
    /**
     * @return the value of this parameter
     * @see fr.cnrs.liris.cross.DbInfo.Parameters
     */
	public String getJdbcUsername () { return params.jdbcUsername; }
    /* (non javadoc)
     * @return the value of this parameter
     * @see fr.cnrs.liris.cross.DbInfo.Parameters
     */
    //public String getJdbcPassword () { return params.jdbcPassword; }
    /**
     * @return the value of this parameter
     * @see fr.cnrs.liris.cross.DbInfo.Parameters
     */
	public String getCatalog () { return params.catalog; }
    /**
     * @return the value of this parameter
     * @see fr.cnrs.liris.cross.DbInfo.Parameters
     */
	public String getSchemaPattern () { return params.schemaPattern; }
    /**
     * @return the value of this parameter
     * @see fr.cnrs.liris.cross.DbInfo.Parameters
     */
	public String getTablePattern () { return params.tablePattern; }
    /**
     * @return the value of this parameter
     * @see fr.cnrs.liris.cross.DbInfo.Parameters
     */
	public String[] getTableTypes () { return params.tableTypes; }
    /**
     * @return the value of this parameter
     * @see fr.cnrs.liris.cross.DbInfo.Parameters
     */
	public Encoding getEnc () { return params.enc; }
    /**
     * @return the value of this parameter
     * @see fr.cnrs.liris.cross.DbInfo.Parameters
     */
	public String getTBoxBaseUri () { return params.tboxBaseUri; }
    /**
     * @return the value of this parameter
     * @see fr.cnrs.liris.cross.DbInfo.Parameters
     */
    public String getABoxBaseUri () { return params.aboxBaseUri; }
    /**
     * @return the value of this parameter
     * @see fr.cnrs.liris.cross.DbInfo.Parameters
     */
    public String getImportedTboxUri () { return params.importedTboxUri; }
    /**
     * @return the value of this parameter
     * @see fr.cnrs.liris.cross.DbInfo.Parameters
     */
    public int getVerbosity () { return params.verbosity; }
    
    /**
     * Return the URI of the TBox
     * (this is the base URI of the TBox, without the trailing '#' or '/').
     * @return the URI of the TBox
     */
    String getTBoxUri () {
        return ns2uri (params.tboxBaseUri);
    }
    
    /**
     * Return the URI of the ABox
     * (this is the base URI of the ABox, without the trailing '#' or '/').
     * @return the URI of the ABox
     */
    public String getABoxUri () {
        return ns2uri (params.aboxBaseUri);
    }
    
    
    /**
     * Returns a connection to the database described by this DbInfo.
     * @return a connection to the database
     * @throws SQLException
     */
    java.sql.Connection openConnection () throws SQLException {
        return java.sql.DriverManager.getConnection(params.jdbcUrl,
                                                    params.jdbcUsername,
                                                    params.jdbcPassword);
    }

    /**
     * Return the URI for the given table, according to the Encoding used by
     * this DbInfo.
     * Note that the Table instance is assumed to belong to this DbInfo.
     * @param table a table of this DbInfo
     * @return the URI of the table
     * @see fr.cnrs.liris.cross.Encoding#uri(fr.cnrs.liris.cross.Table, java.lang.String)
     */    
    String uri (Table table) {
        return params.enc.uri (table, params.tboxBaseUri);
    }

    /**
     * Return the URI for the given column, according to the Encoding used by
     * this DbInfo.
     * Note that the Column instance is assumed to belong to this DbInfo.
     * @param column a column of this DbInfo
     * @return the URI of the column
     * @see fr.cnrs.liris.cross.Encoding#uri(fr.cnrs.liris.cross.Column, java.lang.String)
     */    
    String uri (Column column) {
        return params.enc.uri (column, params.tboxBaseUri);
    }

    /**
     * Return the URI for the given foreign key, according to the Encoding used
     * by this DbInfo.
     * Note that the ForeignKey instance is assumed to belong to this DbInfo,
     * and to belong to the given table.
     * @param fk a foreign key of this DbInfo
     * @param table the table this foreign key belongs to
     * @return the URI of the foreign key
     * @see fr.cnrs.liris.cross.Encoding#uri(fr.cnrs.liris.cross.ForeignKey, fr.cnrs.liris.cross.Table, java.lang.String)
     */    
    String uri (ForeignKey fk, Table table) {
        return params.enc.uri (fk, table, params.tboxBaseUri);
    }

    /**
     * Return the URI for the given foreign key pair, according to the Encoding
     * used by this DbInfo.
     * Note that the ForeignKey instances are assumed to belong to this DbInfo,
     * and to belong to the given table.
     * @param fk1 a foreign key of this DbInfo
     * @param fk2 a foreign key of this DbInfo
     * @param table the table these foreign key belong to
     * @return the URI of the foreign key pair
     * @see fr.cnrs.liris.cross.Encoding#uri(fr.cnrs.liris.cross.ForeignKey, fr.cnrs.liris.cross.Table, java.lang.String)
     */    
    String uri (ForeignKey fk1, ForeignKey fk2, Table table) {
        return params.enc.uri (fk1, fk2, table, params.tboxBaseUri);
    }

    /**
     * Return a URI for some row, according to the Encoding used by this
     * DbInfo.
     * It is assumed that the given table belongs to this DbInfo.
     * @param rs a result set
     * @param columnNames an iterator over the columns identifying this row
     * @param table the table containing the row to be identified
     * @return the URI of the row
     * @see fr.cnrs.liris.cross.Encoding#uri(java.sql.ResultSet, java.util.Iterator, fr.cnrs.liris.cross.Table, java.lang.String)
     */    
    String uri (java.sql.ResultSet rs, Iterator columnNames, Table table) {
        return params.enc.uri (rs, columnNames, table, params.aboxBaseUri);
    }
    
    private static void register_unique_index (ColumnSet index, Table table) {
        if (index.size () == 1) {
            index.getColumn(1).unique = true;
        }
        Iterator it_fk = table.foreignKeys.values ().iterator();
        while (it_fk.hasNext ()) {
            ForeignKey fk = (ForeignKey) it_fk.next ();
            if (fk.unique) continue;
            if (index.subsetOf (fk)) fk.unique = true;
        }
    }
    
    private static boolean checkPriviledge (DatabaseMetaData md,
                                            String cat,
                                            String schem,
                                            String table_name)
    throws SQLException {
        ResultSet rs = md.getTablePrivileges(cat, schem, table_name);
        return checkPrivilege(md, rs);
    }

    private static boolean checkPriviledge (DatabaseMetaData md,
                                            Table table,
                                            String column_name)
    throws SQLException {
        ResultSet rs = md.getColumnPrivileges(table.catalog,
                                              table.schema,
                                              table.name,
                                              column_name);
        return checkPrivilege(md, rs);
    }
    
    private static boolean checkPrivilege (DatabaseMetaData md, ResultSet rs)
    throws SQLException {
        // if result set is empty, we assume that it is a lack of support
        // for checkPrivilege in the JDBC driver rather than an absence of
        // privilege, so we return true.
        // e.g. MySQL does not 
        // TODO LATER any better idea ?
        int counter = 0;
         
        while (rs.next ()) {
            counter ++;
            if ((rs.getString("grantee").equals(md.getUserName())
              || rs.getString("grantee").equals("PUBLIC"))
             && rs.getString("privilege").equals("SELECT")) {
                 // TODO quite unsatisfactory: find a way to detect that a
                 //      granted group contains current user !
                 return true;
             }
        }
        return (counter==0);
    }
    
    private static String ns2uri (String base) {
        char last = base.charAt(base.length()-1);
        if (last == '/' || last == '#') {
            return base.substring(0, base.length()-1);
        } else {
            return base;
        }
    }
    
    private void verbose (int level, String msg) {
        if (level <= params.verbosity) {
            System.err.println(msg);
        }
    }

    /**
     * For debug.
     */
	public void printDescription ()
	{
		Iterator i = tables.values ().iterator();
		while (i.hasNext()) {
			Table t = (Table) i.next ();
			PrimaryKey pk = t.primaryKey;
			System.out.println (t.name);
			Iterator j = t.columnList.iterator();
			while (j.hasNext ()) {
				Column c = (Column) j.next ();
				if (pk!=null && pk.contains (c.name)) System.out.print ("  P ");
				else System.out.print ("    ");
				System.out.print (c.name);
				if (!c.canBeNull) System.out.print (" NOT NULL");
				if (c.unique) System.out.print (" UNIQUE");
				System.out.println ();
			}
			j = t.foreignKeys.values ().iterator();
			while (j.hasNext ()) {
				ForeignKey fk = (ForeignKey) j.next ();
				System.out.print(" FK "+fk.name+":");
				if (!fk.canBeNull ()) System.out.print (" NOT NULL");
				if (fk.unique) System.out.print (" UNIQUE");
				if (fk.subsumesPrimaryKey ()) System.out.print (" IS_PK");
				System.out.println ();
				Iterator k = fk.columnList.iterator();
				while (k.hasNext ()) {
					Column c = (Column) k.next ();
					Column fc = fk.mappedColumn (c);
					if (c!=null && fc!=null) // DEBUG
					System.out.println("    "+c.name+" -> "+fc.table.name+"."+fc.name);
				}
			}
		}
	}
}
