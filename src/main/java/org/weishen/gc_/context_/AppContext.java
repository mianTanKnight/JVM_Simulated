package org.weishen.gc_.context_;

import org.weishen.gc_.gcm_.SimulatedGC;
import org.weishen.gc_.heap_.JVMArrayGenerationHeap;
import org.weishen.gc_.heap_.SimulatedHeap;
import org.weishen.gc_.obj_.ObjTest;
import org.weishen.gc_.obj_.SimulatedObject;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    /**
     * 支持Stop-The-World (STW) 的全局应用锁。
     * 用于模拟GC事件触发时，整个应用暂停的行为，确保在GC执行期间内存状态的一致性。
     * 读锁用于正常操作时保护内存操作，写锁用于GC操作，确保GC执行期间，不会有新的内存分配或对象创建。
     */
    private final ReentrantReadWriteLock stwLockOfApp = new ReentrantReadWriteLock();

    // 模拟的堆，负责底层的内存分配和管理。
    private final SimulatedHeap simulatedHeap;

    // 可能有多个垃圾收集策略，这里通过列表管理。
    private final List<SimulatedGC> gcs;

    // 私有构造方法
    private AppContext(SimulatedHeap simulatedHeap, List<SimulatedGC> gcs) {
        this.simulatedHeap = simulatedHeap;
        this.gcs = gcs;
    }

    // 静态内部类实现单例模式
    private static class SingletonHolder {
        // 在SingletonHolder被加载时，单例会被初始化
        private static final AppContext INSTANCE = new AppContext(new JVMArrayGenerationHeap(Integer.MAX_VALUE), null);
    }

    // 公有静态方法，提供全局访问点
    public static AppContext getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // 将new_方法改造为静态方法
    public static <T extends SimulatedObject> T new_(Class<T> clazz, Object... constructorArgs) throws Exception {
        // 通过单例获取heap实例
        JVMArrayGenerationHeap heap = (JVMArrayGenerationHeap) getInstance().simulatedHeap;

        // 查找匹配的构造函数
        Constructor<T> constructor = null;
        for (Constructor<?> ctor : clazz.getConstructors()) {
            if (ctor.getParameterTypes().length == constructorArgs.length) {
                constructor = (Constructor<T>) ctor;
                break;
            }
        }
        if (constructor == null) {
            throw new NoSuchMethodException("No suitable constructor found for " + clazz);
        }

        // 创建实例并分配内存
        T instance = constructor.newInstance(constructorArgs);
        heap.memSet(instance, JVMArrayGenerationHeap.EDEN_);

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


    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 100; i++) {
            ObjTest objTest = new_(ObjTest.class, "zs");
            System.out.println(objTest.getName());
        }
//        Random random = new Random();
//        List<Long> ca = new ArrayList<>();
//        for (int i = 0; i < 1000; i++) {
//            int size = (random.nextInt(1000) + 7) & ~7;
//            int point = jvmArrayGenerationHeap.allocate(size, EDEN_);
//            long pointAndSize = ((long) point << 32) | size;
//            ca.add(pointAndSize);
//        }
//        // 释放第25个分配的内存块
//        long pointAndSize = ca.get(25);
//        int point = (int) (pointAndSize >> 32); // 取出point
//        int size = (int) (pointAndSize & 0xFFFFFFFFL); // 取出size
//        jvmArrayGenerationHeap.free(point, size);
//        //再free 26 结果应该是mrage 成功
//        long pointAndSize1 = ca.get(26);
//        int point1 = (int) (pointAndSize1 >> 32); // 取出point
//        int size1 = (int) (pointAndSize1 & 0xFFFFFFFFL); // 取出size
//        jvmArrayGenerationHeap.free(point1, size1);
//        //再free 24 结果应该是mrage 成功
//        long pointAndSize2 = ca.get(24);
//        int point2 = (int) (pointAndSize2 >> 32); // 取出point
//        int size2 = (int) (pointAndSize2 & 0xFFFFFFFFL); // 取出size
//        jvmArrayGenerationHeap.free(point2, size2);
//
//        System.out.println(jvmArrayGenerationHeap.getHeapDetails());
//
//        //尝试复用已回收的缓冲
//        jvmArrayGenerationHeap.allocate((16) & ~7, EDEN_);
//
//        System.out.println(jvmArrayGenerationHeap.getHeapDetails());
//
//        //把 500 move 到 sv1中
//        long pointAndSize3 = ca.get(500);
//        int point3 = (int) (pointAndSize3 >> 32); // 取出point
//        int size3 = (int) (pointAndSize3 & 0xFFFFFFFFL); // 取出size
//        int allocate = jvmArrayGenerationHeap.allocate(size3, SV1_);
//        jvmArrayGenerationHeap.move(point3, allocate, size3);
//
//        System.out.println(jvmArrayGenerationHeap.getHeapDetails());

    }
}
