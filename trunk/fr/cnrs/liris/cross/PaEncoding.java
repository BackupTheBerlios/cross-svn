/*
 * Created on Aug 5, 2004
 *
 */
package fr.cnrs.liris.cross;

import java.util.Iterator;

/**
 * @author Pierre-Antoine Champin
 *
 * <p>An implementation of the interface Encoding.
 * This is not the most element one can think of, but it works.</p>
 * <p>The design rationale behind this encoding is to generate URIs which are
 * both XML-friendly and N3-friendly, hence a limited number of usable
 * non-alphanumeric characters.</p>
 * <p>The different kinds of URIs are generated according to the following
 * patterns:
 * <table>
 * <tr><td>Table</td><td>T--<em>table_name</em></td><tr>
 * <tr><td>Column</td><td>c--<em>table_name</em>--<em>column_name</em></td><tr>
 * <tr><td>Foreign key</td><td>k--<em>table_name</em>--<em>foreign_key_name</em></td><tr>
 * <tr><td>Foreign key pair</td><td>p--<em>table_name</em>--<em>foreign_key_1_name</em>--<em>foreign_key_2_name</em></td><tr>
 * <tr><td>Row</td><td>r--<em>table_name</em>--<em>field_1_value</em>--<em>field_2_value</em>--...
 *                 <br />where the fields are the columns of the table's primary key</td><tr>
 * </table></p>
 * <p>In those patterns, all names and values are encoded using the following
 * method: letters, digit and underscores are let as is; all other characters
 * are represented by their unicode value in hexadeximal, surrounded by the
 * character '-'. Additionnaly, to prevent the sequence '--' to appear in the
 * encoded strings (since it is the separator in the patterns above), the two
 * following rules apply:<ul>
 * <li>When several encoded characters occur, their codes are separated by an
 * underscore rather than '--'. E.g. "$$a" is encoded "-24_24-a" rather than
 * "-24--24-a".</li>
 * <li>When the last character of a string is encoded, the trailing '-' is
 * omitted. E.g. "a$" is encoded "a-24" rather than "a-24-".</li> 
 * </ul></p>
 */
public class PaEncoding implements Encoding {
	
	public static final char ESCAPE_CHAR = '-';
	public static final char ESCAPE_SEP = '_';
	public static final String SEPARATOR = "--";
	
	public String encode (String str) {
		StringBuffer sb = new StringBuffer ();
		boolean escape = false;
		for (int i = 0; i < str.length(); i++)
		{
			char c = str.charAt (i);
			if (Character.isLetter(c)
			  ||Character.isDigit(c)
			  ||c== '_') {
			  	if (escape) {
			  		sb.append (ESCAPE_CHAR);
			  		escape = false;
			  	}
				sb.append (c);
			}else {
				if (!escape) {
					sb.append (ESCAPE_CHAR);
					escape = true;
				} else sb.append (ESCAPE_SEP);
				sb.append (Integer.toString((int) c, 16));
			}
		}
		return sb.toString ();
	}

	public String decode (String str) {
		StringBuffer sb = new StringBuffer ();
		boolean escape = false;
		int charval = 0;
		for (int i = 0; i < str.length(); i++)
		{
			char c = str.charAt (i);
			if (escape)
			{
				if (('0' <= c && c <= '9')
				 || ('A' <= c && c <= 'F')
				 || ('a' <= c && c <= 'f')) {
					charval *= 16;
					charval += Character.digit(c, 16);
				} else if (c == ESCAPE_SEP) {
					 sb.append ((char)charval);
				} else if (c == ESCAPE_CHAR) {
					sb.append ((char)charval);
					escape = false;
				} else throw new RuntimeException ("Invalid character "+c);
			} else {
				if (Character.isLetter(c)
				  ||Character.isDigit(c)
				  ||c== '_'
				)
				{
					sb.append (c);
				} else if (c == ESCAPE_CHAR) {
					escape = true;
					charval = 0;
				} else throw new RuntimeException ("Invalid character "+c);
			}
		}
		if (escape) throw new RuntimeException ("Unexpected end of string "+str);
		return sb.toString ();
	}
	
