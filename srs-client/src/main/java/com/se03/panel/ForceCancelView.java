package com.se03.panel;

import com.se03.api.ReservationApi;
import com.se03.app.UserSession;
import com.se03.theme.AppTheme;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Map;

public final class ForceCancelView extends JPanel {
    private final ReservationApi reservationApi;
    private final UserSession session;
    private final javax.swing.JTextField reservationId = AppTheme.field("", 14);
    private final javax.swing.JTextField reason = AppTheme.field("조교 강제 취소", 30);

    public ForceCancelView(ReservationApi reservationApi, UserSession session) {
        this.reservationApi = reservationApi;
        this.session = session;
        setLayout(new BorderLayout(10, 10));
        add(AppTheme.title("조교 예약 강제 취소"), BorderLayout.NORTH);
        JPanel controls = AppTheme.panel();
        controls.setLayout(new FlowLayout(FlowLayout.LEFT));
        controls.add(new javax.swing.JLabel("예약 ID"));
        controls.add(reservationId);
        controls.add(new javax.swing.JLabel("취소 사유"));
        controls.add(reason);
        var cancel = AppTheme.button("강제 취소");
        cancel.addActionListener(e -> forceCancel());
        controls.add(cancel);
        add(controls, BorderLayout.CENTER);
    }

    private void forceCancel() {
        try {
            var body = Map.of("assistantId", session.userId(), "reservationId", reservationId.getText(), "reason", reason.getText());
            javax.swing.JOptionPane.showMessageDialog(this, reservationApi.forceCancelReservation(body).message());
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
