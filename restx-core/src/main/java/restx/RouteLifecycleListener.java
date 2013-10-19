package restx;

/**
 * User: xavierhanin
 * Date: 1/30/13
 * Time: 10:12 PM
 */
public interface RouteLifecycleListener {
    public static final RouteLifecycleListener DEAF = new AbstractRouteLifecycleListener() {};

    void onRouteMatch(RestxRoute source);
    void onBeforeWriteContent(RestxRoute source);
}
