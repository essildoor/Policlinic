package com.exigen.client.gui;

import com.alee.laf.WebLookAndFeel;
import com.exigen.client.Client;
import com.exigen.entity.Patient;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PatientSearchDialog extends JDialog implements Runnable {

    private Client client;
    private JLabel statusLabel;
    private MainForm form;
    private ArrayList<Patient> patientsList;

    public PatientSearchDialog(MainForm form, JLabel statusLabel) {
        this.statusLabel = statusLabel;
        this.form = form;
        client = form.getClient();
    }

    @Override
    public void run() {
        WebLookAndFeel.install();
        int width = 350;
        int height = 520;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(
                (int) ((screenSize.getWidth() - width) / 2),
                (int) ((screenSize.getHeight() - height) / 2));
        Container pane = getContentPane();
        pane.setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(width, height));
    }
}
