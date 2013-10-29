package com.exigen.client.gui;
import javax.swing.*;
import java.awt.*;
public class JStatusBar extends JPanel {
    public JStatusBar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(10, 23));
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        add(rightPanel, BorderLayout.EAST);
    }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int y = 0;
        g.setColor(new Color(156, 154, 140));
        g.drawLine(0, y, getWidth(), y);
        y++;
        g.setColor(new Color(196, 194, 183));
        g.drawLine(0, y, getWidth(), y);
    }
}

