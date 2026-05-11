# 慢慢记 API 接口文档

**Base URL**: `http://localhost:8080`

## 通用响应格式

所有接口统一返回 `R<T>` 结构：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

| code | 含义 |
|------|------|
| 200  | 成功 |
| 400  | 请求参数错误 |
| 401  | 未登录 / Token 过期 |
| 403  | 无权限 |
| 404  | 资源不存在 |
| 500  | 服务端错误 |

分页接口使用 `PageDTO<T>`：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "page": 1,
    "size": 10,
    "records": [ ... ]
  }
}
```

---

## 1. 认证模块 `/api/auth`

### 1.1 注册

```
POST /api/auth/register
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名，3-50 字符 |
| email | String | 是 | 邮箱，需符合邮箱格式 |
| password | String | 是 | 密码，6-100 字符 |
| nickname | String | 否 | 昵称，不填则用 username |

**示例**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"zhangsan","email":"zhangsan@test.com","password":"123456"}'
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi...",
    "expiresIn": 900,
    "user": {
      "id": 1,
      "username": "zhangsan",
      "email": "zhangsan@test.com",
      "nickname": "zhangsan",
      "avatarUrl": null,
      "role": "USER"
    }
  }
}
```

| 字段 | 说明 |
|------|------|
| accessToken | 访问令牌，后续请求携带，15 分钟有效 |
| refreshToken | 刷新令牌，用于获取新 accessToken，7 天有效 |
| expiresIn | accessToken 有效期（秒） |

> 可能错误：`400` 用户名已存在 / 邮箱已被注册

---

### 1.2 登录

```
POST /api/auth/login
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**示例**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"zhangsan","password":"123456"}'
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi...",
    "expiresIn": 900,
    "user": {
      "id": 1,
      "username": "zhangsan",
      "email": "zhangsan@test.com",
      "nickname": "zhangsan",
      "avatarUrl": null,
      "role": "USER"
    }
  }
}
```

> 可能错误：`401` 用户名或密码错误

---

### 1.3 刷新令牌

```
POST /api/auth/refresh
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refreshToken | String | 是 | 登录/注册时返回的 refreshToken |

**示例**

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"eyJhbGciOi..."}'
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi...",
    "expiresIn": 900,
    "user": {
      "id": 1,
      "username": "zhangsan",
      "email": "zhangsan@test.com",
      "nickname": "zhangsan",
      "avatarUrl": null,
      "role": "USER"
    }
  }
}
```

> 会同时返回新的 `accessToken` 和 `refreshToken`（滚动刷新）

---

## 认证方式

除了注册、登录、刷新令牌三个接口外，其他需要登录的接口在请求头携带：

```
Authorization: Bearer <accessToken>
```

accessToken 过期后用 `/api/auth/refresh` 换新，不用重复登录。
