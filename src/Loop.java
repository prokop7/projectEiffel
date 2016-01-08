import java.util.LinkedList;

/**
 * Created by proko on 19-Dec-15.
 */
public class Loop extends Node {
    Loop(String beginCondition, String endCondition, String iterator) {
        this.fromContent = beginCondition;
        this.untilContent = endCondition;
        this.iterator = iterator;
        this.variables = new LinkedList<>();
        this.name = "";
        this.type = Type.LOOP;
        this.children = new LinkedList<>();
    }

    public String fromContent;
    public String untilContent;
    public String iterator;
}
