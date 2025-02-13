package com.cedarsoftware.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static com.cedarsoftware.util.StringUtilities.hashCodeIgnoreCase;

/**
 * Useful Map that does not care about the case-sensitivity of keys
 * when the key value is a String.  Other key types can be used.
 * String keys will be treated case insensitively, yet key case will
 * be retained.  Non-string keys will work as they normally would.
 * <p>
 * The internal CaseInsensitiveString is never exposed externally
 * from this class. When requesting the keys or entries of this map,
 * or calling containsKey() or get() for example, use a String as you
 * normally would.  The returned Set of keys for the keySet() and
 * entrySet() APIs return the original Strings, not the internally
 * wrapped CaseInsensitiveString.
 *
 * As an added benefit, .keySet() returns a case-insenstive
 * Set, however, again, the contents of the entries are actual Strings.
 * Similarly, .entrySet() returns a case-insensitive entry set, such that
 * .getKey() on the entry is case insensitive when compared, but the
 * returned key is a String.
 *
 * @author John DeRegnaucourt (john@cedarsoftware.com)
 *         <br>
 *         Copyright (c) Cedar Software LLC
 *         <br><br>
 *         Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *         <br><br>
 *         http://www.apache.org/licenses/LICENSE-2.0
 *         <br><br>
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */
public class CaseInsensitiveMap<K, V> implements Map<K, V>
{
    private final Map<K, V> map;

    public CaseInsensitiveMap()
    {
        map = new LinkedHashMap<>();
    }

    public CaseInsensitiveMap(int initialCapacity)
    {
        map = new LinkedHashMap<>(initialCapacity);
    }

    /**
     * Wrap the passed in Map with a CaseInsensitiveMap, allowing other Map types like
     * TreeMap, ConcurrentHashMap, etc. to be case insensitive.
     * @param m Map to wrap.
     */
    public CaseInsensitiveMap(Map<K, V> m)
    {
        if (m instanceof TreeMap)
        {
            map = copy(m, new TreeMap());
        }
        else if (m instanceof LinkedHashMap)
        {
            map = copy(m, new LinkedHashMap(m.size()));
        }
        else if (m instanceof ConcurrentSkipListMap)
        {
            map = copy(m, new ConcurrentSkipListMap());
        }
        else if (m instanceof ConcurrentMap)
        {
            map = copy(m, new ConcurrentHashMap(m.size()));
        }
        else if (m instanceof WeakHashMap)
        {
            map = copy(m, new WeakHashMap(m.size()));
        }
        else
        {
            map = copy(m, new LinkedHashMap(m.size()));
        }
    }

    protected Map<K, V> copy(Map<K, V> source, Map dest)
    {
        for (Map.Entry<K, V> entry : source.entrySet())
        {
            // Get get from Entry, leaving it in it's original state (in case the key is a CaseInsensitiveString)
            Object key;
            if (entry instanceof CaseInsensitiveEntry)
            {
                key = ((CaseInsensitiveEntry)entry).getOriginalKey();
            }
            else
            {
                key = entry.getKey();
            }

            // Wrap any String keys with a CaseInsensitiveString.  Keys that were already CaseInsensitiveStrings will
            // remain as such.
            K altKey;
            if (key instanceof String)
            {
                altKey = (K) new CaseInsensitiveString((String)key);
            }
            else
            {
                altKey = (K)key;
            }

            dest.put(altKey, entry.getValue());
        }
        return dest;
    }

    public CaseInsensitiveMap(int initialCapacity, float loadFactor)
    {
        map = new LinkedHashMap<>(initialCapacity, loadFactor);
    }

    public V get(Object key)
    {
        if (key instanceof String)
        {
            String keyString = (String) key;
            return map.get(new CaseInsensitiveString(keyString));
        }
        return map.get(key);
    }

    public V put(K key, V value)
    {
        if (key instanceof String)
        {    // Must remove entry because the key case can change
            final CaseInsensitiveString newKey = new CaseInsensitiveString((String) key);
            return map.put((K) newKey, value);
        }
        return map.put(key, value);
    }

    public boolean containsKey(Object key)
    {
        if (key instanceof String)
        {
            String keyString = (String) key;
            return map.containsKey(new CaseInsensitiveString(keyString));
        }
        return map.containsKey(key);
    }

    public void putAll(Map<? extends K, ? extends V> m)
    {
        if (m == null)
        {
            return;
        }

        for (Map.Entry entry : m.entrySet())
        {
            if (entry instanceof CaseInsensitiveEntry)
            {
                CaseInsensitiveEntry ciEntry = (CaseInsensitiveEntry) entry;
                put((K) ciEntry.getOriginalKey(), (V) entry.getValue());
            }
            else
            {
                put((K) entry.getKey(), (V) entry.getValue());
            }
        }
    }

    public V remove(Object key)
    {
        if (key instanceof String)
        {
            String keyString = (String) key;
            return map.remove(new CaseInsensitiveString(keyString));
        }
        return map.remove(key);
    }

