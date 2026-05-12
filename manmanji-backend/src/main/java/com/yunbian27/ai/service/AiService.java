package com.yunbian27.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunbian27.ai.dto.LlmProviderVO;
import com.yunbian27.ai.entity.LlmProviderEntity;
import com.yunbian27.ai.mapper.LlmProviderMapper;
import com.yunbian27.common.exception.BusinessException;
import com.yunbian27.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final LlmProviderMapper llmProviderMapper;

    public List<LlmProviderVO> listProviders() {
        List<LlmProviderEntity> entities = llmProviderMapper.selectList(
                new LambdaQueryWrapper<LlmProviderEntity>()
                        .orderByAsc(LlmProviderEntity::getId)
        );
        return entities.stream().map(this::toVO).toList();
    }

    public LlmProviderVO getProvider(String id) {
        LlmProviderEntity entity = llmProviderMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.PROVIDER_NOT_FOUND);
        }
        return toVO(entity);
    }

    private LlmProviderVO toVO(LlmProviderEntity entity) {
        LlmProviderVO vo = new LlmProviderVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setHasApiKey(entity.getApiKeyCiphertext() != null
                && !entity.getApiKeyCiphertext().isBlank());
        return vo;
    }
}
