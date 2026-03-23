package com.movie.shop.api.auditorium.domain.aggregate;

import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import com.movie.shop.api.auditorium.domain.policy.AuditoriumNameDuplicatePolicy;
import com.movie.shop.api.auditorium.domain.policy.AuditoriumStatusPolicy;
import com.movie.shop.api.auditorium.domain.policy.AuditoriumTheaterExistencePolicy;
import com.movie.shop.api.shared.domain.EntityValidator;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auditorium {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auditorium_id", nullable = false)
    private long id;

    @Column(name = "theater_id", nullable = false)
    private long theaterId;

    @Setter(AccessLevel.PRIVATE)
    @AttributeOverride(
            name = "name",
            column = @Column(name = "name", nullable = false, length = 50)
    )
    @Embedded
    private AuditoriumName name;

    @Range(min = -10, max = 100, message = "층수는 -10에서 100 사이여야 합니다.")
    @Column
    private int floor;

    @Enumerated(EnumType.STRING)
    @Column
    private AuditoriumType auditoriumType;

    @Setter(AccessLevel.PRIVATE)
    @NotNull(message = "최소 하나 이상의 좌석이 필요합니다.")
    @Embedded
    private AuditoriumSeats seats;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public static Auditorium register(AuditoriumNameDuplicatePolicy nameDuplicateValidator,
                                      AuditoriumTheaterExistencePolicy theaterExistencePolicy,
                                      long theaterId,
                                      String name,
                                      int floor,
                                      AuditoriumType type,
                                      List<String> seats,
                                      int rowCount,
                                      int columnCount) {
        if (theaterId <= 0) {
            throw new AuditoriumDomainException("영화관 ID는 0보다 커야 합니다.");
        }
        if (theaterExistencePolicy == null) {
            throw new AuditoriumDomainException("상영관 등록 정책은 필수입니다.");
        }
        theaterExistencePolicy.validateCanRegister();

        var auditorium = new Auditorium();
        auditorium.theaterId = theaterId;
        auditorium.floor = floor;
        auditorium.auditoriumType = type;
        auditorium.active = true;

        EntityValidator.create()
                .apply(AuditoriumName.createNew(name, nameDuplicateValidator), auditorium::setName)
                .apply(AuditoriumSeats.create(seats, rowCount, columnCount), auditorium::setSeats)
                .validateBean(auditorium)
                .throwIfInvalid(AuditoriumDomainException::new);

        return auditorium;
    }

    public void update(AuditoriumNameDuplicatePolicy nameDuplicateValidator,
                       String name,
                       int floor,
                       AuditoriumType type,
                       List<String> seats,
                       int rowCount,
                       int columnCount) {
        this.floor = floor;
        this.auditoriumType = type;

        EntityValidator.create()
                .apply(AuditoriumName.createFrom(this.name, name, nameDuplicateValidator), this::setName)
                .apply(AuditoriumSeats.create(seats, rowCount, columnCount), this::setSeats)
                .validateBean(this)
                .throwIfInvalid(AuditoriumDomainException::new);
    }

    private void deactivate() {
        active = false;
    }

    private void activate() {
        active = true;
    }

    public void changeStatus(AuditoriumStatusChange activeChange, AuditoriumStatusPolicy policy) {
        if (activeChange == null) {
            throw new AuditoriumDomainException("변경할 상영관 활성 상태는 필수입니다.");
        }

        if (policy == null) {
            throw new AuditoriumDomainException("상영관 활성 상태 변경 정책은 필수입니다.");
        }

        policy.validateCanChangeStatus(this, activeChange);
        
        switch (activeChange) {
            case ACTIVATE -> activate();
            case DEACTIVATE -> deactivate();
        }
    }

    public boolean canHostScreening() {
        return active;
    }
}
