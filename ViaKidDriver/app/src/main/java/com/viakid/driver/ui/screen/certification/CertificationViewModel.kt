package com.viakid.driver.ui.screen.certification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viakid.driver.data.repository.DriverRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 认证界面的UI状态数据类
 *
 * @property currentStep 当前认证步骤(1:基础信息 2:证件上传 3:背景调查 4:审核完成)
 * @property basicInfo 基础信息状态
 * @property certificates 证件上传状态
 * @property backgroundCheck 背景调查状态
 * @property isLoading 是否正在加载数据
 * @property errorMessage 错误消息,无错误时为null
 * @property isCompleted 认证是否已完成
 */
data class CertificationUiState(
    val currentStep: Int = 1,
    val basicInfo: BasicInfoState = BasicInfoState(),
    val certificates: CertificatesState = CertificatesState(),
    val backgroundCheck: BackgroundCheckState = BackgroundCheckState(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isCompleted: Boolean = false
)

/**
 * 基础信息状态数据类
 *
 * @property name 姓名
 * @property gender 性别(male/female)
 * @property birthday 生日
 * @property emergencyContact 紧急联系人姓名
 * @property emergencyPhone 紧急联系人电话
 * @property isCompleted 基础信息是否填写完成
 * @property isSaving 是否正在保存中
 */
data class BasicInfoState(
    val name: String = "",
    val gender: String = "male",
    val birthday: String = "",
    val emergencyContact: String = "",
    val emergencyPhone: String = "",
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false
)

/**
 * 证件上传状态数据类
 *
 * @property idCardFront 身份证正面信息
 * @property idCardBack 身份证反面信息
 * @property driverLicense 驾驶证信息
 * @property criminalRecord 无犯罪记录证明信息
 * @property healthCert 健康证明信息
 * @property vehicleLicense 行驶证信息
 * @property completedCount 已完成的证件数量
 * @property totalCount 总证件数量(6个)
 * @property isUploading 当前正在上传的证件类型,无上传时为null
 * @property isSaving 是否正在保存中
 */
@Suppress("MemberVisibilityCanBePrivate")
data class CertificatesState(
    val idCardFront: CertificateItemState = CertificateItemState(),
    val idCardBack: CertificateItemState = CertificateItemState(),
    val driverLicense: CertificateItemState = CertificateItemState(),
    val criminalRecord: CertificateItemState = CertificateItemState(),
    val healthCert: CertificateItemState = CertificateItemState(),
    val vehicleLicense: CertificateItemState = CertificateItemState(),
    val completedCount: Int = 0,
    val totalCount: Int = 6,
    val isUploading: String? = null,
    val isSaving: Boolean = false
)

/**
 * 单个证件项的状态数据类
 *
 * @property url 证件图片的服务器URL,未上传时为null
 * @property status 证件审核状态(pending:待审核, approved:已通过, rejected:已拒绝)
 * @property localFilePath 本地文件路径,用于临时存储选择的图片路径
 */
data class CertificateItemState(
    val url: String? = null,
    val status: String = "pending",
    val localFilePath: String? = null
)

/**
 * 背景调查状态数据类
 *
 * @property status 背景调查状态(pending:待处理, processing:处理中, approved:已通过, rejected:已拒绝)
 * @property progress 背景调查进度百分比(0-100)
 * @property estimatedTime 预计完成时间描述,无预估时为null
 * @property rejectReason 被拒绝的原因,未被拒绝时为null
 */
@Suppress("MemberVisibilityCanBePrivate")
data class BackgroundCheckState(
    val status: String = "pending",
    val progress: Int = 0,
    val estimatedTime: String? = null,
    val rejectReason: String? = null
)

/**
 * 认证界面的ViewModel类,管理认证流程的业务逻辑和状态
 *
 * @property driverRepository 司机数据仓库,用于访问认证相关的API接口和数据操作
 */
@Suppress("MemberVisibilityCanBePrivate")
@HiltViewModel
class CertificationViewModel @Inject constructor(
    private val driverRepository: DriverRepository
) : ViewModel() {

    /** UI状态的StateFlow,供界面观察和更新 */
    private val _uiState = MutableStateFlow(CertificationUiState())

    /** UI状态的只读属性,供界面读取 */
    val uiState: StateFlow<CertificationUiState> = _uiState.asStateFlow()

    init {
        loadCertificationStatus()
    }

    /**
     * 加载认证状态信息,从服务器获取当前认证进度和各步骤的状态数据。
     * 成功时更新UI状态为最新的认证信息,失败时设置错误消息。
     */
    fun loadCertificationStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            driverRepository.getCertificationStatus().onSuccess {
                /** @param status 认证状态信息 */
                    status ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentStep = status.step,
                    basicInfo = BasicInfoState(isCompleted = status.basicInfo?.completed ?: false),
                    certificates = CertificatesState(
                        idCardFront = CertificateItemState(
                            url = status.certificates?.idCardFront?.url,
                            status = status.certificates?.idCardFront?.status ?: "pending"
                        ),
                        idCardBack = CertificateItemState(
                            url = status.certificates?.idCardBack?.url,
                            status = status.certificates?.idCardBack?.status ?: "pending"
                        ),
                        driverLicense = CertificateItemState(
                            url = status.certificates?.driverLicense?.url,
                            status = status.certificates?.driverLicense?.status ?: "pending"
                        ),
                        criminalRecord = CertificateItemState(
                            url = status.certificates?.criminalRecord?.url,
                            status = status.certificates?.criminalRecord?.status ?: "pending"
                        ),
                        healthCert = CertificateItemState(
                            url = status.certificates?.healthCert?.url,
                            status = status.certificates?.healthCert?.status ?: "pending"
                        ),
                        vehicleLicense = CertificateItemState(
                            url = status.certificates?.vehicleLicense?.url,
                            status = status.certificates?.vehicleLicense?.status ?: "pending"
                        ),
                        completedCount = status.certificates?.let {
                            /** @param certs 证件信息 */
                                certs ->
                            listOfNotNull(
                                certs.idCardFront?.status == "approved",
                                certs.idCardBack?.status == "approved",
                                certs.driverLicense?.status == "approved",
                                certs.criminalRecord?.status == "approved",
                                certs.healthCert?.status == "approved",
                                certs.vehicleLicense?.status == "approved"
                            ).count { it }
                        } ?: 0
                    ),
                    backgroundCheck = BackgroundCheckState(
                        status = status.backgroundCheck?.status ?: "pending",
                        progress = status.backgroundCheck?.progress ?: 0,
                        estimatedTime = status.backgroundCheck?.estimatedTime
                    ),
                    isCompleted = status.step >= 4
                )
            }.onFailure {
                /** @param e 异常对象 */
                    e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    /**
     * 更新基础信息字段值,仅更新UI状态中的数据,不触发保存操作。
     *
     * @param name 姓名
     * @param gender 性别
     * @param birthday 生日
     * @param emergencyContact 紧急联系人姓名
     * @param emergencyPhone 紧急联系人电话
     */
    fun updateBasicInfo(name: String, gender: String, birthday: String, emergencyContact: String, emergencyPhone: String) {
        _uiState.value = _uiState.value.copy(
            basicInfo = _uiState.value.basicInfo.copy(
                name = name,
                gender = gender,
                birthday = birthday,
                emergencyContact = emergencyContact,
                emergencyPhone = emergencyPhone
            )
        )
    }

    /**
     * 保存基础信息到服务器,验证必填字段后调用API保存数据。
     * 保存成功后将当前步骤推进到下一步骤(至少到第2步)。
     */
    fun saveBasicInfo() {
        val info = _uiState.value.basicInfo
        if (info.name.isBlank() || info.emergencyContact.isBlank() || info.emergencyPhone.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "请填写完整信息")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                basicInfo = info.copy(isSaving = true),
                errorMessage = null
            )

            driverRepository.updateProfile(
                info.name,
                info.gender,
                info.birthday,
                info.emergencyContact,
                info.emergencyPhone
            ).onSuccess {
                _uiState.value = _uiState.value.copy(
                    basicInfo = _uiState.value.basicInfo.copy(isSaving = false, isCompleted = true),
                    currentStep = maxOf(_uiState.value.currentStep, 2)
                )
            }.onFailure {
                /** @param e 异常对象 */
                    e ->
                _uiState.value = _uiState.value.copy(
                    basicInfo = _uiState.value.basicInfo.copy(isSaving = false),
                    errorMessage = e.message
                )
            }
        }
    }

    /**
     * 上传指定类型的证件图片到服务器。
     * 读取本地文件并调用API上传,上传成功后更新对应证件的状态。
     *
     * @param type 证件类型(如:idCardFront, idCardBack, driverLicense等)
     * @param filePath 本地文件路径,指向要上传的图片文件
     */
    fun uploadCertificate(type: String, filePath: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                certificates = _uiState.value.certificates.copy(isUploading = type),
                errorMessage = null
            )

            // 读取文件并上传
            val file = java.io.File(filePath)
            if (!file.exists()) {
                _uiState.value = _uiState.value.copy(
                    certificates = _uiState.value.certificates.copy(isUploading = null),
                    errorMessage = "文件不存在"
                )
                return@launch
            }

            val fileBytes = file.readBytes()
            val fileName = file.name

            driverRepository.uploadCertificate(type, fileBytes, fileName).onSuccess {
                /** @param url 证件图片URL */
                    url ->
                updateCertificateState(type, url)
                _uiState.value = _uiState.value.copy(
                    certificates = _uiState.value.certificates.copy(isUploading = null)
                )
            }.onFailure {
                /** @param e 异常对象 */
                    e ->
                _uiState.value = _uiState.value.copy(
                    certificates = _uiState.value.certificates.copy(isUploading = null),
                    errorMessage = e.message
                )
            }
        }
    }

    /**
     * 更新指定类型证件的状态信息,包括URL和完成计数。
     *
     * @param type 证件类型(如:idCardFront, idCardBack等)
     * @param url 上传成功后返回的证件图片URL
     */
    private fun updateCertificateState(type: String, url: String) {
        val current = _uiState.value.certificates
        val newState = when (type) {
            "idCardFront" -> current.copy(
                idCardFront = CertificateItemState(url = url, status = "pending"),
                completedCount = current.completedCount + 1
            )

            "idCardBack" -> current.copy(
                idCardBack = CertificateItemState(url = url, status = "pending"),
                completedCount = current.completedCount + 1
            )

            "driverLicense" -> current.copy(
                driverLicense = CertificateItemState(url = url, status = "pending"),
                completedCount = current.completedCount + 1
            )

            "criminalRecord" -> current.copy(
                criminalRecord = CertificateItemState(url = url, status = "pending"),
                completedCount = current.completedCount + 1
            )

            "healthCert" -> current.copy(
                healthCert = CertificateItemState(url = url, status = "pending"),
                completedCount = current.completedCount + 1
            )

            "vehicleLicense" -> current.copy(
                vehicleLicense = CertificateItemState(url = url, status = "pending"),
                completedCount = current.completedCount + 1
            )

            else -> current
        }
        _uiState.value = _uiState.value.copy(certificates = newState)
    }

    /**
     * 清除当前的错误消息,将errorMessage设置为null。
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
