package com.se03.panel;

import com.se03.theme.AppTheme;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

final class WireTable {
    final DefaultTableModel model;
    final JTable table;
    final JScrollPane scrollPane;

    WireTable(String... columns) {
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        AppTheme.table(table);
        scrollPane = new JScrollPane(table);
    }

    void fill(String raw, String prefix) {
        // 다이어그램의 ReservationDto/ClassroomDto 목록은 별도 DTO 클래스 대신
        // 서버 wire text의 행 prefix를 기준으로 테이블에 투영한다. 작은 Swing 과제에서
        // DTO 변환 코드를 반복하지 않기 위한 선택이다.
        model.setRowCount(0);
        for (String line : raw.split("\n")) {
            if (!line.startsWith(prefix + "\t")) continue;
            String[] parts = line.split("\t", -1);
            List<String> row = new ArrayList<>();
            for (int i = 1; i < parts.length; i++) row.add(parts[i]);
            model.addRow(row.toArray());
        }
    }

    String selectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? "" : String.valueOf(table.getValueAt(row, 0));
    }
}
