package org.weishen.gc_.context;

import org.weishen.gc_.gcm.ThreeColourSerialUnSafeGraph;
import org.weishen.gc_.gcm.ThreeColourNode;
import org.weishen.gc_.heap.JVMArrayGenerationHeap;
import org.weishen.gc_.heap.inter.SimulatedHeap;
import org.weishen.gc_.obj_.inter.SimulatedObj;

import java.lang.reflect.Constructor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * AppContext 类是整个模拟环境的中心枢纽，负责协调内存管理和垃圾收集（GC）的整体工作流程。
 * 它封装了对象的创建、内存分配和GC触发的逻辑，提供了一个统一的接口来管理模拟环境中的资源和生命周期。
 * <p>
 * 主要职责包括：
 * - 管理对象的生命周期，包括创建和销毁。
 * - 触发和协调垃圾收集过程。
 * - 维护和管理GC Root集合及其与对象间的引用关系。
 * <p>
 * 通过将对象创建和GC触发的逻辑上移至AppContext层，实现了与底层内存管理逻辑的解耦，
 * 使得内存管理（Heap）专注于内存空间的分配和回收，而垃圾收集逻辑可以根据应用层的需求灵活触发，
 * 提高了系统设计的灵活性和可维护性。
 */
public class AppContext {

    private static final Logger logger = Logger.getLogger(AppContext.class.getName());

    /**
     * 支持Stop-The-World (STW) 的全局应用锁。
     * 用于模拟GC事件触发时，整个应用暂停的行为，确保在GC执行期间内存状态的一致性。
     * 读锁用于正常操作时保护内存操作，写锁用于GC操作，确保GC执行期间，不会有新的内存分配或对象创建。
     */
    private final ReentrantReadWriteLock stwLockOfApp = new ReentrantReadWriteLock();

    private final ThreeColourSerialUnSafeGraph gcGraph = new ThreeColourSerialUnSafeGraph();

    // 模拟的堆，负责底层的内存分配和管理。
    private final SimulatedHeap simulatedHeap;

    // 私有构造方法
    private AppContext(SimulatedHeap simulatedHeap) {
        this.simulatedHeap = simulatedHeap;
    }

    // 静态内部类实现单例模式
    private static class SingletonHolder {
        // 在SingletonHolder被加载时，单例会被初始化
        private static final AppContext INSTANCE = new AppContext(new JVMArrayGenerationHeap(Integer.MAX_VALUE));
    }

    public static void gc() {
        logger.info("GC Before : " + getSimulatedHeap().getHeapDetails());
        getGCGraph().gc();
        logger.info("GC Later : " + getSimulatedHeap().getHeapDetails());
    }

    // 公有静态方法，提供全局访问点
    public static AppContext getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static ThreeColourSerialUnSafeGraph getGCGraph() {
        return SingletonHolder.INSTANCE.gcGraph;
    }


    public static SimulatedHeap getSimulatedHeap() {
        return SingletonHolder.INSTANCE.simulatedHeap;
    }

    public Lock getAppReadLock() {
        System.out.println("获取了锁");
        return this.stwLockOfApp.readLock();
    }

    public Lock getAppWriterLock() {
        System.out.println("释放了锁");
        return this.stwLockOfApp.writeLock();
    }


    public static <T> T new_(Class<T> clazz, Object... constructorArgs) {
        return new_(clazz, false, constructorArgs);
    }

    public static <T> T newRoot_(Class<T> clazz, Object... constructorArgs) {
        return new_(clazz, true, constructorArgs);
    }

    private static <T> T new_(Class<T> clazz, boolean isRoot, Object... constructorArgs) {
        T instance;
        SimulatedHeap hp = getInstance().simulatedHeap;
        try {
            // 将参数类型转换为 Class 对象数组
            Class<?>[] parameterTypes = new Class[constructorArgs.length];
            for (int i = 0; i < constructorArgs.length; i++) {
                parameterTypes[i] = constructorArgs[i].getClass();
            }
            // 获取匹配的构造函数
            Constructor<T> constructor = clazz.getConstructor(parameterTypes);
            //  创建实例
            instance = constructor.newInstance(constructorArgs);
            if (hp instanceof JVMArrayGenerationHeap h) {
                h.memSet(instance, JVMArrayGenerationHeap.EDEN_);
            }
            if (instance instanceof SimulatedObj so) {
                so.setIsRoot(isRoot);
                getGCGraph().register(new ThreeColourNode(clazz.getName(), so));
            }

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    /**
     * 查找与提供的参数匹配的构造函数。
     *
     * @param clazz           要创建的对象类
     * @param constructorArgs 构造函数的参数
     * @return 匹配的构造函数
     * @throws NoSuchMethodException 如果没有找到匹配的构造函数
     */
    private static <T> Constructor<T> findSuitableConstructor(Class<T> clazz, Object... constructorArgs) throws NoSuchMethodException {
        // 实现略，基于constructorArgs查找合适的构造函数
        return null;
    }
}
