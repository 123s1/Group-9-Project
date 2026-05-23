// 请求封装
import {showLoading, hideLoading} from './util';

const BASE_URL = 'http://localhost:8900'; // 替换为实际的后端API地址

interface RequestOptions {
    url: string;
    method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
    data?: any;
    header?: any;
    showLoading?: boolean;
}







const request = (options: RequestOptions): Promise<any> => {
    return new Promise((resolve, reject) => {
        if (options.showLoading !== false) {
            showLoading();
        }

        const token = wx.getStorageSync('token');

        wx.request({
            url: BASE_URL + options.url,
            method: options.method || 'GET',
            data: options.data || {},
            header: {
                'Content-Type': 'application/json',
                'Authorization': token ? `Bearer ${token}` : '',
                ...options.header
            },
            success(res: any) {
                hideLoading();
                if (res.data.code === 200) {
                    resolve(res.data);
                } else if (res.data.code === 401) {
                    // token过期，跳转登录
                    wx.removeStorageSync('token');
                    wx.removeStorageSync('userInfo');
                    wx.showToast({
                        title: '请重新登录',
                        icon: 'none',
                        success() {
                            setTimeout(() => {
                                wx.reLaunch({url: '/pages/login/login'});
                            }, 1500);
                        }
                    });
                    reject(res.data);
                } else {
                    wx.showToast({
                        title: res.data.message || '请求失败',
                        icon: 'none'
                    });
                    reject(res.data);
                }
            },
            fail(err) {
                hideLoading();
                wx.showToast({
                    title: '网络异常，请稍后重试',
                    icon: 'none'
                });
                reject(err);
            }
        });
    });
};

export default request;
