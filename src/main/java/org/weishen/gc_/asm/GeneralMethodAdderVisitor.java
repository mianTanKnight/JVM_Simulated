package org.weishen.gc_.asm;

import org.objectweb.asm.ClassVisitor;
import java.util.List;
import java.util.function.Consumer;

public class GeneralMethodAdderVisitor extends ClassVisitor {

    private String className;

    final List<Consumer<MethodGenerator>> consumers;

    SimpleMethodGenerator simpleMethodGenerator;

    public GeneralMethodAdderVisitor(int api, ClassVisitor cv, List<Consumer<MethodGenerator>> consumers, String methodType) {
        super(api, cv);
        this.consumers = consumers;
        if (MethodGenerator.METHOD_SIMPLE_GET_SET.equals(methodType))
            simpleMethodGenerator = new SimpleMethodGenerator();
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name; // Use internal JVM format
        simpleMethodGenerator.setOwnerClass(this.className);
    }

    @Override
    public void visitEnd() {
        if (null != simpleMethodGenerator && null != className && !className.isEmpty())
            consumers.forEach(c -> c.accept(simpleMethodGenerator));
        super.visitEnd();
    }
}
