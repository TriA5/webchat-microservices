# 🎬 Hướng dẫn đăng Poster với Ảnh và Video

## 🎯 Tổng quan

Hệ thống poster giờ hỗ trợ:
- ✅ **Ảnh** (images) - như trước
- ✅ **Video** (videos) - **MỚI!**
- ✅ Kết hợp cả ảnh và video trong cùng một poster

---

## 📊 Database Schema

### **Table: `video`**
```sql
CREATE TABLE video (
    id_video UUID PRIMARY KEY,
    url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    duration INTEGER,           -- Thời lượng (giây)
    file_size BIGINT,          -- Kích thước file (bytes)
    id_poster UUID NOT NULL,   -- FK to poster
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (id_poster) REFERENCES poster(id_poster)
);
```

---

## 🚀 API Usage

### 1️⃣ **Tạo poster với ảnh và video**

```http
POST /posters
Content-Type: application/json

{
  "content": "Check out my awesome post! 🎉",
  "privacyStatusName": "PUBLIC",
  "userId": "user-uuid",
  "imageUrls": [
    "data:image/jpeg;base64,/9j/4AAQSkZJRg...",  // Base64 image
    "https://example.com/image1.jpg"              // Hoặc URL có sẵn
  ],
  "videoUrls": [
    "data:video/mp4;base64,AAAAIGZ0eXBpc29t...",  // Base64 video
    "https://example.com/video1.mp4"              // Hoặc URL có sẵn
  ]
}
```

**Response:**
```json
"✅ Tạo poster thành công!"
```

---

### 2️⃣ **Tạo poster chỉ có video (không có ảnh)**

```http
POST /posters
Content-Type: application/json

{
  "content": "My video content 🎥",
  "privacyStatusName": "FRIENDS",
  "userId": "user-uuid",
  "videoUrls": [
    "data:video/mp4;base64,..."
  ]
}
```

---

### 3️⃣ **Lấy poster (trả về cả ảnh và video)**

```http
GET /posters/{posterId}
```

**Response:**
```json
{
  "idPoster": "poster-uuid",
  "content": "Check out my awesome post! 🎉",
  "idUser": "user-uuid",
  "userName": "john_doe",
  "userFirstName": "John",
  "userLastName": "Doe",
  "userAvatar": "https://...",
  "privacyStatusName": "PUBLIC",
  "imageUrls": [
    "https://storage.example.com/poster_uuid_0.jpg",
    "https://storage.example.com/poster_uuid_1.jpg"
  ],
  "videos": [
    {
      "url": "https://storage.example.com/poster_video_uuid_0.mp4",
      "thumbnailUrl": "https://storage.example.com/thumb_0.jpg",
      "duration": 120,
      "fileSize": 5242880
    }
  ],
  "createdAt": "2025-11-04T10:00:00",
  "updatedAt": "2025-11-04T10:00:00"
}
```

---

### 4️⃣ **Cập nhật poster (thêm/xóa ảnh hoặc video)**

```http
PUT /posters/{posterId}
Content-Type: application/json

{
  "content": "Updated content",
  "userId": "user-uuid",
  "imageUrls": [
    "https://storage.example.com/existing-image.jpg",  // Giữ ảnh cũ
    "data:image/jpeg;base64,..."                       // Thêm ảnh mới
  ],
  "videoUrls": [
    "https://storage.example.com/existing-video.mp4",  // Giữ video cũ
    "data:video/mp4;base64,..."                        // Thêm video mới
  ]
}
```

**Note:** 
- Ảnh/video **KHÔNG** có trong request sẽ bị xóa
- Ảnh/video có trong request sẽ được giữ lại
- Base64 mới sẽ được upload

---

### 5️⃣ **Xóa poster (tự động xóa cả ảnh và video)**

```http
DELETE /posters/{posterId}?userId={userId}
```

**Response:**
```json
"✅ Xóa poster thành công!"
```

---

## 🎨 Frontend Implementation (React/Next.js)

### **Upload Video với Preview**

