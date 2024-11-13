package com.lzy.springbootinit.generate;

import cn.hutool.core.io.FileUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 代码生成器
 */
public class CodeGenerator {
    /**
     * todo 需要修改的地方
     */
    static final String packageName = "com.lzy.springbootinit";

    /**
     * todo 需要修改的地方
     */
    static final String dataBaseName = "my_db";
    static final String url = "jdbc:mysql://localhost:3306/my_db";
    static final String user = "root";
    static final String password = "123456";

    public static void main(String[] args) throws SQLException, ClassNotFoundException, TemplateException, IOException {

        List<String> includeTableName = Collections.emptyList();
        List<String> excludeTableName = Collections.emptyList();
        // List<String> excludeTableName = Arrays.asList("user", "post", "post_thumb", "post_favour");

        List<String> excludeDTOAddFieldName = Arrays.asList("id", "createTime", "updateTime", "isDelete");
        List<String> excludeDTOUpdateFieldName = Arrays.asList("createTime", "updateTime", "isDelete");
        List<String> excludeDTOEditFieldName = Arrays.asList("createTime", "updateTime", "isDelete");
        List<String> excludeDTOQueryFieldName = Arrays.asList("createTime", "updateTime", "isDelete");
        List<String> excludeVOFieldName = Collections.singletonList("isDelete");

        // 加载驱动
        Class.forName("com.mysql.cj.jdbc.Driver");

        // 建立连接
        Connection conn = DriverManager.getConnection(url, user, password);

        // 创建Statement
        Statement stmt = conn.createStatement();
        List<TableInfo> tableList = new ArrayList<>();
        // 执行查询
        String tableSql = String.format("SELECT table_name, table_comment FROM information_schema.tables WHERE table_schema = %s", "'" + dataBaseName + "'");
        ResultSet rs = stmt.executeQuery(tableSql);
        // 处理结果
        while (rs.next()) {
            TableInfo tableInfo = new TableInfo();
            String tableName = rs.getString("table_name");
            String tableComment = rs.getString("table_comment");
            tableInfo.setDtoPackageName(tableName.replaceAll("_", "").toLowerCase());
            tableInfo.setTableName(tableName);
            tableInfo.setTableComment(tableComment);
            tableInfo.setClassName(toUpperCamelCase(tableName));
            tableInfo.setPackageName(packageName);
            tableInfo.setFieldName(toLowerCamelCase(tableName));
            tableList.add(tableInfo);
        }
        // 过滤
        if (!includeTableName.isEmpty()) {
            tableList = tableList.stream().filter(tableInfo -> includeTableName.contains(tableInfo.getTableName())).collect(Collectors.toList());
        }
        if (!excludeTableName.isEmpty()) {
            tableList = tableList.stream().filter(tableInfo -> !excludeTableName.contains(tableInfo.getTableName())).collect(Collectors.toList());
        }

        if (tableList.isEmpty()) {
            System.out.println("没有需要生成的表");
            return;
        }

        for (TableInfo tableInfo : tableList) {
            List<ColumnInfo> columnList = new ArrayList<>();
            String columnSql = String.format("SELECT column_name, data_type, column_comment, column_key, character_maximum_length FROM information_schema.columns WHERE table_name = %s AND table_schema = %s ORDER BY ORDINAL_POSITION", "'" + tableInfo.getTableName() + "'", "'" + dataBaseName + "'");
            ResultSet rs2 = stmt.executeQuery(columnSql);
            Set<String> importClassSet = new HashSet<>();
            while (rs2.next()) {
                String columnName = rs2.getString("column_name");
                String dataType = rs2.getString("data_type");
                String columnComment = rs2.getString("column_comment");
                String columnKey = rs2.getString("column_key");
                String characterMaximumLength = rs2.getString("character_maximum_length");

                ColumnInfo columnInfo = new ColumnInfo();
                columnInfo.setColumnName(columnName);
                columnInfo.setUpperCamelCaseName(toUpperCamelCase(columnName));
                columnInfo.setLowerCamelCaseName(toLowerCamelCase(columnName));
                columnInfo.setComment(columnComment);
                columnInfo.setJavaType(convertMySQLtoJavaType(dataType));
                columnInfo.setJdbcType(dataType);
                columnInfo.setCharacterMaximumLength(characterMaximumLength);
                columnInfo.setXmlJdbcType(xmlJdbcType(dataType));
                columnInfo.setColumnKey(columnKey);
                columnInfo.setTableLogic(false);
                HashSet<String> annotationSet = new HashSet<>();
                if ("PRI".equals(columnKey)) {
                    annotationSet.add("@TableId(type = IdType.ASSIGN_ID)");
                }
                if ("isDelete".equals(columnName)) {
                    annotationSet.add("@TableLogic");
                    columnInfo.setTableLogic(true);
                }
                columnInfo.setAnnotationSet(annotationSet);
                if ("Date".equals(convertMySQLtoJavaType(dataType))) {
                    importClassSet.add("java.util.Date");
                }

                columnList.add(columnInfo);
            }

            List<ColumnInfo> addRequest = getColumnInfos(columnList, excludeDTOAddFieldName);

            List<ColumnInfo> updateRequest = getColumnInfos(columnList, excludeDTOUpdateFieldName);

            List<ColumnInfo> editRequest = getColumnInfos(columnList, excludeDTOEditFieldName);

            List<ColumnInfo> queryRequest = getColumnInfos(columnList, excludeDTOQueryFieldName);

            List<ColumnInfo> vo = getColumnInfos(columnList, excludeVOFieldName);

            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("importClassSet", importClassSet);
            dataModel.put("tableInfo", tableInfo);
            dataModel.put("columnInfoList", columnList);
            dataModel.put("addRequestList", addRequest);
            dataModel.put("updateRequestList", updateRequest);
            dataModel.put("editRequestList", editRequest);
            dataModel.put("queryRequestList", queryRequest);
            dataModel.put("voList", vo);

            String projectPath = System.getProperty("user.dir");
            // 1、生成 Entity
            // 指定生成路径
            String inputPath = projectPath + File.separator + "src/main/resources/templates/model/templateEntity.java.ftl";
            String outputPath = String.format("%s/generator/model/entity/%s.java", projectPath, tableInfo.getClassName());
            doGenerate(inputPath, outputPath, dataModel);
            System.out.println("生成 Entity 成功，文件路径：" + outputPath);

            // 2、生成数据模型封装类（包括 DTO 和 VO）
            // 生成 DTO
            inputPath = projectPath + File.separator + "src/main/resources/templates/model/TemplateAddRequest.java.ftl";
            outputPath = String.format("%s/generator/model/dto/%s/%sAddRequest.java", projectPath, tableInfo.getDtoPackageName(), tableInfo.getClassName());
            doGenerate(inputPath, outputPath, dataModel);
            inputPath = projectPath + File.separator + "src/main/resources/templates/model/TemplateEditRequest.java.ftl";
            outputPath = String.format("%s/generator/model/dto/%s/%sEditRequest.java", projectPath, tableInfo.getDtoPackageName(), tableInfo.getClassName());
            doGenerate(inputPath, outputPath, dataModel);
            inputPath = projectPath + File.separator + "src/main/resources/templates/model/TemplateQueryRequest.java.ftl";
            outputPath = String.format("%s/generator/model/dto/%s/%sQueryRequest.java", projectPath, tableInfo.getDtoPackageName(), tableInfo.getClassName());
            doGenerate(inputPath, outputPath, dataModel);
            inputPath = projectPath + File.separator + "src/main/resources/templates/model/TemplateUpdateRequest.java.ftl";
            outputPath = String.format("%s/generator/model/dto/%s/%sUpdateRequest.java", projectPath, tableInfo.getDtoPackageName(), tableInfo.getClassName());
            doGenerate(inputPath, outputPath, dataModel);
            System.out.println("生成 DTO 成功，文件路径：" + outputPath);

            // 生成 VO
            // Map<String, Object> voDataModel = getDataModel(tableInfo, columnList, excludeVOFieldName);
            inputPath = projectPath + File.separator + "src/main/resources/templates/model/TemplateVO.java.ftl";
            outputPath = String.format("%s/generator/model/vo/%sVO.java", projectPath, tableInfo.getClassName());
            doGenerate(inputPath, outputPath, dataModel);
            System.out.println("生成 VO 成功，文件路径：" + outputPath);

            // 1、生成 Mapper
            inputPath = projectPath + File.separator + "src/main/resources/templates/templateMapper.java.ftl";
            outputPath = String.format("%s/generator/mapper/%sMapper.java", projectPath, tableInfo.getClassName());
            doGenerate(inputPath, outputPath, dataModel);
            System.out.println("生成 Mapper 成功，文件路径：" + outputPath);

            // 1、生成 Service
            inputPath = projectPath + File.separator + "src/main/resources/templates/templateService.java.ftl";
            outputPath = String.format("%s/generator/service/%sService.java", projectPath, tableInfo.getClassName());
            doGenerate(inputPath, outputPath, dataModel);
            inputPath = projectPath + File.separator + "src/main/resources/templates/templateServiceImpl.java.ftl";
            outputPath = String.format("%s/generator/service/impl/%sServiceImpl.java", projectPath, tableInfo.getClassName());
            doGenerate(inputPath, outputPath, dataModel);
            System.out.println("生成 Service 成功，文件路径：" + outputPath);

            // 1、生成 Controller
            inputPath = projectPath + File.separator + "src/main/resources/templates/templateController.java.ftl";
            outputPath = String.format("%s/generator/controller/%sController.java", projectPath, tableInfo.getClassName());
            doGenerate(inputPath, outputPath, dataModel);
            System.out.println("生成 Controller 成功，文件路径：" + outputPath);

            // 生成 Mapper XML
            inputPath = projectPath + File.separator + "src/main/resources/templates/templateMapper.xml.ftl";
            outputPath = String.format("%s/generator/xml/%sMapper.xml", projectPath, tableInfo.getClassName());
            doGenerate(inputPath, outputPath, dataModel);
            System.out.println("生成 Mapper XML 成功，文件路径：" + outputPath);
        }

    }

