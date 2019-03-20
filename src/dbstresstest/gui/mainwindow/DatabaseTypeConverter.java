/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.gui.mainwindow;

import dbstresstest.plugins.DatabasePlugin;
import dbstresstest.plugins.PluginLoader;
import javafx.util.StringConverter;

/**
 *
 * @author Lukas Hanusek
 */
public class DatabaseTypeConverter extends StringConverter {

    @Override
    public String toString(Object object) {
        return ((DatabasePlugin)object).getName();
    }

    @Override
    public DatabasePlugin fromString(String string) {
        for (DatabasePlugin pl : PluginLoader.getInstance().getPlugins()) {
            if (pl.getName().equals(string)) {
                return pl;
            }
        }
        return null;
    }

}
