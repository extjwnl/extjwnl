package net.didion.jwnl.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LexFileIdMap maps the names of the lexiographer files to the identifiers
 * found in the data.pos files.
 * @author brett
 *
 */
public class LexFileIdMap {

	/**
	 * A mapping <Long, String> of id's to files.
	 */
	static Map lexIdMap = new HashMap();
	
	/**
	 * A mapping <String, Long> of files to id's.
	 */
	static Map lexNameMap = new HashMap();
	
	/**
	 * Initialization variable for this map.
	 */
	static boolean init = false;
	
	
	/**
	 * Gets the file name based on an id.
	 * @param id - the file id: see LEXNAMES(5WN)
	 * @return lexicographer file name (ex adj.all)
	 */
	public static String getFileName(long id) {
		checkInit();
		return (String) lexIdMap.get(new Long(id));
	}
	
	/**
	 * Gets the file id based on the name.
	 * @param fileName the file name: see LEXNAMES(5WN)
	 * @return lexicographer file id
	 */
	public static long getFileId(String fileName) {
		checkInit();
		Long rval = (Long)(lexNameMap.get(fileName));
		return rval.longValue();
	}
	
	/**
	 * Initializes our mappings
	 *
	 */
	private static void initMap() {
		
		List names = new ArrayList();
		names.add("adj.all");
		names.add("adj.pert");
		names.add("adv.all");
		names.add("noun.Tops");
		names.add("noun.act");
		names.add("noun.animal");
		names.add("noun.artifact");
		names.add("noun.attribute");
		names.add("noun.body");
		names.add("noun.cognition");
		names.add("noun.communication");
		names.add("noun.event");
		names.add("noun.feeling");
		names.add("noun.food");
		names.add("noun.group");
		names.add("noun.location");
		names.add("noun.motive");
		names.add("noun.object");
		names.add("noun.person");
		names.add("noun.phenomenon");
		names.add("noun.plant");
		names.add("noun.possession");
		names.add("noun.process");
		names.add("noun.quantity");
		names.add("noun.relation");
		names.add("noun.shape");
		names.add("noun.state");
		names.add("noun.substance");
		names.add("noun.time");
		names.add("verb.body");
		names.add("verb.change");
		names.add("verb.cognition");
		names.add("verb.communication");
		names.add("verb.competition");
	 	names.add("verb.consumption");
	 	names.add("verb.contact");
	 	names.add("verb.creation");
	 	names.add("verb.emotion");
	 	names.add("verb.motion");
	 	names.add("verb.perception");
	 	names.add("verb.possession");
	 	names.add("verb.social");
	 	names.add("verb.stative");
	 	names.add("verb.weather");
	 	names.add("adj.ppl");
		
		for (int i = 0; i < names.size(); i++) {
				lexIdMap.put(new Long(i), names.get(i));
				lexNameMap.put(names.get(i), new Long(i));
		}
		
		init = true;
	}
	
	/**
	 * Check if the maps are initialized.
	 *
	 */
	private static void checkInit() {
		if (!init) {
			initMap();
		}
	}
	
}
