package com.example.dynamicdataapp.repository;

import com.example.dynamicdataapp.model.TableDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableDefinitionRepository extends JpaRepository<TableDefinition, Long> {
}