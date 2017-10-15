package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestAdjSynset extends BaseData {

    @Test
    public void setIsAdjectiveCluster() throws JWNLException {
        AdjectiveSynset synset = new AdjectiveSynset(dictionary);
        synset.setIsAdjectiveCluster(true);
        Assert.assertTrue(synset.isAdjectiveCluster());
    }
}
