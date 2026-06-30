package org.example;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.example.enums.TipTranzactie;
import org.example.exceptions.*;

public class BancomatGUI{
    private BanKitService banca;

    public BancomatGUI(BanKitService banca) {
        this.banca = banca;
        deschideFereastraAutentificare();
    }

    private void deschideFereastraAutentificare(){
        JFrame frame = new JFrame("ATM BanKit - Autentificare");
        frame.setSize(350, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        frame.setLayout(new GridLayout(3, 2, 10, 10));

        JLabel lblCard = new JLabel(" Număr Card (16 cifre):");
        JTextField txtCard = new JTextField();
        
        JLabel lblPin = new JLabel(" PIN:");
        JPasswordField txtPin = new JPasswordField();
        
        JButton btnLogin = new JButton("Introduceți Cardul:");

        frame.add(lblCard);
        frame.add(txtCard);
        frame.add(lblPin);
        frame.add(txtPin);
        frame.add(new JLabel(""));
        frame.add(btnLogin);

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String numarCard = txtCard.getText().trim();
                String pin = new String(txtPin.getPassword()).trim();

                Card card = banca.cautaCardDupaNumar(numarCard);
                if (card == null) {
                    JOptionPane.showMessageDialog(frame, "Cardul nu a fost găsit.", "Eroare", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    card.verificaPin(pin);
                    frame.dispose();
                    deschideMeniuPrincipalATM(card);
                } catch (CardBlocatException ex) {
                    JOptionPane.showMessageDialog(frame, "Card blocat! Contactați banca.", "Card Blocat", JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "PIN Incorect", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void deschideMeniuPrincipalATM(Card card) {
        Account cont = card.getContSursa();
        
        String[] optiuni = {"Interogare Sold", "Retragere Numerar", "Depunere Numerar", "Schimbare PIN", "Scoate Cardul",};
        
        boolean cardInBancomat = true;

        while (cardInBancomat) {
            int alegere = JOptionPane.showOptionDialog(null, 
                    "Bine ai venit la bancomatul BanKit!\nCe operațiune dorești să efectuezi?", 
                    "Meniu Principal ATM", 
                    JOptionPane.DEFAULT_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, 
                    null, optiuni, optiuni[0]);

            switch (alegere) {
                case 0: 
                    JOptionPane.showMessageDialog(null, "Soldul disponibil este: " + cont.getSold() + " " + cont.getValuta(), "Sold Cont", JOptionPane.INFORMATION_MESSAGE);
                    break;
                    
                case 1:
                    String sumaRetragereStr = JOptionPane.showInputDialog("Introdu suma de retras:");
                    if (sumaRetragereStr != null && !sumaRetragereStr.isEmpty()) {
                        try {
                            double suma = Double.parseDouble(sumaRetragereStr);
                            if (cont.retragere(suma)) {
                                JOptionPane.showMessageDialog(null, "Te rugăm să ridici banii (" + suma + ").", "Succes", JOptionPane.INFORMATION_MESSAGE);
                                Tranzactie t = new Tranzactie(suma, TipTranzactie.RETRAGERE_NUMERAR, cont, "Retragere numerar de la ATM");
                                t.marcheazaCompletata();
                                banca.adaugaTranzactieInIstoric(t);
                            } else {
                                JOptionPane.showMessageDialog(null, "Fonduri insuficiente!", "Eroare", JOptionPane.ERROR_MESSAGE);
                                Tranzactie tEsec = new Tranzactie(suma, TipTranzactie.RETRAGERE_NUMERAR, cont, "[RESPINS]: fonduri insuficiente");
                                tEsec.anuleazaTranzactie();
                                banca.adaugaTranzactieInIstoric(tEsec);
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, "Sumă invalidă!", "Eroare", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    break;
                    
                case 2: 
                    String sumaDepunereStr = JOptionPane.showInputDialog("Introdu suma de depus:");
                    if (sumaDepunereStr != null && !sumaDepunereStr.isEmpty()) {
                        try {
                            double suma = Double.parseDouble(sumaDepunereStr);
                            cont.depunere(suma);
                            JOptionPane.showMessageDialog(null, "Suma de " + suma + " a fost adăugată cu succes.", "Succes", JOptionPane.INFORMATION_MESSAGE);
                            Tranzactie t = new Tranzactie(suma, TipTranzactie.DEPUNERE, cont, "Depunere numerar la ATM");
                            t.marcheazaCompletata();
                            banca.adaugaTranzactieInIstoric(t);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, "Sumă invalidă!", "Eroare", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    break;
                    
                case 3:
                    String pinVechi = JOptionPane.showInputDialog("Introdu PIN-ul curent: ");
                    String nouPin = JOptionPane.showInputDialog("Introdu noul PIN (4 cifre): ");
                    if (pinVechi != null && nouPin != null) {
                        try {
                            banca.schimbaPinCard(card.getNumarCard(), pinVechi, nouPin, card.getContSursa().getTitular());
                            JOptionPane.showMessageDialog(null, "PIN-ul a fost schimbat cu succes!", "Succes", JOptionPane.INFORMATION_MESSAGE);
                        } catch (CardBlocatException ex) {
                            JOptionPane.showMessageDialog(null, "Card blocat după 3 tentative greșite! Contactați banca.", "Card Blocat", JOptionPane.ERROR_MESSAGE);
                            cardInBancomat = false;
                        } catch (BanKitException | IllegalArgumentException ex) {
                            JOptionPane.showMessageDialog(null, ex.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    break;
                case 4:
                case JOptionPane.CLOSED_OPTION:
                    JOptionPane.showMessageDialog(null, "Nu uita să iei cardul. La revedere!");
                    cardInBancomat = false;
                    break;
            }
        }
    }
}