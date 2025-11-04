# 🐛 Debug Guide - 400 Bad Request Error

## 📊 Lỗi hiện tại
```
POST http://localhost:8080/api/posters 400 (Bad Request)
```

## 🔍 Các nguyên nhân thường gặp

### 1️⃣ **Request Body thiếu field bắt buộc**

Backend yêu cầu:
```json
{
  "idUser": "uuid-string",           // ✅ BẮT BUỘC
  "content": "Nội dung poster",      // ✅ BẮT BUỘC
  "privacyStatusName": "PUBLIC",     // ✅ BẮT BUỘC (PUBLIC/FRIENDS/PRIVATE)
  "imageUrls": [],                   // ⚪ Optional
  "videoUrls": []                    // ⚪ Optional
}
```

**Fix Frontend:**
```typescript
// posterApi.ts
export const createPoster = async (data: CreatePosterData) => {
  console.log('📤 Sending create poster request:', data); // Debug log
  
  const response = await axios.post('/api/posters', {
    idUser: data.userId,              // ✅ Đảm bảo có field này
    content: data.content,            // ✅ Không được empty
    privacyStatusName: data.privacy || 'PUBLIC', // ✅ Default value
    imageUrls: data.images || [],
    videoUrls: data.videos || []
  });
  
  return response.data;
};
```

---

### 2️⃣ **UUID format không đúng**

UUID phải đúng format: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`

**Kiểm tra:**
```typescript
// CreatePoster.tsx
const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  
  console.log('Current User ID:', currentUserId); // Debug
  
  // Validate UUID format
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
  if (!uuidRegex.test(currentUserId)) {
    alert('❌ User ID không hợp lệ!');
    return;
  }
  
  try {
    await createPoster({
      userId: currentUserId,
      content: content,
      privacy: privacy,
      images: images,
      videos: videos
    });
  } catch (error) {
    console.error('❌ Error:', error.response?.data); // Log response detail
  }
};
```

---

### 3️⃣ **Privacy Status không tồn tại trong database**

Kiểm tra `privacy_status_poster` table có các giá trị:
- `PUBLIC`
- `FRIENDS`
- `PRIVATE`

**SQL Query:**
```sql
SELECT * FROM privacy_status_poster;
```

Nếu không có, chạy:
```sql
INSERT INTO privacy_status_poster (id_privacy_status, name) VALUES
  (gen_random_uuid(), 'PUBLIC'),
  (gen_random_uuid(), 'FRIENDS'),
  (gen_random_uuid(), 'PRIVATE');
```

---

### 4️⃣ **Content rỗng**

```typescript
// CreatePoster.tsx
const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  
  if (!content || content.trim() === '') {
    alert('❌ Nội dung không được để trống!');
    return;
  }
  
  // ... rest of code
};
```

---

### 5️⃣ **CORS Issue**

Nếu backend chặn CORS, thêm config:

```java
// SecurityConfiguration.java hoặc WebConfig.java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

---

## 🔧 Debug Steps

### **Step 1: Check Backend Logs**

Sau khi đã thêm logging, check console backend:

```
📥 Received poster creation request: {...}
👤 User ID: xxx-xxx-xxx
📝 Creating poster with data: {...}
✅ User found: john_doe
✅ Privacy status found: PUBLIC
✅ Poster created with ID: xxx-xxx-xxx
📷 Processing 2 images
🎉 Poster created successfully!
```

Nếu có lỗi, sẽ thấy:
```
❌ Content is empty
❌ Privacy status not found: PUBLC  (typo)
❌ User not found: invalid-uuid
```

---

### **Step 2: Check Network Tab (Browser DevTools)**

**Request Headers:**
```
POST /api/posters HTTP/1.1
Content-Type: application/json
```

**Request Payload:**
```json
{
  "idUser": "123e4567-e89b-12d3-a456-426614174000",
  "content": "Test post",
  "privacyStatusName": "PUBLIC"
}
```

**Response (nếu lỗi):**
```json
{
  "message": "❌ Nội dung poster không được để trống"
}
```

