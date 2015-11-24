package net.frebib.sscdownloader.gui;

import net.frebib.sscdownloader.DownloadTask;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class DownloadList extends JList<DownloadTask> implements MouseListener, KeyListener {

    public DownloadList(ListModel<DownloadTask> model) {
        super(model);

        addMouseListener(this);
        addKeyListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger()) {
            // TODO: Trigger right click menu
        }
    }
    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            DownloadTask dl = getSelectedValue();
            if (dl != null)
                dl.cancel();
        }
    }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void keyPressed(KeyEvent e) { }
    public void keyReleased(KeyEvent e) { }
}
