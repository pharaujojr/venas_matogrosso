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
import com.example.vendasjaragua.model.VendedorMatoGrosso;
import com.example.vendasjaragua.model.ProdutoMatoGrosso;
import com.example.vendasjaragua.model.Filial;
import com.example.vendasjaragua.repository.ProdutoRepository;
import com.example.vendasjaragua.repository.TimeRepository;
import com.example.vendasjaragua.repository.VendedorRepository;
import com.example.vendasjaragua.repository.VendedorMatoGrossoRepository;
import com.example.vendasjaragua.repository.ProdutoMatoGrossoRepository;
import com.example.vendasjaragua.repository.FilialRepository;
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
    private final VendedorMatoGrossoRepository vendedorMatoGrossoRepository;
    private final ProdutoMatoGrossoRepository produtoMatoGrossoRepository;
    private final FilialRepository filialRepository;

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
                
                // Conforme solicitado, ignoramos a col 16 (Nome do produto) e usamos sempre "Não Especificado"
                // para garantir consistência e evitar problemas de agrupamento ou dados sujos.
                VendaItem itemNaoEspecificado = new VendaItem();
                itemNaoEspecificado.setNomeProduto("Não Especificado");
                itemNaoEspecificado.setQuantidade(1);
                
                // Use os valores das colunas 12 e 13 para Venda e Custo
                itemNaoEspecificado.setValorUnitarioVenda(valorVenda != null ? valorVenda : BigDecimal.ZERO);
                itemNaoEspecificado.setValorUnitarioCusto(valorMaterial != null ? valorMaterial : BigDecimal.ZERO);
                
                venda.setProduto(new ArrayList<>(Collections.singletonList(itemNaoEspecificado)));
                
                venda.setInverterInfo(new ArrayList<>()); // Initialize with empty list as requested

                String rawTime = getStringValue(row.getCell(17));
                venda.setTime(rawTime.isEmpty() ? null : rawTime);

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
            String value = "";
            switch (cell.getCellType()) {
                case STRING: value = cell.getStringCellValue(); break;
                case NUMERIC: value = String.valueOf((long) cell.getNumericCellValue()); break;
                case BOOLEAN: value = String.valueOf(cell.getBooleanCellValue()); break;
                default: value = "";
            }
            return value != null ? value.trim() : "";
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
    
    // Métodos de upload para Mato Grosso
    public void saveVendedoresMatoGrosso(MultipartFile file) {
        try {
            List<VendedorMatoGrosso> vendedores = parseVendedoresMatoGrossoExcel(file.getInputStream());
            vendedorMatoGrossoRepository.saveAll(vendedores);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao importar vendedores: " + e.getMessage());
        }
    }
    
    private List<VendedorMatoGrosso> parseVendedoresMatoGrossoExcel(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            List<VendedorMatoGrosso> vendedores = new ArrayList<>();
            
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
                
                VendedorMatoGrosso vendedor = new VendedorMatoGrosso();
                // Colunas: NOME, FILIAL, EMAIL, TELEFONE, ATIVO
                String nomeVendedor = getStringValue(row.getCell(0));
                vendedor.setNome(nomeVendedor);
                
                String nomeFilial = getStringValue(row.getCell(1));
                if (nomeFilial == null || nomeFilial.trim().isEmpty()) {
                    throw new IOException("Linha " + (rowIndex + 1) + ": Vendedor '" + nomeVendedor + "' não possui filial informada. Importação cancelada.");
                }
                
                final String filialNome = nomeFilial; // Make effectively final for lambda
                final int currentRow = rowIndex; // Make effectively final for lambda
                Filial filial = filialRepository.findByNome(filialNome)
                    .orElseThrow(() -> new IOException("Linha " + (currentRow + 1) + ": Filial '" + filialNome + "' não encontrada. Cadastre a filial primeiro."));
                vendedor.setFilial(filial);
                
                vendedor.setEmail(getStringValue(row.getCell(2)));
                vendedor.setTelefone(getStringValue(row.getCell(3)));
                
                String ativoStr = getStringValue(row.getCell(4));
                vendedor.setAtivo(ativoStr == null || ativoStr.trim().isEmpty() || 
                                 ativoStr.equalsIgnoreCase("sim") || 
                                 ativoStr.equalsIgnoreCase("true") ||
                                 ativoStr.equalsIgnoreCase("ativo"));
                
                vendedores.add(vendedor);
                rowIndex++;
            }
            
            workbook.close();
            return vendedores;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar arquivo Excel: " + e.getMessage());
        }
    }
    
    public void saveProdutosMatoGrosso(MultipartFile file) {
        try {
            List<ProdutoMatoGrosso> produtos = parseProdutosMatoGrossoExcel(file.getInputStream());
            produtoMatoGrossoRepository.saveAll(produtos);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao importar produtos: " + e.getMessage());
        }
    }
    
    private List<ProdutoMatoGrosso> parseProdutosMatoGrossoExcel(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            List<ProdutoMatoGrosso> produtos = new ArrayList<>();
            
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
                
                ProdutoMatoGrosso produto = new ProdutoMatoGrosso();
                // Colunas: DESCRIÇÃO, GRUPO, UNIDADE
                produto.setDescricao(getStringValue(row.getCell(0)));
                produto.setGrupo(getStringValue(row.getCell(1)));
                produto.setUnidade(getStringValue(row.getCell(2)));
                
                produtos.add(produto);
                rowIndex++;
            }
            
            workbook.close();
            return produtos;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar arquivo Excel: " + e.getMessage());
        }
    }
    
    public void saveGruposMatoGrosso(MultipartFile file) {
        // Este método pode ser usado para importar apenas grupos
        // ou podemos reutilizar saveProdutosMatoGrosso já que grupos vêm dos produtos
        try {
            saveProdutosMatoGrosso(file);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao importar grupos: " + e.getMessage());
        }
    }

    public java.io.ByteArrayInputStream generateVendasExcel(List<Venda> vendas, List<String> grupos, List<String> produtos) {
        try (Workbook workbook = new XSSFWorkbook(); java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Vendas");
            
            // Build cache for product groups if filtering by group is needed
            final java.util.Map<String, String> productGroupMap = new java.util.HashMap<>();
            // Always fetch to show groups if possible, or just if filters are active. 
            // For safety and correctness of calculation, we should fetch if groups filter is active.
            if (grupos != null && !grupos.isEmpty()) {
                List<ProdutoMatoGrosso> allProds = produtoMatoGrossoRepository.findAll();
                for (ProdutoMatoGrosso p : allProds) {
                    productGroupMap.put(p.getDescricao(), p.getGrupo());
                }
            }

            // Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "DATA", "FILIAL", "VENDEDOR", "CLIENTE", "VALOR (BANCO)", "TOTAL (ITENS)", "FILTRADO (ITENS)", "PRODUTOS DETALHADOS"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(createBoldStyle(workbook));
            }
            
            int rowIdx = 1;
            for (Venda venda : vendas) {
                Row row = sheet.createRow(rowIdx++);
                
                row.createCell(0).setCellValue(venda.getId() != null ? venda.getId() : 0);
                row.createCell(1).setCellValue(venda.getData() != null ? venda.getData().toString() : "");
                row.createCell(2).setCellValue(venda.getTime() != null ? venda.getTime() : "");
                row.createCell(3).setCellValue(venda.getVendedor());
                row.createCell(4).setCellValue(venda.getCliente());
                
                double valorBanco = venda.getValorVenda() != null ? venda.getValorVenda().doubleValue() : 0.0;
                row.createCell(5).setCellValue(valorBanco);
                
                double totalItens = 0;
                double totalFiltrado = 0;
                StringBuilder prodDetails = new StringBuilder();
                
                if (venda.getProduto() != null) {
                    for (VendaItem item : venda.getProduto()) {
                        double valUnit = item.getValorUnitarioVenda() != null ? item.getValorUnitarioVenda().doubleValue() : 0.0;
                        int qtd = item.getQuantidade() != null ? item.getQuantidade() : 0;
                        double subtotal = valUnit * qtd;
                        
                        totalItens += subtotal;
                        
                        boolean matches = true;
                        // Filter by Product Name
                        if (produtos != null && !produtos.isEmpty()) {
                            if (!produtos.contains(item.getNomeProduto())) matches = false;
                        }
                        
                        // Filter by Group
                        if (matches && grupos != null && !grupos.isEmpty()) {
                             String grp = productGroupMap.get(item.getNomeProduto());
                             // Handle "Não Especificado" logic
                             boolean matchGroup = false;
                             if (grp == null || grp.trim().isEmpty()) {
                                 if (grupos.contains("Não Especificado")) matchGroup = true;
                             } else {
                                 if (grupos.contains(grp)) matchGroup = true;
                             }
                             if (!matchGroup) matches = false;
                        }
                        
                        if (matches) {
                            totalFiltrado += subtotal;
                        }
                        
                        prodDetails.append(String.format("[%s x %s = %s] ", qtd, item.getNomeProduto(), subtotal));
                    }
                }
                
                row.createCell(6).setCellValue(totalItens);
                row.createCell(7).setCellValue(totalFiltrado);
                row.createCell(8).setCellValue(prodDetails.toString());
            }
            
            for(int i=0; i<columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return new java.io.ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }
    
    private CellStyle createBoldStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
    
    public java.io.ByteArrayInputStream generateRelatorioExcel(List<Venda> vendas) {
        try (Workbook workbook = new XSSFWorkbook(); java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Relatório de Vendas");
            
            // Criar estilo para o cabeçalho
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            // Criar estilo para datas
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
            
            // Criar estilo para valores monetários
            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(createHelper.createDataFormat().getFormat("R$ #,##0.00"));
            
            // Criar cabeçalho
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Data", "Cliente", "Vendedor", "Filial", "Valor da Venda", "Ganho"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Preencher dados
            int rowIdx = 1;
            for (Venda venda : vendas) {
                Row row = sheet.createRow(rowIdx++);
                
                // Data
                Cell dateCell = row.createCell(0);
                if (venda.getData() != null) {
                    dateCell.setCellValue(java.util.Date.from(venda.getData().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    dateCell.setCellStyle(dateStyle);
                } else {
                    dateCell.setCellValue("");
                }
                
                // Cliente
                row.createCell(1).setCellValue(venda.getCliente() != null ? venda.getCliente() : "");
                
                // Vendedor
                row.createCell(2).setCellValue(venda.getVendedorObj() != null ? venda.getVendedorObj().getNome() : (venda.getVendedor() != null ? venda.getVendedor() : ""));
                
                // Unidade (Filial)
                row.createCell(3).setCellValue(venda.getFilial() != null ? venda.getFilial().getNome() : (venda.getTime() != null ? venda.getTime() : ""));
                
                // Valor da Venda
                Cell valorCell = row.createCell(4);
                if (venda.getValorVenda() != null) {
                    valorCell.setCellValue(venda.getValorVenda().doubleValue());
                    valorCell.setCellStyle(currencyStyle);
                } else {
                    valorCell.setCellValue(0.0);
                    valorCell.setCellStyle(currencyStyle);
                }
                
                // Ganho
                row.createCell(5).setCellValue(venda.getGanho() != null && venda.getGanho() ? "Sim" : "Não");
            }
            
            // Criar tabela do Excel (ListObject)
            if (rowIdx > 1) {
                org.apache.poi.xssf.usermodel.XSSFSheet xssfSheet = (org.apache.poi.xssf.usermodel.XSSFSheet) sheet;
                org.apache.poi.xssf.usermodel.XSSFTable table = xssfSheet.createTable(
                    new org.apache.poi.ss.util.AreaReference(
                        new org.apache.poi.ss.util.CellReference(0, 0),
                        new org.apache.poi.ss.util.CellReference(rowIdx - 1, columns.length - 1),
                        org.apache.poi.ss.SpreadsheetVersion.EXCEL2007
                    )
                );
                
                table.setName("TabelaVendas");
                table.setDisplayName("TabelaVendas");
                table.setStyleName("TableStyleMedium2");
                
                // Configurar auto-filtro
                org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable ctTable = table.getCTTable();
                ctTable.setTotalsRowShown(false);
                
                org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter autoFilter = ctTable.getAutoFilter();
                if (autoFilter == null) {
                    autoFilter = ctTable.addNewAutoFilter();
                }
                autoFilter.setRef(new org.apache.poi.ss.util.AreaReference(
                    new org.apache.poi.ss.util.CellReference(0, 0),
                    new org.apache.poi.ss.util.CellReference(rowIdx - 1, columns.length - 1),
                    org.apache.poi.ss.SpreadsheetVersion.EXCEL2007
                ).formatAsString());
                
                // Adicionar colunas à tabela
                org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumns ctTableColumns = ctTable.getTableColumns();
                if (ctTableColumns == null) {
                    ctTableColumns = ctTable.addNewTableColumns();
                }
                ctTableColumns.setCount(columns.length);
                
                for (int i = 0; i < columns.length; i++) {
                    org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn column;
                    if (i < ctTableColumns.sizeOfTableColumnArray()) {
                        column = ctTableColumns.getTableColumnArray(i);
                    } else {
                        column = ctTableColumns.addNewTableColumn();
                    }
                    column.setId(i + 1);
                    column.setName(columns[i]);
                }
            }
            
            // Auto-ajustar largura das colunas (após criar a tabela para melhor ajuste)
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
                // Adicionar um pouco de espaço extra para garantir que o conteúdo caiba
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512);
            }
            
            workbook.write(out);
            return new java.io.ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar Excel: " + e.getMessage(), e);
        }
    }
}
