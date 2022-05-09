package top.iseason.bukkit.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.iseason.bukkit.ConfigManager;
import top.iseason.bukkit.PluginLimiter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Commands implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, @Nullable Command arg1, @Nullable String arg2, @Nullable String[] args) {
        if (sender.isOp() || sender.hasPermission("pluginLimiter.admin")) {
            if (args.length == 1) {
                String arg = Objects.requireNonNull(args[0]);
                if (arg.equalsIgnoreCase("reload")) {
                    ConfigManager.reload();
                    sender.sendMessage(PluginLimiter.color("&7[&aPluginLimiter&7] &a 插件重载成功!"));
                } else if (arg.equalsIgnoreCase("version")) {
                    sender.sendMessage(PluginLimiter.color("&7[&aPluginLimiter&7] &a当前版本为 &l" + PluginLimiter.getInstance().getDescription().getVersion() + "&f of PerWorldPlugins."));
                } else {
                    sender.sendMessage(PluginLimiter.color("&7[&aPluginLimiter&7] &fUsage: &7/pl reload|version"));
                }
                return true;
            }
            sender.sendMessage(PluginLimiter.color("&7[&aPluginLimiter&7] &f用法: &7/pl reload|version"));
        } else {
            sender.sendMessage(PluginLimiter.color("&7[&aPluginLimiter&7] &c没有权限! &7(节点为: pluginLimiter.admin&7)"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String str, @NotNull String[] args) {
        if (args.length == 1) {
            ArrayList<String> list = new ArrayList<>();
            list.add("reload");
            list.add("version");
            list.removeIf(s -> !s.startsWith(args[0].toLowerCase()));
            return list;
        } else {
            return null;
        }
    }
}
