package com.tinytelly.purpose;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tinytelly.purpose.model.*;
import com.tinytelly.purpose.utils.XProperties;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Customise a file based on the purpose.json file which encapsulates all Products (default is called purpose).
 */
public class FileCustomiser {
    Properties purposeProperties;

    public void customise(Purposes intendedPurposes, String jsonFilePath, String fileToCustomisePath, String fileToPurposeProperties) throws Exception {
        purposeProperties = getPurposeProperties(fileToPurposeProperties);

        System.out.println("Passing this file [" + fileToCustomisePath + "] to the customiser");
        java.io.File fileToCustomise = new java.io.File(fileToCustomisePath);
        String fileToCustomiseContents = FileUtils.readFileToString(fileToCustomise, "UTF-8");
        long fileSize = fileToCustomiseContents.length();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Products products = gson.fromJson(new FileReader(applyOverrides(jsonFilePath, intendedPurposes)), Products.class);

        for (Product product : products.getProducts()) {
            if (intendedPurposes.getPurposeNames().contains(product.getName())) {
                if (product.hasPurposes()) {
                    for (Purpose purpose : product.getPurposes().getPurposes()) {
                        for (Purpose intendedPurpose : intendedPurposes.getPurposes()) {
                            if (intendedPurpose.getName().equals(purpose.getName())) {
                                fileToCustomiseContents = customiseFiles(purpose.getFiles(), fileToCustomise.getName(), fileToCustomiseContents);
                            }
                        }
                    }
                }
            }
        }

        if (fileSize != fileToCustomiseContents.length()) {
            System.out.println("This file has been customised [" + fileToCustomisePath + "]");
            java.nio.file.Files.write(fileToCustomise.toPath(), fileToCustomiseContents.getBytes());
        }
    }

    private String customiseFiles(Files files, String fileToCustomiseName, String fileToCustomiseContents) throws Exception {
        for (File file : files.getFiles()) {
            if (fileToCustomiseName.equals(file.getName())) {
                for (FileValue fileValue : file.getValues()) {
                    if (fileValue.containsPurposeKey()) {
                        String valueWithPurposeValueIncluded = fileValue.getValue();
                        valueWithPurposeValueIncluded = valueWithPurposeValueIncluded.replace(fileValue.getPurposeKey(), purposeProperties.getProperty(fileValue.getPurposeKey()));
                        valueWithPurposeValueIncluded = valueWithPurposeValueIncluded.replace(fileValue.PURPOSE_KEYWORK_START, "");
                        valueWithPurposeValueIncluded = valueWithPurposeValueIncluded.replace(fileValue.PURPOSE_KEYWORK_END, "");
                        fileValue.setValue(valueWithPurposeValueIncluded);
                    }
                    if (FileValue.STATE.set.equals(fileValue.getState())) {
                        if (fileValue.getName().contains(FileValue.STAR)) {
                            String[] result = fileValue.getName().split("\\" + FileValue.STAR);
                            if (result.length != 2) {
                                throw new Exception("Incorrect use of the wildcard [" + FileValue.STAR + "] - usage [,*value] - not [" + fileValue.getName() + "]");
                            } else {
                                BufferedReader bufReader = new BufferedReader(new StringReader(fileToCustomiseContents));

                                StringBuffer replaced = new StringBuffer();
                                String line = null;
                                while ((line = bufReader.readLine()) != null) {
                                    replaced.append(subString(line, result[0], result[1]));
                                    replaced.append("\n");
                                }

                                fileToCustomiseContents = replaced.toString();
                            }
                        } else if (fileToCustomiseContents.contains(FileValue.EXCLAMATION)) {

                        } else if (fileToCustomiseContents.contains(fileValue.getLeftSide())) {
                            fileToCustomiseContents = replace(fileToCustomiseContents, fileValue.getLeftSide(), fileValue.getPair());
                        }
                    } else if (FileValue.STATE.add.equals(fileValue.getState())) {
                        fileToCustomiseContents += fileValue.getPair();
                    } else if (FileValue.STATE.replace.equals(fileValue.getState())) {
                        fileToCustomiseContents = replace(fileToCustomiseContents, fileValue.getName(), fileValue.getValue());
                    }
                }
            }
        }
        return fileToCustomiseContents;
    }

