Page({
    data: {
        orderType: 'pickup', // pickup: 放学接, dropoff: 上学送
        children: [
            {id: 1, name: '小明', school: '第一小学', grade: '一年级'},
            {id: 2, name: '小红', school: '第一小学', grade: '三年级'}
        ],
        selectedChild: {} as any,
        today: '',
        orderDate: '',
        orderTime: '',
        startLocation: {address: ''},
        endLocation: {address: ''},
        remark: '',
        packageType: 'single',
        services: [
            {id: 1, name: '作业辅导', desc: '接送员协助检查作业', price: 15, selected: false},
            {id: 2, name: '营养加餐', desc: '水果+牛奶', price: 8, selected: false},
            {id: 3, name: '临时托管', desc: '延长至20:00', price: 30, selected: false}
        ],
        basePrice: 58,
        servicesPrice: 0,
        totalPrice: 58,
        reorderId: null // 重新预约的订单ID
    },

    onLoad(options: any) {
        // 设置今天日期
        const today = new Date();
        const year = today.getFullYear();
        const month = String(today.getMonth() + 1).padStart(2, '0');
        const day = String(today.getDate()).padStart(2, '0');
        this.setData({
            today: `${year}-${month}-${day}`,
            orderDate: `${year}-${month}-${day}`,
            orderTime: this.data.orderType === 'pickup' ? '16:30' : '08:00'
        });

        // 默认选择第一个孩子
        if (this.data.children.length > 0) {
            this.setData({selectedChild: this.data.children[0]});
        }

        // 如果是重新预约，加载原订单数据
        if (options && options.reorderId) {
            this.setData({reorderId: options.reorderId});
            this.loadOrderData(options.reorderId);
        }
    },

    // 加载原订单数据（重新预约时使用）
    loadOrderData(orderId: string) {
        const allOrders = wx.getStorageSync('orderList') || [];
        const order = allOrders.find((o: any) => o.id === orderId);
        if (order) {
            this.setData({
                orderType: order.type,
                selectedChild: order.child,
                orderDate: order.date,
                orderTime: order.time,
                startLocation: order.startLocation,
                endLocation: order.endLocation,
                remark: order.remark || '',
                packageType: order.packageType || 'single',
                basePrice: order.amount || 58,
                totalPrice: order.amount || 58
            });
        }
    },

    // 选择接送类型
    selectType(e: any) {
        const type = e.currentTarget.dataset.type;
        this.setData({orderType: type});
        // 重置地点
        this.setData({
            startLocation: {address: ''},
            endLocation: {address: ''}
        });
    },

    // 选择孩子
    selectChild(e: any) {
        const index = e.detail.value;
        this.setData({
            selectedChild: this.data.children[index]
        });
    },

    // 选择日期
    selectDate(e: any) {
        this.setData({orderDate: e.detail.value});
    },

    // 选择时间
    selectTime(e: any) {
        this.setData({orderTime: e.detail.value});
    },

    // 选择起点位置
    selectStartLocation() {
        wx.chooseLocation({
            success: (res) => {
                this.setData({
                    startLocation: {address: res.address || res.name}
                });
            }
        });
    },

    // 选择终点位置
    selectEndLocation() {
        wx.chooseLocation({
            success: (res) => {
                this.setData({
                    endLocation: {address: res.address || res.name}
                });
            }
        });
    },

    // 切换增值服务
    toggleService(e: any) {
        const id = e.currentTarget.dataset.id;
        const services = this.data.services.map((service: any) => {
            if (service.id === id) {
                service.selected = !service.selected;
            }
            return service;
        });

        // 计算增值服务费用
        let servicesPrice = 0;
        services.forEach((service: any) => {
            if (service.selected) {
                servicesPrice += service.price;
            }
        });

        const totalPrice = this.data.basePrice + servicesPrice;

        this.setData({services, servicesPrice, totalPrice});
    },

    // 输入备注
    inputRemark(e: any) {
        this.setData({remark: e.detail.value});
    },

    // 选择套餐
    selectPackage(e: any) {
        const type = e.currentTarget.dataset.type;
        let basePrice = 58;
        if (type === 'week') {
            basePrice = 258;
        }
        if (type === 'month') {
            basePrice = 980;
        }

        this.setData({
            packageType: type,
            basePrice,
            totalPrice: basePrice + this.data.servicesPrice
        });
    },

    // 提交订单
    submitOrder() {
        // 验证必填项
        if (!this.data.selectedChild.id) {
            wx.showToast({title: '请选择孩子', icon: 'none'});
            return;
        }
        if (!this.data.startLocation.address) {
            wx.showToast({title: '请选择起点', icon: 'none'});
            return;
        }
        if (!this.data.endLocation.address) {
            wx.showToast({title: '请选择终点', icon: 'none'});
            return;
        }

        // 生成订单ID
        const orderId = Date.now().toString();

        // 构建订单数据
        const orderData = {
            id: orderId,
            orderNo: 'ORD' + orderId,
            type: this.data.orderType,
            child: this.data.selectedChild,
            childName: this.data.selectedChild.name,
            date: this.data.orderDate,
            time: this.data.orderTime,
            scheduleTime: `${this.data.orderDate} ${this.data.orderTime}`,
            startLocation: this.data.startLocation,
            endLocation: this.data.endLocation,
            remark: this.data.remark,
            packageType: this.data.packageType,
            amount: this.data.totalPrice,
            services: this.data.services.filter((s: any) => s.selected),
            status: 0, // 待派单
            createTime: new Date().toISOString(),
            evaluated: false
        };

        // 保存到本地存储
        const existingOrders = wx.getStorageSync('orderList') || [];
        existingOrders.unshift(orderData);
        wx.setStorageSync('orderList', existingOrders);

        console.log('订单已保存:', orderData);

        wx.showToast({
            title: '订单提交成功',
            icon: 'success',
            success: () => {
                setTimeout(() => {
                    wx.switchTab({url: '/pages/order/order'});
                }, 1500);
            }
        });
    },

    // 返回上一页
    goBack() {
        wx.navigateBack();
    }
});
