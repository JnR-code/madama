package com.madama.data.service;

import java.util.Optional;
import java.util.UUID;

import com.madama.data.entity.Mate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MateService {

    private final MateRepository repository;

    @Autowired
    public MateService(MateRepository repository) {
        this.repository = repository;
    }

    public Optional<Mate> get(UUID id) {
        return repository.findById(id);
    }

    public Mate update(Mate entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<Mate> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
