/*
 * Javassist, a Java-bytecode translator toolkit.
 * Copyright (C) 1999-2003 Shigeru Chiba. All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License.  Alternatively, the contents of this file may be used under
 * the terms of the GNU Lesser General Public License Version 2.1 or later.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */

package javassist;

import javassist.bytecode.*;
import javassist.compiler.Javac;
import javassist.compiler.CompileError;
import javassist.expr.ExprEditor;

/* Some methods do nothing except calling the super's method.
 * They might seem redundant but they are necessary so that javadoc
 * includes the description of those methods in the page of this class.
 */

/**
 * An instance of CtConstructor represents a constructor.
 * It may represent a static constructor
 * (class initializer).  To distinguish a constructor and a class
 * initializer, call <code>isClassInitializer()</code>.
 *
 * @see CtClass#getDeclaredConstructors()
 * @see CtClass#getClassInitializer()
 * @see CtNewConstructor
 */
public final class CtConstructor extends CtBehavior {
    protected CtConstructor next;

    protected CtConstructor(MethodInfo minfo, CtClass declaring) {
        super(declaring, minfo);
        next = null;
    }

    /**
     * Creates a constructor with no constructor body.
     * The created constructor
     * must be added to a class with <code>CtClass.addConstructor()</code>.
     *
     * <p>The created constructor does not include a constructor body,
     * must be specified with <code>setBody()</code>.
     *
     * @param declaring         the class to which the created method is added.
     * @param parameters        a list of the parameter types
     *
     * @see CtClass#addConstructor(CtConstructor)
     * @see CtConstructor#setBody(String)
     * @see CtConstructor#setBody(CtConstructor,ClassMap)
     */
    public CtConstructor(CtClass[] parameters, CtClass declaring) {
        this((MethodInfo)null, declaring);
        ConstPool cp = declaring.getClassFile2().getConstPool();
        String desc = Descriptor.ofConstructor(parameters);
        methodInfo = new MethodInfo(cp, "<init>", desc);
        setModifiers(Modifier.PUBLIC);
    }

