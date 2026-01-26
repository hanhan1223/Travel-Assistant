/**
 * 生成高德静态地图 URL
 * @param lat 纬度
 * @param lng 经度
 * @param width 图片宽度
 * @param height 图片高度
 */
export declare const getStaticMapUrl: (lat: number, lng: number, width?: number, height?: number) => string;
/**
 * 唤起高德地图导航 (H5 URL Scheme)
 * 如果用户没装 App，可以降级跳转到 Web 版 (这里简化为直接跳 URL Scheme)
 */
export declare const openNavigation: (lat: number, lng: number, name: string) => void;
