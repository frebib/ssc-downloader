package net.frebib.sscdownloader.gui;

import net.frebib.sscdownloader.MimeType;
import net.frebib.sscdownloader.MimeTypeCollection;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class FilterFrame extends JDialog implements ListCellRenderer<MimeType> {
    private final Color BG_COL, FG_COL, HL_BG_COL, HL_FG_COL;
    private final Border BORDER;

    private JList<MimeType> mimeList;
    private JScrollPane listScroll;
    private ArrayListModel<MimeType> listModel;
    private JComboBox<MimeTypeCollection> collections;
    private JTextField txtMime, txtExts;
    private JPopupMenu menu;

    private JPanel pnlCellRenderer;
    private JLabel lblMime, lblExts;

    private MimeType prev;
    private boolean editing;

    public FilterFrame(Frame parent, MimeTypeCollection initCollection) {
        super(parent, "Apply Filetype Filters", true);
        listModel = new ArrayListModel<>(initCollection.getMimes());
        mimeList = new JList<>(listModel);
        mimeList.setCellRenderer(this);
        mimeList.addMouseListener(new RightClickHandler());
        listScroll = new JScrollPane(mimeList);
        listScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        UIDefaults lf = UIManager.getLookAndFeel().getDefaults();
        BG_COL = this.getBackground();
        FG_COL = lf.getColor("List.foreground");
        BORDER = lf.getBorder("activeCaptionBorder");
        HL_BG_COL = lf.getColor("List.selectionBackground");
        HL_FG_COL = lf.getColor("List.selectionForeground");

        initForm(parent);
        initCellRenderer();

        setPreferredSize(new Dimension(360, 480));
        setMinimumSize(getPreferredSize());
        pack();
        setLocationRelativeTo(parent);
    }

    private void initForm(Frame parent) {
        JPanel pnlMain = new JPanel(new GridBagLayout());
        pnlMain.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JPanel pnlAdd = new JPanel(new GridBagLayout());
        pnlAdd.setBorder(BorderFactory.createTitledBorder(BORDER, "Add a MimeType"));

        lblMime = new JLabel("Enter the Mime Type");
        lblExts = new JLabel("Provide any extensions:");
        txtMime = new JTextFieldHint("Example: \"image/jpeg\"");
        txtExts = new JTextFieldHint("Comma, Semicolon or Space delimited");
        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(e -> addNewExt());
        JButton btnClose = new JButton("Apply");
        btnClose.addActionListener(e -> this.setVisible(false));
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> {
            txtExts.setText("");
            txtMime.setText("");
            listModel.add(prev);
            prev = null;
            editing = false;
        });
        collections = new JComboBox<>(MimeTypeCollection.DEF_COLLECTIONS);
        collections.addActionListener(e -> {
            listModel.clear();
            listModel.addAll(((MimeTypeCollection) collections.getSelectedItem()).getMimes());
        });
        collections.setSelectedIndex(0);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(10, 12, 0, 12);
        pnlAdd.add(lblMime, c);
        c.insets = new Insets(2, 12, 0, 12);
        pnlAdd.add(txtMime, c);
        c.insets = new Insets(10, 12, 0, 12);
        pnlAdd.add(lblExts, c);
        c.insets = new Insets(2, 12, 12, 12);
        pnlAdd.add(txtExts, c);
        c.gridx = 0;
        c.gridy = 4;
        c.ipadx = 24;
        c.weightx = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(0, 0, 10, 12);
        pnlAdd.add(btnCancel, c);
        c.gridx = 1;
        c.weightx = 0;
        pnlAdd.add(btnAdd, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 12, 0);
        pnlMain.add(listScroll, c);

        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        pnlMain.add(pnlAdd, c);

        c.gridx = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);
        pnlMain.add(collections, c);

        c.gridx = 1;
        c.weightx = 0;
        c.ipadx = 48;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 12, 0, 0);
        pnlMain.add(btnClose, c);

        this.add(pnlMain);
    }

    private void initCellRenderer() {
        pnlCellRenderer = new JPanel(new GridBagLayout());
        pnlCellRenderer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        lblMime = new JLabel();
        lblMime.setHorizontalAlignment(SwingConstants.LEFT);
        lblExts = new JLabel();
        lblExts.setHorizontalAlignment(SwingConstants.LEFT);
        lblExts.setFont(lblExts.getFont().deriveFont(Font.BOLD));

        JMenuItem mi;
        menu = new JPopupMenu();
        mi = new JMenuItem("Remove");
        mi.addActionListener(e -> {
            if (collections.getSelectedItem() != null)
                listModel.remove(collections.getSelectedIndex());
        });
        menu.add(mi);
        mi = new JMenuItem("Edit");
        mi.addActionListener(e -> {
            MimeType sel = mimeList.getSelectedValue();
            if (sel == null) return;

            if (editing)
                listModel.add(prev);
            else
                editing = true;
            prev = sel;
            listModel.remove(sel);
            txtMime.setText(sel.getMime());
            txtExts.setText(sel.getDelimitedExts(" ", false));
        });
        menu.add(mi);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = GridBagConstraints.RELATIVE;
        c.weightx = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipadx = 16;
        pnlCellRenderer.add(lblMime, c);

        c.ipadx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        pnlCellRenderer.add(lblExts, c);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends MimeType> list, MimeType value, int index, boolean isSelected, boolean cellHasFocus) {
        if (cellHasFocus) setColors(HL_BG_COL, HL_FG_COL);
        else setColors(BG_COL, FG_COL);

        lblMime.setText(value.getMime());
        lblExts.setText(value.getDelimitedExts(", ", true));

        return pnlCellRenderer;
    }

    private void setColors(Color bg, Color fg) {
        pnlCellRenderer.setBackground(bg);
        lblExts.setForeground(fg);
        lblMime.setForeground(fg);
    }

    private void addNewExt() {
        String mime = txtMime.getText();
        try {
            MimeType mt = MimeType.fromDelimitedExts(mime, txtExts.getText());
            listModel.add(mt);
            txtExts.setText("");
            txtMime.setText("");
        } catch (IllegalArgumentException e) {
            String message;
            if (mime.isEmpty())
                message = "Please enter a Mime Type, it is required";
            else
                message = e.getMessage();
            JOptionPane.showMessageDialog(this, message, "Invalid MimeType", JOptionPane.WARNING_MESSAGE);
        }
    }

    public MimeTypeCollection getMimeTypes() {
        return new MimeTypeCollection(listModel.getList());
    }

    public void setMimeTypes(MimeTypeCollection mimeTypes) {
        listModel.clear();
        listModel.addAll(mimeTypes.getMimes());
    }

    private class RightClickHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            onMouseEvent(e);
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            onMouseEvent(e);
        }
        private void onMouseEvent(MouseEvent e) {
            if (e.isPopupTrigger()) {
                MimeType mt = listModel.getElementAt(mimeList.locationToIndex(e.getPoint()));
                mimeList.setSelectedValue(mt, false);
                menu.show(mimeList, e.getX(), e.getY());
            }
        }
    }
}
