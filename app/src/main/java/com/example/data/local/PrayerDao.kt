package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {

    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM prayer_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<PrayerLogEntity>>

    @Query("SELECT * FROM prayer_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<PrayerLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PrayerLogEntity): Long

    @Query("SELECT * FROM prayer_logs WHERE date = :date AND prayerName = :prayerName LIMIT 1")
    suspend fun getLogForPrayerToday(date: String, prayerName: String): PrayerLogEntity?

    @Query("SELECT * FROM penalty_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<PenaltyTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PenaltyTransactionEntity): Long

    @Query("SELECT COUNT(*) FROM prayer_logs WHERE status = 'OFFERED'")
    fun getTotalOfferedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM prayer_logs WHERE status = 'SKIPPED'")
    fun getTotalSkippedCount(): Flow<Int>
}
