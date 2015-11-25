package net.frebib.sscdownloader.gui;

import net.frebib.sscdownloader.DownloadTask;
import net.frebib.sscdownloader.MimeTypeCollection;
import net.frebib.sscdownloader.WebpageCrawler;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

public class DownloadFrame extends JFrame implements Observer {
    private JPanel pnlMain, pnlTop, pnlButton;
    private JScrollPane scroller;
    private JList<DownloadTask> dlList;
    private DownloadListModel listModel;

    private JLabel lblUrl, lblDir, lblThreads;
    private JTextField txtUrl, txtSaveDir;
    private JCheckBox chkAnchor, chkImage;
    private JSpinner numThreads;
    public JButton btnBrowse, btnFilter, btnGo, btnMenu;
    public final String GET_LINKS_LABEL = "Grab Links!",
            GETTING_LINKS_LABEL = "Grabbing Links ",
            GET_FILES_LABEL = "Download Files!",
            GETTING_FILES_LABEL = "Downloading Files ",
            DONE_LABEL = "Complete! Click to reset...";

    private int count;
    private FilterFrame filterFrame;
    private Status status = Status.UNINITIALIZED;
    private WebpageCrawler.LinkType linkType = WebpageCrawler.LinkType.Both;

    private MimeTypeCollection mimeTypes;

