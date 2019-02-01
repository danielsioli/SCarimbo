/*
 * Copyright (C) 2007  Daniel da Silva Oliveira
 *
 * This file is part of SCarimbo
 *
 * SCarimbo is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * Contact: Daniel da Silva Oliveira danieloliveira@anatel.gov.br
 */

/*
 * SettingsXMLManager.java
 *
 * Created on 2 de Julho de 2007, 14:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Vector;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLParserFactory;
import net.n3.nanoxml.XMLWriter;

/**
 *
 * @author danieloliveira
 */
public class SettingsXMLManager {
    
    private File configFile;
    private static SettingsXMLManager instance = null;
    private static IXMLElement iXMLElement;
    
    /** Creates a new instance of SettingsXMLManager */
    public SettingsXMLManager(String xmlFile) throws Exception{
        configFile = new File(xmlFile);
        configFile.getParentFile().mkdirs();
        configFile.createNewFile();
        IXMLParser iXMLParser = XMLParserFactory.createDefaultXMLParser();
        IXMLReader reader = new StdXMLReader(new FileInputStream(xmlFile));
        iXMLParser.setReader(reader);
        try{
            iXMLElement = (IXMLElement) iXMLParser.parse();
        }catch(Exception ex){}
        if(iXMLElement == null || !iXMLElement.getFullName().equals("variables")){
            iXMLElement = new XMLElement("variables");
            writeXML();
        }
    }
    
    public static SettingsXMLManager getInstance(String xmlFile) throws Exception{
        if(instance == null){
            instance = new SettingsXMLManager(xmlFile);
        }
        return instance;
    }
    
    public String getProperty(String name){
        Vector children = iXMLElement.getChildren();
        for(int i = 0; i < children.size(); i++) {
            IXMLElement child = (IXMLElement) children.get(i);
            if(child.getAttribute("name",name).equals(name)){
                return child.getAttribute("value","");
            }
        }
        return null;
    }
    
    public String[] getProperties(String[] names){
        String[] values = new String[names.length];
        Vector children = iXMLElement.getChildren();
        for(int i = 0; i < children.size(); i++) {
            IXMLElement child = (IXMLElement) children.get(i);
            for(int j = 0; j < names.length; j++){
                if(child.getAttribute("name",names[j]).equals(names[j])){
                    values[j] = child.getAttribute("value","");
                }
            }
        }
        return values;
    }
    
    public void setProperties(String[] names, String[] values) throws Exception{
        if(names.length == values.length){
            for(int i = 0; i < names.length; i++){
                boolean newProperty = true;
                Vector children = iXMLElement.getChildren();
                for(int j = 0; j < children.size(); j++){
                    IXMLElement child = (IXMLElement) children.get(j);
                    if(child.getAttribute("name","").equals(names[i])){
                        child.setAttribute("value",values[i]);
                        newProperty = false;
                        break;
                    }
                }
                if(newProperty){
                    IXMLElement elt = new XMLElement("variable");
                    elt.setAttribute("name", names[i]);
                    elt.setAttribute("value", values[i]);
                    iXMLElement.addChild(elt);
                }
            }
            writeXML();
        }else{
            throw new Exception("Quantidade de name e de values diferente.");
        }
    }
    
    public void setProperty(String name, String value){
        boolean newProperty = true;
        Vector children = iXMLElement.getChildren();
        for(int i = 0; i < children.size(); i++){
            IXMLElement child = (IXMLElement) children.get(i);
            if(child.getAttribute("name","").equals(name)){
                child.setAttribute("value",value);
                newProperty = false;
                break;
            }
        }
        if(newProperty){
            IXMLElement elt = new XMLElement("variable");
            elt.setAttribute("name", name);
            elt.setAttribute("value", value);
            iXMLElement.addChild(elt);
        }
        writeXML();
    }
    
    public void printXML(){
        try{
            XMLWriter xmlWriter = new XMLWriter(System.out);
            xmlWriter.write(iXMLElement,true,0,true);
        }catch(Exception ex){
            
        }
    }
    
    private void writeXML(){
        try {
            XMLWriter xmlWriter = new XMLWriter(new FileOutputStream(configFile));
            xmlWriter.write(iXMLElement,true,0,true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
}
