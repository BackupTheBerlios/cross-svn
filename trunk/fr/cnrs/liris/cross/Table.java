/*
 * Created on Aug 5, 2004
 *
 */
package fr.cnrs.liris.cross;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * @author Pierre-Antoine Champin
 *
 * The description of a table in a DbInfo.
 */
class Table extends ColumnSet {
	
    /**
     * The DbInfo this table belongs to.
     */
    final DbInfo info;
    /**
     * The catalog this table belongs to.
     */
	final String catalog;
    /**
     * The schema this table belongs to.
     */
	final String schema;
    /**
     * The name of this table.
     */
	final String name;
    /**
     * The URI of this table.
     */
    final String uri;
    /**
     * The name of the table as used in SQL queries, i.e. fully qualified and
     * quoted.
     */
    final String sqlname;
    /**
     * The primary key of this table.
     */
	PrimaryKey primaryKey = null;
    /**
     * A map whose keys are names of the foreign keys of this table, and values
     * are the corresponding ForeignKey instances.
     */
	final HashMap foreignKeys = new HashMap ();
    /**
     * A list of all the relevant foreign key pairs (rule 4).
     */
	final ArrayList fkPairs = new ArrayList (0); 

    /**
     * @param info the DbInfo this table belongs to
     * @param catalog the catalog this table belongs to
     * @param schema the schema this table belongs to
     * @param name the name of this table
     */
	Table (DbInfo info, String catalog, String schema, String name)
	{
        this.info = info;
		this.catalog = catalog;
		this.schema = schema;
		this.name = name;
        this.uri = info.uri(this);
        
        StringBuffer tmp = new StringBuffer ();
        
        if (catalog != null && info.catalogAtStart) {
            tmp.append(info.quote).append(catalog).append(info.quote)
               .append(info.catalogSep);
        }
        if (schema != null) {
            tmp.append(info.quote).append(schema).append(info.quote)
               .append(".");
        }
        tmp.append(info.quote).append(name).append(info.quote);
        if (catalog != null && !info.catalogAtStart) {
            tmp.append(info.catalogSep)
            .append(info.quote).append(catalog).append(info.quote);
            // TODO LATER I'm not sure how this kind of DBMS work...
            // In particular, is a column name like
            //      schema.table.column@catalog
            // or
            //      schema.table@catalog.column
            // The following assumes the second option,
            // because it is easier to implement.
            // If the first option is correct, other classes might
            // have to be modified (Column, TBoxGraph, etc...)
        }
        
        this.sqlname = tmp.toString();
	}
}