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
 * Task.java
 *
 * Created on 5 de Julho de 2007, 09:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package main;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.SimpleBookmark;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import progress.ProgressJFrame;
import settings.SettingsXMLManager;
import signature.PasswordJFrame;

/**
 *
 * @author danieloliveira
 */
public class Task extends SwingWorker<Void, Void> {

    private ProgressJFrame progressJFrame;
    private String[] values;
    private int filesToCreate = Main.FILES_TO_CREATE_ASK;
    private int printingWay = Main.PRINTING_WAY_ASK;
    private boolean deleteFiles = false;
    private int autoPrint = Main.AUTO_PRINT_ASK;
    private int autoSign = Main.AUTO_SIGN_ASK;
    private long initialPageNumber = -1;
    private File signatureFile;
    private SettingsXMLManager settings;

    /** Creates a new instance of Task */
    public Task() {
        progressJFrame = new ProgressJFrame();
        progressJFrame.reset();
    }

    @Override
    protected Void doInBackground() throws Exception {

        settings = SettingsXMLManager.getInstance(Main.APP_EXT_SETTINGS);
        values = settings.getProperties(Main.KEYS);
        try {
            if (values == null || values.length != Main.KEYS.length) {
                throw new Exception("Não foi possível ler o arquivo de configurações.");
            }

            for (int i = 0; i < values.length; i++) {
                if (values[i] == null) {
                    throw new Exception("Não foi possível ler o arquivo de configurações.");
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Erro: " + ex.getMessage(), Main.APP_NAME, JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            progressJFrame.exit(0);
            return null;
        }

        signatureFile = new File(values[Main.SIGNATURE_IMAGE]);

        File[] screensPDFFiles = getScreensPDFFiles();
        if (screensPDFFiles.length > 0) {
            for (int i = 0; i < screensPDFFiles.length; i++) {
                if (screensPDFFiles[i].getName().equals(Main.UNNUMBERED_PDF_FILE) | screensPDFFiles[i].getName().equals(Main.NUMBERED_PDF_FILE)) {
                    JOptionPane.showMessageDialog(null, new StringBuffer("Há arquivos PDF antigos na pasta ").append(values[Main.SCREENS_DIR]).toString(), Main.APP_NAME, JOptionPane.ERROR_MESSAGE);
                    progressJFrame.exit(0);
                }
            }
            getSettings();
            make(screensPDFFiles);
            prepareToFinish();
        } else {
            JOptionPane.showMessageDialog(null, new StringBuffer("Nenhum arquivo PDF encontrado na pasta ").append(values[Main.SCREENS_DIR]).toString(), Main.APP_NAME, JOptionPane.ERROR_MESSAGE);
            progressJFrame.exit(0);
        }
        return null;
    }

    @Override
    public void done() {
    }

    private File[] getScreensPDFFiles() {
        File[] screensPDFFiles = null;

        screensPDFFiles = new File(values[Main.SCREENS_DIR]).listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".pdf")) {
                    return true;
                }
                return false;
            }
        });

        return screensPDFFiles;
    }

    private void getSettings() {
        try {
            filesToCreate = Integer.parseInt(values[Main.FILES_TO_CREATE]);
        } catch (Exception ex) {
        }

        if (filesToCreate == Main.FILES_TO_CREATE_ASK) {
            int option;
            option = JOptionPane.showOptionDialog(null, "Quais arquivos serão criados?", Main.APP_NAME, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Numerado", "Não Numerado", "Ambos", "Cancelar"}, "Numerado");
            if (option == JOptionPane.CLOSED_OPTION | option == 3) {
                progressJFrame.exit(0);
            }
            filesToCreate = option;
        }

        try {
            printingWay = Integer.parseInt(values[Main.PRINTING_WAY]);
        } catch (Exception ex) {
        }

        if (values[Main.DELETE_AFTER].equals("0")) {
            deleteFiles = true;
        } else if (values[Main.DELETE_AFTER].equals("1")) {
            deleteFiles = false;
        } else {
            int option;
            option = JOptionPane.showOptionDialog(null, "Deletar telas em PDF?", Main.APP_NAME, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Sim", "Não", "Cancelar"}, "Sim");
            if (option == JOptionPane.CLOSED_OPTION | option == 2) {
                progressJFrame.exit(0);
            }
            if (option == 0) {
                deleteFiles = true;
            } else if (option == 1) {
                deleteFiles = false;
            }
        }

        try {
            autoPrint = Integer.parseInt(values[Main.AUTO_PRINT]);
        } catch (Exception ex) {
        }

        if (autoPrint == Main.AUTO_PRINT_ASK) {
            int option;
            option = JOptionPane.showOptionDialog(null, "Imprimir arquivo depois de criado?", Main.APP_NAME, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Nenhum", "Numerado", "Não Numerado", "Ambos", "Cancelar"}, "Nenhum");
            if (option == JOptionPane.CLOSED_OPTION | option == 4) {
                progressJFrame.exit(0);
            }
            autoPrint = option;
        }

        try {
            autoSign = Integer.parseInt(values[Main.AUTO_SIGN]);
        } catch (Exception ex) {
        }

        if (autoSign == Main.AUTO_SIGN_ASK) {
            int option;
            option = JOptionPane.showOptionDialog(null, "Assinar documentos digitalmente?", Main.APP_NAME, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Sim", "Não", "Cancelar"}, "Sim");
            if (option == JOptionPane.CLOSED_OPTION | option == 2) {
                progressJFrame.exit(0);
            }
            autoSign = option;
        }

        if ((filesToCreate == Main.FILES_TO_CREATE_NUMBERED || filesToCreate == Main.FILES_TO_CREATE_BOTH) || (autoPrint == Main.AUTO_PRINT_NUMBERED || autoPrint == Main.AUTO_PRINT_BOTH)) {
            if (printingWay == Main.PRINTING_WAY_ASK) {
                int option;
                option = JOptionPane.showOptionDialog(null, "Qual a forma de Impressão?", Main.APP_NAME, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"1 página por folha", "1 página por folha verso em branco", "1 página na frente e 1 no verso", "2 páginas na frente e 2 no verso", "2 páginas na frente", "Ótimo", "Cancelar"}, "1 página por folha");
                if (option == JOptionPane.CLOSED_OPTION | option == 4) {
                    progressJFrame.exit(0);
                }
                printingWay = option;
            }
            while (initialPageNumber == -1) {
                try {
                    String initialPageNumberString = "" + JOptionPane.showInputDialog(null, "Digite o número da última folha do processo:", Main.APP_NAME, JOptionPane.QUESTION_MESSAGE, new ImageIcon(this.getClass().getResource("/images/Mini - SCarimbo.png")), null, null);
                    if (!initialPageNumberString.equals("null")) {
                        initialPageNumber = Long.parseLong(initialPageNumberString);
                    } else {
                        progressJFrame.exit(0);
                    }
                } catch (Exception ex) {
                    initialPageNumber = -1;
                }
            }
            initialPageNumber++;
        }
    }

    private void make(File[] screensPDFFiles) {
        File mergedPDFFile = mergeScreensPDFFiles(screensPDFFiles, Main.UNNUMBERED_PDF_FILE);
        File numberedPDFFile = null;

        if (filesToCreate == Main.FILES_TO_CREATE_NUMBERED || filesToCreate == Main.FILES_TO_CREATE_BOTH || autoPrint == Main.AUTO_PRINT_NUMBERED || autoPrint == Main.AUTO_PRINT_BOTH) {
            try {
                numberedPDFFile = drawStamp(mergedPDFFile);
                numberedPDFFile.createNewFile();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, new StringBuffer("Problemas ao criar arquivo: ").append(ex.getMessage()).toString(), Main.APP_NAME, JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }

        if (filesToCreate == Main.FILES_TO_CREATE_UNNUMBERED || filesToCreate == Main.FILES_TO_CREATE_BOTH || autoPrint == Main.AUTO_PRINT_UNNUMBERED || autoPrint == Main.AUTO_PRINT_BOTH) {
            try {
                mergedPDFFile.createNewFile();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, new StringBuffer("Problemas ao criar arquivo: ").append(ex.getMessage()).toString(), Main.APP_NAME, JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }

        if (filesToCreate == Main.FILES_TO_CREATE_NUMBERED && (autoPrint == Main.AUTO_PRINT_NUMBERED || autoPrint == Main.AUTO_PRINT_NONE)) {
            mergedPDFFile.delete();
        }

        if (deleteFiles) {
            deleteFiles(screensPDFFiles);
        }
    }

    private File mergeScreensPDFFiles(File[] screensPDFFiles, String destinationFileName) {
        progressJFrame.reset();
        progressJFrame.setLengthOfTask(screensPDFFiles.length + 2);
        progressJFrame.setVisible(true);
        File mergedPDFFile = new File(new StringBuffer(values[Main.SCREENS_DIR]).append("\\").append(destinationFileName).toString());
        try {
            int pageOffset = 0;
            ArrayList<HashMap<String, String>> master = new ArrayList<HashMap<String, String>>();
            Document document = null;
            PdfCopy writer = null;
            for (int f = 0; f < screensPDFFiles.length; f++) {
                // we create a reader for a certain document
                progressJFrame.setMessage(new StringBuffer("Lendo arquivo ").append(screensPDFFiles[f].getName()).toString());
                PdfReader reader = new PdfReader(screensPDFFiles[f].getAbsolutePath());
                reader.consolidateNamedDestinations();
                // we retrieve the total number of pages
                int n = reader.getNumberOfPages();
                List<HashMap<String, String>> bookmarks = (List<HashMap<String, String>>) SimpleBookmark.getBookmark(reader);
                if (bookmarks != null) {
                    if (pageOffset != 0) {
                        SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset, null);
                    }
                    master.addAll(bookmarks);
                }
                pageOffset += n;

                if (f == 0) {
                    // step 1: creation of a document-object
                    document = new Document(reader.getPageSizeWithRotation(1));
                    // step 2: we create a writer that listens to the document
                    writer = new PdfCopy(document, new FileOutputStream(mergedPDFFile));
                    // step 3: we open the document
                    document.open();
                }
                // step 4: we add content
                PdfImportedPage page;
                for (int i = 0; i < n;) {
                    ++i;
                    page = writer.getImportedPage(reader, i);
                    writer.addPage(page);
                }
                PRAcroForm form = reader.getAcroForm();
                if (form != null) {
                    writer.copyAcroForm(reader);
                }
            }
            if (!master.isEmpty()) {
                writer.setOutlines(master);
            // step 5: we close the document
            }
            progressJFrame.setMessage(new StringBuffer("Salvando arquivo ").append(Main.UNNUMBERED_PDF_FILE).toString());
            document.close();
            progressJFrame.setMessage(new StringBuffer("Fim da criação do arquivo ").append(Main.UNNUMBERED_PDF_FILE).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mergedPDFFile;
    }

    private File drawStamp(File mergedPDFFile) {
        File numberedPDFFile = new File(new StringBuffer(values[Main.SCREENS_DIR]).append("\\").append(Main.NUMBERED_PDF_FILE).toString());
        float[] position;
        try {
            Image stampImage = Image.getInstance(values[Main.STAMP_IMAGE]);
            PdfReader reader = new PdfReader(mergedPDFFile.getAbsolutePath());
            int n = reader.getNumberOfPages();
            if (printingWay == Main.PRINTING_WAY_OPTIMAL) {
                if (n <= 2) {
                    printingWay = Main.PRINTING_WAY_1_FRONT_1_BACK;
                } else {
                    printingWay = Main.PRINTING_WAY_2_FRONT_2_BACK;
                }
            }
            if (printingWay == Main.PRINTING_WAY_1_FRONT_1_BACK || printingWay == Main.PRINTING_WAY_1_PER_PAPER || printingWay == Main.PRINTING_WAY_1_PER_PAPER_BLANK_BACK) {
                stampImage.scalePercent(Main.IMAGE_SCALE * 100f);
            }
            for (int i = 1; i <= n; i++) {
                int currentRotation = reader.getPageRotation(i);
                if (currentRotation != 0) {
                    PdfDictionary dic = reader.getPageN(i);
                    dic.put(PdfName.ROTATE, new PdfNumber(currentRotation + (printingWay == Main.PRINTING_WAY_1_FRONT_1_BACK || printingWay == Main.PRINTING_WAY_1_PER_PAPER || printingWay == Main.PRINTING_WAY_1_PER_PAPER_BLANK_BACK ? -90 : 90)));
                }
            }
            PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(numberedPDFFile));
            HashMap<String, String> moreInfo = new HashMap<String, String>();
            moreInfo.put("Author", System.getProperty("user.name"));
            stamp.setMoreInfo(moreInfo);
            PdfContentByte over;
            BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, BaseFont.EMBEDDED);
            boolean signed = true;
            if (printingWay == Main.PRINTING_WAY_1_FRONT_1_BACK) {
                if (n % 2 != 0) {
                    stamp.insertPage(n + 1, PageSize.A4);
                    n++;
                    double angle = Math.PI / 6d;
                    if (Math.random() > 0.5d) {
                        angle += (Math.PI / 18d * Math.random());
                    } else {
                        angle -= (Math.PI / 18d * Math.random());
                    }
                    Image blankImage = Image.getInstance(getClass().getResource(Main.BLANK_IMAGE));
                    blankImage.setRotation((float) angle);
                    blankImage.setAbsolutePosition(reader.getPageSizeWithRotation(n).getWidth() / 2 - (int) ((Math.cos(90d - angle) * blankImage.getPlainHeight() + Math.cos(angle) * blankImage.getPlainWidth()) / 2d), reader.getPageSizeWithRotation(n).getHeight() / 2 - (int) (Math.cos(angle) * blankImage.getPlainHeight() / 2d) - (int) (Math.sin(angle) * blankImage.getPlainWidth() / 2d));
                    stamp.getUnderContent(n).addImage(blankImage);
                }
                stamp.setViewerPreferences(PdfWriter.PageLayoutOneColumn | PdfWriter.PageModeUseOutlines);
                progressJFrame.reset();
                progressJFrame.setLengthOfTask((n / 2 == 0 ? 3 : n / 2 + 2));
                for (int i = 1; i <= n; i += 2) {
                    progressJFrame.setMessage(new StringBuffer("Carimbando página ").append(i).append(" com número de folha ").append(initialPageNumber).toString());
                    over = stamp.getOverContent(i);
                    position = getStampPosition(reader.getPageSizeWithRotation(i), stampImage);
                    stampImage.setAbsolutePosition(position[0], position[1]);
                    stampImage.setRotationDegrees(position[2]);
                    over.addImage(stampImage);
                    over.moveTo(0, reader.getPageSizeWithRotation(i).getHeight() / 2f);
                    over.lineTo(10, reader.getPageSizeWithRotation(i).getHeight() / 2f);
                    over.stroke();
                    over.beginText();
                    over.setFontAndSize(bf, Main.FONT_SIZE);
                    over.showTextAligned(Element.ALIGN_CENTER, "" + initialPageNumber++, position[0] + Integer.parseInt(values[Main.PAGE_NUMBER_X_OFFSET]) * Main.IMAGE_SCALE, position[1] + stampImage.getPlainHeight() - Main.FONT_SIZE - Integer.parseInt(values[Main.PAGE_NUMBER_Y_OFFSET]) * Main.IMAGE_SCALE, position[2]);
                    over.endText();

                    if (autoSign == Main.AUTO_SIGN_YES) {
                        try {
                            Image signatureImage = Image.getInstance(signatureFile.getAbsolutePath());
                            signatureImage.scalePercent(Main.IMAGE_SCALE * 100f);
                            signatureImage.setAbsolutePosition(position[0] + Float.parseFloat(values[Main.SIGNATURE_X_OFFSET]) * Main.IMAGE_SCALE, position[1] + stampImage.getPlainHeight() - signatureImage.getPlainHeight() - Float.parseFloat(values[Main.SIGNATURE_Y_OFFSET]) * Main.IMAGE_SCALE);
                            signatureImage.setRotationDegrees(position[2]);
                            over.addImage(signatureImage);
                        } catch (Exception ex) {
                            signed = false;
                            ex.printStackTrace();
                        }
                    }
                }
            } else if (printingWay == Main.PRINTING_WAY_2_FRONT_2_BACK) {
                if ((n - 1) % 4 == 0) {
                    stamp.insertPage(n + 1, PageSize.A4);//página a ser carimbada com o número

                    n++;
                }
                if ((n - 2) % 4 == 0) {
                    stamp.insertPage(n + 1, PageSize.A4);//página a ser carimbada com página em branco

                    n++;
                    stamp.insertPage(n + 1, PageSize.A4);//página a ser carimbada com página em branco

                    n++;
                    double angle = Math.PI / 6d;
                    if (Math.random() > 0.5d) {
                        angle += (Math.PI / 18d * Math.random());
                    } else {
                        angle -= (Math.PI / 18d * Math.random());
                    }
                    Image blankImage = Image.getInstance(getClass().getResource(Main.BLANK_IMAGE));
                    blankImage.setRotation((float) angle);
                    blankImage.setAbsolutePosition(reader.getPageSizeWithRotation(n).getWidth() / 2 - (int) ((Math.cos(90d - angle) * blankImage.getPlainHeight() + Math.cos(angle) * blankImage.getPlainWidth()) / 2d), reader.getPageSizeWithRotation(n).getHeight() / 2 - (int) (Math.cos(angle) * blankImage.getPlainHeight() / 2d) - (int) (Math.sin(angle) * blankImage.getPlainWidth() / 2d));
                    stamp.getUnderContent(n - 1).addImage(blankImage);
                    angle = Math.PI / 6d;
                    if (Math.random() > 0.5d) {
                        angle += (Math.PI / 18d * Math.random());
                    } else {
                        angle -= (Math.PI / 18d * Math.random());
                    }
                    blankImage.setAbsolutePosition(reader.getPageSizeWithRotation(n).getWidth() / 2 - (int) ((Math.cos(90d - angle) * blankImage.getPlainHeight() + Math.cos(angle) * blankImage.getPlainWidth()) / 2d), reader.getPageSizeWithRotation(n).getHeight() / 2 - (int) (Math.cos(angle) * blankImage.getPlainHeight() / 2d) - (int) (Math.sin(angle) * blankImage.getPlainWidth() / 2d));
                    stamp.getUnderContent(n).addImage(blankImage);
                }
                progressJFrame.reset();
                progressJFrame.setLengthOfTask((n / 4 == 0 ? 3 : n / 4 + 2));
                stamp.setViewerPreferences(PdfWriter.PageLayoutTwoColumnLeft | PdfWriter.PageModeUseOutlines);
                for (int i = 2; i <= n; i += 4) {
                    progressJFrame.setMessage(new StringBuffer("Carimbando página ").append(i).append(" com número de folha ").append(initialPageNumber).toString());
                    over = stamp.getOverContent(i);
                    position = getStampPosition(reader.getPageSizeWithRotation(i), stampImage);
                    stampImage.setAbsolutePosition(position[0], position[1]);
                    stampImage.setRotationDegrees(position[2]);
                    over.addImage(stampImage);
                    over.beginText();
                    over.setFontAndSize(bf, Main.FONT_SIZE_2);
                    over.showTextAligned(Element.ALIGN_CENTER, "" + initialPageNumber++, position[0] + stampImage.getPlainHeight() - Main.FONT_SIZE_2 - Integer.parseInt(values[Main.PAGE_NUMBER_Y_OFFSET]), position[1] + stampImage.getPlainWidth() - Integer.parseInt(values[Main.PAGE_NUMBER_X_OFFSET]), position[2]);
                    over.endText();
                    if (autoSign == Main.AUTO_SIGN_YES) {
                        try {
                            Image signatureImage = Image.getInstance(signatureFile.getAbsolutePath());
                            signatureImage.setAbsolutePosition(position[0] + stampImage.getPlainHeight() - signatureImage.getPlainHeight() - Float.parseFloat(values[Main.SIGNATURE_Y_OFFSET]), position[1] + stampImage.getPlainWidth() - signatureImage.getPlainWidth() - Float.parseFloat(values[Main.SIGNATURE_X_OFFSET]));
                            signatureImage.setRotationDegrees(position[2]);
                            over.addImage(signatureImage);
                        } catch (Exception ex) {
                            signed = false;
                            ex.printStackTrace();
                        }
                    }
                }
            } else if (printingWay == Main.PRINTING_WAY_1_PER_PAPER) {
                stamp.setViewerPreferences(PdfWriter.PageLayoutOneColumn | PdfWriter.PageModeUseOutlines);
                progressJFrame.reset();
                progressJFrame.setLengthOfTask((n == 0 ? 3 : n + 2));
                for (int i = 1; i <= n; i++) {
                    progressJFrame.setMessage(new StringBuffer("Carimbando página ").append(i).append(" com número de folha ").append(initialPageNumber).toString());
                    over = stamp.getOverContent(i);
                    position = getStampPosition(reader.getPageSizeWithRotation(i), stampImage);
                    stampImage.setAbsolutePosition(position[0], position[1]);
                    stampImage.setRotationDegrees(position[2]);
                    over.addImage(stampImage);
                    int rotation = reader.getPageSizeWithRotation(i).getRotation();
                    over.moveTo(0, reader.getPageSizeWithRotation(i).getHeight() / 2f);
                    over.lineTo(10, reader.getPageSizeWithRotation(i).getHeight() / 2f);
                    over.stroke();
                    over.beginText();
                    over.setFontAndSize(bf, Main.FONT_SIZE);
                    over.showTextAligned(Element.ALIGN_CENTER, "" + initialPageNumber++, position[0] + Integer.parseInt(values[Main.PAGE_NUMBER_X_OFFSET]) * Main.IMAGE_SCALE, position[1] + stampImage.getPlainHeight() - Main.FONT_SIZE - Integer.parseInt(values[Main.PAGE_NUMBER_Y_OFFSET]) * Main.IMAGE_SCALE, position[2]);
                    over.endText();

                    if (autoSign == Main.AUTO_SIGN_YES) {
                        try {
                            Image signatureImage = Image.getInstance(signatureFile.getAbsolutePath());
                            signatureImage.scalePercent(Main.IMAGE_SCALE * 100f);
                            signatureImage.setAbsolutePosition(position[0] + Float.parseFloat(values[Main.SIGNATURE_X_OFFSET]) * Main.IMAGE_SCALE, position[1] + stampImage.getPlainHeight() - signatureImage.getPlainHeight() - Float.parseFloat(values[Main.SIGNATURE_Y_OFFSET]) * Main.IMAGE_SCALE);
                            signatureImage.setRotationDegrees(position[2]);
                            over.addImage(signatureImage);
                        } catch (Exception ex) {
                            signed = false;
                            ex.printStackTrace();
                        }
                    }
                }
            } else if (printingWay == Main.PRINTING_WAY_1_PER_PAPER_BLANK_BACK) {
                stamp.setViewerPreferences(PdfWriter.PageLayoutOneColumn | PdfWriter.PageModeUseOutlines);
                progressJFrame.reset();
                progressJFrame.setLengthOfTask((n == 0 ? 3 : n + 2));
                for (int i = 1; i <= n; i++) {
                    progressJFrame.setMessage(new StringBuffer("Carimbando página ").append(i).append(" com número de folha ").append(initialPageNumber).toString());
                    over = stamp.getOverContent(i);
                    position = getStampPosition(reader.getPageSizeWithRotation(i), stampImage);
                    stampImage.setAbsolutePosition(position[0], position[1]);
                    stampImage.setRotationDegrees(position[2]);
                    over.addImage(stampImage);
                    int rotation = reader.getPageSizeWithRotation(i).getRotation();
                    over.moveTo(0, reader.getPageSizeWithRotation(i).getHeight() / 2f);
                    over.lineTo(10, reader.getPageSizeWithRotation(i).getHeight() / 2f);
                    over.stroke();
                    over.beginText();
                    over.setFontAndSize(bf, Main.FONT_SIZE);
                    over.showTextAligned(Element.ALIGN_CENTER, "" + initialPageNumber++, position[0] + Integer.parseInt(values[Main.PAGE_NUMBER_X_OFFSET]) * Main.IMAGE_SCALE, position[1] + stampImage.getPlainHeight() - Main.FONT_SIZE - Integer.parseInt(values[Main.PAGE_NUMBER_Y_OFFSET]) * Main.IMAGE_SCALE, position[2]);
                    over.endText();

                    if (autoSign == Main.AUTO_SIGN_YES) {
                        try {
                            Image signatureImage = Image.getInstance(signatureFile.getAbsolutePath());
                            signatureImage.scalePercent(Main.IMAGE_SCALE * 100f);
                            signatureImage.setAbsolutePosition(position[0] + Float.parseFloat(values[Main.SIGNATURE_X_OFFSET]) * Main.IMAGE_SCALE, position[1] + stampImage.getPlainHeight() - signatureImage.getPlainHeight() - Float.parseFloat(values[Main.SIGNATURE_Y_OFFSET]) * Main.IMAGE_SCALE);
                            signatureImage.setRotationDegrees(position[2]);
                            over.addImage(signatureImage);
                        } catch (Exception ex) {
                            signed = false;
                            ex.printStackTrace();
                        }
                    }
                    
                    stamp.insertPage(i + 1, PageSize.A4);
                    n++;
                    i++;
                    double angle = Math.PI / 6d;
                    if (Math.random() > 0.5d) {
                        angle += (Math.PI / 18d * Math.random());
                    } else {
                        angle -= (Math.PI / 18d * Math.random());
                    }
                    Image blankImage = Image.getInstance(getClass().getResource(Main.BLANK_IMAGE));
                    blankImage.setRotation((float) angle);
                    blankImage.setAbsolutePosition(reader.getPageSizeWithRotation(i).getWidth() / 2 - (int) ((Math.cos(90d - angle) * blankImage.getPlainHeight() + Math.cos(angle) * blankImage.getPlainWidth()) / 2d), reader.getPageSizeWithRotation(i).getHeight() / 2 - (int) (Math.cos(angle) * blankImage.getPlainHeight() / 2d) - (int) (Math.sin(angle) * blankImage.getPlainWidth() / 2d));
                    stamp.getUnderContent(i).addImage(blankImage);
                    
                }
            } else if (printingWay == Main.PRINTING_WAY_2_FRONT) {
                if (n % 2 != 0) {
                    stamp.insertPage(n + 1, PageSize.A4);//página a ser carimbada com o número

                    n++;
                }
                progressJFrame.reset();
                progressJFrame.setLengthOfTask((n / 2 == 0 ? 3 : n / 2 + 2));
                stamp.setViewerPreferences(PdfWriter.PageLayoutTwoColumnLeft | PdfWriter.PageModeUseOutlines);
                for (int i = 2; i <= n; i += 2) {
                    progressJFrame.setMessage(new StringBuffer("Carimbando página ").append(i).append(" com número de folha ").append(initialPageNumber).toString());
                    over = stamp.getOverContent(i);
                    position = getStampPosition(reader.getPageSizeWithRotation(i), stampImage);
                    stampImage.setAbsolutePosition(position[0], position[1]);
                    stampImage.setRotationDegrees(position[2]);
                    over.addImage(stampImage);
                    over.beginText();
                    over.setFontAndSize(bf, Main.FONT_SIZE_2);
                    over.showTextAligned(Element.ALIGN_CENTER, "" + initialPageNumber++, position[0] + stampImage.getPlainHeight() - Main.FONT_SIZE_2 - Integer.parseInt(values[Main.PAGE_NUMBER_Y_OFFSET]), position[1] + stampImage.getPlainWidth() - Integer.parseInt(values[Main.PAGE_NUMBER_X_OFFSET]), position[2]);
                    over.endText();
                    if (autoSign == Main.AUTO_SIGN_YES) {
                        try {
                            Image signatureImage = Image.getInstance(signatureFile.getAbsolutePath());
                            signatureImage.setAbsolutePosition(position[0] + stampImage.getPlainHeight() - signatureImage.getPlainHeight() - Float.parseFloat(values[Main.SIGNATURE_Y_OFFSET]), position[1] + stampImage.getPlainWidth() - signatureImage.getPlainWidth() - Float.parseFloat(values[Main.SIGNATURE_X_OFFSET]));
                            signatureImage.setRotationDegrees(position[2]);
                            over.addImage(signatureImage);
                        } catch (Exception ex) {
                            signed = false;
                            ex.printStackTrace();
                        }
                    }
                }
            }

            if (autoSign == Main.AUTO_SIGN_YES & signed == false) {
                JOptionPane.showMessageDialog(null, new StringBuffer("Erro ao assinar página. Certifique-se que o arquivo ").append(signatureFile.getAbsolutePath()).append(" existe e tem as dimensões: ").append(Main.SIGNATURE_WIDTH).append("x").append(Main.SIGNATURE_HEIGHT).append(" pixels.").toString(), Main.APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
            progressJFrame.setMessage(new StringBuffer("Salvando arquivo ").append(Main.NUMBERED_PDF_FILE).toString());
            stamp.close();
            progressJFrame.setMessage(new StringBuffer("Fim da criação do arquivo ").append(Main.NUMBERED_PDF_FILE).toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return numberedPDFFile;
    }

    private void prepareToFinish() {
        if (!values[Main.KEY_STORE].equals("")) {
            progressJFrame.reset();
            progressJFrame.setLengthOfTask(5);
            File keyStore = new File(values[Main.KEY_STORE]);
            if (keyStore.exists()) {
                if (values[Main.KEY_PASS].equals("") || values[Main.STORE_PASS].equals("")) {
                    PasswordJFrame passwordJFrame = new PasswordJFrame(this);
                    passwordJFrame.setKeyPass(values[Main.KEY_PASS]);
                    passwordJFrame.setStorePass(values[Main.STORE_PASS]);
                    passwordJFrame.setVisible(true);
                } else {
                    finish(values[Main.KEY_PASS], values[Main.STORE_PASS]);
                }
            } else {
                finish(values[Main.KEY_PASS], values[Main.STORE_PASS]);
            }
        } else {
            finish(values[Main.KEY_PASS], values[Main.STORE_PASS]);
        }
    }

    public void finish(String keyPass, String storePass) {

        if (autoSign == Main.AUTO_SIGN_YES && filesToCreate != Main.FILES_TO_CREATE_UNNUMBERED) {
            if (new File(values[Main.KEY_STORE]).exists()) {
                sign(keyPass, storePass);
            }
        }

        if (autoPrint == Main.AUTO_PRINT_NUMBERED || autoPrint == Main.AUTO_PRINT_BOTH) {
            printFile(new File(values[Main.SCREENS_DIR] + "\\" + Main.NUMBERED_PDF_FILE));
        }
        if (autoPrint == Main.AUTO_PRINT_UNNUMBERED || autoPrint == Main.AUTO_PRINT_BOTH) {
            printFile(new File(values[Main.SCREENS_DIR] + "\\" + Main.UNNUMBERED_PDF_FILE));
        }

        if (filesToCreate == Main.FILES_TO_CREATE_NUMBERED && autoPrint == Main.AUTO_PRINT_NUMBERED) {
            new File(values[Main.SCREENS_DIR] + "\\" + Main.UNNUMBERED_PDF_FILE).deleteOnExit();
        }
        if (filesToCreate == Main.FILES_TO_CREATE_UNNUMBERED && autoPrint == Main.AUTO_PRINT_UNNUMBERED) {
            new File(values[Main.SCREENS_DIR] + "\\" + Main.NUMBERED_PDF_FILE).deleteOnExit();
        }
        exit(0);
    }

    private void sign(String keyPass, String storePass) {
        try {
            String original = values[Main.SCREENS_DIR] + "\\" + Main.NUMBERED_PDF_FILE;
            String signed = values[Main.SCREENS_DIR] + "\\signed.pdf";
            KeyStore ks = KeyStore.getInstance("pkcs12");
            progressJFrame.setMessage("Abrindo arquivo com a chave");
            ks.load(new FileInputStream(values[Main.KEY_STORE]), storePass.toCharArray());
            String alias = (String) ks.aliases().nextElement();
            progressJFrame.setMessage("Abrindo a chave");
            PrivateKey key = (PrivateKey) ks.getKey(alias, keyPass.toCharArray());
            Certificate[] chain = ks.getCertificateChain(alias);
            PdfReader reader = new PdfReader(original);
            FileOutputStream fout = new FileOutputStream(signed);
            progressJFrame.setMessage("Assinando documento");
            PdfStamper stp = PdfStamper.createSignature(reader, fout, '\0');
            PdfSignatureAppearance sap = stp.getSignatureAppearance();
            sap.setCrypto(key, chain, null, PdfSignatureAppearance.SELF_SIGNED);
            sap.setReason("Eu sou o autor");
            sap.setLocation("Brasil");
            sap.setContact(values[Main.EMAIL]);
            sap.setSignDate(Calendar.getInstance());
            sap.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
            progressJFrame.setMessage("Salvando arquivo assinado");
            stp.close();
            new File(original).delete();
            new File(signed).renameTo(new File(original));
            progressJFrame.setMessage("Fim da assinatura");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, new StringBuffer("Problemas ao assinar o arquivo: ").append(ex.getMessage()).toString(), Main.APP_NAME, JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void exit(int status) {
        progressJFrame.exit(status);
    }

    private void deleteFiles(File[] files) {
        progressJFrame.reset();
        progressJFrame.setLengthOfTask(files.length + 1);
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
            progressJFrame.setMessage(new StringBuffer("Deletando arquivo ").append(files[i].getName()).toString());
        }
        progressJFrame.setMessage("Fim da execução");
    }

    private void printFile(File file) {
        progressJFrame.reset();
        progressJFrame.setLengthOfTask(2);
        progressJFrame.setMessage(new StringBuffer("Imprimindo o arquivo ").append(file.getName()).toString());
        StringBuffer sb = new StringBuffer("\"").append(values[Main.ACROBAT_READER]).append("\" /t \"").append(file.getAbsolutePath()).append("\"");
        if (printingWay == Main.PRINTING_WAY_1_FRONT_1_BACK) {
            sb.append(" \"").append(values[Main.PRINTER1]).append("\"");
        } else if (printingWay == Main.PRINTING_WAY_2_FRONT_2_BACK) {
            sb.append(" \"").append(values[Main.PRINTER2]).append("\"");
        } else if (printingWay == Main.PRINTING_WAY_1_PER_PAPER) {
            sb.append(" \"").append(values[Main.PRINTER3]).append("\"");
        } else if (printingWay == Main.PRINTING_WAY_2_FRONT) {
            sb.append(" \"").append(values[Main.PRINTER4]).append("\"");
        }
        try {
            if (file.exists()) {
                Runtime.getRuntime().exec(sb.toString());
            } else {
                file.createNewFile();
                Runtime.getRuntime().exec(sb.toString());
                file.delete();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, new StringBuffer("Não foi possivel enviar arquivo ").append(file.getName()).append(" para a impressora").toString(), Main.APP_NAME, JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        progressJFrame.setMessage(new StringBuffer("Terminou de imprimir o arquivo ").append(file.getName()).toString());
    }

    private float[] getStampPosition(Rectangle pageSize, Image stamp) {
        float[] position = new float[3];
        float margin = 2f;
        if (printingWay == Main.PRINTING_WAY_1_FRONT_1_BACK || printingWay == Main.PRINTING_WAY_1_PER_PAPER  || printingWay == Main.PRINTING_WAY_1_PER_PAPER_BLANK_BACK) {
            position[0] = pageSize.getRight(margin) - stamp.getPlainWidth();
            position[1] = pageSize.getTop(margin) - stamp.getPlainHeight();
            position[2] = 0;
        } else {
            position[0] = pageSize.getRight(margin) - stamp.getPlainWidth();
            position[1] = pageSize.getBottom(margin);
            position[2] = -90;
        }
        return position;
    }
}