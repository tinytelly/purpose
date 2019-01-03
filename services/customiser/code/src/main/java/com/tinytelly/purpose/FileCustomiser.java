package com.tinytelly.purpose;

import com.tinytelly.purpose.model.*;
import com.tinytelly.purpose.utils.XProperties;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.io.File;
import java.util.*;

public class FileCustomiser {
    private Properties devopsProperties;
    private Purposes allPurposes = new Purposes(new ArrayList<Purpose>());
    private static boolean silent = true;
    private List<com.tinytelly.purpose.model.File> overrideFiles = new ArrayList<com.tinytelly.purpose.model.File>();
    private StringBuilder purposeTree;

    public void customise(com.tinytelly.purpose.model.Purposes intendedPurposes, String jsonFilePath, String fileToCustomisePath, String devopsPropertiesPath) throws Exception {
        final String HEADING = "PURPOSES :";
        purposeTree = new StringBuilder(HEADING + System.lineSeparator());
        devopsProperties = getDevopsProperties(devopsPropertiesPath);
        allPurposes.getPurposes().addAll(intendedPurposes.getPurposes());

        File fileToCustomise = new java.io.File(fileToCustomisePath);

        if (fileToCustomise.isDirectory()) {
            System.out.printf("This process only customises files not directories.  This is a directory [" + fileToCustomisePath + "]");
        } else {
            populateAllNestedPurposes(intendedPurposes, jsonFilePath, devopsPropertiesPath);
            handleMultipleFilesUnderSamePurpose(jsonFilePath);
            applyMissingPurposes(allPurposes, jsonFilePath);
            String fileToCustomiseContentsBefore = FileUtils.readFileToString(fileToCustomise, "UTF-8");
            allPurposes.deduplicatePurposes();
            if(!silent) System.out.println("ALL Purposes (pre reverse)= " + allPurposes.toString());
            Collections.reverse(allPurposes.getPurposes());
            if(!silent) System.out.println("ALL Purposes = " + allPurposes.toString());
            String fileToCustomiseContents = customise(allPurposes, jsonFilePath, fileToCustomise);

            if (!fileToCustomiseContentsBefore.equals(fileToCustomiseContents)) {
                if (fileToCustomise.getName().endsWith(".properties")) {
                    fileToCustomiseContents = sort(fileToCustomiseContents);

                    if (com.tinytelly.purpose.model.File.DEVOPS_PROPERTIES.equals(fileToCustomise.getName())) {
                        StringBuffer sb = new StringBuffer(fileToCustomiseContents);
                        sb.append(System.getProperty("line.separator"));
                        sb.append("finished=true");
                        fileToCustomiseContents = sb.toString();
                    }
                }

                System.out.println("\n######################################################################################################################################################################################## ");
                if(purposeTree.toString().trim().length() > HEADING.length()) {
                    System.out.println(purposeTree);
                }
                System.out.println("\nThis file has been customised [" + fileToCustomise.getName() + "]");
                System.out.println("######################################################################################################################################################################################## ");
                java.nio.file.Files.write(fileToCustomise.toPath(), fileToCustomiseContents.getBytes());
            } else {
                if (!silent) System.out.println("This file did not get customised [" + fileToCustomisePath + "]");
            }
        }
    }

    public String sort(String fileToSortContents) throws IOException {
        File propertiesFile = new java.io.File("unsorted.properties");
        java.nio.file.Files.write(propertiesFile.toPath(), fileToSortContents.getBytes());

        ArrayList<String> rows = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(propertiesFile));

        String s;
        while((s = reader.readLine())!=null) {
            if(!"".equals(s) && s.contains(FileValue.OVERRIDE_DELIMITER)) {
                rows.add(s);
            }
        }

        Collections.sort(rows);

        FileWriter writer = new FileWriter("sorted.properties");
        for(String cur: rows)
            writer.write(cur+"\n");

        reader.close();
        writer.close();

