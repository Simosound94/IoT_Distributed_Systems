package it.cipi.esercitazione.VirtualObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.google.gson.Gson;

import it.cipi.esercitazione.CameraInfo;
import net.spreadsheetspace.sdk.Sdk;
import net.spreadsheetspace.sdk.StatusCode;
import net.spreadsheetspace.sdk.model.AddressBookDescriptor;
import net.spreadsheetspace.sdk.model.ChangeRecipientDescriptor;
import net.spreadsheetspace.sdk.model.Contact;
import net.spreadsheetspace.sdk.model.GenerateKeyDescriptor;
import net.spreadsheetspace.sdk.model.ListViewDescriptor;
import net.spreadsheetspace.sdk.model.ValuesViewDescriptor;
import net.spreadsheetspace.sdk.model.ViewDescriptor;

public class SSSConnection implements Runnable {
	static String server = "https://www.spreadsheetspace.net";
	static String username = "simonemerello@hotmail.it";//username di spread
	static String password = "123stella";//psw di spread tua
	
	static String recipient1 = "simonemerello@hotmail.it";//un tua mail
	static String recipient2 = "g.camera@cipi.unige.it";//un altra mail

	static boolean createPublic = false;
	static boolean createPrivate = true;
	static int sleepTime = 30000;
	static boolean sleep = true;
	

	CameraInfo[] inputs;
	
