package org.autopipes.util;

import java.util.Collection;
import java.util.Comparator;

public class CollectionComparator<T> implements Comparator<Collection<T>> {
    private boolean ascending = true;
//	@Override
	public int compare(final Collection<T> o1, final Collection<T> o2) {
		return ascending ? o1.size() - o2.size() : o2.size() - o1.size();
	}
    public void setAscending(final boolean ascending){
    	this.ascending = ascending;
    }
}
