package org.weishen.gc_.gcm;

import org.weishen.gc_.gcm.inter.Mark;
import org.weishen.gc_.gcm.inter.SimulatedGC;
import org.weishen.gc_.obj_.inter.SimulatedObj;

import java.util.*;
import java.util.logging.Logger;

/**
 * ThreeColourSerialUnSafeGraph 是GC的一个实现 串行(单线程) ,unsafe 它需要使用者提供安全保障
 * 它使用三色标记算法来跟踪和管理节点的生命周期。
 * 该图结构类似于矩阵，其中节点通过引用关系与其他节点相连接。
 * 每个Node都会 维护一个向量
 * {A:{B, C ,D}, F: { G ,H ,K }, E :{Z}}
 * <p>
 * Attributes:
 * - roots: 存储图中所有根节点的列表。根节点通常是从程序的全局作用域或特定栈帧引用的对象。
 * - all: 存储图中所有节点的列表，包括根节点和非根节点。
 * <p>
 * Methods:
 * - mark(): 实现三色标记过程，标记所有从根节点可达的节点。
 * 使用工作队列（workList）来避免递归，并确保所有从根节点可达的节点都被正确标记。
 * - sweep(): 清扫阶段，移除所有未被标记（即颜色为白色）的节点，并重置剩余节点的颜色为白色，为下一次垃圾收集循环做准备。
 * - register(ThreeColourNode obj): 将新节点注册到图中。如果节点是根节点，则同时添加到根列表中。
 * - getRootObjs(): 返回图中所有根节点的列表。
 * - safeTime(): 返回垃圾收集算法的安全执行时间，目前返回0，表示无特定延迟。
 */
public class ThreeColourSerialUnSafeGraph implements SimulatedGC<ThreeColourNode> {
    private static final Logger logger = Logger.getLogger(ThreeColourSerialUnSafeGraph.class.getName());
    private final List<ThreeColourNode> roots = new ArrayList<>();
    private final Map<SimulatedObj, ThreeColourNode> nodesMap = new HashMap<>();
    private final List<ThreeColourNode> shortActingRoots = new ArrayList<>();

    @Override
    public void gc() {
        mark();
        collect();
    }

    /**
     * 标记作业
     */
    @Override
    public void mark() {
        logger.info("Starting mark phase.");
        Queue<Mark> workList = new ArrayDeque<>(roots);
        while (!workList.isEmpty()) {
            Mark current = workList.poll();
            current.mark(workList);
        }
        logger.info("Mark phase completed.");
    }


    @Override
    public void collect() {
        sweep();
    }

    /**
     * nodesMap是储存 getSource()的包装
     * x[source]
     * if  x.color != white 那它一定存在引用
     * else 它可能存在引用或引用已被清楚 因为是此引用可能是别人的子序元素 它可能被联级"提前清扫"
     * 但不管怎么样 color == white  就会被移除
     */
    private void sweep() {
        logger.info("Starting sweep phase.");
        Iterator<ThreeColourNode> it = nodesMap.values().iterator();
        while (it.hasNext()) {
            ThreeColourNode node = it.next();
            if (node.getColor() == ThreeColourNode.Color.WHITE) {
                if (node.getSource() != null) {
                    node.clear();
                    logger.info("Node swept: " + node.getId());
                }
                it.remove();
            } else {
                node.setColor(ThreeColourNode.Color.WHITE);
            }
        }
        for (ThreeColourNode shortActingRoot : shortActingRoots) {
            shortActingRoot.clear();
        }
        shortActingRoots.clear();
        logger.info("Sweep phase completed.");
    }

    /**
     * 短效的GCroot 在断开引用之后并回收
     * 但注意回收动作由sweep()负责 的这里只负责"准备工作"
     * @param root 要断开引用的根对象
     */
    @Override
    public void disconnectAndRecycle(Object root) {
        if (root instanceof SimulatedObj so && so.getIsRoot()) {
            ThreeColourNode rootNode = nodesMap.get(so);
            if (rootNode != null) {
                rootNode.getReference().clear();
                roots.remove(rootNode);
                nodesMap.remove(root);
                shortActingRoots.add(rootNode);
            }
        }
    }

    @Override
    public void disconnect(Object root) {
        if (root instanceof SimulatedObj so && so.getIsRoot()) {
            ThreeColourNode rootNode = nodesMap.get(so);
            if (rootNode != null)
                rootNode.getReference().clear();
        }
    }
    @Override
    public void register(ThreeColourNode obj) {
        nodesMap.putIfAbsent(obj.getSource(), obj);
        if (obj.getSource().getIsRoot()) roots.add(obj);
    }


    @Override
    public void register(Object current, Object... next) {
        if (!(current instanceof SimulatedObj so)) {
            return;
        }
        ThreeColourNode currentNode = nodesMap.get(so);
        if (currentNode == null) {
            return;
        }
        for (Object obj : next) {
            if (obj instanceof SimulatedObj soNext) {
                ThreeColourNode nextNode = nodesMap.get(soNext);
                if (nextNode != null)
                    currentNode.addReference(nextNode);

            }
        }
    }

    @Override
    public List<ThreeColourNode> getRootObjs() {
        return roots;
    }

    @Override
    public long safeTime() {
        // no impl
        return 0;
    }

}
