package net.didion.jwnl.test.generic;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Contains static variables that the JUnit tests need to access. Controls which wordnet
 * is accessed for the test scripts. 
 * @author brett
 *
 */
public class TestDefaults {

      
    /**
     * The location of the configuration file. 
     */
    public static String CONFIG_PATH = "C:\\21csi\\jwnl\\";
    
    /**
     * The name of the file configuration. 
     */
    public static String FILE_CONFIG_NAME = "file_properties.xml";
    
    /**
     * The name of the database configuration. 
     */
    public static String DATABASE_CONFIG_NAME = "database_properties.xml";
    
    /**
     * The flag to use file backed wordnet.
     */
    public static String FILE = "Use File";
    
    /**
     * The flag to use database backed wordnet.
     */
    public static String DB = "Use DB";
    
    /**
     * The testing type. Currently either FILE or DB.
     */
    public static String testingType = TestDefaults.FILE;
  
    
    /**
     * Gets the input stream based on the type. 
     * @return input stream
     */
    public static InputStream getInputStream() {
        try {
            if (testingType.equals(TestDefaults.FILE)) {
                return new FileInputStream(CONFIG_PATH + FILE_CONFIG_NAME);
            } else if (testingType.equals(TestDefaults.DB)) {
                return new FileInputStream(CONFIG_PATH + DATABASE_CONFIG_NAME);
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
}
