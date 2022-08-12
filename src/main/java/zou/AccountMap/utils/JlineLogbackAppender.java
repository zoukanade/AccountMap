package zou.AccountMap.utils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import zou.AccountMap.AccountMap;

import java.util.Arrays;

public class JlineLogbackAppender extends ConsoleAppender<ILoggingEvent> {
    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!this.started) {
            return;
        }
        Arrays.stream(
            new String(this.encoder.encode(eventObject)).split("\n\r")
        ).forEach(AccountMap.getConsole()::printAbove);
    }
}
