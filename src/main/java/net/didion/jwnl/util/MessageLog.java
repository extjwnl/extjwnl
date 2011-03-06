package net.didion.jwnl.util;

import net.didion.jwnl.JWNL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MessageLog {
    private Log log;

    public MessageLog(Class clazz) {
        log = LogFactory.getLog(clazz);
    }

    public void log(MessageLogLevel level, String messageKey) {
        doLog(level, JWNL.resolveMessage(messageKey));
    }

    public void log(MessageLogLevel level, String messageKey, Object param) {
        doLog(level, JWNL.resolveMessage(messageKey, param));
    }

    public void log(MessageLogLevel level, String messageKey, Object[] params) {
        doLog(level, JWNL.resolveMessage(messageKey, params));
    }

    public void log(MessageLogLevel level, String messageKey, Throwable t) {
        doLog(level, JWNL.resolveMessage(messageKey), t);
    }

    public void log(MessageLogLevel level, String messageKey, Object param, Throwable t) {
        doLog(level, JWNL.resolveMessage(messageKey, param), t);
    }

    public void log(MessageLogLevel level, String messageKey, Object[] params, Throwable t) {
        doLog(level, JWNL.resolveMessage(messageKey, params), t);
    }

    public boolean isLevelEnabled(MessageLogLevel level) {
        if (level == MessageLogLevel.TRACE) {
            return log.isTraceEnabled();
        } else if (level == MessageLogLevel.DEBUG) {
            return log.isDebugEnabled();
        } else if (level == MessageLogLevel.INFO) {
            return log.isInfoEnabled();
        } else if (level == MessageLogLevel.WARN) {
            return log.isWarnEnabled();
        } else if (level == MessageLogLevel.ERROR) {
            return log.isErrorEnabled();
        } else if (level == MessageLogLevel.FATAL) {
            return log.isFatalEnabled();
        }
        return false;
    }

    private void doLog(MessageLogLevel level, String message) {
        if (isLevelEnabled(level)) {
            if (level == MessageLogLevel.TRACE) {
                log.trace(message);
            } else if (level == MessageLogLevel.DEBUG) {
                log.debug(message);
            } else if (level == MessageLogLevel.INFO) {
                log.info(message);
            } else if (level == MessageLogLevel.WARN) {
                log.warn(message);
            } else if (level == MessageLogLevel.ERROR) {
                log.error(message);
            } else if (level == MessageLogLevel.FATAL) {
                log.fatal(message);
            }
        }
    }

    private void doLog(MessageLogLevel level, String message, Throwable t) {
        if (isLevelEnabled(level)) {
            if (level == MessageLogLevel.TRACE) {
                log.trace(message, t);
            } else if (level == MessageLogLevel.DEBUG) {
                log.debug(message, t);
            } else if (level == MessageLogLevel.INFO) {
                log.info(message, t);
            } else if (level == MessageLogLevel.WARN) {
                log.warn(message, t);
            } else if (level == MessageLogLevel.ERROR) {
                log.error(message, t);
            } else if (level == MessageLogLevel.FATAL) {
                log.fatal(message, t);
            }
        }
    }
}