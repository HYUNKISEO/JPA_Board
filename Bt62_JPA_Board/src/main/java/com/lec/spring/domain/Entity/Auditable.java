package com.lec.spring.domain.Entity;

import java.time.LocalDateTime;

public interface Auditable {

    LocalDateTime getRegDate();

    void setRegDate(LocalDateTime regDate);

}
