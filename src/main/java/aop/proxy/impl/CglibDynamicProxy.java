package aop.proxy.impl;

import aop.AopUtils;
import aop.advisor.Advisor;
import aop.proxy.AopProxy;
import beandefinition.BeanDefinition;
import factory.BeanFactory;
import factory.impl.DefaultBeanFactory;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * @Auther: Administrator
 * @Date: 2018-12-11 16:39
 * @Description:
 */
public class CglibDynamicProxy implements MethodInterceptor,AopProxy {

    private static final Log logger = LogFactory.getLog(JDKDynamicProxy.class);
    //加强器 用于生成代理对象
    private Enhancer enhancer;
    private Object target;
    private List<Advisor> advisors;
    private BeanFactory beanFactory;
    private String beanName;

    public CglibDynamicProxy(Object target, List<Advisor> advisors, BeanFactory beanFactory, String beanName) {
        this.target = target;
        this.advisors = advisors;
        this.beanFactory = beanFactory;
        this.beanName = beanName;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Exception {
        return AopUtils.applyAdvice(target, advisors, objects, method, beanFactory);
    }

    @Override
    public Object getProxy() {
        return getProxy(target.getClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        if (logger.isDebugEnabled()) {
            logger.debug("为" + target + "创建cglib代理");
        }
        enhancer.setSuperclass(target.getClass());
        enhancer.setClassLoader(classLoader);
        enhancer.setInterfaces(target.getClass().getInterfaces());
        enhancer.setCallback(this);
        Constructor<?>[] constructors = target.getClass().getConstructors();
        Object res = null;
        if(constructors.length > 0){
            BeanDefinition bd = ((DefaultBeanFactory) beanFactory).getBeanDefinition(beanName);
            return enhancer.create(bd.getConstructor().getParameterTypes(), bd.getConstructorArg().toArray());
        }else {
            return enhancer.create();
        }
    }
}
