package top.iseason.bukkit.model.matchers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CommandMatcher extends BaseMatcher {
    private final ArrayList<Pattern> commandPatterns = new ArrayList<>();

    private final boolean matchAll;

    public CommandMatcher(boolean matchAll) {
        this.matchAll = matchAll;
    }

    public static CommandMatcher fromConfig(List<String> stringList) {
        CommandMatcher commandMatcher = new CommandMatcher(stringList.isEmpty());
        for (String s : stringList) {
            commandMatcher.addPattern(s);
        }
        return commandMatcher;
    }

    public void addPattern(String str) {
        if (matchAll) return;
        commandPatterns.add(Pattern.compile(str));
    }

    @Override
    public boolean match(Object obj) {
        if (matchAll) return checkIfReverse(true);
        if (!(obj instanceof String)) return checkIfReverse(false);
        String command = (String) obj;
        for (Pattern pattern : commandPatterns) {
            if (pattern.matcher(command).find()) {
                return checkIfReverse(true);
            }
        }
        return checkIfReverse(false);
    }
}
