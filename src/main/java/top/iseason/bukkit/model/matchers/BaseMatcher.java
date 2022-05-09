package top.iseason.bukkit.model.matchers;

public abstract class BaseMatcher {

    private boolean isReverse = false;

    public abstract boolean match(Object obj);

    /**
     * 是否将结果反转
     *
     * @return if reverse the result
     */
    public boolean isReverse() {
        return isReverse;
    }

    public void setReverse(boolean reverse) {
        isReverse = reverse;
    }

    /**
     * @param result the excepted result
     * @return opposite result if isReverse
     */
    boolean checkIfReverse(boolean result) {
        if (isReverse) return !result;
        return result;
    }

}
