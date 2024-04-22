package org.weishen.gc_.gcm.inter;


import java.util.List;

public interface SimulatedGC<T> {
    void mark();

    void sweep();

    void register(T obj);

    List<T> getRootObjs();

    long safeTime();

}
