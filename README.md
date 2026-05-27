# 模型管理系统

一个基于 Vue 3 + Spring Boot 的模型/工具管理系统，提供完整的 CRUD 操作、数据统计分析、Excel 批量导入、用户认证和个人信息管理功能。

## 📋 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [核心功能](#核心功能)
- [快速启动](#快速启动)
- [线上部署](#线上部署)
- [API 接口](#api-接口)
- [数据库设计](#数据库设计)
- [配置说明](#配置说明)
- [常见问题](#常见问题)

---

## 项目简介

这是一个全栈 Web 应用，用于管理模型和工具数据。系统支持多用户数据隔离，每个用户只能查看和管理自己的数据。主要功能包括：

- 📊 **数据统计看板** - 实时统计图表展示
- 📦 **模型管理** - 完整的增删改查功能
- 🔧 **工具管理** - 工具数据的CRUD操作
- 📥 **Excel 导入** - 批量导入模型和工具数据
- 👤 **用户认证** - JWT Token 登录注册
- 📝 **个人信息** - 修改资料和头像上传

---

## 技术栈

### 前端
- **框架**: Vue 3 (Composition API)
- **UI 库**: Element Plus
- **构建工具**: Vite 5.x
- **状态管理**: Pinia
- **路由**: Vue Router 4.x
- **HTTP**: Axios
- **图表**: ECharts 5.x

### 后端
- **框架**: Spring Boot 3.2.5
- **ORM**: MyBatis Plus 3.5.9
- **数据库**: MySQL 8.x
- **安全**: Spring Security Crypto (BCrypt)
- **JWT**: JJWT 0.12.5
- **Excel**: EasyExcel 3.3.3
- **文档**: SpringDoc OpenAPI 2.3.0

---

## 项目结构

```
模型管理系统/
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/model/management/
│   │   ├── common/                   # 通用类（Result, PageResult）
│   │   ├── config/                   # 配置类（CORS, JWT, MyBatis Plus）
│   │   ├── controller/               # 控制器（7个）
│   │   │   ├── AuthController        # 认证接口
│   │   │   ├── UserController        # 用户信息接口
│   │   │   ├── FileUploadController  # 文件上传接口
│   │   │   ├── ModelController       # 模型管理接口
│   │   │   ├── ToolController        # 工具管理接口
│   │   │   ├── ManufacturerController # 厂家管理接口
│   │   │   ├── StatisticsController  # 统计分析接口
│   │   │   └── ExcelImportController # Excel导入接口
│   │   ├── dto/                      # 数据传输对象（7个）
│   │   ├── entity/                   # 实体类（4个）
│   │   ├── excel/                    # Excel行映射（2个）
│   │   ├── mapper/                   # Mapper接口（4个）
│   │   ├── service/                  # 服务层（7个接口+实现）
│   │   └── vo/                       # 视图对象（6个）
│   ├── src/main/resources/
│   │   ├── db/migration/             # 数据库迁移脚本
│   │   ├── mapper/                   # MyBatis XML
│   │   └── application.yml           # 应用配置
│   ├── uploads/avatars/              # 头像上传目录
│   └── pom.xml                       # Maven配置
│
├── frontend/                         # Vue 3 前端
│   ├── src/
│   │   ├── api/                      # API接口封装（7个文件）
│   │   │   ├── auth.js              # 认证API
│   │   │   ├── user.js              # 用户API
│   │   │   ├── model.js             # 模型API
│   │   │   ├── tool.js              # 工具API
│   │   │   ├── manufacturer.js      # 厂家API
│   │   │   ├── statistics.js        # 统计API
│   │   │   ├── excel.js             # Excel导入API
│   │   │   └── request.js           # Axios封装
│   │   ├── stores/                   # Pinia Store（2个）
│   │   ├── views/                    # 页面组件（7个）
│   │   │   ├── Login.vue            # 登录页
│   │   │   ├── Register.vue         # 注册页
│   │   │   ├── Layout.vue           # 主布局
│   │   │   ├── Dashboard.vue        # 数据统计
│   │   │   ├── ModelList.vue        # 模型管理
│   │   │   ├── ToolList.vue         # 工具管理
│   │   │   ├── ExcelImport.vue      # Excel导入
│   │   │   └── Profile.vue          # 个人信息
│   │   ├── router/index.js           # 路由配置
│   │   ├── App.vue                   # 根组件
│   │   └── main.js                   # 入口文件
│   ├── package.json                  # npm依赖
│   ├── vite.config.js                # Vite配置
│   └── index.html                    # HTML模板
│
└── README.md                         # 项目说明文档
```

---

## 核心功能

### 1. 用户认证系统
- ✅ 用户注册（用户名、密码、邮箱、手机号）
- ✅ 用户登录（JWT Token 认证）
- ✅ Token 自动刷新和管理
- ✅ 登录状态持久化
- ✅ 路由守卫保护

### 2. 数据统计看板
- 📊 模型和工具的总数、总金额统计
- 🥧 厂家模型占比饼图
- 📈 价格区间分布柱状图
- 🔄 数据按用户隔离

### 3. 模型管理
- ➕ 新增模型（支持选择或手动输入厂家）
- ✏️ 编辑模型信息
- 🗑️ 删除单个或批量删除
- 🔍 多条件筛选（厂家、名称、价格范围）
- 📄 分页查询

### 4. 工具管理
- ➕ 新增工具
- ✏️ 编辑工具信息
- 🗑️ 删除工具
- 🔍 按名称和价格范围筛选
- 📄 分页查询

### 5. Excel 批量导入
- 📥 下载标准导入模板
- 📂 拖拽上传或点击上传
- 🔄 支持模型和工具两种类型
- 📊 实时显示导入结果
- ❌ 详细的错误信息展示

### 6. 个人信息管理
- 👤 查看个人信息
- ✏️ 修改昵称、邮箱、手机号
- 🖼️ 上传头像（支持JPG/PNG/GIF/WEBP）
- 🔐 修改密码
- 🗑️ 删除自定义头像

---

## 快速启动

### 前置要求

- Node.js >= 16.x
- JDK 17+
- MySQL 8.x
- Maven 3.6+

### 第一步：初始化数据库

1. 创建数据库：
```sql
CREATE DATABASE IF NOT EXISTS model_management 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;
```

2. 执行建表脚本：
```bash
# 使用MySQL客户端执行
mysql -u root -p model_management < backend/src/main/resources/db/migration/V1__add_user_table.sql
```

或者在 MySQL 命令行中手动执行 `V1__add_user_table.sql` 文件中的SQL语句。

3. 修改数据库配置：
编辑 `backend/src/main/resources/application.yml`，设置正确的数据库用户名和密码：
```yaml
spring:
  datasource:
    username: root
    password: your_password  # 修改为你的密码
```

### 第二步：启动后端

```bash
cd backend
mvn spring-boot:run
```

等待看到以下日志表示成功：
```
Started ModelManagementApplication in X.XXX seconds
Tomcat started on port 8080 (http)
```

访问 Swagger UI: http://localhost:8080/swagger-ui.html

### 第三步：启动前端

```bash
cd frontend
npm install    # 首次需要安装依赖
npm run dev
```

等待看到以下日志表示成功：
```
VITE ready in XXX ms
Local: http://localhost:5173/
```

### 第四步：登录系统

1. 打开浏览器访问: http://localhost:5173
2. 注册登录账号
---

## 线上部署

想要将项目部署到互联网，让所有人都可以访问？查看详细的免费部署指南：

📖 **[部署指南.md](./部署指南.md)**

### 推荐方案：Vercel + Railway

- **前端**: Vercel (https://vercel.com) - 完全免费，自动HTTPS，全球CDN
- **后端**: Railway (https://railway.app) - 每月$5免费额度，支持MySQL
- **总成本**: $0/月 🎉

### 快速开始

1. **准备GitHub仓库** - 推送代码到GitHub
2. **部署数据库** - 在Railway创建MySQL
3. **部署后端** - Railway自动构建Spring Boot
4. **部署前端** - Vercel自动构建Vue3
5. **配置CORS** - 允许跨域访问
6. **测试访问** - 完成！

详细步骤请查看 [部署指南.md](./部署指南.md)

---

## API 接口

### 认证接口 `/api/auth`
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 用户接口 `/api/user`
- `GET /api/user/profile` - 获取当前用户信息
- `PUT /api/user/profile` - 更新用户信息
- `PUT /api/user/password` - 修改密码

### 文件上传 `/api/upload`
- `POST /api/upload/avatar` - 上传头像

### 模型管理 `/api/models`
- `GET /api/models` - 分页查询模型列表
- `GET /api/models/{id}` - 获取单个模型
- `POST /api/models` - 新增模型
- `PUT /api/models/{id}` - 更新模型
- `DELETE /api/models/{id}` - 删除模型
- `DELETE /api/models/batch` - 批量删除

### 工具管理 `/api/tools`
- `GET /api/tools` - 分页查询工具列表
- `POST /api/tools` - 新增工具
- `PUT /api/tools/{id}` - 更新工具
- `DELETE /api/tools/{id}` - 删除工具

### 厂家管理 `/api/manufacturers`
- `GET /api/manufacturers` - 获取厂家列表
- `POST /api/manufacturers` - 新增厂家
- `PUT /api/manufacturers/{id}` - 更新厂家
- `DELETE /api/manufacturers/{id}` - 删除厂家

### 统计分析 `/api/statistics`
- `GET /api/statistics/overview` - 获取统计概览
- `GET /api/statistics/manufacturer` - 获取厂家统计
- `GET /api/statistics/price-distribution` - 获取价格分布

### Excel 导入 `/api/import`
- `POST /api/import/excel?type=model` - 导入模型数据
- `POST /api/import/excel?type=tool` - 导入工具数据
- `GET /api/import/template/model` - 下载模型模板
- `GET /api/import/template/tool` - 下载工具模板

---

## 数据库设计

### 主要表结构

#### user（用户表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名（唯一） |
| password | VARCHAR(255) | 密码（BCrypt加密） |
| email | VARCHAR(100) | 邮箱（唯一） |
| phone | VARCHAR(20) | 手机号 |
| nickname | VARCHAR(50) | 昵称 |
| avatar | VARCHAR(255) | 头像URL |
| status | TINYINT | 状态：0-禁用，1-正常 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### manufacturer（厂家表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| name | VARCHAR(100) | 厂家名称（唯一） |
| description | VARCHAR(500) | 描述 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| deleted | TINYINT(1) | 逻辑删除 |

#### model（模型表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| manufacturer_id | BIGINT | 厂家ID |
| name | VARCHAR(200) | 模型名称 |
| price | DECIMAL(10,2) | 价格 |
| remark | VARCHAR(500) | 备注 |
| sold | INT | 是否售出：0-未售，1-已售 |
| user_id | BIGINT | 用户ID（数据归属） |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| deleted | TINYINT(1) | 逻辑删除 |

#### tool（工具表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| name | VARCHAR(200) | 工具名称 |
| price | DECIMAL(10,2) | 价格 |
| remark | VARCHAR(500) | 备注 |
| sold | INT | 是否售出：0-未售，1-已售 |
| user_id | BIGINT | 用户ID（数据归属） |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| deleted | TINYINT(1) | 逻辑删除 |

---

## 配置说明

### 后端配置（application.yml）

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/model_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    map-underscore-to-camel-case: true

jwt:
  secret: mySecretKeyForJWTTokenGenerationAndValidation123456789
  expiration: 86400000  # 24小时（毫秒）

file:
  upload:
    path: uploads/avatars
  access:
    url: http://localhost:8080/uploads
```

### 前端配置（vite.config.js）

```javascript
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

---

## 常见问题

### Q1: 后端启动报错 "Table 'user' doesn't exist"
**解决方案**: 
- 必须先执行数据库初始化脚本 `V1__add_user_table.sql`
- 确认已选择正确的数据库（model_management）

### Q2: 登录失败提示"用户名或密码错误"
**解决方案**:
- 执行密码修复脚本：`fix_admin_password.sql`
- 或注册一个新用户进行测试

### Q3: 前端无法连接后端
**解决方案**:
- 确认后端服务已启动（访问 http://localhost:8080/swagger-ui.html）
- 检查 `vite.config.js` 中的代理配置
- 确认防火墙没有阻止 8080 端口

### Q4: 上传头像后刷新消失
**解决方案**:
- 上传成功后必须点击"保存修改"按钮
- 检查浏览器控制台是否有错误
- 确认后端返回的头像URL正确

### Q5: 厂家下拉框为空
**解决方案**:
- 这是正常的！厂家列表只显示有模型的厂家
- 手动输入新厂家名称即可（如"华为"）
- 创建后该厂家会出现在下拉列表中

### Q6: 看不到其他用户的数据
**解决方案**:
- 这是预期行为！系统设计为数据隔离
- 每个用户只能看到自己的数据
- 如需查看所有数据，使用 admin 账户登录

### Q7: Excel 导入失败
**解决方案**:
- 确认使用正确的模板（模型模板 vs 工具模板）
- 检查文件格式是否为 .xlsx 或 .xls
- 确认数据格式与模板一致
- 查看导入结果中的错误详情

---

## 开发规范

### 代码风格
- 前端：遵循 Vue 3 Composition API 最佳实践
- 后端：遵循 RESTful API 设计规范
- 统一使用驼峰命名法

### Git 提交规范
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构代码
- `test`: 测试相关
- `chore`: 构建/工具链相关

---

## 部署建议

### 生产环境部署

1. **前端构建**：
```bash
cd frontend
npm run build
# 生成的 dist 目录部署到 Nginx
```

2. **后端打包**：
```bash
cd backend
mvn clean package
# 生成的 jar 包使用 java -jar 运行
java -jar target/management-1.0.0.jar
```

3. **Nginx 配置示例**：
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location /uploads {
        alias /path/to/backend/uploads;
        expires 30d;
    }
}
```

---

## 安全特性

- ✅ JWT Token 认证
- ✅ BCrypt 密码加密
- ✅ CORS 跨域配置
- ✅ SQL 注入防护（MyBatis Plus 参数化查询）
- ✅ XSS 防护（Element Plus 自动转义）
- ✅ 文件上传验证（类型、大小）
- ✅ 数据隔离（用户只能访问自己的数据）
- ✅ 逻辑删除（防止数据丢失）

---

## 后续优化计划

1. **功能增强**
   - [ ] 权限管理（RBAC）
   - [ ] 操作日志记录
   - [ ] 数据导出功能
   - [ ] 更多统计维度
   - [ ] 消息通知系统

2. **性能优化**
   - [ ] Redis 缓存热点数据
   - [ ] 前端资源 CDN 加速
   - [ ] 后端接口响应压缩
   - [ ] 数据库读写分离
   - [ ] 图片压缩和缩略图

3. **用户体验**
   - [ ] 国际化支持
   - [ ] 主题切换
   - [ ] 快捷键支持
   - [ ] 头像裁剪功能
   - [ ] 上传进度显示

---

## 技术支持

如遇到问题，请检查：
1. 浏览器控制台（F12）的错误信息
2. 后端服务的日志输出
3. 数据库中的数据是否正确
4. 网络请求的响应状态码和数据

---

**最后更新时间**: 2026-05-27
