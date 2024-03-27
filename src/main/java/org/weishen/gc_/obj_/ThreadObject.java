package org.weishen.gc_.obj_;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thread 在 JVM 中属于GCRoot
 * 但它是特殊的GCRoot
 * 它不需要序列号到模拟内存中
 * 它需要监控函数栈
 * 所有的编程语言 都有一个最基本的原则: 独立性
 * 例如 函数A  对于所以得执行线程来说 它都是独立 互不影响的 像大楼中的层与层
 *
 * 一个线程执行 A{
 *  Object x =    new_ obj(); 申请资源 添加到 ThreadObject 的GC子节点中
 *    .....
 *
 * } // 执行完 弹出函数栈  x 被标记不可达
 * ... 执行下个函数
 */
public class ThreadObject extends SimulatedObject {

}
