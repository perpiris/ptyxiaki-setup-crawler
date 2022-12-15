package crawler;

import crawler.models.Prefecture;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import crawler.models.State;

import java.io.IOException;
import java.util.ArrayList;

public class App {
    private static final String _baseUrl = "https://www.xo.gr/efimerevonta-farmakeia/";

    public static void main(String[] args) {
        System.out.println("Gathering data...");
        ArrayList<State> mainUrlList = GetMainUrlList();
        System.out.println("All data gathered.");
        System.out.println( mainUrlList.size() + " states found.");

        System.out.println("Saving data...");
        try (Repository repository = new Repository()) {
            repository.AddStatesWithPrefectures(mainUrlList);
        }
        System.out.println("Data saved successfully.");
    }

    private static ArrayList<State> GetMainUrlList() {
        ArrayList<State> mainUrlList = new ArrayList<>();

        Document pageDocument = GetPage(_baseUrl);
        Elements stateList = pageDocument.select("div#pharmacyArea2 ul.pharmacyCity li");

        for (Element stateElement : stateList) {
            String stateName, stateUrl;

            stateName = stateElement.text().replace("Ν. ", "");
            stateUrl = stateElement.select("a").attr("href").replace("/efimerevonta-farmakeia/","");

            mainUrlList.add(new State(stateName, stateUrl));
        }

        for(State state : mainUrlList) {
            boolean hasNextPage;
            String stateFullUrl = _baseUrl + state.getUrl() + "/?page=";
            int pageIndex = 1;

            do {
                pageDocument = GetPage(stateFullUrl + pageIndex);

                Elements prefectureList = pageDocument.select("div#PerfectureArea li");
                for(Element prefectureElement : prefectureList) {
                    String prefectureName, prefectureUrl;

                    prefectureName = prefectureElement.select("a").text() + " " + prefectureElement.select("span").text();
                    prefectureUrl = prefectureElement.select("a").attr("href").replace("/efimerevonta-farmakeia/","");

                    state.getPrefectureList().add(new Prefecture(prefectureName, prefectureUrl));
                }

                Element paginator = pageDocument.selectFirst("div.row.pagination");
                Element nextPageButton = pageDocument.selectFirst("div.row.pagination a.page_next");
                if (paginator != null && nextPageButton != null) {
                    pageIndex++;
                    hasNextPage = true;
                } else {
                    pageIndex = 1;
                    hasNextPage = false;
                }
            }while (hasNextPage);
        }

        return mainUrlList;
    }

    private static Document GetPage(String url) {
        Document page = null;

        try {
            page = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.246")
                    .referrer("https://www.google.com")
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return page;
    }
}
