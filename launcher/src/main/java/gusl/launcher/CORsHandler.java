package gusl.launcher;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;

/**
 * @author dhudson
 */
public class CORsHandler implements HttpHandler {

    private final HttpString OPTIONS = HttpString.tryFromString("OPTIONS");
    private final HttpString ORIGIN = HttpString.tryFromString("origin");
    private final HttpString VARY = HttpString.tryFromString("vary");
    //    private final String VARY_RESPONSE = "origin,access-control-request-method,access-control-request-headers";
    private final HttpString LAUNCHER_HEADER = HttpString.tryFromString("X-launcher");
    private final String LAUNCHER_RESPONSE = "Casanova";
    private final HttpString ACCESS_CONTROL_ALLOW_METHODS = HttpString.tryFromString("access-control-allow-methods");
    private final String ALLOW_METHODS_RESPONSE = "GET,OPTIONS,POST,PUT";
    private final HttpString ACCESS_CONTROL_ALLOW_HEADERS = HttpString.tryFromString("access-control-allow-headers");
    private final String ALLOW_HEADERS_RESPONSE = "content-disposition, content-type, accept, player-token, access_token, analytics-token, customer-token,user-token, session-token, latitude, longitude, accuracy, proxy_set_header, origin, x-forwarded-for, x-requested-with, timeout, build-id, x-access-token, user-token,totp-user-token,totp-session-token, msal-token, Media-Type, Orientation";
    private final HttpString ACCESS_CONTROL_ALLOW_ORIGIN = HttpString.tryFromString("access-control-allow-origin");

    //    private final String ALLOW_ORIGIN_RESPONSE = "*";
    private final HttpString ACCESS_CONTROL_REQUEST_METHOD = HttpString.tryFromString("access-control-request-method");

    //    private final HttpString ACCESS_CONTROL_REQUEST_HEADERS = HttpString.tryFromString("access-control-request-headers");
    private final HttpString ACCESS_CONTROL_MAX_AGE = HttpString.tryFromString("Access-Control-Max-Age");
    private final String MAX_AGE_RESPONSE = "1728000";

    private final HttpHandler handler;
    private final PathHandler path;
    private final String ALLOWED_HEADERS;

    public CORsHandler(String customHeaders) {
        if (customHeaders == null) {
            ALLOWED_HEADERS = ALLOW_HEADERS_RESPONSE;
        } else {
            ALLOWED_HEADERS = ALLOW_HEADERS_RESPONSE + ", " + customHeaders;
        }

        path = Handlers.path();

        // Add gzip and deflate
        handler = new EncodingHandler.Builder().build(null).wrap(path);
    }

    public PathHandler getPath() {
        return path;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

        if (isPreflightRequest(exchange)) {
            HeaderMap headers = exchange.getResponseHeaders();

            headers.put(ACCESS_CONTROL_ALLOW_ORIGIN, exchange.getRequestHeaders().getFirst(ORIGIN));
            headers.put(ACCESS_CONTROL_ALLOW_METHODS, ALLOW_METHODS_RESPONSE);
            headers.put(ACCESS_CONTROL_ALLOW_HEADERS, ALLOW_HEADERS_RESPONSE);
            headers.put(ACCESS_CONTROL_MAX_AGE, MAX_AGE_RESPONSE);
            headers.put(LAUNCHER_HEADER, LAUNCHER_RESPONSE);

            exchange.setStatusCode(200);
            // This doesn't need to go any further
            exchange.endExchange();
            return;
        }

        HeaderMap headers = exchange.getRequestHeaders();
        if (headers.contains(ORIGIN)) {
            headers.put(ACCESS_CONTROL_ALLOW_ORIGIN, exchange.getRequestHeaders().getFirst(ORIGIN));
            headers.put(LAUNCHER_HEADER, LAUNCHER_RESPONSE);
        }

        handler.handleRequest(exchange);
    }

    public String getCorsHeaders() {
        return ALLOWED_HEADERS;
    }

    private boolean isPreflightRequest(HttpServerExchange exchange) {
        HeaderMap headers = exchange.getRequestHeaders();
        return exchange.getRequestMethod().equals(OPTIONS)
                && headers.contains(ORIGIN) && headers.contains(ACCESS_CONTROL_REQUEST_METHOD);
    }

}
