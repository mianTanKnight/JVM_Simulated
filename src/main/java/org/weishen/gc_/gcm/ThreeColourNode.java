package org.weishen.gc_.gcm;

import org.weishen.gc_.context.AppContext;
import org.weishen.gc_.gcm.inter.Clear;
import org.weishen.gc_.gcm.inter.ReferenceGC;
import org.weishen.gc_.gcm.inter.Mark;
import org.weishen.gc_.obj_.inter.SimulatedObj;

import java.util.*;

/**
 * 实现三色标记算法的节点类。该类管理一个模拟对象及其引用，并根据GC的三色标记算法进行标记和清除。
 * <p>
 * Attributes:
 * - references: 存储当前节点引用的其他节点集合。
 * - color: 表示节点的当前颜色，用于三色标记算法中的标记过程。
 * - id: 节点的唯一标识符，通常结合模拟对象的指针地址使用。
 * - simulatedObj: 与此节点关联的模拟对象，代表实际的数据或对象。
 * <p>
 * Methods:
 * - mark: 根据三色标记算法将节点及其引用的节点标记为到达。
 * - clear: 清除所有未标记（白色）的引用节点，并最终清理自身。
 * - freeMemory: 释放与该节点关联的模拟对象占用的内存。
 */
public class ThreeColourNode implements Mark, Clear, ReferenceGC {

    private final Set<ReferenceGC> references = new HashSet<>();
    private Color color = Color.WHITE;  // 默认所有节点初始为白色
    private final String id; // 节点标识符
    private final SimulatedObj simulatedObj;

    public ThreeColourNode(String id, SimulatedObj simulatedObj) {
        assert null != simulatedObj && null != id;
        this.simulatedObj = simulatedObj;
        //如果没制定就使用指针
        this.id = "MarkNode : id" + id + ", pointer :" + simulatedObj.getPointer();
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
        if (this.color == Color.WHITE) {
            this.color = Color.GREY;  // 标记为灰色，表示待处理
            for (ReferenceGC ref : references) {
                if (ref instanceof ThreeColourNode node && node.color == Color.WHITE) {
                    workQueue.add(node);  // 只有白色节点需要被加入队列
                    node.mark(workQueue); // 递归标记引用的节点
                }
            }
            this.color = Color.BLACK; // 处理完所有引用后，标记为黑色
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
            SimulatedObj source = getSource();
            if (null != source)
                AppContext.getInstance().getSimulatedHeap().free(source.getPointer(), source.getSize());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public enum Color {
        WHITE, GREY, BLACK
    }
}
