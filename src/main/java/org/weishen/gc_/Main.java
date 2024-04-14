package org.weishen.gc_;


import org.weishen.gc_.context.AppContext;
import org.weishen.gc_.ds.DoublySkipList;
import org.weishen.gc_.heap.JVMArrayGenerationHeap;
import org.weishen.gc_.obj_.SimpleGcObj;
import org.weishen.gc_.obj_.SimulatedObj;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        List<SimpleGcObj> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            SimpleGcObj simpleGcObj = AppContext.new_(SimpleGcObj.class, "A" + i);
            SimulatedObj so = (SimulatedObj) simpleGcObj;
            System.out.println("指针头 :" + so.getPointer());
            System.out.println("size :" + so.getSize());
            System.out.println("是否GcRoot : " + so.getIsRoot());
            System.out.println(simpleGcObj);
            list.add(simpleGcObj);
        }
        for (SimpleGcObj simpleGcObj : list) {
            simpleGcObj.free();
        }
    }


    public static void testMem() throws Exception {

        JVMArrayGenerationHeap heap = (JVMArrayGenerationHeap) AppContext.getInstance().getSimulatedHeap();
        Random random = new Random();
        List<Long> ca = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            int size = (random.nextInt(1000) + 7) & ~7;
            int point = heap.allocateOfGeneration(size, JVMArrayGenerationHeap.EDEN_);
            long pointAndSize = ((long) point << 32) | size;
            ca.add(pointAndSize);
        }
        // 释放第25个分配的内存块
        long pointAndSize = ca.get(25);
        int point = (int) (pointAndSize >> 32); // 取出point
        int size = (int) (pointAndSize & 0xFFFFFFFFL); // 取出size
        heap.free(point, size);
        //再free 26 结果应该是mrage 成功
        long pointAndSize1 = ca.get(26);
        int point1 = (int) (pointAndSize1 >> 32); // 取出point
        int size1 = (int) (pointAndSize1 & 0xFFFFFFFFL); // 取出size
        heap.free(point1, size1);
        //再free 24 结果应该是mrage 成功
        long pointAndSize2 = ca.get(24);
        int point2 = (int) (pointAndSize2 >> 32); // 取出point
        int size2 = (int) (pointAndSize2 & 0xFFFFFFFFL); // 取出size
        heap.free(point2, size2);

        System.out.println(heap.getHeapDetails());

        //尝试复用已回收的缓冲
        heap.allocateOfGeneration((16) & ~7, JVMArrayGenerationHeap.EDEN_);

        System.out.println(heap.getHeapDetails());

        //把 500 move 到 sv1中
        long pointAndSize3 = ca.get(500);
        int point3 = (int) (pointAndSize3 >> 32); // 取出point
        int size3 = (int) (pointAndSize3 & 0xFFFFFFFFL); // 取出size
        int allocate = heap.allocateOfGeneration(size3, JVMArrayGenerationHeap.SV1_);
        heap.move(point3, allocate, size3);
        System.out.println(heap.getHeapDetails());
    }


    public static void testSkipList() {
        DoublySkipList<Integer> list = new DoublySkipList<>();

        // 插入大量元素
        for (int i = 0; i < 100000; i++) {
            list.insert(i, i + 1000);
        }

        // 搜索并验证元素
        boolean valid = true;
        for (int i = 0; i < 100000; i++) {
            DoublySkipList.SkipListNode<Integer> node = list.search(i);
            if (node == null || node.getValue() != i + 1000) {
                valid = false;
                break;
            }
        }
        System.out.println("SkipList is " + (valid ? "valid" : "invalid"));

        DoublySkipList.SkipListNode<Integer> search = list.search(500);
        // 取出500 的前节点 是499
        DoublySkipList.SkipListNode<Integer> integerSkipListNode = search.getBackward();
        System.out.println(integerSkipListNode.getKey());
        // 删掉499
        list.delete(499);
        DoublySkipList.SkipListNode<Integer> xg = search.getBackward();
        // 输出 498 正确
        System.out.println(xg.getKey());
        // 再插入499
        list.insert(499, null);
        DoublySkipList.SkipListNode<Integer> fg = search.getBackward();
        //输出499正确›
        System.out.println(fg.getKey());

    }

}
