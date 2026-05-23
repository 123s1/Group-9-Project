import {showConfirm} from '../../utils/util';

interface Child {
    id: string;
    name: string;
    gender: string;
    school: string;
    grade: string;
    birthday: string;
    interests: string[];
    remark: string;
    avatarColor: string;
    status: string;
    statusText: string;
}







interface Recommendation {
    id: string;
    type: string; // class, book, activity
    title: string;
    desc: string;
    matchTags: string[];
    location?: string;
    price?: string;
    author?: string;
    ageRange?: string;
    time?: string;
    matchScore?: number; // 添加匹配分数属性
}







const avatarColors = [
    '#FF9A56', '#4CAF50', '#2196F3', '#9C27B0',
    '#FF5722', '#00BCD4', '#E91E63', '#673AB7'
];

const availableInterests = [
    '恐龙', '绘画', '阅读', '运动', '音乐', '舞蹈',
    '科学', '拼图', '积木', '游泳', '足球', '篮球',
    '钢琴', '书法', '围棋', '象棋', '编程', '外语'
];

// Mock推荐数据
const mockRecommendations: Recommendation[] = [
    // 兴趣班
    {
        id: 'rec_1',
        type: 'class',
        title: '恐龙主题科学探索班',
        desc: '通过化石、模型、游戏等方式，带孩子走进恐龙的神秘世界，培养科学探索精神。',
        matchTags: ['恐龙', '科学'],
        location: '青少年活动中心',
        price: '¥1280/月'
    },
    {
        id: 'rec_2',
        type: 'class',
        title: '创意绘画启蒙课',
        desc: '专业老师引导，用画笔表达想象，接触水彩、油画、线描等多种绘画形式。',
        matchTags: ['绘画'],
        location: '艺术培训中心',
        price: '¥980/月'
    },
    {
        id: 'rec_3',
        type: 'class',
        title: '儿童绘本阅读班',
        desc: '精选优质绘本，培养阅读兴趣，提高语言表达和理解能力。',
        matchTags: ['阅读'],
        location: '图书馆绘本馆',
        price: '¥680/月'
    },
    {
        id: 'rec_4',
        type: 'class',
        title: '感统运动训练营',
        desc: '专业感统训练器材，提升孩子平衡感、协调性和运动能力。',
        matchTags: ['运动', '游泳'],
        location: '儿童运动馆',
        price: '¥1580/月'
    },
    // 绘本
    {
        id: 'rec_5',
        type: 'book',
        title: '《恐龙大百科》',
        desc: '600余种恐龙的详细介绍，配有精美插图，是恐龙迷必备的百科全书。',
        matchTags: ['恐龙'],
        author: '张明 主编',
        ageRange: '4-10岁'
    },
    {
        id: 'rec_6',
        type: 'book',
        title: '《小王子》绘本版',
        desc: '经典名著改编，配合精美插画，适合亲子共读，启迪心灵。',
        matchTags: ['阅读'],
        author: '圣埃克苏佩里',
        ageRange: '3-8岁'
    },
    {
        id: 'rec_7',
        type: 'book',
        title: '《我的情绪小怪兽》',
        desc: '用颜色管理情绪，帮助孩子认识和管理自己的情绪，适合亲子共读。',
        matchTags: ['阅读'],
        author: '安娜·耶纳斯',
        ageRange: '3-6岁'
    },
    // 周末活动
    {
        id: 'rec_8',
        type: 'activity',
        title: '恐龙博物馆半日游',
        desc: '专业讲解员带队，参观真实恐龙化石，参与化石挖掘体验活动。',
        matchTags: ['恐龙'],
        time: '周六/周日 9:00-12:00',
        price: '¥198/人'
    },
    {
        id: 'rec_9',
        type: 'activity',
        title: '户外写生活动',
        desc: '专业美术老师带队，在公园进行户外写生，学习观察与表达。',
        matchTags: ['绘画', '运动'],
        time: '周六 14:00-17:00',
        price: '¥168/人'
    },
    {
        id: 'rec_10',
        type: 'activity',
        title: '亲子绘本故事会',
        desc: '专业绘本老师讲读，开展创意手工活动，培养阅读兴趣。',
        matchTags: ['阅读', '绘画'],
        time: '周日 10:00-11:30',
        price: '免费'
    }
];

