package org.weishen.gc_.heap;

import org.weishen.gc_.ds.DoublySkipList;
import org.weishen.gc_.heap.inter.Generation;
import org.weishen.gc_.heap.inter.SimulatedHeap;
import org.weishen.gc_.obj_.inter.SimulatedObj;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * JVM 堆的严谨模拟
 * <p>
 * 遵循计算机内存设计
 * ######
 * 1: 内存对象
 * 以8为基础单位
 * 每个对象占用的内存大小也按8的倍数向上取整 ，这意味着即使一个对象实际上只需要几个字节的空间，它也至少占用8个字节（如果大小超过8个字节，则占用16个字节，以此类推）
 * #####
 * 2: 内存分代
 * 模拟JVM分代 eden,s1,s2,old (不需要元空间)
 * #####
 * 3: 内存顺序分配
 * allocate 会以正序分配 也就是以 0 开始到 capacity,这个为指针的模拟提供基础
 * #####
 * 4: 指针的模拟
 * 使用指针操作内存 其基础是内存顺序 这点和真内存是一样的
 * 支持运算和比较
 * #####
 * 5: 高效 简洁的内存管理
 * 使用assignedAddressPointer 指针控制内存的分配进度
 * 注意: assignedAddressPointer几乎不会"回头" (除非重置和暂停整合),回头会带来多余的复杂性和内存维护
 * 使用freedMemoryMaps 管理内存的回收与复用
 * freedMemoryMaps.V 是 DoublySkipList(基于双向链表的跳表 ) 可以在维持内存块顺序的同时，实现更高效的合并和分割操作，有效减少内存碎片化
 *
 * <p>’»
 * 提供new_
 * 依然会使用Java的反射(假如A对象 会在Java堆中真正的存在 反射实例化,也会以二进制和我们上述的设计储存在此虚拟内存中)
 * 并且返回的对象一定是一个我们设计的基类(类似于 Object)
 * 但这个基类的设计一定是参考 Java对象信息设计(head GC信息 颜色(支持二色和三色算法) 虚拟内存指针 等.. )
 * <p>
 */
public class JVMArrayGenerationHeap implements SimulatedHeap, Generation {

    /*
     * 整堆内存
     * Java单个数组最大容量限制（Integer.MAX_VALUE，大约2^31或2GB）
     *
     *
     * ##### 大小不满足的实现方案
     * 考虑使用二维数组。
     * 使用二维数组模拟可以绕过单个数组大小的限制，但这样做会引入更复杂的指针计算和内存管理逻辑：
     *
     * 例如，假设我们有三个数组A、B、C，每个数组可以存储100个字节：
     * [[0, 100] as A, [0, 100] as B, [0, 100] as C]
     *
     * 1. 指针计算：对于二维数组，指针不再是单一维度的索引，而需要映射到两个维度。
     *    例如，如果要分配到数组B，指针值应为A的容量加上B中的索引（例如，100 + B的索引）。
     *
     * 2. 边界处理：当内存分配请求跨越单个数组的边界时，需要特殊处理。
     *    例如，如果eden区的指针在数组A的位置88，并且请求分配16字节的内存，
     *    则需要跨越A数组的边界，并在数组B中开始新的分配。
     *
     * 这种方法虽然能够模拟更大的内存空间，但也要求更复杂的内存管理策略和指针运算逻辑。
     */
    private final byte[] heapMemory;

    /**
     * 幸存者区间指针 属于年轻代
     */
    private int edenPointer;

    private int survivor1Pointer;

    private int survivor2Pointer;

    private int oldPointer;

    /**
     * 内存的分配管理的简化设计
     * 一个对象new_会检查在 freedMemory 是否存在符合可用的地址
     * 如果有则直接使用(需要使用allocateLock锁)
     * 不需要移动assignedAddressPointer
     * 如果没有找到assignedAddressPointer 移动指定长度 分配给与的内存
     * <p>
     * 我们要设置四个指针地址和回收集 分别对应这每个代
     */
    public static final String EDEN_ = "eden_";
    public static final String SV1_ = "sv1_";
    public static final String SV2_ = "sv2_";
    public static final String OLD_ = "old_";