    private String replace(String fileToCustomiseContents, String findThis, String replaceWithThis) {
        System.out.println("Replacing [" + findThis + "] with [" + replaceWithThis + "]");
        StringBuilder sb = new StringBuilder();
        Splitter splitter = Splitter.on(System.getProperty("line.separator"));
        for (String line : splitter.split(fileToCustomiseContents)) {
                sb.append(line.replace(findThis, replaceWithThis));
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    public static void main(String... args) throws Exception {
        System.out.println("Args passed in : " + args.length);
        for (String arg : args) {
            System.out.println(arg);
        }

        if (args.length < 3 || args.length > 4) {
            System.out.println("Usage: 3 or 4 arguments required, Argument 4 is optional");
            System.out.println("Argument 1: a String containing a purpose, Multiple purposes separate by |");
            System.out.println("Argument 2: a String containing a full path to a json file called purpose.json");
            System.out.println("Argument 3: a String containing a full path to a file to customise");
            System.out.println("Argument 4: a String containing a full path to a file purpose.properties");
        } else {
            FileCustomiser fileCustomiser = new FileCustomiser();
            String PurposeFile = null;
            if (args.length == 4) {
                PurposeFile = args[3];
            }
            fileCustomiser.customise(getPurposesFromCommandLine(args[0]), args[1], args[2], PurposeFile);
        }
    }

    protected static Purposes getPurposesFromCommandLine(String commandLinePurposes) {
        commandLinePurposes += "|purpose";
        Purposes purposes = new Purposes();
        List<Purpose> purposeList = new ArrayList<Purpose>();

        List<String> list = Lists.newArrayList(Splitter.on("|").omitEmptyStrings().trimResults().split(commandLinePurposes));
        for (String str : list) {
            Purpose purpose = new Purpose(str);
            purposeList.add(purpose);
            purposes.setPurposes(purposeList);
        }

        System.out.println(purposes.toString());
        return purposes;
    }

    private java.io.File applyOverrides(String jsonFilePath, Purposes intendedPurposes) throws Exception {
        java.io.File fileToCustomise = new java.io.File(jsonFilePath);
        java.io.File fileToCustomiseTemp = new java.io.File(fileToCustomise.getParent() + java.io.File.separator + "OverridesApplied_" + fileToCustomise.getName());
        FileUtils.copyFile(fileToCustomise, fileToCustomiseTemp);

        String fileToCustomiseContents = FileUtils.readFileToString(fileToCustomiseTemp, "UTF-8");

        if (intendedPurposes != null) {
            for (Purpose intendedPurpose : intendedPurposes.getPurposes()) {
                if (intendedPurpose.hasValue()) {
                    fileToCustomiseContents = replace(fileToCustomiseContents, "{" + intendedPurpose.getName() + "}", intendedPurpose.getValue());
                }
            }
        }

        FileUtils.writeStringToFile(fileToCustomiseTemp, fileToCustomiseContents, "UTF-8");

        return fileToCustomiseTemp;
    }

    public Properties convert(java.io.File file) throws Exception {
        XProperties properties = new XProperties();

        properties.load(new FileInputStream(file));

        return properties;
    }

    private Properties getPurposeProperties(String fileToPurposeProperties) throws Exception {
        if (fileToPurposeProperties == null) {
            return new XProperties();
        }
        java.io.File PurposeFile = new java.io.File(fileToPurposeProperties);
        return convert(PurposeFile);
    }

    protected String subString(String line, String start, String end) {
        int indexOfEnd = line.lastIndexOf(end);
        if (indexOfEnd == -1) {
            return line;
        }
        String shortened = line.substring(0, indexOfEnd);
        int indexOfStart = shortened.lastIndexOf(start);

        if (indexOfStart == -1) {
            return line;
        }
        String frontString = line.substring(0, indexOfStart);
        String endString = line.substring(indexOfEnd + end.length());

        return frontString + endString;
    }
}

