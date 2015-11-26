package net.frebib.sscdownloader.gui;

import net.frebib.sscdownloader.DownloadTask;

import javax.swing.*;
import java.awt.event.*;

/**
 * A GUI Jlist object that displays {@link DownloadTask} objects,
 * their state and progress and updates when they change.
 */
public class DownloadList extends JList<DownloadTask> implements KeyListener {

    /**
     * Creates a new DownloadList supported by supplied ListModel
     * @param model model to contain the task objects
     */
    public DownloadList(ListModel<DownloadTask> model) {
        super(model);

        addKeyListener(this);
        addMouseListener(new RightClickHandler(this));
    }

    private JPopupMenu initMenu(DownloadTask task) {
        JPopupMenu menu = new JPopupMenu();
        menu.removeAll();

        JMenuItem mi = new JMenuItem(task.getFilename());
        mi.setEnabled(false);
        menu.add(mi);

        mi = new JMenuItem(String.format("%.1f%% Complete", task.getProgress()));
        mi.setEnabled(false);
        menu.add(mi);

        mi = new JMenuItem(humanReadableBytes(task.getBytes(), false) + " / " +
                           humanReadableBytes(task.getSize(), false));
        mi.setEnabled(false);
        menu.add(mi);

        mi = new JMenuItem(humanReadableBytes(task.getSize(), false));
        mi.setEnabled(false);
        menu.add(mi);

        menu.addSeparator();

        boolean paused = task.getState() == DownloadTask.State.PAUSED;
        mi = new JMenuItem(paused ? "Resume" : "Pause");
        mi.addActionListener(e -> {
            if (paused) task.resume();
            else task.pause();
        });
        menu.add(mi);

        mi = new JMenuItem("Cancel");
        mi.addActionListener(e -> task.cancel());
        menu.add(mi);

        return menu;
    }

    /**
     * Converts byte values into human-readable values
     * Courtesy of  http://stackoverflow.com/questions/3758606/
     * @param bytes amount of bytes to convert
     * @param si sets the base of calculation between 1000 & 1024
     * @return a formatted string representing the amount of bytes provided
     */
    public static String humanReadableBytes(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            DownloadTask dl = getSelectedValue();
            if (dl != null)
                dl.cancel();
        }
    }
    public void keyPressed(KeyEvent e) { }
    public void keyReleased(KeyEvent e) { }

    private class RightClickHandler extends MouseAdapter {
        private DownloadList list;

        public RightClickHandler(DownloadList list) {
            this.list = list;
        }

        public void onMouseEvent(MouseEvent e) {
            if (e.isPopupTrigger()) {
                DownloadTask mt = getModel().getElementAt(locationToIndex(e.getPoint()));
                setSelectedValue(mt, false);
                initMenu(mt).show(list, e.getX(), e.getY());
            }
        }
        public void mousePressed(MouseEvent e) { onMouseEvent(e); }
        public void mouseReleased(MouseEvent e) { onMouseEvent(e); }
    }
}
