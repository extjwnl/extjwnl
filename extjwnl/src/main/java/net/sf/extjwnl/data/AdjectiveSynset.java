package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;

/**
 * A <code>Synset</code> for adjectives.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class AdjectiveSynset extends Synset {

    private static final long serialVersionUID = 5L;

    /**
     * for use only with WordNet 1.6 and earlier
     */
    private boolean isAdjectiveCluster = false;

    public AdjectiveSynset(Dictionary dictionary) throws JWNLException {
        super(dictionary, POS.ADJECTIVE);
    }

    public AdjectiveSynset(Dictionary dictionary, long offset) throws JWNLException {
        super(dictionary, POS.ADJECTIVE, offset);
    }

    public boolean isAdjectiveCluster() {
        return isAdjectiveCluster;
    }

    public void setIsAdjectiveCluster(boolean isAdjectiveCluster) {
        this.isAdjectiveCluster = isAdjectiveCluster;
    }
}