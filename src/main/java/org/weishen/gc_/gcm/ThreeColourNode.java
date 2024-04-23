package org.weishen.gc_.gcm;

import org.weishen.gc_.context.AppContext;
import org.weishen.gc_.gcm.inter.Clear;
import org.weishen.gc_.gcm.inter.ReferenceGC;
import org.weishen.gc_.gcm.inter.Mark;
import org.weishen.gc_.obj_.inter.SimulatedObj;

import java.util.logging.Logger;

import java.util.*;

/**
 * ThreeColourSerialUnSafeGraph 类实现了SimulatedGC接口，提供了基于三色标记算法的垃圾回收机制。
 *
 * 这个类的实现是串行的，即在单个线程中执行，因此被标记为"UnSafe"，意味着在并发环境下它不提供线程安全保障，
 *
 * 使用时需要外部同步控制或保证其只在单线程环境中运行。
 *
 * 特性：
 * - roots: 存储所有被认为是GC Roots的节点，这些节点通常是全局可访问的或者由栈直接引用的对象。
 * - nodesMap: 存储所有节点，包括它们的标识和引用关系。
 *
 * 主要方法：
 * - mark(): 实现三色标记过程，逐个访问并标记从根节点可达的所有节点。
 * - sweep(): 执行清扫阶段，移除所有未被标记的节点，并重置已处理节点的状态。
 * - register(): 注册新的节点到GC图中。
 * - disconnect(): 断开选定根节点的引用，通常在该对象不再需要时调用。
 */
public class ThreeColourNode implements Mark, Clear, ReferenceGC {
    private static final Logger logger = Logger.getLogger(ThreeColourNode.class.getName());
    private final Set<ReferenceGC> references = new HashSet<>();
    private Color color = Color.WHITE;  // 默认所有节点初始为白色
    private final String id; // 节点标识符
    private final SimulatedObj simulatedObj;

    public ThreeColourNode(String id, SimulatedObj simulatedObj) {
        assert null != simulatedObj && null != id;
        this.simulatedObj = simulatedObj;
        //如果没制定就使用指针
        this.id = String.valueOf(simulatedObj.getPointer());
    }

    public String getId() {
        return id;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public void mark(Queue<Mark> workQueue) {
        //每个节点都只负责自己的向量
        logger.info("Marking node: " + id + " | Current color: " + color);
        if (this.color == Color.WHITE) {
            this.color = Color.GREY;  // 标记为灰色，表示待处理
            for (ReferenceGC ref : references) {
                if (ref instanceof ThreeColourNode node && node.color == Color.WHITE) {
                    workQueue.add(node);  // 只有白色节点需要被加入队列
                    node.mark(workQueue); // 递归标记引用的节点
                }
            }
            this.color = Color.BLACK; // 处理完所有引用后，标记为黑色
            logger.info("Node marked BLACK: " + id);
        }
    }


    @Override
    public void addReference(ReferenceGC... references) {
        this.references.addAll(List.of(references));
    }

    @Override
    public Set<ReferenceGC> getReference() {
        return this.references;
    }

    @Override
    public SimulatedObj getSource() {
        return this.simulatedObj;
    }

    @Override
    public String toString() {
        return "Node " + id;
    }

    @Override
    public void clear() {
        logger.info("Clearing node: " + id);
        Iterator<ReferenceGC> iterator = references.iterator();
        while (iterator.hasNext()) {
            ReferenceGC node = iterator.next();
            if (node instanceof ThreeColourNode threeNode) {
                if (threeNode.color == Color.WHITE) {
                    threeNode.clear(); // 清理未标记（白色）的节点
                }
            }
            iterator.remove();
        }
        freeMemory();
    }


    public void freeMemory() {
        try {
            logger.info("Freeing memory for node: " + id);
            SimulatedObj source = getSource();
            if (null != source) {
                AppContext.getSimulatedHeap().free(source.getPointer(), source.getSize());
                logger.info("Memory freed for node: " + id);
            }
        } catch (Exception e) {
            logger.severe("Error freeing memory for node: " + id + " | Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public enum Color {
        WHITE, GREY, BLACK
    }
}
