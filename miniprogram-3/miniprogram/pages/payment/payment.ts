// 支付页面逻辑
import {showLoading, hideLoading} from '../../utils/util';

interface PaymentPageOption {
    orderNo: string;
    amount: string;
    serviceType: string;
    childName: string;
    scheduleTime: string;
}







Page({
    data: {
        orderNo: 'AX202504210001',
        amount: '28.00',
        serviceType: '放学接',
        childName: '王小明',
        scheduleTime: '2024-04-21 16:30',
        selectedPayment: 'wechat',
        qrcodeUrl: '/images/payment-qrcode.png', // 请替换为实际的二维码图片路径
        isLoading: false
    },

    onLoad(options: Record<string, string | undefined>) {
        // 从页面参数获取订单信息
        if (options.orderNo) {
            this.setData({orderNo: options.orderNo});
        }
        if (options.amount) {
            this.setData({amount: options.amount});
        }
        if (options.serviceType) {
            this.setData({serviceType: options.serviceType});
        }
        if (options.childName) {
            this.setData({childName: options.childName});
        }
        if (options.scheduleTime) {
            this.setData({scheduleTime: options.scheduleTime});
        }

        // 生成支付二维码
        this.generateQrcode();
    },

    // 选择支付方式
    selectPayment(e: any) {
        const paymentType = e.currentTarget.dataset.type;
        this.setData({selectedPayment: paymentType});
    },

    // 生成支付二维码
    generateQrcode() {
        // 实际项目中，这里应该调用后端接口获取二维码
        // const that = this;
        // wx.request({
        //   url: 'api/generate-payment-qrcode',
        //   data: { orderNo: this.data.orderNo },
        //   success(res) {
        //     that.setData({ qrcodeUrl: res.data.qrcodeUrl });
        //   }
        // });

        // 模拟生成二维码
        console.log('生成支付二维码，订单号：', this.data.orderNo);
    },

    // 预览二维码
    previewQrcode() {
        if (!this.data.qrcodeUrl) {
            wx.showToast({title: '二维码加载中', icon: 'loading'});
            return;
        }

        wx.previewImage({
            urls: [this.data.qrcodeUrl],
            current: this.data.qrcodeUrl
        });
    },

    // 确认支付
    confirmPayment() {
        if (this.data.isLoading) {
            return;
        }

        this.setData({isLoading: true});

        showLoading('正在唤起支付...');

        // 实际项目中，这里应该调用微信支付统一下单接口
        // const that = this;
        // wx.requestPayment({
        //   timeStamp: '',
        //   nonceStr: '',
        //   package: '',
        //   signType: 'MD5',
        //   paySign: '',
        //   success(res) {
        //     wx.hideLoading();
        //     that.paymentSuccess();
        //   },
        //   fail(err) {
        //     wx.hideLoading();
        //     that.setData({ isLoading: false });
        //     if (err.errMsg !== 'requestPayment:fail cancel') {
        //       wx.showToast({ title: '支付失败', icon: 'none' });
        //     }
        //   }
        // });

        // 模拟支付流程
        setTimeout(() => {
            hideLoading();
            this.paymentSuccess();
        }, 1500);
    },

    // 支付成功
    paymentSuccess() {
        wx.showModal({
            title: '支付成功',
            content: '您的订单已支付成功，我们将尽快为您安排接送服务',
            showCancel: false,
            confirmText: '返回首页',
            success(res) {
                if (res.confirm) {
                    wx.switchTab({
                        url: '/pages/index/index'
                    });
                }
            }
        });
    }
});
