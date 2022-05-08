package top.iseason.bukkit.model.matchers;

abstract class BaseMatcher {
    private boolean isReverse = false;

    public abstract boolean match(Object obj);

    boolean checkIfReverse(boolean result) {
        if (isReverse) return !result;
        return result;
    }

}
