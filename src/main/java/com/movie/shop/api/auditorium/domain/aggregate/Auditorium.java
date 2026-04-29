package com.movie.shop.api.auditorium.domain.aggregate;

import com.movie.shop.api.auditorium.domain.condition.AuditoriumNameUniquenessCondition;
import com.movie.shop.api.auditorium.domain.condition.AuditoriumOperatingTheaterStatus;
import com.movie.shop.api.auditorium.domain.condition.AuditoriumRegistrationTheater;
import com.movie.shop.api.auditorium.domain.condition.AuditoriumScreeningPresence;
import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
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
import java.util.Optional;

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

    public static Auditorium register(AuditoriumNameUniquenessCondition nameCondition,
                                      Optional<AuditoriumRegistrationTheater> registrationTheater,
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

        AuditoriumRegistrationTheater resolvedRegistrationTheater = resolveRegistrationTheater(registrationTheater);

        var auditorium = new Auditorium();
        auditorium.theaterId = resolvedRegistrationTheater.theaterId();
        auditorium.floor = floor;
        auditorium.auditoriumType = type;
        auditorium.active = true;

        EntityValidator.create()
                .apply(AuditoriumName.createNew(name, nameCondition), auditorium::setName)
                .apply(AuditoriumSeats.create(seats, rowCount, columnCount), auditorium::setSeats)
                .validateBean(auditorium)
                .throwIfInvalid(AuditoriumDomainException::new);

        return auditorium;
    }

    public void update(AuditoriumNameUniquenessCondition nameCondition,
                       String name,
                       int floor,
                       AuditoriumType type,
                       List<String> seats,
                       int rowCount,
                       int columnCount) {
        this.floor = floor;
        this.auditoriumType = type;

        EntityValidator.create()
                .apply(AuditoriumName.createFrom(this.name, name, nameCondition), this::setName)
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

    public void changeStatus(AuditoriumStatusChange activeChange,
                             AuditoriumScreeningPresence screeningPresence,
                             Optional<AuditoriumOperatingTheaterStatus> operatingTheaterStatus) {
        if (activeChange == null) {
            throw new AuditoriumDomainException("변경할 상영관 활성 상태는 필수입니다.");
        }

        switch (activeChange) {
            case ACTIVATE -> {
                validateCanActivate(operatingTheaterStatus);
                activate();
            }
            case DEACTIVATE -> {
                validateCanDeactivate(screeningPresence);
                deactivate();
            }
        }
    }

    public boolean canHostScreening() {
        return active;
    }

    public void validateCanDelete(AuditoriumScreeningPresence screeningPresence) {
        validateScreeningPresenceRequired(screeningPresence);

        if (screeningPresence.hasBlockingScreening()) {
            throw new AuditoriumDomainException("예정/판매중/판매종료 상영이 존재하는 상영관은 삭제할 수 없습니다.");
        }
    }

    private static AuditoriumRegistrationTheater resolveRegistrationTheater(
            Optional<AuditoriumRegistrationTheater> registrationTheater
    ) {
        if (registrationTheater == null) {
            throw new AuditoriumDomainException("상영관 등록 대상 영화관은 필수입니다.");
        }

        return registrationTheater.orElseThrow(
                () -> new AuditoriumDomainException("존재하지 않는 영화관에는 상영관을 등록할 수 없습니다.")
        );
    }

    private void validateCanDeactivate(AuditoriumScreeningPresence screeningPresence) {
        if (!active) {
            return;
        }

        validateScreeningPresenceRequired(screeningPresence);

        if (screeningPresence.hasBlockingScreening()) {
            throw new AuditoriumDomainException("예정/판매중/판매종료 상영이 존재하는 상영관은 비활성화할 수 없습니다.");
        }
    }

    private void validateCanActivate(Optional<AuditoriumOperatingTheaterStatus> operatingTheaterStatus) {
        if (operatingTheaterStatus == null) {
            throw new AuditoriumDomainException("소속 영화관 운영 상태는 필수입니다.");
        }

        AuditoriumOperatingTheaterStatus resolvedStatus = operatingTheaterStatus.orElseThrow(
                () -> new AuditoriumDomainException("영화관 정보를 찾을 수 없습니다.")
        );

        if (!resolvedStatus.active()) {
            throw new AuditoriumDomainException("비활성화된 영화관의 상영관은 활성화할 수 없습니다.");
        }
    }

    private static void validateScreeningPresenceRequired(AuditoriumScreeningPresence screeningPresence) {
        if (screeningPresence == null) {
            throw new AuditoriumDomainException("상영관의 차단 상영 존재 여부는 필수입니다.");
        }
    }
}
