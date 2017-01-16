package discordbot.db.controllers;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OBetOption;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CBetOption {
	public static List<OBetOption> getOptionsForBet(int id) {
		List<OBetOption> ret = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select(
				"SELECT *  " +
						"FROM bet_options " +
						"WHERE bet_id = ? ", id)) {
			while (rs.next()) {
				ret.add(fillRecord(rs));
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return ret;
	}

	public static OBetOption findById(int betId, int id) {
		OBetOption b = new OBetOption();
		try (ResultSet rs = WebDb.get().select(
				"SELECT *  " +
						"FROM bet_options " +
						"WHERE bet_id = ? AND id = ? ", betId, id)) {
			if (rs.next()) {
				b = fillRecord(rs);
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return b;
	}

	public static OBetOption findById(int id) {
		OBetOption b = new OBetOption();
		try (ResultSet rs = WebDb.get().select(
				"SELECT *  " +
						"FROM bet_options " +
						"WHERE id = ? ", id)) {
			if (rs.next()) {
				b = fillRecord(rs);
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return b;
	}

	private static OBetOption fillRecord(ResultSet rs) throws SQLException {
		OBetOption b = new OBetOption();
		b.id = rs.getInt("id");
		b.betId = rs.getInt("bet_id");
		b.description = rs.getString("description");
		return b;
	}

	public static void delete(OBetOption record) {
		try {
			WebDb.get().query(
					"DELETE FROM bet_options WHERE id = ? ",
					record.id
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteOptionsFor(int betId) {
		try {
			WebDb.get().query(
					"DELETE FROM bet_options WHERE bet_id = ? ",
					betId
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void update(OBetOption record) {
		try {
			record.id = WebDb.get().query(
					"UPDATE bet_options SET description = ? WHERE id = ?",
					record.description, record.id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insert(OBetOption record) {
		if (record.id > 0) {
			update(record);
			return;
		}
		try {
			record.id = WebDb.get().insert(
					"INSERT INTO bet_options(bet_id, description) " +
							"VALUES (?,?)",
					record.betId, record.description);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addOption(int betId, String description) {
		OBetOption b = new OBetOption();
		b.betId = betId;
		b.description = description;
		insert(b);
	}
}
