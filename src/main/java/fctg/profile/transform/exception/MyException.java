package fctg.profile.transform.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MyException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Exception e;
	private String msg;
	private Integer line;
	private String methodName;
	private String classPath;
	private static final Logger logger = LoggerFactory.getLogger(MyException.class);
	
	public MyException(Exception e) {
		msg = e.getMessage();
		if(e.getStackTrace().length>0) {
			StackTraceElement stackTraceElement = e.getStackTrace()[0];
			line = stackTraceElement.getLineNumber();
			methodName = stackTraceElement.getMethodName();
			classPath = stackTraceElement.getClassName();
		}
		logger.error(this.toString());
//		异常追踪
		StackTraceElement[] trace = e.getStackTrace();
		for (StackTraceElement stackTraceElement : trace) {
			logger.error(stackTraceElement.toString());
		}
	}

	public MyException(Exception e, String msgs) {
		msg = msgs;
		System.out.println(msg);
		if(e.getStackTrace().length>0) {
			StackTraceElement stackTraceElement = e.getStackTrace()[0];
			line = stackTraceElement.getLineNumber();
			methodName = stackTraceElement.getMethodName();
			classPath = stackTraceElement.getClassName();
		}
		logger.error(this.toString());
	}

	public Exception getE() {
		return e;
	}

	public void setE(Exception e) {
		this.e = e;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getClassPath() {
		return classPath;
	}

	public void setClassPath(String classPath) {
		this.classPath = classPath;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public MyException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public MyException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public MyException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public MyException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "MyException [e=" + e + ", msg=" + msg + ", line=" + line + ", methodName=" + methodName + ", classPath="
				+ classPath + "]";
	}
	
}
