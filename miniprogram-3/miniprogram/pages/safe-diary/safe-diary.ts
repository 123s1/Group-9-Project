// 安心日记页面逻辑
interface DiaryItem {
    id: number;
    title: string;
    videoUrl: string;
    coverUrl: string;
    duration: string;
    type: 'pickup' | 'dropoff' | 'emergency';
    typeName: string;
    typeClass: string;
    childName: string;
    date: string;
    time: string;
    description: string;
    escortName: string;
    escortAvatar: string;
    likeCount: number;
    commentCount: number;
}







Page({
    data: {
        diaryList: [] as DiaryItem[],
        isLoading: true,
        showVideoPlayer: false,
        currentVideo: {
            title: '',
            videoUrl: '',
            childName: '',
            date: '',
            time: ''
        } as Partial<DiaryItem>,
        danmuList: [
            {text: '安全送达', color: '#FF9A56', time: 1},
            {text: '辛苦老师啦', color: '#FF9A56', time: 3},
            {text: '宝贝真棒', color: '#FF9A56', time: 5}
        ]
    },

    onLoad() {
        this.loadDiaryList();
    },

    onShow() {
        // 每次显示页面时刷新数据
        // this.loadDiaryList();
    },

    onPullDownRefresh() {
        this.loadDiaryList();
        setTimeout(() => {
            wx.stopPullDownRefresh();
        }, 1000);
    },

    // 加载日记列表
    loadDiaryList() {
        this.setData({isLoading: true});

        // 模拟数据 - 实际项目中应该调用接口获取
        const mockData: DiaryItem[] = [
            {
                id: 1,
                title: '放学安全接回',
                videoUrl: 'https://example.com/video1.mp4', // 请替换为实际的视频URL
                coverUrl: '/images/video-cover-1.png',
                duration: '01:32',
                type: 'pickup',
                typeName: '放学接',
                typeClass: 'pickup',
                childName: '王小明',
                date: '2024-04-21',
                time: '16:35',
                description: '今天的放学接送顺利完成，宝贝在接送员的陪护下安全到家，感谢老师的细心照顾！',
                escortName: '李老师',
                escortAvatar: '/images/avatar-teacher.png',
                likeCount: 24,
                commentCount: 8
            },
            {
                id: 2,
                title: '上学送校服务',
                videoUrl: 'https://example.com/video2.mp4', // 请替换为实际的视频URL
                coverUrl: '/images/video-cover-2.png',
                duration: '02:15',
                type: 'dropoff',
                typeName: '上学送',
                typeClass: 'dropoff',
                childName: '王小明',
                date: '2024-04-21',
                time: '07:25',
                description: '早晨的上学送服务顺利完成，宝贝开心地进入了校园，开启美好的一天！',
                escortName: '李老师',
                escortAvatar: '/images/avatar-teacher.png',
                likeCount: 18,
                commentCount: 5
            },
            {
                id: 3,
                title: '昨日安心日记',
                videoUrl: 'https://example.com/video3.mp4',
                coverUrl: '/images/video-cover-3.png',
                duration: '01:48',
                type: 'pickup',
                typeName: '放学接',
                typeClass: 'pickup',
                childName: '王小明',
                date: '2024-04-20',
                time: '16:30',
                description: '昨天的放学接送顺利完成，宝贝状态很好，期待今天的表现！',
                escortName: '王老师',
                escortAvatar: '/images/avatar-teacher.png',
                likeCount: 32,
                commentCount: 12
            }
        ];

        // 实际项目中，应该调用接口获取数据
        // const that = this;
        // wx.request({
        //   url: 'api/diary/list',
        //   success(res) {
        //     that.setData({
        //       diaryList: res.data.list,
        //       isLoading: false
        //     });
        //   },
        //   fail() {
        //     that.setData({ isLoading: false });
        //     wx.showToast({ title: '加载失败', icon: 'none' });
        //   }
        // });

        setTimeout(() => {
            this.setData({
                diaryList: mockData,
                isLoading: false
            });
        }, 800);
    },

    // 播放视频
    playVideo(e: any) {
        const index = e.currentTarget.dataset.index;
        const video = this.data.diaryList[index];

        this.setData({
            showVideoPlayer: true,
            currentVideo: video
        });

        // 动态设置视频组件
        setTimeout(() => {
            const videoContext = wx.createVideoContext('videoPlayer');
            videoContext.play();
        }, 100);
    },

    // 关闭视频
    closeVideo() {
        const videoContext = wx.createVideoContext('videoPlayer');
        videoContext.pause();

        this.setData({
            showVideoPlayer: false,
            currentVideo: {}
        });
    },

    // 视频播放结束
    onVideoEnded() {
        wx.showToast({
            title: '播放完成',
            icon: 'success',
            duration: 1500
        });
    },

    // 视频播放错误
    onVideoError(e: any) {
        console.error('视频播放错误:', e.detail);
        wx.showModal({
            title: '播放失败',
            content: '视频加载失败，请检查网络后重试',
            showCancel: false
        });
    },

    // 封面图片加载失败
    onCoverError(e: any) {
        const index = e.currentTarget.dataset.index;
        const diaryList = this.data.diaryList;
        // 使用默认背景
        diaryList[index].coverUrl = '';
        this.setData({diaryList});
    },

    // 刷新日记
    refreshDiary() {
        this.loadDiaryList();
    },

    // 阻止触摸移动
    preventTouchMove() {
        return false;
    },

    onShareAppMessage() {
        return {
            title: '安心日记 - 记录宝贝的每一次接送',
            path: '/pages/safe-diary/safe-diary'
        };
    }
});
