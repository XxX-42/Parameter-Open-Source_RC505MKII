// 从 'vue' 库中导入 createApp 函数，用于创建 Vue 应用实例
import { createApp } from "vue";

// 导入 App.vue 组件，它是应用的根组件
import App from "./App.vue";

// 创建 Vue 应用实例并挂载到 HTML 页面中的元素 #app 上
createApp(App).mount('#app');
