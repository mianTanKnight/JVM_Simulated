package org.weishen.gc_.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

import java.util.Arrays;
import java.util.HashSet;

public class EnhancedClassVisitor extends ClassVisitor {

    private final String fieldName;
    private final int access;
    private final String descriptor;
    private final Object value;
    private boolean isFieldPresent;
    private final String[] interfacesToAdd;

    public EnhancedClassVisitor(int api, ClassVisitor classVisitor, String fieldName, int access, String descriptor, Object value, String... interfacesToAdd) {
        super(api, classVisitor);
        this.fieldName = fieldName;
        this.access = access;
        this.descriptor = descriptor;
        this.value = value;
        this.isFieldPresent = false;
        this.interfacesToAdd = interfacesToAdd;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // Combine existing interfaces with new ones
        String[] combinedInterfaces = combineInterfaces(interfaces, interfacesToAdd);
        super.visit(version, access, name, signature, superName, combinedInterfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if (name.equals(this.fieldName)) {
            isFieldPresent = true;
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public void visitEnd() {
        if (!isFieldPresent) {
            FieldVisitor fv = super.visitField(this.access, fieldName, descriptor, null, this.value);
            if (fv != null) {
                fv.visitEnd();
            }
        }
        super.visitEnd();
    }

    private String[] combineInterfaces(String[] existing, String[] toAdd) {
        HashSet<String> allInterfaces = new HashSet<>(Arrays.asList(existing));
        allInterfaces.addAll(Arrays.asList(toAdd));
        return allInterfaces.toArray(new String[0]);
    }
}