    /**
     * Creates a copy of a <code>CtConstructor</code> object.
     * The created constructor must be
     * added to a class with <code>CtClass.addConstructor()</code>.
     *
     * <p>All occurrences of class names in the created constructor
     * are replaced with names specified by
     * <code>map</code> if <code>map</code> is not <code>null</code>.
     *
     * <p>By default, all the occurrences of the names of the class
     * declaring <code>src</code> and the superclass are replaced
     * with the name of the class and the superclass that
     * the created constructor is added to.
     * This is done whichever <code>map</code> is null or not.
     * To prevent this replacement, call <code>ClassMap.fix()</code>.
     *
     * <p><b>Note:</b> if the <code>.class</code> notation (for example,
     * <code>String.class</code>) is included in an expression, the
     * Javac compiler may produce a helper method.
     * Since this constructor never
     * copies this helper method, the programmers have the responsiblity of
     * copying it.  Otherwise, use <code>Class.forName()</code> in the
     * expression.
     *
     * @param src       the source method.
     * @param declaring    the class to which the created method is added.
     * @param map       the hashtable associating original class names
     *                  with substituted names.
     *                  It can be <code>null</code>.
     *
     * @see CtClass#addConstructor(CtConstructor)
     * @see ClassMap#fix(String)
     */
    public CtConstructor(CtConstructor src, CtClass declaring, ClassMap map)
        throws CannotCompileException
    {
        this((MethodInfo)null, declaring);
        MethodInfo srcInfo = src.methodInfo;
        CtClass srcClass = src.getDeclaringClass();
        ConstPool cp = declaring.getClassFile2().getConstPool();
        if (map == null)
            map = new ClassMap();

        map.put(srcClass.getName(), declaring.getName());
        try {
            boolean patch = false;
            CtClass srcSuper = srcClass.getSuperclass();
            String destSuperName = declaring.getSuperclass().getName();
            if (srcSuper != null) {
                String srcSuperName = srcSuper.getName();
                if (!srcSuperName.equals(destSuperName))
                    if (srcSuperName.equals(CtClass.javaLangObject))
                        patch = true;
                    else
                        map.put(srcSuperName, destSuperName);
            }

            methodInfo = new MethodInfo(cp, srcInfo.getName(), srcInfo, map);
            if (patch)
                methodInfo.setSuperclass(destSuperName);
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
        catch (BadBytecode e) {
            throw new CannotCompileException(e);
        }
    }

    static CtConstructor append(CtConstructor list, CtConstructor tail) {
        tail.next = null;
        if (list == null)
            return tail;
        else {
            CtConstructor lst = list;
            while (lst.next != null)
                lst = lst.next;

            lst.next = tail;
            return list;
        }
    }

    static int count(CtConstructor m) {
        int n = 0;
        while (m != null) {
            ++n;
            m = m.next;
        }

        return n;
    }

    /**
     * Returns the MethodInfo representing the constructor in the
     * class file.
     */
    public MethodInfo getMethodInfo() {
        return super.getMethodInfo();
    }

    /**
     * Returns true if this object represents a constructor.
     */
    public boolean isConstructor() {
        return methodInfo.isConstructor();
    }

    /**
     * Returns true if this object represents a static initializer.
     */
    public boolean isClassInitializer() {
        return methodInfo.isStaticInitializer();
    }

    /**
     * Obtains the encoded modifiers of the constructor.
     *
     * @return          modifiers encoded with
     *                  <code>javassist.Modifier</code>.
     * @see Modifier
     */
    public int getModifiers() {
        return super.getModifiers();
    }

    /**
     * Sets the encoded modifiers of the constructor.
     *
     * @see Modifier
     */
    public void setModifiers(int mod) {
        super.setModifiers(mod);
    }

    /**
     * Obtains the name of this constructor.
     * It is the same as the simple name of the class declaring this
     * constructor.  If this object represents a class initializer,
     * then this method returns <code>"&lt;clinit&gt;"</code>.
     */
    public String getName() {
        if (methodInfo.isStaticInitializer())
            return MethodInfo.nameClinit;
        else
            return declaringClass.getName();
    }

    /**
     * Returns the class that declares this constructor.
     */
    public CtClass getDeclaringClass() {
        return super.getDeclaringClass();
    }

    /**
     * Obtains parameter types of this constructor.
     */
    public CtClass[] getParameterTypes() throws NotFoundException {
        return super.getParameterTypes();
    }

    /**
     * Returns the character string representing the parameter types.
     * If two constructors have the same parameter types,
     * <code>getSignature()</code> returns the same string.
     */
    public String getSignature() {
        return super.getSignature();
    }

    /**
     * Obtains exceptions that this constructor may throw.
     */
    public CtClass[] getExceptionTypes() throws NotFoundException {
        return super.getExceptionTypes();
    }

    /**
     * Sets exceptions that this constructor may throw.
     */
    public void setExceptionTypes(CtClass[] types)
        throws NotFoundException
    {
        super.setExceptionTypes(types);
    }

    /**
     * Returns true if the constructor is the default one.
     */
    public boolean isEmpty() {
        CodeAttribute ca = getMethodInfo2().getCodeAttribute();
        if (ca == null)
            return false;       // native or abstract??
                                // they are not allowed, though.

        ConstPool cp = ca.getConstPool();
        CodeIterator it = ca.iterator();
        try {
            int pos, desc;
            return it.byteAt(it.next()) == Opcode.ALOAD_0
                && it.byteAt(pos = it.next()) == Opcode.INVOKESPECIAL
                && (desc = cp.isConstructor(CtClass.javaLangObject,
                                            it.u16bitAt(pos + 1))) != 0
                && cp.getUtf8Info(desc).equals("()V")
                && it.byteAt(it.next()) == Opcode.RETURN
                && !it.hasNext();
        }
        catch (BadBytecode e) {}
        return false;
    }

    /**
     * Sets a constructor body.
     *
     * @param src       the source code representing the constructor body.
     *                  It must be a single statement or block.
     */
    public void setBody(String src) throws CannotCompileException {
        super.setBody(src);
    }

    /**
     * Copies a constructor body from another constructor.
     *
     * <p>All occurrences of the class names in the copied body
     * are replaced with the names specified by
     * <code>map</code> if <code>map</code> is not <code>null</code>.
     *
     * @param src       the method that the body is copied from.
     * @param map       the hashtable associating original class names
     *                  with substituted names.
     *                  It can be <code>null</code>.
     */
    public void setBody(CtConstructor src, ClassMap map)
        throws CannotCompileException
    {
        setBody0(src.declaringClass, src.methodInfo,
                 declaringClass, methodInfo, map);
    }

    /**
     * Obtains an attribute with the given name.
     * If that attribute is not found in the class file, this
     * method returns null.
     *
     * @param name              attribute name
     */
    public byte[] getAttribute(String name) {
        return super.getAttribute(name);
    }

    /**
     * Adds an attribute. The attribute is saved in the class file.
     *
     * @param name      attribute name
     * @param data      attribute value
     */
    public void setAttribute(String name, byte[] data) {
        super.setAttribute(name, data);
    }

    /**
     * Declares to use <code>$cflow</code> for this constructor.
     * If <code>$cflow</code> is used, the class files modified
     * with Javassist requires a support class
     * <code>javassist.runtime.Cflow</code> at runtime
     * (other Javassist classes are not required at runtime).
     *
     * <p>Every <code>$cflow</code> variable is given a unique name.
     * For example, if the given name is <code>"Point.paint"</code>,
     * then the variable is indicated by <code>$cflow(Point.paint)</code>.
     *
     * @param name      <code>$cflow</code> name.  It can include
     *                  alphabets, numbers, <code>_</code>,
     *                  <code>$</code>, and <code>.</code> (dot).
     *
     * @see javassist.runtime.Cflow
     */
    public void useCflow(String name) throws CannotCompileException {
        super.useCflow(name);
    }

    /**
     * Modifies the constructor body.
     *
     * @param converter         specifies how to modify.
     */
    public void instrument(CodeConverter converter)
        throws CannotCompileException
    {
        super.instrument(converter);
    }

    /**
     * Modifies the constructor body.
     *
     * @param editor            specifies how to modify.
     */
    public void instrument(ExprEditor editor)
        throws CannotCompileException
    {
        super.instrument(editor);
    }

    /**
     * Inserts bytecode at the beginning of the constructor body.
     * Since the bytecode is inserted before a constructor in the super
     * class or this class is called, the bytecode is subject
     * to constraints described
     * in Section 4.8.2 of The Java Virtual Machine Specification (2nd ed).
     * For example, it cannot access instance members although it can access
     * static members.
     *
     * @param src       the source code representing the inserted bytecode.
     *                  It must be a single statement or block.
     */
    public void insertBefore(String src) throws CannotCompileException {
        super.insertBefore(src);
    }

    /**
     * Inserts bytecode just after another constructor in the super class
     * or this class is called.
     *
     * @param src       the source code representing the inserted bytecode.
     *                  It must be a single statement or block.
     */
    public void insertBeforeBody(String src) throws CannotCompileException {
        declaringClass.checkModify();
        CodeAttribute ca = methodInfo.getCodeAttribute();
        CodeIterator iterator = ca.iterator();
        Bytecode b = new Bytecode(methodInfo.getConstPool(),
                                  ca.getMaxStack(), ca.getMaxLocals());
        b.setStackDepth(ca.getMaxStack());
        Javac jv = new Javac(b, declaringClass);
        try {
            jv.recordParams(getParameterTypes(), false);
            jv.compileStmnt(src);
            ca.setMaxStack(b.getMaxStack());
            ca.setMaxLocals(b.getMaxLocals());
            iterator.skipConstructor();
            int pos = iterator.insertEx(b.get());
            iterator.insert(b.getExceptionTable(), pos);
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
        catch (CompileError e) {
            throw new CannotCompileException(e);
        }
        catch (BadBytecode e) {
            throw new CannotCompileException(e);
        }
    }

    /**
     * Inserts bytecode at the end of the constructor body.
     * The bytecode is inserted just before every return insturction.
     * It is not executed when an exception is thrown.
     *
     * @param src       the source code representing the inserted bytecode.
     *                  It must be a single statement or block.
     */
    public void insertAfter(String src)
        throws CannotCompileException
    {
        super.insertAfter(src);
    }

    /**
     * Inserts bytecode at the end of the constructor body.
     * The bytecode is inserted just before every return insturction.
     *
     * @param src       the source code representing the inserted bytecode.
     *                  It must be a single statement or block.
     * @param asFinally         true if the inserted bytecode is executed
     *                  not only when the transfer normally returns
     *                  but also when an exception is thrown.
     */
    public void insertAfter(String src, boolean asFinally)
        throws CannotCompileException
    {
        super.insertAfter(src, asFinally);
    }

    /**
     * Adds a catch clause that handles an exception thrown in the
     * constructor body.
     * The catch clause must end with a return or throw statement.
     *
     * @param src       the source code representing the catch clause.
     *                  It must be a single statement or block.
     * @param exceptionType     the type of the exception handled by the
     *                          catch clause.
     */
    public void addCatch(String src, CtClass exceptionType)
        throws CannotCompileException
    {
        super.addCatch(src, exceptionType);
    }
}
