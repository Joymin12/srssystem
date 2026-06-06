package com.se03.panel;

import com.se03.api.NotificationApi;
import com.se03.app.UserSession;
import com.se03.theme.AppTheme;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public final class NotificationView extends JPanel {
    private final NotificationApi notificationApi;
    private final UserSession session;
    private final WireTable notifications = new WireTable("ID", "사용자", "메시지", "읽음");
    private final javax.swing.JTextField notificationId = AppTheme.field("", 12);

    public NotificationView(NotificationApi notificationApi, UserSession session) {
        this.notificationApi = notificationApi;
        this.session = session;
        setLayout(new BorderLayout(10, 10));
        add(AppTheme.title("예약 결과 알림"), BorderLayout.NORTH);

        JPanel controls = AppTheme.panel();
        controls.setLayout(new FlowLayout(FlowLayout.LEFT));
        controls.add(new javax.swing.JLabel("알림 ID"));
        controls.add(notificationId);
        var refresh = AppTheme.button("조회");
        refresh.addActionListener(e -> load());
        var read = AppTheme.button("읽음");
        read.addActionListener(e -> markRead());
        controls.add(refresh);
        controls.add(read);

        notifications.table.getSelectionModel().addListSelectionListener(e -> notificationId.setText(notifications.selectedId()));
        add(notifications.scrollPane, BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);
        load();
    }

    private void load() {
        try {
            notifications.fill(notificationApi.getNotifications(session.userId()).raw(), "NOTIFICATION");
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void markRead() {
        try {
            javax.swing.JOptionPane.showMessageDialog(this, notificationApi.markRead(notificationId.getText()).message());
            load();
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