Page({
    data: {
        children: [] as Child[],
        currentChild: null as Child | null,
        currentChildId: '',
        recommendations: mockRecommendations,
        filteredRecommendations: [] as Recommendation[],
        selectedFilter: 'all',
        showAddModal: false,
        showInterestModal: false,
        showChildPickerModal: false,
        isEditing: false,
        editingChildId: '',
        formData: {
            name: '',
            gender: 'male',
            school: '',
            grade: '',
            birthday: '',
            interests: [] as string[],
            remark: ''
        },
        availableInterests,
        selectedInterests: [] as string[]
    },

    onLoad() {
        this.loadChildren();
    },

    onShow() {
        this.loadChildren();
    },

    // 加载孩子列表
    loadChildren() {
        const children = wx.getStorageSync('childrenList') || [];

        // 如果没有数据，初始化一个示例
        if (children.length === 0) {
            const sampleChildren: Child[] = [
                {
                    id: 'child_1',
                    name: '小明',
                    gender: 'male',
                    school: '第一小学',
                    grade: '一年级(2)班',
                    birthday: '2018-03-15',
                    interests: ['恐龙', '绘画', '科学'],
                    remark: '',
                    avatarColor: avatarColors[0],
                    status: 'active',
                    statusText: '在读'
                }
            ];
            wx.setStorageSync('childrenList', sampleChildren);
            children.push(...sampleChildren);
        }

        this.setData({children}, () => {
            if (children.length > 0) {
                this.selectChildById(children[0].id);
            }
        });
    },

    // 根据ID选择孩子
    selectChildById(id: string) {
        const child = this.data.children.find(c => c.id === id);
        if (child) {
            this.setData({
                currentChild: child,
                currentChildId: id
            }, () => {
                this.filterRecommendations();
            });
        }
    },

    // 筛选推荐
    filterRecommendations() {
        const {currentChild, recommendations, selectedFilter} = this.data;

        if (!currentChild) {
            this.setData({filteredRecommendations: []});
            return;
        }

        let filtered = recommendations;

        // 按类型筛选
        if (selectedFilter !== 'all') {
            const typeMap: Record<string, string> = {
                'class': 'class',
                'book': 'book',
                'activity': 'activity'
            };
            filtered = filtered.filter(r => r.type === typeMap[selectedFilter]);
        }

        // 按兴趣标签筛选，优先显示匹配度高的
        const childInterests = currentChild.interests;
        filtered = filtered.map(r => {
            const matchCount = r.matchTags.filter(tag => childInterests.includes(tag)).length;
            return {...r, matchScore: matchCount};
        });

        // 按匹配度排序
        filtered.sort((a: any, b: any) => b.matchScore - a.matchScore);

        // 移除matchScore字段
        filtered = filtered.map(({matchScore, ...rest}) => rest);

        this.setData({filteredRecommendations: filtered});
    },

    // 显示添加弹窗
    showAddModal() {
        this.setData({
            showAddModal: true,
            isEditing: false,
            formData: {
                name: '',
                gender: 'male',
                school: '',
                grade: '',
                birthday: '',
                interests: [],
                remark: ''
            }
        });
    },

    // 隐藏添加弹窗
    hideAddModal() {
        this.setData({showAddModal: false});
    },

    // 编辑孩子
    editChild(e: any) {
        const id = e.currentTarget.dataset.id;
        const child = this.data.children.find(c => c.id === id);

        if (child) {
            this.setData({
                showAddModal: true,
                isEditing: true,
                editingChildId: id,
                formData: {...child}
            });
        }
    },

    // 删除孩子
    async deleteChild(e: any) {
        const id = e.currentTarget.dataset.id;

        const confirmed = await showConfirm('确认删除', '确定要删除该孩子的信息吗？此操作不可恢复。');
        if (confirmed) {
            const children = this.data.children.filter(c => c.id !== id);
            wx.setStorageSync('childrenList', children);

            this.setData({children}, () => {
                if (this.data.currentChildId === id) {
                    if (children.length > 0) {
                        this.selectChildById(children[0].id);
                    } else {
                        this.setData({
                            currentChild: null,
                            filteredRecommendations: []
                        });
                    }
                }
            });

            wx.showToast({title: '已删除', icon: 'success'});
        }
    },

    // 保存孩子
    saveChild() {
        const {formData, isEditing, editingChildId} = this.data;

        // 验证
        if (!formData.name.trim()) {
            wx.showToast({title: '请输入孩子姓名', icon: 'none'});
            return;
        }
        if (!formData.school.trim()) {
            wx.showToast({title: '请输入学校名称', icon: 'none'});
            return;
        }

        let children = [...this.data.children];

        if (isEditing) {
            // 更新
            const index = children.findIndex(c => c.id === editingChildId);
            if (index !== -1) {
                children[index] = {...children[index], ...formData};
            }
        } else {
            // 新增
            const newChild: Child = {
                id: 'child_' + Date.now(),
                ...formData,
                avatarColor: avatarColors[children.length % avatarColors.length],
                status: 'active',
                statusText: '在读'
            };
            children.push(newChild);
        }

        wx.setStorageSync('childrenList', children);

        this.setData({
            children,
            showAddModal: false
        }, () => {
            // noinspection NegatedIfStatementJS
            if (!isEditing) {
                this.selectChildById(children[children.length - 1].id);
            } else if (this.data.currentChildId) {
                this.selectChildById(this.data.currentChildId);
            }
        });

        wx.showToast({
            title: isEditing ? '已保存' : '添加成功',
            icon: 'success'
        });
    },

    // 查看孩子详情
    viewChildDetail(e: any) {
        const id = e.currentTarget.dataset.id;
        // 可以跳转到详情页，这里简单处理
        this.selectChildById(id);
    },

    // 编辑兴趣标签弹窗
    editInterests(e: any) {
        const id = e.currentTarget.dataset.id;
        const child = this.data.children.find(c => c.id === id);

        if (child) {
            this.setData({
                showInterestModal: true,
                editingChildId: id,
                selectedInterests: [...child.interests]
            });
        }
    },

    // 隐藏兴趣编辑弹窗
    hideInterestModal() {
        this.setData({showInterestModal: false});
    },

    // 切换兴趣标签（添加弹窗）
    toggleInterest(e: any) {
        const interest = e.currentTarget.dataset.interest;
        const {formData} = this.data;
        const interests = formData.interests.includes(interest)
                ? formData.interests.filter(i => i !== interest)
                : [...formData.interests, interest];

        this.setData({'formData.interests': interests});
    },

    // 切换兴趣标签（编辑弹窗）
    toggleInterestEdit(e: any) {
        const interest = e.currentTarget.dataset.interest;
        const {selectedInterests} = this.data;
        const interests = selectedInterests.includes(interest)
                ? selectedInterests.filter(i => i !== interest)
                : [...selectedInterests, interest];

        this.setData({selectedInterests: interests});
    },

    // 保存兴趣标签
    saveInterests() {
        const {editingChildId, selectedInterests} = this.data;
        let children = [...this.data.children];

        const index = children.findIndex(c => c.id === editingChildId);
        if (index !== -1) {
            children[index].interests = selectedInterests;
            wx.setStorageSync('childrenList', children);

            this.setData({
                children,
                showInterestModal: false
            }, () => {
                if (this.data.currentChildId === editingChildId) {
                    this.selectChildById(editingChildId);
                }
            });

            wx.showToast({title: '已保存', icon: 'success'});
        }
    },

    // 去安心日记
    goToSafeDiary(e: any) {
        const id = e.currentTarget.dataset.id;
        wx.navigateTo({
            url: `/pages/safe-diary/safe-diary?childId=${id}`
        });
    },

    // 去个性推荐
    goToRecommendations(e: any) {
        const id = e.currentTarget.dataset.id;
        this.selectChildById(id);
        // 滚动到推荐区域
        wx.pageScrollTo({scrollTop: 400, duration: 300});
    },

    // 显示孩子选择器
    showChildPicker() {
        this.setData({showChildPickerModal: true});
    },

    // 隐藏孩子选择器
    hideChildPicker() {
        this.setData({showChildPickerModal: false});
    },

    // 选择孩子
    selectChild(e: any) {
        const id = e.currentTarget.dataset.id;
        this.selectChildById(id);
        this.setData({showChildPickerModal: false});
    },

    // 筛选推荐
    filterRecs(e: any) {
        const type = e.currentTarget.dataset.type;
        this.setData({selectedFilter: type}, () => {
            this.filterRecommendations();
        });
    },

    // 阻止事件冒泡
    preventBubble() {
    },

    // 表单输入处理
    onNameInput(e: any) {
        this.setData({'formData.name': e.detail.value});
    },

    onSchoolInput(e: any) {
        this.setData({'formData.school': e.detail.value});
    },

    onGradeInput(e: any) {
        this.setData({'formData.grade': e.detail.value});
    },

    onBirthdayChange(e: any) {
        this.setData({'formData.birthday': e.detail.value});
    },

    onRemarkInput(e: any) {
        this.setData({'formData.remark': e.detail.value});
    },

    selectGender(e: any) {
        this.setData({'formData.gender': e.currentTarget.dataset.gender});
    }
});
