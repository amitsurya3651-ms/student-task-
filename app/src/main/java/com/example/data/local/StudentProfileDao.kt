package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.StudentProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentProfileDao {
    @Query("SELECT * FROM student_profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<StudentProfile>>

    @Query("SELECT * FROM student_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): StudentProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: StudentProfile): Long

    @Update
    suspend fun updateProfile(profile: StudentProfile)

    @Delete
    suspend fun deleteProfile(profile: StudentProfile)
}
