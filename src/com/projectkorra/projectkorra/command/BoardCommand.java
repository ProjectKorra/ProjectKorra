package com.projectkorra.projectkorra.command;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.board.BendingBoardManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;

/**
 * Executor for /bending board. Extends {@link PKCommand}.
 */
public class BoardCommand extends PKCommand {

	public BoardCommand() {
		super("board", "/bending board", ConfigManager.languageConfig.get().getString("Commands.Board.Description"), new String[]{ "bendingboard", "board", "bb" });
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.isPlayer(sender) || !this.correctLength(sender, args.size(), 0, 0)) {
			return;
		}
		BendingBoardManager.toggleScoreboard((Player) sender);
	}
}
