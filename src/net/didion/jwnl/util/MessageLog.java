package net.didion.jwnl.util;

import net.didion.jwnl.JWNL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MessageLog {
	private Log _log;

	public MessageLog(Class clazz) {
		_log = LogFactory.getLog(clazz);
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
			return _log.isTraceEnabled();
		} else if (level == MessageLogLevel.DEBUG) {
			return _log.isDebugEnabled();
		} else if (level == MessageLogLevel.INFO) {
			return _log.isInfoEnabled();
		} else if (level == MessageLogLevel.WARN) {
			return _log.isWarnEnabled();
		} else if (level == MessageLogLevel.ERROR) {
			return _log.isErrorEnabled();
		} else if (level == MessageLogLevel.FATAL) {
			return _log.isFatalEnabled();
		}
		return false;
	}

	private void doLog(MessageLogLevel level, String message) {
		if (isLevelEnabled(level)) {
			if (level == MessageLogLevel.TRACE) {
				_log.trace(message);
			} else if (level == MessageLogLevel.DEBUG) {
				_log.debug(message);
			} else if (level == MessageLogLevel.INFO) {
				_log.info(message);
			} else if (level == MessageLogLevel.WARN) {
				_log.warn(message);
			} else if (level == MessageLogLevel.ERROR) {
				_log.error(message);
			} else if (level == MessageLogLevel.FATAL) {
				_log.fatal(message);
			}
		}
	}

	private void doLog(MessageLogLevel level, String message, Throwable t) {
		if (isLevelEnabled(level)) {
			if (level == MessageLogLevel.TRACE) {
				_log.trace(message, t);
			} else if (level == MessageLogLevel.DEBUG) {
				_log.debug(message, t);
			} else if (level == MessageLogLevel.INFO) {
				_log.info(message, t);
			} else if (level == MessageLogLevel.WARN) {
				_log.warn(message, t);
			} else if (level == MessageLogLevel.ERROR) {
				_log.error(message, t);
			} else if (level == MessageLogLevel.FATAL) {
				_log.fatal(message, t);
			}
		}
	}
}