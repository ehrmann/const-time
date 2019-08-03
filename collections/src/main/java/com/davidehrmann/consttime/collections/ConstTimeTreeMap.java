package com.davidehrmann.consttime.collections;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ConstTimeTreeMap<K, V> extends AbstractMap<K,V> {

    private static final TreeNode EMPTY_TREE_NODE = new TreeNode(null, null);

    private volatile int maxDepth = 0;
    private volatile int size = 0;
    private volatile TreeNode<K, V> root = EMPTY_TREE_NODE;

    private final Comparator<? super K> comparator;

    public ConstTimeTreeMap(Comparator<? super K> comparator) {
        this.comparator = Objects.requireNonNull(comparator);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public V get(Object key) {
        TreeNode<K, V> root = this.root;
        for (int i = maxDepth - 1; i >= 0; --i) {
            root = root.children[comparator.compare(root.key, (K) key) + 1];
        }

        return root.value;
    }

    @Override
    public V put(K key, V value) {
        return super.put(key, value);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

    private TreeNode<K, V> getTreeNode(K key) {

    }

    private void addTreeNode(TreeNode<K, V> node) {

    }

    private void removeTreeNode(TreeNode<K, V> node) {

    }

    private static class TreeNode<K, V> {
        // -1, 0, 1
        // References are volatile to discourage caching
        private volatile TreeNode[] children = new TreeNode[3];
        private volatile K key;
        private volatile V value;

        TreeNode(K key, V value) {
            children[1] = this;
            this.key = key;
            this.value = value;
        }
    }

    private class EntrySet extends AbstractSet<Entry<K, V>> {

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return null;
        }

        @Override
        public int size() {
            return ConstTimeTreeMap.this.size;
        }

        @Override
        public boolean contains(Object o) {

        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }

            Map.Entry<K, V> entry = (Map.Entry<K, V>) o;


            TreeNode<K, V> node = ConstTimeTreeMap.this.getTreeNode(entry.getKey());
            if (node != null) {

            }
            for (int i = maxDepth - 1; i >= 0; --i) {
                root = root.children[comparator.compare(root.key, entry.getKey()) + 1];
            }

            if (root.key != null) {
                //

                return true;
            }

            return false;
        }

        @Override
        public void clear() {
            super.clear();
        }
    }
}
