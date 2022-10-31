package org.celllife.idart.commonobjects;

import org.apache.log4j.Logger;
import org.celllife.idart.misc.PropertiesEncrypter;
import org.celllife.idart.misc.iDARTRuntimeException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Properties;

public class CentralizationProperties {


    public static String centralization = "on";

    public static String centralized_server_url = "http://localhost:3000";

    public static String pharmacy_type = "U";

    public static String last_dispense_before_reference = "L";
    public static String last_dispense_after_reference = "N";

    public static String rest_access_username = "postgres";

    public static String rest_access_password = "postgres";

    public static String location = "660e84da-e72c-4080-b921-5131b77484cc";

    public static String syncFarmacTimerInSeconds = "30";

    public static final String FILE = "centralization.properties";

    private static Logger log = null;

    private static Properties loadedProperties;

    /**
     * private constructor to prevent instantiation
     */
    private CentralizationProperties() {
    }

    public static void setCentralizationProperties() throws UnsupportedEncodingException, FileNotFoundException, IOException {
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

        centralization = setStringProperty("centralization");
        centralized_server_url = setStringProperty("centralized_server_url");
        pharmacy_type = setStringProperty("pharmacy_type");
        rest_access_username = setStringProperty("rest_access_username");
        rest_access_password = setStringProperty("rest_access_password");
        location = setStringProperty("location");
        syncFarmacTimerInSeconds = setStringProperty("syncFarmacTimerInSeconds");
    }

    public static String getPropertiesString() {

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("centralization=" + centralization);
        sb.append("\n");
        sb.append("centralized_server_url=" + centralized_server_url);
        sb.append("\n");
        sb.append("pharmacy_type=" + pharmacy_type);
        sb.append("\n");
        sb.append("rest_access_username=" + rest_access_username);
        sb.append("\n");
        sb.append("rest_access_password=" + rest_access_password);
        sb.append("\n");
        sb.append("location=" + location);
        sb.append("\n");
        sb.append("syncFarmacTimerInSeconds=" + syncFarmacTimerInSeconds);
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
    public static String centralizationProperties() throws IllegalArgumentException,
            IllegalAccessException, ClassNotFoundException {
        Field[] fields = CentralizationProperties.class.getDeclaredFields();
        StringBuffer props = new StringBuffer();
        for (int i = 0; i < fields.length; i++) {
            props.append(fields[i].getName());
            props.append(" : '");
            Object value = fields[i].get(Class.forName(CentralizationProperties.class
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
