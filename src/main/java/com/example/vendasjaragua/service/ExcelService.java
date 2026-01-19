package com.example.vendasjaragua.service;

import com.example.vendasjaragua.model.Venda;
import com.example.vendasjaragua.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import java.util.Collections;
import com.example.vendasjaragua.model.VendaItem;
import com.example.vendasjaragua.model.Produto;
import com.example.vendasjaragua.model.Time;
import com.example.vendasjaragua.model.Vendedor;
import com.example.vendasjaragua.repository.ProdutoRepository;
import com.example.vendasjaragua.repository.TimeRepository;
import com.example.vendasjaragua.repository.VendedorRepository;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelService.class);

    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final TimeRepository timeRepository;
    private final VendedorRepository vendedorRepository;

    public void save(MultipartFile file) {
        try {
            List<Venda> vendas = parseExcelFile(file.getInputStream());
            vendaRepository.saveAll(vendas);
            vendaRepository.deleteEmptyRows(); // Cleanup after import
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    public List<Venda> parseExcelFile(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Sheet sheet = workbook.getSheetAt(0); // Assume first sheet
            List<Venda> vendas = new ArrayList<>();

            int rowIndex = 0;
            for (Row row : sheet) {
                if (rowIndex == 0) { // Skip header
                    rowIndex++;
                    continue;
                }
                
                // Skip completely empty rows during parsing to save resource
                if (isRowEmpty(row)) {
                     rowIndex++;
                     continue;
                }

                Venda venda = new Venda();
                // Column mapping:
                // 0: CLIENTE, 1: NF, 2: OV, 3: ENTREGA, 4: TELEFONE, 5: CIDADE
                // 6: ESTADO, 7: VENDEDOR, 8: DATA, 9: PLACAS, 10: INVERSOR, 11: POTÊNCIA
                // 12: R$ VENDA, 13: R$ MATERIAL, 14: R$ BRUTO, 15: MARKUP, 16: PRODUTO, 17: TIME

                venda.setCliente(getStringValue(row.getCell(0)));
                venda.setNf(getStringValue(row.getCell(1)));
                venda.setOv(getStringValue(row.getCell(2)));
                venda.setEntrega(getStringValue(row.getCell(3)));
                venda.setTelefone(getStringValue(row.getCell(4)));
                venda.setCidade(getStringValue(row.getCell(5)));
                venda.setEstado(getStringValue(row.getCell(6)));
                venda.setVendedor(getStringValue(row.getCell(7)));
                
                // DATA
                venda.setData(getDateValue(row.getCell(8)));

                venda.setPlacas(getStringValue(row.getCell(9)));
                venda.setInversor(getStringValue(row.getCell(10)));
                venda.setPotencia(getStringValue(row.getCell(11)));

                // Log raw values for debug
                Cell cellVal = row.getCell(12);
                Cell cellMat = row.getCell(13);
                
                if (rowIndex <= 10) { // Log only first 10 rows to avoid spam
                     logger.info("Row {}: Col 12 (Venda) Type={}; Col 13 (Mat) Type={}", 
                        rowIndex, 
                        cellVal != null ? cellVal.getCellType() : "NULL",
                        cellMat != null ? cellMat.getCellType() : "NULL"
                    );
                }

                BigDecimal valorVenda = getBigDecimalValue(cellVal, evaluator);
                venda.setValorVenda(valorVenda);
                BigDecimal valorMaterial = getBigDecimalValue(cellMat, evaluator);
                venda.setValorMaterial(valorMaterial); 
                // campos calculados automaticamente pelo modelo: row.getCell(14) e (15) sao ignorados.
                
                String rawProduto = getStringValue(row.getCell(16));
                if (rawProduto != null && !rawProduto.trim().isEmpty()) {
                    VendaItem item = new VendaItem();
                    item.setNomeProduto(rawProduto);
                    item.setQuantidade(1);
                    // Use the values from columns 12 and 13 as the unit values for the product
                    item.setValorUnitarioVenda(valorVenda != null ? valorVenda : BigDecimal.ZERO);
                    item.setValorUnitarioCusto(valorMaterial != null ? valorMaterial : BigDecimal.ZERO);
                    venda.setProduto(new ArrayList<>(Collections.singletonList(item)));
                } else {
                    // Se não há produto detalhado, cria um item "Não Especificado" com o valor total
                    VendaItem itemNaoEspecificado = new VendaItem();
                    itemNaoEspecificado.setNomeProduto("Não Especificado");
                    itemNaoEspecificado.setQuantidade(1);
                    itemNaoEspecificado.setValorUnitarioVenda(valorVenda != null ? valorVenda : BigDecimal.ZERO);
                    // Use the parsed material value (Column N/13) for the cost
                    itemNaoEspecificado.setValorUnitarioCusto(valorMaterial != null ? valorMaterial : BigDecimal.ZERO);
                    venda.setProduto(new ArrayList<>(Collections.singletonList(itemNaoEspecificado)));
                }
                
                venda.setInverterInfo(new ArrayList<>()); // Initialize with empty list as requested

                venda.setTime(getStringValue(row.getCell(17)));

                vendas.add(venda);
                rowIndex++;
            }
            workbook.close();
            return vendas;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    public void saveProdutos(MultipartFile file) {
        try {
            List<Produto> produtos = parseProdutoExcelFile(file.getInputStream());
            produtoRepository.saveAll(produtos);
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    public void saveTimes(MultipartFile file) {
        try {
            List<Time> times = parseTimeExcelFile(file.getInputStream());
            timeRepository.saveAll(times);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void saveVendedores(MultipartFile file) {
        try {
            List<Vendedor> vendedores = parseVendedorExcelFile(file.getInputStream());
            vendedorRepository.saveAll(vendedores);
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    public List<Produto> parseProdutoExcelFile(InputStream is) {
         try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            List<Produto> produtos = new ArrayList<>();
            
            int rowIndex = 0;
            for (Row row : sheet) {
                if (rowIndex == 0) { // Skip header
                    rowIndex++;
                    continue;
                }
                if (isRowEmpty(row)) {
                     rowIndex++;
                     continue;
                }
                
                // DESCRIÇÃO|GRUPO|UNIDADE -> 0, 1, 2
                Produto p = new Produto();
                p.setDescricao(getStringValue(row.getCell(0)));
                p.setGrupo(getStringValue(row.getCell(1)));
                p.setUnidade(getStringValue(row.getCell(2)));
                
                produtos.add(p);
                rowIndex++;
            }
            workbook.close();
            return produtos;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    public List<Time> parseTimeExcelFile(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            List<Time> times = new ArrayList<>();
            
            int rowIndex = 0;
            for (Row row : sheet) {
                if (rowIndex == 0) { rowIndex++; continue; } // Skip Header
                if (isRowEmpty(row)) { rowIndex++; continue; }

                String nome = getStringValue(row.getCell(0));
                String lider = getStringValue(row.getCell(1));

                if (lider == null || lider.trim().isEmpty()) {
                    throw new IOException("Linha " + (rowIndex + 1) + ": Time '" + nome + "' não possui líder informado. Importação cancelada.");
                }

                // Check if exists
                Time time = timeRepository.findByNome(nome).stream().findFirst().orElse(new Time());
                time.setNome(nome);
                time.setLider(lider);
                
                times.add(time);
                rowIndex++;
            }
            workbook.close();
            return times;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Vendedor> parseVendedorExcelFile(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            List<Vendedor> vendedores = new ArrayList<>();
            
            // Allow duplicate logic: we want to update existing by name or create new?
            // "importar" usually implies create if not exists
            
            int rowIndex = 0;
            for (Row row : sheet) {
                if (rowIndex == 0) { rowIndex++; continue; }
                if (isRowEmpty(row)) { rowIndex++; continue; }

                String nome = getStringValue(row.getCell(0));
                String nomeTime = getStringValue(row.getCell(1));

                Vendedor vendedor = vendedorRepository.findByNome(nome).stream().findFirst().orElse(new Vendedor());
                vendedor.setNome(nome);

                if (nomeTime != null && !nomeTime.trim().isEmpty()) {
                    Time time = timeRepository.findByNome(nomeTime).stream().findFirst().orElse(null);
                    vendedor.setTime(time);
                } else {
                    vendedor.setTime(null);
                }
                
                vendedores.add(vendedor);
                rowIndex++;
            }
            workbook.close();
            return vendedores;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        // Check critical columns (CLIENTE=0, DATA=8, VALOR=12)
        // If all these are missing, we consider the row empty.
        Cell c0 = row.getCell(0);
        Cell c8 = row.getCell(8);
        Cell c12 = row.getCell(12);

        boolean c0Empty = (c0 == null || c0.getCellType() == CellType.BLANK);
        boolean c8Empty = (c8 == null || c8.getCellType() == CellType.BLANK);
        boolean c12Empty = (c12 == null || c12.getCellType() == CellType.BLANK);
        
        return c0Empty && c8Empty && c12Empty;
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return "";
        try {
            switch (cell.getCellType()) {
                case STRING: return cell.getStringCellValue();
                case NUMERIC: return String.valueOf((long) cell.getNumericCellValue()); // integer string mostly
                case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
                default: return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    private Double getDoubleValue(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return 0.0;
        try {
            // First: If it's a formula, try to get the pre-calculated (cached) value if evaluation fails or returns error
            if (cell.getCellType() == CellType.FORMULA) {
                try {
                    // Start by assuming we might want the cached value if evaluation is tricky
                    // But usually we try evaluate first.
                    CellValue cellValue = evaluator.evaluate(cell);
                    
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        return cellValue.getNumberValue();
                    }
                    if (cellValue.getCellType() == CellType.ERROR) {
                        // Evaluation calculated an error (e.g. #DIV/0!), fallback to cached if valid
                         if (cell.getCachedFormulaResultType() == CellType.NUMERIC) {
                             return cell.getNumericCellValue();
                         }
                    }
                } catch (Exception e) {
                   // Evaluation failed (e.g. unsupported function), strictly fallback to cached
                   if (cell.getCachedFormulaResultType() == CellType.NUMERIC) {
                        return cell.getNumericCellValue();
                   }
                }
                // If evaluation resulted in string or failed all above, continue to formatter
            }

            // For numeric cells, return directly to avoid string parsing issues
             if (cell.getCellType() == CellType.NUMERIC && !DateUtil.isCellDateFormatted(cell)) {
                return cell.getNumericCellValue();
            }

            // Last resort: String parsing (handles currency "R$ 1.000,00" etc)
            DataFormatter formatter = new DataFormatter(new java.util.Locale("pt", "BR"));
            // Note: passing evaluator here might cause re-evaluation of formula. 
            // If we are here, formula evaluation might have been weird.
            String strVal = formatter.formatCellValue(cell, evaluator); 
            return parseStringValue(strVal);
            
        } catch (Exception e) {
            // Final safety net: try to get numeric value directly if possible
            if (cell.getCellType() == CellType.NUMERIC || 
               (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.NUMERIC)) {
                return cell.getNumericCellValue();
            }
            logger.error("Error getting double value from cell", e);
            return 0.0;
        }
    }
    
    private Double parseStringValue(String val) {
         try {
             if (val == null || val.trim().isEmpty()) return 0.0;
             
             // Regex to keep only digits, minus sign and comma
             // This assumes Brazilian format where '.' is thousand separator and ',' is decimal
             val = val.replaceAll("[^0-9,-]", ""); 
             
             if (val.isEmpty() || val.equals("-")) return 0.0;
             
             // Replace decimal comma with dot for Java parsing
             val = val.replace(",", ".");
             
             return Double.parseDouble(val);
         } catch (NumberFormatException e) {
             logger.warn("Failed to parse string value: '{}'", val);
             return 0.0;
         }
    }

    private BigDecimal getBigDecimalValue(Cell cell, FormulaEvaluator evaluator) {
        Double val = getDoubleValue(cell, evaluator);
        return val != null ? BigDecimal.valueOf(val) : BigDecimal.ZERO;
    }
    
    private BigDecimal getBigDecimalValue(Cell cell) {
         // Helper for compatibility if needed, but we should use the one with evaluator
         return getBigDecimalValue(cell, null); // Will crash if not updated everywhere? No, handled above.
    }

    private java.time.LocalDate getDateValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        } catch (Exception e) {
            return null;
        }
        return null; // fallback or parse string if needed
    }
}
