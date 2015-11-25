package net.frebib.sscdownloader.gui;

import net.frebib.sscdownloader.DownloadTask;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

public class DownloadListModel extends ArrayListModel<DownloadTask> implements Observer {
    private JList<DownloadTask> list;
    private int lastIndex;

    public DownloadListModel() {
        super();
    }

    public void setList(JList<DownloadTask> list) {
        this.list=list;
    }
    @Override
    public void add(int index, DownloadTask dl) {
        if (dl == null || contains(dl)) return;
        super.add(index, dl);
        dl.addObserver(this);
    }
    @Override
    public boolean add(DownloadTask dl) {
        if (dl == null || contains(dl)) return false;
        boolean ret = super.add(dl);
        dl.addObserver(this);
        return ret;
    }
    @Override
    public void update(Observable o, Object arg) {
        int index = indexOf(o);
        if (index < 0) return;

        lastIndex = Math.max(index, lastIndex);

        DownloadTask task = (DownloadTask)o;
        if (task.getState() == DownloadTask.State.INITIALISED)
            SwingUtilities.invokeLater(() -> list.ensureIndexIsVisible(lastIndex));

        fireContentsChanged(o, index, index);
    }
}
