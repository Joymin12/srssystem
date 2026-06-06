package com.se03;

import com.se03.api.ApiClient;
import com.se03.app.AppFrame;
import com.se03.app.UserSession;
import com.se03.theme.AppTheme;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        AppTheme.install();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    public static final class LoginFrame extends JFrame {
        private final JTextField userId = AppTheme.field("student1", 16);
        private final JPasswordField password = new JPasswordField("1234", 16);

        public LoginFrame() {
            setTitle("강의실 예약 시스템 - 로그인");
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setSize(360, 220);
            setLocationRelativeTo(null);
            JPanel form = AppTheme.panel();
            form.setLayout(new GridLayout(0, 2, 8, 8));
            form.add(new javax.swing.JLabel("아이디")); form.add(userId);
            form.add(new javax.swing.JLabel("비밀번호")); form.add(password);
            var login = AppTheme.button("로그인");
            login.addActionListener(e -> login());
            add(AppTheme.title("강의실 예약 시스템"), BorderLayout.NORTH);
            add(form, BorderLayout.CENTER);
            add(login, BorderLayout.SOUTH);
        }

        private void login() {
            try {
                var response = new ApiClient("http://localhost:8080").post("/login", Map.of("userId", userId.getText(), "password", new String(password.getPassword())));
                if (!response.ok()) {
                    JOptionPane.showMessageDialog(this, response.message());
                    return;
                }
                dispose();
                new AppFrame(new UserSession(response.value("userId"), response.value("name"), response.value("role"))).setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "서버를 먼저 실행하세요.\n" + e.getMessage());
            }
        }
    }
}
