# Tính năng kiểm tra nội dung ảnh nhạy cảm

## Mô tả

Tính năng này tự động kiểm tra ảnh khi người dùng đăng poster để phát hiện nội dung không phù hợp (sexy/porn/hentai) trước khi upload lên hệ thống.

## Cách hoạt động

1. **Khi tạo poster mới**: 
   - Khi người dùng đăng poster có chứa ảnh (dạng base64), hệ thống sẽ gọi AI service để kiểm tra nội dung ảnh
   - Nếu phát hiện nội dung nhạy cảm, poster sẽ bị từ chối và không được lưu vào database
   - Nếu ảnh hợp lệ, tiến hành upload lên Cloudinary và lưu thông tin poster

2. **AI Service Response**:
   ```json
   {
       "top_label": "sexy",
       "porn_score": 0.0001185,
       "confidence": 0.9997840,
       "sexy_score": 0.9997840,
       "hentai_score": 0.0000345,
       "is_sexy": true,
       "message": "Ảnh có nội dung nhạy cảm (sexy/porn/hentai)"
   }
   ```

3. **Xác định loại nội dung**:
   - Hệ thống so sánh `porn_score`, `sexy_score`, `hentai_score`
   - Loại nào có điểm cao nhất sẽ được sử dụng để thông báo cho người dùng

## Response API

### Thành công
```json
"✅ Tạo poster thành công!"
```

### Thất bại - Phát hiện nội dung nhạy cảm

**Porn:**
```json
"❌ Ảnh chứa nội dung porn (độ tin cậy: 99.78%)"
```

**Sexy:**
```json
"❌ Ảnh chứa nội dung sexy (độ tin cậy: 99.78%)"
```

**Hentai:**
```json
"❌ Ảnh chứa nội dung hentai (độ tin cậy: 99.78%)"
```

## Các file liên quan

1. **InappropriateContentException.java**
   - Custom exception để xử lý trường hợp ảnh không phù hợp
   - Chứa thông tin về loại nội dung và độ tin cậy

2. **PosterServiceImpl.java**
   - Method `validateImageContent()`: Gọi AI service để kiểm tra ảnh
   - Method `getDoubleValue()`: Helper để convert response từ AI service
   - Logic kiểm tra ảnh trước khi upload trong method `save()`

3. **AiClient.java**
   - Feign client để gọi AI service
   - Endpoint: `POST /huggingface/is-image-sexy-base64`
   - **Request body**: `{ "image": "<base64_string_without_prefix>" }`
   - Lưu ý: Cần loại bỏ prefix `data:image/...;base64,` trước khi gửi

4. **InappropriateContentResponse.java** (Optional)
   - DTO để format response một cách có cấu trúc hơn

## Luồng xử lý

```
User đăng poster với ảnh
        ↓
PosterController.createPoster()
        ↓
PosterServiceImpl.save()
        ↓
Tạo Poster entity và lưu DB
        ↓
Duyệt qua từng ảnh base64
        ↓
Loại bỏ prefix "data:image/...;base64,"
        ↓
validateImageContent(base64Image)
        ↓
AiClient.checkImageSexyBase64({"image": cleanBase64})
        ↓
[AI Service Response]
        ↓
    is_sexy = true?
        ↓
   YES ─────────────────────────> Throw InappropriateContentException
        │                                      ↓
        │                         Xóa poster đã tạo từ DB
        │                                      ↓
        │                         Return error message
        ↓
       NO
        ↓
Upload ảnh lên Cloudinary
        ↓
Lưu ImagePoster entity
        ↓
Return success
```

## Cấu hình

- **AI Service**: Cần đảm bảo `ai-service` đang chạy và có thể truy cập
- **Fallback**: Nếu AI service không hoạt động, mặc định sẽ **CHO PHÉP** upload (có thể thay đổi logic này nếu cần)

## Testing

### Test case 1: Ảnh bình thường
```bash
POST /posters
Body: { "idUser": "...", "content": "...", "imageUrls": ["data:image/jpeg;base64,..."] }
Expected: 200 OK - Poster được tạo thành công
```

### Test case 2: Ảnh sexy
```bash
POST /posters
Body: { "idUser": "...", "content": "...", "imageUrls": ["data:image/jpeg;base64,..."] }
Expected: 400 Bad Request - "❌ Ảnh chứa nội dung sexy (độ tin cậy: XX.XX%)"
```

### Test case 3: AI service không hoạt động
```bash
POST /posters (với AI service bị tắt)
Body: { "idUser": "...", "content": "...", "imageUrls": ["data:image/jpeg;base64,..."] }
Expected: 200 OK - Poster được tạo thành công (fallback behavior)
```

## Lưu ý

- Kiểm tra chỉ áp dụng cho ảnh **base64** (ảnh mới upload)
- Ảnh đã có URL (từ Cloudinary) sẽ không được kiểm tra lại
- Nếu có nhiều ảnh, chỉ cần 1 ảnh vi phạm thì toàn bộ poster sẽ bị từ chối
- Poster đã tạo sẽ bị xóa nếu phát hiện ảnh không hợp lệ (rollback)
