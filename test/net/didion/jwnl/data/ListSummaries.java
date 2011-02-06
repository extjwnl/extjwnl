package net.didion.jwnl.data;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Lists summaries.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class ListSummaries {
    public static void main(String[] args) throws IOException, JWNLException {
        JWNL.initialize(new FileInputStream("../config/file_properties.xml"));
        Dictionary dic = Dictionary.getInstance();

        for (POS pos : POS.getAllPOS()) {
            Iterator iwi = dic.getIndexWordIterator(pos);
            System.out.println("POS: " + pos.getLabel());

            while (iwi.hasNext()) {
                IndexWord iw = (IndexWord) iwi.next();
                String lemma = iw.getLemma().toLowerCase();
                for (Synset ss : iw.getSenses()) {
                    String summary = "VOID";
                    for (Word w : ss.getWords()) {
                        if (lemma.equals(w.getLemma().toLowerCase().replaceAll("_", " "))) {
                            summary = w.getSummary();
                            break;
                        }
                    }
                    System.out.println(iw.getLemma() + " (" + summary + ")\t\t" + ss.getGloss());
                }
                System.out.println("");
            }
        }
    }
}