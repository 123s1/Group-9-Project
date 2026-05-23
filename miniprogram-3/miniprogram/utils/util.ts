// 工具函数

// 格式化时间
export const formatTime = (date: Date): string => {
    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const hour = date.getHours();
    const minute = date.getMinutes();
    const second = date.getSeconds();

    return `${[year, month, day].map(formatNumber).join('/')} ${[hour, minute, second].map(formatNumber).join(':')}`;
};

const formatNumber = (n: number): string => {
    return n.toString().padStart(2, '0');
};

// 格式化金额
export const formatMoney = (amount: number): string => {
    return `¥${amount.toFixed(2)}`;
};

// 手机号脱敏
export const maskPhone = (phone: string): string => {
    if (!phone) {
        return '';
    }
    return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
};

// 获取订单状态文本
export const getOrderStatusText = (status: number): string => {
    const statusMap: Record<number, string> = {
        0: '待派单',
        1: '已派单',
        2: '接送员已出发',
        3: '已到达学校',
        4: '已接到孩子',
        5: '途中',
        6: '已送达',
        7: '已完成',
        8: '已取消'
    };
    return statusMap[status] || '未知状态';
};

// 获取订单状态样式
export const getOrderStatusClass = (status: number): string => {
    if (status === 7) {
        return 'status-success';
    }
    if (status === 8) {
        return 'status-danger';
    }
    if (status >= 2 && status <= 6) {
        return 'status-info';
    }
    return 'status-warning';
};

// 显示确认弹窗
export const showConfirm = (title: string, content: string): Promise<boolean> => {
    return new Promise((resolve) => {
        wx.showModal({
            title,
            content,
            success(res) {
                resolve(res.confirm);
            }
        });
    });
};

// 显示加载提示
export const showLoading = (title: string = '加载中...') => {
    wx.showLoading({title, mask: true});
};

// 隐藏加载提示
export const hideLoading = () => {
    wx.hideLoading();
};

// 显示提示
export const showToast = (title: string, icon: 'success' | 'error' | 'none' = 'none') => {
    wx.showToast({title, icon});
};

// 防抖函数
export const debounce = <T extends (...args: any[]) => any>(fn: T, delay: number = 500): T => {
    let timer: any = null;
    return ((...args: any[]) => {
        if (timer) {
            clearTimeout(timer);
        }
        timer = setTimeout(() => fn(...args), delay);
    }) as T;
};

// 节流函数
export const throttle = <T extends (...args: any[]) => any>(fn: T, delay: number = 500): T => {
    let lastTime = 0;
    return ((...args: any[]) => {
        const now = Date.now();
        if (now - lastTime >= delay) {
            fn(...args);
            lastTime = now;
        }
    }) as T;
};
