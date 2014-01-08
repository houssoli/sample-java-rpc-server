package com.merchantry.goncharov;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Do actual work to execute {@link MethodInvocationRequest}.
 * First {@code addServices} and then {@code invoke}.
 * Relies on {@link Immutable} and {@link ThreadSafe} annotations to decide whether to lock service or not.
 */
public class ServiceInvoker {
    static Logger log = Logger.getLogger(ServiceInvoker.class);

    private static ServiceInvoker ourInstance = new ServiceInvoker();
    public static ServiceInvoker getInstance() {
        return ourInstance;
    }

    private ServiceInvoker() {}

    private final Map<String, Object> services = new ConcurrentHashMap<String, Object>();
    private final Map<String, Boolean> servicesSafety = new ConcurrentHashMap<String, Boolean>();

    public Object invoke(String serviceName, String method, Serializable[] parameters) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InvalidCommunicationProtocolException {
        Object service = getService(serviceName);
        if (servicesSafety.get(serviceName)) {
            return invokeUnsafe(service, method, parameters);
        }

        synchronized (service) {
            return invokeUnsafe(service, method, parameters);
        }
    }

    private Object invokeUnsafe(Object service, String method, Serializable[] parameters) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InvalidCommunicationProtocolException {
        Serializable[] p = parameters;

        if (p == null) {
            p = new Serializable[] {};
        }

        Class[] classes = new Class[p.length];
        try {
            for (int i = 0; i < p.length; i++) {
                classes[i] = p[i].getClass();
            }
        } catch (NullPointerException e) {
            throw new InvalidCommunicationProtocolException("null parameter values are not supported");
        }

        Method m = service.getClass().getMethod(method, classes);

        Object result =  m.invoke(service, (Object[]) parameters);
        return result;
    }

    public Object getService(String serviceName) {
        if (!services.containsKey(serviceName)) {
            throw new UnsupportedOperationException(String.format("Service %s is not found", serviceName));
        }
        return services.get(serviceName);
    }

    public void addServices(Set<Map.Entry<Object, Object>> serviceDefinitions) {
        //ClassLoader classLoader = this.getClass().getClassLoader();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        for (Map.Entry<Object, Object> entry : serviceDefinitions) {
            try {
                Class<?> c = classLoader.loadClass(entry.getValue().toString());
                services.put(entry.getKey().toString(), c.newInstance());
                boolean isSafe = (c.isAnnotationPresent(Immutable.class)) || c.isAnnotationPresent(ThreadSafe.class);
                servicesSafety.put(entry.getKey().toString(), isSafe);
            } catch (ClassNotFoundException e) {
                log.error(String.format("Cannot load class for service definition %s. Service skipped.", entry), e);
            } catch (InstantiationException e) {
                log.error(String.format("Cannot create service instance %s. Service skipped.", entry), e);
            } catch (IllegalAccessException e) {
                log.error(String.format("Cannot load class for service definition %s. Service skipped.", entry), e);
            }
        }
    }
}
