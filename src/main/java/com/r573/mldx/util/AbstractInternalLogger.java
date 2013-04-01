package com.r573.mldx.util;

public abstract class AbstractInternalLogger {
	public abstract void logError(String msg, Throwable t);

	public abstract void logError(String string);
}