    private static List<ColumnInfo> getColumnInfos(List<ColumnInfo> columnList, List<String> excludeDTOEditFieldName) {
        List<ColumnInfo> resultList = new ArrayList<>();
        resultList = columnList.stream()
                .filter(columnInfo -> !excludeDTOEditFieldName.contains(columnInfo.getColumnName()))
                .map(columnInfo -> {
                    ColumnInfo columnInfoCopy = new ColumnInfo();
                    BeanUtils.copyProperties(columnInfo, columnInfoCopy);
                    String columnKey = columnInfo.getColumnKey();
                    HashSet<String> annotationSetCopy = getAnnotationSetCopy(columnInfo, columnKey);
                    columnInfoCopy.setAnnotationSet(annotationSetCopy);
                    return columnInfoCopy;
                })
                .collect(Collectors.toList());
        return resultList;
    }

    private static HashSet<String> getAnnotationSetCopy(ColumnInfo columnInfo, String columnKey) {
        Boolean tableLogic = columnInfo.getTableLogic();
        Set<String> annotationSet = columnInfo.getAnnotationSet();
        HashSet<String> annotationSetCopy = new HashSet<>(annotationSet);
        if ("PRI".equals(columnKey)) {
            annotationSetCopy.remove("@TableId(type = IdType.ASSIGN_ID)");
        }
        if (Boolean.TRUE.equals(tableLogic)) {
            annotationSetCopy.remove("TableLogic");
        }
        return annotationSetCopy;
    }

