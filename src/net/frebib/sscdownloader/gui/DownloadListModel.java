package net.frebib.sscdownloader.gui;

import net.frebib.sscdownloader.DownloadTask;
import net.frebib.sscdownloader.DownloaderClient;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class DownloadListModel extends DefaultListModel<DownloadTask> implements Observer {
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
    public void addElement(DownloadTask dl) {
        if (dl == null || contains(dl)) return;
        super.addElement(dl);
        dl.addObserver(this);
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
