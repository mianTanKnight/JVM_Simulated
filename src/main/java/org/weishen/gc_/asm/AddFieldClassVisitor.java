package org.weishen.gc_.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

/**
 * public AddField To Class of ASM
 * unsafe of Thread
 * <p>
 * 给 Class 新增Field的 公共类 不支持泛型
 * example :
 * Class  A -> new AddFieldClassVisitor1
 * Class  B -> new AddFieldClassVisitor2
 */
public class AddFieldClassVisitor extends ClassVisitor {

    private final String fieldName;
    private final int access;
    private final String descriptor;
    private final Object value;
    private boolean isFieldPresent;

    public AddFieldClassVisitor(int api, ClassVisitor classVisitor, String fieldName, int access, String descriptor, Object value) {
        super(api, classVisitor);
        this.fieldName = fieldName;
        this.access = access;
        this.descriptor = descriptor;
        this.value = value;
        this.isFieldPresent = false;
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
            FieldVisitor fv = super.visitField(access, fieldName, descriptor, null, value);
            if (fv != null) fv.visitEnd();

        }
        super.visitEnd();
    }
}
