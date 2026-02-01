package com.movie.shop.api.movie.domain.aggregate;

import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import com.movie.shop.api.shared.domain.DomainValidator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Actor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false)
    private OffsetDateTime dateOfBirth;
    
    @Column(nullable = false, length = 100)
    private String national;
    
    @Column(nullable = false, length = 100)
    private String role;
    
    public Actor(String name, OffsetDateTime dateOfBirth, String national, String role) {
        DomainValidator.builder()
                // 이름 검증
                .notBlank(name, "배우 이름은 필수입니다.")
                .maxLength(name, 100, "배우 이름은 100자를 초과할 수 없습니다.")
                
                // 생년월일 검증
                .notNull(dateOfBirth, "배우의 생년월일은 필수입니다.")
                .validate(() -> dateOfBirth != null && dateOfBirth.isBefore(OffsetDateTime.now()),
                         "배우의 생년월일은 과거여야 합니다.")
                
                // 국적 검증
                .notBlank(national, "배우의 국적은 필수입니다.")
                .maxLength(national, 100, "배우의 국적은 100자를 초과할 수 없습니다.")
                
                // 역할 검증
                .notBlank(role, "배우의 역할은 필수입니다.")
                .maxLength(role, 100, "배우의 역할은 100자를 초과할 수 없습니다.")
                
                .throwIfInvalid(MovieDomainException::new);
        
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.national = national;
        this.role = role;
    }
}
