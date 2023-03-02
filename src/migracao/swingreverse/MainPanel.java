/*
 * Decompiled with CFR 0_114.
 */
package migracao.swingreverse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.PrintStream;

public class MainPanel
extends JPanel {
    private final JProgressBar progress1;
    private final JProgressBar progress2;
    private static Boolean stop = false;

    public MainPanel() {
        super(new BorderLayout());
        this.progress1 = new JProgressBar(){

            @Override
            public void updateUI() {
                super.updateUI();
                this.setUI(new ProgressCircleUI());
                this.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
            }
        };
        this.progress2 = new JProgressBar(){

            @Override
            public void updateUI() {
                super.updateUI();
                this.setUI(new ProgressCircleUI());
                this.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
            }
        };
        this.progress1.setForeground(new Color(-1426085206));
        this.progress2.setStringPainted(true);
        this.progress2.setFont(this.progress2.getFont().deriveFont(24.0f));
        JSlider slider = new JSlider();
        slider.putClientProperty("Slider.paintThumbArrowShape", Boolean.TRUE);
        this.progress1.setModel(slider.getModel());
        JTextArea textArea = new JTextArea(50, 10);
        textArea.setEditable(false);
        PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
        PrintStream standardOut = System.out;
        System.setErr(printStream);
        this.add((Component)new JButton(new AbstractAction("Importar Pacientes"){

            @Override
            public void actionPerformed(ActionEvent e) {
                final JButton b = (JButton)e.getSource();
                b.setEnabled(false);
                Task worker = new Task(){

                    @Override
                    public void done() {
                        if (b.isDisplayable()) {
                            b.setEnabled(true);
                            b.setLabel("Fechar");
                        }
                    }
                };
                worker.addPropertyChangeListener(new ProgressListener(MainPanel.this.progress2));
                worker.execute();
                if (b.getLabel().equalsIgnoreCase("Fechar")) {
                    MainPanel.this.setStop(true);
                    MainPanel.this.setVisible(false);
                    JComponent comp = (JComponent) e.getSource();
                    Window win = SwingUtilities.getWindowAncestor(comp);
                    win.dispose();
                }
            }

        }), "South");
        JPanel p = new JPanel(new GridLayout(1, 2));
        p.add(new JScrollPane(textArea));
        p.add(this.progress2);
        this.add(p);
        this.setPreferredSize(new Dimension(920, 440));
    }

    public static /* varargs */ void main(String... args) {
        EventQueue.invokeLater(new Runnable(){

            @Override
            public void run() {
                MainPanel.createAndShowGUI();
            }
        });
    }

    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("Import Patients from OpenMRS");
        frame.setDefaultCloseOperation(1);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void run() {
        while (!stop) {
            MainPanel.createAndShowGUI();
            MainPanel.this.setStop(true);
        }
        //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Boolean getStop() {
        return stop;
    }

    public void setStop(Boolean stop) {
        this.stop = stop;
    }

}

