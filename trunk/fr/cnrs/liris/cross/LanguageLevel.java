/*
 * Created on Aug 13, 2004
 *
 */
package fr.cnrs.liris.cross;

/**
 * @author Pierre-Antoine Champin
 *
 * <p>This abstract class provides constants describing the different language
 * levels cross is able to produce. They are integers, and the natural order of
 * integers matches the inclusion order of the language levels (that is,
 * <code>OWL_LITE &lt; OWL_FULL</code>, for example.</p>
 * <p>Note that OWL DL is not present because the only non-Lite features that
 * Cross may produce are in OWL Full.</p> 
 * @see fr.cnrs.liris.cross.TBoxGraph#languageLevel
 */
public abstract class LanguageLevel {
    
    /* No longer used 
     **
     * OWL Lite language level, with some restrictions to fit some limitations
     * of the racer engine.
     *
    public static int OWL_LITE_RACER = 0;
    */
    
    /**
     * OWL Lite language level.
     */
    public static int OWL_LITE = 1; 
    /**
     * OWL Full language level.
     */
    public static int OWL_FULL = 2; 
    /**
     * The default language level, namely OWL_LITE.
     * @see #OWL_LITE
     */
    public static int DEFAULT = OWL_LITE; 
}
