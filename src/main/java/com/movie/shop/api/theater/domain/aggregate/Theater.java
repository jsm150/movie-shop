package com.movie.shop.api.theater.domain.aggregate;

import com.movie.shop.api.shared.domain.EntityValidator;
import com.movie.shop.api.theater.domain.aggregate.newtype.TheaterName;
import com.movie.shop.api.theater.domain.aggregate.validator.TheaterNameDuplicateValidator;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import jakarta.persistence.*;
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

    @Range(min = -10, max = 100, message = "층수는 -10에서 100 사이여야 합니다.")
    @Column
    private int floor;

    // 상영관 타입
    @Enumerated(EnumType.STRING)
    @Column
    private TheaterType theaterType;

    @Setter(AccessLevel.PRIVATE)
    @NotNull(message = "최소 하나 이상의 좌석이 필요합니다.")
    @Embedded
    private TheaterSeats seats;

    // 운영 상태
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public static Theater Register(TheaterNameDuplicateValidator nameDuplicateValidator, String name, int floor, TheaterType type, List<String> seats, int rowCount, int columnCount) {
        var theater = new Theater();
        theater.floor = floor;
        theater.theaterType = type;
        theater.active = true;

        EntityValidator.create()
            .apply(TheaterName.createNew(name, nameDuplicateValidator), theater::setName)
            .apply(TheaterSeats.create(seats, rowCount, columnCount), theater::setSeats)
            .validateBean(theater)
            .throwIfInvalid(TheaterDomainException::new);

        return theater;
    }

    public void Update(TheaterNameDuplicateValidator nameDuplicateValidator, String name, int floor, TheaterType type, List<String> seats, int rowCount, int columnCount) {
        this.floor = floor;
        this.theaterType = type;

        EntityValidator.create()
            .apply(TheaterName.createFrom(this.name, name, nameDuplicateValidator), this::setName)
            .apply(TheaterSeats.create(seats, rowCount, columnCount), this::setSeats)
            .validateBean(this)
            .throwIfInvalid(TheaterDomainException::new);
    }

    public void deactivate() {
        active = false;
    }

    public void activate() {
        active = true;
    }

}