package org.example.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.travel.model.entity.EnvSnapshot;
import org.example.travel.service.EnvSnapshotService;
import org.example.travel.mapper.EnvSnapshotMapper;
import org.springframework.stereotype.Service;

/**
* @author 13763
* @description 针对表【env_snapshot(环境快照表)】的数据库操作Service实现
* @createDate 2026-01-01 16:40:25
*/
@Service
public class EnvSnapshotServiceImpl extends ServiceImpl<EnvSnapshotMapper, EnvSnapshot>
    implements EnvSnapshotService{

}




