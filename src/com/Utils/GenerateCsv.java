package com.Utils;

import au.com.bytecode.opencsv.CSVWriter;
import com.Vo.CsvElementVo;
import com.Vo.TestScript;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.tasks.GenerateTestScript;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * dongdong Created by 上午10:05  2018/9/14
 */
public class GenerateCsv {

    private List<PsiClass> requests;
    private PsiClass response;
    private List<PsiClass> dbInserts;
    private List<PsiClass> dbChecks;
    private boolean isNormal;
    private String path;
    private String testMethodName;//测试方法name

    public GenerateCsv(CsvElementVo csvElementVo,TestScript testScript) {
        this.requests = csvElementVo.getRequest();
        this.response = csvElementVo.getResponse();
        this.dbChecks = csvElementVo.getDbCheck();
        this.dbInserts = csvElementVo.getDbInsert();
        this.isNormal = csvElementVo.isNormal();
        this.path = csvElementVo.getPath();
        this.testMethodName = csvElementVo.getTestMethodName();
        getPath();
        generateRequestCsv();
        generateResponseCsv();
        generateDbInsertCsv();
        generateDbCheckCsv();
        generateHostCsv(testScript);
    }

    /**
     * 根据isNormal确定路径
     */
    private void getPath() {
        if (isNormal) {
            path = path + "normal/" + testMethodName + "/";
        } else {
            path = path + "funcExp/" + testMethodName + "/";
        }
    }

