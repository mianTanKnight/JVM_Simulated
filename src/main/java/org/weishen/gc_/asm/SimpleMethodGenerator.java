package org.weishen.gc_.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.weishen.gc_.asm.inter.MethodGenerator;

import java.util.HashSet;
import java.util.Set;

public class SimpleMethodGenerator implements MethodGenerator {

    private String ownerClass;
    private final Set<String> existingMethods = new HashSet<>();

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
    public void toString(ClassVisitor cv, String name, String type) {
        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, name, "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn("SimulatedObj[Pointer=%d, Size=%d, AligningSize=%d, IsRoot=%s]");

        mv.visitInsn(Opcodes.ICONST_4);
        mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");

        loadAndBox(mv, 0, "pointer", "I");
        loadAndBox(mv, 1, "size", "I");
        loadAndBox(mv, 2, "aligningSize", "I");
        loadAndBox(mv, 3, "isRoot", "Z");

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "format", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(6, 1);
        mv.visitEnd();
    }

    private void loadAndBox(MethodVisitor mv, int index, String fieldName, String fieldType) {
        mv.visitInsn(Opcodes.DUP);
        mv.visitIntInsn(Opcodes.BIPUSH, index);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, ownerClass, fieldName, fieldType);
        autoBox(mv, fieldType);
        mv.visitInsn(Opcodes.AASTORE);
    }

    private void autoBox(MethodVisitor mv, String fieldType) {
        switch(fieldType) {
            case "I":
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                break;
            case "Z":
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
                break;
            default:
                // handle other types if necessary
                break;
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
