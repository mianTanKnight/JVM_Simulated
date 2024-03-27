
package org.weishen.gc_.heap_;


import java.io.IOException;

/**
 * 模拟堆的标准接口。
 * 定义了模拟堆应支持的核心操作，包括创建对象、内存分配和释放等。
 */
public interface SimulatedHeap {

    /**
     * 获取模拟堆的总容量。
     *
     * @return 模拟堆的总容量
     */
    long getCapacity();

    /**
     * 释放指定的内存区域。
     *
     * @param point 开始释放的内存地址（指针）
     * @param size  要释放的内存大小
     */
    void free(int point, int size) throws Exception;

    /**
     * 申请内存空间。
     *
     * @param size 申请的内存大小，必须大于0
     * @return 分配的内存地址（指针），如果分配失败返回-1
     * @throws OutOfMemoryError 如果没有足够的内存抛出此异常
     * @throws Exception 可能抛出其他异常
     */
    int allocate(int size) throws OutOfMemoryError, Exception;

    /**
     * 将内存从一个位置移动到另一个位置。
     *
     * @param srcPoint 源内存地址（指针）
     * @param desPoint 目标内存地址（指针）
     * @param size 移动的内存大小
     * @throws Exception 可能抛出移动过程中的异常
     */
    void move(int srcPoint, int desPoint, int size) throws Exception;




    void memSet(Object o, String generation) throws IOException, OutOfMemoryError, Exception;



    String getHeapDetails();
}
