import request from './request';

// ==================== 用户相关 API ====================
export const userApi = {
    getUserInfo: () => request({url: '/user/info', method: 'GET'}),
    realNameAuth: (data: any) => request({url: '/user/auth', method: 'POST', data}),
    setEmergencyContact: (data: any) => request({url: '/user/emergency-contact', method: 'POST', data}),
    getEmergencyContacts: () => request({url: '/user/emergency-contacts', method: 'GET'}),
    getChildren: () => request({url: '/user/children', method: 'GET'}),
    addChild: (data: any) => request({url: '/user/child', method: 'POST', data}),
    updateChild: (id: string, data: any) => request({url: `/user/child/${id}`, method: 'PUT', data}),
    deleteChild: (id: string) => request({url: `/user/child/${id}`, method: 'DELETE'}),
    updateNickname: (data: any) => request({url: '/user/nickname', method: 'PUT', data}),
    updateAvatar: (data: any) => request({url: '/user/avatar', method: 'PUT', data})
};

// ==================== 订单相关 API ====================
// 路径已对齐后端 /api/v1/orders 路由
export const orderApi = {
    createOrder: (data: any) => request({url: '/orders', method: 'POST', data}),
    getOrders: (params: any) => request({url: '/orders', method: 'GET', data: params}),
    getOrderDetail: (id: string) => request({url: `/orders/${id}`, method: 'GET'}),
    cancelOrder: (id: string, reason: string) => request({url: `/orders/${id}/cancel`, method: 'POST', data: {reason}}),
    modifyOrder: (id: string, data: any) => request({url: `/orders/${id}`, method: 'PUT', data}),
    evaluateOrder: (id: string, data: any) => request({url: `/orders/${id}/evaluate`, method: 'POST', data}),
    emergencyOrder: (data: any) => request({url: '/orders/emergency', method: 'POST', data})
};

// ==================== 追踪相关 API ====================
export const trackApi = {
    getRealTimeLocation: (orderId: string) => request({url: `/track/location/${orderId}`, method: 'GET'}),
    getTrackHistory: (orderId: string) => request({url: `/track/history/${orderId}`, method: 'GET'}),
    getPhotos: (orderId: string) => request({url: `/track/photos/${orderId}`, method: 'GET'}),
    setGeofence: (data: any) => request({url: '/track/geofence', method: 'POST', data}),
    getEscortInfo: (orderId: string) => request({url: `/track/escort/${orderId}`, method: 'GET'})
};

// ==================== 支付相关 API ====================
export const paymentApi = {
    getPackages: () => request({url: '/payment/packages', method: 'GET'}),
    createPayment: (data: any) => request({url: '/payment/create', method: 'POST', data}),
    getMembership: () => request({url: '/payment/membership', method: 'GET'}),
    getWallet: () => request({url: '/payment/wallet', method: 'GET'}),
    recharge: (amount: number) => request({url: '/payment/recharge', method: 'POST', data: {amount}}),
    getBills: (params: any) => request({url: '/payment/bills', method: 'GET', data: params})
};

// ==================== 增值服务相关 API ====================
export const valueAddedApi = {
    getRecommendations: () => request({url: '/value-added/recommend', method: 'GET'}),
    getAnxinDiary: (params: any) => request({url: '/value-added/diary', method: 'GET', data: params}),
    getLiveInfo: () => request({url: '/value-added/live', method: 'GET'}),
    getServices: () => request({url: '/value-added/services', method: 'GET'})
};

// ==================== 售后相关 API ====================
export const afterSaleApi = {
    submitComplaint: (data: any) => request({url: '/after-sale/complaint', method: 'POST', data}),
    applyAfterSale: (data: any) => request({url: '/after-sale/apply', method: 'POST', data}),
    getAfterSaleRecords: () => request({url: '/after-sale/records', method: 'GET'})
};

// ==================== 紧急求助 API ====================
export const emergencyApi = {
    emergencyHelp: (data: any) => request({url: '/emergency/help', method: 'POST', data}),
    getEmergencyContacts: () => request({url: '/emergency/contacts', method: 'GET'})
};
