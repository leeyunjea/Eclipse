import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

import javax.swing.*;

public class Server extends JFrame {
	private JTextArea ta = new JTextArea();
	private JTextField tf = new JTextField();
	
	public Server() {
		setTitle("Server");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container c = getContentPane();
		c.add(new JScrollPane(ta), BorderLayout.CENTER);
		c.add(tf, BorderLayout.SOUTH);
		
		setSize(300, 300);
		setVisible(true);
		setup();
	}
	
	public void setup() {
		new ServerThread().start();
	}
	
	public void handleError(String text) {
		System.out.println(text);
	}
	
	class ServerThread extends Thread {
		ServerSocket listener = null;
		Socket socket = null;
		
		public void run() {
			try {
				listener = new ServerSocket(9995);
			} catch (IOException e1) {
				handleError(e1.getMessage());
			}
			while(true) {
				try {
					socket = listener.accept();
					ta.append("연결되었습니다.\n");
					Thread service = new Thread(new ServiceThread(socket));
					service.start();
				} catch (IOException e) {
					handleError(e.getMessage());
				}
			}
		}
	}
	
	class SendThread extends Thread {
		
		Socket socket = null;
		BufferedWriter out = null;
		
		public SendThread(Socket socket) {
			this.socket = socket;
			try {
				out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			} catch (IOException e) {
				handleError(e.getMessage());
			}
		}
		
		synchronized public void run() {
			while(true) {
				try {
					wait();
				} catch (InterruptedException e) {
					handleError(e.getMessage());
				}
				try {
					out.write(tf.getText() + "\n");
					ta.append(tf.getText() + "\n");
					tf.setText("");
					out.flush();
				} catch (IOException e1) {
					handleError(e1.getMessage());
				}
			}
		}
		synchronized public void resend() {
			notify();
		}
	}
	
	
	class ServiceThread extends Thread {
		
		Socket socket = null;
		BufferedReader in = null;
		SendThread send;
		
		public ServiceThread(Socket socket) {
			this.socket = socket;
			send = new SendThread(socket);
			send.start();
			tf.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					send.resend();
				}
			});
		}
		
		public void run() {
			try {
				
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
			} catch (IOException e) {
				handleError(e.getMessage());
			}
			while(true) {
				try {
					String input = in.readLine();
					ta.append(input + "\n");
					int pos = ta.getText().length();
					ta.setCaretPosition(pos);
				} catch (IOException e) {
					handleError(e.getMessage());
				}
			}
		}
		
	}
	
	public static void main(String[] args) {
		new Server();
	}

}
