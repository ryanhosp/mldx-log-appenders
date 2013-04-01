package com.r573.mldx.logger.logback;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.ErrorStatus;

import com.r573.mldx.util.AbstractInternalLogger;

public class LogBackInternalLogger extends AbstractInternalLogger {
	private Context context;
	private Object declaredOrigin;
	
	public LogBackInternalLogger(Context context, Object declaredOrigin) {
		this.context = context;
		this.declaredOrigin = declaredOrigin;
	}
	
	@Override
	public void logError(String msg, Throwable t) {
		System.out.println(msg);
		t.printStackTrace();
		context.getStatusManager().add(new ErrorStatus(msg, declaredOrigin, t));
	}

	@Override
	public void logError(String msg) {
		System.out.println(msg);
		context.getStatusManager().add(new ErrorStatus(msg, declaredOrigin));
	}
}
