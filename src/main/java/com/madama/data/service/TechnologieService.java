package com.madama.data.service;

import com.madama.data.entity.Project;
import com.madama.data.entity.Technologie;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TechnologieService {

    private final TechnologieRepository repository;

    @Autowired
    public TechnologieService(TechnologieRepository repository) {
        this.repository = repository;
    }

    public Optional<Technologie> get(UUID id) {
        return repository.findById(id);
    }

    public Technologie update(Technologie entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<Technologie> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }


}
