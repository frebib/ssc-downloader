package javax.swing;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Objects;

public class JTextFieldHint extends JTextField implements FocusListener, KeyListener {
    private String hint;
    private Font defaultFont;
    private Font lightFont;

    private final UIDefaults lf = UIManager.getLookAndFeel().getDefaults();
    private final Color INACTIVE_TEXT, ACTIVE_TEXT;

    public JTextFieldHint(String hint) {
        this(hint, null);
    }
    public JTextFieldHint(String hint, String text) {
        super();

        INACTIVE_TEXT = lf.getColor("TextField.inactiveForeground");
        ACTIVE_TEXT = lf.getColor("TextField.foreground");

        this.hint = hint;
        super.setText(hint);
        setForeground(INACTIVE_TEXT);


        this.addFocusListener(this);

        addKeyListener(this);
    }

    @Override
    public void setText(String t) {
        super.setText(t.isEmpty() ? hint : t);
        focusLost(null);
    }

    @Override
    public String getText() {
        String text = super.getText();
        return text.equals(hint) ? "" : text;
    }
    @Override
    public void focusGained(FocusEvent e) {
        if (super.getText().equals(hint))
            super.setText("");
        setForeground(ACTIVE_TEXT);
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (super.getText().equals(hint) || super.getText().isEmpty()) {
            super.setText(hint);
            setForeground(INACTIVE_TEXT);
        } else {
            setForeground(ACTIVE_TEXT);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            super.setText(hint);
            setForeground(INACTIVE_TEXT);
            getParent().requestFocus();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
