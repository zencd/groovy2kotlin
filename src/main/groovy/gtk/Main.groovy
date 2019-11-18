package gtk

import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class Main {
    private static final Logger log = LoggerFactory.getLogger(this)

    static void main(String[] args) {
        println "Args: " + args.join(', ')
        log.debug("debug")
        log.info("info")
        log.warn("warning")
    }
}
