package org.weishen.gc_.obj_.inter;


/**
 * SimulatedObj 是通用接口
 * 它不是被显性 implements
 * 所有的模拟对象都会通过ASM 隐式的继承它 提供它的属性并提供方法
 *
 * SimulatedObj 不会参与构造
 */
public interface SimulatedObj {

    // public int pointer
    // public int size
    // public bool isRoot

    void setPointer(int pointer);

    void setSize(int size);

    int getPointer();

    int getSize();

    void setIsRoot(boolean isRoot);

    boolean getIsRoot();

}
