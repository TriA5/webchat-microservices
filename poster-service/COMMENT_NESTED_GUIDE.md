# 💬 Hướng dẫn sử dụng Comment System - Multi-level Nested Replies

## 🎯 Tính năng

Hệ thống comment hỗ trợ **reply đa cấp không giới hạn**:
- A comment vào poster
- B reply comment của A
- C reply comment của B
- D reply comment của C
- ... (không giới hạn số cấp)

---

## 🚀 API Endpoints

### 1️⃣ **Comment vào poster (Root comment)**

Người dùng A comment vào poster:

```http
POST /comments/{posterId}
Content-Type: application/json

{
  "content": "Đây là comment của A",
  "userId": "uuid-của-A"
}
```

**Response:**
```json
{
  "idComment": "comment-A-uuid",
  "content": "Đây là comment của A",
  "idUser": "uuid-của-A",
  "idPoster": "poster-uuid",
  "parentCommentId": null,
  "replies": [],
  "replyCount": 0,
  ...
}
```

---

### 2️⃣ **Reply comment của người khác**

**B reply comment của A:**

```http
POST /comments/{posterId}/{commentId-của-A}/reply
Content-Type: application/json

{
  "content": "B trả lời A",
  "userId": "uuid-của-B"
}
```

**C reply comment của B:**

```http
POST /comments/{posterId}/{commentId-của-B}/reply
Content-Type: application/json

{
  "content": "C trả lời B",
  "userId": "uuid-của-C"
}
```

**D reply comment của C:**

```http
POST /comments/{posterId}/{commentId-của-C}/reply
Content-Type: application/json

{
  "content": "D trả lời C",
  "userId": "uuid-của-D"
}
```

✅ **Không giới hạn số cấp reply!**

---

### 3️⃣ **Lấy tất cả comment (với nested replies đầy đủ)**

```http
GET /comments/{posterId}
```

**Response có cấu trúc nested:**

```json
[
  {
    "idComment": "comment-A-uuid",
    "content": "Đây là comment của A",
    "idUser": "uuid-của-A",
    "parentCommentId": null,
    "replies": [
      {
        "idComment": "comment-B-uuid",
        "content": "B trả lời A",
        "idUser": "uuid-của-B",
        "parentCommentId": "comment-A-uuid",
        "replies": [
          {
            "idComment": "comment-C-uuid",
            "content": "C trả lời B",
            "idUser": "uuid-của-C",
            "parentCommentId": "comment-B-uuid",
            "replies": [
              {
                "idComment": "comment-D-uuid",
                "content": "D trả lời C",
                "idUser": "uuid-của-D",
                "parentCommentId": "comment-C-uuid",
                "replies": [],
                "replyCount": 0
              }
            ],
            "replyCount": 1
          }
        ],
        "replyCount": 1
      }
    ],
    "replyCount": 1,
    "createdAt": "...",
    "updatedAt": "..."
  }
]
```

---

## 🔄 Flow hoạt động

### **Scenario: A → B → C → D**

1. **A comment vào poster:**
   ```
   POST /comments/{posterId}
   Body: { "content": "Comment A", "userId": "A-uuid" }
   ```

2. **B reply comment của A:**
   ```
   POST /comments/{posterId}/{comment-A-uuid}/reply
   Body: { "content": "Reply B to A", "userId": "B-uuid" }
   ```
   - Backend tạo comment B với `parentCommentId = comment-A-uuid`
   - Gửi notification cho A: "B đã trả lời bình luận của bạn"

3. **C reply comment của B:**
   ```
   POST /comments/{posterId}/{comment-B-uuid}/reply
   Body: { "content": "Reply C to B", "userId": "C-uuid" }
   ```
   - Backend tạo comment C với `parentCommentId = comment-B-uuid`
   - Gửi notification cho B: "C đã trả lời bình luận của bạn"

4. **D reply comment của C:**
   ```
   POST /comments/{posterId}/{comment-C-uuid}/reply
   Body: { "content": "Reply D to C", "userId": "D-uuid" }
   ```
   - Backend tạo comment D với `parentCommentId = comment-C-uuid`
   - Gửi notification cho C: "D đã trả lời bình luận của bạn"

---

## 🎨 Frontend Implementation (React Example)

### Recursive Comment Component

