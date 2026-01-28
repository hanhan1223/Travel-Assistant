package org.example.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.constants.CacheConstants;
import org.example.travel.model.entity.Merchant;
import org.example.travel.service.CacheService;
import org.example.travel.service.MerchantService;
import org.example.travel.mapper.MerchantMapper;
import org.springframework.stereotype.Service;

/**
* @author 13763
* @description 针对表【merchant(商户表：非遗体验工坊、文创店、老字号)】的数据库操作Service实现
* @createDate 2026-01-01 17:06:08
*/
@Service
@Slf4j
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant>
    implements MerchantService{

    @Resource
    private CacheService cacheService;
    
    @Override
    public Merchant getById(java.io.Serializable id) {
        if (id == null) {
            return null;
        }
        
        String cacheKey = CacheConstants.buildKey(CacheConstants.MERCHANT_PREFIX, id);
        return cacheService.get(
            cacheKey,
            () -> super.getById(id),
            CacheConstants.MERCHANT_TIMEOUT,
            CacheConstants.MERCHANT_UNIT,
            Merchant.class
        );
    }
    
    @Override
    public boolean save(Merchant merchant) {
        boolean saved = super.save(merchant);
        if (saved && merchant.getId() != null) {
            // 缓存新商户
            String cacheKey = CacheConstants.buildKey(CacheConstants.MERCHANT_PREFIX, merchant.getId());
            cacheService.set(cacheKey, merchant, CacheConstants.MERCHANT_TIMEOUT, CacheConstants.MERCHANT_UNIT);
        }
        return saved;
    }
    
    @Override
    public boolean updateById(Merchant merchant) {
        boolean updated = super.updateById(merchant);
        if (updated && merchant.getId() != null) {
            // 清除缓存
            String cacheKey = CacheConstants.buildKey(CacheConstants.MERCHANT_PREFIX, merchant.getId());
            cacheService.delete(cacheKey);
        }
        return updated;
    }
    
    @Override
    public boolean removeById(java.io.Serializable id) {
        boolean removed = super.removeById(id);
        if (removed) {
            // 清除缓存
            String cacheKey = CacheConstants.buildKey(CacheConstants.MERCHANT_PREFIX, id);
            cacheService.delete(cacheKey);
        }
        return removed;
    }
}




