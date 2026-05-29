import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/manager/Layout.vue'

const routes = [
  // 登录页面
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  // 注册页面
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  // 模型广场布局路由
  {
    path: '/square',
    component: () => import('../views/ModelSquare/SquareLayout.vue'),
    redirect: '/square/home',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'home',
        name: 'SquareHome',
        component: () => import('../views/ModelSquare/SquareHome.vue'),
        meta: { title: '模型广场', keepAlive: true }
      },
      {
        path: 'detail/:type/:id',
        name: 'SquareDetail',
        component: () => import('../views/ModelSquare/SquareDetail.vue'),
        meta: { title: '详情' }
      },
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('../views/ModelSquare/Messages.vue'),
        meta: { title: '消息' }
      },
      {
        path: 'chat/:userId',
        name: 'Chat',
        component: () => import('../views/ModelSquare/Chat.vue'),
        meta: { title: '聊天' }
      },
      {
        path: 'orders',
        name: 'MyOrders',
        component: () => import('../views/ModelSquare/MyOrders.vue'),
        meta: { title: '我的订单' }
      },
      {
        path: 'profile',
        name: 'MyProfile',
        component: () => import('../views/ModelSquare/MyProfile.vue'),
        meta: { title: '我的' }
      },
      {
        path: 'user/:userId',
        name: 'UserProfile',
        component: () => import('../views/ModelSquare/UserProfile.vue'),
        meta: { title: '用户主页' }
      }
    ]
  },
  // 主布局路由
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/manager/Dashboard.vue'),
        meta: { title: '数据统计' }
      },
      {
        path: 'models',
        name: 'ModelList',
        component: () => import('../views/manager/ModelList.vue'),
        meta: { title: '模型管理' }
      },
      {
        path: 'tools',
        name: 'ToolList',
        component: () => import('../views/manager/ToolList.vue'),
        meta: { title: '工具管理' }
      },
      {
        path: 'import',
        name: 'ExcelImport',
        component: () => import('../views/manager/ExcelImport.vue'),
        meta: { title: 'Excel 导入' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/manager/Profile.vue'),
        meta: { title: '个人信息' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - Model Share` : 'Model Share'
  
  // 检查是否需要认证
  if (to.meta.requiresAuth !== false) {
    // 需要认证的路由
    if (!token) {
      // 未登录，跳转到登录页
      next('/login')
    } else {
      next()
    }
  } else {
    // 不需要认证的路由（登录、注册）
    if (token && (to.path === '/login' || to.path === '/register')) {
      // 已登录用户访问登录/注册页，跳转到首页
      next('/dashboard')
    } else {
      next()
    }
  }
})

export default router
