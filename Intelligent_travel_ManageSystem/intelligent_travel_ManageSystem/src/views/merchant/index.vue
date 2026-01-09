<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { 
  getMerchantListAPI, 
  addMerchantAPI, 
  updateMerchantAPI, 
  deleteMerchantAPI,
  getTopMerchantsAPI 
} from '@/api/merchant'
import { getICHListAPI } from '@/api/ich' 
import MapPicker from '@/components/MapPicker/index.vue'
import AMapLoader from '@amap/amap-jsapi-loader'

// === 高德地图配置 ===
const AMAP_KEY = 'ae49d6da7c2b2e512cfd0eee52a8e84a'           
const AMAP_SECURITY_CODE = 'b53b60a2ff86751a31550af6a570fc7b' 

// @ts-ignore
window._AMapSecurityConfig = { securityJsCode: AMAP_SECURITY_CODE }

// === 数据定义 ===
const loading = ref(false)
const tableData = ref<any[]>([]) // 当前页显示的数据
const allTopData = ref<any[]>([]) // 缓存所有高分数据(用于前端分页)
const total = ref(0)
const projectOptions = ref<any[]>([]) 
const computedAddresses = reactive<Record<number, string>>({}) 

const queryParams = reactive({
  current: 1,
  pageSize: 10,
  name: '',
  category: '',
  projectId: undefined as number | undefined,
  sortType: 'default' as 'default' | 'rating' // 排序方式
})

// 表单相关
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const submitLoading = ref(false)
const formRef = ref()

const formData = reactive({
  id: undefined as number | undefined,
  name: '',
  category: '',
  projectId: undefined as number | undefined,
  address: '',
  phone: '',
  lat: undefined as number | undefined,
  lng: undefined as number | undefined,
  rating: 5.0,
  relevanceScore: 80
})

const categoryOptions = ['体验馆', '文创店', '老字号', '博物馆', '研学基地']

const rules = {
  name: [{ required: true, message: '请输入商户名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择商户类别', trigger: 'change' }]
}

// === 地图服务 ===
let geocoder: any = null

const initGeocoder = async () => {
  try {
    const AMap = await AMapLoader.load({
      key: AMAP_KEY,
      version: '2.0',
      plugins: ['AMap.Geocoder']
    })
    geocoder = new AMap.Geocoder()
    if (tableData.value.length > 0) {
      tableData.value.forEach(row => resolveAddress(row))
    }
  } catch (e) {
    console.error('地图加载失败', e)
  }
}

const resolveAddress = (row: any) => {
  if (row.address) return row.address
  if (computedAddresses[row.id]) return computedAddresses[row.id]
  
  if (row.lat && row.lng) {
    if (!computedAddresses[row.id]) computedAddresses[row.id] = '地址加载中...'
    if (geocoder) {
      geocoder.getAddress([row.lng, row.lat], (status: string, result: any) => {
        if (status === 'complete' && result.regeocode) {
          computedAddresses[row.id] = result.regeocode.formattedAddress
        } else {
          computedAddresses[row.id] = '地址解析失败'
        }
      })
      return computedAddresses[row.id]
    }
  }
  return '暂无坐标'
}

// === 核心业务逻辑 ===

const getProjectList = async () => {
  try {
    const res = await getICHListAPI({ current: 1, pageSize: 1000 })
    projectOptions.value = res.data.records
  } catch (error) {
    console.error(error)
  }
}

// 获取数据入口
const getList = async () => {
  loading.value = true
  try {
    if (queryParams.sortType === 'default') {
      // === 模式1: 后端分页 (默认) ===
      const res = await getMerchantListAPI({
        current: queryParams.current,
        pageSize: queryParams.pageSize,
        name: queryParams.name,
        category: queryParams.category,
        projectId: queryParams.projectId
      })
      tableData.value = res.data.records
      total.value = typeof res.data.total === 'string' ? parseInt(res.data.total) : res.data.total
    } else {
      // === 模式2: 评分排序 (使用 /merchant/top 接口 + 前端分页) ===
      // 注意: 这里 limit 设为 100，模拟获取"全部"高分数据
      const res = await getTopMerchantsAPI(100) 
      allTopData.value = res.data || [] // 存储所有数据
      total.value = allTopData.value.length // 设置总条数
      
      // 执行前端分页切割
      updateFrontendPagination()
    }
    
    // 解析地址
    if (geocoder) {
      tableData.value.forEach(row => resolveAddress(row))
    }
  } finally {
    loading.value = false
  }
}

// 前端分页切割逻辑
const updateFrontendPagination = () => {
  if (queryParams.sortType === 'rating') {
    const start = (queryParams.current - 1) * queryParams.pageSize
    const end = start + queryParams.pageSize
    // 从缓存的全量数据中切出当前页
    tableData.value = allTopData.value.slice(start, end)
    
    if (geocoder) {
      tableData.value.forEach(row => resolveAddress(row))
    }
  }
}

