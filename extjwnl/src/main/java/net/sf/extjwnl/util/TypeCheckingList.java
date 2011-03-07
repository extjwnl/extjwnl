package net.sf.extjwnl.util;

import net.sf.extjwnl.JWNLRuntimeException;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Wrapper for a list that checks the type of arguments before putting them in the list.
 * It also does type-checking on methods which iterate over the list so that they fail
 * fast if the argument is not of the correct type.
 *
 * @author John Didion <jdidion@users.sourceforge.net>
 */
public class TypeCheckingList implements List, DeepCloneable {
    private List list;
    private Class type;

    public TypeCheckingList(Class type) {
        this(new ArrayList(), type);
    }

    public TypeCheckingList(List backingList, Class type) {
        init(backingList, type);
    }

    /**
     * Create a new Type checking list that checks for type <var>type</var>, but only if <var>parentType</var> is
     * equal to, a super class/interface of, or an interface implemented by <var>type</var>.
     */
    protected TypeCheckingList(List backingList, Class type, Class parentType) {
        if (!parentType.isAssignableFrom(type)) {
            throw new JWNLRuntimeException("UTILS_EXCEPTION_001", new Object[]{type, parentType});
        }
        init(backingList, type);
    }

    private void init(List backingList, Class type) {
        this.type = type;
        if (!backingList.isEmpty()) {
            checkType(backingList);
        }
        list = backingList;
    }

    public Class getType() {
        return type;
    }

    private List getList() {
        return list;
    }

    public Object clone() throws CloneNotSupportedException {
        super.clone();
        return new TypeCheckingList(copyBackingList(), getType());
    }

    /**
     * Make a copy of the wrapped list - used by subclasses when the overriding the clone method
     */
    protected List copyBackingList() throws CloneNotSupportedException {
        try {
            Method cloneMethod = getList().getClass().getMethod("clone");
            return (List) cloneMethod.invoke(getList());
        } catch (Exception c) {
            throw new CloneNotSupportedException();
        }
    }

    public Object deepClone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    // object methods

    public boolean equals(Object obj) {
        return (obj instanceof TypeCheckingList) && super.equals(obj);
    }

    // methods that do type-checking

    public boolean add(Object o) {
        checkType(o);
        return getList().add(o);
    }

    public void add(int index, Object o) {
        checkType(o);
        getList().add(index, o);
    }

    public boolean addAll(Collection c) {
        checkType(c);
        return getList().addAll(c);
    }

    public boolean addAll(int index, Collection c) {
        checkType(c);
        return getList().addAll(index, c);
    }

    public boolean contains(Object o) {
        try {
            checkType(o);
            return getList().contains(o);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean containsAll(Collection c) {
        try {
            checkType(c);
            return getList().containsAll(c);
        } catch (Exception e) {
            return false;
        }
    }

    public Object set(int index, Object element) {
        checkType(element);
        return getList().set(index, element);
    }

    public int indexOf(Object o) {
        try {
            checkType(o);
            return getList().indexOf(o);
        } catch (Exception e) {
            return -1;
        }
    }

    public int lastIndexOf(Object o) {
        try {
            checkType(o);
            return getList().lastIndexOf(o);
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean remove(Object o) {
        try {
            checkType(o);
            return getList().remove(o);
        } catch (Exception e) {
            return false;
        }
    }

    // listIterator methods

    public ListIterator listIterator() {
        return getTypeCheckingListIterator();
    }

    public ListIterator listIterator(int index) {
        return getTypeCheckingListIterator(index);
    }

    protected TypeCheckingListIterator getTypeCheckingListIterator() {
        return getTypeCheckingListIterator(0);
    }

    protected TypeCheckingListIterator getTypeCheckingListIterator(int index) {
        return new TypeCheckingListIterator(getList().listIterator(index));
    }

    // pass-through methods

    public int size() {
        return getList().size();
    }

    public boolean isEmpty() {
        return getList().isEmpty();
    }

    public Iterator iterator() {
        return getList().iterator();
    }

    public Object[] toArray() {
        return getList().toArray();
    }

    // type-checking happens already with this method, so we don't have to explicitly do it
    public Object[] toArray(Object[] a) {
        return getList().toArray(a);
    }

    // type-checking for fail-fast doesn't really improve the performance of this method
    public boolean removeAll(Collection c) {
        return getList().removeAll(c);
    }

    // type-checking for fail-fast doesn't really improve the performance of this method
    public boolean retainAll(Collection c) {
        return getList().retainAll(c);
    }

    public void clear() {
        getList().clear();
    }

    public Object get(int index) {
        return getList().get(index);
    }

    public Object remove(int index) {
        return getList().remove(index);
    }

    public List subList(int fromIndex, int toIndex) {
        return getList().subList(fromIndex, toIndex);
    }

    // type checking methods

    private void checkType(Object obj) {
        if (!getType().isInstance(obj)) {
            throw new JWNLRuntimeException("UTILS_EXCEPTION_003", getType());
        }
    }

    private Collection lastCheckedCollection = null;

    private void checkType(Collection c) {
        if (c != lastCheckedCollection) {
            for (Object aC : c) {
                checkType(aC);
            }
        }
        lastCheckedCollection = c;
    }

    public class TypeCheckingListIterator implements ListIterator {
        private ListIterator itr;

        /**
         * Create a TypeCheckingListIterator from a ListIterator.
         */
        private TypeCheckingListIterator(ListIterator itr) {
            this.itr = itr;
        }

        public Class getType() {
            return TypeCheckingList.this.getType();
        }

        // Methods that do type-checking.

        public void set(Object o) {
            checkType(o);
            getListIterator().set(o);
        }

        public void add(Object o) {
            checkType(o);
            getListIterator().add(o);
        }

        // Pass-through methods

        public boolean hasNext() {
            return getListIterator().hasNext();
        }

        public Object next() {
            return getListIterator().next();
        }

        public boolean hasPrevious() {
            return getListIterator().hasPrevious();
        }

        public Object previous() {
            return getListIterator().previous();
        }

        public int nextIndex() {
            return getListIterator().nextIndex();
        }

        public int previousIndex() {
            return getListIterator().previousIndex();
        }

        public void remove() {
            getListIterator().remove();
        }

        private ListIterator getListIterator() {
            return itr;
        }
    }
}