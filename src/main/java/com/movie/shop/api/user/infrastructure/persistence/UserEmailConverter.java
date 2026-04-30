package com.movie.shop.api.user.infrastructure.persistence;

import com.movie.shop.api.user.domain.aggregate.UserEmail;
import com.movie.shop.api.user.domain.exceptions.UserDomainException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Optional;

@Converter(autoApply = true)
public class UserEmailConverter implements AttributeConverter<Optional<UserEmail>, String> {

    @Override
    public String convertToDatabaseColumn(Optional<UserEmail> attribute) {
        if (attribute == null) {
            throw new IllegalStateException("사용자 이메일 저장 상태가 올바르지 않습니다.");
        }

        return attribute.map(UserEmail::getEmail).orElse(null);
    }

    @Override
    public Optional<UserEmail> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(UserEmail.create(dbData));
        } catch (UserDomainException ex) {
            throw new IllegalStateException("저장된 사용자 이메일 데이터가 올바르지 않습니다.", ex);
        }
    }
}
