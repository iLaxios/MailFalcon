package com.laxios.MailFalcon.repository;
import com.laxios.MailFalcon.model.EmailRecord;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class EmailRepository {
    private final Map<String, EmailRecord> storage = new ConcurrentHashMap<>();

    public void save(EmailRecord record) {
        storage.put(record.getId(), record);
    }

    public EmailRecord findById(String id) {
        return storage.get(id);
    }

    public Map<String, EmailRecord> findAll() {
        return storage;
    }
}