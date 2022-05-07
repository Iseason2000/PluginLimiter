package top.iseason.perworldplugins.command;

import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import top.iseason.perworldplugins.PerWorldPlugins;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Commands implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, @Nullable Command arg1, @Nullable String arg2, @Nullable String[] args) {
        if (sender.isOp() || sender.hasPermission("pwp.admin")) {
            assert args != null;

            if (args.length == 0) {
                sender.sendMessage(PerWorldPlugins.color("&c[&4PWP&c] &f用法: &7/pwp reload|version"));
            } else {
                if (args[0].equalsIgnoreCase("reload")) {
                    PerWorldPlugins.getInstance().reload();
                    sender.sendMessage(PerWorldPlugins.color("&a[&2PWP&a] &f 插件重载成功!"));
                } else if (args[0].equalsIgnoreCase("version")) {
                    sender.sendMessage(PerWorldPlugins.color("&a[&2PWP&a] &f当前版本为 &l" + PerWorldPlugins.getInstance().getDescription().getVersion() + "&f of PerWorldPlugins."));
                } else {
                    sender.sendMessage(PerWorldPlugins.color("&c[&4PWP&c] &fUsage: &7/pwp reload|version"));
                }

                if (args.length >= 2) {
                    sender.sendMessage(PerWorldPlugins.color("&c[&4PWP&c] &f用法: &7/pwp reload|version"));
                }
            }
        } else {
            sender.sendMessage(PerWorldPlugins.color("&c[&4PWP&c] &f没有权限! &7(节点为: &opwp.admin&7)"));
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
