package restx.tests;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;
import restx.common.Crypto;
import restx.common.UUIDGenerator;
import restx.factory.Factory;
import restx.security.RestxSessionCookieDescriptor;
import restx.security.SignatureKey;

import java.util.Map;

/**
 * HttpTestClient is a helper to create com.github.kevinsawicki.http.HttpRequest
 * which sets the RestxThreadLocal header to share client thread local components with server one
 * (when used with a per request factory loading).
 */
public class HttpTestClient {
    public static HttpTestClient withBaseUrl(String baseUrl) {
        return new HttpTestClient(baseUrl, null, ImmutableMap.<String,String>of());
    }

    private final String baseUrl;
    private final String principal;
    private final ImmutableMap<String, String> cookies;


    private HttpTestClient(String baseUrl, String principal, ImmutableMap<String, String> cookies) {
        this.baseUrl = baseUrl;
        this.principal = principal;
        this.cookies = cookies;
    }

    public HttpTestClient authenticatedAs(String principal) {
        Factory factory = Factory.newInstance();
        RestxSessionCookieDescriptor restxSessionCookieDescriptor = factory.getComponent(RestxSessionCookieDescriptor.class);
        SignatureKey signature = factory.queryByClass(SignatureKey.class).findOneAsComponent().or(SignatureKey.DEFAULT);

        ImmutableMap.Builder<String, String> cookiesBuilder = ImmutableMap.<String, String>builder().putAll(cookies);
        String uuid = factory.getComponent(UUIDGenerator.class).doGenerate();
        String expires = DateTime.now().plusHours(1).toString();
        String sessionContent = String.format(
                "{\"_expires\":\"%s\",\"principal\":\"%s\",\"sessionKey\":\"%s\"}", expires, principal, uuid);
        cookiesBuilder.put(restxSessionCookieDescriptor.getCookieName(), sessionContent);
        cookiesBuilder.put(restxSessionCookieDescriptor.getCookieSignatureName(), Crypto.sign(sessionContent, signature.getKey()));

        return new HttpTestClient(baseUrl, principal, cookiesBuilder.build());
    }

    public HttpTestClient withCookie(String cookieName, String cookieValue) {
        return new HttpTestClient(baseUrl, principal,
                ImmutableMap.<String,String>builder().putAll(cookies).put(cookieName, cookieValue).build());
    }

    public HttpRequest http(String method, String url) {
        HttpRequest httpRequest = new HttpRequest(baseUrl + root(url), method)
                .header("RestxThreadLocal", Factory.LocalMachines.threadLocal().getId());

        if (!cookies.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : cookies.entrySet()) {
                sb.append(entry.getKey()).append("=\"").append(entry.getValue().replace("\"", "\\\"")).append("\"; ");
            }
            sb.setLength(sb.length() - 2);
            httpRequest.header("Cookie", sb.toString());
        }

        return httpRequest;
    }

    public HttpRequest GET(String url) {
        return http("GET", url);
    }

    public HttpRequest HEAD(String url) {
        return http("HEAD", url);
    }

    public HttpRequest OPTIONS(String url) {
        return http("OPTIONS", url);
    }

    public HttpRequest POST(String url) {
        return http("POST", url);
    }

    public HttpRequest PUT(String url) {
        return http("PUT", url);
    }

    public HttpRequest DELETE(String url) {
        return http("DELETE", url);
    }


    private static String root(String url) {
        return url.startsWith("http")
                || url.startsWith("/") ? url : "/" + url;
    }
}
