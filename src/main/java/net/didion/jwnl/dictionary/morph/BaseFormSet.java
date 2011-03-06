package net.didion.jwnl.dictionary.morph;

import net.didion.jwnl.JWNLRuntimeException;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of possible base forms for a particular lemma
 */
public class BaseFormSet {
    private List forms = new ArrayList();
    private int index = -1;
    private boolean allowDuplicates;

    public BaseFormSet() {
        this(false);
    }

    public BaseFormSet(boolean allowDuplicates) {
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
                add((String) forms.forms.get(i));
            }
        }
    }

    public String getForm(int index) {
        if (!isFormAvailable(index)) {
            throw new IllegalArgumentException(String.valueOf(index));
        }
        return (String) forms.get(index);
    }

    public List getForms() {
        return forms;
    }

    public boolean isCurrentFormAvailable() {
        return isFormAvailable(index);
    }

    public String getCurrentForm() {
        if (!isCurrentFormAvailable()) {
            throw new JWNLRuntimeException("DICTIONARY_EXCEPTION_012");
        }
        return getForm(index);
    }

    public boolean isMoreFormsAvailable() {
        return isFormAvailable(index + 1);
    }

    public String getNextForm() {
        if (!isMoreFormsAvailable()) {
            throw new JWNLRuntimeException("DICTIONARY_EXCEPTION_013");
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