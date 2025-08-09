package com.laxios.MailFalcon.repository;

import com.laxios.MailFalcon.model.EmailRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailJpaRepository extends JpaRepository<EmailRecord, String> {
    // no method body needed, Spring Data implements this automatically
}
