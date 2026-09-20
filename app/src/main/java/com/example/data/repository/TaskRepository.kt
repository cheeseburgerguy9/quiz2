package com.example.data.repository

import com.example.data.db.StudySessionDao
import com.example.data.db.TaskDao
import com.example.data.model.StudySessionEntity
import com.example.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao, private val studyDao: StudySessionDao) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val studySessions: Flow<List<StudySessionEntity>> = studyDao.getAll()
    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)
    suspend fun clearTasks() = taskDao.clearAllTasks()
    suspend fun addStudySession(session: StudySessionEntity) = studyDao.insert(session)
    suspend fun clearStudySessions() = studyDao.clearAll()
}
