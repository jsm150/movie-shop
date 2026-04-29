package com.movie.shop.api.theater.domain.aggregate;

import com.movie.shop.api.shared.domain.EntityValidator;
import com.movie.shop.api.theater.domain.condition.TheaterAuditoriumPresence;
import com.movie.shop.api.theater.domain.condition.TheaterNameUniquenessCondition;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Theater {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "theater_id", nullable = false)
    private long id;

    @AttributeOverride(
            name = "name",
            column = @Column(name = "name", unique = true, nullable = false, length = 50)
    )
    @Embedded
    private TheaterName name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public static Theater register(String name, TheaterNameUniquenessCondition nameCondition) {
        var theater = new Theater();
        theater.active = true;
        theater.name = TheaterName.createNew(name, nameCondition);

        EntityValidator.create()
                .validateBean(theater)
                .throwIfInvalid(TheaterDomainException::new);

        return theater;
    }

    public void updateName(String name, TheaterNameUniquenessCondition nameCondition) {
        this.name = TheaterName.createFrom(this.name, name, nameCondition);
    }

    private void deactivate() {
        active = false;
    }

    private void activate() {
        active = true;
    }

    public void changeActive(TheaterActiveChange activeChange, TheaterAuditoriumPresence auditoriumPresence) {
        if (activeChange == null) {
            throw new TheaterDomainException("변경할 영화관 활성 상태는 필수입니다.");
        }

        switch (activeChange) {
            case ACTIVATE -> activate();
            case DEACTIVATE -> {
                validateCanDeactivate(auditoriumPresence);
                deactivate();
            }
        }
    }

    public void validateCanDelete(TheaterAuditoriumPresence auditoriumPresence) {
        if (auditoriumPresence == null) {
            throw new TheaterDomainException("영화관의 상영관 보유 여부는 필수입니다.");
        }

        if (auditoriumPresence.hasAnyAuditorium()) {
            throw new TheaterDomainException("상영관이 연결된 영화관은 삭제할 수 없습니다.");
        }
    }

    private void validateCanDeactivate(TheaterAuditoriumPresence auditoriumPresence) {
        if (!active) {
            return;
        }

        if (auditoriumPresence == null) {
            throw new TheaterDomainException("영화관의 상영관 보유 여부는 필수입니다.");
        }

        if (auditoriumPresence.hasAnyAuditorium()) {
            throw new TheaterDomainException("상영관이 연결된 영화관은 비활성화할 수 없습니다.");
        }
    }
}