    // delegates
    public int size()
    {
        return map.size();
    }

    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    public boolean equals(Object other)
    {
        if (other == this) return true;
        if (!(other instanceof Map)) return false;

        Map<?, ?> that = (Map<?, ?>) other;
        if (that.size() != size())
        {
            return false;
        }

        for (Map.Entry entry : that.entrySet())
        {
            final Object thatKey = entry.getKey();
            if (!containsKey(thatKey))
            {
                return false;
            }

            Object thatValue = entry.getValue();
            Object thisValue = get(thatKey);

            if (thatValue == null || thisValue == null)
            {   // Perform null checks
                if (thatValue != thisValue)
                {
                    return false;
                }
            }
            else if (!thisValue.equals(thatValue))
            {
                return false;
            }
        }
        return true;
    }

    public int hashCode()
    {
        int h = 0;
        for (Map.Entry<K, V> entry : map.entrySet())
        {
            Object key = entry.getKey();
            int hKey = key == null ? 0 : key.hashCode();
            Object value = entry.getValue();
            int hValue = value == null ? 0 : value.hashCode();
            h += hKey ^ hValue;
        }
        return h;
    }

    public String toString()
    {
        return map.toString();
    }

    public void clear()
    {
        map.clear();
    }

    public boolean containsValue(Object value)
    {
        return map.containsValue(value);
    }

