<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, VideoPlay, Picture } from '@element-plus/icons-vue'
import type { UploadRequestOptions } from 'element-plus'
import { getICHMediaListAPI, addICHMediaAPI, deleteICHMediaAPI } from '@/api/ich'
import { uploadFileAPI } from '@/api/file'

// 接收父组件传入的 projectId
const props = defineProps<{
  projectId: number
}>()

const loading = ref(false)
const mediaList = ref<any[]>([])
const uploadLoading = ref(false)

// 获取媒体列表
const fetchMediaList = async () => {
  if (!props.projectId) return
  loading.value = true
  try {
    const res = await getICHMediaListAPI(props.projectId)
    mediaList.value = res.data
  } finally {
    loading.value = false
  }
}

// 监听 projectId 变化，自动刷新列表
watch(() => props.projectId, (newVal) => {
  if (newVal) fetchMediaList()
}, { immediate: true })

// 自定义上传逻辑
const handleUpload = async (options: UploadRequestOptions) => {
  uploadLoading.value = true
  try {
    // 1. 先上传文件到 COS/OSS
    const formData = new FormData()
    formData.append('file', options.file)
    const fileRes = await uploadFileAPI(formData)
    const fileUrl = fileRes.data // 假设后端返回 { code: 0, data: "url" }

    // 2. 判断类型
    const isVideo = options.file.type.startsWith('video/')
    
    // 3. 调用业务接口新增媒体记录
    await addICHMediaAPI({
      projectId: props.projectId,
      mediaType: isVideo ? 'video' : 'image',
      mediaUrl: fileUrl,
      title: options.file.name
    })

    ElMessage.success('上传成功')
    fetchMediaList() // 刷新列表
  } catch (error) {
    console.error(error)
    ElMessage.error('上传失败')
  } finally {
    uploadLoading.value = false
  }
}

// 删除媒体
const handleDelete = (item: any) => {
  ElMessageBox.confirm('确定删除该资源吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteICHMediaAPI(item.id)
    ElMessage.success('删除成功')
    fetchMediaList()
  })
}
</script>

<template>
  <div class="media-container" v-loading="loading">
    <div class="header">
      <el-upload
        action="#"
        :http-request="handleUpload"
        :show-file-list="false"
        accept="image/*,video/*"
        :disabled="uploadLoading"
      >
        <el-button type="primary" :icon="Plus" :loading="uploadLoading">上传图片/视频</el-button>
      </el-upload>
      <span class="tips">支持 jpg, png, mp4 格式</span>
    </div>

    <div v-if="mediaList.length > 0" class="media-grid">
      <div v-for="item in mediaList" :key="item.id" class="media-card">
        <div class="media-content">
          <el-image 
            v-if="item.mediaType === 'image'" 
            :src="item.mediaUrl" 
            fit="cover"
            class="media-img"
            :preview-src-list="[item.mediaUrl]"
            preview-teleported
          />
          <video 
            v-else 
            :src="item.mediaUrl" 
            class="media-video" 
            controls 
            preload="metadata"
          ></video>
          
          <div class="type-badge">
            <el-icon v-if="item.mediaType === 'video'"><VideoPlay /></el-icon>
            <el-icon v-else><Picture /></el-icon>
          </div>
        </div>
        
        <div class="media-footer">
          <span class="media-title" :title="item.title">{{ item.title || '无标题' }}</span>
          <el-button type="danger" :icon="Delete" circle size="small" @click="handleDelete(item)" />
        </div>
      </div>
    </div>
    
    <el-empty v-else description="暂无媒体资源，请点击上方按钮上传" />
  </div>
</template>

<style scoped>
.header {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
}
.tips {
  font-size: 12px;
  color: #999;
}
.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
  max-height: 500px;
  overflow-y: auto;
}
.media-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  transition: all 0.3s;
}
.media-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.media-content {
  position: relative;
  height: 150px;
  background: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
}
.media-img, .media-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.type-badge {
  position: absolute;
  top: 5px;
  right: 5px;
  background: rgba(0,0,0,0.6);
  color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}
.media-footer {
  padding: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #eee;
}
.media-title {
  font-size: 12px;
  color: #666;
  width: 140px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>