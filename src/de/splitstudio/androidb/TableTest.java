package de.splitstudio.androidb;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

public class TableTest extends AndroidTestCase {

	private SQLiteDatabase db;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		String dbFilename = "test.db";
		getContext().getDatabasePath(dbFilename).delete();
		Table.createdTables.clear();
		db = getContext().openOrCreateDatabase(dbFilename, SQLiteDatabase.CREATE_IF_NECESSARY, null);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		db.close();
	}

	public void testConstructorWithContext_createDbFile() {
		new TableExample(getContext());
		assertTrue(getContext().getDatabasePath(Table.DB_FILENAME).exists());
	}

	public void testConstructor_createTable() {
		String tableName = TableExample.class.getSimpleName();
		new TableExample(db);
		assertEquals(1, getTableCount(tableName));
	}

	public void testConstructor_tableCreated_noTableInDb() {
		String tableName = TableExample.class.getSimpleName();
		Table.createdTables.add(tableName);
		new TableExample(db);
		assertEquals(0, getTableCount(tableName));
	}

	public void testConstructor_tableWithNewId_callOnUpgrade() {
		String tableName = TableExample.class.getSimpleName();

		Metadata metadata = new Metadata(db);
		metadata.setTable(tableName).setTableVersion(1).save();

		new TableExample(db);
		assertEquals(0, getTableCount(tableName));
	}

	private int getTableCount(final String table) {
		String sql = String.format("SELECT name FROM sqlite_master WHERE type='table' and name='%s'", table);
		Cursor c = db.rawQuery(sql, null);
		return c.getCount();
	}
}
