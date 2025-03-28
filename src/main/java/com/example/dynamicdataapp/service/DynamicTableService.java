package com.example.dynamicdataapp.service;

import com.example.dynamicdataapp.exception.TableCreationException;
import com.example.dynamicdataapp.model.TableDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DynamicTableService {
    private static final Logger logger = LoggerFactory.getLogger(DynamicTableService.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DynamicTableService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createTable(TableDefinition tableDefinition) {
        String tableName = sanitizeTableName(tableDefinition.getTableName().toLowerCase());
        String[] fields = tableDefinition.getFields().split(",");
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS `" + tableName + "` (");
        sql.append("`id` BIGINT AUTO_INCREMENT PRIMARY KEY, "); // Add an ID column for each table
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].trim();
            sql.append("`").append(field).append("` VARCHAR(255)");
            if (i < fields.length - 1) {
                sql.append(", ");
            }
        }
        sql.append(")");
        try {
            jdbcTemplate.execute(sql.toString());
            logger.info("Created table: {}", tableName);
        } catch (Exception e) {
            logger.error("Failed to create table '{}': {}", tableName, e.getMessage());
            throw new TableCreationException("Failed to create table: " + tableName);
        }
    }

    public void deleteTable(String tableName) {
        String sanitizedTableName = sanitizeTableName(tableName.toLowerCase());
        String sql = "DROP TABLE IF EXISTS `" + sanitizedTableName + "`";
        try {
            jdbcTemplate.execute(sql);
            logger.info("Dropped table: {}", sanitizedTableName);
        } catch (Exception e) {
            logger.error("Failed to drop table '{}': {}", sanitizedTableName, e.getMessage());
            throw new TableCreationException("Failed to drop table: " + sanitizedTableName);
        }
    }

    public String sanitizeTableName(String tableName) {
        return tableName.replaceAll("\\W", "");
    }

}