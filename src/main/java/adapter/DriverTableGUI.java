package adapter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Observer;
import java.util.Observable;

import javax.swing.*;
import javax.swing.table.TableModel;

import domain.Driver;


public class DriverTableGUI extends JFrame{ 
	private Driver driver; 
	private JTable tabla;
	
	public DriverTableGUI(Driver driver){ 
	super(driver.getUsername()+"’s rides "); 
	this.setBounds(100, 100, 700, 200); 
	this.driver = driver; 
	
	DriverAdapter adapt = new DriverAdapter(driver); 
	tabla = new JTable(adapt); 
	tabla.setPreferredScrollableViewportSize(new Dimension(500, 70)); //Creamos un JscrollPane y le agregamos la JTable 
	JScrollPane scrollPane = new JScrollPane(tabla); 
	//Agregamos el JScrollPane al contenedor 
	getContentPane().add(scrollPane, BorderLayout.CENTER); 
	} 
}
