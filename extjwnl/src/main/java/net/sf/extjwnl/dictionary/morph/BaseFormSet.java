package net.sf.extjwnl.dictionary.morph;

import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.dictionary.Dictionary;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of possible base forms for a particular lemma.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class BaseFormSet {

    private final Dictionary dictionary;
    private final List<String> forms = new ArrayList<>();
    private final boolean allowDuplicates;
    private int index = -1;

    public BaseFormSet(Dictionary dictionary) {
        this(dictionary, false);
    }

    public BaseFormSet(Dictionary dictionary, boolean allowDuplicates) {
        this.dictionary = dictionary;
        this.allowDuplicates = allowDuplicates;
    }

    public void add(String s) {
        if (allowDuplicates || !forms.contains(s)) {
            forms.add(s);
        }
    }

    public void addAll(BaseFormSet forms) {
        if (allowDuplicates) {
            this.forms.addAll(forms.forms);
        } else {
            for (int i = 0; i < forms.forms.size(); i++) {
                add(forms.forms.get(i));
            }
        }
    }

    public String getForm(int index) {
        if (!isFormAvailable(index)) {
            throw new IllegalArgumentException(String.valueOf(index));
        }
        return forms.get(index);
    }

    public List<String> getForms() {
        return forms;
    }

    public boolean isCurrentFormAvailable() {
        return isFormAvailable(index);
    }

    public String getCurrentForm() {
        if (!isCurrentFormAvailable()) {
            throw new JWNLRuntimeException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_012"));
        }
        return getForm(index);
    }

    public boolean isMoreFormsAvailable() {
        return isFormAvailable(index + 1);
    }

    public String getNextForm() {
        if (!isMoreFormsAvailable()) {
            throw new JWNLRuntimeException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_013"));
        }
        return getForm(++index);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        if (index < forms.size()) {
            this.index = index;
        }
    }

    public int size() {
        return forms.size();
    }

    private boolean isFormAvailable(int index) {
        return (index >= 0 && index < forms.size());
    }
}