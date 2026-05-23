package com.viakid.driver.ui.screen.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viakid.driver.data.remote.*
import com.viakid.driver.data.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 考试界面 UI 状态数据类
 *
 * @param examInfo 考试信息，包含考试时间限制等配置
 * @param questions 考试题目列表，包含所有试题内容、选项和类型
 * @param currentQuestionIndex 当前显示的题目索引位置，从 0 开始
 * @param answers 用户答案映射表，key 为题目 ID，value 为选择的答案（多选时为多个字母组合）
 * @param timeRemainingSeconds 剩余考试时间（秒），用于倒计时显示
 * @param isLoading 是否正在加载考试数据，控制加载状态显示
 * @param isSubmitting 是否正在提交试卷，控制提交按钮的加载状态
 * @param errorMessage 错误信息，当加载或提交失败时显示的错误提示
 * @param examResult 考试结果数据，包含分数、是否通过、证书信息等，交卷后填充
 * @param isExamFinished 考试是否已结束，用于判断是否需要跳转到结果页面
 */
data class ExamUiState(
    val examInfo: ExamInfoDto? = null,
    val questions: List<QuestionDto> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val answers: Map<String, String> = emptyMap(),
    val timeRemainingSeconds: Int = 0,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val examResult: ExamResultDto? = null,
    val isExamFinished: Boolean = false
)

/**
 * 考试界面视图模型
 *
 * 管理考试界面的状态和业务逻辑，包括加载考试信息、计时器控制、
 * 答题处理、题目导航和试卷提交等功能。
 *
 * @property trainingRepository 培训仓库，提供考试相关的网络请求和数据访问功能
 */
@HiltViewModel
class ExamViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamUiState())

    /**
     * 考试界面 UI 状态流，供界面观察和响应式更新
     */
    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()
    private var timerJob: Job? = null

    init {
        loadExam()
    }

    private fun loadExam() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val examInfoResult = trainingRepository.getExamInfo()
            val questionsResult = trainingRepository.getExamQuestions()

            if (examInfoResult.isSuccess && questionsResult.isSuccess) {
                val examInfo = examInfoResult.getOrNull()
                val questions = questionsResult.getOrNull() ?: emptyList()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    examInfo = examInfo,
                    questions = questions,
                    timeRemainingSeconds = examInfo?.timeLimit ?: 1800,
                    currentQuestionIndex = 0,
                    answers = emptyMap()
                )
                startTimer()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = examInfoResult.exceptionOrNull()?.message
                        ?: questionsResult.exceptionOrNull()?.message
                        ?: "加载考试失败"
                )
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemainingSeconds > 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    timeRemainingSeconds = _uiState.value.timeRemainingSeconds - 1
                )
            }
            submitExam()
        }
    }

    /**
     * 选择答案
     *
     * 根据题目类型处理答案选择：单选题直接替换答案，多选题支持取消已选答案。
     *
     * @param questionId 题目唯一标识符
     * @param answer 选择的答案选项标识（如 "A"、"B" 等）
     */
    fun selectAnswer(questionId: String, answer: String) {
        val currentQuestion = _uiState.value.questions.getOrNull(_uiState.value.currentQuestionIndex)
        if (currentQuestion?.type == "multiple") {
            val currentAnswers = _uiState.value.answers[questionId] ?: ""
            val newAnswers = if (answer in currentAnswers) {
                currentAnswers.replace(answer, "")
            } else {
                currentAnswers + answer
            }
            _uiState.value = _uiState.value.copy(
                answers = _uiState.value.answers + (questionId to newAnswers)
            )
        } else {
            _uiState.value = _uiState.value.copy(
                answers = _uiState.value.answers + (questionId to answer)
            )
        }
    }

    /**
     * 跳转到指定索引的题目
     *
     * @param index 目标题目的索引位置，必须在有效范围内（0 到题目总数-1）
     */
    @Suppress("unused")
    fun goToQuestion(index: Int) {
        if (index in _uiState.value.questions.indices) {
            _uiState.value = _uiState.value.copy(currentQuestionIndex = index)
        }
    }

    /**
     * 跳转到上一题
     *
     * 如果当前不是第一题，则将题目索引减 1。
     */
    fun previousQuestion() {
        val currentIndex = _uiState.value.currentQuestionIndex
        if (currentIndex > 0) {
            _uiState.value = _uiState.value.copy(currentQuestionIndex = currentIndex - 1)
        }
    }

    /**
     * 跳转到下一题
     *
     * 如果当前不是最后一题，则将题目索引加 1。
     */
    fun nextQuestion() {
        val currentIndex = _uiState.value.currentQuestionIndex
        if (currentIndex < _uiState.value.questions.size - 1) {
            _uiState.value = _uiState.value.copy(currentQuestionIndex = currentIndex + 1)
        }
    }

    /**
     * 提交试卷并获取考试结果
     *
     * 停止计时器，将用户答案转换为提交格式，调用仓库接口提交试卷。
     * 提交成功后更新考试结果和完成状态，失败则显示错误信息。
     */
    fun submitExam() {
        timerJob?.cancel()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            val answersList = _uiState.value.answers.map { (questionId, answer) ->
                AnswerDto(questionId, answer)
            }
            trainingRepository.submitExam(answersList).onSuccess {
                /**
                 * @param result 考试结果数据，包含分数、是否通过、证书编号等信息
                 */
                    result ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    examResult = result,
                    isExamFinished = true
                )
            }.onFailure {
                /**
                 * @param e 提交失败时的异常对象，用于获取错误信息
                 */
                    e ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = e.message
                )
            }
        }
    }

    /**
     * 清除错误信息
     *
     * 将错误消息重置为 null，用于用户处理后隐藏错误提示。
     */
    @Suppress("unused")
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * ViewModel 销毁时的清理工作
     *
     * 取消计时器任务，防止内存泄漏。
     */
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
