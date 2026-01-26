<template>
  <div class="config-group">
    <el-table :data="configs" border stripe>
      <el-table-column prop="description" label="配置项" width="200">
        <template #default="{ row }">
          <div class="config-name">
            <span>{{ row.description || row.configKey }}</span>
            <el-tag v-if="row.encrypted" size="small" type="warning" style="margin-left: 8px">
              加密
            </el-tag>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="configKey" label="配置键" width="250" />
      
      <el-table-column label="配置值" min-width="300">
        <template #default="{ row }">
          <div class="config-value">
            <el-input
              v-if="editingKey === row.configKey"
              v-model="editingValue"
              :type="isPasswordField(row.configKey) ? 'password' : 'text'"
              placeholder="请输入配置值"
              show-password
            />
            <span v-else class="value-text">
              {{ displayValue(row) }}
            </span>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column label="更新时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.updatedAt) }}
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <div class="action-buttons">
            <template v-if="editingKey === row.configKey">
              <el-button type="primary" size="small" @click="handleSave(row)">
                保存
              </el-button>
              <el-button size="small" @click="handleCancel">
                取消
              </el-button>
            </template>
            <template v-else>
              <el-button type="primary" size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button 
                v-if="canTest(row.configKey)" 
                type="success" 
                size="small" 
                @click="handleTestClick(row.configKey)"
              >
                测试
              </el-button>
            </template>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface ConfigItem {
  configKey: string
  configValue: string
  configGroup: string
  description: string
  encrypted: boolean
  updatedAt: string
}

interface Props {
  configs: ConfigItem[]
}

const props = defineProps<Props>()
const emit = defineEmits<{
  update: [config: ConfigItem]
  test: [configKey: string]
}>()

const editingKey = ref<string | null>(null)
const editingValue = ref('')
const originalValue = ref('')

// 判断是否为密码字段
const isPasswordField = (key: string): boolean => {
  const passwordKeys = ['api.key', 'password', 'secret', 'token', 'access.key']
  return passwordKeys.some(k => key.toLowerCase().includes(k))
}

// 显示值（敏感信息脱敏）
const displayValue = (row: ConfigItem): string => {
  if (row.encrypted || isPasswordField(row.configKey)) {
    return '********'
  }
  return row.configValue || '未设置'
}

// 判断是否可以测试连接
const canTest = (key: string): boolean => {
  const testableKeys = [
    'spring.ai.openai.api-key',
    'spring.ai.openai.base-url',
    'spring.data.redis.host',
    'spring.datasource.url',
    'minio.endpoint',
    'amap.key'
  ]
  return testableKeys.includes(key)
}

// 格式化日期
const formatDate = (dateStr: string): string => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 编辑
const handleEdit = (row: ConfigItem) => {
  editingKey.value = row.configKey
  editingValue.value = row.configValue
  originalValue.value = row.configValue
}

// 保存
const handleSave = (row: ConfigItem) => {
  if (editingValue.value === originalValue.value) {
    editingKey.value = null
    return
  }
  
  emit('update', {
    ...row,
    configValue: editingValue.value
  })
  editingKey.value = null
}

// 取消
const handleCancel = () => {
  editingKey.value = null
  editingValue.value = ''
}

// 测试连接
const handleTestClick = (configKey: string) => {
  emit('test', configKey)
}
</script>

<style scoped>
.config-group {
  margin-top: 20px;
}

.config-name {
  display: flex;
  align-items: center;
}

.config-value {
  width: 100%;
}

.value-text {
  color: #606266;
  word-break: break-all;
}

.action-buttons {
  display: flex;
  gap: 8px;
}
</style>
