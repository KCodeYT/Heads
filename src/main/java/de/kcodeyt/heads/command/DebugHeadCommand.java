package de.kcodeyt.heads.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import de.kcodeyt.heads.Heads;
import de.kcodeyt.heads.util.HeadInput;

public class DebugHeadCommand extends Command {

    public DebugHeadCommand() {
        super("debughead", "Command to give a player head", "/debughead <name>");
        this.setPermission("heads.command.debughead");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("This command is only available for players!");
            return false;
        }

        if(!this.testPermission(sender))
            return false;

        final Player player = (Player) sender;
        if(args.length == 0) {
            player.sendMessage("§cUsage: " + this.getUsage());
            return false;
        }

        Heads.createItem(HeadInput.ofPlayer(String.join(" ", args))).whenComplete((result, throwable) -> {
            if(throwable != null) {
                player.sendMessage("§cPlayer not found!");
                return;
            }

            player.getInventory().addItem(result.getItem());
            player.sendMessage("§aGave you the player head of " + result.getName() + "!");
        });
        return false;
    }

}
