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
 * Main.java
 *
 * Created on 10 de Junho de 2007, 09:28
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package main;

import java.security.Permission;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import settings.SettingsJFrame;

/**
 *
 * @author Daniel
 */
public class Main extends JFrame {

    public static final String APP_NAME = "SCarimbo";
    protected static final String UNNUMBERED_PDF_FILE = "Não Numerado.pdf";
    protected static final String NUMBERED_PDF_FILE = "Numerado.pdf";
    protected static final float FONT_SIZE = 12;
    protected static final float FONT_SIZE_2 = FONT_SIZE * 1.44f;
    protected static final float IMAGE_SCALE = 0.68f;
    private static String appDataDir = null;
    public static final String APP_EXT_SETTINGS = new StringBuffer(getAppDataDir()).append("\\Settings\\Settings.sca").toString();
    public static final String BLANK_IMAGE = "/images/blank.png";
    public static final String EVERGREEN_IMAGE = "/images/evergreen.png";
    public static final int SIGNATURE_WIDTH = 65;
    public static final int SIGNATURE_HEIGHT = 40;
    public static final String[] KEYS = new String[]{"directory", "filesToCreate", "printingWay", "deleteFiles", "printAfterCreation", "digitallySign", "acrobatReader", "printer1", "printer2", "printer3", "printer4", "signatureXOffset", "signatureYOffset", "keystore", "keypass", "storepass", "email", "signatureImage", "stampImage", "pageNumberXOffset", "pageNumberYOffset"};
    public static final int SCREENS_DIR = 0;
    public static final int FILES_TO_CREATE = 1;
    public static final int PRINTING_WAY = 2;
    public static final int DELETE_AFTER = 3;
    public static final int AUTO_PRINT = 4;
    public static final int AUTO_SIGN = 5;
    public static final int ACROBAT_READER = 6;
    public static final int PRINTER1 = 7;
    public static final int PRINTER2 = 8;
    public static final int PRINTER3 = 9;
    public static final int PRINTER4 = 10;
    public static final int SIGNATURE_X_OFFSET = 11;
    public static final int SIGNATURE_Y_OFFSET = 12;
    public static final int KEY_STORE = 13;
    public static final int KEY_PASS = 14;
    public static final int STORE_PASS = 15;
    public static final int EMAIL = 16;
    public static final int SIGNATURE_IMAGE = 17;
    public static final int STAMP_IMAGE = 18;
    public static final int PAGE_NUMBER_X_OFFSET = 19;
    public static final int PAGE_NUMBER_Y_OFFSET = 20;
    public static final int PRINTING_WAY_1_PER_PAPER = 0;
    public static final int PRINTING_WAY_1_PER_PAPER_BLANK_BACK = 1;
    public static final int PRINTING_WAY_1_FRONT_1_BACK = 2;
    public static final int PRINTING_WAY_2_FRONT_2_BACK = 3;
    public static final int PRINTING_WAY_2_FRONT = 4;
    public static final int PRINTING_WAY_OPTIMAL = 5;
    public static final int PRINTING_WAY_ASK = 6;
    public static final int AUTO_PRINT_NONE = 0;
    public static final int AUTO_PRINT_NUMBERED = 1;
    public static final int AUTO_PRINT_UNNUMBERED = 2;
    public static final int AUTO_PRINT_BOTH = 3;
    public static final int AUTO_PRINT_ASK = 4;
    public static final int DELETE_AFTER_YES = 0;
    public static final int DELETE_AFTER_NO = 1;
    public static final int DELETE_AFTER_ASK = 2;
    public static final int FILES_TO_CREATE_NUMBERED = 0;
    public static final int FILES_TO_CREATE_UNNUMBERED = 1;
    public static final int FILES_TO_CREATE_BOTH = 2;
    public static final int FILES_TO_CREATE_ASK = 3;
    public static final int AUTO_SIGN_YES = 0;
    public static final int AUTO_SIGN_NO = 1;
    public static final int AUTO_SIGN_ASK = 2;

    //public static final String APP_EXT_SIGNATURE = new StringBuffer(getAppDataDir()).append("\\Images\\").append(System.getProperty("user.name")).append(".png").toString();
    /**
     * Creates a new instance of Main
     */
    public Main() {
        changeLookAndFeel();
        System.setSecurityManager(new SecurityManager(){
            @Override
            public void checkPermission(Permission perm) {
            }
            @Override
            public void checkPermission(Permission perm, Object context) {
            }
        });
        new Task().execute();
    }

    private void changeLookAndFeel() {
        try {
            UIManager.LookAndFeelInfo looks[] = UIManager.getInstalledLookAndFeels();
            UIManager.setLookAndFeel(looks[2].getClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String getAppDataDir() {
        if (appDataDir == null) {
            if (System.getenv("APPDATA") != null) {
                appDataDir = System.getenv("APPDATA") + "\\" + APP_NAME;
            } else {
                appDataDir = System.getProperty("user.home") + "\\" + APP_NAME;
            }
        }
        return appDataDir;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args != null && args.length != 0 && args[0].equalsIgnoreCase("-config")) {
            java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    new SettingsJFrame().setVisible(true);
                }
            });
        } else if (args != null && args.length != 0 && args[0].equalsIgnoreCase("-run")) {
            java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    new Main();
                }
            });
        } else {
            JOptionPane.showMessageDialog(null, new StringBuffer("Uso: ").append(APP_NAME).append(".jar -[run,config]").toString(), APP_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }
}
