package com.se03.panel;

import com.se03.api.RoomApi;
import com.se03.theme.AppTheme;

import javax.swing.JPanel;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class AvailableRoomView extends JPanel {
    private final RoomApi roomApi;
    private final JComboBox<String> building = new JComboBox<>();
    private final javax.swing.JTextField date = AppTheme.field(LocalDate.now().plusDays(1).toString(), 10);
    private final javax.swing.JTextField day = AppTheme.field("월", 4);
    private final javax.swing.JTextField start = AppTheme.field("4", 3);
    private final javax.swing.JTextField end = AppTheme.field("5", 3);
    private final WireTable classrooms = new WireTable("건물", "강의실", "수용인원");

    public AvailableRoomView(RoomApi roomApi) {
        this.roomApi = roomApi;
        setLayout(new BorderLayout(10, 10));
        add(AppTheme.title("빈 강의실 조회"), BorderLayout.NORTH);
        JPanel form = AppTheme.panel();
        form.setLayout(new FlowLayout(FlowLayout.LEFT));
        form.add(new javax.swing.JLabel("건물")); form.add(building);
        form.add(new javax.swing.JLabel("날짜")); form.add(date);
        form.add(new javax.swing.JLabel("요일")); form.add(day);
        form.add(new javax.swing.JLabel("시작")); form.add(start);
        form.add(new javax.swing.JLabel("종료")); form.add(end);
        var reload = AppTheme.button("건물 목록");
        reload.addActionListener(e -> loadBuildings());
        var button = AppTheme.button("검색");
        button.addActionListener(e -> search());
        form.add(reload);
        form.add(button);
        add(form, BorderLayout.SOUTH);
        add(classrooms.scrollPane, BorderLayout.CENTER);
        loadBuildings();
    }

    private void search() {
        try {
            String raw = roomApi.searchAvailableRooms(Map.of("buildingId", String.valueOf(building.getSelectedItem()), "date", date.getText(), "dayOfWeek", day.getText(), "startPeriod", start.getText(), "endPeriod", end.getText())).raw();
            classrooms.fill(raw, "CLASSROOM");
            if (!raw.contains("CLASSROOM\t")) {
                javax.swing.JOptionPane.showMessageDialog(this, "예약 가능한 강의실이 없습니다.");
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void loadBuildings() {
        try {
            Set<String> buildings = new LinkedHashSet<>();
            for (String line : roomApi.getClassrooms().raw().split("\n")) {
                if (!line.startsWith("CLASSROOM\t")) continue;
                String[] f = line.split("\t", -1);
                buildings.add(f[1]);
            }
            building.removeAllItems();
            buildings.forEach(building::addItem);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
