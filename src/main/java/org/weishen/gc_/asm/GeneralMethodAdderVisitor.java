package org.weishen.gc_.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.weishen.gc_.asm.inter.MethodGenerator;

import java.util.List;
import java.util.function.Consumer;

public class GeneralMethodAdderVisitor extends ClassVisitor {

    private String className;
    final List<MethodsAndType> methodsAndType;

    SimpleMethodGenerator simpleMethodGenerator;

    public GeneralMethodAdderVisitor(int api, ClassVisitor cv, MethodsAndType... methodTypes) {
        super(api, cv);
        methodsAndType = List.of(methodTypes);
        for (MethodsAndType methodType : methodTypes) {
            if (MethodGenerator.METHOD_SIMPLE_GET_SET.equals(methodType.getMethodType()) || MethodGenerator.METHOD_TO_STRING.equals(methodType.getMethodType()))
                simpleMethodGenerator = new SimpleMethodGenerator();
        }
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name; // Use internal JVM format
        simpleMethodGenerator.setOwnerClass(this.className);
    }

    @Override
    public void visitEnd() {
        if (null != simpleMethodGenerator && null != className && !className.isEmpty()) {
            for (MethodsAndType mt : methodsAndType) {
                for (Consumer<MethodGenerator> consumer : mt.consumers) {
                    consumer.accept(simpleMethodGenerator);
                }
            }
        }
        super.visitEnd();
    }

    public static class MethodsAndType {
        final List<Consumer<MethodGenerator>> consumers;

        final String methodType;

        MethodsAndType(List<Consumer<MethodGenerator>> consumers, String methodType) {
            this.consumers = consumers;
            this.methodType = methodType;
        }

        public List<Consumer<MethodGenerator>> getConsumers() {
            return consumers;
        }

        public String getMethodType() {
            return methodType;
        }


    }
}
