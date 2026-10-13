# 分片上传设计方案

## 1. 需求背景

现有文件上传接口 `FileUploadController.upload()` 将文件一次性加载到内存，高并发大文件上传时易导致内存溢出。需要实现分片上传，支持大文件切片上传、合并，并兼容本地存储和 OSS 存储。

## 2. 技术选型

- **分片上传方案**：使用 x-file-storage 原生分片上传 API
- **断点续传**：通过 `uploadId` 关联分片记录，支持中断后从断点继续
- **分片大小**：后台可配置（默认 5MB）

## 3. 整体流程

```
前端                    Controller                   Service                    存储
  │                         │                          │                          │
  │──1. 初始化上传────────>│                          │                          │
  │<──uploadId, chunkSize──│                          │                          │
  │                         │                          │                          │
  │──2. 分片上传(循环)────>│                          │                          │
  │                         │──保存分片到临时目录──────>│                          │
  │                         │<──返回 eTag──────────────│                          │
  │                         │                          │                          │
  │──3. 查询进度(可选)────>│                          │                          │
  │<──已上传分片列表───────│                          │                          │
  │                         │                          │                          │
  │──4. 完成合并──────────>│                          │                          │
  │                         │──合并分片,上传到OSS──────>│                          │
  │                         │<──返回 FileInfo─────────│                          │
  │<──文件信息─────────────│                          │                          │
```

## 4. 接口设计

### 4.1 初始化分片上传

```
POST /anyone/filePart/init
Content-Type: multipart/form-data

fileName: 文件名
fileSize: 文件大小(字节)
fileHash: 文件哈希(可选,用于秒传)
objectType: 业务类型
objectId: 业务ID
platform: 存储平台(可选)

响应:
{
  "code": 0,
  "data": {
    "uploadId": "uuid",
    "chunkSize": 5242880,  // 5MB
    "totalChunks": 10
  }
}
```

### 4.2 上传分片

```
POST /anyone/filePart/upload
Content-Type: multipart/form-data

uploadId: 上传ID
partNumber: 分片号(从1开始)
file: 分片文件

响应:
{
  "code": 0,
  "data": {
    "partNumber": 1,
    "eTag": "etag-xxx"
  }
}
```

### 4.3 查询上传进度

```
GET /anyone/filePart/progress?uploadId={uploadId}

响应:
{
  "code": 0,
  "data": {
    "uploadId": "uuid",
    "uploadedParts": [
      {"partNumber": 1, "eTag": "etag-1"},
      {"partNumber": 2, "eTag": "etag-2"}
    ],
    "isComplete": false
  }
}
```

### 4.4 完成合并

```
POST /anyone/filePart/complete
Content-Type: application/json

{
  "uploadId": "uuid"
}

响应:
{
  "code": 0,
  "data": {
    "fileId": 123,
    "url": "http://xxx/xxx.jpg"
  }
}
```

### 4.5 取消上传

```
POST /anyone/filePart/abort
Content-Type: application/json

{
  "uploadId": "uuid"
}

响应:
{
  "code": 0
}
```

## 5. 配置项

```yaml
mdp:
  file:
    chunk-size: 5  # 分片大小，单位 MB，默认 5MB
```

## 6. 数据结构

### 6.1 FilePart（已有）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| uploadId | String | 上传批次ID |
| platform | String | 存储平台 |
| partNumber | Integer | 分片序号 |
| eTag | String | 分片标识 |
| partSize | Long | 分片大小 |
| hashInfo | String | 分片哈希 |
| createdAt | DateTime | 创建时间 |

### 6.2 新增 DTO

```java
// 初始化请求
InitPartUploadDto {
    fileName: String      // 文件名
    fileSize: Long       // 文件大小
    fileHash: String     // 文件哈希
    objectType: String   // 业务类型
    objectId: Long       // 业务ID
    platform: String     // 存储平台
}

// 初始化响应
InitPartUploadResp {
    uploadId: String     // 上传ID
    chunkSize: Long      // 分片大小(字节)
    totalChunks: Integer // 总分片数
}

// 分片上传请求
UploadPartDto {
    uploadId: String     // 上传ID
    partNumber: Integer  // 分片号
    file: MultipartFile  // 分片文件
}

// 分片上传响应
UploadPartResp {
    partNumber: Integer
    eTag: String
}

// 查询进度响应
UploadProgressResp {
    uploadId: String
    uploadedParts: List<UploadedPart>
    isComplete: Boolean
}

// 完成合并请求
CompletePartUploadDto {
    uploadId: String
}

// 完成合并响应
CompletePartUploadResp {
    fileId: Long
    url: String
}

// 取消上传请求
AbortPartUploadDto {
    uploadId: String
}
```

## 7. 核心逻辑

### 7.1 初始化分片上传

1. 验证文件后缀
2. 生成 `uploadId`（UUID）
3. 计算总分片数 = ceil(文件大小 / 分片大小)
4. 调用 `fileStorageService.initiateMultipartUpload()` 创建上传任务
5. 返回 `uploadId`、分片大小、总分片数

### 7.2 分片上传

1. 验证 `uploadId` 存在
2. 获取分片文件
3. 调用 `fileStorageService.uploadPart()` 上传分片
4. 保存分片信息到 `mdc_file_part` 表
5. 返回 `eTag`

### 7.3 查询进度

1. 根据 `uploadId` 查询已上传的分片列表
2. 返回已上传的分片号和 eTag

### 7.4 完成合并

1. 验证所有分片已上传
2. 调用 `fileStorageService.completeMultipartUpload()` 合并分片
3. 清理临时分片文件
4. 删除 `mdc_file_part` 表中的分片记录
5. 返回文件信息

### 7.5 取消上传

1. 调用 `fileStorageService.abortMultipartUpload()` 中止上传
2. 删除临时分片文件
3. 删除 `mdc_file_part` 表中的分片记录

## 8. 文件路径

```
mdp-apps/md-console/
├── console-web/src/main/java/top/mddata/console/controller/
│   └── FilePartController.java          # 新增分片上传接口
├── console-service/src/main/java/top/mddata/console/service/system/
│   ├── FileService.java                 # 新增分片相关方法声明
│   └── impl/FileServiceImpl.java       # 新增分片相关实现
├── console-pojo/src/main/java/top/mddata/console/dto/system/
│   ├── InitPartUploadDto.java
│   ├── UploadPartDto.java
│   ├── CompletePartUploadDto.java
│   ├── AbortPartUploadDto.java
│   └── resp/
│       ├── InitPartUploadResp.java
│       ├── UploadPartResp.java
│       ├── UploadProgressResp.java
│       └── CompletePartUploadResp.java
```

## 9. 实现顺序

1. 新增 DTO 类
2. 在 FileService 接口新增分片相关方法
3. 在 FileServiceImpl 实现分片逻辑
4. 在 FilePartController 新增分片上传接口
5. 在 FileProperties 新增分片大小配置
6. 添加单元测试

## 10. 注意事项

1. 分片必须按顺序上传和合并
2. 并发上传时需保证同一分片不重复上传
3. 上传完成后需清理临时分片文件
4. 需处理 x-file-storage 与不同存储平台的兼容性
