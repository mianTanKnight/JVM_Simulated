package org.weishen.gc_.gcm.inter;


import java.util.Set;
/**
 * 定义了一个可以参与垃圾回收模拟的节点应具备的引用管理功能。
 *
 * Methods:
 * - addReference: 向当前节点添加一个或多个直接引用。
 * - getReference: 获取当前节点所有直接引用的集合。
 */
public interface ReferenceGC {

    /**
     * 为当前节点添加对一个或多个其他节点的引用。
     *
     * @param references 一个或多个当前节点将依赖的节点，表示这些节点被当前节点直接引用。
     */
    void addReference(ReferenceGC... references);

    /**
     * 获取当前节点直接引用的所有节点。
     *
     * @return 一个包含所有被当前节点直接引用的节点的集合。
     */
    Set<ReferenceGC> getReference();

}