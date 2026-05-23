// 接口定义放在 Page 外部
import {showLoading, hideLoading} from '../../utils/util';

interface LoginData {
    phoneNumber: string;
    verifyCode: string;
    password: string;
    agree: boolean;
    countdown: number;
    canLogin: boolean;
    loading: boolean;
    loginType: 'sms' | 'password';
    showPassword: boolean;
    errors: {
        phone?: string;
        code?: string;
        password?: string;
    };
}







Page({
    data: {
        phoneNumber: '',
        verifyCode: '',
        password: '',
        agree: false,
        countdown: 0,
        canLogin: false,
        loading: false,
        loginType: 'sms', // 默认验证码登录
        showPassword: false,
        errors: {}
    } as LoginData,

    // 定时器变量（不能使用 private，直接声明）
    timer: null as number | null,

    onLoad() {
        // 检查是否已登录
        const token = wx.getStorageSync('token');
        if (token) {
            wx.switchTab({
                url: '/pages/index/index'
            });
        }
    },

    onUnload() {
        // 清理定时器
        if (this.timer) {
            clearInterval(this.timer);
            this.timer = null;
        }
    },

    // 切换登录方式
    switchLoginType(e: any) {
        const type = e.currentTarget.dataset.type as 'sms' | 'password';
        this.setData({
            loginType: type,
            errors: {}
        });
        this.checkCanLogin();
    },

    // 手机号输入
    onPhoneInput(e: any) {
        const phoneNumber = e.detail.value;
        this.setData({
            phoneNumber,
            errors: {...this.data.errors, phone: undefined}
        });
        this.checkCanLogin();
    },

    // 验证码输入
    onCodeInput(e: any) {
        const verifyCode = e.detail.value;
        this.setData({
            verifyCode,
            errors: {...this.data.errors, code: undefined}
        });
        this.checkCanLogin();
    },

    // 密码输入
    onPasswordInput(e: any) {
        const password = e.detail.value;
        this.setData({
            password,
            errors: {...this.data.errors, password: undefined}
        });
        this.checkCanLogin();
    },

    // 切换密码显示
    togglePassword() {
        this.setData({
            showPassword: !this.data.showPassword
        });
    },

    // 切换协议勾选
    toggleAgreement() {
        this.setData({agree: !this.data.agree});
        this.checkCanLogin();
    },

    // 检查是否可以登录
    checkCanLogin() {
        const {phoneNumber, verifyCode, password, agree, loginType} = this.data;
        const isPhoneValid = /^1[3-9]\d{9}$/.test(phoneNumber);

        let isValid = false;
        if (loginType === 'sms') {
            isValid = isPhoneValid && verifyCode.length === 6 && agree;
        } else {
            isValid = isPhoneValid && password.length >= 6 && agree;
        }

        this.setData({canLogin: isValid});
    },

    // 验证表单
    validateForm(): boolean {
        const {phoneNumber, verifyCode, password, agree, loginType} = this.data;
        const errors: LoginData['errors'] = {};

        // 手机号验证
        if (!phoneNumber) {
            errors.phone = '请输入手机号';
        } else if (!/^1[3-9]\d{9}$/.test(phoneNumber)) {
            errors.phone = '手机号格式不正确';
        }

        // 验证码/密码验证
        if (loginType === 'sms') {
            // noinspection NegatedIfStatementJS
            if (!verifyCode) {
                errors.code = '请输入验证码';
            } else if (verifyCode.length !== 6) {
                errors.code = '验证码为6位数字';
            }
        } else {
            // noinspection NegatedIfStatementJS
            if (!password) {
                errors.password = '请输入密码';
            } else if (password.length < 6) {
                errors.password = '密码至少6位';
            } else if (password.length > 20) {
                errors.password = '密码最多20位';
            }
        }

        // 协议验证
        if (!agree) {
            wx.showToast({
                title: '请先阅读并同意用户协议',
                icon: 'none'
            });
            return false;
        }

        if (Object.keys(errors).length > 0) {
            this.setData({errors});
            // 显示第一个错误
            const firstError = Object.values(errors)[0];
            if (firstError) {
                wx.showToast({
                    title: firstError,
                    icon: 'none'
                });
            }
            return false;
        }

        return true;
    },

    // 获取验证码
    getVerifyCode() {
        const {phoneNumber, countdown} = this.data;

        if (countdown > 0) {
            return;
        }

        // 验证手机号
        if (!phoneNumber) {
            this.setData({errors: {phone: '请输入手机号'}});
            wx.showToast({title: '请输入手机号', icon: 'none'});
            return;
        }

        if (!/^1[3-9]\d{9}$/.test(phoneNumber)) {
            this.setData({errors: {phone: '手机号格式不正确'}});
            wx.showToast({title: '手机号格式不正确', icon: 'none'});
            return;
        }

        // 开始倒计时
        let count = 60;
        this.setData({countdown: count});

        this.timer = setInterval(() => {
            count--;
            if (count <= 0) {
                if (this.timer) {
                    clearInterval(this.timer);
                    this.timer = null;
                }
                this.setData({countdown: 0});
            } else {
                this.setData({countdown: count});
            }
        }, 1000);

        // 模拟发送验证码
        wx.showToast({
            title: '验证码已发送',
            icon: 'success'
        });

        // 开发环境下，自动填充验证码 123456
        console.log('验证码：123456');

        // 可选：自动填充验证码方便测试
        // this.setData({ verifyCode: '123456' });
        // this.checkCanLogin();
    },

    // 登录
    handleLogin() {
        if (!this.validateForm()) {
            return;
        }

        const {phoneNumber, verifyCode, password, loginType} = this.data;

        this.setData({loading: true});

        // 模拟登录请求
        setTimeout(() => {
            this.setData({loading: false});

            // 验证码：123456，密码：123456
            const isValidCode = verifyCode === '123456';
            const isValidPwd = password === '123456';

            if ((loginType === 'sms' && isValidCode) || (loginType === 'password' && isValidPwd)) {
                // 保存登录信息
                wx.setStorageSync('token', 'mock_token_' + Date.now());
                wx.setStorageSync('userInfo', {
                    phone: phoneNumber,
                    nickname: '家长用户',
                    avatar: '',
                    totalConsumption: 0
                });

                wx.showToast({
                    title: '登录成功',
                    icon: 'success',
                    success: () => {
                        setTimeout(() => {
                            wx.switchTab({
                                url: '/pages/index/index'
                            });
                        }, 1000);
                    }
                });
            } else {
                if (loginType === 'sms') {
                    this.setData({errors: {code: '验证码错误'}});
                    wx.showToast({
                        title: '验证码错误',
                        icon: 'none'
                    });
                } else {
                    this.setData({errors: {password: '密码错误'}});
                    wx.showToast({
                        title: '密码错误',
                        icon: 'none'
                    });
                }
            }
        }, 1500);
    },

    // 微信登录
    wechatLogin() {
        showLoading('登录中...');

        wx.login({
            success: (res) => {
                if (res.code) {
                    // 调用后端接口
                    setTimeout(() => {
                        hideLoading();

                        wx.setStorageSync('token', 'mock_token_' + Date.now());
                        wx.setStorageSync('userInfo', {
                            nickname: '微信用户',
                            avatar: '',
                            totalConsumption: 0
                        });

                        wx.showToast({
                            title: '登录成功',
                            icon: 'success',
                            success: () => {
                                setTimeout(() => {
                                    wx.switchTab({
                                        url: '/pages/index/index'
                                    });
                                }, 1000);
                            }
                        });
                    }, 1000);
                }
            },
            fail: () => {
                hideLoading();
                wx.showToast({
                    title: '登录失败',
                    icon: 'error'
                });
            }
        });
    },

    // 忘记密码
    onForgotPassword() {
        wx.showModal({
            title: '忘记密码',
            content: '请拨打客服热线 400-888-9999 重置密码',
            showCancel: true,
            confirmText: '拨打',
            success: (res) => {
                if (res.confirm) {
                    wx.makePhoneCall({phoneNumber: '4008889999'});
                }
            }
        });
    },

    // 显示用户协议
    showUserAgreement() {
        wx.showModal({
            title: '用户协议',
            content: '这里是用户协议内容...',
            showCancel: false
        });
    },

    // 显示隐私政策
    showPrivacyPolicy() {
        wx.showModal({
            title: '隐私政策',
            content: '这里是隐私政策内容...',
            showCancel: false
        });
    }
});
