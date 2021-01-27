package dtmproject.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import dtmproject.DTM;
import dtmproject.NameTagColorer;
import dtmproject.commands.EditModeCommand;
import dtmproject.configuration.LangConfig;
import dtmproject.data.DTMDataHandler;
import dtmproject.data.DefaultMapLoader;
import dtmproject.events.DeathHandler;
import dtmproject.logic.CountdownHandler;
import dtmproject.logic.DTMLogicHandler;
import dtmproject.scoreboard.ScoreboardHandler;
import dtmproject.shop.ShopHandler;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DTMBinderModule extends AbstractModule {
    private final DTM plugin;
    private final LangConfig langConfig;

    @Override
    protected void configure() {
        this.bind(DTM.class).toInstance(this.plugin);
        this.bind(LangConfig.class).toInstance(this.langConfig);

        this.bind(ScoreboardHandler.class).in(Scopes.SINGLETON);
        this.bind(ShopHandler.class).in(Scopes.SINGLETON);
        this.bind(DTMDataHandler.class).in(Scopes.SINGLETON);
        this.bind(DTMLogicHandler.class).in(Scopes.SINGLETON);
        this.bind(EditModeCommand.class).in(Scopes.SINGLETON);
        this.bind(DeathHandler.class).in(Scopes.SINGLETON);
        this.bind(CountdownHandler.class).in(Scopes.SINGLETON);
        this.bind(NameTagColorer.class).in(Scopes.SINGLETON);
        this.bind(DefaultMapLoader.class).in(Scopes.SINGLETON);
    }

}
