package net.didion.jwnl.dictionary.morph;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.util.factory.Param;

import java.util.*;

/**
 * Remove all applicable suffixes from the word(s) and do a look-up.
 * This class accepts parameters in the form of:
 * <pre>
 * <p/>
 *  <param name="{part-of-speech}" value="|{suffix}={stemmed suffix}|..."/>
 * <p/>
 * </pre>
 * where suffix is the {suffix} to convert from, and {stemmed suffix} is
 * the suffix to convert to.
 */
public class DetachSuffixesOperation extends AbstractDelegatingOperation {
    public static final String OPERATIONS = "operations";

    private Map<POS, String[][]> _suffixMap;

    protected AbstractDelegatingOperation getInstance(Map params) throws JWNLException {
        Map<POS, String[][]> suffixMap = new HashMap<POS, String[][]>();
        for (Object o : params.values()) {
            Param p = (Param) o;
            POS pos = POS.getPOSForLabel(p.getName());
            if (pos != null) {
                suffixMap.put(pos, getSuffixArray(p.getValue()));
            }
        }
        return new DetachSuffixesOperation(suffixMap);
    }

    private String[][] getSuffixArray(String suffixes) throws JWNLException {
        StringTokenizer tokenizer = new StringTokenizer(suffixes, "|=", true);
        if (!"|".equals(tokenizer.nextToken())) {
            throw new JWNLException("DICTIONARY_EXCEPTION_028");
        }
        List<String[]> suffixList = new ArrayList<String[]>();
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

    public DetachSuffixesOperation() {
    }

    public DetachSuffixesOperation(Map<POS, String[][]> suffixMap) {
        _suffixMap = suffixMap;
    }

    protected String[] getKeys() {
        return new String[]{OPERATIONS};
    }

    public Map<POS, String[][]> getSuffixMap() {
        return _suffixMap;
    }

    public void setSuffixMap(Map<POS, String[][]> suffixMap) {
        _suffixMap = suffixMap;
    }

    public boolean execute(POS pos, String derivation, BaseFormSet forms) throws JWNLException {
        String[][] suffixArray = _suffixMap.get(pos);
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