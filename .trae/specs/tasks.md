# AI 助手功能 - 实现计划（分解与优先级任务列表）

## [ ] 任务 1: 数据库设计与迁移
- **优先级**: P0
- **依赖**: 无
- **描述**: 
  - 设计对话会话表（ai_chat_session），存储用户的对话会话
  - 设计对话消息表（ai_chat_message），存储具体的对话消息
  - 创建 Flyway 数据库迁移脚本
- **验收标准对应**: AC-3
- **测试要求**:
  - programmatic: 数据库表能够正确创建
  - programmatic: 能够插入和查询对话数据
- **注释**: 遵循项目现有的数据库迁移规范

## [ ] 任务 2: 后端 DeepSeek API 集成
- **优先级**: P0
- **依赖**: 无
- **描述**: 
  - 添加 Spring WebClient 或 RestTemplate 依赖（如果需要）
  - 创建 DeepSeek API 配置类（DeepSeekConfig）
  - 创建 DeepSeek API 服务类（DeepSeekService）
  - 实现 API 调用逻辑，包括请求封装和响应解析
  - 添加错误处理和重试机制
  - 在 application.yml 中添加 DeepSeek 配置项
- **验收标准对应**: AC-2, AC-6
- **测试要求**:
  - programmatic: 能够成功调用 DeepSeek API 并获取响应
  - programmatic: 错误情况下能够正确处理异常

## [ ] 任务 3: 后端实体类、DTO、VO、Mapper、Service
- **优先级**: P0
- **依赖**: 任务 1
- **描述**: 
  - 创建对话会话实体类（AiChatSession）
  - 创建对话消息实体类（AiChatMessage）
  - 创建相关的 DTO（如消息发送请求 DTO）
  - 创建相关的 VO（如会话列表 VO、消息 VO）
  - 创建 Mapper 接口
  - 创建 Service 接口和实现类（AiChatService）
- **验收标准对应**: AC-3, AC-4
- **测试要求**:
  - programmatic: Service 层能够正确保存和查询对话数据

## [ ] 任务 4: 后端控制器实现
- **优先级**: P0
- **依赖**: 任务 2, 任务 3
- **描述**: 
  - 创建 AiChatController 控制器
  - 实现以下 API 端点：
    - GET /api/ai-chat/sessions - 获取用户的对话会话列表
    - POST /api/ai-chat/sessions - 创建新会话
    - DELETE /api/ai-chat/sessions/{id} - 删除会话
    - GET /api/ai-chat/sessions/{id}/messages - 获取会话的消息列表
    - POST /api/ai-chat/sessions/{id}/messages - 发送消息并获取 AI 回复
- **验收标准对应**: AC-2, AC-3, AC-4
- **测试要求**:
  - programmatic: API 端点能够正确响应请求
  - programmatic: 需要用户认证才能访问 API

## [ ] 任务 5: 前端 API 封装
- **优先级**: P1
- **依赖**: 任务 4
- **描述**: 
  - 创建前端 API 文件（aiChat.js）
  - 封装所有 AI 聊天相关的 API 调用
- **验收标准对应**: AC-2, AC-3, AC-4
- **测试要求**:
  - programmatic: API 封装能够正确调用后端接口

## [ ] 任务 6: 前端聊天界面组件
- **优先级**: P1
- **依赖**: 任务 5
- **描述**: 
  - 创建 AiChatAssistant.vue 组件
  - 实现会话列表侧边栏
  - 实现消息展示区域
  - 实现消息输入和发送功能
  - 实现加载状态和错误提示
  - 样式与现有系统保持一致
- **验收标准对应**: AC-1, AC-2, AC-6
- **测试要求**:
  - human-judgment: 界面美观且功能完整
  - programmatic: 消息能够正确发送和显示

## [ ] 任务 7: 全局 AI 助手入口
- **优先级**: P1
- **依赖**: 任务 6
- **描述**: 
  - 在管理后台 Layout.vue 中添加 AI 助手入口
  - 在模型广场 SquareLayout.vue 中添加 AI 助手入口
  - 实现悬浮按钮或可展开/收起的聊天窗口
- **验收标准对应**: AC-5
- **测试要求**:
  - human-judgment: 入口易于找到和使用
  - human-judgment: 聊天窗口可以正常展开和收起

## [ ] 任务 8: 前端 Pinia 状态管理（可选）（需要）
- **优先级**: P2
- **依赖**: 任务 5
- **描述**: 
  - 创建 AI 聊天相关的 Pinia store（可选，如果需要跨组件共享状态）
- **验收标准对应**: AC-3
- **测试要求**:
  - programmatic: 状态管理工作正常

## [ ] 任务 9: 测试与优化
- **优先级**: P2
- **依赖**: 任务 6, 任务 7
- **描述**: 
  - 进行完整的功能测试
  - 优化用户体验
  - 性能优化
  - 边界情况测试
- **验收标准对应**: 所有 AC
- **测试要求**:
  - human-judgment: 整体功能流畅可用
  - programmatic: 所有核心功能正常工作
