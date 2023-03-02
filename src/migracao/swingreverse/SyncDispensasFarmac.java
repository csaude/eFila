/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migracao.swingreverse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.PrintStream;

/**
 *
 * @author colaco
 */
public class SyncDispensasFarmac extends JPanel implements Runnable {

    private final JProgressBar progress1;
    private final JProgressBar progress2;
    private static Boolean stop = false;

    public SyncDispensasFarmac() {
        super(new BorderLayout());
        this.progress1 = new JProgressBar() {

            @Override
            public void updateUI() {
                super.updateUI();
                this.setUI(new ProgressCircleUI());
                this.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
            }
        };
        this.progress2 = new JProgressBar() {

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
        this.add((Component) new JButton(new AbstractAction("Enviar Dispensas para Unidade Sanitaria") {

            @Override
            public void actionPerformed(ActionEvent e) {
                final JButton b = (JButton) e.getSource();
                b.setEnabled(false);

                if (b.getLabel().equalsIgnoreCase("Fechar")) {
                    SyncDispensasFarmac.this.setStop(true);
                    SyncDispensasFarmac.this.setVisible(false);
                       JComponent comp = (JComponent) e.getSource();
                    Window win = SwingUtilities.getWindowAncestor(comp);
                    win.dispose();
                } else {
                    Task5 worker = new Task5() {

                        @Override
                        public void done() {
                            if (b.isDisplayable()) {
                                b.setEnabled(true);
                                b.setLabel("Fechar");
                            }
                        }
                    };
                    worker.addPropertyChangeListener(new ProgressListener(SyncDispensasFarmac.this.progress2));
                    worker.execute();

                }
            }

        }), "South");
        JPanel p = new JPanel(new GridLayout(1, 2));
        p.add(new JScrollPane(textArea));
        // p.add(this.progress2);
        this.add(p);
        this.setPreferredSize(new Dimension(920, 440));
    }

    public static /* varargs */ void main(String... args) {
        SyncDispensasFarmac syncDispensasFarmac = new SyncDispensasFarmac();
        syncDispensasFarmac.run();
//        EventQueue.invokeLater(new Runnable(){
//
//            @Override
//            public void run() {
//                SyncDispensasFarmac.createAndShowGUI();
//            }
//        });
    }

    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("Enviar Dispensas para Unidade Sanitaria");
        frame.setDefaultCloseOperation(1);
        frame.getContentPane().add(new SyncDispensasFarmac());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public void run() {
        while (!stop) {
            SyncDispensasFarmac.createAndShowGUI();
            SyncDispensasFarmac.this.setStop(true);
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
