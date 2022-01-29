package info.llort.torrent.util;

import com.google.common.net.HttpHeaders;
import info.llort.torrent.bean.PageLinkInfo;
import net.lightbody.bmp.BrowserMobProxy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.fusesource.jansi.Ansi.Color.WHITE;

public class PctmixWebParserV2 {
	public static void capture(String urlWebToParse, String geckoDriverPath, List<String> filters, long downloadTimeOut, WebDriver driver, BrowserMobProxy proxy) throws IOException, InterruptedException {
		Set<PageLinkInfo> mainPageLinks = findMainPageLinks(urlWebToParse, geckoDriverPath, filters, driver);
		for (PageLinkInfo pli : mainPageLinks) {
			Console.println("Main page link: " + pli.getUrl(), WHITE);
		}

		Set<PageLinkInfo> torrentPageLinks = findPageTorrentLinks(mainPageLinks, driver, proxy);
		for (PageLinkInfo pli : torrentPageLinks) {
			Console.println("Torrent page link: " + pli.getUrl(), WHITE);
		}

		Set<String> downloadTorrentLinks = downloadTorrentLinks(torrentPageLinks, driver, proxy);
		for (String link : downloadTorrentLinks) {
			Console.println("Download link: " + link, WHITE);
			downloadTorrentFile(link, driver, downloadTimeOut);
		}
	}

	public static Set<PageLinkInfo> findMainPageLinks(String url, String geckoDriverPath, List<String> filters, WebDriver driver) throws IOException {
		// Inspired by https://www.javatpoint.com/selenium-webdriver-running-test-on-firefox-browser-gecko-driver
		driver.get(url);

		Set<PageLinkInfo> links = new HashSet<>();
		Document doc = Jsoup.parse(driver.getPageSource());
		Elements elements = doc.select("a[href]");
		for (Element element : elements) {
			String value = element.attr("href");
			if (value.startsWith("https:")) {
				if ((value.contains("/descargar/peliculas-castellano/") && value.contains("blurayrip")) ||
					(value.contains("/descargar/peliculas-x264-mkv/") && value.contains("bluray")) ||
					(value.contains("/descargar/cine-alta-definicion-hd/") && value.contains("bluray")) ||
					value.contains("/descargar/serie/") ||
					value.contains("/descargar/serie-en-hd/") ||
					value.contains("/descargar/serie-4k/")) {
					for (String filter : filters) {
						if (value.toLowerCase().contains(filter.toLowerCase())) {
							// /descargar/ must be replaced by /descargar/torrent/
							PageLinkInfo pli = new PageLinkInfo();
							pli.setReferer(value);
							value = value.replace("/descargar/", "/descargar/torrent/");
							pli.setUrl(value);
							links.add(pli);
							break;
						}
					}
				}
			}
		}

		return links;
	}

	public static Set<PageLinkInfo> findPageTorrentLinks(Set<PageLinkInfo> pageLinks, WebDriver driver, BrowserMobProxy proxy) throws IOException {
		Set<PageLinkInfo> links = new HashSet<>();
		for (PageLinkInfo pli :  pageLinks) {
			// Setting referer before jump to the page
			proxy.addHeader(HttpHeaders.REFERER, pli.getReferer());
			driver.get(pli.getUrl());

			String htmlContent = driver.getPageSource();
			//System.out.println(htmlContent);
			String regex = "window.open\\(\"(.*?)\"\\)";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(htmlContent);
			while (matcher.find()) {
				String tLink = matcher.group(1);
				tLink = "https:" + tLink; // add https at the begining
				PageLinkInfo newPli = new PageLinkInfo();
				newPli.setUrl(tLink);
				newPli.setReferer(pli.getUrl());
				links.add(newPli);
			}
		}
		return links;
	}

	public static Set<String> downloadTorrentLinks(Set<PageLinkInfo> pageLinks, WebDriver driver, BrowserMobProxy proxy) throws IOException {
		Set<String> links = new HashSet<>();
		for (PageLinkInfo pli :  pageLinks) {
			// Setting referer before jump to the page
			proxy.addHeader(HttpHeaders.REFERER, pli.getReferer());
			driver.get(pli.getUrl());

			Document doc = Jsoup.parse(driver.getPageSource());
			Elements elements = doc.select("a[href]");
			for (Element element : elements) {
				String value = element.attr("data-u");
				if (value.startsWith("https:") && value.contains("/download-link/")) {
					links.add(value);
				}
			}
		}
		return links;
	}

	public static void downloadTorrentFile(String link, WebDriver driver, long timeOut) throws InterruptedException {
		try {
			// Set timeout otherwise the driver lock
			driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(timeOut));
			driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeOut));
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeOut));
			driver.navigate().to(link);
		} catch (Exception e) {
			// Silent error
		}
	}
}
