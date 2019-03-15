import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String str = null;

//        try {
//            str.substring(0);
//        } catch (Exception e) {
//            logger.error("dfdf", e);
//        }
        logger.trace("trace");
        logger.debug("debug");
        logger.info("info", "what?");
        logger.warn("warn", "what?", 2);
        logger.error("error");
    }
}
