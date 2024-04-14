package org.weishen.gc_.obj_;

import org.weishen.gc_.context.AppContext;
import org.weishen.gc_.gcm.GCCounterNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SimpleGcObj implements GCCounterNode {
    private final Set<GCCounterNode> references = new HashSet<>();
    private int gcRootCount = 0;
    private final String id; // 节点标识符

    public SimpleGcObj(String id) {
        this.id = id;
    }

    @Override
    public void addReference(GCCounterNode... nodes) {
        Collections.addAll(references, nodes);
    }

    @Override
    public Set<GCCounterNode> getReference() {
        return references;
    }

    @Override
    public int getGCRootCount() {
        return gcRootCount;
    }

    @Override
    public void setGCRootCount(int gcRootCount) {
        this.gcRootCount = gcRootCount;
    }

    @Override
    public String toString() {
        return "Node " + id;
    }

    // 实现freeMemory方法打印日志
    @Override
    public void freeMemory() {
        try {
            if (this instanceof SimulatedObj obj)
                AppContext.getInstance().getSimulatedHeap().free(obj.getPointer(), obj.getSize());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}