```jsx
// CommentItem.jsx
const CommentItem = ({ comment, posterId, currentUserId }) => {
  const [showReplyForm, setShowReplyForm] = useState(false);
  const [replyContent, setReplyContent] = useState("");

  const handleReply = async () => {
    const response = await fetch(
      `http://localhost:8080/comments/${posterId}/${comment.idComment}/reply`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          content: replyContent,
          userId: currentUserId
        })
      }
    );

    if (response.ok) {
      setReplyContent("");
      setShowReplyForm(false);
      // Reload comments
    }
  };

  return (
    <div className="comment-item" style={{ marginLeft: comment.parentCommentId ? "30px" : "0" }}>
      <div className="comment-header">
        <strong>User {comment.idUser}</strong>
        <small>{new Date(comment.createdAt).toLocaleString()}</small>
      </div>
      
      <p className="comment-content">{comment.content}</p>
      
      <button onClick={() => setShowReplyForm(!showReplyForm)}>
        💬 Trả lời
      </button>

      {showReplyForm && (
        <div className="reply-form">
          <textarea
            value={replyContent}
            onChange={(e) => setReplyContent(e.target.value)}
            placeholder="Viết phản hồi..."
          />
          <button onClick={handleReply}>Gửi</button>
        </div>
      )}

      {/* 🔄 RECURSIVE: Hiển thị tất cả replies (nested) */}
      {comment.replies && comment.replies.length > 0 && (
        <div className="replies">
          {comment.replies.map(reply => (
            <CommentItem
              key={reply.idComment}
              comment={reply}
              posterId={posterId}
              currentUserId={currentUserId}
            />
          ))}
        </div>
      )}
    </div>
  );
};

// CommentList.jsx
const CommentList = ({ posterId, currentUserId }) => {
  const [comments, setComments] = useState([]);

  useEffect(() => {
    fetchComments();
  }, [posterId]);

  const fetchComments = async () => {
    const response = await fetch(`http://localhost:8080/comments/${posterId}`);
    const data = await response.json();
    setComments(data);
  };

  return (
    <div className="comment-list">
      {comments.map(comment => (
        <CommentItem
          key={comment.idComment}
          comment={comment}
          posterId={posterId}
          currentUserId={currentUserId}
        />
      ))}
    </div>
  );
};
```

---

## 📊 Cấu trúc Database

```sql
comment_posters
├── id_comment (PK)
├── content
├── id_user
├── id_poster (FK → poster.id_poster)
├── parent_comment_id (FK → comment_posters.id_comment) -- Self-referencing
├── created_at
└── updated_at
```

**Quan hệ:**
- `parent_comment_id = NULL` → Root comment (comment gốc)
- `parent_comment_id = {uuid}` → Reply của comment khác

---

## ✅ Testing

### Test Case: A → B → C

**1. A comment:**
```bash
curl -X POST http://localhost:8080/comments/{posterId} \
  -H "Content-Type: application/json" \
  -d '{"content":"Comment A","userId":"user-A-uuid"}'
```

**2. B reply A (lấy `idComment` của A từ response trên):**
```bash
curl -X POST http://localhost:8080/comments/{posterId}/{comment-A-uuid}/reply \
  -H "Content-Type: application/json" \
  -d '{"content":"Reply B","userId":"user-B-uuid"}'
```

**3. C reply B (lấy `idComment` của B):**
```bash
curl -X POST http://localhost:8080/comments/{posterId}/{comment-B-uuid}/reply \
  -H "Content-Type: application/json" \
  -d '{"content":"Reply C","userId":"user-C-uuid"}'
```

**4. Lấy tất cả comments:**
```bash
curl http://localhost:8080/comments/{posterId}
```

**Expected:** Thấy cấu trúc nested: A có reply B, B có reply C

---

## 🔔 Notifications

Khi reply comment, hệ thống tự động gửi notification:

- **B reply A** → A nhận notification: "B đã trả lời bình luận của bạn"
- **C reply B** → B nhận notification: "C đã trả lời bình luận của bạn"
- **D reply C** → C nhận notification: "D đã trả lời bình luận của bạn"

✅ Mỗi người chỉ nhận notification khi có người reply **trực tiếp** comment của họ.

---

## 🎯 Key Points

✅ **Unlimited nesting**: Không giới hạn số cấp reply  
✅ **Recursive loading**: API tự động load tất cả replies lồng nhau  
✅ **Notification**: Tự động gửi thông báo cho người được reply  
✅ **Cascade delete**: Xóa comment cha → tự động xóa tất cả replies  
✅ **Authorization**: Chỉ người tạo mới được xóa/sửa comment  

---

## 🐛 Common Issues

### **Issue 1: Reply không hiển thị**
**Giải pháp:** Đảm bảo gọi API `GET /comments/{posterId}` sau khi reply để reload data

### **Issue 2: Nested replies không load đầy đủ**
**Giải pháp:** Đã fix! Method `convertToDTOWithReplies` giờ gọi đệ quy để load tất cả cấp

### **Issue 3: Notification không nhận được**
**Giải pháp:** Kiểm tra WebSocket connection và subscribe đúng topic `/user/{userId}/queue/notifications`

---

Chúc bạn xây dựng comment system thành công! 🚀
