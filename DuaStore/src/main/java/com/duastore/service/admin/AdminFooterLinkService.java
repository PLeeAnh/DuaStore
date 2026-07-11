package com.duastore.service.admin;

import com.duastore.dto.FooterLinkDTO;
import com.duastore.model.FooterLink;
import com.duastore.repository.FooterLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminFooterLinkService {

    private final FooterLinkRepository footerLinkRepository;

    public AdminFooterLinkService(FooterLinkRepository footerLinkRepository) {
        this.footerLinkRepository = footerLinkRepository;
    }

    @Transactional(readOnly = true)
    public List<FooterLinkDTO> findAll() {
        return footerLinkRepository.findAllByOrderByColumnIndexAscDisplayOrderAsc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FooterLinkDTO findById(Integer id) {
        FooterLink entity = footerLinkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy liên kết"));
        return toDTO(entity);
    }

    public FooterLinkDTO save(FooterLinkDTO dto) {
        FooterLink entity = new FooterLink();
        if (dto.getId() != null) {
            entity = footerLinkRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy liên kết"));
        }
        entity.setTitle(dto.getTitle());
        entity.setUrl(dto.getUrl());
        entity.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0);
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        entity.setColumnIndex(dto.getColumnIndex() != null ? dto.getColumnIndex() : 1);
        entity = footerLinkRepository.save(entity);
        return toDTO(entity);
    }

    public void delete(Integer id) {
        FooterLink entity = footerLinkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy liên kết"));
        footerLinkRepository.delete(entity);
    }

    private FooterLinkDTO toDTO(FooterLink entity) {
        FooterLinkDTO dto = new FooterLinkDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setUrl(entity.getUrl());
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setIsActive(entity.getIsActive());
        dto.setColumnIndex(entity.getColumnIndex());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
