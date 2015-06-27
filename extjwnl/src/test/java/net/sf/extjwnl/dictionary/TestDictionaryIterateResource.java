package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import org.junit.BeforeClass;

public class TestDictionaryIterateResource extends TestDictionaryIterate {

    @BeforeClass
    public static void initDic() throws JWNLException {
        s_d = Dictionary.getDefaultResourceInstance();
    }
}
