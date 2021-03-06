package com.chaosenterprise.goodreads;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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

	private static final By BOOK_TITLE = By.xpath("//h1[@id='bookTitle']");

	private static final By NEXT_PAGE = By.xpath("//a[@rel='next' and @class='next_page']");

	private WebDriver webDriver;

	private String search;

	public ImageDownloader() {
		getWebDriver();
	}

	public void quit() {
		getWebDriver().quit();
	}

	private WebDriver getWebDriver() {
		log.info("webdriver: {}", webDriver);
		if (webDriver == null || webDriver.toString()
											.contains("null") || webDriver.getWindowHandles() == null || webDriver.getWindowHandles()
																													.isEmpty()) {
			try {
				ChromeOptions options = new ChromeOptions();
				options.addExtensions(new File("Drivers/Chrome/uBlock-Origin_v1.11.0.crx"), new File("Drivers/Chrome/Ghostery_v7.1.2.3.crx"));
				webDriver = new ChromeDriver(options);
			} catch (Exception e) {
				log.error("{}", e);
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

	public ImageDownloader openEditionPage() {
		WebElement moreDetails = waitForElementClickable(MORE_DETAILS);
		moreDetails.click();

		WebElement button = waitForElementClickable(OTHER_EDITIONS);
		button.click();
		return this;
	}

	public void saveAllCoverImages() {
		log.info("Saving all cover images for {}", search);

		findAllEditionLinks().stream()
								.forEach(x -> {
									try {
										log.info("Processing edition {}", x);
										getWebDriver().get(x);
										saveCoverImage();
									} catch (TimeoutException e) {
										log.warn("{}", e);
									} catch (Exception e) {
										log.error("{}", e);
										quit();
									}

								});

		log.info("Finished saving all cover images for {}", search);

	}

	private List<String> findAllEditionLinks() {
		List<String> editions = new ArrayList<String>();

		try {
			while (true) {
				editions.addAll(getWebDriver().findElements(BOOK_ELEMENTS)
												.stream()
												.map(x -> x.getAttribute("href"))
												.collect(Collectors.toList()));

				WebElement nextPage = waitForElementClickable(NEXT_PAGE);
				nextPage.click();

			}

		} catch (TimeoutException e) {
			log.warn("{}", e);
		} catch (Exception e) {
			log.error("{}", e);
			quit();
		}

		log.debug("Editions: {}", editions);

		return editions;
	}

	private String getFileName() {
		WebElement title = getWebDriver().findElement(BOOK_TITLE);

		String fileName = String.format("%s %s.png", title.getText(), UUID.randomUUID())
								.replaceAll("[\\<\\>\\:\\\\\"\\/\\|\\?\\*]", "");

		log.info("filename {}", fileName);

		return fileName;
	}

	private void saveCoverImage() throws MalformedURLException, IOException {

		WebElement image = waitForElementPresenet(COVER_IMAGE);

		BufferedImage bi = ImageIO.read(new URL(image.getAttribute("src")));

		File f = new File("Images" + File.separator + search);
		if (!f.exists()) {
			f.mkdirs();
		}

		File output = new File("Images" + File.separator + search, getFileName());

		ImageIO.write(bi, "png", output);

	}

	private WebElement waitForElementClickable(By field) {
		log.info("Waiting for element to be clickable {}", field);
		WebElement webElement = new WebDriverWait(getWebDriver(), 10).until(ExpectedConditions.elementToBeClickable(field));
		log.info("Found weblement for field {}", field);
		return webElement;
	}

	private WebElement waitForElementPresenet(By field) {
		log.info("Waiting for element to be clickable {}", field);
		WebElement webElement = new WebDriverWait(getWebDriver(), 10).until(ExpectedConditions.presenceOfElementLocated(field));
		log.info("Found weblement for field {}", field);
		return webElement;
	}

}
