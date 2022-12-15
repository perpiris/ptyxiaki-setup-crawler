package crawler.models;

import java.util.ArrayList;

public class State {
    String name;
    String url;
    ArrayList<Prefecture> prefectureList;

    public State(String name, String url) {
        this.name = name;
        this.url = url;
        this.prefectureList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public ArrayList<Prefecture> getPrefectureList() {
        return prefectureList;
    }
}