    private final Map<String, Integer> assignedAddressPointers = new HashMap<>();

    /**
     * 已释放的内存地址映射。
     * <p>
     * 初始方案使用 TreeMap 存储，其中键为起始内存地址（指针），值为该块内存的长度（以8字节为单位）。
     * TreeMap 保持内存块按地址有序，便于合并相邻的空闲块并快速定位可用内存。然而，TreeMap 在插入和删除操作中的效率较低，
     * 特别是在需要频繁合并内存块的场景中。
     * <p>
     * 考虑到性能问题，我们提出使用基于双向链表的跳表（Skip List）作为替代方案。跳表提供了平衡树级别的效率，
     * 同时在实现上更加简单。跳表可以快速地进行搜索、插入和删除操作，并且更加高效地支持内存块的合并操作。
     * <p>
     * 使用跳表作为内存管理的数据结构，可以在维持内存块顺序的同时，实现更高效的合并和分割操作，有效减少内存碎片化。
     * 跳表的节点可以直接前后遍历，使得合并连续空闲内存块变得简单直接。
     * <p>
     * 空间管理示例：
     * 堆内存空间：[--------------------------------------------------]
     * 空闲块示例：[----空闲----][-----空闲-----]    [----空闲----]
     * 释放后合并：[----------------空闲-----------------]    [----空闲----]
     * 请求分配后：[----已分配----][空闲]    [----------------空闲-----------------]
     * <p>
     * 例如，内存布局可能如下所示：
     * 初始状态（"f"表示空闲，"u"表示已使用）：
     * |u|u|f|f|u|f|u|f|f|
     * <p>
     * 经过一段时间的分配和释放后（一些"u"块被释放变成"f"）：
     * |u|f|f|u|f|f|u|f|f|
     * <p>
     * 合并相邻的空闲块：
     * |u|fff|u|ff|u|fff|
     * <p>
     * 当请求新的内存块时，如果空闲块大于请求的大小，则进行分割：
     * |u|f[2]|u|ff|u|fff|
     * ^
     * 分配的内存
     * <p>
     * 在这种管理策略下，内存分配过程被大大简化，同时也通过合并相邻空闲块来减少内存碎片。
     * 考虑到性能和操作复杂度，跳表可能是管理这种内存模型的理想选择。
     * <p>
     * <p>
     * freedMemoryMaps和 freedMemorySizeMap 并不是安全的 需要在线程安全的环境下进行
     */
    private final Map<String, DoublySkipList<Integer>> freedMemoryMaps = new HashMap<>();

    private final Map<String, Integer> freedMemorySizeMap = new HashMap<>();

    /**
     * 分段锁实现
     * 为了提高并发性和避免死锁，我们为不同的内存代分别实现了锁机制。
     * 每个内存代（segment）通过索引在数组中对应一个锁对象。
     * <p>
     * 分段如下：
     * - index 0 -> eden_: 年轻代中的Eden区。
     * - index 1 -> sv1_: 年轻代中的第一个幸存者区（Survivor1）。
     * - index 2 -> sv2_: 年轻代中的第二个幸存者区（Survivor2）。
     * - index 3 -> old_: 老年代。
     * <p>
     * 死锁预防：
     * 在进行跨代内存操作时（如对象晋升），可能需要同时获取多个锁。
     * 为避免死锁，操作时获取锁的顺序必须从索引小的代向索引大的代进行。
     * 这种顺序保证策略有效防止了死锁的发生。
     */
    private final Object[] segmentLock = new Object[]{
            new Object(),  // 锁对象 for Eden
            new Object(),  // 锁对象 for Survivor1
            new Object(),  // 锁对象 for Survivor2
            new Object()   // 锁对象 for Old
    };

