package org.weishen.gc_.ds;

import java.util.Random;

public class DoublySkipList<T> {

    public static class SkipListNode<T> {
        int key;
        T value;
        //后索引块
        SkipListNode<T>[] forward;
        //前索引块
        SkipListNode<T>[] backward;

        @SuppressWarnings("unchecked")
        public SkipListNode(int key, T value, int level) {
            this.key = key;
            this.value = value;
            this.forward = new SkipListNode[level + 1];
            this.backward = new SkipListNode[level + 1];
        }

        public SkipListNode<T> getForward() {
            return forward[0];
        }

        public SkipListNode<T> getBackward() {
            return backward[0];
        }

        public int getKey() {
            return key;
        }

        public T getValue() {
            return value;
        }
    }

    private static final double P = 0.5; //百分之50 的晋升率
    private static final int MAX_LEVEL = 16; //控制层数

    private final SkipListNode<T> header;
    private int level;
    private final Random random;

    public SkipListNode<T> getHeader() {
        return header;
    }

    public DoublySkipList() {
        this.header = new SkipListNode<>(Integer.MIN_VALUE, null, MAX_LEVEL);
        this.level = 0;
        this.random = new Random();
    }

    private int randomLevel() {
        int lvl = 0;
        while (lvl < MAX_LEVEL && random.nextDouble() < P) {
            lvl++;
        }
        return lvl;
    }

    public SkipListNode<T> search(int key) {
        SkipListNode<T> current = header;
        for (int i = level; i >= 0; i--) {
            while (current.forward[i] != null && current.forward[i].key < key) {
                current = current.forward[i];
            }
        }
        current = current.forward[0];
        return (current != null && current.key == key) ? current : null;
    }

    @SuppressWarnings("unchecked")
    public SkipListNode insert(int key, T value) {
        SkipListNode<T>[] update = new SkipListNode[MAX_LEVEL + 1];
        SkipListNode<T> current = header;

        for (int i = level; i >= 0; i--) {
            //横向同级遍历
            while (current.forward[i] != null && current.forward[i].key < key) {
                current = current.forward[i];
            }
            update[i] = current;
        }
        current = current.forward[0];
        SkipListNode<T> newNode = null;
        if (current == null || current.key != key) {
            int lvl = randomLevel();
            if (lvl > level) {
                for (int i = level + 1; i <= lvl; i++) {
                    update[i] = header;
                }
                level = lvl;
            }

            newNode = new SkipListNode<>(key, value, lvl);
            for (int i = 0; i <= lvl; i++) {
                newNode.forward[i] = update[i].forward[i];
                update[i].forward[i] = newNode;
                if (newNode.forward[i] != null) {
                    newNode.forward[i].backward[i] = newNode;
                }
                newNode.backward[i] = update[i];
            }
        }
        return newNode;
    }

    @SuppressWarnings("unchecked")
    public void delete(int key) {
        SkipListNode<T>[] update = new SkipListNode[MAX_LEVEL + 1];
        SkipListNode<T> current = header;

        for (int i = level; i >= 0; i--) {
            while (current.forward[i] != null && current.forward[i].key < key) {
                current = current.forward[i];
            }
            update[i] = current;
        }
        current = current.forward[0];

        if (current != null && current.key == key) {
            for (int i = 0; i <= level; i++) {
                if (update[i].forward[i] != current) break;
                update[i].forward[i] = current.forward[i];
                if (current.forward[i] != null) {
                    current.forward[i].backward[i] = update[i];
                }
            }
            while (level > 0 && header.forward[level] == null) {
                level--;
            }
        }
    }
}
