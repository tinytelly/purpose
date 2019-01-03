package com.tinytelly.purpose;

import com.tinytelly.purpose.model.*;
import com.google.common.collect.Ordering;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tinytelly.purpose.FileCustomiser;
import org.apache.commons.io.FileUtils;
import org.junit.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

public class FileCustomiserTest {
    public File devopsProperties;
    public File devopsPropertiesInTest;
    private FileCustomiser fileCustomiser = new FileCustomiser();
    public File realDevopsJson;
    public File realDevopsJsonInTest;
    public File realReadmeMd;
    private int randomNumber;

    @Before
    public void setUp() throws IOException {
        randomNumber = ThreadLocalRandom.current().nextInt(1, 9999 + 1);
        File path = new File(this.getClass().getResource("FileCustomiserTest.class").getPath());
        File rootFile = path.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();

        devopsProperties = new File(rootFile + File.separator + com.tinytelly.purpose.model.File.DEVOPS_PROPERTIES);
        devopsPropertiesInTest = new File(rootFile + "/tmp/" + randomNumber + "/" + devopsProperties.getName());
        FileUtils.copyFile(devopsProperties, devopsPropertiesInTest);

        realDevopsJson = new File(rootFile + File.separator + "devops.json");
        realDevopsJsonInTest = new File(realDevopsJson.getParent() + "/tmp/" + realDevopsJson.getName());
        FileUtils.copyFile(realDevopsJson, realDevopsJsonInTest);
        Assert.assertTrue(realDevopsJson.exists());

        realReadmeMd = new File(rootFile + File.separator + "readme.md");
        Assert.assertTrue(realReadmeMd.exists());
    }

    @Test
    public void customiseProperties() throws Exception {
        fileCustomiser.customise(FileCustomiser.getPurposesFromCommandLine("australia"), realDevopsJsonInTest.getAbsolutePath(), devopsPropertiesInTest.getAbsolutePath(), devopsPropertiesInTest.getAbsolutePath());
        Properties properties = fileCustomiser.convert(devopsPropertiesInTest);
        Assert.assertEquals("Canberra", properties.getProperty("country.capital"));
        Assert.assertEquals("24 million", properties.getProperty("country.population"));
        Assert.assertEquals("Australia", properties.getProperty("country.name"));

        fileCustomiser.customise(FileCustomiser.getPurposesFromCommandLine("greeting=Gidday"), realDevopsJsonInTest.getAbsolutePath(), devopsPropertiesInTest.getAbsolutePath(), devopsPropertiesInTest.getAbsolutePath());
        properties = fileCustomiser.convert(devopsPropertiesInTest);
        Assert.assertEquals("In Australia people say Gidday mate", properties.getProperty("country.greeting"));
    }

    @Test
    public void updateReadme() throws Exception {
        StringBuilder md = new StringBuilder("Current list of Purposes" + System.lineSeparator());
        md.append("```").append(System.lineSeparator());;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Products products = gson.fromJson(new FileReader(realDevopsJson), com.tinytelly.purpose.model.Products.class);
        for (Product product : products.getProducts()) {
            if(product.hasPurposes()) {
                md.append(product.getName() + System.lineSeparator());
                for (Purpose purpose : product.getPurposes().getPurposes()) {
                    md.append("   " + purpose.getName() + System.lineSeparator());
                }
            }
        }
        md.append("```");

        String readmeContents = FileUtils.readFileToString(realReadmeMd, "UTF-8");
        readmeContents = readmeContents.substring(0, readmeContents.lastIndexOf("Current list of Purposes"));
        readmeContents += md.toString();
        FileUtils.writeStringToFile(realReadmeMd, readmeContents, "UTF-8");
    }

    @Test
    public void notExistingPurpose() throws Exception {
        fileCustomiser.customise(FileCustomiser.getPurposesFromCommandLine("elvis-works-in-a-shop=true"), realDevopsJsonInTest.getAbsolutePath(), devopsPropertiesInTest.getAbsolutePath(), devopsPropertiesInTest.getAbsolutePath());

        Properties properties = fileCustomiser.convert(devopsPropertiesInTest);
        Assert.assertEquals("true",properties.getProperty("elvis.works.in.a.shop"));
    }
    @Test
    public void devopsJsonOrderedCorrectly() throws Exception {
        verifyJsonIsOrdered("devops");
    }

    private void verifyJsonIsOrdered(String projectName) throws IOException {
        ArrayList<String> purposes = new ArrayList<String>(), purposesSorted = new ArrayList<String>();
        String devopsJson = FileUtils.readFileToString(realDevopsJson, "UTF-8");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        com.tinytelly.purpose.model.Products products = gson.fromJson(devopsJson, com.tinytelly.purpose.model.Products.class);
        for (com.tinytelly.purpose.model.Product product : products.getProducts()) {
            if (product.getName().equals(projectName) && product.hasPurposes()) {
                for (com.tinytelly.purpose.model.Purpose purpose : product.getPurposes().getPurposes()) {
                    purposes.add(purpose.getName());
                    purposesSorted.add(purpose.getName());
                }
            }
        }
        Collections.sort(purposesSorted, String.CASE_INSENSITIVE_ORDER);
        if(!Ordering.natural().isOrdered(purposes)){
            for(int i=0; i<purposesSorted.size(); i++){
                Assert.assertEquals(purposesSorted.get(i),purposes.get(i));
            }
        }
    }
}