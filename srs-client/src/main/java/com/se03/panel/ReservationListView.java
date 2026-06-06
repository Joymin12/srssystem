package com.se03.panel;

import com.se03.api.ReservationApi;
import com.se03.app.UserSession;
import com.se03.theme.AppTheme;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Map;

public final class ReservationListView extends JPanel {
    private final ReservationApi reservationApi;
    private final UserSession session;
    private final WireTable reservations = new WireTable("ID", "신청자", "역할", "건물", "강의실", "날짜", "요일", "시작", "종료", "목적", "인원", "동반자", "상태", "사유");
    private final javax.swing.JTextField reservationId = AppTheme.field("", 12);
    private final javax.swing.JTextField reason = AppTheme.field("사용자 요청", 24);

    public ReservationListView(ReservationApi reservationApi, UserSession session) {
        this.reservationApi = reservationApi;
        this.session = session;
        setLayout(new BorderLayout(10, 10));
        add(AppTheme.title("내 예약 목록 및 취소"), BorderLayout.NORTH);
        JPanel form = AppTheme.panel();
        form.setLayout(new FlowLayout(FlowLayout.LEFT));
        form.add(new javax.swing.JLabel("예약 ID"));
        form.add(reservationId);
        form.add(new javax.swing.JLabel("사유"));
        form.add(reason);
        var load = AppTheme.button("목록 조회");
        load.addActionListener(e -> load());
        var detail = AppTheme.button("상세");
        detail.addActionListener(e -> detail());
        var button = AppTheme.button("취소");
        button.addActionListener(e -> cancel());
        form.add(load);
        form.add(detail);
        form.add(button);
        reservations.table.getSelectionModel().addListSelectionListener(e -> reservationId.setText(reservations.selectedId()));
        add(reservations.scrollPane, BorderLayout.CENTER);
        add(form, BorderLayout.SOUTH);
        load();
    }

    private void load() {
        try {
            reservations.fill(reservationApi.getReservations(session.userId()).raw(), "RESERVATION");
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void detail() {
        try {
            reservations.fill(reservationApi.getReservation(reservationId.getText()).raw(), "RESERVATION");
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void cancel() {
        try {
            var response = reservationApi.cancelReservation(Map.of("requesterId", session.userId(), "reservationId", reservationId.getText(), "reason", reason.getText()));
            javax.swing.JOptionPane.showMessageDialog(this, response.message());
            load();
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
