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
		assertEquals(1, getTablesInMetadataCount(tableName));
	}

	public void testConstructor_tableCreated_noTableInDb() {
		String tableName = TableExample.class.getSimpleName();
		Table.createdTables.add(tableName);
		new TableExample(db);
		assertEquals(0, getTablesInMetadataCount(tableName));
	}

	public void testConstructor_tableWithNewId_callOnUpgrade() {
		String tableName = TableExample.class.getSimpleName();

		Metadata metadata = new Metadata(db);
		metadata.setTable(tableName).setTableVersion(1).save();

		new TableExample(db);
		assertEquals(0, getTablesInMetadataCount(tableName));
	}

	public void test_isNew_withId_false() {
		Table table = new TableExample(db);
		table._id = 1L;
		assertEquals(false, table.isNew());
	}

	public void test_isNew_withoutId_true() {
		Table table = new TableExample(db);
		assertEquals(true, table.isNew());
	}

	public void test_isNew_withZeroId_true() {
		Table table = new TableExample(db);
		table._id = 0L;
		assertEquals(true, table.isNew());
	}

	public void test_all_emptyTable_emptyCursor() {
		Table table = new TableExample(db);
		Cursor c = table.all();
		assertEquals(0, c.getCount());
	}

	public void test_all_threeRows_threeRows() {
		Table table = new TableExample(db);

		table.insert();
		table._id = null;
		table.insert();
		table._id = null;
		table.insert();

		Cursor c = table.all();
		assertEquals(3, c.getCount());
	}

	public void test_find_withoutId_false() {
		assertEquals(false, new TableExample(db).find());
	}

	public void test_find_withoutZeroId_false() {
		TableExample table = new TableExample(db);
		table._id = 0L;
		assertEquals(false, table.find());
	}

	public void test_find_withId_trueAndFilled() {
		TableExample tableObject = new TableExample(db);
		tableObject.amount = 3.14f;
		tableObject.text = "foo";
		tableObject.save();
		Long id = tableObject._id;

		Table tableDB = new TableExample(db);
		tableDB.find(id);

		assertEquals(tableObject, tableDB);
	}

	public void test_insert_allColumnsInserted() {
		float amount = 3.14f;
		String text = "foo";
		TableExample tableObject = new TableExample(db);
		tableObject.amount = amount;
		tableObject.text = text;
		tableObject.insert();
		Long id = tableObject._id;

		tableObject = new TableExample(db);
		tableObject.find(id);

		assertEquals(amount, tableObject.amount);
		assertEquals(id, tableObject._id);
		assertEquals(text, tableObject.text);
	}

	private int getTablesInMetadataCount(final String table) {
		String sql = String.format("SELECT name FROM sqlite_master WHERE type='table' and name='%s'", table);
		Cursor c = db.rawQuery(sql, null);
		return c.getCount();
	}

}
