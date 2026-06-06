package com.se03.panel;

import com.se03.api.ManagementApi;
import com.se03.app.UserSession;
import com.se03.theme.AppTheme;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Map;

public final class ManagementView extends JPanel {
    private final ManagementApi managementApi;
    private final UserSession session;
    private final WireTable classrooms = new WireTable("건물", "강의실", "수용인원");
    private final WireTable schedules = new WireTable("ID", "건물", "강의실", "요일", "시작", "종료", "강의명", "교수");

    private final javax.swing.JTextField roomBuilding = AppTheme.field("정보공학관", 10);
    private final javax.swing.JTextField roomId = AppTheme.field("", 8);
    private final javax.swing.JTextField capacity = AppTheme.field("40", 6);

    private final javax.swing.JTextField scheduleId = AppTheme.field("", 10);
    private final javax.swing.JTextField scheduleBuilding = AppTheme.field("정보공학관", 10);
    private final javax.swing.JTextField scheduleRoom = AppTheme.field("", 8);
    private final javax.swing.JTextField dayOfWeek = AppTheme.field("월", 4);
    private final javax.swing.JTextField startPeriod = AppTheme.field("1", 4);
    private final javax.swing.JTextField endPeriod = AppTheme.field("2", 4);
    private final javax.swing.JTextField title = AppTheme.field("", 14);
    private final javax.swing.JTextField professor = AppTheme.field("", 10);

    public ManagementView(ManagementApi managementApi, UserSession session) {
        this.managementApi = managementApi;
        this.session = session;
        setLayout(new BorderLayout(10, 10));
        add(AppTheme.title("강의실 및 강의 시간 관리"), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("강의실", classroomPanel());
        tabs.addTab("강의 시간", schedulePanel());
        add(tabs, BorderLayout.CENTER);
        loadClassrooms();
        loadSchedules();
    }

    private JPanel classroomPanel() {
        JPanel panel = AppTheme.panel();
        panel.setLayout(new BorderLayout(10, 10));
        JPanel controls = AppTheme.panel();
        controls.setLayout(new FlowLayout(FlowLayout.LEFT));
        controls.add(new javax.swing.JLabel("건물")); controls.add(roomBuilding);
        controls.add(new javax.swing.JLabel("강의실")); controls.add(roomId);
        controls.add(new javax.swing.JLabel("수용")); controls.add(capacity);
        var load = AppTheme.button("조회"); load.addActionListener(e -> loadClassrooms());
        var save = AppTheme.button("저장"); save.addActionListener(e -> saveClassroom());
        var delete = AppTheme.button("삭제"); delete.addActionListener(e -> deleteClassroom());
        controls.add(load); controls.add(save); controls.add(delete);
        classrooms.table.getSelectionModel().addListSelectionListener(e -> {
            int row = classrooms.table.getSelectedRow();
            if (row >= 0) {
                roomBuilding.setText(String.valueOf(classrooms.table.getValueAt(row, 0)));
                roomId.setText(String.valueOf(classrooms.table.getValueAt(row, 1)));
                capacity.setText(String.valueOf(classrooms.table.getValueAt(row, 2)));
            }
        });
        panel.add(classrooms.scrollPane, BorderLayout.CENTER);
        panel.add(controls, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel schedulePanel() {
        JPanel panel = AppTheme.panel();
        panel.setLayout(new BorderLayout(10, 10));
        JPanel controls = AppTheme.panel();
        controls.setLayout(new FlowLayout(FlowLayout.LEFT));
        controls.add(new javax.swing.JLabel("ID")); controls.add(scheduleId);
        controls.add(new javax.swing.JLabel("건물")); controls.add(scheduleBuilding);
        controls.add(new javax.swing.JLabel("강의실")); controls.add(scheduleRoom);
        controls.add(new javax.swing.JLabel("요일")); controls.add(dayOfWeek);
        controls.add(new javax.swing.JLabel("시작")); controls.add(startPeriod);
        controls.add(new javax.swing.JLabel("종료")); controls.add(endPeriod);
        controls.add(new javax.swing.JLabel("강의명")); controls.add(title);
        controls.add(new javax.swing.JLabel("교수")); controls.add(professor);
        var load = AppTheme.button("조회"); load.addActionListener(e -> loadSchedules());
        var save = AppTheme.button("저장"); save.addActionListener(e -> saveSchedule());
        var delete = AppTheme.button("삭제"); delete.addActionListener(e -> deleteSchedule());
        controls.add(load); controls.add(save); controls.add(delete);
        schedules.table.getSelectionModel().addListSelectionListener(e -> {
            int row = schedules.table.getSelectedRow();
            if (row >= 0) {
                scheduleId.setText(String.valueOf(schedules.table.getValueAt(row, 0)));
                scheduleBuilding.setText(String.valueOf(schedules.table.getValueAt(row, 1)));
                scheduleRoom.setText(String.valueOf(schedules.table.getValueAt(row, 2)));
                dayOfWeek.setText(String.valueOf(schedules.table.getValueAt(row, 3)));
                startPeriod.setText(String.valueOf(schedules.table.getValueAt(row, 4)));
                endPeriod.setText(String.valueOf(schedules.table.getValueAt(row, 5)));
                title.setText(String.valueOf(schedules.table.getValueAt(row, 6)));
                professor.setText(String.valueOf(schedules.table.getValueAt(row, 7)));
            }
        });
        panel.add(schedules.scrollPane, BorderLayout.CENTER);
        panel.add(controls, BorderLayout.SOUTH);
        return panel;
    }

    private void loadClassrooms() {
        try { classrooms.fill(managementApi.getClassrooms().raw(), "CLASSROOM"); }
        catch (Exception e) { javax.swing.JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void saveClassroom() {
        try {
            var body = Map.of("assistantId", session.userId(), "buildingId", roomBuilding.getText(), "roomId", roomId.getText(), "capacity", capacity.getText());
            javax.swing.JOptionPane.showMessageDialog(this, managementApi.saveClassroom(body).message());
            loadClassrooms();
        } catch (Exception e) { javax.swing.JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void deleteClassroom() {
        try {
            var body = Map.of("assistantId", session.userId(), "roomId", roomId.getText());
            javax.swing.JOptionPane.showMessageDialog(this, managementApi.deleteClassroom(body).message());
            loadClassrooms();
            loadSchedules();
        } catch (Exception e) { javax.swing.JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void loadSchedules() {
        try { schedules.fill(managementApi.getSchedules().raw(), "LECTURE"); }
        catch (Exception e) { javax.swing.JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void saveSchedule() {
        try {
            var body = Map.of("assistantId", session.userId(), "scheduleId", scheduleId.getText(), "buildingId", scheduleBuilding.getText(),
                    "roomId", scheduleRoom.getText(), "dayOfWeek", dayOfWeek.getText(), "startPeriod", startPeriod.getText(),
                    "endPeriod", endPeriod.getText(), "title", title.getText(), "professor", professor.getText());
            javax.swing.JOptionPane.showMessageDialog(this, managementApi.saveSchedule(body).message());
            loadSchedules();
        } catch (Exception e) { javax.swing.JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void deleteSchedule() {
        try {
            var body = Map.of("assistantId", session.userId(), "scheduleId", scheduleId.getText());
            javax.swing.JOptionPane.showMessageDialog(this, managementApi.deleteSchedule(body).message());
            loadSchedules();
        } catch (Exception e) { javax.swing.JOptionPane.showMessageDialog(this, e.getMessage()); }
    }
}
