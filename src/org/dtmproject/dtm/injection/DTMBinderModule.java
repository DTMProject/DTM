package org.dtmproject.dtm.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.dtmproject.dtm.DTM;
import org.dtmproject.dtm.NameTagColorer;
import org.dtmproject.dtm.commands.EditModeCommand;
import org.dtmproject.dtm.configuration.LangConfig;
import org.dtmproject.dtm.data.DTMDataHandler;
import org.dtmproject.dtm.data.DefaultMapLoader;
import org.dtmproject.dtm.events.DeathHandler;
import org.dtmproject.dtm.logic.CountdownHandler;
import org.dtmproject.dtm.logic.DTMLogicHandler;
import org.dtmproject.dtm.scoreboard.ScoreboardHandler;
import org.dtmproject.dtm.shop.ShopHandler;
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
