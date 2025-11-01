# 🔔 Hướng dẫn tích hợp Notification System

## 📖 Tổng quan

Hệ thống thông báo realtime sử dụng **WebSocket (STOMP)** để gửi thông báo ngay lập tức cho user khi:
- ✅ Có người **like** poster của bạn
- ✅ Có người **comment** vào poster của bạn  
- ✅ Có người **reply** comment của bạn

---

## 🎯 Backend Architecture

### 1. Notification Entity
```java
- id_notification: UUID (PK)
- recipient_id: UUID (người nhận)
- actor_id: UUID (người thực hiện hành động)
- notification_type: LIKE_POSTER | COMMENT_POSTER | REPLY_COMMENT
- reference_id: UUID (ID của poster/comment liên quan)
- message: TEXT (nội dung thông báo)
- is_read: BOOLEAN
- created_at: TIMESTAMP
- read_at: TIMESTAMP
```

### 2. WebSocket Endpoint
```
ws://localhost:8080/ws
```

### 3. Topic/Queue
- User subscribe: `/user/{userId}/queue/notifications`
- Khi có thông báo mới → server push tự động qua WebSocket

---

## 🚀 REST API Endpoints

### **Lấy tất cả thông báo**
```http
GET /notifications?userId={userId}
```

### **Lấy thông báo chưa đọc**
```http
GET /notifications/unread?userId={userId}
```

### **Đếm số thông báo chưa đọc**
```http
GET /notifications/unread/count?userId={userId}
```
**Response:**
```json
{
  "userId": "uuid",
  "unreadCount": 5,
  "message": "✅ Lấy số thông báo chưa đọc thành công"
}
```

### **Đánh dấu một thông báo là đã đọc**
```http
PUT /notifications/{notificationId}/read?userId={userId}
```

### **Đánh dấu tất cả là đã đọc**
```http
PUT /notifications/read-all?userId={userId}
```

### **Xóa thông báo**
```http
DELETE /notifications/{notificationId}?userId={userId}
```

---

## 💻 Frontend Integration (React/Next.js)

### 1. Cài đặt dependencies
```bash
npm install sockjs-client @stomp/stompjs
```

### 2. Tạo WebSocket Service

```javascript
// services/notificationService.js
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

class NotificationService {
  constructor() {
    this.stompClient = null;
    this.userId = null;
  }

  connect(userId, onNotificationReceived) {
    this.userId = userId;
    const socket = new SockJS('http://localhost:8080/ws');
    this.stompClient = Stomp.over(socket);

    this.stompClient.connect({}, (frame) => {
      console.log('✅ Connected to WebSocket:', frame);

      // Subscribe to user's notification queue
      this.stompClient.subscribe(
        `/user/${userId}/queue/notifications`,
        (message) => {
          const notification = JSON.parse(message.body);
          console.log('🔔 New notification:', notification);
          
          // Callback để xử lý thông báo mới
          if (onNotificationReceived) {
            onNotificationReceived(notification);
          }
        }
      );
    }, (error) => {
      console.error('❌ WebSocket connection error:', error);
    });
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.disconnect();
      console.log('🔌 Disconnected from WebSocket');
    }
  }
}

export default new NotificationService();
```

### 3. Sử dụng trong React Component

