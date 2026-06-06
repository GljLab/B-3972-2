<template>
  <div class="min-h-screen flex flex-col bg-gray-50">
    <header class="bg-indigo-600 text-white shadow relative z-10">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16 items-center">
          <div class="flex items-center gap-2">
            <el-icon :size="24"><DataLine /></el-icon>
            <h1 class="text-xl font-bold tracking-tight">三方校验式库存与工资核算系统</h1>
          </div>
          <nav class="flex gap-4">
            <router-link to="/dashboard" class="px-3 py-2 rounded-md transition hover:bg-indigo-500 font-medium" active-class="bg-indigo-700">业务校验大盘</router-link>
            <router-link to="/entry" class="px-3 py-2 rounded-md transition hover:bg-indigo-500 font-medium" active-class="bg-indigo-700">数据录入模块</router-link>
            <router-link to="/exception" class="px-3 py-2 rounded-md transition hover:bg-indigo-500 font-medium" active-class="bg-indigo-700">异常管理</router-link>
          </nav>
        </div>
      </div>
    </header>

    <main class="flex-grow max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
</script>

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
