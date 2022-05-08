package top.iseason.bukkit.model.matchers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;

public class ListenerMethodMatcher extends BaseMatcher {

    private final Class<?> listenerClass;
    private final ArrayList<Method> methods = new ArrayList<>();
    private final HashSet<Method> methodsCache = new HashSet<>();
    private final boolean matchAll;

    public ListenerMethodMatcher(Class<?> listenerClass, boolean matchAll) {
        this.listenerClass = listenerClass;
        this.matchAll = matchAll;
    }

    //todo: 完成反序列化
    public static BaseMatcher fromConfig(ConfigurationSection section) {
        return null;
    }

    /**
     * 只能有一个参数
     * 方法名:参数类型
     *
     * @param methodString such as  "onPlayerDeathEvent:org.bukkit.event.entity.PlayerDeathEvent"
     */
    public boolean addMethod(String methodString) {
        if (matchAll) return true;
        String[] split = methodString.split(":");
        if (split.length != 2) return false;
        String parameter = split[1];
        try {
            Class<?> parameterClass = Class.forName(parameter);
            String name = split[0];
            Method method = listenerClass.getMethod(name, parameterClass);
            if (method.getAnnotation(EventHandler.class) == null) {
                return false;
            }
            methods.add(method);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean match(Object obj) {
        if (matchAll) return checkIfReverse(true);
        if (!(obj instanceof Method)) return checkIfReverse(false);
        Method method = (Method) obj;
        if (methodsCache.contains(method)) return checkIfReverse(true);
        for (Method m : methods) {
            if (m.equals(method)) {
                methodsCache.add(m);
                return checkIfReverse(true);
            }
        }
        return checkIfReverse(false);
    }
}
