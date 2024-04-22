package org.weishen.gc_.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.weishen.gc_.asm.inter.MethodGenerator;

import java.util.HashSet;
import java.util.Set;

public class SimpleMethodGenerator implements MethodGenerator {

    private String ownerClass;
    private Set<String> existingMethods = new HashSet<>();

    public SimpleMethodGenerator() {
    }

    public void setOwnerClass(String ownerClass) {
        this.ownerClass = ownerClass;
    }

    public void addGetter(ClassVisitor cv, String fieldName, String fieldType) {
        String methodName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String methodDesc = "()" + fieldType;
        if (!existingMethods.contains(methodName + methodDesc)) {
            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, methodName, methodDesc, null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, ownerClass, fieldName, fieldType);
            mv.visitInsn(getReturnOpcode(fieldType));
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
    }

    public void addSetter(ClassVisitor cv, String fieldName, String fieldType) {
        String methodName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String methodDesc = "(" + fieldType + ")V";
        if (!existingMethods.contains(methodName + methodDesc)) {
            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, methodName, methodDesc, null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(getLoadOpcode(fieldType), 1);
            mv.visitFieldInsn(Opcodes.PUTFIELD, ownerClass, fieldName, fieldType);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
    }
    private int getReturnOpcode(String type) {
        return switch (type) {
            case "I", "Z", "B", "C", "S" -> Opcodes.IRETURN;
            case "J" -> Opcodes.LRETURN;
            case "F" -> Opcodes.FRETURN;
            case "D" -> Opcodes.DRETURN;
            default -> Opcodes.ARETURN; // for objects and arrays
        };
    }

    private int getLoadOpcode(String type) {
        return switch (type) {
            case "I", "Z", "B", "C", "S" -> Opcodes.ILOAD;
            case "J" -> Opcodes.LLOAD;
            case "F" -> Opcodes.FLOAD;
            case "D" -> Opcodes.DLOAD;
            default -> Opcodes.ALOAD; // for objects and arrays
        };
    }
}
