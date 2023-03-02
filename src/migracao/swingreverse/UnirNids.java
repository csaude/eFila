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
 * @author ColacoVM
 */
public class UnirNids extends JPanel implements Runnable {

    private final JProgressBar progress1;
    private final JProgressBar progress2;
    private static Boolean stop = false;

    public UnirNids() {
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
        this.add((Component) new JButton(new AbstractAction("Unir NIDS dos Pacientes do OpenMRS para o IDART") {

            @Override
            public void actionPerformed(ActionEvent e) {
                final JButton b = (JButton) e.getSource();
                b.setEnabled(false);

                if (b.getLabel().equalsIgnoreCase("Fechar")) {
                    UnirNids.this.setStop(false);
                    UnirNids.this.setVisible(false);
                    JComponent comp = (JComponent) e.getSource();
                    Window win = SwingUtilities.getWindowAncestor(comp);
                    win.dispose();
                } else {
                    Task2 worker = new Task2() {

                        @Override
                        public void done() {
                            if (b.isDisplayable()) {
                                b.setEnabled(true);
                                b.setLabel("Fechar");
                            }
                        }
                    };
                    worker.addPropertyChangeListener(new ProgressListener(UnirNids.this.progress2));
                    worker.execute();

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
        UnirNids unirNids = new UnirNids();
        unirNids.run();
//        EventQueue.invokeLater(new Runnable(){
//
//            @Override
//            public void run() {
//                UnirNids.createAndShowGUI();
//            }
//        });
    }

    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("Unir NIDS dos Pacientes do OpenMRS para o IDART");
        frame.setDefaultCloseOperation(1);
        frame.getContentPane().add(new UnirNids());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public void run() {
        while (!stop) {
            UnirNids.createAndShowGUI();
            UnirNids.this.setStop(true);
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