    /**
     * 生成文件
     *
     * @param inputPath  模板文件输入路径
     * @param outputPath 输出路径
     * @param model      数据模型
     * @throws IOException
     * @throws TemplateException
     */
    public static void doGenerate(String inputPath, String outputPath, Object model) throws IOException, TemplateException {
        // new 出 Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);

        // 指定模板文件所在的路径
        File templateDir = new File(inputPath).getParentFile();
        configuration.setDirectoryForTemplateLoading(templateDir);

        // 设置模板文件使用的字符集
        configuration.setDefaultEncoding("utf-8");

        // 创建模板对象，加载指定模板
        String templateName = new File(inputPath).getName();
        Template template = configuration.getTemplate(templateName);

        // 文件不存在则创建文件和父目录
        if (!FileUtil.exist(outputPath)) {
            FileUtil.touch(outputPath);
        }

        // 生成
        Writer out = new FileWriter(outputPath);
        template.process(model, out);

        // 生成文件后别忘了关闭哦
        out.close();
    }

    public static String xmlJdbcType(String mysqlType) {
        mysqlType = mysqlType.toUpperCase();
        switch (mysqlType) {
            case "BIGINT":
                return "BIGINT";
            case "INT":
                return "Integer";
            case "DATE":
            case "TIME":
            case "DATETIME":
            case "TIMESTAMP":
                return "TIMESTAMP";
            case "VARCHAR":
            case "TINYTEXT":
            case "TEXT":
            case "MEDIUMTEXT":
            case "LONGTEXT":
                return "VARCHAR";
            default:
                return mysqlType;
        }
    }


