package fctg.profile.transform.Utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class GetBeanUtil  implements ApplicationContextAware {

	private static ApplicationContext applicationContext = null;
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (GetBeanUtil.applicationContext == null) {
        	GetBeanUtil.applicationContext = applicationContext;
        }
    }
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	public static Object getBean(String beanName) {
		return applicationContext.getBean(beanName);
	}
	public static <T> T getBean(Class<T> c) {
		return applicationContext.getBean(c);
	}
	public static <T> T getBean(String name, Class<T> c) {
		return getApplicationContext().getBean(name, c);
	}
}
