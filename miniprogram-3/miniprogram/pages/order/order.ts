import {getOrderStatusText, getOrderStatusClass, showToast, showConfirm} from '../../utils/util';

Page({
    data: {
        currentTab: 0,
        orderList: [] as any[],
        tabNames: ['进行中', '已完成', '已取消']
    },

    onLoad(options: any) {
        // 初始化mock数据
        this.initMockData();

        if (options.tab !== undefined) {
            const tab = parseInt(options.tab);
            if (tab >= 0 && tab <= 2) {
                this.setData({currentTab: tab});
            }
        }
        this.loadOrderList();
    },

    onShow() {
        // 每次显示页面时刷新数据
        this.loadOrderList();
    },

    onPullDownRefresh() {
        this.loadOrderList().finally(() => {
            wx.stopPullDownRefresh();
        });
    },

    // 初始化mock数据（仅在没有数据时）
    initMockData() {
        const existingOrders = wx.getStorageSync('orderList') || [];

        // 如果已有数据，不再添加mock
        if (existingOrders.length > 0) {
            return;
        }

        // 添加一些mock订单数据用于测试
        const mockOrders = [
            {
                id: 'mock_1',
                orderNo: 'ORD202504210001',
                type: 'pickup',
                child: {id: 1, name: '小明', school: '第一小学', grade: '一年级'},
                childName: '小明',
                date: '2024-04-21',
                time: '16:30',
                scheduleTime: '2024-04-21 16:30',
                startLocation: {address: '第一小学'},
                endLocation: {address: '家里'},
                remark: '',
                packageType: 'single',
                amount: 58,
                services: [],
                status: 2, // 服务中
                createTime: new Date().toISOString(),
                evaluated: false
            },
            {
                id: 'mock_2',
                orderNo: 'ORD202504200001',
                type: 'dropoff',
                child: {id: 1, name: '小明', school: '第一小学', grade: '一年级'},
                childName: '小明',
                date: '2024-04-20',
                time: '08:00',
                scheduleTime: '2024-04-20 08:00',
                startLocation: {address: '家里'},
                endLocation: {address: '第一小学'},
                remark: '请准时',
                packageType: 'single',
                amount: 58,
                services: [{id: 1, name: '作业辅导', price: 15}],
                status: 7, // 已完成
                createTime: new Date(Date.now() - 86400000).toISOString(),
                evaluated: true
            },
            {
                id: 'mock_3',
                orderNo: 'ORD202504190001',
                type: 'pickup',
                child: {id: 2, name: '小红', school: '第一小学', grade: '三年级'},
                childName: '小红',
                date: '2024-04-19',
                time: '16:30',
                scheduleTime: '2024-04-19 16:30',
                startLocation: {address: '第一小学'},
                endLocation: {address: '家'},
                remark: '',
                packageType: 'single',
                amount: 58,
                services: [],
                status: 8, // 已取消
                createTime: new Date(Date.now() - 172800000).toISOString(),
                evaluated: false
            }
        ];

        wx.setStorageSync('orderList', mockOrders);
        console.log('Mock数据已初始化');
    },

    switchTab(e: any) {
        const index = e.currentTarget.dataset.index;

        // 直接更新tab值
        this.setData({currentTab: index});

        // 确保在setData完成后执行loadOrderList
        // 使用setTimeout确保setData已经同步更新了this.data
        setTimeout(() => {
            this.loadOrderList();
        }, 0);
    },

    // 加载订单列表
    loadOrderList(): Promise<void> {
        return new Promise((resolve) => {
            // 确保有mock数据（如果storage为空则初始化）
            this.ensureMockData();

            // 从本地存储读取订单数据
            const allOrders = wx.getStorageSync('orderList') || [];

            console.log('当前tab:', this.data.currentTab, '所有订单数量:', allOrders.length);

            // 根据tab筛选
            let filtered: any[] = [];
            if (this.data.currentTab === 0) {
                // 进行中：状态 0-6
                filtered = allOrders.filter((o: any) => o.status >= 0 && o.status <= 6);
            } else if (this.data.currentTab === 1) {
                // 已完成：状态 7
                filtered = allOrders.filter((o: any) => o.status === 7);
            } else if (this.data.currentTab === 2) {
                // 已取消：状态 8
                filtered = allOrders.filter((o: any) => o.status === 8);
            }

            // 按时间倒序排列
            filtered.sort((a: any, b: any) => {
                return new Date(b.createTime).getTime() - new Date(a.createTime).getTime();
            });

            const orderList = filtered.map((order: any) => ({
                ...order,
                statusText: getOrderStatusText(order.status),
                statusClass: getOrderStatusClass(order.status)
            }));

            console.log('筛选后的订单:', orderList);

            this.setData({orderList});
            resolve();
        });
    },

    // 确保有mock数据
    ensureMockData() {
        const existingOrders = wx.getStorageSync('orderList') || [];

        // 如果已有数据，直接返回
        if (existingOrders.length > 0) {
            return;
        }

        // 添加mock订单数据
        const mockOrders = [
            {
                id: 'mock_1',
                orderNo: 'ORD202504210001',
                type: 'pickup',
                child: {id: 1, name: '小明', school: '第一小学', grade: '一年级'},
                childName: '小明',
                date: '2024-04-21',
                time: '16:30',
                scheduleTime: '2024-04-21 16:30',
                startLocation: {address: '第一小学'},
                endLocation: {address: '家里'},
                remark: '',
                packageType: 'single',
                amount: 58,
                services: [],
                status: 2,
                createTime: new Date().toISOString(),
                evaluated: false
            },
            {
                id: 'mock_2',
                orderNo: 'ORD202504200001',
                type: 'dropoff',
                child: {id: 1, name: '小明', school: '第一小学', grade: '一年级'},
                childName: '小明',
                date: '2024-04-20',
                time: '08:00',
                scheduleTime: '2024-04-20 08:00',
                startLocation: {address: '家里'},
                endLocation: {address: '第一小学'},
                remark: '请准时',
                packageType: 'single',
                amount: 73,
                services: [{id: 1, name: '作业辅导', price: 15}],
                status: 7,
                createTime: new Date(Date.now() - 86400000).toISOString(),
                evaluated: true
            },
            {
                id: 'mock_3',
                orderNo: 'ORD202504190001',
                type: 'pickup',
                child: {id: 2, name: '小红', school: '第一小学', grade: '三年级'},
                childName: '小红',
                date: '2024-04-19',
                time: '16:30',
                scheduleTime: '2024-04-19 16:30',
                startLocation: {address: '第一小学'},
                endLocation: {address: '家'},
                remark: '',
                packageType: 'single',
                amount: 58,
                services: [],
                status: 8,
                createTime: new Date(Date.now() - 172800000).toISOString(),
                evaluated: false
            }
        ];

        wx.setStorageSync('orderList', mockOrders);
        console.log('Mock数据已初始化，共', mockOrders.length, '条订单');
    },

    // 取消订单
    async cancelOrder(e: any) {
        const id = e.currentTarget.dataset.id;

        const confirmed = await showConfirm('提示', '确定要取消该订单吗？');
        if (confirmed) {
            // 更新订单状态
            const allOrders = wx.getStorageSync('orderList') || [];
            const orderIndex = allOrders.findIndex((o: any) => o.id === id);

            if (orderIndex === -1) {
                console.error('未找到订单，ID:', id);
                wx.showToast({title: '操作失败', icon: 'none'});
            } else {
                allOrders[orderIndex].status = 8;
                allOrders[orderIndex].cancelTime = new Date().toISOString();
                wx.setStorageSync('orderList', allOrders);

                console.log('订单已取消，ID:', id, '更新后状态:', allOrders[orderIndex].status);

                // 显示成功提示
                wx.showToast({
                    title: '订单已取消',
                    icon: 'success',
                    duration: 1500
                });

                // 延迟切换到已取消tab，让用户看到取消成功的反馈
                setTimeout(() => {
                    // 先更新tab值
                    this.setData({currentTab: 2});
                    // 然后加载数据
                    setTimeout(() => {
                        this.loadOrderList();
                    }, 0);
                }, 1500);
            }
        }
    },

    // 支付订单
    payOrder(e: any) {
        const id = e.currentTarget.dataset.id;
        // 获取订单信息传递给支付页面
        const allOrders = wx.getStorageSync('orderList') || [];
        const order = allOrders.find((o: any) => o.id === id);

        if (order) {
            const url = `/pages/payment/payment?orderNo=${order.orderNo}&amount=${order.amount}&serviceType=${order.type === 'pickup' ?
                    '放学接' : '上学送'}&childName=${order.childName}&scheduleTime=${order.scheduleTime}`;
            wx.navigateTo({url});
        } else {
            wx.navigateTo({url: `/pages/payment/payment?orderId=${id}`});
        }
    },

    // 查看订单详情
    viewOrderDetail(e: any) {
        const id = e.currentTarget.dataset.id;
        wx.navigateTo({
            url: `/pages/order-detail/order-detail?id=${id}`
        });
    },

    // 实时追踪
    trackOrder(e: any) {
        const id = e.currentTarget.dataset.id;
        wx.navigateTo({
            url: `/pages/track/track?orderId=${id}`
        });
    },

    // 评价订单
    evaluateOrder(e: any) {
        const id = e.currentTarget.dataset.id;
        wx.showToast({title: '评价功能开发中', icon: 'none'});
        // wx.navigateTo({
        //   url: `/pages/evaluate/evaluate?orderId=${id}`
        // });
    },

    // 再次预约
    reorder(e: any) {
        const id = e.currentTarget.dataset.id;
        // 获取原订单数据
        const allOrders = wx.getStorageSync('orderList') || [];
        const originalOrder = allOrders.find((o: any) => o.id === id);

        if (originalOrder) {
            // 跳转到创建订单页面，并传递原订单数据
            wx.navigateTo({
                url: `/pages/order/create-order?reorderId=${id}`
            });
        }
    },

    // 创建新订单
    goToCreateOrder() {
        wx.navigateTo({
            url: '/pages/order/create-order'
        });
    }
});
