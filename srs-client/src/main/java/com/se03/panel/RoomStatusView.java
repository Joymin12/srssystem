package com.se03.panel;

import com.se03.api.RoomApi;
import com.se03.theme.AppTheme;

import javax.swing.JPanel;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.Map;

public final class RoomStatusView extends JPanel {
    private final RoomApi roomApi;
    private final JComboBox<String> roomSelect = new JComboBox<>();
    private final javax.swing.JTextField date = AppTheme.field(LocalDate.now().toString(), 10);
    private final JComboBox<String> viewType = new JComboBox<>(new String[]{"daily", "weekly", "monthly"});
    private final WireTable lectures = new WireTable("ID", "건물", "강의실", "요일", "시작", "종료", "강의명", "교수");
    private final WireTable reservations = new WireTable("ID", "신청자", "역할", "건물", "강의실", "날짜", "요일", "시작", "종료", "목적", "인원", "상태", "사유");

    public RoomStatusView(RoomApi roomApi) {
        this.roomApi = roomApi;
        setLayout(new BorderLayout(10, 10));
        add(AppTheme.title("강의실 및 강의 현황 조회"), BorderLayout.NORTH);
        JPanel form = AppTheme.panel();
        form.setLayout(new FlowLayout(FlowLayout.LEFT));
        form.add(new javax.swing.JLabel("강의실")); form.add(roomSelect);
        form.add(new javax.swing.JLabel("기준일")); form.add(date);
        form.add(new javax.swing.JLabel("조회")); form.add(viewType);
        var reload = AppTheme.button("강의실 목록");
        reload.addActionListener(e -> loadClassrooms());
        var button = AppTheme.button("조회");
        button.addActionListener(e -> load());
        form.add(reload);
        form.add(button);
        JPanel center = new JPanel(new java.awt.GridLayout(2, 1, 0, 10));
        center.add(lectures.scrollPane);
        center.add(reservations.scrollPane);
        add(form, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
        loadClassrooms();
    }

    private void load() {
        try {
            RoomItem selected = selectedRoom();
            if (selected == null) {
                javax.swing.JOptionPane.showMessageDialog(this, "강의실을 선택하세요.");
                return;
            }
            String raw = roomApi.showRoomStatus(Map.of("buildingId", selected.buildingId(), "roomId", selected.roomId(),
                    "date", date.getText(), "viewType", String.valueOf(viewType.getSelectedItem()))).raw();
            lectures.fill(raw, "LECTURE");
            reservations.fill(raw, "RESERVATION");
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void loadClassrooms() {
        try {
            roomSelect.removeAllItems();
            for (String line : roomApi.getClassrooms().raw().split("\n")) {
                if (!line.startsWith("CLASSROOM\t")) continue;
                String[] f = line.split("\t", -1);
                roomSelect.addItem(f[1] + " / " + f[2] + " / " + f[3] + "명");
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private RoomItem selectedRoom() {
        Object value = roomSelect.getSelectedItem();
        if (value == null) return null;
        String[] f = String.valueOf(value).split(" / ");
        return new RoomItem(f[0], f[1]);
    }

    private record RoomItem(String buildingId, String roomId) { }
}
