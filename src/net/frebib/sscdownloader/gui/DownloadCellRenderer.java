package net.frebib.sscdownloader.gui;

import net.frebib.sscdownloader.DownloadTask;

import javax.swing.*;
import java.awt.*;

public class DownloadCellRenderer extends JPanel implements ListCellRenderer<DownloadTask> {
    private final Color BG_COL, FG_COL, HL_BG_COL, HL_FG_COL;

    private JPanel downloadCol, textPanel;
    private JLabel filename, filepath;
    private JProgressBar progress;

    public DownloadCellRenderer() {
        super(new GridBagLayout());

        downloadCol = new JPanel();
        downloadCol.setPreferredSize(new Dimension(16, 1));

        textPanel = new JPanel(new GridBagLayout());

        filename = new JLabel();
        filepath = new JLabel();
        progress = new JProgressBar();

        filename.setHorizontalAlignment(SwingConstants.LEFT);
        filepath.setHorizontalAlignment(SwingConstants.LEFT);

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 0.5;
        c.insets = new Insets(4, 0, 0, 0);
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.HORIZONTAL;
        textPanel.add(filename, c);
        textPanel.add(filepath, c);
        textPanel.add(progress, c);

        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.insets = new Insets(0, 6, 0, 12);
        c.fill = GridBagConstraints.VERTICAL;
        this.add(downloadCol, c);

        c.gridx = 1;
        c.weightx = 1;
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.BOTH;
        this.add(textPanel, c);

        UIDefaults lf = UIManager.getLookAndFeel().getDefaults();
        BG_COL = this.getBackground();
        FG_COL = filename.getForeground();
        HL_BG_COL = lf.getColor("List.selectionBackground");
        HL_FG_COL = lf.getColor("List.selectionForeground");
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends DownloadTask> l, DownloadTask dl, int i, boolean isSelected, boolean cellHasFocus) {
        if (cellHasFocus) setColors(HL_BG_COL, HL_FG_COL);
        else setColors(BG_COL, FG_COL);

        downloadCol.setBackground(dl.getState().getCol());
        filename.setText(dl.getURL());
        filepath.setText("> " + dl.getFilepath());
        progress.setValue((int) dl.getProgress());

        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width = getParent().getWidth();
        return size;
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    private void setColors(Color bg, Color fg) {
        setBackground(bg);
        textPanel.setBackground(bg);
        filename.setForeground(fg);
        filepath.setForeground(fg);
        progress.setBackground(bg);
        progress.setForeground(bg);
    }
}
