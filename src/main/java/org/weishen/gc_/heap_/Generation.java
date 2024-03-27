package org.weishen.gc_.heap_;


/**
 * 分代接口定义了对内存分代管理的基本操作。
 * 它允许针对特定的内存代进行操作，如分配内存，获取已使用和空闲内存信息等。
 */
public interface Generation {

    /**
     * 在指定的内存代中申请内存空间。
     *
     * @param size       申请的内存大小，必须大于0
     * @param Generation 指定的内存代名称
     * @return 分配的内存地址（指针），如果分配失败返回-1
     * @throws OutOfMemoryError 如果在指定的代中没有足够的内存抛出此异常
     * @throws Exception 可能因为内存分配过程中抛出其他异常
     */
    int allocateOfGeneration(int size, String Generation) throws OutOfMemoryError, Exception;

    /**
     * 获取指定代中已使用的内存大小。
     *
     * @param Generation 指定的内存代名称
     * @return 指定代中已使用的内存大小
     */
    long getUsedOfGeneration(String Generation);

    /**
     * 获取指定代中空闲的内存大小。
     *
     * @param Generation 指定的内存代名称
     * @return 指定代中空闲的内存大小
     */
    long getFreeGeneration(String Generation);

    /**
     * 获取指定代的详细状态信息。
     *
     * @param Generation 指定的内存代名称
     * @return 指定代的详细状态信息
     */
    String getDetails(String Generation);
}

