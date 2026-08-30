package com.saas.media_core.entity;

import com.saas.media_core.enums.Algorithm;
import com.saas.media_core.enums.ServiceType;
import com.saas.media_core.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "tasks")
@Getter
@Setter
@ToString(exclude = "user")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Algorithm algorithm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "input_payload", nullable = false, columnDefinition = "TEXT")
    private String inputPayload;

    @Column(name = "output_payload", columnDefinition = "TEXT")
    private String outputPayload;

    
    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "original_size")
    private Long originalSize;

    @Column(name = "compressed_size")
    private Long compressedSize;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}