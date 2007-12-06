/**
 * Java WordNet Library (JWNL)
 * See the documentation for copyright information.
 */
package net.didion.jwnl.util.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A fixed-capacity <code>Cache</code> that stores the most recently used elements. Once the cache reaches
 * capacity, the least recently used elements will be removed.
 */
public class LRUCache extends LinkedHashMap implements Cache {
	private int _capacity;

	/**
	 * @param capacity the maximum number of elements that can be contained in the cache.
	 */
	public LRUCache(int capacity) {
		super(capacity);
		setCapacity(capacity);
	}

	public boolean isFull() {
		return size() >= getCapacity();
	}

	protected boolean removeEldestEntry(Map.Entry eldest) {
		return size() > getCapacity();
	}

	public int setCapacity(int capacity) {
		_capacity = capacity;
		return _capacity;
	}

	public int getCapacity() {
		return _capacity;
	}

	public int getSize() {
		return size();
	}
}