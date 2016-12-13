package simplechatclient;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import simplechatserver.StandardParameters;

class ClientView extends JFrame implements Observer, StandardParameters {

	private static final long serialVersionUID = 1L;
	private ClientNetworkHandler networkHandler;
    private JTextPane textPane;
    private Document doc;
    private JTextArea textArea;

    public static void main(String[] args){
		if (args.length != 2) {
			new ClientView(HOST, PORT);
		} else {
			new ClientView(args[0], Integer.parseInt(args[1]));
		}
    }
    
    public ClientView(String serverAddress, int serverPort) {
        super("Chat Client");
        this.networkHandler = new ClientNetworkHandler(serverAddress, serverPort, this);
        Container pane = getContentPane();
        pane.setLayout(new BorderLayout());
        textPane = new JTextPane();
        textPane.setBorder(new TitledBorder("Messages"));
        textPane.setEditable(false);
        doc = textPane.getDocument();
        try {
            doc.insertString(doc.getLength(), "Waiting for Messages\n", null);
        } catch (BadLocationException e) {;}
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(300, 500));
        pane.add(scrollPane, BorderLayout.CENTER);

        textArea = new JTextArea();
        textArea.setBorder(new TitledBorder("Your Message"));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    // Enter/Return Key pressed-> send message
                    try {
						ClientView.this.networkHandler.sendMessage(
						        ClientView.this.textArea.getText());
					} catch (Exception e1) {
						display(e1.getMessage());
					}
                    ClientView.this.textArea.setText("");  // clear TextField
                }
            }
        });
        scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(300, 100));
        pane.add(scrollPane, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ClientView.this.networkHandler.stop();
                ClientView.this.dispose();
                System.exit(0);
            }
        });
        pack();
        setVisible(true);
        new Thread(networkHandler).start();
        textArea.requestFocus();;
        textArea.setCaretPosition(0);
        networkHandler.addObserver(this);
        update(networkHandler, null);
    }



    /*
     * This method is called when notified by the Observable
     * and will get the message from NetworkHandler and append it to 
     * the messages area. 
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable model, Object arg) {
        final String message = networkHandler.getMessage();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    ClientView.this.doc.insertString(
                            ClientView.this.doc.getLength(),
                            message,
                            null);
                } catch (BadLocationException e) {;}
            }
        });
    }

	public void display(String str) {
		final String message = str;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	if (ClientView.this.doc == null) return;
                try {
                    ClientView.this.doc.insertString(
                            ClientView.this.doc.getLength(),
                            message,
                            null);
                } catch (BadLocationException e) {;}
            }
        });
	}

	public void stop() {
		this.dispose();
	}
}