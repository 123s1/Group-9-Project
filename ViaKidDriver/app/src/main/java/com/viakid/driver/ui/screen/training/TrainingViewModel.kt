package com.viakid.driver.ui.screen.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viakid.driver.data.remote.*
import com.viakid.driver.data.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 培训界面的UI状态数据类
 *
 * @property courses 培训课程列表
 * @property completedCount 已完成的课程数量
 * @property totalCount 课程总数
 * @property examInfo 考试信息，如果尚未解锁则为null
 * @property certificate 证书信息，如果尚未获得则为null
 * @property isLoading 是否正在加载数据
 * @property errorMessage 错误信息，如果没有错误则为null
 * @property isTrainingCompleted 是否已完成所有培训课程
 * @property canTakeExam 是否可以参加考试
 */
data class TrainingUiState(
    val courses: List<CourseDto> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 6,
    val examInfo: ExamInfoDto? = null,
    val certificate: CertificateDto? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isTrainingCompleted: Boolean = false,
    val canTakeExam: Boolean = false
)

/**
 * 培训界面的ViewModel，负责管理培训相关的数据和业务逻辑
 *
 * @property trainingRepository 培训数据仓库，用于获取和操作培训相关数据
 */
@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingUiState())

    /**
     * UI状态流，供界面观察和响应式更新
     */
    val uiState: StateFlow<TrainingUiState> = _uiState.asStateFlow()

    init {
        loadCourses()
        loadExamInfo()
    }

    /**
     * 加载培训课程列表，并更新完成状态和考试资格
     */
    fun loadCourses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            trainingRepository.getCourses().onSuccess {
                /** @param courses 培训课程列表 */
                    courses ->
                val completed = courses.count { it.status == "completed" }
                val allCompleted = courses.all { it.status == "completed" }
                _uiState.value = _uiState.value.copy(
                    courses = courses, completedCount = completed, totalCount = courses.size,
                    isLoading = false, isTrainingCompleted = allCompleted, canTakeExam = allCompleted
                )
            }.onFailure {
                /** @param e 错误信息 */
                    e ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    /**
     * 加载考试信息，包括考试资格和考试详情
     */
    fun loadExamInfo() {
        viewModelScope.launch {
            trainingRepository.getExamInfo().onSuccess {
                /** @param info 考试信息 */
                    info ->
                _uiState.value = _uiState.value.copy(examInfo = info, canTakeExam = info.canTake)
            }
        }
    }

    /**
     * 加载证书信息，在通过考试后调用以获取证书详情
     */
    fun loadCertificate() {
        viewModelScope.launch {
            trainingRepository.getCertificate().onSuccess {
                /** @param cert 证书信息 */
                    cert ->
                _uiState.value = _uiState.value.copy(certificate = cert)
            }
        }
    }

    /**
     * 标记指定课程为已完成，并更新整体培训进度和考试资格
     *
     * @param courseId 要标记为完成的课程ID
     */
    fun markCourseComplete(courseId: String) {
        viewModelScope.launch {
            trainingRepository.markCourseComplete(courseId).onSuccess {
                val updatedCourses = _uiState.value.courses.map {
                    if (it.id == courseId) it.copy(status = "completed") else it
                }
                val completed = updatedCourses.count { it.status == "completed" }
                val allCompleted = updatedCourses.all { it.status == "completed" }
                _uiState.value = _uiState.value.copy(
                    courses = updatedCourses, completedCount = completed,
                    isTrainingCompleted = allCompleted, canTakeExam = allCompleted
                )
                loadExamInfo()
            }.onFailure {
                /** @param e 错误信息 */
                    e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    /**
     * 清除当前的错误信息，通常在界面处理完错误后调用
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
