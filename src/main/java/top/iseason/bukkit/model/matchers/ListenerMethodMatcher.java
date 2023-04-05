package top.iseason.bukkit.model.matchers;

import org.bukkit.event.EventHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;

public class ListenerMethodMatcher extends BaseMatcher {

    private final Class<?> listenerClass;
    private final ArrayList<Method> methods = new ArrayList<>();
    private final HashSet<Method> methodsCache = new HashSet<>();

    private boolean matchAll;

    public ListenerMethodMatcher(Class<?> listenerClass, boolean matchAll) {
        this.listenerClass = listenerClass;
        this.matchAll = matchAll;
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
        if (split.length == 2) {
            String parameter = split[1];
            try {
                Class<?> parameterClass = Class.forName(parameter);
                String name = split[0];
                Method method = listenerClass.getDeclaredMethod(name, parameterClass);
                if (method.getAnnotation(EventHandler.class) == null) {
                    return false;
                }
                methods.add(method);
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                return false;
            }
            return true;
        } else if (split.length == 1) {
            //不知道参数，遍历查找第一个符合的
            for (Method declaredMethod : listenerClass.getDeclaredMethods()) {
                if (!declaredMethod.getName().equals(methodString)) continue;
                if (declaredMethod.getAnnotation(EventHandler.class) == null) continue;
                methods.add(declaredMethod);
                return true;
            }
            return false;
        }
        return false;
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

    public void setMatchAll(boolean matchAll) {
        this.matchAll = matchAll;
    }

}
