package at.schulrecht.trainer.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context

@Database(
    entities = [
        ModuleEntity::class,
        QuestionEntity::class,
        AttemptEntity::class,
        ReviewStateEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TrainerDatabase : RoomDatabase() {
    abstract fun moduleDao(): ModuleDao
    abstract fun questionDao(): QuestionDao
    abstract fun attemptDao(): AttemptDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `review_states` (" +
                        "`questionId` TEXT NOT NULL, " +
                        "`modulId` TEXT NOT NULL, " +
                        "`level` TEXT NOT NULL, " +
                        "`box` INTEGER NOT NULL, " +
                        "`nextDue` INTEGER NOT NULL, " +
                        "`fails` INTEGER NOT NULL, " +
                        "`passes` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`questionId`))"
                )
            }
        }

        fun build(context: Context): TrainerDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                TrainerDatabase::class.java,
                "trainer.db"
            ).addMigrations(MIGRATION_1_2).build()
    }
}
