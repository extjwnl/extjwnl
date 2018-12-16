package net.sf.extjwnl.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps the lexicographer files identifiers to names. See LEXNAMES(5WN).
 *
 * @author Brett Walenz (bwalenz@users.sourceforge.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class LexFileIdFileNameMap {

    /**
     * A mapping of id's to files.
     */
    private static Map<Long, String> lexFileIdLexFileName;

    static {
        String[] names = {
                "adj.all",
                "adj.pert",
                "adv.all",
                "noun.Tops",
                "noun.act",
                "noun.animal",
                "noun.artifact",
                "noun.attribute",
                "noun.body",
                "noun.cognition",
                "noun.communication",
                "noun.event",
                "noun.feeling",
                "noun.food",
                "noun.group",
                "noun.location",
                "noun.motive",
                "noun.object",
                "noun.person",
                "noun.phenomenon",
                "noun.plant",
                "noun.possession",
                "noun.process",
                "noun.quantity",
                "noun.relation",
                "noun.shape",
                "noun.state",
                "noun.substance",
                "noun.time",
                "verb.body",
                "verb.change",
                "verb.cognition",
                "verb.communication",
                "verb.competition",
                "verb.consumption",
                "verb.contact",
                "verb.creation",
                "verb.emotion",
                "verb.motion",
                "verb.perception",
                "verb.possession",
                "verb.social",
                "verb.stative",
                "verb.weather",
                "adj.ppl"
        };

        lexFileIdLexFileName = new HashMap<>();
        for (int i = 0; i < names.length; i++) {
            lexFileIdLexFileName.put((long) i, names[i]);
        }
        lexFileIdLexFileName = Collections.unmodifiableMap(lexFileIdLexFileName);
    }

    public static Map<Long, String> getMap() {
        return lexFileIdLexFileName;
    }
}