package com.example.otebookbeta.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Contact::class, Note::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // no-op: схема не менялась
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // no-op
            }
        }

        /**
         * 2 -> 3:
         * - Пересоздать notes с FK (contactId -> contacts.id ON DELETE CASCADE)
         * - Индекс на notes.contactId
         * - Индекс на contacts.category
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `notes_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `contactId` INTEGER NOT NULL,
                        `content` TEXT NOT NULL,
                        `dateAdded` TEXT NOT NULL,
                        FOREIGN KEY(`contactId`) REFERENCES `contacts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `notes_new` (`id`, `contactId`, `content`, `dateAdded`)
                    SELECT `id`, `contactId`, `content`, `dateAdded` FROM `notes`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `notes`")
                db.execSQL("ALTER TABLE `notes_new` RENAME TO `notes`")

                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_contactId` ON `notes` (`contactId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_category` ON `contacts` (`category`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "otebook_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
