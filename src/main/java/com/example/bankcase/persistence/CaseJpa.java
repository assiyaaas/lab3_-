package com.example.bankcase.persistence;

import com.example.bankcase.domain.CaseNumber;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class CaseJpa {

    private final EntityManager em;

    public CaseJpa(EntityManager em) {
        this.em = em;
    }

    // persist always inserts; Spring Data save() with our UUID would SELECT first
    public void insert(CaseEntity row) {
        em.persist(row);
    }

    public int count(CaseNumber number) {
        Long n = em.createQuery("""
                        select count(c) from CaseEntity c
                        where c.businessKey = :key
                        """, Long.class)
                .setParameter("key", number.value())
                .getSingleResult();
        return n.intValue();
    }

    // sends the pending INSERTs now, so a UNIQUE violation is thrown inside the caller's catch
    public void flush() {
        em.flush();
    }
}
