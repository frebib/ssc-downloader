package net.frebib.sscdownloader.gui;

import net.frebib.sscdownloader.DownloadTask;
import net.frebib.sscdownloader.FileEvaluator;
import net.frebib.sscdownloader.MimeTypeCollection;
import net.frebib.sscdownloader.WebpageCrawler;
import net.frebib.sscdownloader.concurrent.BatchExecutor;
import net.frebib.sscdownloader.concurrent.Worker;
import net.frebib.util.Log;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DownloadClient extends JFrame implements Observer, MouseListener {
    public static final Log LOG = new Log(Level.FINEST)
            .setLogOutput(new SimpleDateFormat("'log/mailclient'yyyy-MM-dd hh-mm-ss'.log'")
                    .format(new Date()));

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
    private Status status = Status.UNINITIALIZED;
    private WebpageCrawler.LinkType linkType = WebpageCrawler.LinkType.Both;

    private MimeTypeCollection mimeTypes;
    private FilterFrame filterFrame;
    private FileEvaluator eval;
    private BatchExecutor<DownloadTask, DownloadTask> downloader;

    /**
     * Initialise the frame and create all components on the form
     */
    public DownloadClient() {
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
        btnGo.addActionListener(this::onGoClick);
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

    /**
     * An event handler for the main 'go' button that initiates downloads
     * @param e The ActionEvent provided by the control
     */
    public void onGoClick(ActionEvent e) {
        if (status == DownloadClient.Status.DOWNLOADED) {
            downloader = null;
            eval = null;
            listModel.clear();
            reset();
            return;
        } else if (!listModel.isEmpty()) {
            try {
                updateStatus(DownloadClient.Status.DOWNLOADING);
                downloader.start();
            } catch (InterruptedException ex) {
                DownloadClient.LOG.exception(ex);
            }
            return;
        }

        // Validate URL
        URL webpage;
        try {
            webpage = new URL(getURL());
        } catch (MalformedURLException ex) {
            JOptionPane.showMessageDialog(this, "Invalid URL",
                    "The webpage you entered doesn't appear to be valid.\n\n" +
                            ex.getMessage(), JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Validate Directory
        String dirStr = getSaveDir();
        if (dirStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a directory",
                    "Invalid Directory", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ask about creating the output directory
        File dir = new File(dirStr);
        if (!dir.exists()) {
            int result = JOptionPane.showConfirmDialog(this, "The directory doesn't exist.\nDo you want to create it?",
                    "Directory doesn't exist", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.OK_OPTION)
                dir.mkdirs();
            else
                return;
        }

        fetch(webpage, dir);
    }

    public void fetch(URL link, File outputDir) {
        setURL(link.toString());

        evaluate(link, outputDir);

        // Fetch links and parse them
        btnGo.setEnabled(false);
        updateStatus(DownloadClient.Status.GRABBING);
        new Worker<URL, List<URL>>()
                .todo(url -> WebpageCrawler.parse(url, WebpageCrawler.LinkType.Both, 30000))
                .done(links -> {
                    setDownloadCount(links.size());
                    links.stream().forEach(url ->
                            eval.add(url, outputDir, dl -> {
                                SwingUtilities.invokeLater(() -> listModel.add(dl));
                                decDownloadCount();
                            }));
                    eval.start();
                }).error(ex -> {
            String strace = ex.getMessage() +
                    Arrays.stream(ex.getStackTrace())
                            .limit(5)
                            .map(StackTraceElement::toString)
                            .collect(Collectors.joining("\n"));

            JOptionPane.showMessageDialog(this, "Failed to Connect\n"+ ex.getMessage() + strace,
                    "Failed to Connect", JOptionPane.INFORMATION_MESSAGE);
        }).start(link);
    }

    public void evaluate(URL link, File outputDir) {
        // Create a FileEvaluator object and
        // define what to do when it completes
        final int threads = getThreadCount();
        eval = new FileEvaluator(mimeTypes, threads, tasks -> {
            btnGo.setEnabled(true);
            updateStatus(DownloadClient.Status.GRABBED);
            setDownloadCount(0);

            tasks = tasks.stream()
                    .filter(x -> x != null)
                    .collect(Collectors.toList());
            tasks.stream().forEach(t ->
                    t.done(r -> incDownloadCount()));

            downloader = new BatchExecutor<>(getThreadCount());
            downloader.done(res -> updateStatus(DownloadClient.Status.DOWNLOADED));
            downloader.addAll(tasks);
        });
    }

    @Override
    public void mouseClicked(MouseEvent ev) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem mi;

        mi = new JMenuItem("Cancel All");
        mi.addActionListener(e -> listModel.getList().stream().forEach(DownloadTask::cancel));
        if (status != Status.DOWNLOADING)
            mi.setEnabled(false);
        menu.add(mi);

        mi = new JMenuItem("Pause All");
        mi.addActionListener(e -> listModel.getList().stream().forEach(DownloadTask::pause));
        if (status != Status.DOWNLOADING)
            mi.setEnabled(false);
        menu.add(mi);

        mi = new JMenuItem("Fetch Again");
        mi.addActionListener(e -> JOptionPane.showMessageDialog(this, "This option does nothing."));
        //if (!status.atLeast(Status.GRABBED))
        //    mi.setEnabled(false);
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

    /**
     * Handler method for updating the download
     * status when called by an observable
     * @param o the Observable object that called the change
     * @param arg argument passed by the Observable
     */
    @Override
    public void update(Observable o, Object arg) {
        updateStatus();
    }

    /**
     * Gets the MimeTypeCollection stored by the form
     * @return a MimeTypeCollection
     */
    public MimeTypeCollection getMimeTypeCollection() {
        return mimeTypes;
    }

    /**
     * Gets the DownloadStatus
     * @return the Status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the fully qualified URL from the Text Field on the form
     * @return the URL on the form
     */
    public String getURL() {
        String url = txtUrl.getText();
        if (url.length() >= 8 && !url.substring(0, 8).contains("://"))
            url = "http://" + url;
        return url;
    }

    /**
     * Gets the location to save files to
     * from the Text Field os the form
     * @return the save location from the form
     */
    public String getSaveDir() {
        return txtSaveDir.getText();
    }

    /*
     * Gets the amount of threads specified on the form
     * @return the thread count
     */
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
        numThreads.setValue(Runtime.getRuntime().availableProcessors() * 2);
        filterFrame.setMimeTypes(MimeTypeCollection.WILDCARD);
        chkAnchor.setEnabled(true);
        chkImage.setEnabled(true);
    }

    /**
     * Main entry point into the program.
     * @param args
     */
    public static void main(String[] args) {
        LOG.info("URL: https://lsd-25.ru/uploads/Various%20Artists%20-%20Drum%20%26%20Bass%20Arena%202014%20%20%282014%29/");
        LOG.info("Downloader initialised");

        DownloadClient client = new DownloadClient();
        client.setVisible(true);
    }

    public enum Status {
        UNINITIALIZED(1),
        GRABBING(2),
        GRABBED(3),
        DOWNLOADING(4),
        DOWNLOADED(5);

        private byte val;

        Status(int val) {
            this.val = (byte) val;
        }

        public boolean atLeast(Status status) {
            return this.val >= status.val;
        }
    }
}