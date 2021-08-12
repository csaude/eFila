package org.celllife.idart.commonobjects;

import org.apache.log4j.Logger;
import org.celllife.idart.misc.PropertiesEncrypter;
import org.celllife.idart.misc.iDARTRuntimeException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Properties;

public class JdbcProperties {


    public static String urlBase = "http://localhost:8080/openmrs/ws/rest/v1/";

    public static String urlBaseReportingRest = "http://localhost:8080/openmrs/ws/rest/v1/reportingrest/cohort/ba36b483-c17c-454d-9a3a-f060a933c6da";

    public static String location = "660e84da-e72c-4080-b921-5131b77484cc";

    public static String hibernateOpenMRSConnectionUrl = "jdbc:mysql://localhost:3306/openmrs";

    public static String hibernateOpenMRSPassword = "idart";

    public static String hibernateOpenMRSUsername = "idart";

    public static String hibernateOpenMRSDatabase = "opemrs";

    public static String openMRSVersion = "1.x";

    public static String hibernateOpenMRSDriver = "com.mysql.jdbc.Drive";

    public static String hibernateOpenMRSDialect = "org.hibernate.dialect.MySQLDialect";

    public static String syncOpenmrsTimerInSeconds = "30";

    public static final String FILE = "jdbc.properties";

    private static Logger log = null;

    private static Properties loadedProperties;

    /**
     * private constructor to prevent instantiation
     */
    private JdbcProperties() {
    }

    public static void setJdbcProperties() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        log = Logger.getLogger(JdbcProperties.class);
        log.info("Loading Encrypted System Properties");

        PropertiesEncrypter pe = new PropertiesEncrypter();
        try {
            pe.loadPropertiesFromFile(FILE);
        } catch (IOException e) {
            throw new iDARTRuntimeException("Failed to load properties");
        }
        pe.decryptProperties();
        loadedProperties = pe.getProperties();

        urlBase = setStringProperty("urlBase");
        urlBaseReportingRest = setStringProperty("urlBaseReportingRest");
        location = setStringProperty("location");
        openMRSVersion = setStringProperty("openMRSVersion");
        hibernateOpenMRSConnectionUrl = setStringProperty("hibernateOpenMRSConnectionUrl");
        hibernateOpenMRSPassword = setStringProperty("hibernateOpenMRSPassword");
        hibernateOpenMRSUsername = setStringProperty("hibernateOpenMRSUsername");
        hibernateOpenMRSDatabase = setStringProperty("hibernateOpenMRSDatabase");
        hibernateOpenMRSDriver = setStringProperty("hibernateOpenMRSDriver");
        hibernateOpenMRSDialect = setStringProperty("hibernateOpenMRSDialect");
        syncOpenmrsTimerInSeconds = setStringProperty("syncOpenmrsTimerInSeconds");

    }

    public static String getPropertiesString() {

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("urlBase=" + urlBase);
        sb.append("\n");
        sb.append("urlBaseReportingRest=" + urlBaseReportingRest);
        sb.append("\n");
        sb.append("location=" + location);
        sb.append("\n");
        sb.append("openMRSVersion=" + openMRSVersion);
        sb.append("\n");
        sb.append("hibernateOpenMRSConnectionUrl=" + hibernateOpenMRSConnectionUrl);
        sb.append("\n");
        sb.append("hibernateOpenMRSPassword=" + hibernateOpenMRSPassword);
        sb.append("\n");
        sb.append("hibernateOpenMRSUsername=" + hibernateOpenMRSUsername);
        sb.append("\n");
        sb.append("hibernateOpenMRSDatabase=" + hibernateOpenMRSDatabase);
        sb.append("\n");
        sb.append("hibernateOpenMRSDriver=" + hibernateOpenMRSDriver);
        sb.append("\n");
        sb.append("hibernateOpenMRSDialect=" + hibernateOpenMRSDialect);
        sb.append("\n");
        sb.append("syncOpenmrsTimerInSeconds=" + syncOpenmrsTimerInSeconds);
        sb.append("\n");

        return sb.toString();
    }


    /**
     * Method setStringProperty.
     *
     * @param propertyName String
     * @return String
     */
    private static String setStringProperty(String propertyName) {
        String theSetting = loadedProperties.getProperty(propertyName);
        if (theSetting != null)
            return theSetting.trim();
        else {
            log.warn("Property: " + propertyName + " is null");
            return "";
        }
    }

    /**
     * Generates a string of name value pairs for the fields in this class.
     *
     * @return String listing all the values of the properties in this class.
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static String jdbcProperties() throws IllegalArgumentException,
            IllegalAccessException, ClassNotFoundException {
        Field[] fields = JdbcProperties.class.getDeclaredFields();
        StringBuffer props = new StringBuffer();
        for (int i = 0; i < fields.length; i++) {
            props.append(fields[i].getName());
            props.append(" : '");
            Object value = fields[i].get(Class.forName(JdbcProperties.class
                    .getName()));
            String val = "";
            if (value != null) {
                val = value.getClass().cast(value).toString();
            }
            props.append(val);
            props.append("'\n");
        }
        return props.toString();
    }
}