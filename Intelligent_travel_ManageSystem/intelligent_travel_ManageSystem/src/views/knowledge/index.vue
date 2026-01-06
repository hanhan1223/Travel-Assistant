<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadProps, UploadUserFile } from 'element-plus'
import { Search, Upload, Refresh, Document } from '@element-plus/icons-vue'

import { 
  getKnowledgeListAPI, 
  uploadKnowledgeAPI, 
  deleteKnowledgeAPI, 
  revectorizeKnowledgeAPI,
  updateKnowledgeAPI
} from '@/api/knowledge'
import { getICHListAPI } from '@/api/ich'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const projectOptions = ref<any[]>([]) 

const queryParams = reactive({
  current: 1,
  pageSize: 10,
  title: '',
  projectId: undefined as number | undefined
})

const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const submitLoading = ref(false)
const formRef = ref()

const formData = reactive({
  id: undefined as number | undefined,
  title: '',
  projectId: undefined as number | undefined,
  file: null as File | null
})

const fileList = ref<UploadUserFile[]>([])

const rules = {
  title: [{ required: true, message: '请输入文档标题', trigger: 'blur' }],
  projectId: [{ required: true, message: '请选择关联非遗项目', trigger: 'change' }]
}

// 辅助方法：获取项目名称
const getProjectName = (projectId: number) => {
  const project = projectOptions.value.find(p => p.id === projectId)
  return project ? project.name : null
}

const getProjectList = async () => {
  try {
    const res = await getICHListAPI({ current: 1, pageSize: 100 })
    projectOptions.value = res.data.records
  } catch (error) {
    console.error('获取项目列表失败', error)
  }
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getKnowledgeListAPI(queryParams)
    tableData.value = res.data.records
    total.value = typeof res.data.total === 'string' ? parseInt(res.data.total) : res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.current = 1
  getList()
}
const handleReset = () => {
  queryParams.title = ''
  queryParams.projectId = undefined
  handleSearch()
}

const openDialog = (type: 'add' | 'edit', row?: any) => {
  dialogType.value = type
  dialogVisible.value = true
  fileList.value = []
  
  if (type === 'edit' && row) {
    formData.id = row.id
    formData.title = row.title
    formData.projectId = row.projectId
    formData.file = null 
  } else {
    formData.id = undefined
    formData.title = ''
    formData.projectId = undefined
    formData.file = null
  }
}

const handleFileChange: UploadProps['onChange'] = (uploadFile) => {
  if (uploadFile.raw) {
    formData.file = uploadFile.raw
    if (!formData.title) {
      const name = uploadFile.name
      formData.title = name.substring(0, name.lastIndexOf('.'))
    }
  }
  if (fileList.value.length > 1) {
    fileList.value.splice(0, 1)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      if (dialogType.value === 'add' && !formData.file) {
        ElMessage.warning('请上传文档文件')
        return
      }
      submitLoading.value = true
      try {
        const payload = new FormData()
        payload.append('title', formData.title)
        if (formData.projectId) payload.append('projectId', formData.projectId.toString())
        if (formData.file) payload.append('file', formData.file)

        if (dialogType.value === 'add') {
          await uploadKnowledgeAPI(payload)
          ElMessage.success('上传成功')
        } else {
          if (formData.id) {
            await updateKnowledgeAPI(formData.id, payload)
            ElMessage.success('更新成功')
          }
        }
        dialogVisible.value = false
        getList()
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除吗？', '提示').then(async () => {
    await deleteKnowledgeAPI(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

const handleRevectorize = async (row: any) => {
  try {
    loading.value = true
    await revectorizeKnowledgeAPI(row.id)
    ElMessage.success('已触发重向量化')
    getList()
  } finally {
    loading.value = false
  }
}

const handleCurrentChange = (val: number) => {
  queryParams.current = val
  getList()
}
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  queryParams.current = 1
  getList()
}

// 修复：状态码映射改为数字 (0-待处理 1-处理中 2-已完成 3-失败)
const getStatusTag = (status: number) => {
  const map: Record<number, { type: string, label: string }> = {
    2: { type: 'success', label: '已完成' },
    1: { type: 'warning', label: '处理中' },
    3: { type: 'danger', label: '失败' },
    0: { type: 'info', label: '待处理' }
  }
  return map[status] || { type: 'info', label: '未知' }
}

onMounted(() => {
  getProjectList()
  getList()
})
</script>

<template>
  <div class="app-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="文档标题">
          <el-input v-model="queryParams.title" placeholder="请输入标题关键词" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="关联项目">
          <el-select v-model="queryParams.projectId" placeholder="选择关联项目" clearable style="width: 200px">
            <el-option
              v-for="item in projectOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <el-button type="primary" :icon="Upload" @click="openDialog('add')">上传新文档</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="文档信息" min-width="200">
          <template #default="{ row }">
            <div style="display: flex; align-items: center;">
              <el-icon class="mr-2"><Document /></el-icon>
              <span style="margin-left: 8px; font-weight: 500;">{{ row.title }}</span>
            </div>
            <div style="font-size: 12px; color: #999; margin-left: 24px;">文件名: {{ row.fileUrl ? row.fileUrl.split('/').pop() : '未知' }}</div>
          </template>
        </el-table-column>
        
        <el-table-column label="关联项目" width="150" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.projectId && getProjectName(row.projectId)" effect="plain">
              {{ getProjectName(row.projectId) }}
            </el-tag>
            <span v-else style="color: #999; font-size: 12px;">暂无</span>
          </template>
        </el-table-column>

        <el-table-column label="切片状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.vectorStatus).type as any">{{ getStatusTag(row.vectorStatus).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="chunkCount" label="切片数" width="100" align="center" />
        <el-table-column prop="createdAt" label="上传时间" width="180" align="center" />
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog('edit', row)">编辑</el-button>
            <el-button link type="warning" @click="handleRevectorize(row)">重新向量化</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '上传文档' : '编辑文档'" width="500px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="文档标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入文档展示标题" />
        </el-form-item>
        <el-form-item label="关联项目" prop="projectId">
          <el-select v-model="formData.projectId" placeholder="请选择关联非遗项目" style="width: 100%" clearable>
            <el-option
              v-for="item in projectOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="文档文件" :required="dialogType === 'add'">
          <el-upload
            ref="uploadRef"
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :file-list="fileList"
            :limit="1"
            accept=".pdf,.doc,.docx,.txt,.md"
          >
            <template #trigger><el-button type="primary">选择文件</el-button></template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.app-container { padding: 20px; background-color: #f0f2f5; min-height: calc(100vh - 84px); }
.search-card, .table-card, .toolbar { margin-bottom: 20px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>