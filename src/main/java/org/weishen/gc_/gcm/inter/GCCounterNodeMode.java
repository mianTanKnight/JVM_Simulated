package org.weishen.gc_.gcm.inter;


import java.util.Set;

/**
 * 代表垃圾回收系统中的节点，模拟了对象之间的引用关系及垃圾回收机制。
 * <p>
 * 引用计数的可达性分析节点支持
 * <p>此接口定义了节点间引用的管理、引用计数的更新、以及模拟节点回收的方法，提供了一种方式来探索和理解垃圾回收的基本原理。</p>
 *
 * <p>引用计数（GCRootCount）反映了一个节点被多少个GC Roots直接或间接引用。
 * 当一个节点的引用计数降至零时，表示它不再可达，因此可以被视为垃圾并进行回收。</p>
 *
 * <ul>
 *   <li>节点不再被需要时（例如，当它不再被任何GC Roots引用时），应被认为是可回收的。</li>
 *   <li>GC Roots是特殊节点，通常是栈中的局部变量或全局静态变量，其引用计数默认为-1，表示它们总是可达的。</li>
 * </ul>
 *
 *
 */
public interface GCCounterNodeMode {

    /**
     * 为当前节点添加对一个或多个其他节点的引用。
     *
     * @param references 一个或多个当前节点将依赖的节点，表示这些节点被当前节点直接引用。
     */
    void addReference(GCCounterNodeMode... references);

    /**
     * 获取当前节点直接引用的所有节点。
     *
     * @return 一个包含所有被当前节点直接引用的节点的集合。
     */
    Set<GCCounterNodeMode> getReference();

    /**
     * 获取当前节点的GC Root引用计数。
     *
     * @return 当前节点的GC Root引用计数。
     */
    int getGCRootCount();

    /**
     * 设置当前节点的GC Root引用计数。
     *
     * @param GCRootCount 新的GC Root引用计数值。
     */
    void setGCRootCount(int GCRootCount);

    /**
     * 增加指定节点及其子节点的GC Root引用计数。
     *
     * @param node         要增加引用计数的节点。
     * @param visitedNodes 访问过的节点集合，用于避免循环引用导致的无限递归。
     * @param GCRootCount  增加的GC Root引用计数值。
     */
    default void increase(GCCounterNodeMode node, Set<GCCounterNodeMode> visitedNodes, int GCRootCount) {
        if (node == null || visitedNodes.contains(node)) {
            // 如果节点为空或已经访问过，直接返回
            return;
        }
        // 将当前节点标记为已访问
        visitedNodes.add(node);
        // 增加当前节点的父计数器
        node.setGCRootCount(this.getGCRootCount() + GCRootCount);
        // 递归地增加所有子节点的引用计数
        for (GCCounterNodeMode child : node.getReference()) {
            child.increase(child, visitedNodes, GCRootCount);
        }
    }

    /**
     * 减少指定节点及其子节点的GC Root引用计数，并在引用计数降至零时触发回收。
     *
     * @param node         要减少引用计数的节点。
     * @param visitedNodes 访问过的节点集合，用于避免循环引用导致的无限递归。
     */
    default void decrease(GCCounterNodeMode node, Set<GCCounterNodeMode> visitedNodes) {
        if (!visitedNodes.contains(node)) {
            visitedNodes.add(node);

            // 直接递减当前节点的引用计数
            this.setGCRootCount(node.getGCRootCount() - 1);

            // 递归地对所有子节点执行相同的操作，递减的量随着深度累加
            for (GCCounterNodeMode child : node.getReference()) {
                child.decrease(child, visitedNodes);
            }
        }
    }
}