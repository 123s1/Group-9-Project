import {maskPhone, formatMoney, showConfirm} from '../../utils/util';

Page({
    data: {
        // 用户信息
        avatar: '',
        nickname: '',
        phone: '',

        // 会员信息
        memberLevel: 'silver',
        memberLevelName: '银卡会员',
        nextLevelName: '金卡会员',
        totalConsumption: 1280,
        nextLevelAmount: 1720,
        memberProgress: 42,

        // 钱包信息
        balance: 0,
        formattedBalance: '¥0.00',
        couponCount: 0,

        // 订单统计
        processingCount: 0,
        completedCount: 0,
        cancelledCount: 0
    },

    onLoad() {
        this.loadUserInfo();
        this.loadOrderStats();
        this.loadWalletInfo();
    },

    onShow() {
        // 每次显示页面时刷新数据
        this.loadUserInfo();
        this.loadOrderStats();
        this.loadWalletInfo();
    },

    // 加载用户信息
    loadUserInfo() {
        const userInfo = wx.getStorageSync('userInfo');
        if (userInfo) {
            this.setData({
                avatar: userInfo.avatar || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0',
                nickname: userInfo.nickname || '家长用户',
                phone: userInfo.phone ? maskPhone(userInfo.phone) : '未绑定手机号',
                totalConsumption: userInfo.totalConsumption || 1280,
                formattedTotalConsumption: formatMoney(userInfo.totalConsumption || 1280)
            });
            this.updateMemberLevel();
        }
    },

    // 更新会员等级
    updateMemberLevel() {
        const consumption = this.data.totalConsumption;
        let level = 'silver';
        let levelName = '银卡会员';
        let nextLevelName = '金卡会员';
        let nextAmount = 3000;
        let progress = 0;

        if (consumption >= 8000) {
            level = 'diamond';
            levelName = '钻石卡会员';
            nextLevelName = '钻石卡会员';
            nextAmount = 0;
            progress = 100;
        } else if (consumption >= 3000) {
            level = 'gold';
            levelName = '金卡会员';
            nextLevelName = '钻石卡会员';
            nextAmount = 8000 - consumption;
            progress = ((consumption - 3000) / 5000) * 100;
        } else {
            nextAmount = 3000 - consumption;
            progress = (consumption / 3000) * 100;
        }

        this.setData({
            memberLevel: level,
            memberLevelName: levelName,
            nextLevelName,
            nextLevelAmount: nextAmount,
            memberProgress: Math.min(progress, 100)
        });
    },

    // 加载订单统计
    loadOrderStats() {
        // Mock数据
        this.setData({
            processingCount: 2,
            completedCount: 15,
            cancelledCount: 1
        });
    },

    // 加载钱包信息
    loadWalletInfo() {
        // Mock数据
        this.setData({
            balance: 358.5,
            formattedBalance: formatMoney(358.5),
            couponCount: 3
        });
    },

    // 编辑个人资料
    editProfile() {
        wx.navigateTo({
            url: '/pages/profile/edit-profile'
        });
    },

    // 跳转会员中心
    goToMember() {
        wx.showToast({title: '功能开发中', icon: 'none'});
    },

    // 跳转订单列表
    goToOrders(e: any) {
        const status = e.currentTarget.dataset.status;
        let tab = 0;
        if (status === 'completed') {
            tab = 1;
        }
        if (status === 'cancelled') {
            tab = 2;
        }

        wx.switchTab({
            url: '/pages/order/order'
        });

        // 可以通过全局变量传递tab参数
        // 或者在order页面onLoad中处理
    },

    // 孩子管理
    goToChildren() {
        wx.navigateTo({
            url: '/pages/children/children'
        });
    },

    // 我的钱包
    goToWallet() {
        wx.showToast({title: '功能开发中', icon: 'none'});
    },

    // 优惠券
    goToCoupons() {
        wx.showToast({title: '功能开发中', icon: 'none'});
    },

    // 套餐中心
    goToPackages() {
        wx.showToast({title: '功能开发中', icon: 'none'});
    },

    // 收货地址
    goToAddress() {
        wx.showToast({title: '功能开发中', icon: 'none'});
    },

    // 发票管理
    goToInvoice() {
        wx.showToast({title: '功能开发中', icon: 'none'});
    },

    // 联系客服
    async callCustomerService() {
        const confirmed = await showConfirm('联系客服', '拨打平台热线 400-888-9999');
        if (confirmed) {
            wx.makePhoneCall({phoneNumber: '4008889999'});
        }
    },

    // 意见反馈
    goToFeedback() {
        wx.navigateTo({
            url: '/pages/feedback/feedback'
        });
    },

    // 关于我们
    goToAbout() {
        wx.showToast({title: '版本 1.0.0', icon: 'none'});
    },

    // 设置
    goToSettings() {
        wx.navigateTo({
            url: '/pages/settings/settings'
        });
    },

    // 退出登录
    async logout() {
        const confirmed = await showConfirm('提示', '确定要退出登录吗？');
        if (confirmed) {
            // 清除登录信息
            wx.removeStorageSync('token');
            wx.removeStorageSync('userInfo');
            // 跳转到登录页
            wx.reLaunch({
                url: '/pages/login/login'
            });
        }
    },

    // 跳转到安心日记页面
    goToDiary() {
        wx.navigateTo({
            url: '/pages/safe-diary/safe-diary'
        });
    },

    // 跳转到支付页面
    goToPayment() {
        wx.navigateTo({
            url: '/pages/payment/payment'
        });
    },

    // 跳转到投诉建议页面
    goToComplaint() {
        wx.navigateTo({
            url: '/pages/complaint/complaint'
        });
    }
});
