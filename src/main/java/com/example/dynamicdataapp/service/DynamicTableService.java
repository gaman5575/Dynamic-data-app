package com.example.dynamicdataapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.dynamicdataapp.model.*;

class TableCreationException extends RuntimeException {
    public TableCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}

class TableDeletionException extends RuntimeException {
    public TableDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}

@Service
public class DynamicTableService {
    private static final Logger logger = LoggerFactory.getLogger(DynamicTableService.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DynamicTableService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createTable(TableDefinition tableDefinition) {
        try {
            String rawTableName = tableDefinition.getTableName().toLowerCase();
            String tableName = sanitizeTableName(rawTableName);
            String[] fields = tableDefinition.getFields().split(",");
            StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS `" + tableName + "` (");
            for (int i = 0; i < fields.length; i++) {
                String field = fields[i].trim();
                sql.append("`").append(field).append("` VARCHAR(255)");
                if (i < fields.length - 1) {
                    sql.append(", ");
                }
            }
            sql.append(")");
            logger.info("Executing SQL to create table: {}", sql);
            jdbcTemplate.execute(sql.toString());
            logger.info("Successfully created table: {}", tableName);
        } catch (Exception e) {
            String errorMessage = String.format("Failed to create table '%s': %s", tableDefinition.getTableName(), e.getMessage());
            logger.error(errorMessage, e);
            throw new TableCreationException(errorMessage, e);
        }
    }

    public void deleteTable(String tableName) {
        try {
            String sanitizedTableName = sanitizeTableName(tableName.toLowerCase());
            String sql = "DROP TABLE IF EXISTS `" + sanitizedTableName + "`";
            logger.info("Executing SQL to delete table: {}", sql);
            jdbcTemplate.execute(sql);
            logger.info("Successfully deleted table: {}", sanitizedTableName);
        } catch (Exception e) {
            String errorMessage = String.format("Failed to delete table '%s': %s", tableName, e.getMessage());
            logger.error(errorMessage, e);
            throw new TableDeletionException(errorMessage, e);
        }
    }

    public String sanitizeTableName(String tableName) {
        return tableName.replaceAll("\\W", "_").trim();
    }
}