	public SSSConnection(CameraInfo[]  inputs) {
		super();
		this.inputs=inputs;
	}
	
	
	@Override
	public void run() {
		
		this.UpdateSSSData(inputs);

	}
	private void UpdateSSSData(CameraInfo[]  inputs) {
		
		try {
			Sdk sdk = new Sdk(server, username, password);
			
			if(createPrivate) {
				privateViewExample(sdk);
			}
			
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
// TO SEE qui sotto le due funzioni sono da adattare al nostro per fare quello richiesto nelle specifiche
	public void privateViewExample(Sdk sdk) {
		try {
			String view_id = "";
			String view_server = "";
			
			System.out.println("Generazione chiavi....");
			GenerateKeyDescriptor generateKeyDescriptor = sdk.generateKey();
			String privateKey = generateKeyDescriptor.getPrivateKey();
			//System.out.println(privateKey);
			//System.out.println("Chiave generata e salvata");
			
			int cols = 6;
			int rows = inputs.length;
			String [][] table = new String[rows][cols];
			for(int i= 0; i<rows; i++) {
				table[i][0] = inputs[i].getId();
				table[i][1] = inputs[i].getName();
				table[i][2] = inputs[i].getUrl();
				table[i][3] = inputs[i].getX();
				table[i][4] = inputs[i].getY();
				table[i][5] = inputs[i].img;
			}
			
			LinkedList<String> listRecipients = new LinkedList<String>();
			listRecipients.add(recipient1);
			Set<String> recipients = new HashSet<String>(listRecipients);
			String excel_template = System.getProperty("user.dir") + "/template1.xlsx";
			System.out.print("Folder template: "+System.getProperty("user.dir"));
			//System.out.println("\nCreazione vista privata....");
			ViewDescriptor viewDescriptor = sdk.createPrivateView("Cameras Info", recipients, table, excel_template, false, false, rows, cols);
			
			if(viewDescriptor.getStatusCode() == StatusCode.OK) {
				System.out.println("Vista privata creata");
				view_id = viewDescriptor.getViewId();
				view_server = viewDescriptor.getViewServer();
				ValuesViewDescriptor valuesViewDescriptor = sdk.getValuesView(view_id, view_server, privateKey);
				Object[][] values = valuesViewDescriptor.getValues();
				
				/*
				table = new String[rows][cols];
				table[0][0] = "1";
				table[0][1] = "2";
				table[1][0] = "3";
				table[1][1] = "4";
				
				excel_template = System.getProperty("user.dir") + "/template2.xlsx";
				
				if(sleep) {
					System.out.println("Sleep....");
					Thread.sleep(sleepTime);
				}
				System.out.println("\nAggiunta destinatari...." + recipient2);
				LinkedList<String> addListRecipients = new LinkedList<String>();
				addListRecipients.add(recipient2);
				*/
				
				
				listRecipients.add(recipient2);
				ChangeRecipientDescriptor changeRecipientAddDescriptor = sdk.addRecipients(view_id, view_server, listRecipients);
				if(changeRecipientAddDescriptor.getStatusCode() == StatusCode.OK) {
					System.out.println("Destinatari aggiunti.");
				} else {
					System.out.println("Errore nell'aggiunta dei destinatari: ");
					for(int i=0; i< changeRecipientAddDescriptor.getMessage().size(); i++) {
						System.out.println(changeRecipientAddDescriptor.getMessage().get(i));
					}
				}
				
				System.out.println("\nUpdate vista privata....");
				ViewDescriptor viewDescriptorUpdate;
				int attempts = 0;
				/*
				 * Do-While necessario perche' e' possibile che la vista a cui si sta facendo l'update sia stata aggiornata da Excel. In questo modo
				 * controllo se il numero di versione che ho memorizzato e' l'ultimo a disposizione o se e' necessario farmi restituire l'ultimo
				 * disponibile dal server.
				*/
				do {
					viewDescriptorUpdate = sdk.updateView(viewDescriptor, table, excel_template);
					
					if(viewDescriptorUpdate.getStatusCode() == StatusCode.WRONG_NEXT_NUMBER) {
						viewDescriptor.setNextAvailableSequenceNumber(viewDescriptorUpdate.getNextAvailableSequenceNumber());
					}
					attempts++;
				} while(viewDescriptorUpdate.getStatusCode() != StatusCode.OK && attempts < 5);
				
	
				if(viewDescriptorUpdate.getStatusCode() == StatusCode.OK) {
					System.out.println("Update della vista effettuato");
					
					ValuesViewDescriptor valuesViewDescriptorUpdate = sdk.getValuesView(view_id, view_server, privateKey);
					values = valuesViewDescriptorUpdate.getValues();
					
					int row = values.length;
					int col = values[0].length;
					for (int i=0; i<row; i++) {
						for (int j=0;j<col; j++) {
							int min =Math.min(10, ((String) values[i][j]).length());
							System.out.println("(" + i + ", " + j + "): " + ((String) values[i][j]).substring(0,min)); 
						}
					}
					
					
					/*
					if(sleep) {
						System.out.println("Sleep....");
						Thread.sleep(sleepTime);
					}
					
					System.out.println("\nRimozione destinatari...." + recipient2);
					LinkedList<String> deleteListRecipients = new LinkedList<String>();
					deleteListRecipients.add(recipient2);
					
					ChangeRecipientDescriptor changeRecipientDeleteDescriptor = sdk.removeRecipients(view_id, view_server, deleteListRecipients);
					if(changeRecipientDeleteDescriptor.getStatusCode() == StatusCode.OK) {
						System.out.println("Destinatari rimossi.");
					} else {
						System.out.println("Errore nella rimozione dei destinatari.");
						for(int i=0; i< changeRecipientDeleteDescriptor.getMessage().size(); i++) {
							System.out.println(changeRecipientDeleteDescriptor.getMessage().get(i));
						}
					}
					
					if(sleep) {
						System.out.println("Sleep....");
						Thread.sleep(sleepTime);
					}
					
					System.out.println("\nEliminazione vista...");
					ViewDescriptor viewDescriptorDelete = sdk.deleteView(view_id, view_server);
					if(viewDescriptorDelete.getStatusCode() == StatusCode.OK) {
						System.out.println("Eliminazione effettuata");
					} else {
						System.out.println("Errore nell'eliminazione della vista");
						System.out.println(viewDescriptorDelete.getMessage());
						for(int i=0; i< viewDescriptorDelete.getMessages().size(); i++) {
							System.out.println(viewDescriptorDelete.getMessages().get(i));
						}
					}
					*/
				} else {
					System.out.println("Errore nell'update della vista");
					System.out.println(viewDescriptorUpdate.getMessage());
					for(int i=0; i< viewDescriptorUpdate.getMessages().size(); i++) {
						System.out.println(viewDescriptorUpdate.getMessages().get(i));
					}
				}
			} else {
				System.out.println("Errore nella creazione della vista");
				System.out.println(viewDescriptor.getMessage());
				for(int i=0; i< viewDescriptor.getMessages().size(); i++) {
					System.out.println(viewDescriptor.getMessages().get(i));
				}
			}
			
			/*
			System.out.println("\nRichiesta contatti...");
			AddressBookDescriptor addressBookDescriptor = sdk.getAddressBook();
			if(addressBookDescriptor.getStatusCode() == StatusCode.OK) {
				System.out.println("Contatti ricevuti.");
				Contact[] contacts = addressBookDescriptor.getListContact();
				
				for (int i = 0; i < contacts.length; i++) {
	                System.out.println(contacts[i].getFirstName() + ", " + contacts[i].getLastName() + " - " + contacts[i].getEmailAddress());
	            }
			} else {
				System.out.println("Errore nella richiesta dei contatti.");
			}
			
			System.out.println("\nRichiesta owned view...");
			ListViewDescriptor listOwnedDescriptor = sdk.getOwnedView();
			if(listOwnedDescriptor.getStatusCode() == StatusCode.OK) {
				System.out.println("Owned view ricevute.");
				for(int i=0; i<listOwnedDescriptor.getListView().size(); i++) {
					System.out.println(listOwnedDescriptor.getListView().get(i).getDescription() + " - " + listOwnedDescriptor.getListView().get(i).getOwner());;
				}
			} else {
				System.out.println("Errore nella richiesta delle owned view.");
			}
			
			System.out.println("\nRichiesta inbox view...");
			ListViewDescriptor listInboxDescriptor = sdk.getInboxView();
			if(listInboxDescriptor.getStatusCode() == StatusCode.OK) {
				System.out.println("Inbox view ricevute.");
				for(int i=0; i<listInboxDescriptor.getListView().size(); i++) {
					System.out.println(listInboxDescriptor.getListView().get(i).getDescription() + " - " + listInboxDescriptor.getListView().get(i).getOwner());;
				}
			} else {
				System.out.println("Errore nella richiesta delle inbox view.");
			}
			*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
