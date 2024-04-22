package org.weishen.gc_.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.weishen.gc_.asm.inter.MethodGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * NewEhASM 并不是一个复杂的ASM 增强功能
 * 它为了优化或者解决单继承的限定 和 繁琐的显性继承申明
 * <p>
 * 例如 Class A extends SimulatedObj
 * 这显然不是一个好的点子 SimulatedObj 在环境启动时已经不会再发生改变
 * <p>
 * SimulatedObj 会被申明成一个接口 并使用ASM隐式实现
 * see@org.weishen.gc_.obj_.SimulatedObj
 */
public class NewEhASM {


    public static byte[] enhanceClass(byte[] classBytes) throws Exception {
        ClassReader cr = new ClassReader(classBytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        int access = cr.getAccess();
        /**
         * 接口和抽象类不处理
         */
        boolean isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
        boolean isAbstract = (access & Opcodes.ACC_ABSTRACT) != 0;
        if (isInterface || isAbstract) return classBytes;
        /**
         * int: "I"
         * boolean: "Z"
         * char: "C"
         * byte: "B"
         * short: "S"
         * long: "J"
         * float: "F"
         * double: "D"
         */
        //size int
        ClassVisitor size = new AddFieldClassVisitor(Opcodes.ASM9, cw, "size", Opcodes.ACC_PUBLIC, "I", null);
        //pointer int
        ClassVisitor pointer = new AddFieldClassVisitor(Opcodes.ASM9, size, "pointer", Opcodes.ACC_PUBLIC, "I", null);
        //isRoot bool
        ClassVisitor isRoot = new AddFieldClassVisitor(Opcodes.ASM9, pointer, "isRoot", Opcodes.ACC_PUBLIC, "Z", null);
        //interface Serializable
        EnhancedClassVisitor addInterfaces = new EnhancedClassVisitor(Opcodes.ASM9, isRoot, "size", Opcodes.ACC_PUBLIC, "I", null, "java/io/Serializable", "org/weishen/gc_/obj_/SimulatedObj");
        List<Consumer<MethodGenerator>> consumers = new ArrayList<>(6);
        consumers.add((x) -> {
            x.addGetter(cw, "size", "I");
        });
        consumers.add((x) -> {
            x.addGetter(cw, "pointer", "I");
        });
        consumers.add((x) -> {
            x.addGetter(cw, "isRoot", "Z");
        });
        consumers.add((x) -> {
            x.addSetter(cw, "size", "I");
        });
        consumers.add((x) -> {
            x.addSetter(cw, "pointer", "I");
        });
        consumers.add((x) -> {
            x.addSetter(cw, "isRoot", "Z");
        });
        GeneralMethodAdderVisitor generalMethodAdderVisitor = new GeneralMethodAdderVisitor(Opcodes.ASM9, addInterfaces, consumers, MethodGenerator.METHOD_SIMPLE_GET_SET);
        cr.accept(generalMethodAdderVisitor, 0);

        return cw.toByteArray();
    }


}
