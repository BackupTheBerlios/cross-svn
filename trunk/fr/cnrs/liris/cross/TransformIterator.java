/*
 * Created on Aug 9, 2004
 *
 */
package fr.cnrs.liris.cross;

import java.util.Iterator;

/**
 * @author Pierre-Antoine Champin
 *
 * An iterator wrapping an existing iterator, but transforming the returned
 * values through its method <code>transform</code>.
 * 
 */
abstract class TransformIterator implements Iterator {
    
    private Iterator source;
    
    /**
     * Construct a TransformIterator wrapping the given source Iterator.
     * 
     * @param source the source Iterator
     */
    public TransformIterator (Iterator source) {
        this.source = source;
    }
    
    /**
     * Override this method to implement the way your TransformIterator will
     * transform the objects returned by the source Iterator.
     * 
     * @param o the object returned by the source Iterator.
     * @return the transformed object
     */
    public abstract Object transform (Object o);

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        source.remove();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        return source.hasNext();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Object next() {
        return transform(source.next());
    }

}
