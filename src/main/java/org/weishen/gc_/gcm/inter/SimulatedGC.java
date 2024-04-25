package org.weishen.gc_.gcm.inter;
import java.util.List;


/**
 * 定义了模拟垃圾回收器的行为，该回收器可以管理类型为 T 的对象。
 *
 * @param <T> 此垃圾回收器处理的对象类型，通常是模拟环境中的节点或对象。
 */
public interface SimulatedGC<T> {
    /**
     * 执行完整的垃圾回收周期，包括标记和清扫两个阶段。
     */
    default void gc(){
        mark();
        collect();
    }

    /**
     * 标记阶段：从根对象开始标记所有可达对象，以防止它们被回收。
     * mark() 的具体实现由子类提供 它是一种算法的实现
     * 它可以是串行的 @org.weishen.gc_.gcm.ThreeColourSerialUnSafeGraph
     * 它也可以是并行的  //NO impl
     *
     */
    void mark();

    /**
     * 收集阶段：处理未被标记的对象，可能包括清扫、压缩或复制。
     */
    void collect();

    /**
     * 将一个新对象注册到垃圾回收管理中。
     *
     * @param obj 要注册的对象。
     */
    void register(T obj);

    /**
     * 建立一个对象到其他一个或多个对象的引用关系。
     *
     * @param current 当前对象。
     * @param next 当前对象所引用的一个或多个对象。
     */
    void register(Object current ,Object...next);

    /**
     * 断开一个根对象的所有引用，通常在该根对象不再被程序使用时调用。
     *
     * @param root 要断开引用的根对象。
     */
    void disconnect(Object root);

    /**
     * 断开一个根对象的所有引用，并且回收此root , 通常在该根对象不再被程序使用时调用
     * @param root 要断开引用的根对象
     */
    void disconnectAndRecycle(Object root);

    /**
     * 获取所有根对象的列表。
     *
     * @return 根对象列表。
     */
    List<T> getRootObjs();

    /**
     * 返回垃圾回收算法的安全执行时间，当前返回0，表示没有特定延迟。
     *
     * @return 垃圾回收算法的安全执行时间。
     */
    long safeTime();

}
