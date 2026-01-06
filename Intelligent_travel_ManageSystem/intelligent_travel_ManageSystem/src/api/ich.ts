import request from '@/utils/request'

// === 非遗项目管理 ===

// 分页查询非遗项目 (已修正为 listWithMedia)
export const getICHListAPI = (data: any) => {
  return request({
    url: '/ich/project/listWithMedia',
    method: 'POST',
    data
  })
}

// 新增非遗项目
export const addICHProjectAPI = (data: any) => {
  return request({
    url: '/ich/project/add',
    method: 'POST',
    data
  })
}

// 更新非遗项目
export const updateICHProjectAPI = (id: number, data: any) => {
  return request({
    url: `/ich/project/update/${id}`,
    method: 'PUT',
    data
  })
}

// 删除非遗项目
export const deleteICHProjectAPI = (id: number) => {
  return request({
    url: `/ich/project/${id}`,
    method: 'DELETE'
  })
}

// === 非遗媒体管理 ===

// 获取项目关联的媒体列表
export const getICHMediaListAPI = (projectId: number) => {
  return request({
    url: `/ich/media/project/${projectId}`,
    method: 'GET'
  })
}

// 新增媒体 (图片/视频)
export const addICHMediaAPI = (data: {
  projectId: number
  mediaType: 'image' | 'video'
  mediaUrl: string
  title?: string
}) => {
  return request({
    url: '/ich/media/add',
    method: 'POST',
    data
  })
}

// 删除媒体
export const deleteICHMediaAPI = (mediaId: number) => {
  return request({
    url: `/ich/media/${mediaId}`,
    method: 'DELETE'
  })
}