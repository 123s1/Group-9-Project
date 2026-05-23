// 请求封装
import request from './request';

// ==================== 用户相关 API ====================
export const userApi = {
    // 获取用户信息
    getUserInfo: () => request({url: '/user/info', method: 'GET'}),

    // 实名认证
    realNameAuth: (data: any) => request({url: '/user/auth', method: 'POST', data}),

    // 设置紧急联系人
    setEmergencyContact: (data: any) => request({url: '/user/emergency-contact', method: 'POST', data}),

    // 获取紧急联系人
    getEmergencyContacts: () => request({url: '/user/emergency-contacts', method: 'GET'}),

    // 获取孩子列表
    getChildren: () => request({url: '/user/children', method: 'GET'}),

    // 添加孩子
    addChild: (data: any) => request({url: '/user/child', method: 'POST', data}),

    // 更新孩子信息
    updateChild: (id: string, data: any) => request({url: `/user/child/${id}`, method: 'PUT', data}),

    // 删除孩子
    deleteChild: (id: string) => request({url: `/user/child/${id}`, method: 'DELETE'}),

    // 更新昵称
    updateNickname: (data: any) => request({url: '/user/nickname', method: 'PUT', data}),

    // 更新头像
    updateAvatar: (data: any) => request({url: '/user/avatar', method: 'PUT', data})
};

// ==================== 订单相关 API ====================
export const orderApi = {
    // 创建订单
    createOrder: (data: any) => request({url: '/order/create', method: 'POST', data}),

    // 获取订单列表
    getOrders: (params: any) => request({url: '/order/list', method: 'GET', data: params}),

    // 获取订单详情
    getOrderDetail: (id: string) => request({url: `/order/detail/${id}`, method: 'GET'}),

    // 取消订单
    cancelOrder: (id: string, reason: string) => request({url: `/order/cancel/${id}`, method: 'POST', data: {reason}}),

    // 修改订单
    modifyOrder: (id: string, data: any) => request({url: `/order/modify/${id}`, method: 'PUT', data}),

    // 评价订单
    evaluateOrder: (id: string, data: any) => request({url: `/order/evaluate/${id}`, method: 'POST', data}),

    // 紧急加单
    emergencyOrder: (data: any) => request({url: '/order/emergency', method: 'POST', data})
};

// ==================== 追踪相关 API ====================
export const trackApi = {
    // 获取订单实时位置
    getRealTimeLocation: (orderId: string) => request({url: `/track/location/${orderId}`, method: 'GET'}),

    // 获取轨迹回放数据
    getTrackHistory: (orderId: string) => request({url: `/track/history/${orderId}`, method: 'GET'}),

    // 获取途中照片
    getPhotos: (orderId: string) => request({url: `/track/photos/${orderId}`, method: 'GET'}),

    // 设置电子围栏
    setGeofence: (data: any) => request({url: '/track/geofence', method: 'POST', data}),

    // 获取接送员信息
    getEscortInfo: (orderId: string) => request({url: `/track/escort/${orderId}`, method: 'GET'})
};

// ==================== 支付相关 API ====================
export const paymentApi = {
    // 获取套餐列表
    getPackages: () => request({url: '/payment/packages', method: 'GET'}),

    // 创建支付订单
    createPayment: (data: any) => request({url: '/payment/create', method: 'POST', data}),

    // 获取会员信息
    getMembership: () => request({url: '/payment/membership', method: 'GET'}),

    // 获取钱包余额
    getWallet: () => request({url: '/payment/wallet', method: 'GET'}),

    // 充值
    recharge: (amount: number) => request({url: '/payment/recharge', method: 'POST', data: {amount}}),

    // 获取账单
    getBills: (params: any) => request({url: '/payment/bills', method: 'GET', data: params})
};

// ==================== 增值服务相关 API ====================
export const valueAddedApi = {
    // 获取推荐内容
    getRecommendations: () => request({url: '/value-added/recommend', method: 'GET'}),

    // 获取安心日记
    getAnxinDiary: (params: any) => request({url: '/value-added/diary', method: 'GET', data: params}),

    // 获取直播信息
    getLiveInfo: () => request({url: '/value-added/live', method: 'GET'}),

    // 获取增值服务列表
    getServices: () => request({url: '/value-added/services', method: 'GET'})
};

// ==================== 售后相关 API ====================
export const afterSaleApi = {
    // 提交投诉
    submitComplaint: (data: any) => request({url: '/after-sale/complaint', method: 'POST', data}),

    // 申请售后
    applyAfterSale: (data: any) => request({url: '/after-sale/apply', method: 'POST', data}),

    // 获取售后记录
    getAfterSaleRecords: () => request({url: '/after-sale/records', method: 'GET'})
};

// ==================== 紧急求助 API ====================
export const emergencyApi = {
    // 紧急求助
    emergencyHelp: (data: any) => request({url: '/emergency/help', method: 'POST', data}),

    // 获取紧急联系人
    getEmergencyContacts: () => request({url: '/emergency/contacts', method: 'GET'})
};
