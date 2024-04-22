package org.weishen.gc_.gcm;

import org.weishen.gc_.gcm.inter.ReferenceGC;
import org.weishen.gc_.gcm.inter.Mark;
import org.weishen.gc_.gcm.inter.SimulatedGC;

import java.util.*;

/**
 * GCGraph 类负责模拟垃圾收集图的管理，它使用三色标记算法来跟踪和管理节点的生命周期。
 * 该图结构类似于矩阵，其中节点通过引用关系与其他节点相连接。
 * 每个Node都会 维护一个向量
 * {A:{B, C ,D}, F: { G ,H ,K }, E :{Z}}
 *
 * Attributes:
 * - roots: 存储图中所有根节点的列表。根节点通常是从程序的全局作用域或特定栈帧引用的对象。
 * - all: 存储图中所有节点的列表，包括根节点和非根节点。
 *
 * Methods:
 * - mark(): 实现三色标记过程，标记所有从根节点可达的节点。
 *   使用工作队列（workList）来避免递归，并确保所有从根节点可达的节点都被正确标记。
 * - sweep(): 清扫阶段，移除所有未被标记（即颜色为白色）的节点，并重置剩余节点的颜色为白色，为下一次垃圾收集循环做准备。
 * - register(ThreeColourNode obj): 将新节点注册到图中。如果节点是根节点，则同时添加到根列表中。
 * - getRootObjs(): 返回图中所有根节点的列表。
 * - safeTime(): 返回垃圾收集算法的安全执行时间，目前返回0，表示无特定延迟。
 */
public class GCGraph implements SimulatedGC<ThreeColourNode> {

    private final List<ThreeColourNode> roots = new ArrayList<>();

    private final List<ThreeColourNode> all = new ArrayList<>();

    /**
     * 标记作业
     */
    @Override
    public void mark() {
        Queue<Mark> workList = new ArrayDeque<>(roots);
        //
        while (!workList.isEmpty()) {
            //
            Mark current = workList.poll();
            //
            if (current instanceof ThreeColourNode tr) {
                Set<ReferenceGC> reference = tr.getReference();
                for (ReferenceGC gcCounterNode : reference) {
                    ThreeColourNode threeColourNode = (ThreeColourNode) gcCounterNode;
                    if (threeColourNode.getColor() == ThreeColourNode.Color.WHITE) {
                        threeColourNode.mark(workList);
                    }
                }
            }
        }
    }

    @Override
    public void sweep() {
        Iterator<ThreeColourNode> it = all.iterator();
        while (it.hasNext()){
            ThreeColourNode next = it.next();
            if(next.getColor() == ThreeColourNode.Color.WHITE){
                it.remove();
                next.clear();
            } else {
                next.setColor(ThreeColourNode.Color.WHITE);
            }
        }
    }

    @Override
    public void register(ThreeColourNode obj) {
        all.add(obj);
        if (obj.getSource().getIsRoot()) roots.add(obj);
    }

    @Override
    public List<ThreeColourNode> getRootObjs() {
        return roots;
    }

    @Override
    public long safeTime() {
        return 0;
    }

}
