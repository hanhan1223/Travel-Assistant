package org.example.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.travel.model.entity.Merchant;
import org.example.travel.service.MerchantService;
import org.example.travel.mapper.MerchantMapper;
import org.springframework.stereotype.Service;

/**
* @author 13763
* @description 针对表【merchant(商户表：非遗体验工坊、文创店、老字号)】的数据库操作Service实现
* @createDate 2026-01-01 17:06:08
*/
@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant>
    implements MerchantService{

}




