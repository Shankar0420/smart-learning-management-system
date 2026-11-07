package com.lms.service;

import com.lms.entity.IdSequence;
import com.lms.entity.Role;
import com.lms.repository.IdSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;

@Service
public class ProfessionalIdService {

    private final IdSequenceRepository idSequenceRepository;

    public ProfessionalIdService(IdSequenceRepository idSequenceRepository) {
        this.idSequenceRepository = idSequenceRepository;
    }

    /**
     * Atomically generates the next unique professional ID using row-level pessimistic locking.
     * Teacher format: GUL-TEA-2026-001
     * Student format: GUL-STU-2026-001
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public String generateNextId(Role role) {
        int year = Year.now().getValue();
        String prefix = (role == Role.TEACHER) ? "TEA" : "STU";
        String sequenceKey = prefix + "_" + year;

        IdSequence sequence = idSequenceRepository.findByIdForUpdate(sequenceKey)
                .orElseGet(() -> new IdSequence(sequenceKey, 0L));

        long nextVal = sequence.getCurrentVal() + 1;
        sequence.setCurrentVal(nextVal);
        sequence.setUpdatedAt(LocalDateTime.now());
        idSequenceRepository.save(sequence);

        return String.format("GUL-%s-%d-%03d", prefix, year, nextVal);
    }
}

