/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.gui.mainwindow;

import dbstresstest.data.objects.DbCon;
import dbstresstest.data.objects.managers.ConManager;
import javafx.util.StringConverter;

/**
 *
 * @author Lukas Hanusek
 */
public class SavedConConverter extends StringConverter {
    
    @Override
    public String toString(Object object) {
        return ((DbCon)object).getCustomName();
    }

    @Override
    public DbCon fromString(String string) {
        for (DbCon con : ConManager.getInstance().getConnections()) {
            if (con.getCustomName().equals(string)) {
                return con;
            }
        }
        return null;
    }

}
