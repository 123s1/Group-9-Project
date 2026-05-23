import {showLoading, hideLoading} from './util';

// 通过构建变量或配置文件注入，开发环境默认本地地址
// 部署时替换为实际后端地址，例如 https://api.viakid.com/api/v1
const BASE_URL = 'http://localhost:8900/api/v1';

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
                if (res.data.code === 0) {
                    resolve(res.data);
                } else if (res.data.code === 401 || res.statusCode === 401) {
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
