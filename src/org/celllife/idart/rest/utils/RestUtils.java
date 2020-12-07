package org.celllife.idart.rest.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author helio.machabane
 *
 */
public abstract class RestUtils {

	public static String castDateToString (Date date ) {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		String strDate = dateFormat.format(date);
		
		return strDate;	
	}

	public static Date castStringToDate (String date ) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

		Date strDate = null;
		try {
			strDate = dateFormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return strDate;
	}

	public static Date castStringToDatePattern (String date ) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		Date strDate = null;
		try {
			strDate = dateFormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return strDate;
	}

	public static String castDateToStringPattern (Date date ) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

		String strDate = null;
		strDate = dateFormat.format(date);

		return strDate;
	}

	public static List<String> splitName(String name){
		
		String fullName = name;
	    String[] tokens = fullName.split(" ");
	    String firstName = "";
	    String middleName = "";
	    String lastName = "";
	    if(tokens.length > 0) {
	        firstName = tokens[0];
	        middleName = tokens.length > 2 ? getMiddleName(tokens) : "";
	        lastName = tokens[tokens.length -1];
	    }
	    
	    List<String> _fullname = new ArrayList<String>();
	    _fullname.add(0, firstName);
	    _fullname.add(1, middleName);
	    _fullname.add(2, lastName);
	    
		
		return _fullname;
	}
	
	public static String getMiddleName(String[] middleName){
	      StringBuilder builder = new StringBuilder();
	      for (int i = 1; i < middleName.length-1; i++) {
	          builder.append(middleName[i] + " ");
	      }

	      return builder.toString();
	  }
	
	public static Calendar shortDate (String dateStr) {

		SimpleDateFormat curFormater = new SimpleDateFormat("yyyy-MM-dd"); 
		Date dateObj = null; 
		try {
			dateObj = curFormater.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateObj);
		
		return calendar;
	}

	public static boolean isNumeric(String stringNumber){

		if (stringNumber == null) {
			return false;
		}
		try {
			int d = Integer.parseInt(stringNumber);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;

	}

}
