package com.tinytelly.purpose;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class FileCustomiserTest {

    public File purposeProperties;
    public File purposePropertiesInTest;
    private FileCustomiser fileCustomiser = new FileCustomiser();
    public File purposeJson;

    @Before
    public void setUp() throws IOException {
        File path = new File(this.getClass().getResource("FileCustomiserTest.class").getPath());

        purposeProperties = new File(path.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParent() + File.separator + "purpose.properties");
        purposePropertiesInTest = new File(purposeProperties.getParent() + "/tmp/" + purposeProperties.getName());
        FileUtils.copyFile(purposeProperties, purposePropertiesInTest);

        purposeJson = new File(purposeProperties.getParent() + File.separator + "purpose.json");
        Assert.assertTrue(purposeJson.exists());
    }

    @Test
    public void customiseProperties() throws Exception {
        fileCustomiser.customise(FileCustomiser.getPurposesFromCommandLine("australia"), purposeJson.getAbsolutePath(), purposePropertiesInTest.getAbsolutePath(), purposePropertiesInTest.getAbsolutePath());
        String fileToCustomiseContents = FileUtils.readFileToString(purposePropertiesInTest);
        Assert.assertTrue(fileToCustomiseContents.contains("country.capital=Canberra"));
        Assert.assertTrue(fileToCustomiseContents.contains("country.population=24 million"));
        Assert.assertTrue(fileToCustomiseContents.contains("country.name=Australia"));

        fileCustomiser.customise(FileCustomiser.getPurposesFromCommandLine("greeting=Hello"), purposeJson.getAbsolutePath(), purposePropertiesInTest.getAbsolutePath(), purposePropertiesInTest.getAbsolutePath());
        fileToCustomiseContents = FileUtils.readFileToString(purposePropertiesInTest);
        Assert.assertTrue(fileToCustomiseContents.contains("country.greeting=In Australia a typical greeting is Hello"));
    }
}
