package com.se03.app;

import com.se03.Main;
import com.se03.api.ApiClient;
import com.se03.api.ApprovalApi;
import com.se03.api.BackupApi;
import com.se03.api.ManagementApi;
import com.se03.api.NotificationApi;
import com.se03.api.ReservationApi;
import com.se03.api.RoomApi;
import com.se03.panel.ApprovalView;
import com.se03.panel.AvailableRoomView;
import com.se03.panel.BackupView;
import com.se03.panel.ForceCancelView;
import com.se03.panel.ManagementView;
import com.se03.panel.NotificationView;
import com.se03.panel.ReservationFormView;
import com.se03.panel.ReservationListView;
import com.se03.panel.RoomStatusView;
import com.se03.theme.AppTheme;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;

public final class AppFrame extends JFrame {
    private final Set<String> shownNotificationIds = new HashSet<>();

    public AppFrame(UserSession session) {
        ApiClient apiClient = new ApiClient("http://localhost:8080");
        RoomApi roomApi = new RoomApi(apiClient);
        ReservationApi reservationApi = new ReservationApi(apiClient);
        ApprovalApi approvalApi = new ApprovalApi(apiClient);
        NotificationApi notificationApi = new NotificationApi(apiClient);
        ManagementApi managementApi = new ManagementApi(apiClient);
        BackupApi backupApi = new BackupApi(apiClient);

        setTitle("강의실 예약 시스템 - " + session.name() + " / " + session.role());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 760);
        setLocationRelativeTo(null);
        getContentPane().setBackground(AppTheme.BG);
        setLayout(new BorderLayout());

        JPanel header = AppTheme.panel();
        header.setLayout(new FlowLayout(FlowLayout.RIGHT));
        Timer timer = new Timer(10000, e -> showUnreadNotifications(notificationApi, session, true));
        var logout = AppTheme.button("로그아웃");
        logout.addActionListener(e -> {
            timer.stop();
            dispose();
            new Main.LoginFrame().setVisible(true);
        });
        header.add(logout);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("현황", new RoomStatusView(roomApi));
        if (!"ASSISTANT".equals(session.role())) {
            tabs.addTab("빈 강의실", new AvailableRoomView(roomApi));
            tabs.addTab("예약 신청", new ReservationFormView(reservationApi, session));
            tabs.addTab("내 예약", new ReservationListView(reservationApi, session));
            tabs.addTab("알림", new NotificationView(notificationApi, session));
        } else {
            tabs.addTab("승인/거부", new ApprovalView(approvalApi, session));
            tabs.addTab("강제 취소", new ForceCancelView(reservationApi, session));
            tabs.addTab("강의실 관리", new ManagementView(managementApi, session));
            tabs.addTab("백업/복구", new BackupView(backupApi));
        }
        add(tabs, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> showUnreadNotifications(notificationApi, session, false));
        timer.start();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                timer.stop();
            }
        });
    }

    private void showUnreadNotifications(NotificationApi notificationApi, UserSession session, boolean onlyNew) {
        try {
            StringBuilder messages = new StringBuilder();
            for (String line : notificationApi.getNotifications(session.userId()).raw().split("\n")) {
                if (!line.startsWith("NOTIFICATION\t")) continue;
                String[] f = line.split("\t", -1);
                if (f.length < 5 || Boolean.parseBoolean(f[4])) continue;
                if (onlyNew && shownNotificationIds.contains(f[1])) continue;
                shownNotificationIds.add(f[1]);
                messages.append("- ").append(f[3]).append('\n');
            }
            if (!messages.isEmpty()) {
                JOptionPane.showMessageDialog(this, messages.toString(), "미확인 예약 알림", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ignored) {
            // 알림 확인 실패는 다른 작업 흐름을 막지 않는다.
        }
    }
}
