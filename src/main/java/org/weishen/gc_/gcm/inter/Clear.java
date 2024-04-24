package org.weishen.gc_.gcm.inter;

import java.util.function.Consumer;

/**
 * 定义清除操作，以支持在垃圾回收过程中清理资源和引用。
 * <p>
 * Methods:
 * - clear: 清除节点的所有引用，并最终释放节点所占用的资源。
 */
public interface Clear {
    void clear();
}
