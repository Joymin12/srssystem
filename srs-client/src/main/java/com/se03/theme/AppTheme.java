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
    public static final Color BG = new Color(244, 247, 251);
    public static final Color PANEL = new Color(255, 255, 255);
    public static final Color LINE = new Color(211, 219, 230);
    public static final Color TEXT = new Color(34, 45, 61);
    public static final Color ACCENT = new Color(32, 83, 139);
    public static final Color WARN = new Color(178, 94, 32);
    private static final Color CONTROL = new Color(238, 243, 249);
    private static final Color TABLE_HEADER = new Color(225, 233, 243);
    private static final Color TABLE_SELECTION = new Color(205, 224, 247);

    private AppTheme() {}

    public static void install() {
        UIManager.put("Panel.background", BG);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Button.background", CONTROL);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", ACCENT);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("TabbedPane.background", BG);
        UIManager.put("TabbedPane.foreground", TEXT);
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.gridColor", LINE);
        UIManager.put("Table.selectionBackground", TABLE_SELECTION);
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("TableHeader.background", TABLE_HEADER);
        UIManager.put("TableHeader.foreground", TEXT);
    }

    public static JPanel panel() {
        JPanel p = new JPanel();
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), BorderFactory.createEmptyBorder(12, 12, 12, 12)));
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
        b.setOpaque(true);
        b.setBackground(CONTROL);
        b.setForeground(TEXT);
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        return b;
    }

    public static JTextField field(String text, int columns) {
        JTextField f = new JTextField(text, columns);
        f.setBackground(Color.WHITE);
        f.setForeground(TEXT);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return f;
    }

    public static void table(JTable table) {
        table.setRowHeight(28);
        table.setBackground(Color.WHITE);
        table.setForeground(TEXT);
        table.setGridColor(LINE);
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(TEXT);
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setForeground(TEXT);
    }
}
