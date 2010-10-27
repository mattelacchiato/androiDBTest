package de.splitstudio.androidb;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import de.splitstudio.androidb.annotation.TableMetaData;

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

		TableExample tableDB = new TableExample(db);
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

	public void test_delete_withoutId_false() {
		assertEquals(false, new TableExample(db).delete());
	}

	public void test_delete_idNotInDb_false() {
		TableExample tableExample = new TableExample(db);
		tableExample._id = 42L;
		assertEquals(false, tableExample.delete());
	}

	public void test_delete_idInDb_true() {
		TableExample tableExample = new TableExample(db);
		tableExample._id = 42L;
		tableExample.insert();
		assertEquals(true, tableExample.delete());
	}

	public void test_update_nullId_false() {
		assertEquals(false, new TableExample(db).update());
	}

	public void test_update_withIdInDb_true() {
		TableExample table = new TableExample(db);
		table._id = 42L;
		table.insert();

		table.amount = 3.14f;
		assertEquals(true, table.update());

		table = new TableExample(db);
		table.find(42L);
		assertEquals(3.14f, table.amount);
	}

	public void test_save_noId_insert() {
		TableExample table = new TableExample(db);
		table.save();
		table = new TableExample(db);
		table.save();
		assertEquals(2, table.all().getCount());
	}

	public void test_save_id_update() {
		TableExample table = new TableExample(db);
		table.save();
		table.save();
		assertEquals(1, table.all().getCount());
	}

	public void test_drop_dropExistingTable_droppedAndRemovedFromMemory() {
		TableExample table = new TableExample(db);
		table.drop();
		assertEquals(0, getTablesInMetadataCount(table.getTableName()));
		assertEquals(false, Table.createdTables.contains(table.getClass()));
	}

	public void test_drop_dropNotExistingTable_noOneCares() {
		TableExample table = new TableExample(db);
		table.drop();
		table.drop();
	}

	public void test_equals_equalTable_true() {
		TableExample table1 = new TableExample(db);
		TableExample table2 = new TableExample(db);
		table1._id = 42L;
		table1.amount = 3.14f;
		table1.text = new String("foo");
		table2._id = 42L;
		table2.amount = 3.14f;
		table2.text = new String("foo");
		assertEquals(table1, table2);
	}

	public void test_equals_unequalTable_false() {
		TableExample table1 = new TableExample(db);
		TableExample table2 = new TableExample(db);
		table1._id = 42L;
		table1.amount = 3.14f;
		table1.text = new String("foo");
		table2._id = 42L;
		table2.amount = 3.14001f;
		table2.text = new String("foo");
		assertEquals(false, table1.equals(table2));
	}

	public void test_getVersion_noObjectVersion_exception() {
		class NotVersionedTable extends Table {
			NotVersionedTable(final SQLiteDatabase db) {
				super(db);
			}
		}

		try {
			Table noVersion = new NotVersionedTable(db);
			noVersion.getVersion();
			fail("Expected to throw IllegalStateException");
		} catch (IllegalStateException e) {}
	}

	public void test_getVersion_objectVersionZero_exception() {
		@TableMetaData(version = 0)
		class NotVersionedTable extends Table {
			NotVersionedTable(final SQLiteDatabase db) {
				super(db);
			}
		}

		try {
			Table noVersion = new NotVersionedTable(db);
			noVersion.getVersion();
			fail("Expected to throw IllegalStateException");
		} catch (IllegalStateException e) {}
	}

	public void test_fillFirst_emptyCursor_false() {
		Table table = new TableExample(db);
		Cursor c = table.all();
		assertEquals(false, table.fillFirst(c));
	}

	public void test_fillFirst_cursorMultipleRows_trueAndFirstFilled() {
		Table table = new TableExample(db);
		table.insert();
		table = new TableExample(db);
		table.insert();

		Cursor c = table.all();
		assertEquals(true, table.fillFirst(c));
		assertEquals(1, (long) table._id);//this cast sucks!
	}

	private int getTablesInMetadataCount(final String table) {
		String sql = String.format("SELECT name FROM sqlite_master WHERE type='table' and name='%s'", table);
		Cursor c = db.rawQuery(sql, null);
		return c.getCount();
	}

}
