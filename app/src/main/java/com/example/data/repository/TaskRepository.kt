package com.example.data.repository

import com.example.data.db.StudyRecordDao
import com.example.data.db.TaskDao
import com.example.data.model.StudyRecord
import com.example.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val studyDao: StudyRecordDao
) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val allStudyRecords: Flow<List<StudyRecord>> = studyDao.getAllRecords()

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)
    suspend fun insertAllTasks(tasks: List<TaskEntity>) = taskDao.insertAllTasks(tasks)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)
    suspend fun getAllTasksList(): List<TaskEntity> = taskDao.getAllTasksList()
    suspend fun clearAllTasks() = taskDao.clearAllTasks()

    suspend fun insertStudyRecord(record: StudyRecord): Long = studyDao.insertRecord(record)
    suspend fun insertAllStudyRecords(records: List<StudyRecord>) = studyDao.insertAll(records)
    suspend fun deleteStudyRecord(record: StudyRecord) = studyDao.deleteRecord(record)
    suspend fun clearAllStudyRecords() = studyDao.clearAll()
}
