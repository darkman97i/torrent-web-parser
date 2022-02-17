package info.llort.torrent.util;

import com.google.common.net.HttpHeaders;
import info.llort.torrent.bean.PageLinkInfo;
import net.lightbody.bmp.BrowserMobProxy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.fusesource.jansi.Ansi.Color.*;

public class PctmixWebParserV4 {
	public static void capture(String urlWebToParse, String geckoDriverPath, List<String> filters, long downloadTimeOut, WebDriver driver, BrowserMobProxy proxy) throws IOException, InterruptedException {
		Set<PageLinkInfo> mainPageLinks = findMainPageLinks(urlWebToParse, geckoDriverPath, filters, driver);
		for (PageLinkInfo pli : mainPageLinks) {
			Console.println("Main page link: " + pli.getUrl(), WHITE);
		}

		Set<PageLinkInfo> torrentPageLinks = findPageTorrentLinks(mainPageLinks, driver, proxy);
		for (PageLinkInfo pli : torrentPageLinks) {
			Console.println("Torrent page link: " + pli.getUrl(), WHITE);
		}

		Set<PageLinkInfo> downloadTorrentLinks = downloadTorrentLinks(torrentPageLinks, driver, proxy);
		for (PageLinkInfo pli : downloadTorrentLinks) {
			Console.println("Download link: " + pli.getUrl(), WHITE);
			downloadTorrentFile(pli, driver, downloadTimeOut, proxy);
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

	public static Set<PageLinkInfo> downloadTorrentLinks(Set<PageLinkInfo> pageLinks, WebDriver driver, BrowserMobProxy proxy) throws IOException {
		Set<PageLinkInfo> links = new HashSet<>();
		for (PageLinkInfo pli :  pageLinks) {
			// Setting referer before jump to the page
			proxy.addHeader(HttpHeaders.REFERER, pli.getReferer());
			driver.get(pli.getUrl());

			// Capture int value
			String bodySource = driver.getPageSource();
			String linkRegex = "(parseInt\\(\"(.*)\"\\);)";
			Pattern pattern = Pattern.compile(linkRegex);
			Matcher matcher = pattern.matcher(bodySource);
			if (matcher.find()) {
				String intValue = matcher.group(2);
				Console.println("intValue found: " + intValue, WHITE);
				if (driver instanceof JavascriptExecutor) {
					// Referer must be atomtt
					proxy.addHeader(HttpHeaders.REFERER, pli.getUrl()); // atomtt referer !!!
					// Javascript request to be executed by selenium
					String js = "var values = {'t':'167632'};\n"; // the variable
					js += "var xhr = new XMLHttpRequest();\n";
					js += "xhr.open('POST', 'https://atomtt.com/to.php', false);\n";
					js += "xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');\n";
					js += "xhr.send('t=" + intValue + "');\n";
					js += "return xhr.response;\n";
					Console.println("executing javascript: " + js, YELLOW);
					Object result = ((JavascriptExecutor) driver).executeScript(js);
					Console.println("Javascript result: " + js, GREEN);
					String torrentFileLinkValue = "https://atomixhq.art/t_download/" + result;
					Console.println("torrentFileLinkValue: " + torrentFileLinkValue, GREEN);
					PageLinkInfo newPli = new PageLinkInfo();
					newPli.setUrl(torrentFileLinkValue);
					newPli.setReferer(pli.getUrl());
					links.add(newPli);
				}

			} else {
				Console.println("intValue NOT found for page: " + pli.getUrl(), RED);
			}
		}
		return links;
	}

	public static void downloadTorrentFile(PageLinkInfo pli, WebDriver driver, long timeOut, BrowserMobProxy proxy) throws InterruptedException {
		try {
			// Referer must be atomtt
			proxy.addHeader(HttpHeaders.REFERER, pli.getReferer());
			// Set timeout otherwise the driver lock
			driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(timeOut));
			driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeOut));
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeOut));
			driver.navigate().to(pli.getUrl());
		} catch (Exception e) {
			// Silent error
		}
	}
}
