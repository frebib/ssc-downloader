package net.frebib.sscdownloader.gui;

import net.frebib.sscdownloader.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

public class DownloadFrame extends JFrame implements Observer {
    private JPanel pnlMain, pnlTop, pnlButton;
    private JScrollPane scroller;
    private JList<DownloadTask> dlList;
    private DownloadListModel listModel;

    private JLabel lblUrl, lblDir;
    private JTextField txtUrl, txtSaveDir;
    private JSpinner numThreads;
    public  JButton btnChooseDir, btnFilter, btnGo;
    public final String GET_LINKS_LABEL     = "Grab Links!",
                        GETTING_LINKS_LABEL = "Grabbing Links ",
                        GET_FILES_LABEL     = "Download Files!",
                        GETTING_FILES_LABEL = "Downloading Files ",
                        DONE_LABEL          = "Downloads Complete!";

    private int count;
    private File destination;
    private FilterFrame filterFrame;
    private Status status = Status.UNINITIALIZED;

    // TODO: Add link type selection
    // TODO: Add ThreadCount changer box

    public DownloadFrame() {
        super();

        // TODO: Add thread count

        pnlMain = new JPanel(new GridBagLayout());
        pnlMain.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        pnlTop = new JPanel(new GridBagLayout());
        pnlButton = new JPanel(new GridBagLayout());
        pnlButton.setBorder(BorderFactory.createEmptyBorder(8, 0, 12, 0));

        lblUrl = new JLabel("Webpage");
        lblUrl.setHorizontalAlignment(SwingConstants.RIGHT);
        lblDir = new JLabel("Save Directory");
        lblDir.setHorizontalAlignment(SwingConstants.RIGHT);
        txtUrl = new JTextField();
        // TODO: Remove, for testing only
        txtUrl.setText("http://imgur.com/");
        txtSaveDir = new JTextField();
        // TODO: Remove, for testing only
        txtSaveDir.setText("/home/frebib/Downloads/imgur");

        int coreCount = Runtime.getRuntime().availableProcessors();
        SpinnerModel sm = new SpinnerNumberModel(coreCount * 2, 1, coreCount * 16, 1);
        numThreads = new JSpinner(sm);

        btnFilter = new JButton("FileType Filter");
        btnChooseDir = new JButton("Browse");
        btnGo = new JButton(GET_LINKS_LABEL);

        listModel = new DownloadListModel();
        dlList = new DownloadList(listModel);
        dlList.setCellRenderer(new DownloadCellRenderer());
        listModel.setList(dlList);
        scroller = new JScrollPane(dlList);

        GridBagConstraints c = new GridBagConstraints();
        Insets none = c.insets;
        Insets bottom = new Insets(0, 0, 8, 0);
        Insets bottomright = new Insets(0, 0, 8, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.insets = bottomright;
        pnlTop.add(lblUrl, c);

        c.gridx = 1;
        c.weightx = 1;
        c.gridwidth = 2;
        c.insets = bottom;
        pnlTop.add(txtUrl, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = bottomright;
        pnlTop.add(lblDir, c);

        c.gridx = 1;
        c.weightx = 1;
        c.insets = bottomright;
        pnlTop.add(txtSaveDir, c);

        c.gridx = 2;
        c.gridwidth = 1;
        c.weightx = 0;
        c.ipadx = 6;
        c.insets = bottom;
        pnlTop.add(btnChooseDir, c);

        GridBagConstraints bc = new GridBagConstraints();
        bc.gridy = 0;
        bc.gridx = GridBagConstraints.RELATIVE;
        bc.fill = GridBagConstraints.BOTH;

        bc.ipadx = 8;
        bc.weightx = 0.1;
        pnlButton.add(btnFilter, bc);

        bc.ipadx = 0;
        bc.weightx = 0.35;
        pnlButton.add(new JPanel(), bc);

        bc.ipadx = 8;
        bc.weightx = 0.4;
        pnlButton.add(btnGo, bc);

        bc.ipadx = 0;
        bc.weightx = 0.6;
        pnlButton.add(new JPanel(), bc);

        c.weightx = 1;
        c.insets = none;
        c.ipadx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.BOTH;
        pnlMain.add(pnlTop, c);
        pnlMain.add(pnlButton, c);

        c.weighty = 1;
        pnlMain.add(scroller, c);

        add(pnlMain);
        setPreferredSize(new Dimension(640, 800));
        pack();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        btnChooseDir.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int choice = chooser.showOpenDialog(this);
            if (choice == JFileChooser.APPROVE_OPTION)
                destination = chooser.getSelectedFile();
            else if (choice != JFileChooser.CANCEL_OPTION) {
                choice = JOptionPane.showConfirmDialog(this, "Invalid directory.\nPlease try again!",
                        "Invalid Save Directory", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.OK_OPTION)
                    btnChooseDir.doClick();
            }
        });
        btnFilter.addActionListener(e ->  {
            // TODO: Add btnFilter form here.
        });
    }

    @Override
    public void update(Observable o, Object arg) {
        updateStatus();
    }

    public Status getStatus() {
        return status;
    }
    public String getURL() {
        return txtUrl.getText();
    }
    public String getSaveDir() {
        return txtSaveDir.getText();
    }
    public int getThreadCount() {
        return (int) numThreads.getModel().getValue();
    }
    public DownloadListModel getListModel() {
        return listModel;
    }

    public void incDownloadCount() {
        this.count++;
        updateStatus();
    }
    public void decDownloadCount() {
        this.count--;
        updateStatus();
    }
    public void setDownloadCount(int count) {
        this.count = count;
        updateStatus();
    }
    public void updateStatus(Status status) {
        this.status = status;
        updateStatus();
    }
    public void updateStatus() {
        switch (this.status) {
            case UNINITIALIZED:
                btnGo.setText(GET_LINKS_LABEL);
                break;
            case GRABBING:
                btnGo.setText(GETTING_LINKS_LABEL + listModel.size() + " of " + (count + listModel.size()));
                break;
            case GRABBED:
                btnGo.setText(GET_FILES_LABEL);
                break;
            case DOWNLOADING:
                btnGo.setText(GETTING_FILES_LABEL + count + " of " + listModel.size());
                break;
            case DOWNLOADED:
                btnGo.setText(DONE_LABEL);
                break;
        }
    }

    public enum Status {
        UNINITIALIZED,
        GRABBING,
        GRABBED,
        DOWNLOADING,
        DOWNLOADED
    }
}