package top.iseason.bukkit.model.matchers;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class CommandMatcher extends BaseMatcher {
    private final ArrayList<Pattern> commandPatterns = new ArrayList<>();

    //todo: 完成反序列化
    public static BaseMatcher fromConfig(ConfigurationSection section) {
        return null;
    }

    public void addPattern(String str) {
        commandPatterns.add(Pattern.compile(str));
    }

    @Override
    public boolean match(Object obj) {
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
