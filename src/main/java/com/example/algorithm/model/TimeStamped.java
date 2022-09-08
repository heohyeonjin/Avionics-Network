package com.example.algorithm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
public abstract class TimeStamped {
    // 최초 생성 시점
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate   // 최종 변경 시점
    private LocalDateTime modifiedAt;

}
