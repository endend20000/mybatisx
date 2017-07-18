package org.cabbage.mybatisx.core.utils;

import java.util.Collection;
import java.util.Iterator;

/**
 * 
 * @author GeZhangyuan
 *
 */
public class CollectionUtils{
	
	/**
	 * 获取集合最后的元素
	 * @param list
	 * @return
	 */
	public static <T> T getLast(final Collection<T> coll){
		if(coll==null){
			return null;
		}
		Iterator<T> iterator=coll.iterator();
		
		T item = null;
		while(iterator.hasNext()) {
			item=(T) iterator.next();
		}
		return item;
	}
	
    //-----------------------------------------------------------------------
    /**
     * Null-safe check if the specified collection is empty.
     * <p>
     * Null returns true.
     *
     * @param coll  the collection to check, may be null
     * @return true if empty or null
     * @since 3.2
     */
    public static boolean isEmpty(final Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    /**
     * Null-safe check if the specified collection is not empty.
     * <p>
     * Null returns false.
     *
     * @param coll  the collection to check, may be null
     * @return true if non-null and non-empty
     * @since 3.2
     */
    public static boolean isNotEmpty(final Collection<?> coll) {
        return !isEmpty(coll);
    }
}
