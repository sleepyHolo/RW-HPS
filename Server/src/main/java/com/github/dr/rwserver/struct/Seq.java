package com.github.dr.rwserver.struct;

import com.github.dr.rwserver.math.Mathf;
import com.github.dr.rwserver.math.Rand;
import com.github.dr.rwserver.func.Boolf;
import com.github.dr.rwserver.func.Cons;
import com.github.dr.rwserver.util.log.exp.VariableException.ArrayRuntimeException;

import java.util.*;

/**
 * 可调整大小，有序或无序的对象数组。如果是无序的，则此类在删除元素时避免了内存复制（最后一个元素移动到删除的元素的位置）。
 * @author Nathan Sweet
 */
@SuppressWarnings("unchecked")
public class Seq<T> implements Iterable<T> {
    /** Debugging variable to count total number of iterators allocated.*/
    public static int iteratorsAllocated = 0;
    /**
     * Provides direct access to the underlying array. If the Array's generic type is not Object, this field may only be accessed
     * if the {@link Seq#Seq(boolean, int, Class)} constructor was used.
     */
    public T[] items;

    private int size;
    public boolean ordered;

    private SeqIterable<T> iterable;

    /** Creates an ordered array with a capacity of 16. */
    public Seq(){
        this(true, 16);
    }

    /** Creates an ordered array with the specified capacity. */
    public Seq(int capacity){
        this(true, capacity);
    }

    /** Creates an ordered array with the specified capacity. */
    public Seq(boolean ordered){
        this(ordered, 16);
    }

    /**
     * @param ordered If false, methods that remove elements may change the order of other elements in the array, which avoids a
     * memory copy.
     * @param capacity Any elements added beyond this will cause the backing array to be grown.
     */
    public Seq(boolean ordered, int capacity){
        this.ordered = ordered;
        items = (T[])new Object[capacity];
    }

    /**
     * Creates a new array with {@link #items} of the specified type.
     * @param ordered If false, methods that remove elements may change the order of other elements in the array, which avoids a
     * memory copy.
     * @param capacity Any elements added beyond this will cause the backing array to be grown.
     */
    public Seq(boolean ordered, int capacity, Class arrayType){
        this.ordered = ordered;
        items = (T[])java.lang.reflect.Array.newInstance(arrayType, capacity);
    }

    /** Creates an ordered array with {@link #items} of the specified type and a capacity of 16. */
    public Seq(Class arrayType){
        this(true, 16, arrayType);
    }

    /**
     * Creates a new array containing the elements in the specified array. The new array will have the same type of backing array
     * and will be ordered if the specified array is ordered. The capacity is set to the number of elements, so any subsequent
     * elements added will cause the backing array to be grown.
     */
    public Seq(Seq<? extends T> array){
        this(array.ordered, array.size, array.items.getClass().getComponentType());
        size = array.size;
        System.arraycopy(array.items, 0, items, 0, size);
    }

    /**
     * Creates a new ordered array containing the elements in the specified array. The new array will have the same type of
     * backing array. The capacity is set to the number of elements, so any subsequent elements added will cause the backing array
     * to be grown.
     */
    public Seq(T[] array){
        this(true, array, 0, array.length);
    }

    /**
     * Creates a new array containing the elements in the specified array. The new array will have the same type of backing array.
     * The capacity is set to the number of elements, so any subsequent elements added will cause the backing array to be grown.
     * @param ordered If false, methods that remove elements may change the order of other elements in the array, which avoids a
     * memory copy.
     */
    public Seq(boolean ordered, T[] array, int start, int count){
        this(ordered, count, array.getClass().getComponentType());
        size = count;
        System.arraycopy(array, start, items, 0, size);
    }

    /** @see #Seq(Class) */
    public static <T> Seq<T> of(Class<T> arrayType){
        return new Seq<>(arrayType);
    }

