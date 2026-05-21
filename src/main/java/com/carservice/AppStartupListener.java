package com.carservice;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Component
public class AppStartupListener {

    static {
        System.setProperty("java.awt.headless", "false");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("\n  ✅ AutoElite is Ready → http://localhost:8081/login\n");

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("AutoElite is Ready!");
            frame.setSize(420, 220);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setAlwaysOnTop(true);

            JPanel panel = new JPanel();
            panel.setBackground(new Color(15, 15, 20));
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

            JLabel title = new JLabel("✅  AutoElite is Ready!");
            title.setForeground(new Color(201, 168, 76));
            title.setFont(new Font("Arial", Font.BOLD, 16));
            title.setAlignmentX(0.5f);

            JLabel sub = new JLabel("Click the link below to open your app");
            sub.setForeground(new Color(138, 134, 128));
            sub.setFont(new Font("Arial", Font.PLAIN, 12));
            sub.setAlignmentX(0.5f);

            JLabel link = new JLabel("<html><u>🌐  http://localhost:8081/login</u></html>");
            link.setForeground(new Color(201, 168, 76));
            link.setFont(new Font("Arial", Font.PLAIN, 14));
            link.setAlignmentX(0.5f);
            link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            link.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        // ✅ Opens only ONE tab
                        Runtime.getRuntime().exec(new String[]{
                            "cmd.exe", "/c", "start", "http://localhost:8081/login"
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    link.setForeground(new Color(232, 201, 106));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    link.setForeground(new Color(201, 168, 76));
                }
            });

            panel.add(title);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
            panel.add(sub);
            panel.add(Box.createRigidArea(new Dimension(0, 20)));
            panel.add(link);

            frame.add(panel);
            frame.setVisible(true);
        });
    }
}