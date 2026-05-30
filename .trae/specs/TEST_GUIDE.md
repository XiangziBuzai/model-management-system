# AI 助手功能 - 测试指南

## 环境准备

### 1. 配置 DeepSeek API Key

在启动后端之前，需要配置 DeepSeek API Key。有两种方式：

**方式一：环境变量**
```bash
export DEEPSEEK_API_KEY=your-deepseek-api-key
```

**方式二：修改配置文件**

编辑 `backend/src/main/resources/application.yml`，将以下配置中的 `your-api-key-here` 替换为你的 DeepSeek API Key：

```yaml
deepseek:
  api-key: sk-your-actual-api-key
```

### 2. 数据库迁移

确保数据库已执行迁移脚本 `V10__create_ai_chat_tables.sql`，创建了以下表：
- `ai_chat_session` - AI 对话会话表
- `ai_chat_message` - AI 对话消息表

## 启动服务

### 启动后端
```bash
cd backend
mvn spring-boot:run
```

或使用 IDE 启动 `ModelManagementApplication`

### 启动前端
```bash
cd frontend
npm run dev
```

## 功能测试

### 测试用例

#### 1. AI 助手入口测试
- [ ] 在管理后台（/dashboard）右下角看到 AI 助手悬浮按钮
- [ ] 在模型广场（/square/home）右下角看到 AI 助手悬浮按钮
- [ ] 点击按钮能够打开聊天对话框

#### 2. 新建对话测试
- [ ] 点击"新对话"按钮
- [ ] 成功创建新会话
- [ ] 会话显示在左侧会话列表中

#### 3. 发送消息测试
- [ ] 在输入框输入问题，例如："你好，你能帮我做什么？"
- [ ] 按发送按钮或 Ctrl+Enter
- [ ] 看到用户消息显示在右侧
- [ ] 看到加载动画（三个点跳动）
- [ ] 收到 AI 回复
- [ ] 对话自动保存

#### 4. 对话历史测试
- [ ] 刷新页面后，点击之前的会话
- [ ] 历史消息能够正确加载显示
- [ ] 会话列表显示最后一条消息摘要

#### 5. 删除对话测试
- [ ] 鼠标悬停在会话列表项上
- [ ] 看到删除按钮出现
- [ ] 点击删除按钮
- [ ] 确认删除后，会话从列表中消失

#### 6. 多轮对话测试
- [ ] 在一个会话中进行多轮对话
- [ ] AI 能够理解上下文
- [ ] 对话内容准确保存

### 测试问题排查

#### 问题：点击 AI 助手按钮没有反应
**可能原因**：
1. 前端未正确加载组件
2. 浏览器控制台有报错

**解决方案**：
- 检查浏览器控制台（F12）
- 刷新页面重试

#### 问题：发送消息后长时间无响应
**可能原因**：
1. DeepSeek API Key 配置错误
2. 网络连接问题
3. API 调用超时

**解决方案**：
- 检查 API Key 是否正确配置
- 检查后端控制台日志
- 确认 DeepSeek API 服务可用

#### 问题：历史对话加载失败
**可能原因**：
1. 数据库表未创建
2. 后端 API 调用失败

**解决方案**：
- 检查数据库是否成功执行迁移脚本
- 检查后端控制台错误日志

## 配置参数说明

可在 `application.yml` 中调整以下参数：

```yaml
deepseek:
  api-key: ${DEEPSEEK_API_KEY:your-api-key}
  base-url: ${DEEPSEEK_BASE_URL:https://api.deepseek.com}
  model: ${DEEPSEEK_MODEL:deepseek-chat}
  temperature: ${DEEPSEEK_TEMPERATURE:0.7}
  max-tokens: ${DEEPSEEK_MAX_TOKENS:2048}
```

- **api-key**: DeepSeek API 密钥
- **base-url**: API 基础 URL（默认使用官方地址）
- **model**: 使用的模型（默认 deepseek-chat）
- **temperature**: 生成文本的随机性（0-2，越高越随机）
- **max-tokens**: 最大生成 token 数

## 常见问题

### Q: 如何获取 DeepSeek API Key？
A: 访问 [DeepSeek 官网](https://platform.deepseek.com/) 注册账号并获取 API Key。

### Q: AI 回复速度慢怎么办？
A: 
1. 降低 `max-tokens` 值
2. 检查网络连接
3. 确认 API 配额充足

### Q: 如何自定义 AI 助手的行为？
A: 修改 `DeepSeekService.java` 中的 system prompt（系统提示词）。

## 性能优化建议

1. **流式响应**：当前实现为非流式响应，可以考虑实现流式响应以提升用户体验
2. **缓存机制**：对相似的用户问题可以增加缓存，减少 API 调用
3. **历史消息限制**：当前实现中限制只传递最近 20 条消息，可以根据需求调整
