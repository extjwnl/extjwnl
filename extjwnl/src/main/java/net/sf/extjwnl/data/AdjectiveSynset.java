package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;

/**
 * A <code>Synset</code> for adjectives.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class AdjectiveSynset extends Synset {

    private static final long serialVersionUID = 4L;

    /**
     * for use only with WordNet 1.6 and earlier
     */
    private boolean isAdjectiveCluster = false;

    public AdjectiveSynset(Dictionary dictionary, POS pos) throws JWNLException {
        super(dictionary, pos);
        if (POS.ADJECTIVE != pos) {
            throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_057"));
        }
    }

    public AdjectiveSynset(Dictionary dictionary, POS pos, long offset) throws JWNLException {
        super(dictionary, pos, offset);
        if (POS.ADJECTIVE != pos) {
            throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_057"));
        }
    }

    public boolean isAdjectiveCluster() {
        return isAdjectiveCluster;
    }

    public void setIsAdjectiveCluster(boolean isAdjectiveCluster) {
        this.isAdjectiveCluster = isAdjectiveCluster;
    }
}