package com.axibase.energinet.browser;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EnerginetGrabber
        implements Grabber {
    private static final Logger logger = LoggerFactory.getLogger(EnerginetGrabber.class);
    private static final String FORM_URI = "http://www.energinet.dk/_layouts/Markedsdata/framework/integrations/markedsdatatemplate.aspx?language=en";
    private static final String DOWNLOAD_XLS_URI = "http://www.energinet.dk/_layouts/Markedsdata/Framework/Integrations/MarkedsdataExcelOutput.aspx";
    private DesiredCapabilities caps;
    private PhantomJSDriver driver;

    public EnerginetGrabber(String phantomJsExecutablePath) {
        this.caps = new DesiredCapabilities();
        this.caps.setCapability("phantomjs.binary.path", phantomJsExecutablePath);
        this.driver = new PhantomJSDriver(this.caps);
    }

    private void downloadFile(String path) throws IOException {
        logger.debug("Starting download file from URI: {}", DOWNLOAD_XLS_URI);
        URL website = new URL(DOWNLOAD_XLS_URI);
        File file = new File(path);
        File parent = file.getParentFile();
        if ((!parent.mkdirs()) && (!parent.exists())) {
            throw new IllegalStateException(String.format("Failed to create download path %s", parent.getAbsolutePath()));
        }
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        FileOutputStream fos = new FileOutputStream(path);
        fos.getChannel().transferFrom(rbc, 0L, Long.MAX_VALUE);
        rbc.close();
        fos.close();
        logger.debug("File downloaded to {}", path);
    }

    private void fillForm(Date start, Date end) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        logger.info("Try to get Form from: {}", "http://www.energinet.dk/_layouts/Markedsdata/framework/integrations/markedsdatatemplate.aspx?language=en");
        this.driver.get("http://www.energinet.dk/_layouts/Markedsdata/framework/integrations/markedsdatatemplate.aspx?language=en");

        logger.info("Set up form params");
        WebElement startDate = this.driver.findElement(By.name("startDate"));
        startDate.clear();
        startDate.sendKeys(simpleDateFormat.format(start));

        WebElement endDate = this.driver.findElement(By.name("endDate"));
        endDate.clear();
        endDate.sendKeys(simpleDateFormat.format(end));

        List<WebElement> checkboxes = this.driver.findElements(By.xpath(".//input[@type=\"checkbox\"]"));
        for (WebElement checkbox : checkboxes) {
            if (!checkbox.isSelected()) {
                checkbox.click();
            }
        }

        String optionValueTemplate = ".//option[@value=\"%s\"]";
        Map<String, String> optionByList = new HashMap<String, String>();
        optionByList.put("CurrencyCode", String.format(optionValueTemplate, "EUR"));
        optionByList.put("DecimalFormat", String.format(optionValueTemplate, "english"));
        optionByList.put("DateFormat", String.format(optionValueTemplate, "other"));
        optionByList.put("ExtractMode", String.format(optionValueTemplate, "excel"));

        for (String listId : optionByList.keySet()) {
            WebElement list = this.driver.findElement(By.id(listId));
            list.click();
            String listOptions = optionByList.get(listId);
            WebElement option = list.findElement(By.xpath(listOptions));
            option.click();
        }

        WebElement download = this.driver.findElement(By.xpath(".//*[@value=\"Get extract\"]"));
        download.click();
        logger.info("Form filled");
    }

    public void grab(String path, Date startDate, Date endDate) {
        fillForm(startDate, endDate);
        try {
            downloadFile(path);
        } catch (IOException e) {
            logger.error("Failed to download statistics file, reason: {}", e);
        }
    }

    protected void finalize() throws Throwable {
        this.driver.quit();
        this.driver.close();
    }
}