// 搜索/重置/切页
const handleSearch = () => {
  queryParams.current = 1
  getList()
}

const handleReset = () => {
  queryParams.name = ''
  queryParams.category = ''
  queryParams.projectId = undefined
  queryParams.sortType = 'default' // 重置回默认排序
  handleSearch()
}

const handleCurrentChange = (val: number) => {
  queryParams.current = val
  if (queryParams.sortType === 'default') {
    getList() // 后端分页: 重新请求
  } else {
    updateFrontendPagination() // 前端分页: 直接切割
  }
}

const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  queryParams.current = 1 
  if (queryParams.sortType === 'default') {
    getList()
  } else {
    updateFrontendPagination()
  }
}

// 编辑/新增逻辑
const openDialog = (type: 'add' | 'edit', row?: any) => {
  dialogType.value = type
  dialogVisible.value = true
  
  if (type === 'edit' && row) {
    Object.assign(formData, row)
    formData.projectId = row.projectId ? Number(row.projectId) : undefined
  } else {
    formData.id = undefined
    formData.name = ''
    formData.category = ''
    formData.projectId = undefined
    formData.address = ''
    formData.phone = ''
    formData.lat = undefined
    formData.lng = undefined
    formData.rating = 5.0
    formData.relevanceScore = 80
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      if (!formData.lat || !formData.lng) {
        ElMessage.warning('请在地图上点击选择商户位置')
        return
      }
      
      submitLoading.value = true
      try {
        if (dialogType.value === 'add') {
          await addMerchantAPI(formData)
          ElMessage.success('新增成功')
        } else {
          await updateMerchantAPI(formData.id!, formData)
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        getList() // 刷新列表
      } catch (error: any) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该商户吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteMerchantAPI(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

const getProjectName = (id: number) => {
  if (!id) return ''
  const p = projectOptions.value.find(item => item.id === id)
  return p ? p.name : `ID:${id}`
}

onMounted(() => {
  getProjectList()
  initGeocoder()
  getList()
})
</script>

<template>
  <div class="app-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="商户名称">
          <el-input v-model="queryParams.name" placeholder="支持模糊搜索" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="queryParams.category" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联项目">
          <el-select v-model="queryParams.projectId" placeholder="全部" clearable filterable style="width: 140px">
            <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序方式">
          <el-select 
            v-model="queryParams.sortType" 
            placeholder="默认" 
            style="width: 140px" 
            @change="handleSearch"
          >
            <el-option label="默认排序" value="default" />
            <el-option label="评分: 高→低" value="rating" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="openDialog('add')">新增商户</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        
        <el-table-column prop="name" label="商户名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="category" label="类别" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="关联项目" width="150" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.projectId" effect="plain">{{ getProjectName(row.projectId) }}</el-tag>
            <span v-else style="color:#999; font-size:12px">暂无</span>
          </template>
        </el-table-column>

        <el-table-column prop="rating" label="评分" width="100" align="center" sortable="custom">
          <template #default="{ row }">
             <div style="display: flex; align-items: center; justify-content: center; gap: 5px;">
               <span style="color: #ff9900; font-weight: bold; font-size: 16px;">{{ row.rating }}</span>
               <span style="color: #999; font-size: 12px;">分</span>
             </div>
          </template>
        </el-table-column>

        <el-table-column label="地址" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ computedAddresses[row.id] || row.address || '暂无' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openDialog('edit', row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增商户' : '编辑商户'" width="700px" top="5vh">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商户名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入店铺名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
             <el-form-item label="联系电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="类别" prop="category">
              <el-select v-model="formData.category" style="width: 100%">
                <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectId">
              <el-select 
                v-model="formData.projectId" 
                placeholder="请选择关联非遗项目" 
                style="width: 100%" 
                filterable 
                clearable
              >
                <el-option 
                  v-for="item in projectOptions" 
                  :key="item.id" 
                  :label="item.name" 
                  :value="item.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="评分">
              <el-input-number v-model="formData.rating" :min="0" :max="5" :precision="1" :step="0.1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联度">
              <el-input-number v-model="formData.relevanceScore" :min="0" :max="100" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="详细地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入详细地址" />
        </el-form-item>

        <el-form-item label="地图定位" required>
          <MapPicker 
            v-model:lat="formData.lat" 
            v-model:lng="formData.lng" 
            :search-address="formData.address"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.app-container { padding: 20px; background: #f0f2f5; min-height: calc(100vh - 84px); }
.search-card { margin-bottom: 20px; }
.toolbar { margin-bottom: 20px; display: flex; gap: 10px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>