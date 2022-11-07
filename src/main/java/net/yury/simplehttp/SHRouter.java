package net.yury.simplehttp;

import io.netty.handler.codec.http.HttpMethod;
import net.yury.simplehttp.annotation.SHController;
import net.yury.simplehttp.annotation.SHMapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class SHRouter {
    private Map<String, Object[]> routerMap = new HashMap<>();

    public SHRouter(SHController[] controllers) {
        for (SHController controller : controllers) {
            for (Method declaredMethod : controller.getClass().getDeclaredMethods()) {
                SHMapping annotation = declaredMethod.getAnnotation(SHMapping.class);
                if (annotation == null) {
                    continue;
                }
                String uri = annotation.path();
                if (routerMap.containsKey(uri)) {
                    throw new RuntimeException("uri " + uri + " duplicated");
                }
                routerMap.put(uri, new Object[] {declaredMethod, controller});
            }
        }
    }

    public Object answer(HttpMethod method, String uri, String content) {
        if (!routerMap.containsKey(uri)) {
            throw new RuntimeException("uri " + uri + " not exists");
        }
        Object[] os = routerMap.get(uri);
        Method declaredMethod = (Method)os[0];
        String requiredMethod;
        SHMapping annotation = declaredMethod.getAnnotation(SHMapping.class);
        if (annotation == null) {
            requiredMethod = HttpMethod.GET.name();
        }else {
            requiredMethod = annotation.method();
        }
        if (!requiredMethod.equals(method.name())) {
            throw new RuntimeException("method " + method.name() + " not match");
        }
        return execute(os[1], declaredMethod, content);
    }

    public Object execute(Object o, Method method, String param) {
        Object reply;
        try {
            reply = method.invoke(o, param);
        }catch (Exception ex) {
            ex.printStackTrace();
            reply = ex;
        }
        return reply;
    }
}
