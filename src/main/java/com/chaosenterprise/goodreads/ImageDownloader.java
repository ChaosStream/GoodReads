package com.chaosenterprise.goodreads;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageDownloader {

	private static final Logger log = LoggerFactory.getLogger(ImageDownloader.class);

	private final static String BASE_URL = "https://www.goodreads.com";

	private final static String SEARCH_URL = BASE_URL + "/search";

	private final static By SEARCH_BUTTON = By.xpath("//input[contains(@class,'searchBox__button')]");

	private final static By SEARCH_INPUT = By.xpath("//form[contains(@name,'searchForm')]//input[contains(@class,'searchBox__input')]");

	private final static By FIRST_SEARCH_RESULT = By.xpath("//tbody/tr[1]//a[contains(@class,'bookTitle')]");

	private final static By MORE_DETAILS = By.xpath("//a[contains(@class,'actionLinkLite') and text() = 'More Details...']");

	private final static By OTHER_EDITIONS = By.xpath("//a[contains(@class,'actionLinkLite') and text() = 'All Editions']");

	private final static By COVER_IMAGE = By.xpath("//div[contains(@class,'editionCover')]/img");

	private final static By BOOK_ELEMENTS = By.xpath("//a[contains(@class,'bookTitle')]");

	private WebDriver webDriver;

	private String search;

	public ImageDownloader() {
		getWebDriver();
	}

	public void quit() {
		getWebDriver().quit();
	}

	private WebDriver getWebDriver() {
		if (webDriver == null) {
			try {
				webDriver = new ChromeDriver();

			} catch (Exception e) {
				log.warn(e.getLocalizedMessage());
				e.printStackTrace();
			}

		}

		return webDriver;
	}

	public ImageDownloader searchForBook(String name) {
		this.search = name;

		getWebDriver().get(SEARCH_URL);

		WebElement inputField = waitForElementClickable(SEARCH_INPUT);
		inputField.sendKeys(name);

		WebElement button = waitForElementClickable(SEARCH_BUTTON);
		button.click();

		return this;
	}

	public ImageDownloader loadFirstResult() {
		WebElement firstResult = waitForElementClickable(FIRST_SEARCH_RESULT);
		firstResult.click();
		return this;
	}

	public ImageDownloader getAllEditions() {
		WebElement moreDetails = waitForElementClickable(MORE_DETAILS);
		moreDetails.click();

		WebElement button = waitForElementClickable(OTHER_EDITIONS);
		button.click();
		return this;
	}

	public void saveAllCoverImages() {
		log.info("Saving all cover images for {}", search);

		List<String> urls = getWebDriver().findElements(BOOK_ELEMENTS)
											.stream()
											.map(x -> x.getAttribute("href"))
											.collect(Collectors.toList());

		urls.stream()
			.forEach(x -> {
				try {
					getWebDriver().get(x);
					saveCoverImage();
					Thread.sleep(2_000);
				} catch (Exception e) {
					log.error(e.getLocalizedMessage());
				}

			});

		log.info("Finished saving all cover images for {}", search);

	}

	private String getFileName() {
		String fileNameFormat = "%s %s.png";

		String isbn = "";

		WebElement moreDetails = waitForElementClickable(MORE_DETAILS);
		moreDetails.click();

		WebElement title = getWebDriver().findElement(By.xpath("//h1[@id='bookTitle']"));

		try {
			isbn = getWebDriver().findElement(By.xpath("//div[contains(@class,'infoBoxRowTitle') and text() = 'ISBN']/../div[contains(@class,'infoBoxRowItem')]"))
									.getText();
		} catch (NoSuchElementException e) {
			log.warn(e.getLocalizedMessage());
		}

		try {
			isbn = getWebDriver().findElement(By.xpath("//div[contains(@class,'infoBoxRowTitle') and text() = 'ASIN']/../div[contains(@class,'infoBoxRowItem')]"))
									.getText();
		} catch (NoSuchElementException e) {
			log.warn(e.getLocalizedMessage());
		}

		if (isbn == null || isbn.isEmpty()) {
			isbn = String.valueOf(new Random().nextLong()) + "AAAAAAAAAAAA";
		}

		log.info("filename {}", String.format(fileNameFormat, title.getText(), isbn.substring(0, 9)
																					.trim()));

		return String.format(fileNameFormat, title.getText()
													.trim(), isbn.substring(0, 9)
																	.trim());

	}

	private void saveCoverImage() {

		WebElement image = waitForElementPresenet(COVER_IMAGE);

		BufferedImage bi;
		try {
			bi = ImageIO.read(new URL(image.getAttribute("src")));

			File f = new File("Images" + File.separator + search);
			if (!f.exists()) {
				f.mkdirs();
			}

			File output = new File("Images" + File.separator + search, getFileName());

			ImageIO.write(bi, "png", output);
		} catch (IOException e) {
			log.error("failed to save image {}", image);
			e.printStackTrace();
		}

	}

	private WebElement waitForElementClickable(By field) {
		log.info("Waiting for element to be clickable {}", field);
		WebElement webElement = new WebDriverWait(getWebDriver(), 30).until(ExpectedConditions.elementToBeClickable(field));
		log.info("Found weblement for field {}", field);
		return webElement;
	}

	private WebElement waitForElementPresenet(By field) {
		log.info("Waiting for element to be clickable {}", field);
		WebElement webElement = new WebDriverWait(getWebDriver(), 30).until(ExpectedConditions.presenceOfElementLocated(field));
		log.info("Found weblement for field {}", field);
		return webElement;
	}

}
