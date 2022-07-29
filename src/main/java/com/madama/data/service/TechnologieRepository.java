package com.madama.data.service;

import com.madama.data.entity.Technologie;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnologieRepository extends JpaRepository<Technologie, UUID> {

}