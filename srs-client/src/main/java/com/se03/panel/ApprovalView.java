package com.se03.panel;

import com.se03.api.ApprovalApi;
import com.se03.app.UserSession;
import com.se03.theme.AppTheme;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Map;

public final class ApprovalView extends JPanel {
    private final ApprovalApi approvalApi;
    private final UserSession session;
    private final WireTable pending = new WireTable("ID", "신청자", "역할", "건물", "강의실", "날짜", "요일", "시작", "종료", "목적", "인원", "상태", "사유");
    private final javax.swing.JTextField reservationId = AppTheme.field("", 12);
    private final javax.swing.JTextField reason = AppTheme.field("사유", 20);

    public ApprovalView(ApprovalApi approvalApi, UserSession session) {
        this.approvalApi = approvalApi;
        this.session = session;
        setLayout(new BorderLayout(10, 10));
        add(AppTheme.title("예약 승인 및 거부"), BorderLayout.NORTH);
        JPanel controls = AppTheme.panel();
        controls.setLayout(new FlowLayout(FlowLayout.LEFT));
        controls.add(new javax.swing.JLabel("예약 ID")); controls.add(reservationId);
        controls.add(new javax.swing.JLabel("거부 사유")); controls.add(reason);
        var refresh = AppTheme.button("대기 목록");
        refresh.addActionListener(e -> load());
        var approve = AppTheme.button("승인");
        approve.addActionListener(e -> decide(true));
        var reject = AppTheme.button("거부");
        reject.addActionListener(e -> decide(false));
        controls.add(refresh); controls.add(approve); controls.add(reject);
        pending.table.getSelectionModel().addListSelectionListener(e -> reservationId.setText(pending.selectedId()));
        add(pending.scrollPane, BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);
    }

    private void load() {
        try {
            pending.fill(approvalApi.getPendingReservations().raw(), "RESERVATION");
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void decide(boolean approve) {
        try {
            Map<String, String> body = approve
                    ? Map.of("assistantId", session.userId(), "reservationId", reservationId.getText())
                    : Map.of("assistantId", session.userId(), "reservationId", reservationId.getText(), "reason", reason.getText());
            var response = approve ? approvalApi.approveReservation(body) : approvalApi.rejectReservation(body);
            javax.swing.JOptionPane.showMessageDialog(this, response.message());
            load();
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
