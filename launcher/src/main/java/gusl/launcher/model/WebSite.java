package gusl.launcher.model;

import java.util.ArrayList;
import java.util.List;
import gusl.launcher.UndertowConfig;

/**
 * Contains a collection of Wars and Or statics
 *
 * @author dhudson
 */
public class WebSite {

    private final List<WarDetails> wars;
    private final List<StaticDetails> statics;
    private UndertowConfig config;

    public WebSite() {
        wars = new ArrayList<>(3);
        statics = new ArrayList<>(3);
    }

    public void addWar(WarDetails detail) {
        wars.add(detail);
    }

    public List<WarDetails> getWars() {
        return wars;
    }

    public void addStatic(StaticDetails detail) {
        statics.add(detail);
    }

    public List<StaticDetails> getStatics() {
        return statics;
    }

    public UndertowConfig getConfig() {
        return config;
    }

    public void setConfig(UndertowConfig config) {
        this.config = config;
    }

}