    public Object getLock(String generation) {
        if (EDEN_.equals(generation)) return segmentLock[0];
        if (SV1_.equals(generation)) return segmentLock[1];
        if (SV2_.equals(generation)) return segmentLock[2];
        if (OLD_.equals(generation)) return segmentLock[3];
        return null;
    }

    /***容量***/
    private final int capacity;

    /**
     * 创建JVM堆内存区域。
     *
     * @param capacity 堆的最大容量，自动调整为8的倍数以避免溢出
     */
    public JVMArrayGenerationHeap(int capacity) {
        assert capacity > 0;
        // 确保容量是8的倍数以避免溢出
        this.capacity = capacity & (~7);
        this.heapMemory = new byte[this.capacity];
        initializePointers();
    }


    /**
     * 初始化堆中各个区域的指针。
     */
    private void initializePointers() {
        // 年轻代大约占1/3堆空间
        int youngGenSize = alignToEight(this.capacity / 3);
        this.edenPointer = 0;
        // 为了避免溢出，临时使用long类型进行计算
        long temp = (long) youngGenSize * 8;
        int edenSize = alignToEight((int) (temp / 10)); // Eden占年轻代的8/10

        // Survivor区各占剩余的1/10
        int survivorSize = alignToEight((youngGenSize - edenSize) / 2);

        // 设置Eden区和Survivor区的起始指针
        this.survivor1Pointer = this.edenPointer + edenSize;
        this.survivor2Pointer = this.survivor1Pointer + survivorSize;

        // 老年代紧接着年轻代
        this.oldPointer = this.survivor2Pointer + survivorSize;

        // 记录各区的起始地址
        assignedAddressPointers.put(EDEN_, edenPointer);
        assignedAddressPointers.put(SV1_, survivor1Pointer);
        assignedAddressPointers.put(SV2_, survivor2Pointer);
        assignedAddressPointers.put(OLD_, oldPointer);

        // 输出各区的大小（以MB为单位）
        logSizeInMB("Eden", edenSize);
        logSizeInMB("Survivor1", survivorSize);
        logSizeInMB("Survivor2", survivorSize);
        logSizeInMB("Old", this.capacity - youngGenSize);
    }

    /**
     * 以MB为单位输出内存区域的大小，并精确到两位小数。
     *
     * @param name 区域的名称
     * @param size 区域的大小（以字节为单位）
     */
    private void logSizeInMB(String name, int size) {
        int bytes_mb = 1024 * 1024;
        double sizeInMB = size / (double) bytes_mb;
        String formattedSize = String.format("%.2f", sizeInMB);
        System.out.println(name + " Size: " + formattedSize + " MB");
    }

    // 对齐到8字节边界
    private int alignToEight(int value) {
        return (value + 7) & ~7;
    }

    /***************Core API***************/

    /**
     * allocate 需要保证线程线程安全
     * 返回指定可写入的point(但没有写入,可以直接覆盖不用置0)
     *
     * @param normalizedSize 申请的大小
     * @param generation     指定的分代
     * @return 可写入的point
     * @throws OutOfMemoryError
     */
    private int allocate(int normalizedSize, String generation) throws OutOfMemoryError, Exception {
        assert normalizedSize > 0 && (normalizedSize & 7) == 0;
        Integer allocatePointer;

        synchronized (getLock(generation)) {
            //检查可使用的回收内存
            Integer freeMemoryPointer = findInFreedMemory(normalizedSize, generation);
            if (null != freeMemoryPointer) return freeMemoryPointer;
            // 不需要给heap 带来太多不是它的责任的工作 allocate 只负责检查复用和申请 如果不够直接抛 由外层保证
            if (isSpaceFull(normalizedSize, generation)) {
                // throw omm
                throw new OutOfMemoryError("Heap space is full in " + generation + " generation.");
            }
            allocatePointer = assignedAddressPointers.get(generation);
            assignedAddressPointers.put(generation, allocatePointer + normalizedSize);
        }
        //返回可用的指针 但Map已更新成next
        return allocatePointer;
    }

