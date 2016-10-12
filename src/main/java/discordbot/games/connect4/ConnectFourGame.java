package discordbot.games.connect4;

import discordbot.games.AbstractGame;
import discordbot.games.GameState;
import discordbot.games.GameTurn;
import discordbot.main.Config;
import discordbot.util.Misc;
import net.dv8tion.jda.entities.User;
import sx.blah.discord.handle.obj.IUser;

/**
 * Created on 9-9-2016
 */
public class ConnectFourGame extends AbstractGame<Connect4Turn> {

	public static final int ROWS = 6, COLS = 7;
	private C4Board board;

	public ConnectFourGame() {
		reset();
	}

	public void reset() {
		super.reset();
		board = new C4Board(COLS, ROWS);
	}

	@Override
	public String getCodeName() {
		return "cf";
	}

	@Override
	public String getFullname() {
		return "Connect Four";
	}

	@Override
	public int getTotalPlayers() {
		return 2;
	}

	@Override
	protected boolean isTheGameOver() {
		return false;
	}

	@Override
	public boolean isValidMove(User player, GameTurn turnInfo) {
		return board.canPlaceInColumn(turnInfo.getColumnIndex());
	}

	@Override
	protected void doPlayerMove(IUser player, Connect4Turn turnInfo) {
		board.placeInColumn(turnInfo.getColumnIndex(), getActivePlayerIndex());
	}

	@Override
	public String toString() {
		String ret = "A Connect 4 game." + Config.EOL;
		ret += board.toString();
		for (int i = 0; i < COLS; i++) {
			if (board.canPlaceInColumn(i)) {
				ret += Misc.numberToEmote(i + 1);
			} else {
				ret += ":no_entry_sign:";
			}
		}
		ret += Config.EOL + Config.EOL;
		if (getGameState().equals(GameState.IN_PROGRESS) || getGameState().equals(GameState.READY)) {
			ret += board.intToPlayer(0) + " = " + getPlayer(0).getName() + Config.EOL;
			ret += board.intToPlayer(1) + " = " + getPlayer(1).getName() + Config.EOL;
			ret += "It's the turn of " + getActivePlayer().mention() + Config.EOL;
			ret += "to play type **game <columnnumber>**";
		}
		return ret;
	}
}
