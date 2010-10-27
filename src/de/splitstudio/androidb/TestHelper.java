package de.splitstudio.androidb;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TestHelper {

	public static int getTablesInMetadataCount(final String table, final SQLiteDatabase db) {
		String sql = String.format("SELECT name FROM sqlite_master WHERE type='table' and name='%s'", table);
		Cursor c = db.rawQuery(sql, null);
		return c.getCount();
	}
}
