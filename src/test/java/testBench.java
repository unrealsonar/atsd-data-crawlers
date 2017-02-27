import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class testBench {

    private static final int commandsNumber = 3;
    private static final int metacommandsNumber = 100;
    private static final int columnsNumber = 6;
    public static void main (String[] args) throws IOException, InterruptedException {

/*
        System.setProperty("webdriver.chrome.driver", "chromedriver");
        WebDriver driver = new ChromeDriver();
*/

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,"phantomjs");
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,
                new String[] {"--web-security=no", "--ignore-ssl-errors=yes"});

        Properties pr = new Properties();
        try {
            FileInputStream f = new FileInputStream("src/main/resources/data.properties");
            pr.load(f);
        } catch (Exception e) {
            System.out.println("property file data.properties is not loaded");
        }
        String collector = pr.getProperty("collector");
        String username = pr.getProperty("username");
        String password = pr.getProperty("password");
        String url = "https://data.cityofchicago.org/api/views/7edu-s3u7";

        //authentication
        WebDriver driver = new PhantomJSDriver(caps);
        driver.get(collector);
        WebElement field = driver.findElement(By.id("username"));
        field.sendKeys(username);
        field = driver.findElement(By.name("password"));
        field.sendKeys(password);
        field.submit();

        WebElement link = driver.findElement(By.linkText("Jobs"));
        link.click();

        link = driver.findElement(By.linkText("autosocrata"));
        link.click();

        link = driver.findElement(By.linkText("Create from URL"));
        link.click();

        field = driver.findElement(By.id("inputWizardUrl"));
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        field.sendKeys(url);

        link = driver.findElement(By.id("btnWizardCreate"));
        link.click();

/*
        File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File("image.png"));
 */

        //[dataset]
        int l, j;
        String cat;
        int datasetlen = driver.findElements(By.xpath("//*[@id=\"tblSummaryInfo\"]/tbody/tr")).size();
        String data = "\n", desc = "";
        WebElement temp;

/*
        //contents
        for (l = 2; l <= datasetlen; l++) {
            temp = driver.findElement(By.xpath("//*[@id=\"tblSummaryInfo\"]/tbody/tr[" + l + "]/td[1]"));
//            if (!temp.getText().equals("Description")) {
                data += temp.getText();
                data += " = ";
                temp = driver.findElement(By.xpath("//*[@id=\"tblSummaryInfo\"]/tbody/tr[" + l + "]/td[2]"));
                data += temp.getText();
                data += "\n";
            } else {
                temp = driver.findElement(By.xpath("//*[@id=\"tblSummaryInfo\"]/tbody/tr[" + l + "]/td[2]"));
                System.out.print(temp.getText());
                temp.click();

                File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
                FileUtils.copyFile(scrFile, new File("image.png"));

                temp = driver.findElement(By.xpath(
                        "//*[@id=\"tblSummaryInfo\"]/tbody/tr[" + l + "]/td[2]/div/div[2]/div"));
                desc = temp.getText();
            }
        }

        //[columns]
        int rowsNumber = driver.findElements(By.xpath("//*[@id=\"tblColumnInfos\"]/tbody/tr")).size();
        String[][] cols = new String[rowsNumber][columnsNumber];
        //table head
        for (l = 0; l < columnsNumber; l++) {
            temp = driver.findElement(By.xpath("//*[@id=\"tblColumnInfos\"]/tbody/tr[2]/th[" + (l + 1) + "]"));
            cols[0][l] = temp.getText();
        }
        //contents
        for (j = 1; j < rowsNumber - 1; j++) {
            for (l = 0; l < columnsNumber; l++) {
                temp = driver.findElement(By.xpath(
                        "//*[@id=\"tblColumnInfos\"]/tbody/tr[" + (j + 2) + "]/td[" + (l + 1) + "]"));
                cols[j][l] = temp.getText();
            }
        }

         //[time]
        field = driver.findElement(By.id("timeField_0"));
        String time = field.getAttribute("value");

        field = driver.findElement(By.id("timeFormat_0"));
        String format = field.getAttribute("value");

        //[series]
        field = driver.findElement(By.id("metricPrefix_0"));
        String prefix = field.getAttribute("value");

        field = driver.findElement(By.id("includedFields_0"));
        String included = field.getAttribute("value");

        field = driver.findElement(By.id("excludedFields_0"));
        String excluded = field.getAttribute("value");

        field = driver.findElement(By.id("annotationFields_0"));
        String annotation = field.getAttribute("value");
*/

        //[commands]
        int count = 0;
        j = 1;
        String atrClass;
        String[] commands = new String[commandsNumber];
        String[] metacommands = new String[metacommandsNumber];

        temp = driver.findElement(By.xpath("//*[@id=\"testRow_2\"]/td/table/tbody/tr[3]/td[2]"));

        String[] strings = temp.getText().split("\n");

        for (int i = 0; i < commandsNumber; i++) {
            if (strings[i].substring(0, 6).equals("series")) {
                commands[count++] = "\n" + strings[i];
            }
        }

        count = 0;
        for (int i = strings.length-1; i >= 0; i--) {
            if (!strings[i].substring(0, 6).equals("series")) {
                metacommands[count++] = "\n" + strings[i];
            }
        }

/*
        do {
            File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, new File("image.png"));
            //temp = driver.findElement(By.xpath("//*[@id=\"testRow_2\"]/td/table/tbody/tr[3]/td[2]/pre/span[" + j + "]"));
                    //*[@id="testRow_2"]/td/table/tbody/tr[3]/td[2]/pre/span[1]
            temp = driver.findElement(By.xpath("//*[@id=\"testRow_2\"]/td/table/tbody/tr[3]/td[2]"));

            commands = temp.getText().split("\n")[1..3];

            System.out.print(temp.getText());
            cat = temp.getText();
            atrClass = temp.getAttribute("class");

            if (atrClass.equals("cm-keyword")) {
                if (cat.equals("series")) {
                    cat = "\n" + cat;
                    count++;
                } else {
                    break;
                }
            }
            if ((atrClass.equals("cm-keyword")) || atrClass.equals("cm-attribute")) {
                cat += " ";
            }
            j++;
            if (count <= commandsNumber) {
                commands[count - 1] += cat;
            }
        } while (count <= commandsNumber);
*/

        driver.quit();
    }
}