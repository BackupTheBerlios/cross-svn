/*
 * Created on Aug 5, 2004
 *
 */
package fr.cnrs.liris.cross;

import java.sql.Types;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

/**
 * @author Pierre-Antoine Champin
 *
 * This class provides static method for mapping SQL datatypes (as coded in
 * JDBC) with XML Schema datatypes.
 */
public abstract class Datatypes {
	
    /**
     * Return the instance of XSDDatatype corresponding to the given SQL
     * datatype.
     * @param sqlDataType the code of an SQL datatype
     * @return the corresponding XSDDatatype instance
     */
	public static XSDDatatype xsdType (int sqlDataType) {
		switch (sqlDataType) {
			// TODO LATER type conversion should probably be completed/improved
			case Types.ARRAY: return null;
			case Types.BIGINT:        return XSDDatatype.XSDinteger;
			case Types.BINARY:        return XSDDatatype.XSDhexBinary;
			case Types.BIT:           return XSDDatatype.XSDboolean;
			case Types.BOOLEAN:       return XSDDatatype.XSDboolean;
			case Types.CHAR:          return XSDDatatype.XSDstring;
			case Types.CLOB:          return null;
			case Types.DATALINK:      return null;
			case Types.DATE:          return XSDDatatype.XSDdate;
			case Types.DECIMAL:       return XSDDatatype.XSDdecimal;
			case Types.DISTINCT:      return null;
			case Types.DOUBLE:        return XSDDatatype.XSDdouble;
			case Types.FLOAT:         return XSDDatatype.XSDfloat;
			case Types.INTEGER:       return XSDDatatype.XSDinteger;
			case Types.JAVA_OBJECT:   return null;
			case Types.LONGVARBINARY: return XSDDatatype.XSDhexBinary;
			case Types.LONGVARCHAR:   return XSDDatatype.XSDstring;
			case Types.NULL:          return null;
			case Types.NUMERIC:       return XSDDatatype.XSDdecimal;
			case Types.OTHER:         return null;
			case Types.REAL:          return XSDDatatype.XSDdecimal;
			case Types.REF:           return null;
			case Types.SMALLINT:      return XSDDatatype.XSDshort;
			case Types.STRUCT:        return null;
			case Types.TIME:          return XSDDatatype.XSDtime;
			case Types.TIMESTAMP:     return XSDDatatype.XSDdateTime;
			case Types.TINYINT:       return XSDDatatype.XSDshort;
			case Types.VARBINARY:     return XSDDatatype.XSDhexBinary;
			case Types.VARCHAR:       return XSDDatatype.XSDstring;
		}
		return null;
	}

    /**
     * Return the URI of the XML Schema datatype corresponding to the given SQL
     * datatype.
     * @param sqlDataType the code of an SQL datatype
     * @return the corresponding XML Schema datatype URI
     */
	public static String xsdTypeUri (int sqlDataType) {
		return xsdType (sqlDataType).getURI();
	}
}
