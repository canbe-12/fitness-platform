<template>
  <el-container class="app-shell">
    <el-aside width="240px" class="aside">
      <SideMenu />
    </el-aside>

    <el-container>
      <el-header class="header">
        <TopBar />
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import SideMenu from './SideMenu.vue'
import TopBar from './TopBar.vue'
</script>

<style scoped lang="scss">
.app-shell { height: 100%; }
.aside {
  background: #fff;
  border-right: 1px solid #eef2f7;
}
.header {
  background: #fff;
  border-bottom: 1px solid #eef2f7;
  display: flex;
  align-items: center;
}
.main {
  padding: 18px;
}
</style>
