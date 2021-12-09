/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migracao.swingreverse;

import org.celllife.idart.rest.DateUtilities;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author agnaldo
 */
public class test {
    

        
    public static void main(String[] args)   throws Exception {
        // TODO code application logic here
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = sdf.parse("2020-01-01");
        Date date2 = sdf.parse("2020-02-01");

        System.out.println("date1 : " + sdf.format(date1));
        System.out.println("date2 : " + sdf.format(date2));

        int result = DateUtilities.daysDiff(date2,date1);
        System.out.println("result: " + result);

        if (result == 0) {
            System.out.println("Date1 is equal to Date2");
        } else if (result > 0) {
            System.out.println("Date1 is after Date2");
        } else if (result < 0) {
            System.out.println("Date1 is before Date2");
        } else {
            System.out.println("How to get here?");
        }
        
       // String loc = ta.getLogFileLocation();
        
        
    }
    
}
