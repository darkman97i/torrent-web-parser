package info.jllort.torrent;

import com.google.common.net.HttpHeaders;
import info.llort.torrent.Config;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.proxy.CaptureType;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserCheckIssue31 {
	public static void main(String[] args) throws UnknownHostException, InterruptedException {
		String geckoDriverPath = Config.FIREFOX_DRIVER_PATH;
		String dstPath = Config.FILESYSTEM_DOWNLOAD_PATH;
		long timeOut = Config.FILE_DOWNLOAD_TIMEOUT;

		String referer  = "https://atomixhq.art/descargar/torrent/peliculas-castellano/venganza-a-golpes-2022-/blurayrip-ac3-5-1/";
		String link = "https://atomtt.com/t_download/168581/venganza-a-golpes--2022-/";

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
		String bodySource = driver.getPageSource();
		String linkRegex = "(parseInt\\(\"(.*)\"\\);)";
		Pattern pattern = Pattern.compile(linkRegex);
		Matcher matcher = pattern.matcher(bodySource);
		if (matcher.find()) {
			String intValue = matcher.group(2);
			System.out.println("intValue found: " + intValue);
			if (driver instanceof JavascriptExecutor) {
				// Referer must be atomtt
				proxy.addHeader(HttpHeaders.REFERER, link);
				// Javascript request to be executed by selenium
				String js = "var values = {'t':'168581'};\n"; // the variable
				js += "var xhr = new XMLHttpRequest();\n";
				js += "xhr.open('POST', 'https://atomtt.com/to.php', false);\n";
				js += "xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');\n";
				js += "xhr.send('t=" + intValue + "');\n";
				js += "return xhr.response;\n";
				System.out.println(js);
				Object result = ((JavascriptExecutor) driver).executeScript(js);
				System.out.println("Result value of cal to top: " + result);
//				result = ((JavascriptExecutor) driver).executeScript("alert('Welcome to Guru99');");
				// download URl = https://atomixhq.art/t_download/temp/17022022/168542/El-Pacificador---Temporada-1--HDTV.torrent?md5=b611CNys8mldMTSN_atN6A&expires=1645101721
				String torrentFileLinkValue = "https://atomixhq.art" + result;
				System.out.println(torrentFileLinkValue);
				driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(timeOut));
				driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeOut));
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeOut));
				// Other referer
				proxy.addHeader(HttpHeaders.REFERER, "https://atomtt.com/"); // Referer is the page what contains the torrent link //atomtt etc...
				proxy.addHeader("authority", "atomixhq.art");
				proxy.addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");
				driver.navigate().to(torrentFileLinkValue);
			}
		}
	}
}
