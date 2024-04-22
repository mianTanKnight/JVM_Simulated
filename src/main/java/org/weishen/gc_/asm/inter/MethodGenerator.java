package org.weishen.gc_.asm.inter;

import org.objectweb.asm.ClassVisitor;

public interface MethodGenerator {

     String METHOD_SIMPLE_GET_SET = "get/set";

     void addGetter(ClassVisitor cv, String fieldName, String fieldType);

     void addSetter(ClassVisitor cv,String fieldName, String fieldType);

}