    private int alignSize(int size) {
        return (size + 7) & ~7;
    }

    /**
     * 检查是否超分带范围
     */
    private boolean isSpaceFull(int size, String generation) {
        Integer point = assignedAddressPointers.get(generation);
        assert null != point;
        int nextPoint = point + size;
        return switch (generation) {
            case EDEN_ -> nextPoint >= survivor1Pointer;
            case SV1_ -> nextPoint >= survivor2Pointer;
            case SV2_ -> nextPoint >= oldPointer;
            default -> nextPoint >= capacity;
        };
    }

    /**
     * 检查目标代是否存在可用的已回收的空间
     *
     * @param size       需要的大小
     * @param generation 目标代
     * @return pointer
     */
    private Integer findInFreedMemory(int size, String generation) {
        /** 复用已回收的空间  遵循 fast one 弹出头部 检查大小是否能复用 能? 切成 2块 一块是已使用 一块是未使用 ,不能迭代下一个 **/
        DoublySkipList<Integer> freeMemOfGeneration = freedMemoryMaps.get(generation);
        if (null == freeMemOfGeneration) return null;
        //
        DoublySkipList.SkipListNode<Integer> node = freeMemOfGeneration.getHeader().getForward();
        //
        while (node != null && node.getValue() < size) node = node.getForward();

        if (node != null) {
            int allocatedPointer = node.getKey();
            Integer oldSize = node.getValue();
            int remainingSize = oldSize - size;
            //
            freeMemOfGeneration.delete(node.getKey());
            if (remainingSize > 0) {
                freeMemOfGeneration.insert(allocatedPointer + size, remainingSize);
            }
            freedMemorySizeMap.computeIfPresent(generation, (k, v) -> v - size);

            System.out.println("Find a reusable memory block Point : " + allocatedPointer + " ,new Point : " + (allocatedPointer + size) + ", Old size : " + oldSize + ", reusable : " + size
                    + ", newSize : " + remainingSize);
            return allocatedPointer;
        }
        return null;
    }


    /**
     * 释放指定的内存块，并尝试与相邻的空闲块合并。
     *
     * @param point 要释放的内存块的起始地址（必须是8的倍数）。
     * @param size  要释放的内存块的大小（必须是8的倍数）。
     * @throws Exception 如果释放过程中发生错误。
     */
    @Override
    public void free(int point, int size) throws Exception {
        // 确保释放的内存地址是有效的，即必须是正数且为8的倍数
        assert point > 0 && (point & 7) == 0;
        // 根据内存地址确定它属于哪一个内存分区（代）
        String generation = getGeneration(point);

        // 锁定内存操作，确保线程安全
        synchronized (getLock(generation)) {
            // 重置指定内存区域，填充为0
            memSet(point, size, null, true);
            // 获取当前代对应的跳表，如果不存在则创建一个新的
            DoublySkipList<Integer> skipListOfGeneration = freedMemoryMaps.computeIfAbsent(generation, k -> new DoublySkipList<>());
            int oldSize = freedMemorySizeMap.computeIfAbsent(generation, k -> 0);
            // 在跳表中插入新释放的内存块
            DoublySkipList.SkipListNode<Integer> newNode = skipListOfGeneration.insert(point, size);
            // 初始化合并范围的起始点和结束点
            int mergeStart = point;
            int mergeSize = size;

            // 向前遍历跳表，寻找可以合并的空闲块
            DoublySkipList.SkipListNode<Integer> prev = newNode.getBackward();
            while (prev != null && prev.getValue() != null && (prev.getKey() + prev.getValue()) == mergeStart) {
                mergeStart = prev.getKey(); // 更新合并后的起始地址
                mergeSize += prev.getValue(); // 累加合并后的大小
                int sizePrev = prev.getKey();
                skipListOfGeneration.delete(sizePrev); // 删除被合并的节点
                prev = prev.getBackward();
            }

            // 向后遍历跳表，寻找可以合并的空闲块
            DoublySkipList.SkipListNode<Integer> next = newNode.getForward();
            while (next != null && next.getValue() != null && next.getKey() == mergeStart + mergeSize) {
                mergeSize += next.getValue(); // 累加合并后的大小
                skipListOfGeneration.delete(next.getKey()); // 删除被合并的节点
                next = next.getForward();
            }

            // 检查是否有合并发生，如果有，则更新跳表
            if (mergeStart != point || mergeSize != size) {
                skipListOfGeneration.delete(newNode.getKey()); // 删除原始节点
                skipListOfGeneration.insert(mergeStart, mergeSize); // 插入合并后的节点
                System.out.println("Marge successful  mergeStart: " + mergeStart + ", mergeSize : " + mergeSize);
            }
            oldSize += size;

            freedMemorySizeMap.put(generation, oldSize);
        }
    }