    /** @see #Seq(boolean, int, Class) */
    public static <T> Seq<T> of(boolean ordered, int capacity, Class<T> arrayType){
        return new Seq<>(ordered, capacity, arrayType);
    }

    public <T> Seq<T> withList(Object list){
        Seq<T> result = new Seq<>();
        if(list instanceof List<?>){
            for(Object a : (List<?>)list){
                if(a instanceof Seq){
                    result.addAll((Seq<? extends T>) a);
                }else{
                    result.add((T)a);
                }
            }
        }
        return result;
    }

    public static <T> Seq<T> withArrays(Object... arrays){
        Seq<T> result = new Seq<>();
        for(Object a : arrays){
            if(a instanceof Seq){
                result.addAll((Seq<? extends T>) a);
            }else{
                result.add((T)a);
            }
        }
        return result;
    }

    /** @see #Seq(Object[]) */
    public static <T> Seq<T> with(T... array){
        return new Seq<>(array);
    }

    public static <T> Seq<T> with(Iterable<T> array) {
        Seq<T> out = new Seq<>();
        for(T thing : array) {
            out.add(thing);
        }
        return out;
    }

    public Seq<T> copy(){
        return new Seq<>(this);
    }

    public ArrayList<T> list(){
        ArrayList<T> list = new ArrayList<>(size);
        each(list::add);
        return list;
    }

    public <E extends T> void neach(Boolf<? super T> pred, Cons<E> consumer, Runnable run) {
        for (int i = 0; i < size; i++) {
            if(pred.get(items[i])) {
                consumer.get((E)items[i]);
                return;
            }
        }
        run.run();
    }

    public <E extends T> void each(Boolf<? super T> pred, Cons<E> consumer) {
        for (int i = 0; i < size; i++) {
            if(pred.get(items[i])) {
                consumer.get((E)items[i]);
                return;
            }
        }
    }

    public <E extends T> void each(Boolf<? super T> pred,Boolf<? super T> pred2, Cons<E> consumer) {
        for (int i = 0; i < size; i++) {
            if(pred.get(items[i]) && pred2.get(items[i])) {
                consumer.get((E)items[i]);
                return;
            }
        }
    }

    public <E extends T> void eachs(Boolf<? super T> pred, Cons<E> consumer) {
        for (int i = 0; i < size; i++) {
            if(pred.get(items[i])) {
                consumer.get((E)items[i]);
            }
        }
    }

    public void each(Cons<? super T> consumer) {
        for (int i = 0; i < size; i++) {
            consumer.get(items[i]);
        }
    }


    /** Flattens this array of arrays into one array. Allocates a new instance.*/
    public <R> Seq<R> flatten() {
        Seq<R> arr = new Seq<>();
        for (int i = 0; i < size; i++) {
            arr.addAll((Seq<R>)items[i]);
        }
        return arr;
    }

    public T min(Comparator<T> func){
        T result = null;
        for(int i = 0; i < size; i++){
            T t = items[i];
            if(result == null || func.compare(result, t) > 0){
                result = t;
            }
        }
        return result;
    }

    public T max(Comparator<T> func){
        T result = null;
        for(int i = 0; i < size; i++){
            T t = items[i];
            if(result == null || func.compare(result, t) < 0){
                result = t;
            }
        }
        return result;
    }

    public Seq<T> and(T value){
        add(value);
        return this;
    }

    public void add(T value){
        T[] items = this.items;
        if(size == items.length) {
            items = resize(Math.max(8, (int)(size * 1.75f)));
        }
        items[size++] = value;
    }

    public void add(T value1, T value2){
        T[] items = this.items;
        if(size + 1 >= items.length) {
            items = resize(Math.max(8, (int)(size * 1.75f)));
        }
        items[size] = value1;
        items[size + 1] = value2;
        size += 2;
    }

