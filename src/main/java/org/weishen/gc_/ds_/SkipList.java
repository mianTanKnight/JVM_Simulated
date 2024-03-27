package org.weishen.gc_.ds_;


import java.util.Random;

public class SkipList<T> {

    private static final double P = 0.5;
    private static final int MAX_LEVEL = 16;

    private final SkipListNode<T> header = new SkipListNode<>(Integer.MIN_VALUE, null, MAX_LEVEL);
    private int level = 0;
    private final Random random = new Random();

    static class SkipListNode<T> {
        int key;
        T value;
        SkipListNode<T>[] forward;

        @SuppressWarnings("unchecked")
        public SkipListNode(int key, T value, int level) {
            this.key = key;
            this.value = value;
            this.forward = new SkipListNode[level + 1];
        }

        public String toString() {
            return String.format("%d", key);
        }
    }
    @SuppressWarnings("unchecked")
    public void insert(int key, T value) {
        SkipListNode<T>[] update = new SkipListNode[MAX_LEVEL + 1];
        SkipListNode<T> current = header;

        for (int i = level; i >= 0; i--) {
            while (current.forward[i] != null && current.forward[i].key < key) {
                current = current.forward[i];
            }
            update[i] = current;
        }

        current = current.forward[0];

        if (current == null || current.key != key) {
            int lvl = randomLevel();

            if (lvl > level) {
                for (int i = level + 1; i <= lvl; i++) {
                    update[i] = header;
                }
                level = lvl;
            }

            SkipListNode<T> newNode = new SkipListNode<>(key, value, lvl);
            for (int i = 0; i <= lvl; i++) {
                newNode.forward[i] = update[i].forward[i];
                update[i].forward[i] = newNode;
            }
            System.out.println("Inserted: " + key + " at level " + lvl);
        }

        printSkipList();
    }

    private int randomLevel() {
        int lvl = 0;
        while (lvl < MAX_LEVEL && random.nextDouble() < P) {
            lvl++;
        }
        return lvl;
    }

    public void printSkipList() {
        System.out.println("SkipList:");
        for (int i = level; i >= 0; i--) {
            System.out.print("Level " + i + ": ");
            SkipListNode<T> node = header.forward[i];
            while (node != null) {
                System.out.print(node.key + " -> ");
//                printForwardPointers(node, i);
                node = node.forward[i];
            }
            System.out.println("null");
        }
    }

//    private void printForwardPointers(SkipListNode<T> node, int level) {
//        for (int i = level; i >= 0; i--) {
//            if (node.forward[i] != null) {
//                System.out.print(node.forward[i].key + " ");
//            } else {
//                System.out.print("null ");
//            }
//        }
//        System.out.print("| ");
//    }



}
