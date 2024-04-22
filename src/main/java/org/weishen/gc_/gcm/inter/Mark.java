package org.weishen.gc_.gcm.inter;

import org.weishen.gc_.obj_.inter.SimulatedObj;

import java.util.Queue;

/**
 * 为参与标记的节点定义必须实现的标记操作。
 *
 * Methods:
 * - mark: 将节点标记为访问过，并可能将其加入到待处理队列中，以便进一步处理其引用。
 */
/**
 * 标记算法
 */
public interface Mark {

    void mark(Queue<Mark> workQueue);

    SimulatedObj getSource();
}
