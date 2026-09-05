package at.schulrecht.trainer.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [ModuleEntity::class, QuestionEntity::class, AttemptEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TrainerDatabase : RoomDatabase() {
    abstract fun moduleDao(): ModuleDao
    abstract fun questionDao(): QuestionDao
    abstract fun attemptDao(): AttemptDao

    companion object {
        fun build(context: Context): TrainerDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                TrainerDatabase::class.java,
                "trainer.db"
            ).build()
    }
}
