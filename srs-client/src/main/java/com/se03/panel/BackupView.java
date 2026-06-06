package com.se03.panel;

import com.se03.api.BackupApi;
import com.se03.theme.AppTheme;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public final class BackupView extends JPanel {
    private final BackupApi backupApi;
    private final JTextArea log = new JTextArea();

    public BackupView(BackupApi backupApi) {
        this.backupApi = backupApi;
        setLayout(new BorderLayout(10, 10));
        add(AppTheme.title("백업 및 복구"), BorderLayout.NORTH);
        log.setEditable(false);
        log.setRows(12);
        JPanel controls = AppTheme.panel();
        controls.setLayout(new FlowLayout(FlowLayout.LEFT));
        var backup = AppTheme.button("백업");
        backup.addActionListener(e -> backup());
        var restore = AppTheme.button("복구");
        restore.addActionListener(e -> restore());
        controls.add(backup);
        controls.add(restore);
        add(new javax.swing.JScrollPane(log), BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);
    }

    private void backup() {
        try { log.setText(backupApi.backup().raw()); }
        catch (Exception e) { log.setText(e.getMessage()); }
    }

    private void restore() {
        try { log.setText(backupApi.restore().raw()); }
        catch (Exception e) { log.setText(e.getMessage()); }
    }
}
