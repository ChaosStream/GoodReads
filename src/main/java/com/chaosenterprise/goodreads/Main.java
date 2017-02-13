package com.chaosenterprise.goodreads;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

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
					.forEach(x -> {
						try {
							imageDownload.searchForBook(x)
											.loadFirstResult()
											.openEditionPage()
											.saveAllCoverImages();
						} catch (Exception e) {
							log.error(e.getLocalizedMessage());
						}
					});
		} finally {

			imageDownload.quit();
		}
	}

	private static void setup() throws IOException {

		File f = new File("Drivers/Chrome");
		if (!f.exists()) {
			f.mkdirs();
		}

		String[] filesToExport = new String[] { "Ghostery_v7.1.2.3.crx", "uBlock-Origin_v1.11.0.crx", "chromedriver.exe" };

		for (String file : filesToExport) {
			URL url = Main.class.getClassLoader()
								.getResource(file);

			File f2 = new File("Drivers/Chrome" + File.separator + file);
			if (!f2.exists()) {
				f2.createNewFile();
				FileUtils.copyURLToFile(url, f2);
			}

			if (file.contains("chromedriver")) {
				System.setProperty("webdriver.chrome.driver", f2.getAbsolutePath());
			}

		}

	}

}
