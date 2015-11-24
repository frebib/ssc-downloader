package net.frebib.sscdownloader.gui;

import net.frebib.sscdownloader.MimeType;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ArrayListModel<T> extends AbstractListModel<T> {
    private ArrayList<T> list;

    public ArrayListModel() {
        this.list = new ArrayList<T>();
    }

    public boolean add(T e) {
        boolean b = list.add(e);
        int index = list.indexOf(e);
        fireIntervalAdded(e, index, index);
        return b;
    }

    public void add(int index, T e) {
        list.add(index, e);
        fireIntervalAdded(e, index, index);
    }
    public void addAll(Collection<? extends T> c) {
        list.addAll(c);
        fireIntervalAdded(c, 0, list.size());
    }

    public void remove(T e) {
        int index = list.indexOf(e);
        list.remove(e);
        fireIntervalRemoved(e, index, index);
    }

    public void remove(int index) {
        T e = list.get(index);
        list.remove(index);
        fireIntervalRemoved(e, index, index);
    }

    public void clear() {
        list.clear();
    }

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public T getElementAt(int index) {
        return list.get(index);
    }

    public ArrayList<T> getList() {
        return list;
    }
}