    public Collection<V> values()
    {
        return map.values();
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     */
    public Set<K> keySet()
    {
        return new LocalSet();
    }

    public Map<K, V> getWrappedMap()
    {
        return map;
    }

    private class LocalSet extends AbstractSet<K>
    {
        final Map<K, V> localMap = CaseInsensitiveMap.this;
        Iterator iter;

        public LocalSet()
        { }

        public boolean contains(Object o)
        {
            return localMap.containsKey(o);
        }

        public boolean remove(Object o)
        {
            final int size = map.size();
            localMap.remove(o);
            return map.size() != size;
        }

        public boolean removeAll(Collection c)
        {
            int size = map.size();

            for (Object o : c)
            {
                if (contains(o))
                {
                    remove(o);
                }
            }
            return map.size() != size;
        }

        public boolean retainAll(Collection c)
        {
            Map other = new CaseInsensitiveMap();
            for (Object o : c)
            {
                other.put(o, null);
            }

            final int size = map.size();
            Iterator<Map.Entry<K, V>> i = map.entrySet().iterator();
            while (i.hasNext())
            {
                Map.Entry<K, V> entry = i.next();
                if (!other.containsKey(entry.getKey()))
                {
                    i.remove();
                }
            }

            return map.size() != size;
        }

        public boolean add(K o)
        {
            throw new UnsupportedOperationException("Cannot add() to a 'view' of a Map.  See JavaDoc for Map.keySet()");
        }

        public boolean addAll(Collection c)
        {
            throw new UnsupportedOperationException("Cannot addAll() to a 'view' of a Map.  See JavaDoc for Map.keySet()");
        }

        public Object[] toArray()
        {
            Object[] items = new Object[size()];
            int i=0;
            for (Object key : map.keySet())
            {
                items[i++] = key instanceof CaseInsensitiveString ? key.toString() : key;
            }
            return items;
        }

        public <T> T[] toArray(T[] a)
        {
            if (a.length < size())
            {
                // Make a new array of a's runtime type, but my contents:
                return (T[]) Arrays.copyOf(toArray(), size(), a.getClass());
            }
            System.arraycopy(toArray(), 0, a, 0, size());
            if (a.length > size())
            {
                a[size()] = null;
            }
            return a;
        }

        public int size()
        {
            return map.size();
        }

        public boolean isEmpty()
        {
            return map.isEmpty();
        }

        public void clear()
        {
            map.clear();
        }

        public int hashCode()
        {
            int h = 0;

            // Use map.keySet() so that we walk through the CaseInsensitiveStrings generating a hashCode
            // that is based on the lowerCase() value of the Strings (hashCode() on the CaseInsensitiveStrings
            // with map.keySet() will return the hashCode of .toLowerCase() of those strings).
            for (Object key : map.keySet())
            {
                if (key != null)
                {
                    h += key.hashCode();
                }
            }
            return h;
        }

        public Iterator<K> iterator()
        {
            iter = map.keySet().iterator();
            return new Iterator<K>()
            {
                public boolean hasNext()
                {
                    return iter.hasNext();
                }

                public K next()
                {
                    Object next = iter.next();
                    if (next instanceof CaseInsensitiveString)
                    {
                        next = next.toString();
                    }
                    return (K) next;
                }

                public void remove()
                {
                    iter.remove();
                }
            };
        }
    }

    public Set<Map.Entry<K, V>> entrySet()
    {
        return new EntrySet<>();
    }

    private class EntrySet<E> extends LinkedHashSet<E>
    {
        final Map<K, V> localMap = CaseInsensitiveMap.this;
        Iterator<Map.Entry<K, V>> iter;

        EntrySet() { }

        public int size()
        {
            return map.size();
        }

        public boolean isEmpty()
        {
            return map.isEmpty();
        }

        public void clear()
        {
            map.clear();
        }

        public boolean contains(Object o)
        {
            if (!(o instanceof Map.Entry))
            {
                return false;
            }

            Map.Entry that = (Map.Entry) o;
            if (localMap.containsKey(that.getKey()))
            {
                Object value = localMap.get(that.getKey());
                if (value == null)
                {
                    return that.getValue() == null;
                }
                return value.equals(that.getValue());
            }
            return false;
        }

        public boolean remove(Object o)
        {
            final int size = map.size();
            Map.Entry that = (Map.Entry) o;
            localMap.remove(that.getKey());
            return map.size() != size;
        }

        /**
         * This method is required.  JDK method is broken, as it relies
         * on iterator solution.  This method is fast because contains()
         * and remove() are both hashed O(1) look ups.
         */
        public boolean removeAll(Collection c)
        {
            final int size = map.size();

            for (Object o : c)
            {
                if (contains(o))
                {
                    remove(o);
                }
            }
            return map.size() != size;
        }

        public boolean retainAll(Collection c)
        {
            // Create fast-access O(1) to all elements within passed in Collection
            Map other = new CaseInsensitiveMap();
            for (Object o : c)
            {
                if (o instanceof Map.Entry)
                {
                    other.put(((Map.Entry)o).getKey(), ((Map.Entry) o).getValue());
                }
            }

            int origSize = size();

            // Drop all items that are not in the passed in Collection
            Iterator<Map.Entry<K,V>> i = map.entrySet().iterator();
            while (i.hasNext())
            {
                Map.Entry<K, V> entry = i.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                if (!other.containsKey(key))
                {   // Key not even present, nuke the entry
                    i.remove();
                }
                else
                {   // Key present, now check value match
                    Object v = other.get(key);
                    if (v == null)
                    {
                        if (value != null)
                        {
                            i.remove();
                        }
                    }
                    else
                    {
                        if (!v.equals(value))
                        {
                            i.remove();
                        }
                    }
                }
            }

            return size() != origSize;
        }

        public boolean add(E o)
        {
            throw new UnsupportedOperationException("Cannot add() to a 'view' of a Map.  See JavaDoc for Map.entrySet()");
        }

        public boolean addAll(Collection c)
        {
            throw new UnsupportedOperationException("Cannot addAll() to a 'view' of a Map.  See JavaDoc for Map.entrySet()");
        }

        public Iterator<E> iterator()
        {
            iter = map.entrySet().iterator();
            return new Iterator<E>()
            {
                public boolean hasNext()
                {
                    return iter.hasNext();
                }

                public E next()
                {
                    return (E) new CaseInsensitiveEntry<>(iter.next());
                }

                public void remove()
                {
                    iter.remove();
                }
            };
        }
    }

    /**
     * Entry implementation that will give back a String instead of a CaseInsensitiveString
     * when .getKey() is called.
     *
     * Also, when the setValue() API is called on the Entry, it will 'write thru' to the
     * underlying Map's value.
     */
    public class CaseInsensitiveEntry<KK, VV> extends AbstractMap.SimpleEntry<KK, VV>
    {
        public CaseInsensitiveEntry(Map.Entry<KK, VV> entry)
        {
            super(entry);
        }

        public KK getKey()
        {
            KK superKey = super.getKey();
            if (superKey instanceof CaseInsensitiveString)
            {
                return (KK) superKey.toString();
            }
            return superKey;
        }

        public KK getOriginalKey()
        {
            return super.getKey();
        }

        public VV setValue(VV value)
        {
            return (VV) map.put((K)super.getKey(), (V)value);
        }
    }

    /**
     * Class used to wrap String keys.  This class ignores the
     * case of Strings when they are compared.  Based on known usage,
     * null checks, proper instance, etc. are dropped.
     */
    static final class CaseInsensitiveString implements Comparable
    {
        private final String caseInsensitiveString;
        private final int hash;

        protected CaseInsensitiveString(String string)
        {
            caseInsensitiveString = string;
            hash = hashCodeIgnoreCase(string);  // no new String created unlike .toLowerCase()
        }

        public String toString()
        {
            return caseInsensitiveString;
        }

        public int hashCode()
        {
            return hash;
        }

        public boolean equals(Object other)
        {
            if (other == this)
            {
                return true;
            }
            else if (other instanceof CaseInsensitiveString)
            {
                return hash == ((CaseInsensitiveString)other).hash &&
                        caseInsensitiveString.equalsIgnoreCase(((CaseInsensitiveString)other).caseInsensitiveString);
            }
            else if (other instanceof String)
            {
                return caseInsensitiveString.equalsIgnoreCase((String)other);
            }
            return false;
        }

        public int compareTo(Object o)
        {
            if (o instanceof CaseInsensitiveString)
            {
                CaseInsensitiveString other = (CaseInsensitiveString) o;
                return caseInsensitiveString.compareToIgnoreCase(other.caseInsensitiveString);
            }
            else if (o instanceof String)
            {
                String other = (String)o;
                return caseInsensitiveString.compareToIgnoreCase(other);
            }
            else
            {   // Strings are less than non-Strings (come before)
                return -1;
            }
        }
    }
}
