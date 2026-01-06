<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import AMapLoader from '@amap/amap-jsapi-loader' 

// props 接收初始坐标
const props = defineProps<{
  lat?: number
  lng?: number
  searchAddress?: string
}>()

const emit = defineEmits(['update:lat', 'update:lng', 'select'])

const mapContainer = ref<HTMLElement | null>(null)
let map: any = null
let marker: any = null

// === ⚠️⚠️⚠️ 保持与 Merchant/Index 一致 ⚠️⚠️⚠️ ===
const AMAP_KEY = 'ae49d6da7c2b2e512cfd0eee52a8e84a' 
const AMAP_SECURITY_CODE = 'b53b60a2ff86751a31550af6a570fc7b'

// 初始化地图
const initMap = async () => {
  try {
    // 1. 配置安全密钥
    ;(window as any)._AMapSecurityConfig = {
      securityJsCode: AMAP_SECURITY_CODE,
    }

    // 2. 加载地图 (统一使用 AMapLoader，避免重复加载冲突)
    const AMap = await AMapLoader.load({
      key: AMAP_KEY,
      version: '2.0',
      plugins: ['AMap.Marker', 'AMap.Geocoder'] 
    })
    
    // 3. 渲染地图
    const center = (props.lng && props.lat) ? [props.lng, props.lat] : [113.2644, 23.1291]
    
    map = new AMap.Map(mapContainer.value, {
      zoom: 13,
      center: center,
    })

    // 4. 如果有初始坐标，添加标记
    if (props.lng && props.lat) {
      addMarker(AMap, props.lng, props.lat)
    }

    // 5. 地图点击事件
    map.on('click', (e: any) => {
      const { lng, lat } = e.lnglat
      addMarker(AMap, lng, lat)
      emit('update:lng', lng)
      emit('update:lat', lat)
      emit('select', { lng, lat })
      ElMessage.success(`已选中坐标: ${lng.toFixed(6)}, ${lat.toFixed(6)}`)
    })

  } catch (error) {
    console.error('地图加载失败', error)
    ElMessage.error('地图加载失败，请检查 Key')
  }
}

// 添加/移动标记
const addMarker = (AMap: any, lng: number, lat: number) => {
  if (!marker) {
    marker = new AMap.Marker({
      position: [lng, lat],
      map: map
    })
  } else {
    marker.setPosition([lng, lat])
  }
}

// 监听 props 变化
watch(() => [props.lng, props.lat], ([newLng, newLat]) => {
  if (newLng && newLat && map) {
    const AMap = (window as any).AMap
    if(AMap) {
       addMarker(AMap, newLng as number, newLat as number)
       map.setCenter([newLng, newLat])
    }
  }
})

onMounted(() => {
  initMap()
})

onUnmounted(() => {
  if (map) {
    map.destroy()
  }
})
</script>

<template>
  <div class="map-picker-container">
    <div ref="mapContainer" class="map-container"></div>
    <div class="tips">
      <el-tag type="info" size="small">操作提示</el-tag>
      <span style="font-size: 12px; margin-left: 8px; color: #666;">
        点击地图任意位置即可选取坐标；滚动鼠标缩放地图。
      </span>
    </div>
  </div>
</template>

<style scoped>
.map-picker-container {
  width: 100%;
  height: 350px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  position: relative;
}
.map-container {
  width: 100%;
  height: 100%;
}
.tips {
  position: absolute;
  bottom: 10px;
  left: 10px;
  background: rgba(255, 255, 255, 0.9);
  padding: 5px 10px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
</style>