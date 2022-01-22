package info.jllort.torrent;

import com.google.common.net.HttpHeaders;
import info.llort.torrent.Config;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.filters.RequestFilter;
import net.lightbody.bmp.proxy.CaptureType;
import net.lightbody.bmp.util.HttpMessageContents;
import net.lightbody.bmp.util.HttpMessageInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.time.Duration;

public class ParserCheck {
	public static void main(String[] args) throws UnknownHostException {
		String geckoDriverPath = Config.FIREFOX_DRIVER_PATH;
		String dstPath = Config.FILESYSTEM_DOWNLOAD_PATH;
		long timeOut = Config.FILE_DOWNLOAD_TIMEOUT;

		String referer  = "https://atomixhq.top/descargar/torrent/serie/superman-and-lois/temporada-2/capitulo-02/";
		String link = "https://atomtt.com/t_download/166723_-1642825831-Superman-And-Lois---Temporada-2--HDTV/";

		// Creating proxy
		BrowserMobProxy proxy = new BrowserMobProxyServer();
		proxy.start(8080);
		Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);

		String hostIp = Inet4Address.getLocalHost().getHostAddress();
		seleniumProxy.setHttpProxy(hostIp + ":" + proxy.getPort());
		seleniumProxy.setSslProxy(hostIp + ":" + proxy.getPort());
		proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);

//		RequestFilter requestFilter = new RequestFilter() {
//			@Override
//			public HttpResponse filterRequest(HttpRequest httpRequest, HttpMessageContents httpMessageContents, HttpMessageInfo httpMessageInfo) {
//				return null;
//			}
//		};
//
//		// put our custom header to each request
//		proxy.addRequestFilter((request, contents, messageInfo)-> {
//			request.headers().add(HttpHeaders.REFERER, referer);
//			System.out.println(request.headers().entries().toString());
//			return null;
//		});

		proxy.addHeader(HttpHeaders.REFERER, referer);

		// Creating the driver
		System.setProperty("webdriver.gecko.driver", geckoDriverPath);
		// firefox profile to autosave
		FirefoxOptions firefoxOptions = new FirefoxOptions();
		FirefoxProfile fxProfile = new FirefoxProfile();
		fxProfile.setPreference("browser.download.folderList", 2);
		fxProfile.setPreference("browser.download.dir", dstPath);
		fxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk","application/octet-stream");
		fxProfile.setPreference("pdfjs.enabledCache.state",false);
		firefoxOptions.setProfile(fxProfile);
		// Setting the proxy
		firefoxOptions.setCapability(CapabilityType.PROXY, seleniumProxy);
		firefoxOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

		WebDriver driver = new FirefoxDriver(firefoxOptions);

		// Page with link
		driver.get(link);
		Document doc = Jsoup.parse(driver.getPageSource());
		Elements elements = doc.select("a[href]");
		for (Element element : elements) {
			String torrentFileLinkValue = element.attr("data-u");
			if (torrentFileLinkValue.startsWith("https:") && torrentFileLinkValue.contains("/download-link/")) {

				// Set timeout otherwise the driver lock
//				referer = link; // change referer to the page link
				driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(timeOut));
				driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeOut));
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeOut));
				driver.navigate().to(torrentFileLinkValue);
			}
		}
	}
}
