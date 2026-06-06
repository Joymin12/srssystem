package com.se03.theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

public final class AppTheme {
    public static final Color BG = new Color(22, 27, 34);
    public static final Color PANEL = new Color(31, 38, 49);
    public static final Color LINE = new Color(60, 72, 88);
    public static final Color TEXT = new Color(225, 231, 239);
    public static final Color ACCENT = new Color(40, 199, 111);
    public static final Color WARN = new Color(255, 184, 77);

    private AppTheme() {}

    public static void install() {
        UIManager.put("Panel.background", BG);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Button.background", new Color(44, 54, 68));
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("TextField.background", new Color(14, 18, 24));
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", TEXT);
        UIManager.put("Table.background", new Color(14, 18, 24));
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.gridColor", LINE);
    }

    public static JPanel panel() {
        JPanel p = new JPanel();
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        return p;
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        label.setForeground(ACCENT);
        return label;
    }

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        return b;
    }

    public static JTextField field(String text, int columns) {
        JTextField f = new JTextField(text, columns);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), BorderFactory.createEmptyBorder(5, 7, 5, 7)));
        return f;
    }

    public static void table(JTable table) {
        table.setRowHeight(28);
        table.getTableHeader().setBackground(new Color(44, 54, 68));
        table.getTableHeader().setForeground(TEXT);
    }
}