        String fileToCustomiseContents = FileUtils.readFileToString(new java.io.File("sorted.properties"), "UTF-8");
        return  fileToCustomiseContents;
    }

    private void applyMissingPurposes(com.tinytelly.purpose.model.Purposes intendedPurposes, String jsonFilePath) throws IOException {
        List<String> missingPurposes = new ArrayList<String>();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Products products = gson.fromJson(new FileReader(jsonFilePath), Products.class);
        for (Product product : products.getProducts()) {
            if("devops".equals(product.getName())){
                for (Purpose intendedPurpose : intendedPurposes.getPurposes()) {
                    if(!"devops".equals(intendedPurpose.getName()) && intendedPurpose.hasValue()) {
                        if (!product.getPurposes().contains(intendedPurpose.getName())) {
                            missingPurposes.add(intendedPurpose.getPair());
                            if(!silent) System.out.println("Adhoc Purpose : " + intendedPurpose.getPair());
                        }
                    }
                }
            }
        }

        if(missingPurposes.size() > 0) {
            Collections.reverse(missingPurposes);
            for (Product product : products.getProducts()) {
                if ("devops".equals(product.getName())) {
                    for (String missingPurpose : missingPurposes) {
                        Purpose purpose = new Purpose();
                        String[] parts = missingPurpose.split(FileValue.OVERRIDE_DELIMITER);
                        purpose.setName(parts[0]);

                        com.tinytelly.purpose.model.File file = new com.tinytelly.purpose.model.File();
                        file.setName(FileValue.DEVOPS_PROPERTIES);

                        List<com.tinytelly.purpose.model.File> filesList = new ArrayList<com.tinytelly.purpose.model.File>(1);
                        filesList.add(file);
                        Files files = new Files();
                        files.setFiles(filesList);

                        FileValue fileValue = new FileValue();
                        fileValue.setName(parts[0].replace("-", "."));
                        fileValue.setState(FileValue.STATE.overwrite);
                        fileValue.setValue(parts[1]);
                        Set<FileValue> fileValues = new HashSet<FileValue>(1);
                        fileValues.add(fileValue);
                        file.setValues(fileValues);

                        purpose.setFiles(files);

                        product.getPurposes().getPurposes().add(purpose);
                    }
                }
            }
            String updatedJson = gson.toJson(products);
            FileUtils.writeStringToFile(new java.io.File(jsonFilePath), updatedJson, "UTF-8");
        }
    }

    private void handleMultipleFilesUnderSamePurpose(String jsonFilePath) throws IOException {
        boolean updated = false;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Products products = gson.fromJson(new FileReader(jsonFilePath), Products.class);
        Iterator<Product> productsInter = products.getProducts().iterator();

        while (productsInter.hasNext()) {
            Product product = productsInter.next();
            Iterator<Purpose> purposeIterator = product.getPurposes().getPurposes().iterator();
            while (purposeIterator.hasNext()) {
                Purpose purpose = purposeIterator.next();
                Iterator<com.tinytelly.purpose.model.File> fileIterator = purpose.getFiles().getFiles().iterator();
                List<com.tinytelly.purpose.model.File> filesList = new ArrayList<com.tinytelly.purpose.model.File>();
                Files files = new Files();

                while (fileIterator.hasNext()) {
                    com.tinytelly.purpose.model.File file = fileIterator.next();
                    if(file.getName().contains(FileValue.DEVOPS_PURPOSE_DELIMITER)) {
                        files.setFiles(filesList);

                        List<String> list = Lists.newArrayList(Splitter.on(FileValue.DEVOPS_PURPOSE_DELIMITER).omitEmptyStrings().trimResults().split(file.getName()));
                        for (String str : list) {
                            com.tinytelly.purpose.model.File f = new com.tinytelly.purpose.model.File();
                            f.setName(str);
                            f.setValues(file.getValues());
                            filesList.add(f);
                            updated = true;
                        }
                        fileIterator.remove();
                    }
                }
                if(filesList.size() > 0) {
                    purpose.addFiles(files);
                }
            }
        }

        if(updated) {
            String updatedJson = gson.toJson(products);
            FileUtils.writeStringToFile(new java.io.File(jsonFilePath), updatedJson, "UTF-8");

            if(!silent) {
                //Handy for debugging locally
                File file = new java.io.File(jsonFilePath);
                java.io.File fileToCustomiseTemp = new java.io.File(file.getParent() + java.io.File.separator + "post_handle_splitting_files_" + file.getName());
                FileUtils.copyFile(file, fileToCustomiseTemp);
            }
        }
    }

    private String customise(com.tinytelly.purpose.model.Purposes intendedPurposes, String jsonFilePath, File fileToCustomise) throws Exception {
        String fileToCustomiseContents = FileUtils.readFileToString(fileToCustomise, "UTF-8");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        com.tinytelly.purpose.model.Products products = gson.fromJson(new FileReader(applyOverrides(jsonFilePath, intendedPurposes)), com.tinytelly.purpose.model.Products.class);

        for (com.tinytelly.purpose.model.Product product : products.getProducts()) {
            if (intendedPurposes.getPurposeNames().contains(product.getName())) {
                //1) Set up files at the Product level
                fileToCustomiseContents = customiseFiles(product.getFiles(), fileToCustomise.getName(), fileToCustomiseContents);

                //2) Set up files at the Purpose level
                for (com.tinytelly.purpose.model.Purpose intendedPurpose : intendedPurposes.getPurposes()) {
                    if(product.hasPurposes()) {
                        for (com.tinytelly.purpose.model.Purpose purpose : product.getPurposes().getPurposes()) {
                            if (intendedPurpose.getName().equals(purpose.getName())) {
                                fileToCustomiseContents = customiseFiles(purpose.getFiles(), fileToCustomise.getName(), fileToCustomiseContents);
                            }

                        }
                    }
                }
            }
        }
        return fileToCustomiseContents;
    }

    private void log(int nestLevel, String nestedPurposes){
        if(nestedPurposes.endsWith(FileValue.DEVOPS_PURPOSE_DELIMITER)) {
            nestedPurposes = nestedPurposes.substring(0,nestedPurposes.length() - FileValue.DEVOPS_PURPOSE_DELIMITER.length());
        }
        if(nestLevel > 1){
            purposeTree.append(System.lineSeparator()).append("  ");
            for (int i=1; i<nestLevel; i++){
                purposeTree.append("-");
            }
            purposeTree.append(">");
        }
        purposeTree.append(" ").append(nestedPurposes);
    }

    private void populateAllNestedPurposes(com.tinytelly.purpose.model.Purposes intendedPurposes, String jsonFilePath, String devopsPropertiesPath) throws Exception {
        File fileToCustomise = new java.io.File(devopsPropertiesPath);
        String fileToCustomiseContentsOriginal = FileUtils.readFileToString(fileToCustomise, "UTF-8");
        //First pass of customising Devops.properties
        String fileToCustomiseContents = customise(intendedPurposes, jsonFilePath, fileToCustomise);

        boolean finalNestedLevel = false;
        int nestLevel = 1;
        log(nestLevel, intendedPurposes.getNames());
        while (!finalNestedLevel) {
            java.nio.file.Files.write(fileToCustomise.toPath(), fileToCustomiseContents.getBytes());

            Properties devopsPropertiesCustomised = getDevopsProperties(fileToCustomise.getAbsolutePath());
            String devopsPurpose = devopsPropertiesCustomised.getProperty(FileValue.DEVOPS_PURPOSE);

            if (!FileValue.DEVOPS_PURPOSE_DELIMITER.equals(devopsPurpose) && !devopsPurpose.equals("")) {
                if (devopsPurpose.endsWith(FileValue.DEVOPS_PURPOSE_DELIMITER)) {
                    devopsPurpose = devopsPurpose.substring(0, (devopsPurpose.length() - FileValue.DEVOPS_PURPOSE_DELIMITER.length()));
                }
                if (!silent) System.out.println("Additional devops.purpose [" + devopsPurpose + "]" + " nest level of purpose = " + nestLevel);
                nestLevel+=1;
                devopsPropertiesCustomised.setProperty(FileValue.DEVOPS_PURPOSE, "");
                fileToCustomiseContents = fileToCustomiseContents.replace("devops.purpose=" + devopsPurpose, "devops.purpose=");
                java.nio.file.Files.write(fileToCustomise.toPath(), fileToCustomiseContents.getBytes());
                Purposes nestedPurposes = getPurposesFromCommandLine(devopsPurpose);
                allPurposes.add(nestedPurposes);
                log(nestLevel, devopsPurpose);
                fileToCustomiseContents = customise(nestedPurposes, jsonFilePath, fileToCustomise);
            } else {
                finalNestedLevel = true;
                if (!silent) System.out.println(allPurposes.toString());
                java.nio.file.Files.write(fileToCustomise.toPath(), fileToCustomiseContentsOriginal.getBytes());
            }
        }
    }

    private String add(String fileToCustomiseContents, com.tinytelly.purpose.model.FileValue fileValue) {
        if (!fileToCustomiseContents.endsWith(System.getProperty("line.separator"))) {
            fileToCustomiseContents += System.getProperty("line.separator");
        }
        fileToCustomiseContents += fileValue.getPair() + System.getProperty("line.separator");
        return fileToCustomiseContents;
    }

    private String customiseFiles(com.tinytelly.purpose.model.Files files, String fileToCustomiseName, String fileToCustomiseContents) throws Exception {
        if (overrideFiles.size() > 0) {
            files.addFiles(overrideFiles);
        }
        if(files.hasFiles()) {
            for (com.tinytelly.purpose.model.File file : files.getFiles()) {
                if (fileToCustomiseName.equals(file.getName())) {
                    for (com.tinytelly.purpose.model.FileValue fileValue : file.getValues()) {
                        if (fileValue.containsDevopsKey() && fileValue.getValue() != null) {
                            String valueWithDevopsValueIncluded = fileValue.getValue();
                            valueWithDevopsValueIncluded = valueWithDevopsValueIncluded.replace(fileValue.getDevopsKey(), devopsProperties.getProperty(fileValue.getDevopsKey()));
                            valueWithDevopsValueIncluded = valueWithDevopsValueIncluded.replace(fileValue.DEVOPS_KEYWORK_START, "");
                            valueWithDevopsValueIncluded = valueWithDevopsValueIncluded.replace(fileValue.DEVOPS_KEYWORK_END, "");
                            fileValue.setValue(valueWithDevopsValueIncluded);
                        }
                        if (com.tinytelly.purpose.model.FileValue.STATE.set.equals(fileValue.getState())) {
                            if (fileValue.getName().contains(com.tinytelly.purpose.model.FileValue.STAR)) {
                                String[] result = fileValue.getName().split("\\" + com.tinytelly.purpose.model.FileValue.STAR);
                                if (result.length != 2) {
                                    throw new Exception("Incorrect use of the wildcard [" + com.tinytelly.purpose.model.FileValue.STAR + "] - usage [,*value] - not [" + fileValue.getName() + "]");
                                } else {
                                    BufferedReader bufReader = new BufferedReader(new StringReader(fileToCustomiseContents));

                                    StringBuffer replaced = new StringBuffer();
                                    String line;
                                    while ((line = bufReader.readLine()) != null) {
                                        replaced.append(replace(line, result[0], result[1]));
                                        replaced.append(System.getProperty("line.separator"));
                                    }

                                    fileToCustomiseContents = replaced.toString();
                                }
                            } else if (fileToCustomiseContents.contains(fileValue.getLeftSide())) {
                                fileToCustomiseContents = replace(fileToCustomiseContents, fileValue.getLeftSide(), fileValue.getPair(), false, file.getName());
                            }
                        } else if (com.tinytelly.purpose.model.FileValue.STATE.add.equals(fileValue.getState())) {
                            if (fileToCustomiseContents.contains(fileValue.getPair())) {
                                if (!silent) {
                                    System.out.println("Already replaced [" + fileValue.getPair() + "]");
                                }
                            } else {
                                fileToCustomiseContents = add(fileToCustomiseContents, fileValue);
                            }
                        } else if (com.tinytelly.purpose.model.FileValue.STATE.replace.equals(fileValue.getState())) {
                            fileToCustomiseContents = replace(fileToCustomiseContents, fileValue.getNameForRegex(), fileValue.getValue(), fileValue.canUseRegex());
                        } else if (FileValue.STATE.overwrite.equals(fileValue.getState())) {
                            fileToCustomiseContents = overwrite(fileToCustomiseContents, fileValue);
                        }
                    }
                }
            }
        }
        return fileToCustomiseContents;
    }
    private String replace(String fileToCustomiseContents, String findThis, String replaceWithThis, boolean isRegex, String fileName) {
        if (com.tinytelly.purpose.model.File.DEVOPS_PROPERTIES.equals(fileName)) {
            if (fileToCustomiseContents.contains(replaceWithThis)) {
                if (!silent) System.out.println("This value already customised [" + replaceWithThis + "]");
                return fileToCustomiseContents;
            }
        }
        return replace(fileToCustomiseContents, findThis, replaceWithThis, isRegex);
    }

    private String overwrite(String fileToCustomiseContents, com.tinytelly.purpose.model.FileValue fileValue) {
        if (!silent) System.out.println("overwrite: " + fileValue.getPair());
        fileToCustomiseContents = fileToCustomiseContents.replaceAll(fileValue.getNameForRegex() + "=.*", "");
        return add(fileToCustomiseContents, fileValue);
    }

    private String replace(String fileToCustomiseContents, String findThis, String replaceWithThis, boolean isRegex) {
        if (replaceWithThis.contains("=") && fileToCustomiseContents.contains(replaceWithThis) && !replaceWithThis.endsWith("=")) {
            if (!silent) System.out.println("Already replaced [" + replaceWithThis + "]");
            return fileToCustomiseContents;
        }
        if (!silent) System.out.println("Replacing [" + findThis + "] with [" + replaceWithThis + "]");
        if (findThis.startsWith(FileValue.DEVOPS_PURPOSE)) {
            replaceWithThis += FileValue.DEVOPS_PURPOSE_DELIMITER;
        }
        StringBuilder sb = new StringBuilder();
        Splitter splitter = Splitter.on(System.getProperty("line.separator"));
        for (String line : splitter.split(fileToCustomiseContents)) {
            if (isRegex) {
                sb.append(line.replaceAll(findThis, replaceWithThis));
            } else {
                sb.append(line.replace(findThis, replaceWithThis));
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    public static void main(String... args) throws Exception {
        if (args.length < 3 || args.length > 4) {
            System.out.println("Usage: 3 or 4 arguments required, Argument 4 is optional");
            System.out.println("Argument 1: a String containing a purpose, Multiple purposes separate by |");
            System.out.println("Argument 2: a String containing a full path to a json file called devops.json");
            System.out.println("Argument 3: a String containing a full path to a file to customise");
            System.out.println("Argument 4: a String containing a full path to a file devops.properties");
        } else {
            FileCustomiser fileCustomiser = new FileCustomiser();
            String devopsFile = null;
            if (args.length == 4) {
                devopsFile = args[3];
            }
            fileCustomiser.customise(getPurposesFromCommandLine(args[0]), args[1], args[2], devopsFile);
        }
    }

    protected static com.tinytelly.purpose.model.Purposes getPurposesFromCommandLine(String commandLinePurposes) {
        commandLinePurposes += "|devops";
        com.tinytelly.purpose.model.Purposes purposes = new com.tinytelly.purpose.model.Purposes();
        List<com.tinytelly.purpose.model.Purpose> purposeList = new ArrayList<com.tinytelly.purpose.model.Purpose>();

        if (!silent) System.out.println("Purposes[" + commandLinePurposes + "]");
        List<String> list = Lists.newArrayList(Splitter.on(FileValue.DEVOPS_PURPOSE_DELIMITER).omitEmptyStrings().trimResults().split(commandLinePurposes));
        for (String str : list) {
            com.tinytelly.purpose.model.Purpose purpose = new com.tinytelly.purpose.model.Purpose(str);
            purposeList.add(purpose);
            purposes.setPurposes(purposeList);
        }

        return purposes;
    }

    private java.io.File applyOverrides(String jsonFilePath, com.tinytelly.purpose.model.Purposes intendedPurposes) throws Exception {
        java.io.File fileToCustomise = new java.io.File(jsonFilePath);
        java.io.File fileToCustomiseTemp = new java.io.File(fileToCustomise.getParent() + java.io.File.separator + "OverridesApplied_" + fileToCustomise.getName());
        FileUtils.copyFile(fileToCustomise, fileToCustomiseTemp);

        String fileToCustomiseContents = FileUtils.readFileToString(fileToCustomiseTemp, "UTF-8");

        if (intendedPurposes != null) {
            for (com.tinytelly.purpose.model.Purpose intendedPurpose : intendedPurposes.getPurposes()) {
                if (intendedPurpose.hasValue()) {
                    fileToCustomiseContents = replace(fileToCustomiseContents, "{" + intendedPurpose.getName() + "}", intendedPurpose.getValue(), false);
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

    private Properties getDevopsProperties(String devopsPropertiesPath) throws Exception {
        if (devopsPropertiesPath == null) {
            return new XProperties();
        }
        java.io.File devopsFile = new java.io.File(devopsPropertiesPath);
        return convert(devopsFile);
    }

    protected String replace(String line, String start, String end) {
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
