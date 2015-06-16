package net.sf.extjwnl.dictionary.morph;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.factory.Param;

import java.util.*;

/**
 * Remove all applicable suffixes from the word(s) and do a look-up.
 * This class accepts parameters in the form of:
 * <pre>
 * {@code
 *  <param name="{part-of-speech}" value="|{suffix}={stemmed suffix}|..."/>
 * }
 * </pre>
 * where suffix is the {suffix} to convert from, and {stemmed suffix} is
 * the suffix to convert to.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class DetachSuffixesOperation extends AbstractDelegatingOperation {

    public static final String OPERATIONS = "operations";

    private Map<POS, String[][]> suffixMap;

    public DetachSuffixesOperation(Dictionary dictionary, Map<String, Param> params) throws JWNLException {
        super(dictionary, params);
        suffixMap = new EnumMap<>(POS.class);
        for (Param p : params.values()) {
            POS pos = POS.getPOSForLabel(p.getName());
            if (pos != null) {
                suffixMap.put(pos, getSuffixArray(dictionary, p.getValue()));
            }
        }
    }

    private String[][] getSuffixArray(Dictionary dictionary, String suffixes) throws JWNLException {
        StringTokenizer tokenizer = new StringTokenizer(suffixes, "|=", true);
        if (!"|".equals(tokenizer.nextToken())) {
            throw new JWNLException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_028"));
        }
        List<String[]> suffixList = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            String next = tokenizer.nextToken();
            String first = "";
            String second = "";
            if (!"=".equals(next)) {
                first = next;
                tokenizer.nextToken();
            }
            next = tokenizer.nextToken();
            if (!"|".equals(next)) {
                second = next;
                tokenizer.nextToken();
            }
            suffixList.add(new String[]{first, second});
        }
        return suffixList.toArray(new String[suffixList.size()][]);
    }

    protected String[] getKeys() {
        return new String[]{OPERATIONS};
    }

    public Map<POS, String[][]> getSuffixMap() {
        return suffixMap;
    }

    public void setSuffixMap(Map<POS, String[][]> suffixMap) {
        this.suffixMap = suffixMap;
    }

    public boolean execute(POS pos, String derivation, BaseFormSet forms) throws JWNLException {
        String[][] suffixArray = suffixMap.get(pos);
        if (suffixArray == null) {
            return false;
        }

        boolean addedBaseForm = false;
        for (String[] aSuffixArray : suffixArray) {
            if (derivation.endsWith(aSuffixArray[0])) {
                String stem = derivation.substring(0, derivation.length() - aSuffixArray[0].length()) + aSuffixArray[1];
                if (delegate(pos, stem, forms, OPERATIONS)) {
                    addedBaseForm = true;
                }
            }
        }
        return addedBaseForm;
    }
}