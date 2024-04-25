package org.weishen.gc_.obj_.inter;


/**
 * SimulatedObj 是一个通用接口，用于定义模拟对象的基本结构和行为。
 * 这个接口不会被对象直接实现。而是通过ASM技术在运行时隐式地向目标类添加实现，
 * 这样做可以绕过Java的单继承限制，提高代码的灵活性和复用性。
 *
 * 此接口中定义的方法主要用于管理模拟对象的内存指针和大小，以及标记对象是否为GC根。
 * 这些属性和方法在模拟的垃圾回收环境中至关重要，用于确保正确的内存管理和回收策略。
 *
 * 注意：SimulatedObj 接口的实现不参与对象的构造过程，其属性由ASM注入和管理。
 */
public interface SimulatedObj {

    // public int pointer
    // public int size
    // public int aligningSize;
    // public bool isRoot

    /**
     * 设置对象在模拟堆中的起始指针。
     * @param pointer 指向模拟堆中的内存地址。
     */
    void setPointer(int pointer);


    /**
     * 获取对象在模拟堆中的起始指针。
     * @return 对象的内存起始地址。
     */
    int getPointer();


    /**
     * 设置对象的内存大小。
     * @param size 对象占用的字节数。
     */
    void setSize(int size);

    /**
     * 获取对象的内存大小。
     * @return 对象占用的字节数。
     */
    int getSize();

    /**
     * 设置对象的对齐后的内存大小。
     * @param size 对齐后对象占用的字节数。
     */
    void setAligningSize(int size);

    /**
     * 获取对象的对齐后的内存大小。
     * @return 对齐后对象占用的字节数。
     */
    int getAligningSize();

    /**
     * 设置对象是否为垃圾回收的根。
     * @param isRoot 标记对象是否为GC根。
     */
    void setIsRoot(boolean isRoot);

    /**
     * 判断对象是否为垃圾回收的根。
     * @return 如果对象是GC根，则返回true；否则返回false。
     */
    boolean getIsRoot();
    /**
     * 提供模拟对象的详细信息。
     * @return 模拟对象的状态描述字符串。
     */
    String toStringS();

}
