package top.iseason.perworldplugins.command;

import top.iseason.perworldplugins.PerWorldPlugins;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;

public class Commands implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, @Nullable Command arg1,@Nullable String arg2,@Nullable String[] args) {
		if (sender.isOp() || sender.hasPermission("pwp.admin")) {
			assert args != null;

			if (args.length == 0) {
				sender.sendMessage(PerWorldPlugins.color("&c[&4PWP&c] &fUsage: &7/pwp reload|version"));
			} else {
				if (args[0].equalsIgnoreCase("reload")) {
					PerWorldPlugins.getInstance().reload();
					sender.sendMessage(PerWorldPlugins.color("&a[&2PWP&a] &fPerWorldPlugins successfully reloaded!"));
				} else

				if (args[0].equalsIgnoreCase("version")) {
					sender.sendMessage(PerWorldPlugins.color("&a[&2PWP&a] &fYou are currently running version &l" + PerWorldPlugins.getInstance().getDescription().getVersion() + "&f of PerWorldPlugins."));
				} else {
					sender.sendMessage(PerWorldPlugins.color("&c[&4PWP&c] &fUsage: &7/pwp reload|version"));
				}

				if(args.length >= 2){
					sender.sendMessage(PerWorldPlugins.color("&c[&4PWP&c] &fUsage: &7/pwp reload|version"));
				}
			}
		} else {
			sender.sendMessage(PerWorldPlugins.color("&c[&4PWP&c] &fNo permission! &7(Required node: &opwp.admin&7)"));
		}return true;
	}

}
