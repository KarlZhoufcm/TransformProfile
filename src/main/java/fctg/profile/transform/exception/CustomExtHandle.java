package fctg.profile.transform.exception;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

@RestControllerAdvice
public class CustomExtHandle {

	// 捕获全局异常
    @ExceptionHandler(value = {Exception.class})
    Object handleException(Exception e, HttpServletRequest request) {
    	MyException ex = new MyException(e);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("error.html");
        modelAndView.addObject("msg", ex.getMsg());
        modelAndView.addObject("classPath", ex.getClassPath());
        modelAndView.addObject("methodName", ex.getMethodName());
        modelAndView.addObject("line", ex.getLine());
        modelAndView.addObject("url", request.getRequestURL());
        return modelAndView;
    }
    
    /**
     * 	private String msg;
		private Integer line;
		private String methodName;
		private String classPath;
     * @param e
     * @param request
     * @return
     */
    // 如果是Myexception类
    @ExceptionHandler(value = MyException.class)
    Object handleMyException(MyException e, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("error.html"); // 指定错误跳转页面 需要在templates里面新建 一个error.html
        modelAndView.addObject("msg", e.getMsg());
        modelAndView.addObject("classPath", e.getClassPath());
        modelAndView.addObject("methodName", e.getMethodName());
        modelAndView.addObject("line", e.getLine());
        modelAndView.addObject("url", request.getRequestURL());
        return modelAndView;
        // 当然这里也可以返回json数据 前后台分离的话直接返回一个json即可
    }
}
