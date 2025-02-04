package house.greenhouse.greenhouseconfig.test.client.integration.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import house.greenhouse.greenhouseconfig.test.client.screen.GreenhouseConfigTestScreen;

public class GreenhouseConfigTestModMenuEntrypoint implements ModMenuApi {
    public ConfigScreenFactory<GreenhouseConfigTestScreen> getModConfigScreenFactory() {
        return GreenhouseConfigTestScreen::new;
    }
}
