import {getOrderStatusText, getOrderStatusClass, showToast} from '../../utils/util';

Page({
    data: {
        // 用户信息
        userInfo: {
            avatarUrl: 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0',
            nickName: ''
        },

        // 当前订单
        currentOrder: null as any,
        orderStatusText: '',
        orderStatusClass: '',

        // 会员信息
        memberLevel: 'silver',
        memberLevelName: '银卡会员',
        nextLevelName: '金卡会员',
        totalConsumption: 1280,
        nextLevelAmount: 1720,
        memberProgress: 42,

        // 推荐服务
        recommendServices: [
            {id: 1, name: '作业辅导', price: '+15元', icon: '📚'},
            {id: 2, name: '营养加餐', price: '+8元', icon: '🍎'},
            {id: 3, name: '临时托管', price: '30元/时', icon: '🏠'},
            {id: 4, name: '周末研学', price: '128元', icon: '🎒'}
        ],

        // 安心日记
        diaryList: [
            {id: 1, title: '小宝的快乐放学时光', videoUrl: '', createTime: '1月15日'},
            {id: 2, title: '今天在驿站和小伙伴玩耍', videoUrl: '', createTime: '1月14日'},
            {id: 3, title: '认真做作业的小明', videoUrl: '', createTime: '1月13日'}
        ]
    },

    onLoad() {
        // 检查登录状态
        const token = wx.getStorageSync('token');
        if (!token) {
            wx.reLaunch({url: '/pages/login/login'});
            return;
        }

        this.loadUserInfo();
        this.loadCurrentOrder();
    },

    onShow() {
        this.loadUserInfo();
        this.loadCurrentOrder();
    },

    onPullDownRefresh() {
        this.loadUserInfo();
        this.loadCurrentOrder();
        wx.stopPullDownRefresh();
    },

    // 加载用户信息
    loadUserInfo() {
        const userInfo = wx.getStorageSync('userInfo');
        if (userInfo) {
            this.setData({
                userInfo: {
                    avatarUrl: userInfo.avatar || this.data.userInfo.avatarUrl,
                    nickName: userInfo.nickname || '家长用户'
                },
                totalConsumption: userInfo.totalConsumption || 1280
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

    // 加载当前订单
    loadCurrentOrder() {
        // Mock数据 - 模拟一个进行中的订单
        const mockOrder = {
            id: '10001',
            orderNo: '202401150001',
            type: 'pickup',
            childName: '小明',
            escortName: '张师傅',
            scheduleTime: '今天 16:30',
            amount: 58,
            status: 2
        };

        this.setData({
            currentOrder: mockOrder,
            orderStatusText: getOrderStatusText(2),
            orderStatusClass: getOrderStatusClass(2)
        });
    },

    // 跳转到订单页面
    goToOrder(e: any) {
        const type = e.currentTarget.dataset.type;
        if (type === 'emergency') {
            showToast('紧急加单功能开发中', 'none');
        } else {
            wx.navigateTo({
                url: '/pages/order/create-order'
            });
        }
    },

    // 跳转到订单列表
    goToOrderList() {
        wx.switchTab({
            url: '/pages/order/order'
        });
    },

    // 孩子管理
    goToChildren() {
        showToast('孩子管理功能开发中', 'none');
    },

    // 会员中心
    goToMemberCenter() {
        showToast('会员中心功能开发中', 'none');
    },

    // 查看订单详情
    viewOrderDetail() {
        if (this.data.currentOrder) {
            wx.navigateTo({
                url: `/pages/order-detail/order-detail?id=${this.data.currentOrder.id}`
            });
        }
    },

    // 实时追踪
    trackOrder() {
        if (this.data.currentOrder) {
            wx.navigateTo({
                url: `/pages/track/track?orderId=${this.data.currentOrder.id}`
            });
        }
    },

    // 服务详情
    goToService(e: any) {
        const id = e.currentTarget.dataset.id;
        showToast('服务详情开发中', 'none');
    },

    // 查看更多服务
    viewMoreServices() {
        showToast('更多服务开发中', 'none');
    },

    // 播放安心日记
    playDiary(e: any) {
        wx.navigateTo({
            url: '/pages/safe-diary/safe-diary'
        });
    },

    // 查看更多日记
    viewMoreDiary() {
        wx.navigateTo({
            url: '/pages/safe-diary/safe-diary'
        });
    },

    // 紧急求助
    onEmergencyCall() {
        wx.showActionSheet({
            itemList: ['拨打平台热线 400-888-9999', '拨打110', '取消'],
            success(res) {
                if (res.tapIndex === 0) {
                    wx.makePhoneCall({phoneNumber: '4008889999'});
                } else if (res.tapIndex === 1) {
                    wx.makePhoneCall({phoneNumber: '110'});
                }
            }
        });
    },

    // 跳转到支付页面
    goToPayment() {
        wx.navigateTo({
            url: '/pages/payment/payment'
        });
    },

    // 跳转到安心日记页面
    goToDiary() {
        wx.navigateTo({
            url: '/pages/safe-diary/safe-diary'
        });
    }
});
