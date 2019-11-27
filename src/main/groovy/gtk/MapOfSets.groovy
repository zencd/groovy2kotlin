package gtk

class MapOfSets<K,V> extends HashMap<K, Set<V>> {
    void addToSet(K key, V value) {
        def set = get(key)
        if (set == null) {
            set = new HashSet<V>()
            put(key, set)
        }
        set.add(value)
    }
}
