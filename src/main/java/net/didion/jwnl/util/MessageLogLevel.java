package net.didion.jwnl.util;

import net.didion.jwnl.JWNL;

public class MessageLogLevel {
    public static final MessageLogLevel TRACE = new MessageLogLevel("TRACE");
    public static final MessageLogLevel DEBUG = new MessageLogLevel("DEBUG");
    public static final MessageLogLevel INFO = new MessageLogLevel("INFO");
    public static final MessageLogLevel WARN = new MessageLogLevel("WARN");
    public static final MessageLogLevel ERROR = new MessageLogLevel("ERROR");
    public static final MessageLogLevel FATAL = new MessageLogLevel("FATAL");

    private final String name;

    private MessageLogLevel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return JWNL.resolveMessage("", name);
    }

    public int hashCode() {
        return name.hashCode();
    }
}