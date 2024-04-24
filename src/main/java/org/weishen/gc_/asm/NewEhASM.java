package org.weishen.gc_.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.weishen.gc_.asm.inter.MethodGenerator;

import java.io.FileOutputStream;
import java.io.IOException;
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
        ClassWriter cw = new ClassWriter(cr,ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
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

        ClassVisitor aligningSize = new AddFieldClassVisitor(Opcodes.ASM9, size, "aligningSize", Opcodes.ACC_PUBLIC, "I", null);
        //pointer int
        ClassVisitor pointer = new AddFieldClassVisitor(Opcodes.ASM9, aligningSize, "pointer", Opcodes.ACC_PUBLIC, "I", null);
        //isRoot bool
        ClassVisitor isRoot = new AddFieldClassVisitor(Opcodes.ASM9, pointer, "isRoot", Opcodes.ACC_PUBLIC, "Z", null);
        //interface Serializable
        EnhancedClassVisitor addInterfaces = new EnhancedClassVisitor(Opcodes.ASM9, isRoot, "size", Opcodes.ACC_PUBLIC, "I", null, "java/io/Serializable", "org/weishen/gc_/obj_/inter/SimulatedObj");

        // get/set
        List<Consumer<MethodGenerator>> getSetConsumers = new ArrayList<>(6);
        getSetConsumers.add((x) -> {
            x.addGetter(cw, "size", "I");
        });
        getSetConsumers.add((x) -> {
            x.addSetter(cw, "size", "I");
        });
        getSetConsumers.add((x) -> {
            x.addGetter(cw, "aligningSize", "I");
        });
        getSetConsumers.add((x) -> {
            x.addSetter(cw, "aligningSize", "I");
        });
        getSetConsumers.add((x) -> {
            x.addGetter(cw, "pointer", "I");
        });
        getSetConsumers.add((x) -> {
            x.addSetter(cw, "pointer", "I");
        });
        getSetConsumers.add((x) -> {
            x.addGetter(cw, "isRoot", "Z");
        });
        getSetConsumers.add((x) -> {
            x.addSetter(cw, "isRoot", "Z");
        });

        List<Consumer<MethodGenerator>> toStringConsumers = new ArrayList<>(1);

        toStringConsumers.add((x)->{
            x.toString(cw,MethodGenerator.METHOD_TO_STRING,null);
        });


        GeneralMethodAdderVisitor generalMethodAdderVisitor = new GeneralMethodAdderVisitor(Opcodes.ASM9, addInterfaces
                ,new GeneralMethodAdderVisitor.MethodsAndType(getSetConsumers,MethodGenerator.METHOD_SIMPLE_GET_SET)
                ,new GeneralMethodAdderVisitor.MethodsAndType(toStringConsumers,MethodGenerator.METHOD_TO_STRING)
        );

        cr.accept(generalMethodAdderVisitor, 0);

        return cw.toByteArray();
    }


}
