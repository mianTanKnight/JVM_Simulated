package org.weishen.gc_;

import org.weishen.gc_.context.AppContext;
import org.weishen.gc_.gcm.ThreeColourSerialUnSafeGraph;
import org.weishen.gc_.obj_.Order;
import org.weishen.gc_.obj_.inter.SimulatedObj;

public class Main {

    public static void main(String[] args) {

        /**
         * 获得GC回收器
         */
        ThreeColourSerialUnSafeGraph gcGraph = AppContext.getGCGraph();

        /**
         * 构建业务对象关系
         * o,o1,o2,o3
         * o 和 o3是 GcRoot
         * o -> o1 -> o2
         * o3 ->o2
         * 所以我们知道 o2由两个GCroot持有连接
         */
        Order order = AppContext.newRoot_(Order.class, "N50", "Zhangsan");
        if (order instanceof SimulatedObj so) {
            System.out.println(so.toStringS());
        }
        Order order3 = AppContext.newRoot_(Order.class, "N70", "K");
        if (order3 instanceof SimulatedObj s1) {
            System.out.println(s1.toStringS());
        }
        Order order1 = AppContext.new_(Order.class, "N51", "Lis");
        if (order1 instanceof SimulatedObj s2) {
            System.out.println(s2.toStringS());
        }
        Order order2 = AppContext.new_(Order.class, "N52", "Ww");

        if (order2 instanceof SimulatedObj s3) {
            System.out.println(s3.toStringS());
        }

        //构建GC关系
        gcGraph.register(order, order1);
        gcGraph.register(order1, order2);
        gcGraph.register(order3, order2);

        //GCroot断开
        gcGraph.disconnectAndRecycle(order);
        gcGraph.disconnectAndRecycle(order3);
        /**
         * 结果应该是o1被回收 o2不会被回收
         */
        AppContext.gc();

    }

    private static int alignSize(int size) {
        return (size + 7) & ~7;
    }


//    public static void testMem() throws Exception {
//
//        JVMArrayGenerationHeap heap = (JVMArrayGenerationHeap) AppContext.getInstance().getSimulatedHeap();
//        Random random = new Random();
//        List<Long> ca = new ArrayList<>();
//        for (int i = 0; i < 1000; i++) {
//            int size = (random.nextInt(1000) + 7) & ~7;
//            int point = heap.allocateOfGeneration(size, JVMArrayGenerationHeap.EDEN_);
//            long pointAndSize = ((long) point << 32) | size;
//            ca.add(pointAndSize);
//        }
//        // 释放第25个分配的内存块
//        long pointAndSize = ca.get(25);
//        int point = (int) (pointAndSize >> 32); // 取出point
//        int size = (int) (pointAndSize & 0xFFFFFFFFL); // 取出size
//        heap.free(point, size);
//        //再free 26 结果应该是mrage 成功
//        long pointAndSize1 = ca.get(26);
//        int point1 = (int) (pointAndSize1 >> 32); // 取出point
//        int size1 = (int) (pointAndSize1 & 0xFFFFFFFFL); // 取出size
//        heap.free(point1, size1);
//        //再free 24 结果应该是mrage 成功
//        long pointAndSize2 = ca.get(24);
//        int point2 = (int) (pointAndSize2 >> 32); // 取出point
//        int size2 = (int) (pointAndSize2 & 0xFFFFFFFFL); // 取出size
//        heap.free(point2, size2);
//
//        System.out.println(heap.getHeapDetails());
//
//        //尝试复用已回收的缓冲
//        heap.allocateOfGeneration((16) & ~7, JVMArrayGenerationHeap.EDEN_);
//
//        System.out.println(heap.getHeapDetails());
//
//        //把 500 move 到 sv1中
//        long pointAndSize3 = ca.get(500);
//        int point3 = (int) (pointAndSize3 >> 32); // 取出point
//        int size3 = (int) (pointAndSize3 & 0xFFFFFFFFL); // 取出size
//        int allocate = heap.allocateOfGeneration(size3, JVMArrayGenerationHeap.SV1_);
//        heap.move(point3, allocate, size3);
//        System.out.println(heap.getHeapDetails());
//    }


//    public static void testSkipList() {
//        DoublySkipList<Integer> list = new DoublySkipList<>();
//
//        // 插入大量元素
//        for (int i = 0; i < 100000; i++) {
//            list.insert(i, i + 1000);
//        }
//
//        // 搜索并验证元素
//        boolean valid = true;
//        for (int i = 0; i < 100000; i++) {
//            DoublySkipList.SkipListNode<Integer> node = list.search(i);
//            if (node == null || node.getValue() != i + 1000) {
//                valid = false;
//                break;
//            }
//        }
//        System.out.println("SkipList is " + (valid ? "valid" : "invalid"));
//
//        DoublySkipList.SkipListNode<Integer> search = list.search(500);
//        // 取出500 的前节点 是499
//        DoublySkipList.SkipListNode<Integer> integerSkipListNode = search.getBackward();
//        System.out.println(integerSkipListNode.getKey());
//        // 删掉499
//        list.delete(499);
//        DoublySkipList.SkipListNode<Integer> xg = search.getBackward();
//        // 输出 498 正确
//        System.out.println(xg.getKey());
//        // 再插入499
//        list.insert(499, null);
//        DoublySkipList.SkipListNode<Integer> fg = search.getBackward();
//        //输出499正确›
//        System.out.println(fg.getKey());
//
//    }

}
