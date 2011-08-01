package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Interactively tests summary generation.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestSummaryInteractive {

    public static void main(String[] args) throws IOException, JWNLException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        Dictionary dic = Dictionary.getInstance(new FileInputStream("./src/main/resources/file_properties.xml"));
        System.err.flush();
        System.out.flush();

        while (true) {
            System.out.print("\nEnter lemma (enter to quit): ");
            System.out.flush();
            String queryString = input.readLine();

            if (null == queryString || "".equals(queryString)) {
                return;
            }

            queryString = queryString.toLowerCase();

            for (POS pos : POS.getAllPOS()) {
                IndexWord iw = dic.getIndexWord(pos, queryString);
                if (null != iw) {
                    System.out.println("POS: " + pos.getLabel());
                    for (Synset ss : iw.getSenses()) {
                        String summary = "VOID";
                        for (Word w : ss.getWords()) {
                            if (queryString.equals(w.getLemma().toLowerCase())) {
                                summary = w.getSummary();
                                break;
                            }
                        }
                        System.out.println(summary + "\t" + ss.getGloss());
                    }
                    System.out.println("");
                }
            }
        }
    }
}
