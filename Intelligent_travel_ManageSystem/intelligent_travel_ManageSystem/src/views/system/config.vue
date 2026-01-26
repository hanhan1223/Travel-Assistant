<template>
  <div class="config-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">系统配置管理</span>
          <el-button type="primary" @click="handleReload" :loading="reloadLoading">
            <el-icon><Refresh /></el-icon>
            重新加载配置
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="AI 配置" name="ai">
          <config-group 
            v-if="configGroups.ai" 
            :configs="configGroups.ai" 
            @update="handleUpdate"
            @test="handleTest"
          />
        </el-tab-pane>
        
        <el-tab-pane label="数据库配置" name="database">
          <config-group 
            v-if="configGroups.database" 
            :configs="configGroups.database" 
            @update="handleUpdate"
            @test="handleTest"
          />
        </el-tab-pane>
        
        <el-tab-pane label="缓存配置" name="cache">
          <config-group 
            v-if="configGroups.cache" 
            :configs="configGroups.cache" 
            @update="handleUpdate"
            @test="handleTest"
          />
        </el-tab-pane>
        
        <el-tab-pane label="存储配置" name="storage">
          <config-group 
            v-if="configGroups.storage" 
            :configs="configGroups.storage" 
            @update="handleUpdate"
            @test="handleTest"
          />
        </el-tab-pane>
        
        <el-tab-pane label="地图配置" name="map">
          <config-group 
            v-if="configGroups.map" 
            :configs="configGroups.map" 
            @update="handleUpdate"
            @test="handleTest"
          />
        </el-tab-pane>
        
        <el-tab-pane label="外部服务" name="external">
          <config-group 
            v-if="configGroups.external" 
            :configs="configGroups.external" 
            @update="handleUpdate"
            @test="handleTest"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import type { TabPaneName } from 'element-plus'
import { getAllConfigsAPI, updateConfigAPI, reloadConfigsAPI, testConnectionAPI } from '@/api/config'
import ConfigGroup from './components/ConfigGroup.vue'

interface ConfigItem {
  configKey: string
  configValue: string
  configGroup: string
  description: string
  encrypted: boolean
  updatedAt: string
}

const activeTab = ref('ai')
const reloadLoading = ref(false)
const configGroups = ref<Record<string, ConfigItem[]>>({
  ai: [],
  database: [],
  cache: [],
  storage: [],
  map: [],
  external: []
})

// 加载配置
const loadConfigs = async () => {
  try {
    const res: any = await getAllConfigsAPI()
    if (res.code === 0) {
      configGroups.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载配置失败')
  }
}

// 更新配置
const handleUpdate = async (config: ConfigItem) => {
  try {
    await ElMessageBox.confirm(
      `确定要更新配置 "${config.description || config.configKey}" 吗？`,
      '确认更新',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res: any = await updateConfigAPI({
      configKey: config.configKey,
      configValue: config.configValue,
      description: config.description
    })
    
    if (res.code === 0) {
      ElMessage.success('配置更新成功')
      await loadConfigs()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('配置更新失败')
    }
  }
}

// 测试连接
const handleTest = async (configKey: string) => {
  try {
    const res: any = await testConnectionAPI(configKey)
    if (res.code === 0) {
      const result = res.data
      if (result.success) {
        ElMessage.success(result.message || '连接测试成功')
      } else {
        ElMessage.error(result.message || '连接测试失败')
      }
    }
  } catch (error) {
    ElMessage.error('连接测试失败')
  }
}

// 重新加载配置
const handleReload = async () => {
  try {
    await ElMessageBox.confirm(
      '重新加载配置会使所有配置从数据库重新读取，确定要继续吗？',
      '确认重新加载',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    reloadLoading.value = true
    const res: any = await reloadConfigsAPI()
    if (res.code === 0) {
      ElMessage.success('配置重新加载成功')
      await loadConfigs()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('配置重新加载失败')
    }
  } finally {
    reloadLoading.value = false
  }
}

// 切换标签页
const handleTabChange = (tabName: TabPaneName) => {
  console.log('切换到标签页:', tabName)
}

onMounted(() => {
  loadConfigs()
})
</script>

<style scoped>
.config-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: 600;
}
</style>
