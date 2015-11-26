package net.frebib.sscdownloader.gui;

import net.frebib.sscdownloader.MimeType;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a {@link ListModel} that uses an Array for storing data.
 * Updates the related JList when any element is added, removed or updated in the list
 * @param <T> the type of data stored in the {@link ListModel}
 */
public class ArrayListModel<T> extends AbstractListModel<T> {
    private ArrayList<T> list;

    public ArrayListModel() {
        this.list = new ArrayList<T>();
    }
    public ArrayListModel(List<T> mimes) {
        this.list = new ArrayList<T>(mimes);
    }

    /**
     * Adds an element to the {@link ListModel}
     * @param e element to add
     * @return {@code true} if successful
     */
    public boolean add(T e) {
        boolean b = list.add(e);
        int index = list.indexOf(e);
        fireIntervalAdded(e, index, index);
        return b;
    }

    /**
     * Adds an element to the {@link ListModel} at an index
     * @param index index to insert element at
     * @param e element to add
     */
    public void add(int index, T e) {
        list.add(index, e);
        fireIntervalAdded(e, index, list.size());
    }

    /**
     * Add a {@link Collection} to the {@link ListModel}
     * @param c collection to add
     */
    public void addAll(Collection<? extends T> c) {
        list.addAll(c);
        fireIntervalAdded(c, 0, list.size());
    }

    /**
     * Remove an element from the {@link ListModel}
     * @param e element to remove
     */
    public void remove(T e) {
        int index = list.indexOf(e);
        list.remove(e);
        fireIntervalRemoved(e, index, index);
    }

    /**
     * Remove an element at a specified index from the {@link ListModel}
     * @param index index to remove element from
     */
    public void remove(int index) {
        T e = list.get(index);
        list.remove(index);
        fireIntervalRemoved(e, index, index);
    }

    /**
     * Gets whether the {@link ListModel} contains an element
     * @param e element to check
     * @return {@code true} if {@link ListModel} contains element e
     */
    public boolean contains(T e) {
        return list.contains(e);
    }

    /**
     * Gets the index of an element in the {@link ListModel}
     * @param o element to get index for
     * @return index of element or -1 if it is not in the list
     */
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    /**
     * Removes all elements from the {@link ListModel}
     */
    public void clear() {
        list.clear();
        fireIntervalRemoved(this, 0, list.size());
    }

    /**
     * Gets if the list is empty
     * @return {@code true} if there are no elements in the list
     */
    public boolean isEmpty() {
        return list.size() < 1;
    }

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public T getElementAt(int index) {
        return list.get(index);
    }

    /**
     * Gets the underlying {@link ArrayList} that
     * stores the elements in the list
     * @return the underlying list
     */
    public ArrayList<T> getList() {
        return list;
    }
}