package net.sf.extjwnl.dictionary.morph;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.factory.Param;
import net.sf.extjwnl.util.factory.ParamList;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;

/**
 * Tokenizer operation.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 * @author John Didion (jdidion@didion.net)
 */
public class TokenizerOperation extends AbstractDelegatingOperation {
    /**
     * Parameter that determines the operations this operation
     * will perform on the tokens.
     */
    public static final String TOKEN_OPERATIONS = "token_operations";
    /**
     * Parameter that determines the operations this operation
     * will perform on the phrases.
     */
    public static final String PHRASE_OPERATIONS = "phrase_operations";
    /**
     * Parameter list that determines the delimiters this
     * operation will use to concatenate tokens.
     */
    public static final String DELIMITERS = "delimiters";

    private String[] delimiters;

    public TokenizerOperation(Dictionary dictionary, Map<String, Param> params) throws JWNLException {
        super(dictionary, params);
        ParamList delimiters = (ParamList) params.get(DELIMITERS);
        if (delimiters == null || delimiters.getParams().size() == 0) {
            this.delimiters = new String[]{" "};
        } else {
            this.delimiters = new String[delimiters.getParams().size()];
            for (int i = 0; i < delimiters.getParams().size(); i++) {
                this.delimiters[i] = delimiters.getParams().get(i).getValue();
            }
        }
    }

    protected String[] getKeys() {
        return new String[]{TOKEN_OPERATIONS, PHRASE_OPERATIONS};
    }

    public boolean execute(POS pos, String lemma, BaseFormSet forms) throws JWNLException {
        String[] tokens = Util.split(lemma);
        BaseFormSet[] tokenForms = new BaseFormSet[tokens.length];

        if (!hasDelegate(TOKEN_OPERATIONS)) {
            addDelegate(TOKEN_OPERATIONS, new Operation[]{new LookupIndexWordOperation(dictionary, params)});
        }
        if (!hasDelegate(PHRASE_OPERATIONS)) {
            addDelegate(PHRASE_OPERATIONS, new Operation[]{new LookupIndexWordOperation(dictionary, params)});
        }

        for (int i = 0; i < tokens.length; i++) {
            tokenForms[i] = new BaseFormSet(dictionary);
            tokenForms[i].add(tokens[i]);
            delegate(pos, tokens[i], tokenForms[i], TOKEN_OPERATIONS);
        }
        boolean foundForms = false;
        for (int i = 0; i < tokenForms.length; i++) {
            for (int j = tokenForms.length - 1; j >= i; j--) {
                if (tryAllCombinations(pos, tokenForms, i, j, forms)) {
                    foundForms = true;
                }
            }
        }
        return foundForms;
    }

    private boolean tryAllCombinations(
            POS pos, BaseFormSet[] tokenForms, int startIndex, int endIndex, BaseFormSet forms)
            throws JWNLException {

        int length = endIndex - startIndex + 1;
        int[] indexArray = new int[length];
        int[] endArray = new int[length];
        for (int i = 0; i < indexArray.length; i++) {
            indexArray[i] = 0;
            endArray[i] = tokenForms[startIndex + i].size() - 1;
        }

        boolean foundForms = false;
        for (; ;) {
            String[] tokens = new String[length];
            for (int i = 0; i < length; i++) {
                tokens[i] = tokenForms[i + startIndex].getForm(indexArray[i]);
            }
            for (String delimiter : delimiters) {
                if (tryAllCombinations(pos, tokens, delimiter, forms)) {
                    foundForms = true;
                }
            }

            if (Arrays.equals(indexArray, endArray)) {
                break;
            }

            for (int i = length - 1; i >= 0; i--) {
                if (indexArray[i] == endArray[i]) {
                    indexArray[i] = 0;
                } else {
                    indexArray[i]++;
                    break;
                }
            }
        }
        return foundForms;
    }

    private boolean tryAllCombinations(
            POS pos, String[] tokens, String delimiter, BaseFormSet forms) throws JWNLException {
        BitSet bits = new BitSet();
        int size = tokens.length - 1;

        boolean foundForms = false;
        do {
            String lemma = Util.getLemma(tokens, bits, delimiter);
            if (delegate(pos, lemma, forms, PHRASE_OPERATIONS)) {
                foundForms = true;
            }
        } while (Util.increment(bits, size));

        return foundForms;
    }
}