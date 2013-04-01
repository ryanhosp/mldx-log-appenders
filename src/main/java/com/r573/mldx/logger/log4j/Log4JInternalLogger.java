package com.r573.mldx.logger.log4j;

import org.apache.log4j.helpers.LogLog;

import com.r573.mldx.util.AbstractInternalLogger;

public class Log4JInternalLogger extends AbstractInternalLogger {

	@Override
	public void logError(String msg, Throwable t) {
		LogLog.error(msg, t);
	}

	@Override
	public void logError(String msg) {
		LogLog.error(msg);
	}

}
