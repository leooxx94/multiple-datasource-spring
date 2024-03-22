package com.leox.services.SupaRepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.leox.services.Models.SupaModel;

@Repository
public interface SupaRepository extends JpaRepository<SupaModel, String> {
    
}