```jsx
// components/NotificationBell.jsx
import React, { useEffect, useState } from 'react';
import notificationService from '../services/notificationService';

const NotificationBell = ({ userId }) => {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    // Kết nối WebSocket
    notificationService.connect(userId, handleNewNotification);

    // Lấy thông báo chưa đọc từ API
    fetchUnreadCount();
    fetchNotifications();

    // Cleanup khi component unmount
    return () => {
      notificationService.disconnect();
    };
  }, [userId]);

  const handleNewNotification = (notification) => {
    // Thêm thông báo mới vào danh sách
    setNotifications(prev => [notification, ...prev]);
    setUnreadCount(prev => prev + 1);

    // Hiển thị toast notification
    showToast(notification.message);
  };

  const fetchUnreadCount = async () => {
    const response = await fetch(
      `http://localhost:8080/notifications/unread/count?userId=${userId}`
    );
    const data = await response.json();
    setUnreadCount(data.unreadCount);
  };

  const fetchNotifications = async () => {
    const response = await fetch(
      `http://localhost:8080/notifications?userId=${userId}`
    );
    const data = await response.json();
    setNotifications(data);
  };

  const markAsRead = async (notificationId) => {
    await fetch(
      `http://localhost:8080/notifications/${notificationId}/read?userId=${userId}`,
      { method: 'PUT' }
    );
    
    // Cập nhật UI
    setNotifications(prev =>
      prev.map(n => n.idNotification === notificationId 
        ? { ...n, isRead: true } 
        : n
      )
    );
    setUnreadCount(prev => Math.max(0, prev - 1));
  };

  const markAllAsRead = async () => {
    await fetch(
      `http://localhost:8080/notifications/read-all?userId=${userId}`,
      { method: 'PUT' }
    );
    
    setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
    setUnreadCount(0);
  };

  const showToast = (message) => {
    // Implement your toast notification here
    alert(`🔔 ${message}`);
  };

  return (
    <div className="notification-bell">
      <button className="bell-icon">
        🔔
        {unreadCount > 0 && (
          <span className="badge">{unreadCount}</span>
        )}
      </button>

      <div className="notification-dropdown">
        <div className="header">
          <h3>Thông báo</h3>
          <button onClick={markAllAsRead}>
            Đánh dấu tất cả đã đọc
          </button>
        </div>

        <div className="notification-list">
          {notifications.map(notification => (
            <div 
              key={notification.idNotification}
              className={`notification-item ${!notification.isRead ? 'unread' : ''}`}
              onClick={() => markAsRead(notification.idNotification)}
            >
              <p>{notification.message}</p>
              <small>{new Date(notification.createdAt).toLocaleString()}</small>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default NotificationBell;
```

---

## 📱 Example Usage

```jsx
// App.jsx hoặc Layout.jsx
import NotificationBell from './components/NotificationBell';

function App() {
  const currentUserId = "123e4567-e89b-12d3-a456-426614174000"; // Lấy từ auth

  return (
    <div>
      <header>
        <NotificationBell userId={currentUserId} />
      </header>
      {/* Rest of your app */}
    </div>
  );
}
```

---

## 🎨 CSS Example

```css
.notification-bell {
  position: relative;
}

.bell-icon {
  position: relative;
  font-size: 24px;
  background: none;
  border: none;
  cursor: pointer;
}

.badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: red;
  color: white;
  border-radius: 50%;
  padding: 2px 6px;
  font-size: 12px;
}

.notification-item.unread {
  background-color: #e3f2fd;
  font-weight: bold;
}
```

---

## ✅ Testing

### Test WebSocket Connection
1. Mở browser console
2. Kết nối WebSocket
3. Like/Comment/Reply từ một tài khoản khác
4. Xem thông báo realtime xuất hiện

### Test REST API
```bash
# Lấy số thông báo chưa đọc
curl http://localhost:8080/notifications/unread/count?userId={userId}

# Lấy tất cả thông báo
curl http://localhost:8080/notifications?userId={userId}
```

---

## 🔥 Features

✅ **Realtime notification** qua WebSocket  
✅ **Không spam**: Tránh tạo thông báo trùng lặp  
✅ **Không tự thông báo**: Không gửi thông báo cho chính mình  
✅ **Đánh dấu đã đọc**: Theo dõi trạng thái đọc/chưa đọc  
✅ **Xóa thông báo**: User có thể xóa thông báo của mình  
✅ **Authorization**: Chỉ người nhận mới có quyền xem/sửa/xóa  

---

## 📝 Notes

- WebSocket endpoint: `ws://localhost:8080/ws`
- Cần authentication/authorization cho production
- Có thể thêm pagination cho danh sách thông báo
- Có thể thêm filter theo loại thông báo
- Có thể thêm chức năng tắt/bật thông báo cho từng loại

Chúc bạn triển khai thành công! 🚀
