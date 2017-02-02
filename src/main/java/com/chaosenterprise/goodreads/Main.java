package com.chaosenterprise.goodreads;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

public class Main {

	private Main() {
	}

	public static void main(String[] args) throws IOException {
		List<String> books = new ArrayList<String>();

		Scanner sc = new Scanner(new File("search.txt"));

		while (sc.hasNextLine()) {
			books.add(sc.nextLine());
		}

		sc.close();

		setup();

		ImageDownloader imageDownload = new ImageDownloader();
		try {

			books.stream()
					.forEach(x -> imageDownload.searchForBook(x)
												.loadFirstResult()
												.getAllEditions()
												.saveAllCoverImages());
		} finally {

			imageDownload.quit();
		}
	}

	private static void setup() throws IOException {
		URL resource = Main.class.getClassLoader()
									.getResource("chromedriver.exe");

		File f = new File("Drivers");
		if (!f.exists()) {
			f.mkdirs();
		}
		File chromeDriver = new File("Drivers" + File.separator + "chromedriver.exe");
		if (!chromeDriver.exists()) {
			chromeDriver.createNewFile();
			FileUtils.copyURLToFile(resource, chromeDriver);
		}
		System.setProperty("webdriver.chrome.driver", chromeDriver.getAbsolutePath());
	}

}