    public void memSet(Object o, String generation) throws IOException, OutOfMemoryError, Exception {
        if (null == o) return;
        byte[] objectBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(o);
            oos.flush();
            objectBytes = baos.toByteArray();
        } // 使用try-with-resources自动关闭流

        /**
         * 每个对象都会有和对齐内存和真正内存
         * real 和 aligning 内存
         * 并且 aligning 一定大于 real 向上取对齐数
         * 那么写入内存一定是以对齐内存写入
         * 反序列号读取时间使用 point + realSize
         *
         */
        int realSize = objectBytes.length;
        int aligningSize = alignSize(realSize);
        int allocatePoint = allocate(aligningSize, generation);
        if (o instanceof SimulatedObj so) {
            so.setPointer(allocatePoint);
            so.setSize(realSize);
            so.setAligningSize(aligningSize);
        }

        Arrays.fill(heapMemory, allocatePoint, allocatePoint + aligningSize, (byte) 0);
        System.arraycopy(objectBytes, 0, heapMemory, allocatePoint, realSize);
    }

    /**
     * set target mem to be Obj
     * 设置内存区域的内容。如果指定，可以先重置该区域为零，然后写入对象的序列化数据。
     * memSet 并不做任何保证正确写入和写入的线程安全 在调用memSet之前需要调用方保证
     *
     * @param pointer 指针
     * @param size    写入区域的大小
     * @param o       要写入的对象，如果为null，则只进行重置操作。
     * @param isReset 是否在写入前将内存区域重置为零。
     */
    public void memSet(int pointer, int size, Object o, boolean isReset) throws IOException, OutOfMemoryError, Exception {
        // 验证指针和大小的有效性
        assert pointer > 0 && (pointer & 7) == 0 && size > 0 && (size & 7) == 0;
        // 如果需要，重置内存区域为0
        if (isReset) {
            Arrays.fill(heapMemory, pointer, pointer + size, (byte) 0);
        }
        // 如果提供了对象，则将其序列化并写入内存
        if (o != null)
            memSet(o, getGeneration(pointer));
    }

    private String getGeneration(int point) {
        if (point < survivor1Pointer) {
            return EDEN_;
        } else if (point < survivor2Pointer) {
            return SV1_;
        } else if (point < capacity) {
            return OLD_;
        }
        throw new IllegalArgumentException("point : " + point + " ,illegality.");
    }

    @Override
    public long getCapacity() {
        return this.capacity;
    }


    @Override
    public int allocate(int size) throws OutOfMemoryError, Exception {
        throw new RuntimeException("not support!");
    }

    /**
     * move 通常是代与代之间的移动
     * 安全的move 需要配合allocate使用
     * @param srcPoint 源内存地址（指针）
     * @param desPoint 目标内存地址（指针）
     * @param size     移动的内存大小
     */
    @Override
    public void move(int srcPoint, int desPoint, int size) throws Exception {
        //检查desPoint 的合法性和可行性
        String srcGeneration = getGeneration(srcPoint);
        String desGeneration = getGeneration(desPoint);
        Object[] locks = new Object[2];
        //
        Integer srcAssigned = assignedAddressPointers.get(srcGeneration);
        Integer desAssigned = assignedAddressPointers.get(desGeneration);
        //如果大于当前的分配指针 就证明不是覆盖的 直接检查边界
        if (desPoint >= desAssigned && isSpaceFull(size, desGeneration)) {
            throw new OutOfMemoryError("Heap space is full in " + desGeneration + " generation. move failed");
        }
        if (srcAssigned < desAssigned) {
            locks[0] = getLock(srcGeneration);
            locks[1] = getLock(desGeneration);
        } else {
            locks[1] = getLock(srcGeneration);
            locks[0] = getLock(desGeneration);
        }
        /** 如果是同一个代 那么 lock[0] = lock[1] sync 是可重入的 ,如果不是同一代 assigned 可以确定 分段锁的 优先级 越小优先级越高 */
        synchronized (locks[0]) {
            synchronized (locks[1]) {
                System.arraycopy(heapMemory, srcPoint, heapMemory, desPoint, size);
                free(srcPoint, size);
            }
        }
    }

    @Override
    public String getHeapDetails() {
        // 获取每个分代的空闲内存大小
        Integer freeEden = freedMemorySizeMap.getOrDefault(EDEN_, 0);
        Integer freeSv1 = freedMemorySizeMap.getOrDefault(SV1_, 0);
        Integer freeSv2 = freedMemorySizeMap.getOrDefault(SV2_, 0);
        Integer freeOld = freedMemorySizeMap.getOrDefault(OLD_, 0);

        // 计算每个分代的已使用内存
        int usedEden = assignedAddressPointers.get(EDEN_) - edenPointer - freeEden;
        int usedSv1 = assignedAddressPointers.get(SV1_) - survivor1Pointer - freeSv1;
        int usedSv2 = assignedAddressPointers.get(SV2_) - survivor2Pointer - freeSv2;
        int usedOld = assignedAddressPointers.get(OLD_) - oldPointer - freeOld;

        return String.format("Heap Capacity: %d, Eden Used: %d, Survivor1 Used: %d, Survivor2 Used: %d, Old Used: %d",
                capacity,
                usedEden,
                usedSv1,
                usedSv2,
                usedOld);
    }

    @Override
    public int allocateOfGeneration(int size, String Generation) throws OutOfMemoryError, Exception {
        // 根据代名称调用allocate进行内存分配
        return allocate(size, Generation);
    }

    @Override
    public long getUsedOfGeneration(String Generation) {
        // 返回指定代已使用的内存大小
        Integer pointer = assignedAddressPointers.get(Generation);
        if (pointer == null) {
            return 0;
        }
        int freeSize = freedMemorySizeMap.get(Generation);
        int basePointer = switch (Generation) {
            case EDEN_ -> edenPointer;
            case SV1_ -> survivor1Pointer;
            case SV2_ -> survivor2Pointer;
            case OLD_ -> oldPointer;
            default -> -1;
        };
        return pointer - basePointer - freeSize;
    }

    @Override
    public long getFreeGeneration(String Generation) {
        // 返回指定代的剩余空间
        long used = getUsedOfGeneration(Generation);
        int freeSize = freedMemorySizeMap.get(Generation);
        int totalSpace = switch (Generation) {
            case EDEN_ -> survivor1Pointer - edenPointer;
            case SV1_ -> survivor2Pointer - survivor1Pointer;
            case SV2_ -> oldPointer - survivor2Pointer;
            case OLD_ -> capacity - oldPointer;
            default -> -1;
        };
        return totalSpace - used + freeSize;
    }

    @Override
    public String getDetails(String Generation) {
        // 提供指定代的详细信息
        return String.format("%s: Used: %d, Free: %d", Generation, getUsedOfGeneration(Generation), getFreeGeneration(Generation));
    }


}
