package com.example.dynamicdataapp.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.dynamicdataapp.model.TableDefinition;
import com.example.dynamicdataapp.repository.TableDefinitionRepository;
import com.example.dynamicdataapp.service.DynamicTableService;

@Controller
public class TableController {
    private static final Logger logger = LoggerFactory.getLogger(TableController.class);

    private static final String INVALID_TABLE_ID_MSG = "Invalid table ID: ";
    private static final String TABLE_MODEL_KEY = "table";
    private static final String ERROR_VIEW = "error";
    private static final String REDIRECT_DASHBOARD = "redirect:/dashboard";

    private final TableDefinitionRepository tableDefinitionRepository;
    private final JdbcTemplate jdbcTemplate;
    private final DynamicTableService dynamicTableService;

    @Autowired
    public TableController(TableDefinitionRepository tableDefinitionRepository, JdbcTemplate jdbcTemplate, DynamicTableService dynamicTableService) {
        this.tableDefinitionRepository = tableDefinitionRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.dynamicTableService = dynamicTableService;
    }

    @GetMapping("/create-table")
    public String createTableForm() {
        return "create-table";
    }

    @PostMapping("/save-table")
    public String saveTable(@RequestParam String tableName, @RequestParam String fields) {
        TableDefinition tableDefinition = new TableDefinition(tableName, fields);
        try {
            tableDefinitionRepository.save(tableDefinition);
            logger.info("Saved table definition: {}", tableName);
            dynamicTableService.createTable(tableDefinition);
            logger.info("Table creation initiated for: {}", tableName);
        } catch (Exception e) {
            return ERROR_VIEW;
        }
        return REDIRECT_DASHBOARD;
    }

