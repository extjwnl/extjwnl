package net.sf.extjwnl.cli;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetTreeNode;
import net.sf.extjwnl.data.list.PointerTargetTreeNodeList;
import net.sf.extjwnl.dictionary.Dictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;

/**
 * A command-line (CLI) interface to WordNets via extJWNL. It follows the syntax of the wn shipped with the original
 * WordNet, extending it with WordNet editing commands.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class ewn {

    private static final Logger log = LoggerFactory.getLogger(ewn.class);

    public static final String USAGE =
            "usage browse: ewn word [-hglaosk] [-n#] -searchtype [-searchtype...]\n" +
                    "\n" +
                    "        -h              Display help text before search output\n" +
                    "        -g              Display gloss\n" +
                    "        -l              Display license and copyright notice\n" +
                    "        -a              Display lexicographer file information\n" +
                    "        -o              Display synset offset\n" +
                    "        -s              Display sense numbers in synsets\n" +
                    "        -k              Display sense keys\n" +
                    "        -n#             Search only sense number #\n" +
                    "\n" +
                    "searchtype is at least one of the following:\n" +
                    "        -ants{n|v|a|r}          Antonyms\n" +
                    "        -hype{n|v}              Hypernyms\n" +
                    "        -hypo{n|v}, -tree{n|v}  Hyponyms & Hyponym Tree\n" +
                    "        -entav                  Verb Entailment\n" +
                    "        -syns{n|v|a|r}          Synonyms (ordered by estimated frequency)\n" +
                    "        -smemn                  Member of Holonyms\n" +
                    "        -ssubn                  Substance of Holonyms\n" +
                    "        -sprtn                  Part of Holonyms\n" +
                    "        -membn                  Has Member Meronyms\n" +
                    "        -subsn                  Has Substance Meronyms\n" +
                    "        -partn                  Has Part Meronyms\n" +
                    "        -meron                  All Meronyms\n" +
                    "        -holon                  All Holonyms\n" +
                    "        -causv                  Cause to\n" +
                    "        -pert{a|r}              Pertainyms\n" +
                    "        -attr{n|a}              Attributes\n" +
                    "        -deri{n|v}              Derived Forms\n" +
                    "        -domn{n|v|a|r}          Domain\n" +
                    "        -domt{n|v|a|r}          Domain Terms\n" +
                    "        -faml{n|v|a|r}          Familiarity & Polysemy Count\n" +
                    "        -framv                  Verb Frames\n" +
                    "        -hmern                  Hierarchical Meronyms\n" +
                    "        -hholn                  Hierarchical Holonyms\n" +
                    "        -grep{n|v|a|r}          List of Compound Words\n" +
                    "        -over                   Overview of Senses\n" +
                    "\n" +
                    "usage edit: ewn sensekey -command [value] [-command value] ... [sensekey -command ...]\n" +
                    "            ewn pos#derivation -command value\n" +
                    "\n" +
                    "command is one of the following (for sensekey syntax):\n" +
                    "        -add                           Add a new synset identified by this sensekey\n" +
                    "        -remove                        Remove the synset\n" +
                    "        -addword word                  Add the word to the synset\n" +
                    "        -removeword                    Remove the word as indicated by sensekey, from the synset\n" +
                    "        -setgloss gloss                Set the gloss of the synset\n" +
                    "        -setadjclus true|false         Set the adjective cluster flag\n" +
                    "        -setverbframe [-]n             Set the verb frame flag n (minus removes the flag)\n" +
                    "        -setverbframeall [-]n          Set the verb frame flag n for all words (minus removes the flag)\n" +
                    "        -setlexfile num|name           Set the lex file number or name\n" +
                    "        -addsemptr sensekey key        Add a semantic (synset-synset) pointer to sensekey with type defined by key\n" +
                    "        -removesemptr sensekey key     Remove the semantic pointer to sensekey\n" +
                    "        -addlexptr sensekey key        Add a lexical pointer to sensekey with type defined by key\n" +
                    "        -removelexptr sensekey key     Remove the lexical pointer to sensekey\n" +
                    "        -setlexid lexid                Set the lex id\n" +
                    "        -setusecount count             Set the use count\n" +
                    "\n" +
                    "or one of the following (for pos#derivation syntax):\n" +
                    "        -addexc baseform               Add baseform to exceptional forms of derivation. pos is one of n,v,a,r\n" +
                    "        -removeexc [baseform]          Remove all [or only baseform] exceptional forms of derivation. pos is one of n,v,a,r\n" +
                    "\n" +
                    "usage edit: ewn -script filename\n" +
                    "        filename contains edit commands as above, one sensekey per line. For example,\n" +
                    "        goal%1:09:00:: -add -addword end -setgloss \"the state ... achieve it; \"\"the ends justify the means\"\"\"\n" +
                    "        n#oxen -addexc ox";

    private static final String defaultConfig = "ewn.xml";
    private static final DecimalFormat df = new DecimalFormat("00000000");

    public static void main(String[] args) throws IOException, JWNLException {
        if (args.length < 1) {
            System.out.println(USAGE);
            System.exit(0);
        }
        //find dictionary
        Dictionary d = null;
        File config = new File(defaultConfig);
        if (!config.exists()) {
            if (System.getenv().containsKey("WNHOME")) {
                String wnHomePath = System.getenv().get("WNHOME");
                File wnHome = new File(wnHomePath);
                if (wnHome.exists()) {
                    d = Dictionary.getFileBackedInstance(wnHomePath);
                } else {
                    log.error("Cannot find dictionary. Make sure " + defaultConfig + " is available or WNHOME variable is set.");
                }
            } else {
                d = Dictionary.getDefaultResourceInstance();
            }
        } else {
            d = Dictionary.getInstance(new FileInputStream(config));
        }

        if (null != d) {
            //parse and execute command line
            if ((-1 < args[0].indexOf('%') && -1 < args[0].indexOf(':')) || "-script".equals(args[0]) || (-1 < args[0].indexOf('#'))) {
                d.edit();
                //edit
                if ("-script".equals(args[0])) {
                    if (args.length < 2) {
                        log.error("Filename missing for -script command");
                        System.exit(1);
                    } else {
                        final File script = new File(args[1]);
                        if (script.exists()) {
                            //load into args
                            final ArrayList<String> newArgs = new ArrayList<>();
                            try (final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(script), StandardCharsets.UTF_8))) {
                                String str;
                                while ((str = in.readLine()) != null) {
                                    final String[] bits = str.split(" ");
                                    StringBuilder tempArg = null;
                                    for (final String bit : bits) {
                                        int quoteCnt = 0;
                                        for (int j = 0; j < bit.length(); j++) {
                                            if ('"' == bit.charAt(j)) {
                                                quoteCnt++;
                                            }
                                        }
                                        if (null != tempArg) {
                                            if (0 == quoteCnt) {
                                                tempArg.append(" ").append(bit);
                                            } else {
                                                tempArg.append(" ").append(bit.replaceAll("\"\"", "\""));
                                                if (1 == (quoteCnt % 2)) {
                                                    newArgs.add(tempArg.toString().substring(1, tempArg.length() - 1));
                                                    tempArg = null;
                                                }
                                            }
                                        } else {
                                            if (0 == quoteCnt) {
                                                newArgs.add(bit);
                                            } else {
                                                if (1 == (quoteCnt % 2)) {
                                                    tempArg = new StringBuilder(bit.replaceAll("\"\"", "\""));
                                                } else {
                                                    newArgs.add(bit.replaceAll("\"\"", "\""));
                                                }
                                            }
                                        }
                                    }
                                    if (null != tempArg) {
                                        newArgs.add(tempArg.toString());
                                    }
                                }
                            }
                            //nop
                            args = newArgs.toArray(args);
                        }
                    }
                }

                Word workWord = null;
                String key = null;
                String lemma = null;
                int lexFileNum = -1;
                int lexId = -1;
                POS pos = null;
                String derivation = null;

                for (int i = 0; i < args.length && null != args[i]; i++) {
                    boolean argProcessed = false;
                    if (null == key && '-' != args[i].charAt(0) && ((-1 < args[i].indexOf('%') && -1 < args[i].indexOf(':')))) {
                        key = args[i];
                        argProcessed = true;
                        log.info("Searching {}...", key);
                        workWord = d.getWordBySenseKey(key);

                        if (null == workWord) {
                            // parse sensekey
                            final int percentIndex = key.indexOf('%');
                            if (percentIndex == -1) {
                                log.error("Malformed sensekey. Percent is not found: {}", key);
                                System.exit(1);
                            }

                            lemma = key.substring(0, percentIndex).replace('_', ' ').trim();
                            if (lemma.isEmpty()) {
                                log.error("Malformed sensekey. Lemma is empty: {}", key);
                                System.exit(1);
                            }

                            final int colonIndex = key.indexOf(':');
                            if (colonIndex == -1) {
                                log.error("Malformed sensekey. Colon is not found: {}", key);
                                System.exit(1);
                            }

                            final String posId = key.substring(percentIndex + 1, colonIndex);
                            if (posId.isEmpty()) {
                                log.error("Malformed sensekey. POS id is missing: {}", key);
                                System.exit(1);
                            }

                            final int posIntId = posId.charAt(0) - '0';
                            if (1 > posIntId || posIntId > 5) {
                                log.error("Malformed sensekey. POS id is out of bounds: {}", key);
                                System.exit(1);
                            }

                            pos = POS.getPOSForId(posIntId);
                            final String lexFileString = key.substring(colonIndex + 1);
                            int lexColonIndex = lexFileString.indexOf(':');
                            if (lexColonIndex == -1) {
                                log.error("Malformed sensekey. Lexical file number not found: {}", key);
                                System.exit(1);
                            }

                            try {
                                lexFileNum = Integer.parseInt(lexFileString.substring(0, lexColonIndex));
                            } catch (NumberFormatException e) {
                                log.error("Malformed sensekey. Lexical file number is malformed: {}", key);
                                System.exit(1);
                            }

                            if (lexColonIndex + 1 >= lexFileString.length()) {
                                log.error("Malformed sensekey. Lexical id marker not found: {}", key);
                                System.exit(1);
                            }

                            final String lexIdString = lexFileString.substring(lexColonIndex + 1);
                            int lexIdColonIndex = lexIdString.indexOf(':');
                            if (lexIdColonIndex == -1) {
                                log.error("Malformed sensekey. Lexical id not found: {}", key);
                                System.exit(1);
                            }
                            lexId = Integer.parseInt(lexIdString.substring(0, lexIdColonIndex));
                        }
                    } else if (-1 < args[i].indexOf('#')) {
                        if (2 < args[i].length()) {
                            derivation = args[i].substring(2).replace('_', ' ');
                            if (derivation.isEmpty()) {
                                log.error("Missing derivation");
                                System.exit(1);
                            }

                            pos = POS.getPOSForKey(args[i].substring(0, 1));
                            if (pos == null) {
                                log.error("POS {} is not recognized for derivation {}", args[i], derivation);
                                System.exit(1);
                            }

                            argProcessed = true;
                        }
                    }

                    if ("-add".equals(args[i])) {
                        if (key == null) {
                            log.error("Missing sensekey for -add");
                            System.exit(1);
                        }
                        if (null != workWord) {
                            log.error("Duplicate sensekey for -add: {}", workWord.getSenseKey());
                            System.exit(1);
                        }
                        log.info("Creating {} synset...", pos.getLabel());
                        final Synset tempSynset = d.createSynset(pos);
                        log.info("Creating word {}...", lemma);
                        workWord = new Word(d, tempSynset, lemma);
                        workWord.setLexId(lexId);
                        tempSynset.getWords().add(workWord);
                        tempSynset.setLexFileNum(lexFileNum);

                        key = null;
                        argProcessed = true;
                    }

                    if ("-remove".equals(args[i])) {
                        if (workWord == null) {
                            log.error("Missing current word for -remove. Perhaps, word not found in synset, or synset not found.");
                            System.exit(1);
                        }

                        d.removeSynset(workWord.getSynset());
                        workWord = null;
                        key = null;

                        argProcessed = true;
                    }

                    if ("-addword".equals(args[i])) {
                        if (workWord == null) {
                            log.error("Missing current word for -addword. Perhaps, word not found in synset, or synset not found.");
                            System.exit(1);
                        }

                        i++;
                        if (i < args.length && '-' != args[i].charAt(0)) {
                            final Word tempWord = new Word(d, workWord.getSynset(), args[i].replace('_', ' '));
                            workWord.getSynset().getWords().add(tempWord);
                            key = null;
                        } else {
                            log.error("Missing word for addword command for sensekey {}", workWord.getSenseKey());
                            System.exit(1);
                        }

                        argProcessed = true;
                    }

                    if ("-removeword".equals(args[i])) {
                        if (workWord == null) {
                            log.error("Missing current word for -removeword. Perhaps, word not found in synset, or synset not found.");
                            System.exit(1);
                        }

                        workWord.getSynset().getWords().remove(workWord);
                        key = null;

                        argProcessed = true;
                    }

                    if ("-setgloss".equals(args[i])) {
                        if (workWord == null) {
                            log.error("Missing current word for -setgloss. Perhaps, word not found in synset, or synset not found.");
                            System.exit(1);
                        }

                        i++;
                        if (i < args.length && '-' != args[i].charAt(0)) {
                            workWord.getSynset().setGloss(args[i]);
                            key = null;
                        } else {
                            log.error("Missing gloss for setgloss command for sensekey {}", workWord.getSenseKey());
                            System.exit(1);
                        }

                        argProcessed = true;
                    }

                    if ("-setadjclus".equals(args[i])) {
                        if (workWord == null) {
                            log.error("Missing current word for -setadjclus. Perhaps, word not found in synset, or synset not found.");
                            System.exit(1);
                        }

                        i++;
                        if (i < args.length && '-' != args[i].charAt(0)) {
                            workWord.getSynset().setIsAdjectiveCluster(Boolean.parseBoolean(args[i]));
                            key = null;
                        } else {
                            log.error("Missing flag for setadjclus command for sensekey {}", workWord.getSenseKey());
                            System.exit(1);
                        }

                        argProcessed = true;
                    }

                    if ("-setverbframe".equals(args[i])) {
                        if (workWord == null) {
                            log.error("Missing current word for -setverbframe. Perhaps, word not found in synset, or synset not found.");
                            System.exit(1);
                        }

                        i++;
                        if (i >= args.length) {
                            log.error("Missing index for setverbframe command for sensekey {}", workWord.getSenseKey());
                            System.exit(1);
                        }

                        if (!(workWord instanceof Verb)) {
                            log.error("Word at {} should be verb", workWord.getSenseKey());
                            System.exit(1);
                        }

                        final Verb verb = (Verb) workWord;
                        if ('-' == args[i].charAt(0)) {
                            verb.getVerbFrameFlags().clear(Integer.parseInt(args[i].substring(1)));
                        } else {
                            verb.getVerbFrameFlags().set(Integer.parseInt(args[i]));
                        }
                        key = null;

                        argProcessed = true;
                    }

                    if ("-setverbframeall".equals(args[i])) {
                        if (workWord == null) {
                            log.error("Missing current word for -setverbframeall. Perhaps, word not found in synset, or synset not found.");
                            System.exit(1);
                        }

                        i++;
                        if (i >= args.length) {
                            log.error("Missing index for setverbframeall command for sensekey {}", workWord.getSenseKey());
                            System.exit(1);
                        }

                        if (!(workWord.getSynset() instanceof VerbSynset)) {
                            log.error("Synset at {} should be verb", workWord.getSenseKey());
                            System.exit(1);
                        }

                        if ('-' == args[i].charAt(0)) {
                            workWord.getSynset().getVerbFrameFlags().clear(Integer.parseInt(args[i].substring(1)));
                        } else {
                            workWord.getSynset().getVerbFrameFlags().set(Integer.parseInt(args[i]));
                        }
                        key = null;

                        argProcessed = true;
                    }

                    if ("-setlexfile".equals(args[i])) {
                        if (workWord == null) {
                            log.error("Missing current word for -setlexfile. Perhaps, word not found in synset, or synset not found.");
                            System.exit(1);
                        }

                        i++;
                        if (i < args.length && '-' != args[i].charAt(0)) {
                            if (-1 < args[i].indexOf('.')) {
                                workWord.getSynset().setLexFileNum(LexFileNameFileIdMap.getMap().get(args[i]));
                            } else {
                                workWord.getSynset().setLexFileNum(Integer.parseInt(args[i]));
                            }
                        } else {
                            log.error("Missing file number or name for setlexfile command for sensekey {}", workWord.getSenseKey());
                            System.exit(1);
                        }

                        argProcessed = true;
                    }

                    if ("-addsemptr".equals(args[i])) {
                        if (workWord == null) {
                            log.error("Missing current word for -addsemptr. Perhaps, word not found in synset, or synset not found.");
                            System.exit(1);
                        }

                        i++;
                        if (i >= args.length) {
                            log.error("Missing sensekey for addsemptr command for sensekey {}", workWord.getSenseKey());
                            System.exit(1);
                        }

                        final Word targetWord = d.getWordBySenseKey(args[i]);
                        if (targetWord == null) {
                            log.error("Missing target at {} in addsemptr command for sensekey {}", args[i], workWord.getSenseKey());
                            System.exit(1);
                        }

                        i++;
                        if (i >= args.length) {
                            log.error("Missing pointer type in addsemptr command for sensekey {}", workWord.getSenseKey());
                            System.exit(1);
                        }

                        final PointerType pt = PointerType.getPointerTypeForKey(args[i]);
                        if (pt == null) {
                            log.error("Invalid pointer type at {} in addsemptr command for sensekey {}", args[i], workWord.getSenseKey());
                            System.exit(1);
                        }

                        final Pointer p = new Pointer(pt, workWord.getSynset(), targetWord.getSynset());
                        if (workWord.getSynset().getPointers().contains(p)) {
                            log.error("Duplicate pointer of type {} to {} in addsemptr command for sensekey {}", pt, targetWord.getSenseKey(), workWord.getSenseKey());
                            System.exit(1);
                        }

                        workWord.getSynset().getPointers().add(p);
                        key = null;

                        argProcessed = true;
                    }

                    if ("-addlexptr".equals(args[i])) {
                        if (workWord == null) {
                            log.error("Missing current word for -addlexptr. Perhaps, word not found in synset, or synset not found.");
                            System.exit(1);
                        }

                        i++;
                        if (i >= args.length) {
                            log.error("Missing sensekey for addlexptr command for sensekey " + workWord.getSenseKey());
                            System.exit(1);
                        }

                        final Word targetWord = d.getWordBySenseKey(args[i]);
                        if (targetWord == null) {
                            log.error("Missing target at {} in addlexptr command for sensekey {}", args[i], workWord.getSenseKey());
                            System.exit(1);
                        }

                        i++;
                        if (i >= args.length) {
                            log.error("Missing pointer type in addlexptr command for sensekey {}", workWord.getSenseKey());
                            System.exit(1);
                        }

                        final PointerType pt = PointerType.getPointerTypeForKey(args[i]);
                        if (pt == null) {
                            log.error("Invalid pointer type at {} in addlexptr command for sensekey {}", args[i], workWord.getSenseKey());
                            System.exit(1);
                        }

                        final Pointer p = new Pointer(pt, workWord, targetWord);
                        if (workWord.getSynset().getPointers().contains(p)) {
                            log.error("Duplicate pointer of type {} to {} in addlexptr command for sensekey {}", pt, targetWord.getSenseKey(), workWord.getSenseKey());
                            System.exit(1);
                        }

                        workWord.getSynset().getPointers().add(p);

                        key = null;
                        argProcessed = true;
                    }

                    if ("-removesemptr".equals(args[i])) {
                        if (null == workWord) {
                            log.error("Missing current word for -removesemptr. Perhaps, word not found in synset, or synset not found.");
                            System.exit(1);
                        }

                        i++;
                        if (i >= args.length) {
                            log.error("Missing sensekey for removesemptr command for sensekey {}", workWord.getSenseKey());
                            System.exit(1);
                        }

                        final Word targetWord = d.getWordBySenseKey(args[i]);
                        if (targetWord == null) {
                            log.error("Missing target at {} in removesemptr command for sensekey {}", args[i], workWord.getSenseKey());
                            System.exit(1);
                        }

                        i++;
                        if (i >= args.length) {
                            log.error("Missing pointer type in removesemptr command for sensekey {}", workWord.getSenseKey());
                            System.exit(1);
                        }

                        final PointerType pt = PointerType.getPointerTypeForKey(args[i]);
                        if (pt == null) {
                            log.error("Invalid pointer type at {} in removesemptr command for sensekey {}", args[i], workWord.getSenseKey());
                            System.exit(1);
                        }

                        final Pointer p = new Pointer(pt, workWord.getSynset(), targetWord.getSynset());
                        if (!workWord.getSynset().getPointers().contains(p)) {
                            log.error("Missing pointer of type {} to {} in removesemptr command for sensekey {}", pt, targetWord.getSenseKey(), workWord.getSenseKey());
                            System.exit(1);
                        }

                        workWord.getSynset().getPointers().remove(p);

                        key = null;
                        argProcessed = true;
                    }

                    if ("-removelexptr".equals(args[i])) {
                        if (null == workWord) {
                            log.error("Missing current word for -removelexptr. Perhaps, word not found in synset, or synset not found.");
                            System.exit(1);
                        }

                        i++;
                        if (i >= args.length) {
                            log.error("Missing sensekey for removelexptr command for sensekey {}", workWord.getSenseKey());
                            System.exit(1);
                        }

                        final Word targetWord = d.getWordBySenseKey(args[i]);
                        if (targetWord == null) {
                            log.error("Missing target at {} in removelexptr command for sensekey {}", args[i], workWord.getSenseKey());
                            System.exit(1);
                        }

                        i++;
                        if (i >= args.length) {
                            log.error("Missing pointer type in removelexptr command for sensekey {}", workWord.getSenseKey());
                            System.exit(1);
                        }

                        final PointerType pt = PointerType.getPointerTypeForKey(args[i]);
                        if (pt == null) {
                            log.error("Invalid pointer type at {} in removelexptr command for sensekey {}", args[i], workWord.getSenseKey());
                            System.exit(1);
                        }

                        final Pointer p = new Pointer(pt, workWord, targetWord);
                        if (!workWord.getSynset().getPointers().contains(p)) {
                            log.error("Missing pointer of type {} to {} in removelexptr command for sensekey {}", pt, targetWord.getSenseKey(), workWord.getSenseKey());
                            System.exit(1);
                        }
                        workWord.getSynset().getPointers().remove(p);

                        key = null;
                        argProcessed = true;
                    }

                    if ("-setlexid".equals(args[i])) {
                        if (null == workWord) {
                            log.error("Missing current word for -setlexid. Perhaps, word not found in synset, or synset not found.");
                            System.exit(1);
                        }

                        i++;
                        if (i < args.length && '-' != args[i].charAt(0)) {
                            workWord.setLexId(Integer.parseInt(args[i]));
                            key = null;
                        } else {
                            log.error("Missing lexid for setlexid command for sensekey {}", workWord.getSenseKey());
                            System.exit(1);
                        }

                        argProcessed = true;
                    }

                    if ("-setusecount".equals(args[i])) {
                        if (null == workWord) {
                            log.error("Missing current word for -setusecount. Perhaps, word not found in synset, or synset not found.");
                            System.exit(1);
                        }

                        i++;
                        if (i < args.length && '-' != args[i].charAt(0)) {
                            workWord.setUseCount(Integer.parseInt(args[i]));
                            key = null;
                        } else {
                            log.error("Missing count for setusecount command for sensekey {}", workWord.getSenseKey());
                            System.exit(1);
                        }

                        argProcessed = true;
                    }

                    if ("-addexc".equals(args[i])) {
                        i++;
                        if (derivation == null) {
                            log.error("Missing derivation for addexc command");
                            System.exit(1);
                        }

                        if (pos == null) {
                            log.error("Missing pos for addexc command for derivation {}", derivation);
                            System.exit(1);
                        }

                        if (i < args.length && '-' != args[i].charAt(0)) {
                            final String baseform = args[i].replace('_', ' ');
                            final Exc e = d.getException(pos, derivation);
                            if (null != e) {
                                if (null != e.getExceptions()) {
                                    if (!e.getExceptions().contains(baseform)) {
                                        e.getExceptions().add(baseform);
                                    }
                                }
                            } else {
                                final ArrayList<String> list = new ArrayList<>(1);
                                list.add(baseform);
                                d.createException(pos, derivation, list);
                            }
                            derivation = null;
                        } else {
                            log.error("Missing baseform for addexc command for derivation {}", derivation);
                            System.exit(1);
                        }
                        argProcessed = true;
                    }

                    if ("-removeexc".equals(args[i])) {
                        final Exc e = d.getException(pos, derivation);
                        if (e == null) {
                            log.error("Missing derivation {}", derivation);
                            System.exit(1);
                        }

                        i++;
                        if (i < args.length && '-' != args[i].charAt(0)) {
                            final String baseform = args[i].replace('_', ' ');
                            if (null != e.getExceptions()) {
                                if (e.getExceptions().contains(baseform)) {
                                    e.getExceptions().remove(baseform);
                                }
                                if (0 == e.getExceptions().size()) {
                                    d.removeException(e);
                                }
                            }
                        } else {
                            d.removeException(e);
                        }

                        derivation = null;
                        argProcessed = true;
                    }

                    if (!argProcessed) {
                        log.warn("Argument ignored: {}", args[i]);
                    }
                }

                d.save();
            } else {
                // browse
                final String key = args[0];
                if (1 == args.length) {
                    for (final POS pos : POS.getAllPOS()) {
                        IndexWord iw = d.getIndexWord(pos, key);
                        if (iw == null) {
                            System.out.println("\nNo information available for " + pos.getLabel() + " " + key);
                        } else {
                            System.out.println("\nInformation available for " + iw.getPOS().getLabel() + " " + iw.getLemma());
                            printAvailableInfo(iw);
                        }
                        if (null != d.getMorphologicalProcessor()) {
                            List<String> forms = d.getMorphologicalProcessor().lookupAllBaseForms(pos, key);
                            if (null != forms) {
                                for (String form : forms) {
                                    if (!key.equals(form)) {
                                        iw = d.getIndexWord(pos, form);
                                        if (null != iw) {
                                            System.out.println("\nInformation available for " + iw.getPOS().getLabel() + " " + iw.getLemma());
                                            printAvailableInfo(iw);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    boolean needHelp = false;

                    boolean needGloss = false;
                    boolean needLex = false;
                    boolean needOffset = false;
                    boolean needSenseNum = false;
                    boolean needSenseKeys = false;
                    int needSense = 0;
                    for (String arg : args) {
                        if ("-h".equals(arg)) {
                            needHelp = true;
                        }
                        if ("-g".equals(arg)) {
                            needGloss = true;
                        }
                        if ("-a".equals(arg)) {
                            needLex = true;
                        }
                        if ("-o".equals(arg)) {
                            needOffset = true;
                        }
                        if ("-s".equals(arg)) {
                            needSenseNum = true;
                        }
                        if ("-k".equals(arg)) {
                            needSenseKeys = true;
                        }
                        if (arg.startsWith("-n") && 2 < arg.length()) {
                            needSense = Integer.parseInt(arg.substring(2));
                        }
                    }

                    for (final String arg : args) {
                        if (arg.startsWith("-ants") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Display synsets containing direct antonyms of the search string.\n" +
                                        "\n" +
                                        "Direct antonyms are a pair of words between which there is an\n" +
                                        "associative bond built up by co-occurrences.\n" +
                                        "\n" +
                                        "Antonym synsets are preceded by \"=>\".");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (iw != null) {
                                System.out.println("\nAntonyms of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.ANTONYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//ants

                        if (arg.startsWith("-hype") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Recursively display hypernym (superordinate) tree for the search\n" +
                                        "string.\n" +
                                        "\n" +
                                        "Hypernym is the generic term used to designate a whole class of\n" +
                                        "specific instances.  Y is a hypernym of X if X is a (kind of) Y.\n" +
                                        "\n" +
                                        "Hypernym synsets are preceded by \"=>\", and are indented from\n" +
                                        "the left according to their level in the hierarchy.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nHypernyms of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.HYPERNYM, PointerUtils.INFINITY, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//hype

                        if (arg.startsWith("-hypo") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Display immediate hyponyms (subordinates) for the search string.\n" +
                                        "\n" +
                                        "Hyponym is the generic term used to designate a member of a class.\n" +
                                        "X is a hyponym of Y if X is a (kind of) Y.\n" +
                                        "\n" +
                                        "Hyponym synsets are preceded by \"=>\".");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nHyponyms of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.HYPONYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//hypo

                        if (arg.startsWith("-tree") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Display hyponym (subordinate) tree for the search string.  This is\n" +
                                        "a recursive search that finds the hyponyms of each hyponym. \n" +
                                        "\n" +
                                        "Hyponym is the generic term used to designate a member of a class.\n" +
                                        "X is a hyponym of Y if X is a (kind of) Y. \n" +
                                        "\n" +
                                        "Hyponym synsets are preceded by \"=>\", and are indented from the left\n" +
                                        "according to their level in the hierarchy.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nHyponyms of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.HYPONYM, PointerUtils.INFINITY, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//tree

                        if (arg.startsWith("-enta") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Recursively display entailment relations of the search string.\n" +
                                        "\n" +
                                        "The action represented by the verb X entails Y if X cannot be done\n" +
                                        "unless Y is, or has been, done.\n" +
                                        "\n" +
                                        "Entailment synsets are preceded by \"=>\", and are indented from the left\n" +
                                        "according to their level in the hierarchy.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nEntailment of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.ENTAILMENT, PointerUtils.INFINITY, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//enta


                        if (arg.startsWith("-syns") && 6 == arg.length()) {
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nSynonyms of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                if (POS.ADJECTIVE == p) {
                                    if (needHelp) {
                                        System.out.println("Display synonyms and synsets related to synsets containing\n" +
                                                "the search string.  If the search string is in a head synset\n" +
                                                "the 'cluster's' satellite synsets are displayed.  If the search\n" +
                                                "string is in a satellite synset, its head synset is displayed.\n" +
                                                "If the search string is a pertainym the word or synset that it\n" +
                                                "pertains to is displayed.\n" +
                                                "\n" +
                                                "A cluster is a group of adjective synsets that are organized around\n" +
                                                "antonymous pairs or triplets.  An adjective cluster contains two or more\n" +
                                                "head synsets that contan antonyms.  Each head synset has one or more\n" +
                                                "satellite synsets.\n" +
                                                "\n" +
                                                "A head synset contains at least one word that has a direct antonym\n" +
                                                "in another head synset of the same cluster.\n" +
                                                "\n" +
                                                "A satellite synset represents a concept that is similar in meaning to\n" +
                                                "the concept represented by its head synset.\n" +
                                                "\n" +
                                                "Direct antonyms are a pair of words between which there is an\n" +
                                                "associative bond built up by co-occurrences.\n" +
                                                "\n" +
                                                "Direct antonyms are printed in parentheses following the adjective.\n" +
                                                "The position of an adjective in relation to the noun may be restricted\n" +
                                                "to the prenominal, postnominal or predicative position.  Where present\n" +
                                                "these restrictions are noted in parentheses.\n" +
                                                "\n" +
                                                "A pertainym is a relational adjective, usually defined by such phrases\n" +
                                                "as \"of or pertaining to\" and that does not have an antonym.  It pertains\n" +
                                                "to a noun or another pertainym.\n" +
                                                "\n" +
                                                "Senses contained in head synsets are displayed above the satellites,\n" +
                                                "which are indented and preceded by \"=>\".  Senses contained in\n" +
                                                "satellite synsets are displayed with the head synset below.  The head\n" +
                                                "synset is preceded by \"=>\".\n" +
                                                "\n" +
                                                "Pertainym senses display the word or synsets that the search string\n" +
                                                "pertains to.");
                                    }
                                    tracePointers(iw, PointerType.SIMILAR_TO, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                    tracePointers(iw, PointerType.PARTICIPLE_OF, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                }

                                if (POS.ADVERB == p) {
                                    if (needHelp) {
                                        System.out.println("Display synonyms and synsets related to synsets containing\n" +
                                                "the search string.  If the search string is a pertainym the word\n" +
                                                "or synset that it pertains to is displayed.\n" +
                                                "\n" +
                                                "A pertainym is a relational adverb that is derived from an adjective.\n" +
                                                "\n" +
                                                "Pertainym senses display the word that the search string is derived from\n" +
                                                "and the adjective synset that contains the word.  If the adjective synset\n" +
                                                "is a satellite synset, its head synset is also displayed.");
                                    }
                                    tracePointers(iw, PointerType.PERTAINYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                }

                                if (POS.NOUN == p || POS.VERB == p) {
                                    if (needHelp) {
                                        System.out.println("Recursively display hypernym (superordinate) tree for the search\n" +
                                                "string.\n" +
                                                "\n" +
                                                "Hypernym is the generic term used to designate a whole class of\n" +
                                                "specific instances.  Y is a hypernym of X if X is a (kind of) Y.\n" +
                                                "\n" +
                                                "Hypernym synsets are preceded by \"=>\", and are indented from\n" +
                                                "the left according to their level in the hierarchy.");
                                    }
                                    tracePointers(iw, PointerType.HYPERNYM, PointerUtils.INFINITY, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                }
                            }
                        }//syns

                        if (arg.startsWith("-smem") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Display all member holonyms of the search string.\n" +
                                        "\n" +
                                        "A holonym is the name of the whole of which the 'meronym' names a part.\n" +
                                        "Y is a holonym of X if X is a part of Y.\n" +
                                        "\n" +
                                        "A meronym is the name of a constituent part, the substance of, or a\n" +
                                        "member of something.  X is a meronym of Y if X is a part of Y.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nMember Holonyms of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.MEMBER_HOLONYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//smem

                        if (arg.startsWith("-ssub") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Display all substance holonyms of the search string.\n" +
                                        "\n" +
                                        "A holonym is the name of the whole of which the 'meronym' names a part.\n" +
                                        "Y is a holonym of X if X is a part of Y.\n" +
                                        "\n" +
                                        "A meronym is the name of a constituent part, the substance of, or a\n" +
                                        "member of something.  X is a meronym of Y if X is a part of Y.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nSubstance Holonyms of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.SUBSTANCE_HOLONYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//ssub

                        if (arg.startsWith("-sprt") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Display all part holonyms of the search string.\n" +
                                        "\n" +
                                        "A holonym is the name of the whole of which the 'meronym' names a part.\n" +
                                        "Y is a holonym of X if X is a part of Y.\n" +
                                        "\n" +
                                        "A meronym is the name of a constituent part, the substance of, or a\n" +
                                        "member of something.  X is a meronym of Y if X is a part of Y.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nPart Holonyms of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.PART_HOLONYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//sprt

                        if (arg.startsWith("-memb") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Display all member meronyms of the search string. \n" +
                                        "\n" +
                                        "A meronym is the name of a constituent part, the substance of, or a\n" +
                                        "member of something.  X is a meronym of Y if X is a part of Y.\n" +
                                        "\n" +
                                        "A holonym is the name of the whole of which the meronym names a part.\n" +
                                        "Y is a holonym of X if X is a part of Y.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nMember Meronyms of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.MEMBER_MERONYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//memb


                        if (arg.startsWith("-subs") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Display all substance meronyms of the search string. \n" +
                                        "\n" +
                                        "A meronym is the name of a constituent part, the substance of, or a\n" +
                                        "member of something.  X is a meronym of Y if X is a part of Y.\n" +
                                        "\n" +
                                        "A holonym is the name of the whole of which the meronym names a part.\n" +
                                        "Y is a holonym of X if X is a part of Y.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nSubstance Meronyms of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.SUBSTANCE_MERONYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//subs

                        if (arg.startsWith("-part") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Display all part meronyms of the search string. \n" +
                                        "\n" +
                                        "A meronym is the name of a constituent part, the substance of, or a\n" +
                                        "member of something.  X is a meronym of Y if X is a part of Y.\n" +
                                        "\n" +
                                        "A holonym is the name of the whole of which the meronym names a part.\n" +
                                        "Y is a holonym of X if X is a part of Y.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nPart Meronyms of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.PART_MERONYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//part

                        if (arg.startsWith("-mero") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Display all meronyms of the search string. \n" +
                                        "\n" +
                                        "A meronym is the name of a constituent part, the substance of, or a\n" +
                                        "member of something.  X is a meronym of Y if X is a part of Y.\n" +
                                        "\n" +
                                        "A holonym is the name of the whole of which the meronym names a part.\n" +
                                        "Y is a holonym of X if X is a part of Y.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nMeronyms of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.MEMBER_MERONYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                tracePointers(iw, PointerType.SUBSTANCE_MERONYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                tracePointers(iw, PointerType.PART_MERONYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//mero


                        if (arg.startsWith("-holo") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Display all holonyms of the search string.\n" +
                                        "\n" +
                                        "A holonym is the name of the whole of which the 'meronym' names a part.\n" +
                                        "Y is a holonym of X if X is a part of Y.\n" +
                                        "\n" +
                                        "A meronym is the name of a constituent part, the substance of, or a\n" +
                                        "member of something.  X is a meronym of Y if X is a part of Y.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nHolonyms of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.MEMBER_HOLONYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                tracePointers(iw, PointerType.SUBSTANCE_HOLONYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                tracePointers(iw, PointerType.PART_HOLONYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//holo

                        if (arg.startsWith("-caus") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Recursively display CAUSE TO relations of the search string.\n" +
                                        "\n" +
                                        "The action represented by the verb X causes the action represented by\n" +
                                        "the verb Y.\n" +
                                        "\n" +
                                        "CAUSE TO synsets are preceded by \"=>\", and are indented from the left\n" +
                                        "according to their level in the hierarchy.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\n'Cause to' of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.CAUSE, PointerUtils.INFINITY, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//caus

                        if (arg.startsWith("-pert") && 6 == arg.length()) {
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nPertainyms of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.PERTAINYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//pert

                        if (arg.startsWith("-attr") && 6 == arg.length()) {
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            if (needHelp) {
                                if (POS.NOUN == p) {
                                    System.out.println("Display adjectives for which search string is an attribute.");
                                }
                                if (POS.ADJECTIVE == p) {
                                    System.out.println("Display nouns that are attributes of search string.");
                                }
                            }
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nAttributes of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.ATTRIBUTE, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//attr

                        if (arg.startsWith("-deri") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Display derived forms - nouns and verbs that are related morphologically.\n" +
                                        "Each related synset is preceeded by its part of speech. Each word in the\n" +
                                        "synset is followed by its sense number.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nDerived forms of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.DERIVATION, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//deri

                        if (arg.startsWith("-domn") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Display domain to which this synset belongs.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nDomain of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.CATEGORY, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                tracePointers(iw, PointerType.USAGE, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                tracePointers(iw, PointerType.REGION, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//domn

                        if (arg.startsWith("-domt") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Display all synsets belonging to the domain.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nDomain of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.CATEGORY_MEMBER, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                tracePointers(iw, PointerType.USAGE_MEMBER, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                tracePointers(iw, PointerType.REGION_MEMBER, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//domt

                        if (arg.startsWith("-faml") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Display familiarity and polysemy information for the search string.\n" +
                                        "The polysemy count is the number of senses in WordNet.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            if (p == null) {
                                log.error("POS not recognized for -faml command");
                                System.exit(1);
                            }
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                final String[] freqs = {"extremely rare", "very rare", "rare", "uncommon", "common",
                                        "familiar", "very familiar", "extremely familiar"};
                                final String[] pos = {"a noun", "a verb", "an adjective", "an adverb"};
                                final int cnt = iw.getSenses().size();
                                int familiar = 0;
                                if (cnt == 0) {
                                    familiar = 0;
                                }
                                if (cnt == 1) {
                                    familiar = 1;
                                }
                                if (cnt == 2) {
                                    familiar = 2;
                                }
                                if (cnt >= 3 && cnt <= 4) {
                                    familiar = 3;
                                }
                                if (cnt >= 5 && cnt <= 8) {
                                    familiar = 4;
                                }
                                if (cnt >= 9 && cnt <= 16) {
                                    familiar = 5;
                                }
                                if (cnt >= 17 && cnt <= 32) {
                                    familiar = 6;
                                }
                                if (cnt > 32) {
                                    familiar = 7;
                                }
                                System.out.println("\n" + iw.getLemma() + " used as " + pos[p.getId() - 1] + " is " + freqs[familiar] + " (polysemy count = " + cnt + ")");
                            }
                        }//faml

                        if (arg.startsWith("-fram") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Display applicable verb sentence frames for the search string.\n" +
                                        "\n" +
                                        "A frame is a sentence template illustrating the usage of a verb.\n" +
                                        "\n" +
                                        "Verb sentence frames are preceded with the string \"*>\" if a sentence\n" +
                                        "frame is acceptable for all of the words in the synset, and with \"=>\"\n" +
                                        "if a sentence frame is acceptable for the search string only.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nVerb frames of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                for (int i = 0; i < iw.getSenses().size(); i++) {
                                    final Synset synset = iw.getSenses().get(i);
                                    for (final String vf : synset.getVerbFrames()) {
                                        System.out.println("\t*> " + vf);
                                    }
                                    for (final Word word : synset.getWords()) {
                                        if (iw.getLemma().equalsIgnoreCase(word.getLemma())) {
                                            if (word instanceof Verb) {
                                                final Verb verb = (Verb) word;
                                                for (final String vf : verb.getVerbFrames()) {
                                                    System.out.println("\t=> " + vf);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }//fram

                        if (arg.startsWith("-hmer") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Display meronyms for search string tree.  This is a recursive search\n" +
                                        "the prints all the meronyms of the search string and all of its\n" +
                                        "hypernyms. \n" +
                                        "\n" +
                                        "A meronym is the name of a constituent part, the substance of, or a\n" +
                                        "member of something.  X is a meronym of Y if X is a part of Y.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nMeronyms of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.MEMBER_MERONYM, PointerUtils.INFINITY, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                tracePointers(iw, PointerType.SUBSTANCE_MERONYM, PointerUtils.INFINITY, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                tracePointers(iw, PointerType.PART_MERONYM, PointerUtils.INFINITY, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//hmer


                        if (arg.startsWith("-hhol") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("\"Display holonyms for search string tree.  This is a recursive search\n" +
                                        "that prints all the holonyms of the search string and all of the\n" +
                                        "holonym's holonyms.\n" +
                                        "\n" +
                                        "A holonym is the name of the whole of which the meronym names a part.\n" +
                                        "Y is a holonym of X if X is a part of Y.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nHolonyms of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.MEMBER_HOLONYM, PointerUtils.INFINITY, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                tracePointers(iw, PointerType.SUBSTANCE_HOLONYM, PointerUtils.INFINITY, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                tracePointers(iw, PointerType.PART_HOLONYM, PointerUtils.INFINITY, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//hhol

                        if (arg.startsWith("-mero") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Display all meronyms of the search string. \n" +
                                        "\n" +
                                        "A meronym is the name of a constituent part, the substance of, or a\n" +
                                        "member of something.  X is a meronym of Y if X is a part of Y.\n" +
                                        "\n" +
                                        "A holonym is the name of the whole of which the meronym names a part.\n" +
                                        "Y is a holonym of X if X is a part of Y.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            final IndexWord iw = d.lookupIndexWord(p, key);
                            if (null != iw) {
                                System.out.println("\nMeronyms of " + (p == null ? "" : p.getLabel()) + " " + iw.getLemma());
                                tracePointers(iw, PointerType.MEMBER_MERONYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                tracePointers(iw, PointerType.SUBSTANCE_MERONYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                tracePointers(iw, PointerType.PART_MERONYM, 1, needSense, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                            }
                        }//mero


                        if (arg.startsWith("-grep") && 6 == arg.length()) {
                            if (needHelp) {
                                System.out.println("Print all strings in the database containing the search string\n" +
                                        "as an individual word, or as the first or last string in a word or\n" +
                                        "collocation.");
                            }
                            final POS p = POS.getPOSForKey(arg.substring(5));
                            System.out.println("\nGrep of " + (p == null ? "" : p.getLabel()) + " " + key);
                            final Iterator<IndexWord> ii = d.getIndexWordIterator(p, key);
                            while (ii.hasNext()) {
                                System.out.println(ii.next().getLemma());
                            }
                        }//grep

                        if ("-over".equals(arg)) {
                            for (final POS pos : POS.getAllPOS()) {
                                if (null != d.getMorphologicalProcessor()) {
                                    IndexWord iw = d.getIndexWord(pos, key);
                                    //for plurals like species, glasses
                                    if (null != iw && key.equals(iw.getLemma())) {
                                        printOverview(pos, iw, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                    }

                                    final List<String> forms = d.getMorphologicalProcessor().lookupAllBaseForms(pos, key);
                                    if (null != forms) {
                                        for (final String form : forms) {
                                            if (!form.equals(key)) {
                                                iw = d.getIndexWord(pos, form);
                                                if (null != iw) {
                                                    printOverview(pos, iw, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }//over
                    }
                }
            }
        }
    }

    private static void printOverview(final POS pos, final IndexWord iw, final boolean needGloss,
                                      final boolean needLex, final boolean needOffset,
                                      final boolean needSenseNum, final boolean needSenseKeys) throws JWNLException {
        System.out.println("\nOverview of " + pos.getLabel() + " " + iw.getLemma());
        System.out.println("\nThe " + pos.getLabel() + " " + iw.getLemma() + " has " + iw.getSenses().size() + " senses");
        for (int i = 0; i < iw.getSenses().size(); i++) {
            final Synset synset = iw.getSenses().get(i);
            System.out.print((i + 1) + ". ");
            final int widx = synset.indexOfWord(iw.getLemma());
            if (-1 < widx) {
                final Word word = synset.getWords().get(widx);
                if (0 < word.getUseCount()) {
                    System.out.print("(" + word.getUseCount() + ") ");
                }
            }
            printSense("", synset, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
        }
    }

    private static void tracePointers(final IndexWord iw, final PointerType pt, final int depth,
                                      final int needSense, final boolean needGloss,
                                      final boolean needLex, final boolean needOffset, final boolean needSenseNum,
                                      final boolean needSenseKeys) throws JWNLException {
        final Map<Synset, PointerTargetTreeNodeList> ptrs = new HashMap<>();
        for (final Synset synset : iw.getSenses()) {
            final PointerTargetTreeNodeList list = PointerUtils.makePointerTargetTreeList(synset, pt, depth);
            if (null != list && 0 < list.size()) {
                ptrs.put(synset, list);
            }
        }
        if (0 < ptrs.size()) {
            System.out.println("\n" + ptrs.size() + " of " + iw.getSenses().size() + " senses of " + iw.getLemma());
            for (int i = 0; i < iw.getSenses().size(); i++) {
                if (0 == needSense || i == (needSense - 1)) {
                    final Synset synset = iw.getSenses().get(i);
                    final PointerTargetTreeNodeList list = ptrs.get(synset);
                    if (null != list) {
                        System.out.println("\nSense " + (i + 1));
                        printSense("", synset, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                        for (final PointerTargetTreeNode node : list) {
                            printPointerTargetTree("\t", node, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
                        }
                    }
                }
            }
        }
    }

    private static void printPointerTargetTree(final String lead, final PointerTargetTreeNode hypTree,
                                               final boolean needGloss, final boolean needLex,
                                               final boolean needOffset, final boolean needSenseNum,
                                               final boolean needSenseKeys) throws JWNLException {
        printSense(lead, hypTree.getSynset(), needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
        if (null != hypTree.getChildTreeList()) {
            for (final PointerTargetTreeNode node : hypTree.getChildTreeList()) {
                printPointerTargetTree(lead + "\t", node, needGloss, needLex, needOffset, needSenseNum, needSenseKeys);
            }
        }
    }

    private static void printSense(final String lead, final Synset synset, final boolean needGloss,
                                   final boolean needLex, final boolean needOffset,
                                   final boolean needSenseNum, final boolean needSenseKeys) throws JWNLException {
        System.out.print(lead);
        if (needOffset) {
            System.out.print("{" + df.format(synset.getOffset()) + "} ");
        }
        if (needLex) {
            System.out.print("<" + synset.getLexFileName() + "> ");
        }
        final List<Word> words = synset.getWords();
        for (int i = 0; i < words.size(); i++) {
            final Word word = words.get(i);
            System.out.print(word.getLemma());
            if (needLex && 0 < word.getLexId()) {
                System.out.print(word.getLexId());
            }
            if (needSenseNum) {
                System.out.print("#" + (getSenseNo(word) + 1));
            }
            if (needSenseKeys) {
                System.out.print(" [" + word.getSenseKey() + "]");
            }
            if (i < words.size() - 1) {
                System.out.print(", ");
            }
        }
        if (needGloss) {
            System.out.print(" -- (" + synset.getGloss() + ")");
        }
        System.out.println();
    }

    private static int getSenseNo(final Word word) throws JWNLException {
        final IndexWord iw = word.getDictionary().getIndexWord(word.getPOS(), word.getLemma());
        for (int i = 0; i < iw.getSenses().size(); i++) {
            if (iw.getSenses().get(i).getOffset() == word.getSynset().getOffset()) {
                return i;
            }
        }
        return -1;
    }

    private static boolean hasPointer(final IndexWord iw, final PointerType pointerType) {
        for (final Synset synset : iw.getSenses()) {
            for (final Pointer pointer : synset.getPointers()) {
                if (pointerType == pointer.getType()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasUseCount(final IndexWord iw) {
        for (final Synset synset : iw.getSenses()) {
            for (final Word word : synset.getWords()) {
                if (0 < word.getUseCount()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void printAvailableInfo(final IndexWord iw) {
        if (hasPointer(iw, PointerType.ANTONYM)) {
            System.out.println("\t-ants" + iw.getPOS().getKey() + "\tAntonyms");
        }

        if (POS.NOUN == iw.getPOS() || POS.VERB == iw.getPOS()) {
            if (hasPointer(iw, PointerType.HYPERNYM)) {
                System.out.println("\t-hype" + iw.getPOS().getKey() + "\tHypernyms");
            }
            if (hasPointer(iw, PointerType.HYPONYM)) {
                System.out.println("\t-hypo" + iw.getPOS().getKey() + ", -tree" + iw.getPOS().getKey() + "\tHyponyms & Hyponym Tree");
            }
            if (POS.VERB == iw.getPOS()) {
                if (hasPointer(iw, PointerType.ENTAILMENT)) {
                    System.out.println("\t-enta" + iw.getPOS().getKey() + "\tVerb Entailment");
                }
            }
        }

        if (POS.ADJECTIVE == iw.getPOS() && hasPointer(iw, PointerType.SIMILAR_TO)) {
            System.out.println("\t-syns" + iw.getPOS().getKey() + "\tSimilarity");
        }
        if (POS.NOUN == iw.getPOS() && hasPointer(iw, PointerType.HYPERNYM)) {
            System.out.println("\t-syns" + iw.getPOS().getKey() + "\tSynonyms/Hypernyms (Ordered by Estimated Frequency)");
        }
        if (POS.VERB == iw.getPOS() && hasPointer(iw, PointerType.SIMILAR_TO)) {
            System.out.println("\t-syns" + iw.getPOS().getKey() + "\tSynonyms/Hypernyms (Ordered by Estimated Frequency)");
        }
        if (POS.ADVERB == iw.getPOS() && hasPointer(iw, PointerType.SIMILAR_TO)) {
            System.out.println("\t-syns" + iw.getPOS().getKey() + "\tSynonyms");
        }
        if (POS.NOUN == iw.getPOS() && hasPointer(iw, PointerType.MEMBER_HOLONYM)) {
            System.out.println("\t-smem" + iw.getPOS().getKey() + "\tMember of Holonyms");
        }
        if (POS.NOUN == iw.getPOS() && hasPointer(iw, PointerType.SUBSTANCE_HOLONYM)) {
            System.out.println("\t-ssub" + iw.getPOS().getKey() + "\tSubstance of Holonyms");
        }
        if (POS.NOUN == iw.getPOS() && hasPointer(iw, PointerType.PART_HOLONYM)) {
            System.out.println("\t-smem" + iw.getPOS().getKey() + "\tPart of Holonyms");
        }
        if (POS.NOUN == iw.getPOS() && hasPointer(iw, PointerType.MEMBER_MERONYM)) {
            System.out.println("\t-memb" + iw.getPOS().getKey() + "\tHas Member Meronyms");
        }
        if (POS.NOUN == iw.getPOS() && hasPointer(iw, PointerType.SUBSTANCE_MERONYM)) {
            System.out.println("\t-subs" + iw.getPOS().getKey() + "\tHas Substance Meronyms");
        }
        if (POS.NOUN == iw.getPOS() && hasPointer(iw, PointerType.PART_MERONYM)) {
            System.out.println("\t-part" + iw.getPOS().getKey() + "\tHas Part Meronyms");
        }
        if (POS.NOUN == iw.getPOS() && (hasPointer(iw, PointerType.MEMBER_MERONYM) || hasPointer(iw, PointerType.SUBSTANCE_MERONYM) || hasPointer(iw, PointerType.PART_MERONYM))) {
            System.out.println("\t-mero" + iw.getPOS().getKey() + "\tAll Meronyms");
        }
        if (POS.NOUN == iw.getPOS() && (hasPointer(iw, PointerType.MEMBER_HOLONYM) || hasPointer(iw, PointerType.SUBSTANCE_HOLONYM) || hasPointer(iw, PointerType.PART_HOLONYM))) {
            System.out.println("\t-holo" + iw.getPOS().getKey() + "\tAll Holonyms");
        }
        if (POS.VERB == iw.getPOS() && hasPointer(iw, PointerType.CAUSE)) {
            System.out.println("\t-caus" + iw.getPOS().getKey() + "\tCause to");
        }

        if (POS.ADJECTIVE == iw.getPOS() || POS.ADVERB == iw.getPOS()) {
            if (hasPointer(iw, PointerType.PERTAINYM)) {
                System.out.println("\t-pert" + iw.getPOS().getKey() + "\tPertainyms");
            }
        }

        if (POS.ADJECTIVE == iw.getPOS() || POS.NOUN == iw.getPOS()) {
            if (hasPointer(iw, PointerType.ATTRIBUTE)) {
                System.out.println("\t-attr" + iw.getPOS().getKey() + "\tAttributes");
            }
        }

        if (POS.NOUN == iw.getPOS() || POS.VERB == iw.getPOS()) {
            if (hasPointer(iw, PointerType.DERIVATION)) {
                System.out.println("\t-deri" + iw.getPOS().getKey() + "\tDerived Forms");
            }
        }

        if (hasPointer(iw, PointerType.CATEGORY) || hasPointer(iw, PointerType.USAGE) || hasPointer(iw, PointerType.REGION)) {
            System.out.println("\t-domn" + iw.getPOS().getKey() + "\tDomain");
        }

        if (hasPointer(iw, PointerType.CATEGORY_MEMBER) || hasPointer(iw, PointerType.USAGE_MEMBER) || hasPointer(iw, PointerType.REGION_MEMBER)) {
            System.out.println("\t-domt" + iw.getPOS().getKey() + "\tDomain Terms");
        }

        if (hasUseCount(iw)) {
            System.out.println("\t-faml" + iw.getPOS().getKey() + "\tFamiliarity & Polysemy Count");
        }

        if (POS.VERB == iw.getPOS()) {
            System.out.println("\t-fram" + iw.getPOS().getKey() + "\tVerb Frames");
        }

        if (POS.NOUN == iw.getPOS()) {
            System.out.println("\t-hmer" + iw.getPOS().getKey() + "\tHierarchical Meronyms");
        }

        if (POS.NOUN == iw.getPOS()) {
            System.out.println("\t-hhol" + iw.getPOS().getKey() + "\tHierarchical Holonyms");
        }

        System.out.println("\t-grep" + iw.getPOS().getKey() + "\tList of Compound Words");
        System.out.println("\t-over\tOverview of Senses");
    }
}