/*
 * Created on Jul 13, 2004
 *
 */

import com.hp.hpl.jena.rdf.model.Model;

import fr.cnrs.liris.cross.DbInfo;

/**
 * @author Pierre-Antoine Champin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Test {

	public static void main(String[] args) throws Exception {
		
		DbInfo.Parameters p = new DbInfo.Parameters ();
		p.jdbcDriver = "org.postgresql.Driver";
		p.jdbcUrl = "jdbc:postgresql://localhost/cross_employee";
		p.jdbcUsername = "pa";
		//p.jdbcPassword = "toto";
		p.schemaPattern = "%";
		
        
		Model m = dump.makeModel (p, dump.TBOX);
        m.write(System.out, "N3");
		
	}
}