    public DownloadFrame() {
        super();

        pnlMain = new JPanel(new GridBagLayout());
        pnlMain.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        pnlTop = new JPanel(new GridBagLayout());
        pnlButton = new JPanel(new GridBagLayout());
        pnlButton.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        lblUrl = new JLabel("Webpage");
        lblUrl.setHorizontalAlignment(SwingConstants.RIGHT);
        lblDir = new JLabel("Save Directory");
        lblDir.setHorizontalAlignment(SwingConstants.RIGHT);
        lblThreads = new JLabel("Threads");
        lblThreads.setHorizontalAlignment(SwingConstants.RIGHT);
        txtUrl = new JTextField();
        txtUrl.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                String newStr = new StringBuilder(txtUrl.getText()).insert(offs, str).toString();
                if (newStr.matches("^([!#$&-;=?-\\[\\]_a-z~]|%[0-9a-fA-F]{2})+$"))
                    super.insertString(offs, str, a);
            }
        });
        txtSaveDir = new JTextField();

        int coreCount = Runtime.getRuntime().availableProcessors();
        SpinnerModel sm = new SpinnerNumberModel(coreCount * 2, 1, coreCount * 16, 1);
        numThreads = new JSpinner(sm);
        chkAnchor = new JCheckBox("Anchor tags <a/>", true);
        chkImage = new JCheckBox("Image tags <img/>", true);
        ActionListener chk = e -> {
            if (chkImage.isSelected()) {
                if (chkAnchor.isSelected())
                    linkType = WebpageCrawler.LinkType.Both;
                else
                    linkType = WebpageCrawler.LinkType.Image;
            } else if (chkAnchor.isSelected())
                linkType = WebpageCrawler.LinkType.Anchor;
            else
                ((JCheckBox) e.getSource()).setSelected(true);
        };
        chkAnchor.addActionListener(chk);
        chkImage.addActionListener(chk);

        mimeTypes = MimeTypeCollection.WILDCARD;
        filterFrame = new FilterFrame(this, mimeTypes);
        btnFilter = new JButton("Filter");
        btnFilter.addActionListener(e -> {
            filterFrame.setVisible(true);
            mimeTypes = filterFrame.getMimeTypes();
        });
        btnBrowse = new JButton("Browse");
        btnGo = new JButton(GET_LINKS_LABEL);
        btnMenu = new JButton("\uF0C9");
        btnMenu.addMouseListener(this);

        listModel = new DownloadListModel();
        dlList = new DownloadList(listModel);
        dlList.setCellRenderer(new DownloadCellRenderer());
        listModel.setList(dlList);
        scroller = new JScrollPane(dlList);
        scroller.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        GridBagConstraints c = new GridBagConstraints();
        Insets none = c.insets;
        Insets bottom = new Insets(0, 0, 8, 0);
        Insets bottomright = new Insets(0, 0, 8, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = bottomright;
        pnlTop.add(lblUrl, c);
        pnlTop.add(lblDir, c);
        pnlTop.add(lblThreads, c);

        c.gridx = 1;
        c.weightx = 1;
        c.gridwidth = 3;
        c.insets = bottom;
        pnlTop.add(txtUrl, c);

        c.gridwidth = 2;
        c.insets = bottomright;
        pnlTop.add(txtSaveDir, c);

        c.gridx = 3;
        c.gridwidth = 1;
        c.weightx = 0;
        c.ipadx = 6;
        c.insets = bottom;
        pnlTop.add(btnBrowse, c);

        JPanel pnlChecks = new JPanel(new GridBagLayout());
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 2;
        c.weightx = 1;
        c.gridwidth = 3;
        pnlTop.add(pnlChecks, c);

        c = new GridBagConstraints();
        c.gridx = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.7;
        c.insets = new Insets(0, 0, 8, 24);
        pnlChecks.add(numThreads, c);

        c.weightx = 1;
        pnlChecks.add(chkAnchor, c);

        c.insets = bottom;
        pnlChecks.add(chkImage, c);

        c = new GridBagConstraints();
        c.gridx = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.BOTH;

        c.ipadx = 8;
        c.weightx = 0.05;
        pnlButton.add(btnFilter, c);

        c.ipadx = 0;
        c.weightx = 0.2;
        pnlButton.add(new JPanel(), c);

        c.ipadx = 8;
        c.weightx = 0.5;
        pnlButton.add(btnGo, c);

        c.ipadx = 0;
        c.weightx = 0.25;
        pnlButton.add(new JPanel(), c);

        c.ipadx = 8;
        c.weightx = 0;
        pnlButton.add(btnMenu, c);

        c.weightx = 1;
        c.insets = none;
        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.BOTH;
        pnlMain.add(pnlTop, c);
        pnlMain.add(pnlButton, c);

        c.weighty = 1;
        pnlMain.add(scroller, c);

        add(pnlMain);
        setPreferredSize(new Dimension(640, 800));
        pack();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        btnBrowse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            File dest = null;
            int choice = chooser.showOpenDialog(this);
            if (choice == JFileChooser.APPROVE_OPTION)
                dest = chooser.getSelectedFile();
            else if (choice != JFileChooser.CANCEL_OPTION) {
                choice = JOptionPane.showConfirmDialog(this, "Invalid directory.\nPlease try again!",
                        "Invalid Save Directory", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.OK_OPTION)
                    btnBrowse.doClick();
            }
            if (dest != null)
                txtSaveDir.setText(dest.getAbsolutePath());
        });
    }

    @Override
    public void mouseClicked(MouseEvent ev) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem mi;

        mi = new JMenuItem("Filter Again");
        mi.addActionListener(e -> {
        });
        if (!status.atLeast(Status.GRABBED))
            mi.setEnabled(false);
        menu.add(mi);

        mi = new JMenuItem("Fetch Again");
        mi.addActionListener(e -> {
        });
        if (!status.atLeast(Status.GRABBED))
            mi.setEnabled(false);
        menu.add(mi);

        mi = new JMenuItem("Reset Downloads");
        mi.addActionListener(e -> {
        });
        if (!status.atLeast(Status.DOWNLOADING))
            mi.setEnabled(false);
        menu.add(mi);

        menu.addSeparator();

        mi = new JMenuItem("Reset");
        mi.addActionListener(e -> reset());
        menu.add(mi);

        menu.show(btnMenu, ev.getX(), ev.getY());
    }

    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }

    public void update(Observable o, Object arg) {
        updateStatus();
    }

    public MimeTypeCollection getMimeTypeCollection() {
        return mimeTypes;
    }
    public Status getStatus() {
        return status;
    }
    public String getURL() {
        String url = txtUrl.getText();
        if (url.length() >= 8 && !url.substring(0, 8).contains("://"))
            url = "http://" + url;
        return url;
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

    public void setURL(String url) {
        txtUrl.setText(url);
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
                btnGo.setText(GETTING_LINKS_LABEL + listModel.getSize() + " of " + (count + listModel.getSize()));
                break;
            case GRABBED:
                btnGo.setText(GET_FILES_LABEL);
                break;
            case DOWNLOADING:
                btnGo.setText(GETTING_FILES_LABEL + count + " of " + listModel.getSize());
                break;
            case DOWNLOADED:
                btnGo.setText(DONE_LABEL);
                break;
        }
    }

    public void reset() {
        count = 0;
        updateStatus(Status.UNINITIALIZED);
        linkType = WebpageCrawler.LinkType.Both;

        txtSaveDir.setText("");
        txtUrl.setText("");
        chkAnchor.setEnabled(true);
        chkImage.setEnabled(true);
    }

    public enum Status {
        UNINITIALIZED,
        GRABBING,
        GRABBED,
        DOWNLOADING,
        DOWNLOADED
    }
}