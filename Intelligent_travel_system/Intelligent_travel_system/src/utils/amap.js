// 请替换为你自己的高德 Web 服务 Key
// 注意：Key 需要在高德控制台开启 "Web服务" 权限
var AMAP_KEY = '231e20c40715a74a88291615f7c0576f';
/**
 * 生成高德静态地图 URL
 * @param lat 纬度
 * @param lng 经度
 * @param width 图片宽度
 * @param height 图片高度
 */
export var getStaticMapUrl = function (lat, lng, width, height) {
    if (width === void 0) { width = 400; }
    if (height === void 0) { height = 300; }
    // markers格式: size,font_style,label_letter:lng,lat
    var markers = "mid,,A:".concat(lng, ",").concat(lat);
    return "https://restapi.amap.com/v3/staticmap?location=".concat(lng, ",").concat(lat, "&zoom=14&size=").concat(width, "*").concat(height, "&markers=").concat(markers, "&key=").concat(AMAP_KEY);
};
/**
 * 唤起高德地图导航 (H5 URL Scheme)
 * 如果用户没装 App，可以降级跳转到 Web 版 (这里简化为直接跳 URL Scheme)
 */
export var openNavigation = function (lat, lng, name) {
    // 唤起高德地图 App 导航
    // iOS/Android 通用 Scheme
    var url = "https://uri.amap.com/navigation?to=".concat(lng, ",").concat(lat, ",").concat(encodeURIComponent(name), "&mode=car&callnative=1");
    window.location.href = url;
};