    /**
     * 生成请求csv文件
     */
    private void generateRequestCsv() {
        String requestPath = null;
        int index = 1;
        try {
            if (requests.size() != 0 && requests != null) {
                for (PsiClass request : requests) {
                    requestPath = path + "request" + index + "_" + request.getName() + ".csv";
                    createVerticalCsvForCheck(request, requestPath);
                    index++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成响应csv文件
     */
    private void generateResponseCsv() {
        String responsePath = null;
        responsePath = path + "response.csv";
        try {
            if (!EmptyUtils.isEmpty(response)) {
                createVerticalCsvForCheck(response, responsePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成dbInsertcsv文件
     */
    private void generateDbInsertCsv() {
        String dbInsertPath = null;
        int index = 1;
        try {
            if (dbInserts.size() != 0 && dbInserts != null) {
                for (PsiClass dbInsert : dbInserts) {
                    dbInsertPath = path + "dbInsert" + index + "_" + dbInsert.getName() + ".csv";
                    createTransverseCsvForCheck(dbInsert, dbInsertPath);
                    index++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成dbCheckcsv文件
     */
    private void generateDbCheckCsv() {
        String dbCheckPath = null;
        int index = 1;
        try {
            if (dbChecks.size() != 0 && dbChecks != null) {
                for (PsiClass dbCheck : dbChecks) {
                    dbCheckPath = path + "dbCheck" + index + "_" + dbCheck.getName() + ".csv";
                    createVerticalCsvForCheck(dbCheck, dbCheckPath);
                    index++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成主csv文件
     */
    private void generateHostCsv(TestScript testScript) {
        String HostPath = null;
        try {
            if (isNormal) {
                HostPath = path + GenerateTestScript.subStringToUc(testMethodName) + "NormalTest." + testMethodName + ".csv";
            } else {
                HostPath = path + GenerateTestScript.subStringToUc(testMethodName) + "FuncExceptionTest." + testMethodName + ".csv";
            }
            createHostCsv(HostPath, testScript);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成字段为竖向排列的校验文件
     *
     * @param psiClass 需要生成的类
     * @param path     生成文件的绝对路径
     */
    public static void createVerticalCsvForCheck(PsiClass psiClass, String path) {
        try {
            //组装文件路径
            File file = new File(path);
            File fileParent = file.getParentFile();
            if (!fileParent.exists()) {
                fileParent.mkdirs();
            }
            file.createNewFile();


            List<String[]> outputValues = new ArrayList<String[]>();
            //组装CSV文件第一行，标题行
            List<String> header = new ArrayList<String>();
            header.add("class");
            header.add("property");
            header.add("flag");
            header.add("exp");

            outputValues.add(header.toArray(new String[header.size()]));
            List<PsiField> fileNames = Arrays.asList(psiClass.getAllFields());
            String className = psiClass.getName();
            header.add(className.substring(className.lastIndexOf(".") + 1, className.length()));
            int i = 1;
            for (PsiField filename : fileNames) {
                if (filename.equals("id") || filename.equals("serialVersionUID")) {
                    continue;
                }
                if (filename.equals("createTime") || filename.equals("updateTime")) {
                    continue;
                }
                List<String> value = new ArrayList<String>();
                if (1 == i) {
                    //如果是第一个生成字段，则内容需要包含class名
                    value.add(className);
                } else {
                    value.add("");
                }
                value.add(filename.getName());
                value.add("Y");

                outputValues.add(value.toArray(new String[value.size()]));

                i++;
            }
            //初始化写入文件
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(file);
            } catch (Exception e) {
                throw e;
            }
            //将生成内容写入CSV文件
            try {
                OutputStreamWriter osw = null;
                osw = new OutputStreamWriter(outputStream);
                CSVWriter csvWriter = new CSVWriter(osw);
                csvWriter.writeAll(outputValues);
                csvWriter.close();
            } catch (Exception e) {
                throw e;
            }

        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    /**
     * 生成字段为横向排列的校验文件
     *
     * @param psiClass 需要生成的类
     * @param path     生成文件的绝对路径
     */

    public static void createTransverseCsvForCheck(PsiClass psiClass, String path) {
        try {
            List<PsiField> fileNames = Arrays.asList(psiClass.getAllFields());
            File file = new File(path);
            File fileParent = file.getParentFile();
            if (!fileParent.exists()) {
                fileParent.mkdirs();
            }
            file.createNewFile();
            List<String[]> outputValues = new ArrayList<String[]>();
            List<String> header = new ArrayList<String>();
            String className = psiClass.getName();
            header.add(className.substring(className.lastIndexOf(".") + 1, className.length()));
            for (PsiField name : fileNames) {
                if (name.equals("id") || name.equals("serialVersionUID")) {
                    continue;
                }
                if (name.equals("createDate") || name.equals("modifyDate")) {
                    continue;
                }
                header.add(name.getName());
            }
            outputValues.add(header.toArray(new String[header.size()]));
            //初始化写入文件
            OutputStream outputStream = null;
            outputStream = new FileOutputStream(file);
            //将生成内容写入CSV文件
            OutputStreamWriter osw = null;
            osw = new OutputStreamWriter(outputStream);
            CSVWriter csvWriter = new CSVWriter(osw);
            csvWriter.writeAll(outputValues);
            csvWriter.close();

        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    /**
     * 生成主csv
     *
     * @param path 生成文件的绝对路径
     */

    public static void createHostCsv(String path, TestScript testScript) {
        try {

            //组装文件路径
            File file = new File(path);
            File fileParent = file.getParentFile();
            if (!fileParent.exists()) {
                fileParent.mkdirs();
            }
            file.createNewFile();

            List<String[]> outputValues = new ArrayList<String[]>();
            //组装CSV文件第一行
            List<String> header = new ArrayList<String>();
            header.add("caseId");
            header.add("description");
            if (!EmptyUtils.isEmpty(testScript.getResponse())) {
                header.add("response");
            }
            if (!EmptyUtils.isEmpty(testScript.getAllRequestParem())) {
                for (PsiType key : testScript.getAllRequestParem().keySet()) {
                    if (Arrays.asList(GenerateTestScript.TYPE).contains(key.getPresentableText())) {
                        header.add(testScript.getAllRequestParem().get(key));
                    } else if (!Arrays.asList(GenerateTestScript.TYPE).contains(key.getPresentableText()) && !PsiUtil.isEnum(key) && !PsiUtil.isCollection(key)) {
                        header.add(GenerateTestScript.subString(key.getPresentableText()));
                    }
                }
            }
            if (!EmptyUtils.isEmpty(testScript.getDbList())) {
                for (String db : testScript.getDbList()) {
                    header.add(GenerateTestScript.subString(db));
                }
            }
            if (!EmptyUtils.isEmpty(testScript.getDbCheckList())) {
                for (String dbCheck : testScript.getDbCheckList()) {
                    header.add(GenerateTestScript.subString(dbCheck));
                }
            }
            header.add("index");
            outputValues.add(header.toArray(new String[header.size()]));

            List<String> content = new ArrayList<String>();
            content.add("NO001");
            content.add("");
            if (!EmptyUtils.isEmpty(testScript.getResponse())) {
                content.add(GenerateCsv.getCsvPath("response", testScript));
            }
            if (!EmptyUtils.isEmpty(testScript.getAllRequestParem())) {
                int index = 1;
                for (PsiType key : testScript.getAllRequestParem().keySet()) {
                    if (Arrays.asList(GenerateTestScript.TYPE).contains(key.getPresentableText())) {
                        content.add("");
                    } else if (!Arrays.asList(GenerateTestScript.TYPE).contains(key.getPresentableText()) && !PsiUtil.isEnum(key) && !PsiUtil.isCollection(key)) {
                        String name = "request" + index + "_" + key.getPresentableText();
                        content.add(GenerateCsv.getCsvPath(name, testScript));
                        index++;
                    }
                }
            }
            if (!EmptyUtils.isEmpty(testScript.getDbList())) {
                int index = 1;
                for (String key : testScript.getDbList()) {
                    String name = "dbInsert" + index + "_" + key;
                    content.add(GenerateCsv.getCsvPath(name, testScript));
                    index++;
                }
            }
            if (!EmptyUtils.isEmpty(testScript.getDbCheckList())) {
                int index = 1;
                for (String key : testScript.getDbCheckList()) {
                    String name = "dbCheck" + index + "_" + key;
                    content.add(GenerateCsv.getCsvPath(name, testScript));
                    index++;
                }
            }

            content.add("0");
            outputValues.add(content.toArray(new String[content.size()]));

            //初始化写入文件
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(file);
            } catch (Exception e) {
                throw e;
            }
            //将生成内容写入CSV文件
            try {
                OutputStreamWriter osw = null;
                osw = new OutputStreamWriter(outputStream);
                CSVWriter csvWriter = new CSVWriter(osw);
                csvWriter.writeAll(outputValues);
                csvWriter.close();
            } catch (Exception e) {
                throw e;
            }

        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public static String getCsvPath(String csvName, TestScript testScript) {
        String path = "testers/";
        if (testScript.getIsNormal()) {
            path = path + "normal/" + testScript.getTestMethod() + "/" + csvName + ".csv";
        } else {
            path = path + "funcExp/" + testScript.getTestMethod() + "/" + csvName + ".csv";
        }
        return path;
    }
}