	public String uri (Table table, String base) {
		return base + "T" + SEPARATOR + encode (table.name);
	}

	public String uri (Column column, String base) {
		return base + "c" + SEPARATOR + encode (column.table.name)
		                  + SEPARATOR + encode (column.name);
	}

	public String uri (ForeignKey fk, Table table, String base) {
		return base + "k" + SEPARATOR + encode (table.name)
		                  + SEPARATOR + encode (fk.name);
	}

	public String uri (ForeignKey fk1, ForeignKey fk2, Table table, String base) {
		return base + "p" + SEPARATOR + encode (table.name)
		                  + SEPARATOR + encode (fk1.name)
		                  + SEPARATOR + encode (fk2.name);
	}

	public String uri (java.sql.ResultSet rs, Iterator columnNames, Table table, String base) {
		StringBuffer r = new StringBuffer(base);
		r.append("r").append(SEPARATOR).append(encode (table.name));
		while (columnNames.hasNext()) {
			String value;
			try {
				value = rs.getString(columnNames.next().toString());
			}
			catch (java.sql.SQLException ex) {
				throw new RuntimeException(ex);
			}
            if (value == null) return null;
			r.append(SEPARATOR).append(encode(value));
		}
		return r.toString();
	}
	
	/* (non javadoc)
     * A string tokenizer accepting multicharacters separators. 
	 */
	private class AdvancedStringTokenizer {
		private String str;
		private String separator;
		private int crt = 0;
		
		AdvancedStringTokenizer (String str, String separator) {
			this.str = str;
			this.separator = separator;
		}
		
		void setSeparator (String separator) {
			this.separator = separator;
		}
		
		boolean hasMoreTokens () {
			return (crt != -1);
		}
		
		String nextToken () {
			if (crt == -1) throw new java.util.NoSuchElementException ();
			int next = str.indexOf (separator, crt);
			String r;
			if (next == -1) r = str.substring (crt);
			else r = str.substring (crt, next);
			crt = next + separator.length ();
			return r;
		}
		
		int countTokens () {
			int c = crt;
			int r = 0;
			while (c != -1) {
				r += 1;
				int next = str.indexOf(separator, c);
				if (next == -1) c = -1;
				else c = next + separator.length ();
			}
			return r;
		}		
	}
	
	public Object decodeUri (String uri, DbInfo info) {
		String aboxBase = info.getABoxBaseUri ();
        String tboxBase = info.getTBoxBaseUri ();
		if (uri.startsWith(aboxBase)) {
            uri = uri.substring(aboxBase.length());
        } else if (uri.startsWith(tboxBase)) {
            uri = uri.substring(tboxBase.length());
        } else return null; 
		
		AdvancedStringTokenizer st =
			new AdvancedStringTokenizer (uri, SEPARATOR);
		char type = st.nextToken().charAt (0) ;
		Table table = (Table) info.tables.get (st.nextToken ());
		switch (type) {
			case 'T':
				return table;
			case 'c':
				return table.getColumn (st.nextToken ());
			case 'k':
				return table.foreignKeys.get (st.nextToken ());
			case 'p':
				ForeignKey rl[] = new ForeignKey[2];
				rl[0] = (ForeignKey) table.foreignKeys.get (st.nextToken ());
				rl[1] = (ForeignKey) table.foreignKeys.get (st.nextToken ());
				return rl;
			case 'r':
				String rw[] = new String[st.countTokens () + 1];
				rw[0] = table.name ;
				for (int i=1; st.hasMoreTokens(); i++) {
					rw[i] = st.nextToken ();
				}
				return rw;
		}
		throw new java.lang.IllegalArgumentException (uri);
	}
}