---

### **Step 3: Test với Postman/cURL**

```bash
curl -X POST http://localhost:8080/api/posters \
  -H "Content-Type: application/json" \
  -d '{
    "idUser": "123e4567-e89b-12d3-a456-426614174000",
    "content": "Test post",
    "privacyStatusName": "PUBLIC"
  }'
```

---

## ✅ Complete Working Example

### **Frontend (CreatePoster.tsx)**
```typescript
import React, { useState } from 'react';
import { createPoster } from './api/posterApi';

const CreatePoster = () => {
  const [content, setContent] = useState('');
  const [privacy, setPrivacy] = useState<'PUBLIC' | 'FRIENDS' | 'PRIVATE'>('PUBLIC');
  const [images, setImages] = useState<string[]>([]);
  
  const currentUserId = localStorage.getItem('userId'); // Get from auth

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validation
    if (!currentUserId) {
      alert('❌ Bạn cần đăng nhập!');
      return;
    }
    
    if (!content.trim()) {
      alert('❌ Nội dung không được để trống!');
      return;
    }
    
    try {
      console.log('📤 Creating poster...', {
        userId: currentUserId,
        content,
        privacy,
        imagesCount: images.length
      });
      
      const response = await createPoster({
        userId: currentUserId,
        content: content.trim(),
        privacy: privacy,
        images: images,
        videos: []
      });
      
      console.log('✅ Poster created:', response);
      alert('✅ Đăng poster thành công!');
      
      // Reset form
      setContent('');
      setImages([]);
      
    } catch (error: any) {
      console.error('❌ Error:', error);
      
      if (error.response) {
        // Server trả về error
        alert(`❌ ${error.response.data}`);
      } else if (error.request) {
        // Request gửi đi nhưng không nhận được response
        alert('❌ Không thể kết nối đến server!');
      } else {
        // Lỗi khác
        alert(`❌ Lỗi: ${error.message}`);
      }
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <textarea
        value={content}
        onChange={(e) => setContent(e.target.value)}
        placeholder="Bạn đang nghĩ gì?"
        required
      />
      
      <select value={privacy} onChange={(e) => setPrivacy(e.target.value as any)}>
        <option value="PUBLIC">🌍 Public</option>
        <option value="FRIENDS">👥 Friends</option>
        <option value="PRIVATE">🔒 Private</option>
      </select>
      
      <button type="submit">Đăng</button>
    </form>
  );
};

export default CreatePoster;
```

### **API Client (posterApi.ts)**
```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json'
  }
});

export interface CreatePosterData {
  userId: string;
  content: string;
  privacy: 'PUBLIC' | 'FRIENDS' | 'PRIVATE';
  images?: string[];
  videos?: string[];
}

export const createPoster = async (data: CreatePosterData) => {
  console.log('📤 API Call - Create Poster:', data);
  
  const payload = {
    idUser: data.userId,
    content: data.content,
    privacyStatusName: data.privacy,
    imageUrls: data.images || [],
    videoUrls: data.videos || []
  };
  
  console.log('📦 Request Payload:', payload);
  
  try {
    const response = await api.post('/api/posters', payload);
    console.log('✅ Response:', response.data);
    return response.data;
  } catch (error: any) {
    console.error('❌ API Error:', {
      status: error.response?.status,
      data: error.response?.data,
      message: error.message
    });
    throw error;
  }
};
```

---

## 🎯 Checklist

- [ ] Request body có đầy đủ 3 fields: `idUser`, `content`, `privacyStatusName`
- [ ] UUID format đúng chuẩn
- [ ] `privacyStatusName` là một trong: `PUBLIC`, `FRIENDS`, `PRIVATE`
- [ ] Content không empty
- [ ] Privacy status records tồn tại trong database
- [ ] CORS config đúng
- [ ] Backend logs có hiển thị debug messages
- [ ] Network tab shows correct request payload

---

Sau khi fix, test lại và check backend logs để xem lỗi cụ thể! 🚀