    @GetMapping("/edit-table/{id}")
    public String editTableForm(@PathVariable Long id, Model model) {
        TableDefinition table = tableDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_TABLE_ID_MSG + id));
        model.addAttribute(TABLE_MODEL_KEY, table);
        return "edit-table";
    }

    @PostMapping("/update-table")
    public String updateTable(@RequestParam Long id, @RequestParam String tableName, @RequestParam String fields) {
        TableDefinition table = tableDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_TABLE_ID_MSG + id));
        
        String oldTableName = table.getTableName();
        String sanitizedOldTableName = dynamicTableService.sanitizeTableName(oldTableName.toLowerCase());
        String sanitizedNewTableName = dynamicTableService.sanitizeTableName(tableName.toLowerCase());
        String newFields = fields;

        // Update the TableDefinition
        table.setTableName(tableName);
        table.setFields(fields);
        tableDefinitionRepository.save(table);
        logger.info("Updated table definition: {}", tableName);

        // Synchronize the table schema
        try {
            // If table name changed, rename the table
            if (!sanitizedOldTableName.equals(sanitizedNewTableName)) {
                String renameSql = "RENAME TABLE `" + sanitizedOldTableName + "` TO `" + sanitizedNewTableName + "`";
                jdbcTemplate.execute(renameSql);
                logger.info("Renamed table from '{}' to '{}'", sanitizedOldTableName, sanitizedNewTableName);
            }

            // Get current columns from the table
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SHOW COLUMNS FROM `" + sanitizedNewTableName + "`"
            );
            Set<String> existingColumns = columns.stream()
                .map(column -> ((String) column.get("Field")).toLowerCase())
                .collect(Collectors.toSet());

            // Get new fields from the updated definition
            Set<String> newFieldSet = Arrays.stream(newFields.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

            // Add missing columns
            for (String field : newFieldSet) {
                if (!existingColumns.contains(field.toLowerCase())) {
                    String alterSql = "ALTER TABLE `" + sanitizedNewTableName + "` ADD COLUMN `" + field + "` VARCHAR(255)";
                    jdbcTemplate.execute(alterSql);
                    logger.info("Added column '{}' to table '{}'", field, sanitizedNewTableName);
                }
            }

            // Note: We won't drop columns here to avoid data loss; handle manually if needed
        } catch (Exception e) {
            logger.error("Failed to synchronize table schema for '{}': {}", sanitizedNewTableName, e.getMessage(), e);
            return ERROR_VIEW;
        }

        return REDIRECT_DASHBOARD;
    }

    @GetMapping("/view-table/{id}")
    public String viewTable(@PathVariable Long id, Model model) {
        TableDefinition table = tableDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_TABLE_ID_MSG + id));
        String tableName = dynamicTableService.sanitizeTableName(table.getTableName().toLowerCase());
        List<Map<String, Object>> rows = Collections.emptyList(); // Default to empty list

        // Ensure fields is not null
        if (table.getFields() == null || table.getFields().trim().isEmpty()) {
            logger.warn("Table '{}' (ID: {}) has no fields defined", tableName, id);
            model.addAttribute(TABLE_MODEL_KEY, table);
            model.addAttribute("rows", rows);
            model.addAttribute("errorMessage", "No fields defined for this table.");
            return "view-table"; // Render with a warning
        }

        try {
            rows = jdbcTemplate.queryForList("SELECT * FROM `" + tableName + "`");
            logger.info("Successfully queried table '{}': {} rows retrieved", tableName, rows.size());
        } catch (Exception e) {
            logger.error("Failed to query table '{}': {}", tableName, e.getMessage(), e);
            model.addAttribute("errorMessage", "Could not retrieve data for table: " + tableName);
        }

        model.addAttribute(TABLE_MODEL_KEY, table);
        model.addAttribute("rows", rows);
        return "view-table";
    }

    @GetMapping("/add-data/{id}")
    public String addDataForm(@PathVariable Long id, Model model) {
        TableDefinition table = tableDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_TABLE_ID_MSG + id));
        model.addAttribute(TABLE_MODEL_KEY, table);
        return "add-data";
    }

    @PostMapping("/save-data")
    public String saveData(@RequestParam Long id, @RequestParam Map<String, String> allParams) {
        TableDefinition table = tableDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_TABLE_ID_MSG + id));
        String rawTableName = table.getTableName().toLowerCase();
        String tableName = dynamicTableService.sanitizeTableName(rawTableName);
        String[] fields = table.getFields().split(",");
        
        StringBuilder sql = new StringBuilder("INSERT INTO `" + tableName + "` (");
        StringBuilder values = new StringBuilder(" VALUES (");
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].trim();
            sql.append("`").append(field).append("`");
            values.append("?");
            if (i < fields.length - 1) {
                sql.append(", ");
                values.append(", ");
            }
        }
        sql.append(")");
        values.append(")");
        String query = sql.toString() + values.toString();
        
        Object[] params = Arrays.stream(fields)
                .map(field -> allParams.get(field.trim()))
                .toArray();
        
        if (logger.isInfoEnabled()) {
            logger.info("Executing query: {} with params: {}", query, Arrays.toString(params));
        }
        try {
            jdbcTemplate.update(query, params);
            logger.info("Added data to table: {}", tableName);
        } catch (Exception e) {
            logger.error("Failed to add data to table '{}': {}", tableName, e.getMessage(), e);
            return ERROR_VIEW;
        }
        return "redirect:/view-table/" + id;
    }

    @PostMapping("/delete-table/{id}")
    public String deleteTable(@PathVariable Long id) {
        TableDefinition table = tableDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_TABLE_ID_MSG + id));
        String tableName = table.getTableName().toLowerCase();
        try {
            dynamicTableService.deleteTable(tableName);
            tableDefinitionRepository.deleteById(id);
            logger.info("Deleted table definition and data for: {}", tableName);
        } catch (Exception e) {
            logger.error("Failed to delete table '{}': {}", tableName, e.getMessage());
            return ERROR_VIEW;
        }
        return REDIRECT_DASHBOARD;
    }

    @PostMapping("/delete-data/{tableId}/{rowId}")
    public String deleteData(@PathVariable Long tableId, @PathVariable String rowId) {
        TableDefinition table = tableDefinitionRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_TABLE_ID_MSG + tableId));
        String tableName = dynamicTableService.sanitizeTableName(table.getTableName().toLowerCase());
        
        try {
            String deleteSql = "DELETE FROM `" + tableName + "` WHERE StudentID = ?";
            jdbcTemplate.update(deleteSql, rowId);
            logger.info("Deleted row with StudentID '{}' from table '{}'", rowId, tableName);
        } catch (Exception e) {
            logger.error("Failed to delete row with StudentID '{}' from table '{}': {}", rowId, tableName, e.getMessage(), e);
            return ERROR_VIEW;
        }
        
        return "redirect:/view-table/" + tableId;
    }
}