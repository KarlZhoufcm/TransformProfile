package fctg.profile.transform.Utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 由于线程中无法自动注入类，需使用ApplicationContextAware工具类获取
 * @author karl.zhou
 *
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

	private static ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
	
	public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }
	
	public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }
	
	public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }
}
