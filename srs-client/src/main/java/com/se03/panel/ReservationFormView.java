package com.se03.panel;

import com.se03.api.ReservationApi;
import com.se03.app.UserSession;
import com.se03.theme.AppTheme;

import javax.swing.JPanel;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ReservationFormView extends JPanel {
    private final ReservationApi reservationApi;
    private final UserSession session;
    private final Map<String, javax.swing.JTextField> fields = new LinkedHashMap<>();

    public ReservationFormView(ReservationApi reservationApi, UserSession session) {
        this.reservationApi = reservationApi;
        this.session = session;
        setLayout(new java.awt.BorderLayout(10, 10));
        add(AppTheme.title("강의실 예약 신청"), java.awt.BorderLayout.NORTH);
        JPanel form = AppTheme.panel();
        form.setLayout(new GridLayout(0, 2, 8, 8));
        addField(form, "buildingId", "정보공학관");
        addField(form, "roomId", "912");
        addField(form, "date", LocalDate.now().plusDays(1).toString());
        addField(form, "dayOfWeek", "월");
        addField(form, "startPeriod", "4");
        addField(form, "endPeriod", "5");
        addField(form, "purpose", "개인 학습");
        addField(form, "participantCount", "2");
        addField(form, "companions", "");
        var button = AppTheme.button("예약 신청");
        button.addActionListener(e -> submit());
        add(form, java.awt.BorderLayout.CENTER);
        add(button, java.awt.BorderLayout.SOUTH);
    }

    private void addField(JPanel form, String key, String value) {
        form.add(new javax.swing.JLabel(key));
        javax.swing.JTextField field = AppTheme.field(value, 20);
        fields.put(key, field);
        form.add(field);
    }

    private void submit() {
        try {
            Map<String, String> body = new LinkedHashMap<>();
            body.put("requesterId", session.userId());
            body.put("requesterRole", session.role());
            fields.forEach((k, v) -> body.put(k, v.getText()));
            javax.swing.JOptionPane.showMessageDialog(this, reservationApi.requestReservation(body).message());
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
