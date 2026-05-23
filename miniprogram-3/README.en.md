# AnXin JieSong - Parent Edition

A WeChat Mini Program for parents to schedule child pickup/dropoff services.

## About

The Parent Edition WeChat Mini Program of the AnXin JieSong platform, providing convenient child transportation booking services including order creation, real-time tracking, safety diaries, and child profile management.

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | TypeScript (strict mode) |
| Styling | Less |
| Rendering | Skyline render engine |
| SDK | WeChat Mini Program SDK 2.32.3 |
| Backend | Custom RESTful API (non-cloud) |

## Quick Start

### Prerequisites

- WeChat Developer Tools
- AppID: `wxa44f177469039748`

### Run the Project

1. Open WeChat Developer Tools
2. Import project root `miniprogram-3`
3. Enter the AppID
4. Click Compile & Preview

### Backend Configuration

Update `BASE_URL` in `miniprogram/utils/request.ts`:

```typescript
const BASE_URL = 'https://your-api-server.com'; // Replace with actual URL
```

## Core Features

### Login

- Phone number + SMS code login
- Phone number + password login
- WeChat一键登录 (wx.login)

### Home Page

- Current active order card (with driver info)
- Quick actions: Book now, Urgent order, Order history, Child management
- Membership card (Silver/Gold/Diamond)
- Recommended value-added services: Homework help, Meal supplements, Temporary care, Weekend programs
- Recent safety diary entries
- Emergency assistance (hotline + 110)

### Order Management

- **Create Order**: Select child, service type (pickup/dropoff), time, location, add-on services, package (single/weekly/monthly), real-time price calculation
- **Order List**: Active/Completed/Cancelled tabs with pull-to-refresh
- **Payment**: WeChat Pay (interface ready)

### Order Status

```
Pending → Assigned → Driver Departed → Arrived at School → Child Picked Up → En Route → Delivered → Completed
                                ↕
                             Cancelled
```

### Safety Diary

- Video diary list (cover, title, tags, driver info)
- Video player with bullet comments
- Share to friends

### Child Management

- Child profile CRUD (name, gender, school, grade, birthday, interest tags)
- Personalized recommendation system: Interest classes, books, weekend activities based on tags

### Profile

- User info display
- Membership level & spending progress
- Order statistics
- Wallet & coupons
- Settings, logout

### Complaints

- Complaint types: Attitude, Quality, Delay, Safety, Billing, etc.
- Link orders + image upload (up to 3)
- Complaint history tracking

## Project Structure

```
miniprogram/
├── app.ts                     # App entry
├── app.json                   # Global config
├── app.less                   # Global styles
├── components/
│   └── navigation-bar/        # Custom nav bar
├── pages/
│   ├── login/                 # Login
│   ├── index/                 # Home (TabBar)
│   ├── order/
│   │   ├── order/             # Orders (TabBar)
│   │   └── create-order/      # Create order
│   ├── profile/               # Profile (TabBar)
│   ├── payment/               # Payment
│   ├── safe-diary/            # Safety diary
│   ├── complaint/             # Complaints
│   └── children/              # Child management
└── utils/
    ├── request.ts             # HTTP request wrapper
    └── api.ts                 # 7 API modules
        ├── userApi
        ├── orderApi
        ├── trackApi
        ├── paymentApi
        ├── valueAddedApi
        ├── afterSaleApi
        └── emergencyApi
```

## Development Status

- ✅ Core page UI & interactions (login, home, orders, payment, profile, diary, complaints, children)
- ✅ API interface design & request architecture
- ⏳ Real backend API integration (currently using mock data)
- ⏳ Skeleton pages (real-time tracking, order detail, etc.) pending implementation
- ⏳ Reviews, membership center, wallet management pending completion

### API Integration

All API definitions are in `utils/api.ts`, covering 7 modules with 30+ endpoints. Currently using local storage mock data. Configure `BASE_URL` to connect to the actual backend.

## Related Projects

- [ViaKid-Server](https://gitee.com/viakid/ViaKid-Server) — Backend API service
- [ViaKid-Driver](https://gitee.com/viakid/ViaKid-Driver) — Driver Android app
- [viakid-admin](https://gitee.com/viakid/viakid-admin) — Web admin panel
