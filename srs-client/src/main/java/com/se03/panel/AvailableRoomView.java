package com.se03.panel;

import com.se03.api.RoomApi;
import com.se03.theme.AppTheme;

import javax.swing.JPanel;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.DayOfWeek;
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
        var syncDay = AppTheme.button("요일 계산");
        syncDay.addActionListener(e -> fillDayFromDate());
        form.add(reload);
        form.add(syncDay);
        form.add(button);
        add(form, BorderLayout.SOUTH);
        add(classrooms.scrollPane, BorderLayout.CENTER);
        loadBuildings();
        fillDayFromDate();
    }

    private void search() {
        try {
            if (!validateSearchInput()) return;
            fillDayFromDate();
            String raw = roomApi.searchAvailableRooms(Map.of("buildingId", String.valueOf(building.getSelectedItem()), "date", date.getText().trim(), "dayOfWeek", day.getText().trim(), "startPeriod", start.getText().trim(), "endPeriod", end.getText().trim())).raw();
            classrooms.fill(raw, "CLASSROOM");
            if (!raw.contains("CLASSROOM\t")) {
                javax.swing.JOptionPane.showMessageDialog(this, "예약 가능한 강의실이 없습니다.");
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private boolean validateSearchInput() {
        if (building.getSelectedItem() == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "건물을 선택하세요.");
            return false;
        }
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date.getText().trim());
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "날짜는 yyyy-MM-dd 형식으로 입력하세요.");
            return false;
        }
        int startPeriod;
        int endPeriod;
        try {
            startPeriod = Integer.parseInt(start.getText().trim());
            endPeriod = Integer.parseInt(end.getText().trim());
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "교시는 숫자로 입력하세요.");
            return false;
        }
        if (startPeriod <= 0 || endPeriod < startPeriod) {
            javax.swing.JOptionPane.showMessageDialog(this, "교시 범위를 확인하세요.");
            return false;
        }
        day.setText(koreanDay(parsedDate.getDayOfWeek()));
        return true;
    }

    private void fillDayFromDate() {
        try {
            day.setText(koreanDay(LocalDate.parse(date.getText().trim()).getDayOfWeek()));
        } catch (Exception ignored) {
            // 날짜 입력 중에는 검색 시점의 검증 메시지로 안내한다.
        }
    }

    private String koreanDay(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
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