    public void add(T value1, T value2, T value3){
        T[] items = this.items;
        if(size + 2 >= items.length) {
            items = resize(Math.max(8, (int)(size * 1.75f)));
        }
        items[size] = value1;
        items[size + 1] = value2;
        items[size + 2] = value3;
        size += 3;
    }

    public void add(T value1, T value2, T value3, T value4){
        T[] items = this.items;
        if(size + 3 >= items.length) {
            /* 1.75 isn't enough when size=5 */
            items = resize(Math.max(8, (int)(size * 1.8f)));
        }
        items[size] = value1;
        items[size + 1] = value2;
        items[size + 2] = value3;
        items[size + 3] = value4;
        size += 4;
    }

    public Seq<T> addAll(Seq<? extends T> array){
        addAll(array.items, 0, array.size);

        return this;
    }

    public Seq<T> addAll(Seq<? extends T> array, int start, int count){
        if(start + count > array.size) {
            throw new IllegalArgumentException("start + count must be <= size: " + start + " + " + count + " <= " + array.size);
        }
        addAll(array.items, start, count);

        return this;
    }

    public Seq<T> addAll(T... array){
        addAll(array, 0, array.length);

        return this;
    }

    public Seq<T> addAll(T[] array, int start, int count){
        T[] items = this.items;
        int sizeNeeded = size + count;
        if(sizeNeeded > items.length) {
            items = resize(Math.max(8, (int)(sizeNeeded * 1.75f)));
        }
        System.arraycopy(array, start, items, size, count);
        size += count;

        return this;
    }

    public Seq<T> addAll(Iterable<? extends T> items){
        if(items instanceof Seq){
            addAll((Seq)items);
        }else{
            for(T t : items){
                add(t);
            }
        }

        return this;
    }

    /** Sets this array's contents to the specified array.*/
    public void set(Seq<? extends T> array){
        clear();
        addAll(array);
    }

    public T get(int index){
        if(index >= size) {
            throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
        }
        return items[index];
    }

    public void set(int index, T value){
        if(index >= size) {
            throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
        }
        items[index] = value;
    }

    public void insert(int index, T value){
        if(index > size) {
            throw new IndexOutOfBoundsException("index can't be > size: " + index + " > " + size);
        }
        T[] items = this.items;
        if(size == items.length) {
            items = resize(Math.max(8, (int)(size * 1.75f)));
        }
        if(ordered) {
            System.arraycopy(items, index, items, index + 1, size - index);
        } else {
            items[size] = items[index];
        }
        size++;
        items[index] = value;
    }

    public void swap(int first, int second){
        if(first >= size) {
            throw new IndexOutOfBoundsException("first can't be >= size: " + first + " >= " + size);
        }
        if(second >= size) {
            throw new IndexOutOfBoundsException("second can't be >= size: " + second + " >= " + size);
        }
        T[] items = this.items;
        T firstValue = items[first];
        items[first] = items[second];
        items[second] = firstValue;
    }

    public T find(Boolf<T> predicate){
        for(int i = 0; i < size; i++){
            if(predicate.get(items[i])){
                return items[i];
            }
        }
        return null;
    }

    public boolean contains(T value){
        return contains(value, false);
    }

    /**
     * Returns if this array contains value.
     * @param value May be null.
     * @param identity If true, == comparison will be used. If false, .equals() comparison will be used.
     * @return true if array contains value, false if it doesn't
     */
    public boolean contains(T value, boolean identity){
        T[] items = this.items;
        int i = size - 1;
        if(identity || value == null){
            while(i >= 0) {
                if(items[i--] == value) {
                    return true;
                }
            }
        }else{
            while(i >= 0) {
                if(value.equals(items[i--])) {
                    return true;
                }
            }
        }
        return false;
    }

    public int indexOf(T value){
        return indexOf(value, false);
    }

