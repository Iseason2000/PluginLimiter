package top.iseason.bukkit.model.matchers;

public abstract class BaseMatcher {

    private boolean isReverse = false;

    public abstract boolean match(Object obj);

    public boolean isReverse() {
        return isReverse;
    }

    public void setReverse(boolean reverse) {
        isReverse = reverse;
    }

    boolean checkIfReverse(boolean result) {
        if (isReverse) return !result;
        return result;
    }

}