    public static String convertMySQLtoJavaType(String mysqlType) {
        mysqlType = mysqlType.toUpperCase();
        switch (mysqlType) {
            case "BIGINT":
                return "Long";
            case "TINYINT":
            case "SMALLINT":
            case "MEDIUMINT":
            case "INT":
            case "YEAR":
                return "Integer"; // 对应Java的Long类型
            case "FLOAT":
            case "DOUBLE":
            case "DECIMAL":
                return "Double"; // 对应Java的Double类型
            case "BIT":
            case "BOOL":
            case "BOOLEAN":
                return "Boolean"; // 对应Java的Boolean类型
            case "DATE":
            case "TIME":
            case "DATETIME":
            case "TIMESTAMP":
                return "Date"; // 对应Java的java.util.Date类型
            case "CHAR":
            case "VARCHAR":
            case "TINYTEXT":
            case "TEXT":
            case "MEDIUMTEXT":
            case "LONGTEXT":
                return "String"; // 对应Java的String类型
            case "BINARY":
            case "VARBINARY":
            case "TINYBLOB":
            case "BLOB":
            case "MEDIUMBLOB":
            case "LONGBLOB":
                return "byte[]"; // 对应Java的byte数组
            default:
                return "String"; // 默认转换为String类型
        }
    }


    public static String toCamelCase(String str, boolean upperCase) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        // str = str.toLowerCase();
        StringBuilder result = new StringBuilder();
        boolean nextIsUpperCase = false;

        for (int i = 0; i < str.length(); i++) {
            char currentChar = str.charAt(i);

            if (currentChar == '_') {
                nextIsUpperCase = true;
            } else if (nextIsUpperCase) {
                result.append(Character.toUpperCase(currentChar));
                nextIsUpperCase = false;
            } else {
                result.append(currentChar);
            }
        }

        if (upperCase && Character.isLowerCase(result.charAt(0))) {
            result.setCharAt(0, Character.toUpperCase(result.charAt(0)));
        }

        return result.toString();
    }

    public static String toUpperCamelCase(String str) {
        return toCamelCase(str, true);
    }

    public static String toLowerCamelCase(String str) {
        return toCamelCase(str, false);
    }


}
