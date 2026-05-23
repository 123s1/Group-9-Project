import {getOrderStatusText, getOrderStatusClass, showConfirm} from '../../utils/util';
import {orderApi} from '../../utils/api';

// 订单状态映射：后端字符串 -> 前端数字
const STATUS_MAP: Record<string, number> = {
    'pending': 0, 'assigned': 1, 'departed': 2, 'arrived': 3,
    'picked_up': 4, 'delivered': 5, 'completed': 7, 'cancelled': 8
};

Page({
    data: {
        currentTab: 0,
        orderList: [] as any[],
        tabNames: ['进行中', '已完成', '已取消']
    },

    onLoad(options: any) {
        if (options.tab !== undefined) {
            const tab = parseInt(options.tab);
            if (tab >= 0 && tab <= 2) {
                this.setData({currentTab: tab});
            }
        }
        this.loadOrderList();
    },

    onShow() {
        this.loadOrderList();
    },

    onPullDownRefresh() {
        this.loadOrderList().finally(() => {
            wx.stopPullDownRefresh();
        });
    },

    switchTab(e: any) {
        const index = e.currentTarget.dataset.index;
        this.setData({currentTab: index});
        setTimeout(() => {
            this.loadOrderList();
        }, 0);
    },

    loadOrderList(): Promise<void> {
        const statusFilter = this.data.currentTab === 0 ? 'in_progress'
            : this.data.currentTab === 1 ? 'completed' : 'cancelled';

        return orderApi.getOrders({status: statusFilter, page: 1, size: 50})
            .then((res: any) => {
                const items = (res.data?.items || []).map((order: any) => {
                    const numStatus = STATUS_MAP[order.status] ?? 0;
                    return {
                        ...order,
                        status: numStatus,
                        childName: order.children?.[0]?.name || '',
                        startLocation: order.pickupLocation,
                        endLocation: order.dropOffLocation,
                        statusText: getOrderStatusText(numStatus),
                        statusClass: getOrderStatusClass(numStatus)
                    };
                });
                this.setData({orderList: items});
            })
            .catch(() => {
                this.setData({orderList: []});
            });
    },

    // 取消订单
    async cancelOrder(e: any) {
        const id = e.currentTarget.dataset.id;

        const confirmed = await showConfirm('提示', '确定要取消该订单吗？');
        if (confirmed) {
            orderApi.cancelOrder(id, '用户主动取消')
                .then(() => {
                    wx.showToast({title: '订单已取消', icon: 'success', duration: 1500});
                    setTimeout(() => {
                        this.setData({currentTab: 2});
                        setTimeout(() => this.loadOrderList(), 0);
                    }, 1500);
                })
                .catch(() => {
                    wx.showToast({title: '取消失败', icon: 'none'});
                });
        }
    },

    // 支付订单
    payOrder(e: any) {
        const id = e.currentTarget.dataset.id;
        const order = this.data.orderList.find((o: any) => o.id === id);
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