    /**
     * Returns the index of first occurrence of value in the array, or -1 if no such value exists.
     * @param value May be null.
     * @param identity If true, == comparison will be used. If false, .equals() comparison will be used.
     * @return An index of first occurrence of value in array or -1 if no such value exists
     */
    public int indexOf(T value, boolean identity){
        T[] items = this.items;
        if(identity || value == null){
            for(int i = 0, n = size; i < n; i++) {
                if(items[i] == value) {
                    return i;
                }
            }
        }else{
            for(int i = 0, n = size; i < n; i++) {
                if(value.equals(items[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns an index of last occurrence of value in array or -1 if no such value exists. Search is started from the end of an
     * array.
     * @param value May be null.
     * @param identity If true, == comparison will be used. If false, .equals() comparison will be used.
     * @return An index of last occurrence of value in array or -1 if no such value exists
     */
    public int lastIndexOf(T value, boolean identity){
        T[] items = this.items;
        if(identity || value == null){
            for(int i = size - 1; i >= 0; i--) {
                if(items[i] == value) {
                    return i;
                }
            }
        }else{
            for(int i = size - 1; i >= 0; i--) {
                if(value.equals(items[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /** Removes a value, without using identity. */
    public boolean remove(T value){
        return remove(value, false);
    }

    /**
     * Removes the first instance of the specified value in the array.
     * @param value May be null.
     * @param identity If true, == comparison will be used. If false, .equals() comparison will be used.
     * @return true if value was found and removed, false otherwise
     */
    public boolean remove(T value, boolean identity){
        T[] items = this.items;
        if(identity || value == null){
            for(int i = 0, n = size; i < n; i++){
                if(items[i] == value){
                    remove(i);
                    return true;
                }
            }
        }else{
            for(int i = 0, n = size; i < n; i++){
                if(value.equals(items[i])){
                    remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    /** Removes and returns the item at the specified index. */
    public T remove(int index){
        if(index >= size) {
            throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
        }
        T[] items = this.items;
        T value = items[index];
        size--;
        if(ordered) {
            System.arraycopy(items, index + 1, items, index, size - index);
        } else {
            items[index] = items[size];
        }
        items[size] = null;
        return value;
    }

    /** Removes the items between the specified indices, inclusive. */
    public void removeRange(int start, int end){
        if(end >= size) {
            throw new IndexOutOfBoundsException("end can't be >= size: " + end + " >= " + size);
        }
        if(start > end) {
            throw new IndexOutOfBoundsException("start can't be > end: " + start + " > " + end);
        }
        T[] items = this.items;
        int count = end - start + 1;
        if(ordered) {
            System.arraycopy(items, start + count, items, start, size - (start + count));
        } else{
            int lastIndex = this.size - 1;
            for(int i = 0; i < count; i++) {
                items[start + i] = items[lastIndex - i];
            }
        }
        size -= count;
    }

    public boolean removeAll(Seq<? extends T> array){
        return removeAll(array, false);
    }

    /**
     * Removes from this array all of elements contained in the specified array.
     * @param identity True to use ==, false to use .equals().
     * @return true if this array was modified.
     */
    public boolean removeAll(Seq<? extends T> array, boolean identity){
        int size = this.size;
        int startSize = size;
        T[] items = this.items;
        if(identity){
            for(int i = 0, n = array.size; i < n; i++){
                T item = array.get(i);
                for(int ii = 0; ii < size; ii++){
                    if(item == items[ii]){
                        remove(ii);
                        size--;
                        break;
                    }
                }
            }
        }else{
            for(int i = 0, n = array.size; i < n; i++){
                T item = array.get(i);
                for(int ii = 0; ii < size; ii++){
                    if(item.equals(items[ii])){
                        remove(ii);
                        size--;
                        break;
                    }
                }
            }
        }
        return size != startSize;
    }

    /** Removes and returns the last item. */
    public T pop(){
        if(size == 0) {
            throw new IllegalStateException("Array is empty.");
        }
        --size;
        T item = items[size];
        items[size] = null;
        return item;
    }

    /** Returns the last item. */
    public T peek(){
        if(size == 0) {
            throw new IllegalStateException("Array is empty.");
        }
        return items[size - 1];
    }

    /** Returns the first item. */
    public T first(){
        if(size == 0) {
            throw new IllegalStateException("Array is empty.");
        }
        return items[0];
    }

    /** Returns true if the array is empty. */
    public boolean isEmpty(){
        return size == 0;
    }

    public boolean any(){
        return size > 0;
    }

    public Seq<T> clear(){
        T[] items = this.items;
        for(int i = 0, n = size; i < n; i++) {
            items[i] = null;
        }
        size = 0;

        return this;
    }

    /**
     * Reduces the size of the backing array to the size of the actual items. This is useful to release memory when many items
     * have been removed, or if it is known that more items will not be added.
     * @return {@link #items}
     */
    public T[] shrink(){
        if(items.length != size) {
            resize(size);
        }
        return items;
    }

    /**
     * Increases the size of the backing array to accommodate the specified number of additional items. Useful before adding many
     * items to avoid multiple backing array resizes.
     * @return {@link #items}
     */
    public T[] ensureCapacity(int additionalCapacity){
        if(additionalCapacity < 0) {
            throw new IllegalArgumentException("additionalCapacity must be >= 0: " + additionalCapacity);
        }
        int sizeNeeded = size + additionalCapacity;
        if(sizeNeeded > items.length) {
            resize(Math.max(8, sizeNeeded));
        }
        return items;
    }

    /**
     * Sets the array size, leaving any values beyond the current size null.
     * @return {@link #items}
     */
    public T[] setSize(int newSize){
        truncate(newSize);
        if(newSize > items.length) {
            resize(Math.max(8, newSize));
        }
        size = newSize;
        return items;
    }

    /** Creates a new backing array with the specified size containing the current items. */
    protected T[] resize(int newSize){
        T[] items = this.items;
        //avoid reflection when possible
        T[] newItems = (T[])(items.getClass() == Object[].class ? new Object[newSize] : java.lang.reflect.Array.newInstance(items.getClass().getComponentType(), newSize));
        System.arraycopy(items, 0, newItems, 0, Math.min(size, newItems.length));
        this.items = newItems;
        return newItems;
    }

    public <R> Seq<R> as(){
        return (Seq<R>)this;
    }


    public void reverse(){
        T[] items = this.items;
        for(int i = 0, lastIndex = size - 1, n = size / 2; i < n; i++){
            int ii = lastIndex - i;
            T temp = items[i];
            items[i] = items[ii];
            items[ii] = temp;
        }
    }

    public void shuffle(){
        T[] items = this.items;
        for(int i = size - 1; i >= 0; i--){
            int ii = Mathf.random(i);
            T temp = items[i];
            items[i] = items[ii];
            items[ii] = temp;
        }
    }

    /**
     * Reduces the size of the array to the specified size. If the array is already smaller than the specified size, no action is
     * taken.
     */
    public void truncate(int newSize){
        if(newSize < 0) {
            throw new IllegalArgumentException("newSize must be >= 0: " + newSize);
        }
        if(size <= newSize) {
            return;
        }
        for(int i = newSize; i < size; i++) {
            items[i] = null;
        }
        size = newSize;
    }

    public T random(Rand rand){
        if(size == 0) {
            return null;
        }
        return items[rand.random(0, size - 1)];
    }

    /** Returns a random item from the array, or null if the array is empty. */
    public T random(){
        return random(Mathf.random);
    }

    /** Returns a random item from the array, excluding the specified element. If the array is empty, returns null.
     * If this array only has one element, returns that element. */
    public T random(T exclude){
        if(exclude == null) {
            return random();
        }
        if(size == 0) {
            return null;
        }
        if(size == 1) {
            return first();
        }

        int eidx = indexOf(exclude);
        //this item isn't even in the array!
        if(eidx == -1) {
            return random();
        }

        //shift up the index
        int index = Mathf.random(0, size - 2);
        if(index >= eidx){
            index ++;
        }

        return items[index];
    }

    /**
     * Returns the items as an array. Note the array is typed, so the {@link #Seq(Class)} constructor must have been used.
     * Otherwise use {@link #toArray(Class)} to specify the array type.
     */
    public T[] toArray(){
        return toArray(items.getClass().getComponentType());
    }

    public <V> V[] toArray(Class type){
        V[] result = (V[])java.lang.reflect.Array.newInstance(type, size);
        System.arraycopy(items, 0, result, 0, size);
        return result;
    }

    @Override
    public int hashCode(){
        if(!ordered) {
            return super.hashCode();
        }
        Object[] items = this.items;
        int h = 1;
        for(int i = 0, n = size; i < n; i++){
            h *= 31;
            Object item = items[i];
            if(item != null) {
                h += item.hashCode();
            }
        }
        return h;
    }

    @Override
    public boolean equals(Object object){
        if(object == this) {
            return true;
        }
        if(!ordered) {
            return false;
        }
        if(!(object instanceof Seq)) {
            return false;
        }
        Seq array = (Seq)object;
        if(!array.ordered) {
            return false;
        }
        int n = size;
        if(n != array.size) {
            return false;
        }
        Object[] items1 = this.items;
        Object[] items2 = array.items;
        for(int i = 0; i < n; i++){
            Object o1 = items1[i];
            Object o2 = items2[i];
            if(!(Objects.equals(o1, o2))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString(){
        if(size == 0) {
            return "[]";
        }
        T[] items = this.items;
        StringBuilder buffer = new StringBuilder(32);
        buffer.append('[');
        buffer.append(items[0]);
        for(int i = 1; i < size; i++){
            buffer.append(", ");
            buffer.append(items[i]);
        }
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

    /**
     * Returns an iterator for the items in the array. Remove is supported. Note that the same iterator instance is returned each
     * time this method is called, unless you are using nested loops.
     * <b>Never, ever</b> access this iterator's method manually, e.g. hasNext()/next().
     * Note that calling 'break' while iterating will permanently clog this iterator, falling back to an implementation that allocates new ones.
     */
    @Override
    public Iterator<T> iterator(){
        if(iterable == null) {
            iterable = new SeqIterable<>(this);
        }
        return iterable.iterator();
    }

    public static class SeqIterable<T> implements Iterable<T>{
        private final Seq<T> array;
        private final boolean allowRemove;
        private final SeqIterator iterator1 = new SeqIterator();
        private final SeqIterator iterator2 = new SeqIterator();

        public SeqIterable(Seq<T> array){
            this(array, true);
        }

        public SeqIterable(Seq<T> array, boolean allowRemove){
            this.array = array;
            this.allowRemove = allowRemove;
        }

        @Override
        public Iterator<T> iterator(){
            if(iterator1.done){
                iterator1.index = 0;
                iterator1.done = false;
                return iterator1;
            }

            if(iterator2.done){
                iterator2.index = 0;
                iterator2.done = false;
                return iterator2;
            }
            //allocate new iterator in the case of 3+ nested loops.
            return new SeqIterator();
        }

        private class SeqIterator implements Iterator<T>{
            int index;
            boolean done = true; {
                iteratorsAllocated ++;
            }

            @Override
            public boolean hasNext(){
                if(index >= array.size) {
                    done = true;
                }
                return index < array.size;
            }

            @Override
            public T next(){
                if(index >= array.size) {
                    throw new NoSuchElementException(String.valueOf(index));
                }
                return array.items[index++];
            }

            @Override
            public void remove(){
                if(!allowRemove) {
                    throw new ArrayRuntimeException("Remove not allowed.");
                }
                index--;
                array.remove(index);
            }
        }
    }
}
