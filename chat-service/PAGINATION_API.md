# API Pagination cho Tin Nhắn

## Tổng quan
Đã thêm chức năng pagination cho tin nhắn chat (1-1) và group chat để tối ưu hiệu suất khi tải tin nhắn.

## API Endpoints

### 1. Chat 1-1 (Conversation)

#### 1.1. Lấy 10 tin nhắn mới nhất (trang đầu tiên)
```
GET /chats/{conversationId}/messages/paginated?page=0&size=10
```

**Parameters:**
- `conversationId` (path): UUID của cuộc hội thoại
- `page` (query, optional): Số trang (mặc định: 0)
- `size` (query, optional): Số lượng tin nhắn mỗi trang (mặc định: 10)

**Response:** Danh sách 10 tin nhắn mới nhất, được sắp xếp từ cũ đến mới

**Ví dụ:**
```bash
GET /chats/123e4567-e89b-12d3-a456-426614174000/messages/paginated?page=0&size=10
```

#### 1.2. Load thêm tin nhắn khi scroll lên (theo timestamp)
```
GET /chats/{conversationId}/messages/before?timestamp={timestamp}&size=10
```

**Parameters:**
- `conversationId` (path): UUID của cuộc hội thoại
- `timestamp` (query, required): Thời điểm (ISO 8601 format: `yyyy-MM-dd'T'HH:mm:ss`)
- `size` (query, optional): Số lượng tin nhắn (mặc định: 10)

**Response:** Danh sách tin nhắn cũ hơn timestamp, được sắp xếp từ cũ đến mới

**Ví dụ:**
```bash
GET /chats/123e4567-e89b-12d3-a456-426614174000/messages/before?timestamp=2024-11-07T10:30:00&size=10
```

### 2. Group Chat

#### 2.1. Lấy 10 tin nhắn nhóm mới nhất (trang đầu tiên)
```
GET /groups/{groupId}/messages/paginated?page=0&size=10
```

**Parameters:**
- `groupId` (path): UUID của nhóm chat
- `page` (query, optional): Số trang (mặc định: 0)
- `size` (query, optional): Số lượng tin nhắn mỗi trang (mặc định: 10)

**Response:** Danh sách 10 tin nhắn mới nhất trong nhóm

**Ví dụ:**
```bash
GET /groups/123e4567-e89b-12d3-a456-426614174001/messages/paginated?page=0&size=10
```

#### 2.2. Load thêm tin nhắn nhóm khi scroll lên (theo timestamp)
```
GET /groups/{groupId}/messages/before?timestamp={timestamp}&size=10
```

**Parameters:**
- `groupId` (path): UUID của nhóm chat
- `timestamp` (query, required): Thời điểm (ISO 8601 format: `yyyy-MM-dd'T'HH:mm:ss`)
- `size` (query, optional): Số lượng tin nhắn (mặc định: 10)

**Response:** Danh sách tin nhắn nhóm cũ hơn timestamp

**Ví dụ:**
```bash
GET /groups/123e4567-e89b-12d3-a456-426614174001/messages/before?timestamp=2024-11-07T10:30:00&size=10
```

## Cách sử dụng trên Frontend

### Kịch bản 1: Load tin nhắn lần đầu
```javascript
// Lấy 10 tin nhắn mới nhất
const response = await fetch(
  `/chats/${conversationId}/messages/paginated?page=0&size=10`
);
const messages = await response.json();
// Hiển thị messages trên UI (đã được sắp xếp từ cũ đến mới)
```

### Kịch bản 2: Load thêm tin nhắn khi scroll lên
```javascript
// Giả sử tin nhắn cũ nhất hiện tại có createdAt = "2024-11-07T10:30:00"
const oldestMessage = messages[0];
const timestamp = oldestMessage.createdAt; // "2024-11-07T10:30:00"

// Lấy 10 tin nhắn cũ hơn
const response = await fetch(
  `/chats/${conversationId}/messages/before?timestamp=${timestamp}&size=10`
);
const olderMessages = await response.json();

// Thêm olderMessages vào đầu danh sách hiện tại
messages = [...olderMessages, ...messages];
```

### Ví dụ React Component
```javascript
import React, { useState, useEffect, useRef } from 'react';

function ChatComponent({ conversationId }) {
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const chatContainerRef = useRef(null);

  // Load tin nhắn ban đầu
  useEffect(() => {
    loadInitialMessages();
  }, [conversationId]);

  const loadInitialMessages = async () => {
    setLoading(true);
    try {
      const response = await fetch(
        `/chats/${conversationId}/messages/paginated?page=0&size=10`
      );
      const data = await response.json();
      setMessages(data);
    } catch (error) {
      console.error('Error loading messages:', error);
    } finally {
      setLoading(false);
    }
  };

  // Load thêm tin nhắn khi scroll lên
  const loadMoreMessages = async () => {
    if (loading || messages.length === 0) return;
    
    setLoading(true);
    try {
      const oldestMessage = messages[0];
      const timestamp = oldestMessage.createdAt;
      
      const response = await fetch(
        `/chats/${conversationId}/messages/before?timestamp=${timestamp}&size=10`
      );
      const olderMessages = await response.json();
      
      if (olderMessages.length > 0) {
        setMessages([...olderMessages, ...messages]);
      }
    } catch (error) {
      console.error('Error loading more messages:', error);
    } finally {
      setLoading(false);
    }
  };

  // Xử lý sự kiện scroll
  const handleScroll = () => {
    const container = chatContainerRef.current;
    if (container.scrollTop === 0 && !loading) {
      loadMoreMessages();
    }
  };

  return (
    <div 
      ref={chatContainerRef}
      onScroll={handleScroll}
      style={{ height: '500px', overflowY: 'scroll' }}
    >
      {loading && <div>Loading...</div>}
      {messages.map(msg => (
        <div key={msg.id}>
          <strong>{msg.senderId}:</strong> {msg.content}
          <small>{new Date(msg.createdAt).toLocaleString()}</small>
        </div>
      ))}
    </div>
  );
}
```

## Thay đổi trong Database

Đã thêm các query methods mới trong `MessageRepository`:

1. `findByConversationOrderByCreatedAtDesc` - Lấy tin nhắn theo thứ tự mới nhất trước (với Pageable)
2. `findByConversationBeforeTimestamp` - Lấy tin nhắn cũ hơn một thời điểm cụ thể
3. `findByGroupConversationOrderByCreatedAtDesc` - Tương tự cho group chat
4. `findByGroupConversationBeforeTimestamp` - Tương tự cho group chat

## Lưu ý

1. **Timestamp format**: Phải sử dụng ISO 8601 format (`yyyy-MM-dd'T'HH:mm:ss`)
2. **Sắp xếp**: Kết quả được sắp xếp từ cũ đến mới để dễ hiển thị trên UI
3. **Performance**: Nên giữ size ở khoảng 10-20 tin nhắn để tối ưu hiệu suất
4. **API cũ**: Endpoint `/chats/{conversationId}/messages` vẫn hoạt động bình thường (lấy tất cả tin nhắn)

## Testing

```bash
# Test lấy 10 tin nhắn mới nhất
curl -X GET "http://localhost:8080/chats/{conversationId}/messages/paginated?page=0&size=10"

# Test load thêm tin nhắn
curl -X GET "http://localhost:8080/chats/{conversationId}/messages/before?timestamp=2024-11-07T10:30:00&size=10"

# Test group chat
curl -X GET "http://localhost:8080/groups/{groupId}/messages/paginated?page=0&size=10"
curl -X GET "http://localhost:8080/groups/{groupId}/messages/before?timestamp=2024-11-07T10:30:00&size=10"
```
