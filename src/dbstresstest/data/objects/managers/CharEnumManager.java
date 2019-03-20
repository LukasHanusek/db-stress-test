/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.data.objects.managers;

import dbstresstest.data.objects.CharEnum;
import dbstresstest.util.Log;
import dbstresstest.util.exceptions.EnumNotFoundException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Dakado
 */
public class CharEnumManager {
    
    public static final String DELIMITER = ",";
    
    private static CharEnumManager instance = null;
    private CopyOnWriteArrayList<CharEnum> charEnums;

   
    public static CharEnumManager getInstance() throws IOException { 
        if (instance == null) instance = new CharEnumManager();
        return instance;
    } 
    
    public CharEnumManager() throws IOException {
        loadEnums();
    }

    public void loadEnums() throws IOException {
        Log log = new Log("CharEnumManager");
        charEnums = new CopyOnWriteArrayList();
        File file = new File("data/charenums.txt");
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader("data/charenums.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    charEnums.add(new CharEnum(line.split("=")[0], line.split("=")[1], CharEnumManager.DELIMITER));
                }
            }
            log.logMessage("Loaded " + charEnums.size() + " char enums!");
        } else {
            log.logMessage("Could not locate char enum file at data/charenums.txt");
        }
    }

    /**
     * 
     * @return 
     */
    public CopyOnWriteArrayList<CharEnum> getCharEnums() {
        return charEnums;
    }
    
    /**
     * 
     * @param name
     * @return
     * @throws EnumNotFoundException 
     */
    public CharEnum getCharEnumByName(String name) throws EnumNotFoundException {
        for (CharEnum ce : this.charEnums) {
            if (ce.getName().equals(name)) return ce;
        }
        throw new EnumNotFoundException("Enum " + name + " not found!");
    }
    
}
