package com.madama.data.service;

import java.util.UUID;

import com.madama.data.entity.Mate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MateRepository extends JpaRepository<Mate, UUID> {

}