package utils;

import java.lang.reflect.Method;

public class ReflectionUtils {

    public static Method getMethodByName(Class c, String methodName) {
        for (Method method : c.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }

        return null;
    }
}