```jsx
import React, { useState } from 'react';

const CreatePosterForm = () => {
  const [content, setContent] = useState('');
  const [images, setImages] = useState([]);
  const [videos, setVideos] = useState([]);

  const handleImageUpload = (e) => {
    const files = Array.from(e.target.files);
    
    files.forEach(file => {
      const reader = new FileReader();
      reader.onloadend = () => {
        setImages(prev => [...prev, reader.result]); // Base64
      };
      reader.readAsDataURL(file);
    });
  };

  const handleVideoUpload = (e) => {
    const files = Array.from(e.target.files);
    
    files.forEach(file => {
      // Kiểm tra kích thước video (ví dụ: max 100MB)
      if (file.size > 100 * 1024 * 1024) {
        alert('❌ Video quá lớn! Tối đa 100MB');
        return;
      }

      const reader = new FileReader();
      reader.onloadend = () => {
        setVideos(prev => [...prev, {
          data: reader.result, // Base64
          name: file.name,
          size: file.size,
          type: file.type
        }]);
      };
      reader.readAsDataURL(file);
    });
  };

  const handleSubmit = async () => {
    const response = await fetch('http://localhost:8080/posters', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        content,
        privacyStatusName: 'PUBLIC',
        userId: 'current-user-uuid',
        imageUrls: images,
        videoUrls: videos.map(v => v.data)
      })
    });

    if (response.ok) {
      alert('✅ Đăng poster thành công!');
      setContent('');
      setImages([]);
      setVideos([]);
    }
  };

  return (
    <div className="create-poster-form">
      <textarea
        value={content}
        onChange={(e) => setContent(e.target.value)}
        placeholder="Bạn đang nghĩ gì?"
      />

      <div className="upload-buttons">
        <label className="upload-btn">
          📷 Thêm ảnh
          <input
            type="file"
            accept="image/*"
            multiple
            onChange={handleImageUpload}
            style={{ display: 'none' }}
          />
        </label>

        <label className="upload-btn">
          🎬 Thêm video
          <input
            type="file"
            accept="video/*"
            multiple
            onChange={handleVideoUpload}
            style={{ display: 'none' }}
          />
        </label>
      </div>

      {/* Preview Images */}
      {images.length > 0 && (
        <div className="preview-images">
          {images.map((img, index) => (
            <div key={index} className="preview-item">
              <img src={img} alt={`Preview ${index}`} />
              <button onClick={() => setImages(images.filter((_, i) => i !== index))}>
                ❌
              </button>
            </div>
          ))}
        </div>
      )}

      {/* Preview Videos */}
      {videos.length > 0 && (
        <div className="preview-videos">
          {videos.map((video, index) => (
            <div key={index} className="preview-item">
              <video src={video.data} controls />
              <p>{video.name} ({(video.size / 1024 / 1024).toFixed(2)} MB)</p>
              <button onClick={() => setVideos(videos.filter((_, i) => i !== index))}>
                ❌
              </button>
            </div>
          ))}
        </div>
      )}

      <button onClick={handleSubmit} disabled={!content.trim()}>
        Đăng
      </button>
    </div>
  );
};

export default CreatePosterForm;
```

---

### **Hiển thị Poster với Video**

```jsx
const PosterItem = ({ poster }) => {
  return (
    <div className="poster-item">
      <div className="poster-header">
        <img src={poster.userAvatar} alt={poster.userName} />
        <div>
          <h3>{poster.userFirstName} {poster.userLastName}</h3>
          <small>{new Date(poster.createdAt).toLocaleString()}</small>
        </div>
      </div>

      <p className="poster-content">{poster.content}</p>

      {/* Display Images */}
      {poster.imageUrls && poster.imageUrls.length > 0 && (
        <div className="poster-images">
          {poster.imageUrls.map((url, index) => (
            <img key={index} src={url} alt={`Image ${index}`} />
          ))}
        </div>
      )}

      {/* Display Videos */}
      {poster.videos && poster.videos.length > 0 && (
        <div className="poster-videos">
          {poster.videos.map((video, index) => (
            <div key={index} className="video-container">
              <video 
                src={video.url} 
                controls 
                poster={video.thumbnailUrl}
              />
              {video.duration && (
                <span className="video-duration">
                  {Math.floor(video.duration / 60)}:{video.duration % 60}
                </span>
              )}
            </div>
          ))}
        </div>
      )}

      <div className="poster-actions">
        <button>👍 Like</button>
        <button>💬 Comment</button>
        <button>↗️ Share</button>
      </div>
    </div>
  );
};
```

---

## 🎯 Upload Flow

### **Backend Upload Service (user-service)**

Bạn cần implement 2 endpoint mới trong `user-service`:

#### **1. Upload Video Base64**
```java
@PostMapping("/uploads/video/base64")
public String uploadVideoBase64(@RequestBody Map<String, String> body) {
    String base64Data = body.get("data");
    String fileName = body.get("name");
    
    // Extract video data from base64
    // data:video/mp4;base64,AAAAIGZ0eXBpc29t...
    String[] parts = base64Data.split(",");
    String videoData = parts[1];
    
    // Decode and save
    byte[] decodedBytes = Base64.getDecoder().decode(videoData);
    String savedPath = saveVideoToStorage(decodedBytes, fileName);
    
    return savedPath; // Return URL
}
```

#### **2. Delete Video**
```java
@DeleteMapping("/uploads/video/delete")
public String deleteVideo(@RequestParam String videoUrl) {
    deleteFromStorage(videoUrl);
    return "Video deleted";
}
```

---

## 📝 Notes

### **Video Constraints**
- **Max file size**: 100MB (có thể config)
- **Supported formats**: MP4, WebM, MOV
- **Encoding**: H.264 recommended

### **Performance Tips**
- Nén video trước khi upload
- Generate thumbnail tự động
- Sử dụng CDN để serve video
- Lazy load video (chỉ load khi scroll đến)

### **Storage Recommendations**
- **Local**: Lưu trong folder `uploads/videos/`
- **Cloud**: AWS S3, Google Cloud Storage, Azure Blob
- **CDN**: CloudFront, Cloudflare

---

## ✅ Checklist

Backend:
- ✅ Entity `VideoPoster` created
- ✅ Repository `VideoPosterRepository` created
- ✅ Updated `Poster` entity với video relationship
- ✅ Updated `PosterServiceImpl` để xử lý video
- ✅ Updated `UploadClient` với video endpoints
- ✅ Cascade delete cho videos

Frontend (Cần implement):
- ⬜ Video upload UI
- ⬜ Video preview
- ⬜ Video player component
- ⬜ Progress bar cho upload
- ⬜ Video compression (optional)

---

Chúc bạn triển khai thành công! 🚀
