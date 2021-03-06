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
 * PasswordJFrame.java
 *
 * Created on 10 de Julho de 2007, 12:57
 */

package signature;

import main.Main;
import main.Task;

/**
 *
 * @author  danieloliveira
 */
public class PasswordJFrame extends javax.swing.JFrame {
    
    private Task task;
    
    /** Creates new form PasswordJFrame */
    public PasswordJFrame(Task task) {
        this.task = task;
        initComponents();
        setLocationRelativeTo(null);
    }
    
    public void setKeyPass(String keyPass){
        keyPassJPasswordField.setText(keyPass);
    }
    
    public void setStorePass(String storePass){
        storePassJPasswordField.setText(storePass);
    }
    
    public String getKeyPass(){
        return new String(keyPassJPasswordField.getPassword());
    }
    
    public String getStorePass(){
        return new String(storePassJPasswordField.getPassword());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" C�digo Gerado ">//GEN-BEGIN:initComponents
    private void initComponents() {
        keyPassJLabel = new javax.swing.JLabel();
        storePassJLabel = new javax.swing.JLabel();
        keyPassJPasswordField = new javax.swing.JPasswordField();
        storePassJPasswordField = new javax.swing.JPasswordField();
        okJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(Main.APP_NAME);
        keyPassJLabel.setText("Senha da chave");

        storePassJLabel.setText("Senha do arquivo da chave");

        okJButton.setText("Ok");
        okJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okJButtonActionPerformed(evt);
            }
        });

        cancelJButton.setText("Cancelar");
        cancelJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(keyPassJLabel)
                            .addComponent(storePassJLabel))
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(storePassJPasswordField)
                            .addComponent(keyPassJPasswordField, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cancelJButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
                            .addComponent(okJButton, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keyPassJLabel)
                    .addComponent(keyPassJPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(storePassJLabel)
                    .addComponent(storePassJPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(okJButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelJButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void cancelJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
        task.exit(0);
    }//GEN-LAST:event_cancelJButtonActionPerformed
    
    private void okJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okJButtonActionPerformed
        task.finish(getKeyPass(),getStorePass());
        task.exit(0);
    }//GEN-LAST:event_okJButtonActionPerformed
    
    // Declara��o de vari�veis - n�o modifique//GEN-BEGIN:variables
    private javax.swing.JButton cancelJButton;
    private javax.swing.JLabel keyPassJLabel;
    private javax.swing.JPasswordField keyPassJPasswordField;
    private javax.swing.JButton okJButton;
    private javax.swing.JLabel storePassJLabel;
    private javax.swing.JPasswordField storePassJPasswordField;
    // Fim da declara��o de vari�veis//GEN-END:variables
    
}
