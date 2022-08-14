package zou.AccountMap.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String label() default "";

    String usage() default "commands.generic.no_usage_specified";

    String description() default "commands.generic.no_description_specified";

    String[] aliases() default {};

    String permission() default "";
    //
    boolean threading() default false;
}
