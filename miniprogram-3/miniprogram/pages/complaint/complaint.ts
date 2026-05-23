interface ComplaintType {
    value: string;
    name: string;
    icon: string;
}







interface Order {
    id: string;
    orderNo: string;
    type: string;
    typeText: string;
    childName: string;
    date: string;
    status: number;
}







interface HistoryItem {
    id: string;
    ticketNo: string;
    type: string;
    typeText: string;
    description: string;
    status: number;
    statusText: string;
    statusClass: string;
    createTime: string;
    images: string[];
    response?: string;
}







Page({
    data: {
        // 投诉类型
        complaintTypes: [
            {value: 'service', name: '服务态度', icon: '😠'},
            {value: 'quality', name: '服务质量', icon: '⭐'},
            {value: 'delay', name: '延误问题', icon: '⏰'},
            {value: 'safety', name: '安全隐患', icon: '⚠️'},
            {value: 'fee', name: '费用问题', icon: '💰'},
            {value: 'other', name: '其他问题', icon: '📝'}
        ] as ComplaintType[],

        // 表单数据
        complaintType: '',
        relatedOrder: null as Order | null,
        description: '',
        images: [] as string[],
        phone: '',

        // 提交状态
        isSubmitting: false,

        // 订单选择弹窗
        showOrderModal: false,
        orderOptions: [] as Order[],
        selectedOrderId: '',

        // 历史投诉
        historyList: [] as HistoryItem[]
    },

    onLoad() {
        // 加载历史投诉
        this.loadHistory();
        // 获取用户手机号
        this.loadUserPhone();
    },

    onShow() {
        // 每次显示时刷新历史记录
        this.loadHistory();
    },

    // 加载用户手机号
    loadUserPhone() {
        const userInfo = wx.getStorageSync('userInfo');
        if (userInfo && userInfo.phone) {
            this.setData({phone: userInfo.phone});
        }
    },

    // 选择投诉类型
    selectType(e: any) {
        const value = e.currentTarget.dataset.value;
        this.setData({complaintType: value});
    },

    // 选择关联订单
    selectOrder() {
        // 获取可关联的订单列表（已完成或已取消的订单）
        const allOrders = wx.getStorageSync('orderList') || [];
        const orderOptions = allOrders
                .filter((o: any) => o.status === 7 || o.status === 8)
                .map((o: any) => ({
                    id: o.id,
                    orderNo: o.orderNo,
                    type: o.type,
                    typeText: o.type === 'pickup' ? '放学接' : '上学送',
                    childName: o.childName,
                    date: o.scheduleTime.split(' ')[0],
                    status: o.status
                })) as Order[];

        this.setData({
            showOrderModal: true,
            orderOptions,
            selectedOrderId: this.data.relatedOrder?.id || ''
        });
    },

    // 确认选择订单
    confirmOrder(e: any) {
        const id = e.currentTarget.dataset.id;
        const order = this.data.orderOptions.find((o: any) => o.id === id);

        if (order) {
            this.setData({
                relatedOrder: order,
                showOrderModal: false,
                selectedOrderId: id
            });
        }
    },

    // 清除关联订单
    clearOrder() {
        this.setData({
            relatedOrder: null,
            showOrderModal: false,
            selectedOrderId: ''
        });
    },

    // 关闭订单弹窗
    closeOrderModal() {
        this.setData({showOrderModal: false});
    },

    // 阻止事件冒泡
    preventBubble() {
    },

    // 描述输入
    onDescriptionInput(e: any) {
        this.setData({description: e.detail.value});
    },

    // 手机号输入
    onPhoneInput(e: any) {
        this.setData({phone: e.detail.value});
    },

    // 选择图片
    chooseImage() {
        if (this.data.images.length >= 3) {
            wx.showToast({title: '最多上传3张照片', icon: 'none'});
            return;
        }

        wx.chooseMedia({
            count: 3 - this.data.images.length,
            mediaType: ['image'],
            sourceType: ['album', 'camera'],
            success: (res) => {
                const newImages = res.tempFiles.map((f: any) => f.tempFilePath);
                this.setData({
                    images: [...this.data.images, ...newImages]
                });
            },
            fail: () => {
                wx.showToast({title: '选择图片失败', icon: 'none'});
            }
        });
    },

    // 删除图片
    deleteImage(e: any) {
        const index = e.currentTarget.dataset.index;
        const images = [...this.data.images];
        images.splice(index, 1);
        this.setData({images});
    },

    // 提交投诉
    submitComplaint() {
        // 表单验证
        if (!this.data.complaintType) {
            wx.showToast({title: '请选择投诉类型', icon: 'none'});
            return;
        }

        if (this.data.description.length < 10) {
            wx.showToast({title: '请详细描述问题（至少10个字）', icon: 'none'});
            return;
        }

        if (!this.data.phone) {
            wx.showToast({title: '请输入联系电话', icon: 'none'});
            return;
        }

        if (!/^1[3-9]\d{9}$/.test(this.data.phone)) {
            wx.showToast({title: '请输入正确的手机号', icon: 'none'});
            return;
        }

        if (this.data.isSubmitting) {
            return;
        }

        this.setData({isSubmitting: true});

        // 生成工单号
        const ticketNo = 'TK' + new Date().getFullYear() +
                String(Date.now()).slice(-8) +
                Math.random().toString(36).substring(2, 6).toUpperCase();

        const complaintTypeObj = this.data.complaintTypes.find(
                t => t.value === this.data.complaintType
        );

        // 创建投诉记录
        const complaint: HistoryItem = {
            id: 'cmpl_' + Date.now(),
            ticketNo,
            type: this.data.complaintType,
            typeText: complaintTypeObj?.name || '',
            description: this.data.description,
            status: 0, // 待处理
            statusText: '待处理',
            statusClass: 'pending',
            createTime: this.formatTime(new Date()),
            images: this.data.images
        };

        // 保存到历史记录
        const historyList = wx.getStorageSync('complaintHistory') || [];
        historyList.unshift(complaint);
        wx.setStorageSync('complaintHistory', historyList);

        // 模拟提交到服务器
        setTimeout(() => {
            this.setData({isSubmitting: false});

            // 显示成功弹窗
            wx.showModal({
                title: '提交成功',
                content: `您的投诉已提交，工单号：${ticketNo}\n\n客服将在24小时内处理您的反馈，请保持电话畅通。`,
                showCancel: false,
                confirmText: '知道了',
                success: () => {
                    // 重置表单
                    this.setData({
                        complaintType: '',
                        relatedOrder: null,
                        description: '',
                        images: [],
                        historyList: wx.getStorageSync('complaintHistory') || []
                    });
                }
            });
        }, 1000);
    },

    // 加载历史投诉
    loadHistory() {
        const historyList = wx.getStorageSync('complaintHistory') || [];

        // 处理状态显示
        const processedList = historyList.map((item: HistoryItem) => {
            let statusText = '待处理';
            let statusClass = 'pending';

            if (item.status === 1) {
                statusText = '处理中';
                statusClass = 'processing';
            } else if (item.status === 2) {
                statusText = '已解决';
                statusClass = 'resolved';
            } else if (item.status === 3) {
                statusText = '已关闭';
                statusClass = 'closed';
            }

            return {
                ...item,
                statusText,
                statusClass
            };
        });

        this.setData({historyList: processedList});
    },

    // 查看详情
    viewDetail(e: any) {
        const id = e.currentTarget.dataset.id;
        const complaint = this.data.historyList.find((c: any) => c.id === id);

        if (complaint) {
            let content = `工单号：${complaint.ticketNo}\n`;
            content += `类型：${complaint.typeText}\n`;
            content += `描述：${complaint.description}\n`;
            content += `状态：${complaint.statusText}\n`;
            content += `时间：${complaint.createTime}\n`;

            if (complaint.response) {
                content += `\n客服回复：\n${complaint.response}`;
            }

            wx.showModal({
                title: '投诉详情',
                content,
                showCancel: false,
                confirmText: '关闭'
            });
        }
    },

    // 格式化时间
    formatTime(date: Date): string {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hour = String(date.getHours()).padStart(2, '0');
        const minute = String(date.getMinutes()).padStart(2, '0');
        return `${year}-${month}-${day} ${hour}:${minute}`;
    }
});
