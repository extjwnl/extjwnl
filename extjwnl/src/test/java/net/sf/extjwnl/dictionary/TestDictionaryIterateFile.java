package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import org.junit.BeforeClass;

public class TestDictionaryIterateFile extends TestDictionaryIterate {

    @BeforeClass
    public static void initDic() throws JWNLException {
        s_d = Dictionary.getInstance(TestDictionaryIterateFile.class.getResourceAsStream("/test_file_properties.xml"));
    }
}
