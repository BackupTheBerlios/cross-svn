import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import fr.cnrs.liris.cross.DbInfo;
import fr.cnrs.liris.cross.ABoxGraph;
import fr.cnrs.liris.cross.TBoxGraph;

/*
 * Created on Jul 13, 2004
 *
 */

/**
 * @author Pierre-Antoine Champin
 *
 * A command line program to dump the OWL graphs generated from a JDBC source. 
 */
public class dump {
	
	public static final int ABOX = 0;
	public static final int TBOX = 1;
	
	static int getType (String arg) {
		if (arg.equals ("abox")) {
			return ABOX;
		} else if (arg.equals ("tbox")) {
			return TBOX;
		} else {
			throw new IndexOutOfBoundsException ();
		}
	}
	
	private static void help () {
		System.err.println(
"Dumps the ABox for the given JDBC source to standard output.\n"+
"usage: dump <abox|tbox> <jdbc_url> [options]\n"+
"  options: -d <driver>: JDBC driver name (can also be passed to JVM with\n" +"                        -Djdbc.drivers)\n"+
"           -u <username>: database connexion username\n"+
"           -p <passwd>: database connexion password\n"+
"           -t <url>: base URI of the TBox\n"+
"           -a <url>: base URI of the ABox\n"+
"           -i <url>: URI of the imported TBox\n"+
"           -s <syntax>: jena style syntax for the reuslting RDF\n"+
"           -v <level>: set the verbosity level\n"+
""
		);
	}
	
    /**
     * Takes DbInfo.Parameters, create the DInfo, the graph of the required
     * type and an RDF model wrapping this graph, then return this model.
     * @param p the parameters
     * @param type either TBOX or ABOX
     * @return an RDF model
     * @throws ClassNotFoundException
     * @throws java.sql.SQLException
     * @see #ABOX
     * @see #TBOX
     */
	public static Model makeModel (DbInfo.Parameters p, int type)
	throws ClassNotFoundException, java.sql.SQLException {
		DbInfo index = new DbInfo (p);
		Graph g = null;
		switch (type) {
			case ABOX:
				g = new ABoxGraph (index);
				break;
			case TBOX:
				g = new TBoxGraph (index);
				break;
		}
		Model m = ModelFactory.createModelForGraph (g);
		
		m.setNsPrefix ("rdf",  RDF.getURI ());
		m.setNsPrefix ("rdfs", RDFS.getURI ());
		m.setNsPrefix ("xsd",  "http://www.w3.org/2001/XMLSchema#");
		m.setNsPrefix ("owl",  OWL.getURI ());
		m.setNsPrefix ("",     index.getTBoxBaseUri());
        m.setNsPrefix ("i",  index.getABoxBaseUri());
		
        return m;
	}

	public static void main(String[] args) throws Exception {
		int argc = args.length;
		DbInfo.Parameters p = new DbInfo.Parameters ();
		String syntax = "RDF/XML-ABBREV";
		int type = ABOX;
		try {
			type = getType (args[0]);
			p.jdbcUrl = args[1];
			for (int i=2; i<argc; i++) {
				String a = args[i];
				if (!a.startsWith("-")) throw new IndexOutOfBoundsException();
				i += 1;
				switch (a.charAt(1)) {
					case 'd':
						p.jdbcDriver = args[i]; break;
					case 'u':
						p.jdbcUsername = args[i]; break;
					case 'p':
						p.jdbcPassword = args[i]; break;
					case 't':
                        p.tboxBaseUri = args[i]; break;
                    case 'a':
                        p.aboxBaseUri = args[i]; break;
                    case 'i':
                        p.importedTboxUri = args[i]; break;
					case 's':
						syntax = args[i]; break; 
                    case 'v':
                        p.verbosity = Integer.parseInt(args[i]); break; 
				}
			}
		}
		catch (IndexOutOfBoundsException ex) {
			help ();
			System.exit (-1);
		}

		makeModel (p, type).write(System.out, syntax);
	}
}
