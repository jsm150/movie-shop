package com.movie.shop.api.theater.domain.aggregate;

import com.movie.shop.api.shared.domain.EntityValidator;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import com.movie.shop.api.theater.domain.policy.TheaterAuditoriumLinkProtectionPolicy;
import com.movie.shop.api.theater.domain.policy.TheaterNamePolicy;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Theater {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "theater_id", nullable = false)
    private long id;

    @Setter(AccessLevel.PRIVATE)
    @AttributeOverride(
            name = "name",
            column = @Column(name = "name", unique = true, nullable = false, length = 50)
    )
    @Embedded
    private TheaterName name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public static Theater register(TheaterNamePolicy nameDuplicateValidator, String name) {
        var theater = new Theater();
        theater.active = true;

        EntityValidator.create()
                .apply(TheaterName.createNew(name, nameDuplicateValidator), theater::setName)
                .validateBean(theater)
                .throwIfInvalid(TheaterDomainException::new);

        return theater;
    }

    public void updateName(TheaterNamePolicy nameDuplicateValidator, String name) {
        EntityValidator.create()
                .apply(TheaterName.createFrom(this.name, name, nameDuplicateValidator), this::setName)
                .throwIfInvalid(TheaterDomainException::new);
    }

    private void deactivate() {
        active = false;
    }

    private void activate() {
        active = true;
    }

    public void changeActive(TheaterActiveChange activeChange, TheaterAuditoriumLinkProtectionPolicy policy) {
        if (activeChange == null) {
            throw new TheaterDomainException("변경할 영화관 활성 상태는 필수입니다.");
        }

        if (policy == null) {
            throw new TheaterDomainException("영화관 활성 상태 변경 정책은 필수입니다.");
        }

        switch (activeChange) {
            case ACTIVATE -> activate();
            case DEACTIVATE -> {
                policy.validateCanChangeActive(this, activeChange);
                deactivate();
            }
        }
    }
}
