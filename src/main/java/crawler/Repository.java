package crawler;

import crawler.models.Prefecture;
import crawler.models.State;
import org.neo4j.driver.*;

import java.util.ArrayList;

import static org.neo4j.driver.Values.parameters;

public class Repository implements AutoCloseable {
    private final Driver driver;

    public Repository() {
        driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "password"));
    }

    public void close() {
        driver.close();
    }

    public void AddStatesWithPrefectures(ArrayList<State> stateList) {
        for (State state : stateList) {
            try (var session = driver.session()) {
                session.executeWrite(tx -> AddState(state).consume());

                for (Prefecture prefecture : state.getPrefectureList()) {
                    session.executeWrite(tx -> AddPrefecture(prefecture).consume());
                    session.executeWrite(tx -> LinkPrefectureToState(state.getName(), prefecture.getName()).consume());
                }
            }
        }
    }

    private Result AddState(State state) {
        return driver.session().run("CREATE (s:State {name: $name, url: $url})", parameters("name", state.getName(), "url", state.getUrl()));
    }

    private Result AddPrefecture(Prefecture state) {
        return driver.session().run("CREATE (p:Prefecture {name: $name, url: $url})", parameters("name", state.getName(), "url", state.getUrl()));
    }

    private Result LinkPrefectureToState(String state, String prefecture) {
        return driver.session().run("MATCH (s:State {name: $stateName}) MATCH (p:Prefecture {name: $prefectureName}) CREATE (p)-[:BELONGS]->(s)",
                parameters("stateName", state, "prefectureName", prefecture));
    }
}
