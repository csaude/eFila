/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migracao.swingreverse;

import java.awt.BorderLayout;

import model.manager.excel.conversion.exceptions.ReportException;
import model.manager.excel.download.XLWriteManager;
import model.manager.importData.ImportPatients;
import org.celllife.idart.misc.task.TaskException;
import org.celllife.idart.rest.DateUtilities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.hibernate.Transaction;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author agnaldo
 */
public class test {



    public static void main(String[] args)   throws Exception {
//        // TODO code application logic here
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        Date date1 = sdf.parse("2020-01-01");
//        Date date2 = sdf.parse("2020-02-01");
//
//        System.out.println("date1 : " + sdf.format(date1));
//        System.out.println("date2 : " + sdf.format(date2));
//
//        int result = DateUtilities.daysDiff(date2,date1);
//        System.out.println("result: " + result);
//
//        if (result == 0) {
//            System.out.println("Date1 is equal to Date2");
//        } else if (result > 0) {
//            System.out.println("Date1 is after Date2");
//        } else if (result < 0) {
//            System.out.println("Date1 is before Date2");
//        } else {
//            System.out.println("How to get here?");
//        }
//        
//       // String loc = ta.getLogFileLocation();


////    JFrame parentFrame = new JFrame();
////    parentFrame.setSize(500, 150);
////    JLabel jl = new JLabel();
////    jl.setText("Count : 0");
////
////    parentFrame.add(BorderLayout.CENTER, jl);
////    parentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
////
////    parentFrame.setVisible(true);
////
////    final JDialog dlg = new JDialog(parentFrame, "Progress Dialog", true);
//    JProgressBar dpb = new JProgressBar(0, 500);
//    dlg.add(BorderLayout.CENTER, dpb);
//    dlg.add(BorderLayout.NORTH, new JLabel("Progress..."));
//    dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
//    dlg.setSize(300, 75);
//    dlg.setLocationRelativeTo(parentFrame);
//
//    Thread t = new Thread(new Runnable() {
//      public void run() {
//        dlg.setVisible(true);
//      }
//    });
//    t.start();
//    for (int i = 0; i <= 500; i++) {
//      jl.setText("Count : " + i);
//      dpb.setValue(i);
//      if(dpb.getValue() == 500){
//        dlg.setVisible(false);
//        System.exit(0);
//
//      }
//      try {
//        Thread.sleep(25);
//      } catch (InterruptedException e) {
//        e.printStackTrace();
//      }
//    }


   }

}
