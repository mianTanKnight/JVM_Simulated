package org.weishen.gc_.gcm;


import org.weishen.gc_.context.AppContext;


/**
 * 使用单向图维护对象间的GC（垃圾回收）依赖关系。这个类负责管理图的结构，
 * 其中包括对象（节点）之间的引用关系。单向图通过节点的注册来表示对象间的依赖，
 * 即一个对象可以引用一个或多个其他对象。
 *
 * <p>主要特性：</p>
 * <ul>
 *   <li>单向注册能力：对外只提供单向的注册能力，即一个节点可以知道它依赖了哪些节点（children）。</li>
 *   <li>循环依赖支持：该结构支持循环依赖的场景，例如 A -> B, B -> A，或者更复杂的循环，如 D -> C -> A -> D。</li>
 *   <li>GC Roots作为稳定的入口：GC Roots是垃圾回收过程的起点，系统中的GC Roots是预定义的，并且不会被回收。</li>
 * </ul>
 *
 * <p>垃圾回收逻辑：</p>
 * <ol>
 *   <li>当尝试手动回收一个节点（例如GCRoot A）时，系统将检查A直接依赖的节点（比如B, C, D）。</li>
 *   <li>对于每一个依赖A的节点，减少其依赖计数（即减少对它们“父节点”的引用计数）。如果节点的依赖计数减至0（表示除了A外没有其他节点引用它），则该节点也被标记为可回收。</li>
 *   <li>回收过程会递归进行，直到没有新的节点被标记为可回收。</li>
 * </ol>
 *
 * 注意：此类的设计简化了对象间复杂依赖关系的管理，并专注于模拟垃圾回收过程。它并不处理内部的循环依赖回收问题，需要外部逻辑来辅助处理或避免产生不可达的循环依赖。
 */
public class GCGraph {


//    public static void start(String[] args) {
//        SimpleGCNode nodeA = AppContext.new_(SimpleGCNode.class, "A");
//        SimpleGCNode nodeB = AppContext.new_(SimpleGCNode.class, "B");
//        SimpleGCNode nodeC = AppContext.new_(SimpleGCNode.class, "C");
//        SimpleGCNode nodeE = AppContext.new_(SimpleGCNode.class, "E");
//        // 循环依赖的环境
//        SimpleGCNode nodeK = AppContext.new_(SimpleGCNode.class, "K");
//
//        // 建立引用关系 A -> B -> C  , E -> C
//        nodeA.addReference(nodeB);
//        nodeB.addReference(nodeC);
//        nodeE.addReference(nodeB);
//        /*
//         * C -> K - >B
//         */
//        nodeC.addReference(nodeK);
//        nodeK.addReference(nodeB);
//
//        /**
//         * 上述我们会建立两条引用线
//         * A -> B -> C -> K - > B
//         * E ->B -> C -> K -> B
//         *
//         *  A ,E 是GCroot 并且存在明显的循环引用的情况
//         */
//
//
//        // 假设A是GC Root，增加从A到C的引用计数
//        Set<GCCounterNode> visited = new HashSet<>();
//        nodeA.increase(nodeA, visited, 1); // A的GCRootCount初始值应为1
//        // 假设E是GC Root，增加从A到C的引用计数
//        Set<GCCounterNode> visited1 = new HashSet<>();
//        nodeE.increase(nodeE, visited1, 1); // A的GCRootCount初始值应为1
//
//        // 模拟断开A到B的引用，看是否会导致B和C被"回收"
//        visited.clear(); // 清空visited集合以重新使用
//        nodeA.decrease(nodeA, visited); // 假设这会递减B和C的GCRootCount
//
//        // 由于我们没有实际删除引用，递减GCRootCount，我们可以直接检查B和C的状态
//        System.out.println(nodeB.getGCRootCount()); // 预期是1
//        System.out.println(nodeC.getGCRootCount()); // 预期是1
//        System.out.println(nodeK.getGCRootCount()); // 预期是1
//
//        // 模拟E断开B 的引用
//        visited1.clear(); // 清空visited集合以重新使用
//        nodeE.decrease(nodeE, visited1); // 假设这会递减B和C的GCRootCount
//
//        //E 断开那一刻 B ,C ,K 就会被回收
//        System.out.println(nodeB.getGCRootCount()); // 预期是0
//        System.out.println(nodeC.getGCRootCount()); // 预期是0
//        System.out.println(nodeK.getGCRootCount()); // 预期是0

//    }



}
