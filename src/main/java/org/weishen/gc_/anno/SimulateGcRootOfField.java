package org.weishen.gc_.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 用于显性的标记GC字段(只用于成员变量字段的标注)
 * 用于支持成员变量GCRoot的关系图构建
 *
 * class A {
 *      List<Object> ls = new ArrayList<>();
 *
 *     public void test(){
 *       A  a = new_(A.class);
 *       ls.add(a);
 *     }
 *  }
 *
 * 但要提供目标方法 ls.add(a); 产生关联的方法
 * 1: 目标方法的名称 add
 * 2: 参数个数 1 (这里会影响压栈参数数量dup)
 * 如果是Map 那就是两个 Put(x,x1)
 *
 * add 是典型建立连接
 * remove 就是典型的断开连接
 *
 *
 *
 *
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SimulateGcRootOfField {


}
